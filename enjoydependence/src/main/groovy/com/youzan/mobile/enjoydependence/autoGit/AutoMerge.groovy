package com.youzan.mobile.enjoydependence.autoGit

import com.youzan.mobile.enjoydependence.autoGit.util.AutoGitUtils
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

    def source_branch = ""
    def target_branch = ""
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
        AutoGitExt autoGitExt = project.extensions.findByType(AutoGitExt.class)
        source_branch = autoGitExt.source_branch
        target_branch = autoGitExt.target_branch
        userEmail = autoGitExt.userEmail
        if (project.hasProperty("source_branch") && project.source_branch != "unspecified") {
            source_branch = project.source_branch
        }
        if (project.hasProperty("target_branch") && project.target_branch != "unspecified") {
            target_branch = project.target_branch
        }
        if (project.hasProperty("userEmail") && project.userEmail != "unspecified") {
            userEmail = project.userEmail
        }

        println("-----------------auto create mr s_branch:${source_branch}; t_branch:${target_branch}----------------")
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
                    throw new RuntimeException("create mr failure")
                }

                println("Your mr request id is (${body.id}) & iid is (${body.iid}).")

                HttpBuilder.configure {
                    request.uri = "http://gitlab.qima-inc.com/api/v4/projects/${autoGitExt.projectId}/merge_requests/${body.iid}/changes"
                    request.contentType = JSON[0]
                    response.parser('application/json') { config, resp ->
                        new JsonSlurper().parse(resp.inputStream)
                    }
                    request.headers['PRIVATE-TOKEN'] = "${autoGitExt.token}"
                }.get {
                    response.success { FromServer fs1, Object changesInfo ->
                        if (changesInfo.changes.size <= 0) {
                            //无更改，则不需要accept 删掉 mr即可
                            println("-----------------Your mr request changes is null, should delete----------------")
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
                        } else {
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
                                    sendFailureMessage("${userEmail}", "Accept MR Failure", "merge request合并失败", "${body.web_url}")
                                    throw new RuntimeException(t)
                                }
                            }
                        }

                    }

                    response.failure {
                        println("-----------------get mr changes info error----------------")
                        sendFailureMessage("${userEmail}", "获取MR changes info Failure", "获取changes info 失败", "${projectUrl}")
                    }
                }
            }

            response.failure {
                println("-----------------Create MR Failure----------------")
                sendFailureMessage("${userEmail}", "Create MR Failure", "自动创建merge request失败", "${projectUrl}")
                throw new RuntimeException("Create Mr Failure")
            }
        }
    }

    def sendFailureMessage(String userEmail, String title, String desc, String url) {
        AutoGitUtils.sendMessage("${userEmail}", "{\"status\":\"failure\",\"title\":\"${title}\",\"desc\":\"${desc}\",\"url\":\"${url}\"}")
    }
}