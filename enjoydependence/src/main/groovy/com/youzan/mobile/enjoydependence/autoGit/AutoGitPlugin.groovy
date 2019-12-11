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

    @Override
    void apply(Project project) {
        if (project.name != "app") {
            return
        }

        AutoGitExt autoGitExt = project.extensions.create("autoGit", AutoGitExt.class)

        project.afterEvaluate {
            project.getTasks().create("autoMr", AutoCreateMrTask.class).doFirst {
                println("-----------------auto create mr----------------")
            }.doLast {
                def mrResult = HttpBuilder.configure {
                    request.uri = "http://gitlab.qima-inc.com/api/v4/projects/${autoGitExt.projectId}/merge_requests"
                    request.contentType = JSON[0]
                    response.parser('application/json') { config, resp ->
                        new JsonSlurper().parse(resp.inputStream)
                    }
                    request.headers['PRIVATE-TOKEN'] = "${autoGitExt.token}"
                }.post {
                    request.body = [
                            'source_branch': "${autoGitExt.source_branch}",
                            'target_branch': "${autoGitExt.target_branch}",
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
            }
        }
    }
}