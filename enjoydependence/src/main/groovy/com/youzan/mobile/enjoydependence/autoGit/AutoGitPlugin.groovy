package com.youzan.mobile.enjoydependence.autoGit

import groovy.json.JsonSlurper
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
            def mrResult
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
                mrResult = HttpBuilder.configure {
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
                }
                println "Your request id is (${mrResult.id}) & iid is (${mrResult.iid})."
            }.doLast {
                println("-----------------auto accept mr----------------")
            }.doLast {
                def acceptMr = HttpBuilder.configure {
                    request.uri = "http://gitlab.qima-inc.com/api/v4/projects/${autoGitExt.projectId}/merge_requests/${mrResult.iid}/merge"
                    request.contentType = JSON[0]
                    response.parser('application/json') { config, resp ->
                        new JsonSlurper().parse(resp.inputStream)
                    }
                    request.headers['PRIVATE-TOKEN'] = "${autoGitExt.token}"
                }.put()
            }.doLast {
                println("-----------------auto accept mr over----------------")
            }.doLast {
                println("-----------------auto build start, app version is ${autoGitExt.version}----------------")
            }

            project.getTasks().create("autoSetVersion", AutoSetVersion.class).doLast {
                println("-----------------auto setVersion over ----------------")
            }
        }
    }
}