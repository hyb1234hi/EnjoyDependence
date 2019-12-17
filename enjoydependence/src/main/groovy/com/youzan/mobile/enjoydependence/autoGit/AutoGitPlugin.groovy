package com.youzan.mobile.enjoydependence.autoGit

import groovy.json.JsonSlurper
import groovyx.net.http.FromServer
import groovyx.net.http.HttpBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project

import static groovyx.net.http.ContentTypes.JSON

/**
 * git自动化plugin
 */
class AutoGitPlugin implements Plugin<Project> {

    /**
     * 默认源分支
     */
    String defSourceBranch
    /**
     * 默认目标分支
     */
    String defTargetBranch

    @Override
    void apply(Project project) {
        if (project.name != "app") {
            return
        }

        AutoGitExt autoGitExt = project.extensions.create("autoGit", AutoGitExt.class)

        project.afterEvaluate {
            defSourceBranch = autoGitExt.source_branch
            defTargetBranch = autoGitExt.target_branch
            if (project.hasProperty("source_branch") && project.source_branch != "unspecified") {
                defSourceBranch = project.source_branch
            }
            if (project.hasProperty("target_branch") && project.target_branch != "unspecified") {
                defTargetBranch = project.target_branch
            }

            project.getTasks().create("autoMr", AutoCreateMrTask.class).doFirst {
                println("-----------------auto create mr s_branch:${defSourceBranch}; t_branch:${defTargetBranch}----------------")
            }.doLast {
                HttpBuilder.configure {
                    request.uri = "http://gitlab.qima-inc.com/api/v4/projects/${autoGitExt.projectId}/merge_requests"
                    request.contentType = JSON[0]
                    response.parser('application/json') { config, resp ->
                        new JsonSlurper().parse(resp.inputStream)
                    }
                    request.headers['PRIVATE-TOKEN'] = "${autoGitExt.token}"
                }.post {
                    request.body = [
                            'source_branch': "${defSourceBranch}",
                            'target_branch': "${defTargetBranch}",
                            'title'        : "${autoGitExt.title}",
                            'description'  : "${autoGitExt.desc}"
                    ]

                    response.success { FromServer fs, Object body ->
                        if (body != null && body.iid != null) {
                            println("Your mr request id is (${body.id}) & iid is (${body.iid}).")
                            println("-----------------auto accept mr----------------")
                            HttpBuilder.configure {
                                request.uri = "http://gitlab.qima-inc.com/api/v4/projects/${autoGitExt.projectId}/merge_requests/${body.iid}/merge"
                                request.contentType = JSON[0]
                                response.parser('application/json') { config, resp ->
                                    new JsonSlurper().parse(resp.inputStream)
                                }
                                request.headers['PRIVATE-TOKEN'] = "${autoGitExt.token}"
                            }.put {
                                response.success {
                                    println("-----------------auto accept mr success----------------")
                                    triggerBuild(autoGitExt.version)
                                    println("-----------------auto accept mr over----------------")
                                }
                                response.exception { t ->
                                    println("-----------------accept mr error: ${t.getMessage()}----------------")
                                    throw new RuntimeException(t)
                                }
                            }
                        } else {
                            println("-----------------body is null or iid is null----------------")
                            throw new RuntimeException("body is null or iid is null")
                        }
                    }

                    response.exception { t ->
                        println("-----------------creat mr error: ${t.getMessage()}----------------")
                        throw new RuntimeException(t)
                    }
                }
            }.doLast {
                println("-----------------auto build start, app version is ${autoGitExt.version}----------------")
            }

            project.getTasks().create("autoSetVersion", AutoSetVersion.class).doLast {
                println("-----------------auto setVersion over ----------------")
            }

            project.getTasks().create("sendMsg", AutoSendMsg.class)
        }
    }

    def triggerBuild(String version) {
        try {
            def p = ['sh', '-c', "curl -X POST --data-urlencode \"version=${version}\" http://172.17.1.50:8080/view/MBD/job/mbd_trigger_build_retail_android_apub/buildWithParameters?token=token_mbd_trigger_build_retail_android_apub"].execute()
            p.waitFor()
        } catch (ignored) {
            println("-----------------trigger build error ----------------")
            return ""
        }
    }
}