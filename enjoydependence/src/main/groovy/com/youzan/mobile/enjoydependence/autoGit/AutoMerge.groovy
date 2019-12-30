package com.youzan.mobile.enjoydependence.autoGit

import groovy.json.JsonSlurper
import groovyx.net.http.FromServer
import groovyx.net.http.HttpBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static groovyx.net.http.ContentTypes.JSON

/**
 * 自动merge
 * 可以自定义source_branch/target_branch
 */
class AutoMerge extends DefaultTask {

    def source_branch = "release/latest"
    def target_branch = "dev"
    def userEmail = ""

    //零售git工程
    def projectUrl = "http://gitlab.qima-inc.com/normandy-android/NewRetail/merge_requests"

    @Override
    String getGroup() {
        return "autoGit"
    }

    @Override
    String getDescription() {
        return "auto create a merge request & accept request"
    }

    @TaskAction
    void autoMerge() {
        if (project.hasProperty("source_branch") && project.source_branch != "unspecified") {
            source_branch = project.source_branch
        }
        if (project.hasProperty("target_branch") && project.target_branch != "unspecified") {
            target_branch = project.target_branch
        }
        if (project.hasProperty("userEmail") && project.userEmail != "unspecified") {
            userEmail = project.userEmail
        }
        AutoGitExt autoGitExt = project.extensions.findByType(AutoGitExt.class)

        println(println("-----------------auto create mr s_branch:${source_branch}; t_branch:${target_branch}----------------"))
        HttpBuilder.configure {
            request.uri = "http://gitlab.qima-inc.com/api/v4/projects/${autoGitExt.projectId}/merge_requests"
            request.contentType = JSON[0]
            response.parser('application/json') { config, resp ->
                new JsonSlurper().parse(resp.inputStream)
            }
            request.headers['PRIVATE-TOKEN'] = "${autoGitExt.token}"
        }.post {
            request.body = [
                    'source_branch': "${source_branch}",
                    'target_branch': "${target_branch}",
                    'title'        : "${autoGitExt.title}",
                    'description'  : "${autoGitExt.desc}"
            ]

            response.success { FromServer fs, Object body ->
                if (body == null || body.iid == null) {
                    println("-----------------create mr failure----------------")
                    return
                }
                switch (body.merge_status) {
                    case "unchecked":
                        //如果返回此状态，说明非法merge request，需要将该mr 删除
                        println("Your mr request is unchecked, should delete")
                        HttpBuilder.configure {
                            request.uri = "http://gitlab.qima-inc.com/api/v4/projects/${autoGitExt.projectId}/merge_requests/${body.iid}"
                            request.contentType = JSON[0]
                            response.parser('application/json') { config, resp ->
                                new JsonSlurper().parse(resp.inputStream)
                            }
                            request.headers['PRIVATE-TOKEN'] = "${autoGitExt.token}"
                        }.put {
                            request.body = [
                                    'state_event': "close"
                            ]
                            response.success {
                                println("-----------------delete mr success----------------")
                            }
                            response.exception { t ->
                                println("-----------------delete mr failure----------------")
                                println(t.getMessage())
                            }
                        }
                        break
                    case "can_be_merged":
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
                            }
                            response.exception { t ->
                                println("-----------------accept mr error: ${t.getMessage()}----------------")
                                throw new RuntimeException(t)
                            }
                        }
                        break
                    default:
                        throw new RuntimeException("${body.merge_status}")
                        break
                }
            }
        }

    }
}