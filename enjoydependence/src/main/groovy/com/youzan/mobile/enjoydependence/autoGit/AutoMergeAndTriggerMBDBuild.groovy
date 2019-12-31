package com.youzan.mobile.enjoydependence.autoGit

import com.youzan.mobile.enjoydependence.autoGit.util.AutoGitUtils
import groovy.json.JsonSlurper
import groovyx.net.http.FromServer
import groovyx.net.http.HttpBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.impldep.aQute.bnd.build.Run

import static groovyx.net.http.ContentTypes.JSON

/**
 * 自动merge并且触发零售APP&零售HD构建
 */
class AutoMergeAndTriggerBuild extends DefaultTask {
    def source_branch = "release/latest"
    def target_branch = "dev"
    def userEmail = ""

    //零售git工程
    def projectUrl = "http://gitlab.qima-inc.com/normandy-android/NewRetail/merge_requests"
    //零售HD MBD构建任务
    def newRetailHDMBDUrl = "https://mbd.qima-inc.com/#/integration/3901"
    //零售 MBD构建任务
    def newRetailMBDUrl = "https://mbd.qima-inc.com/#/integration/3900"

    @Override
    String getGroup() {
        return "autoGit"
    }

    @Override
    String getDescription() {
        return "auto create a merge request、 accept request & auto trigger retail mbd build"
    }

    @TaskAction
    void autoMergeAndTriggerBuild() {
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
                                    triggerBuild(autoGitExt.version)
                                    sendSuccessMessage("${userEmail}", "恭喜你，第一阶段成功完成，即将开始第二阶段", "成功触发构建提测包，请继续跟进", "${newRetailHDMBDUrl}")
                                    println("-----------------auto build start, app version is ${autoGitExt.version}----------------")
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

    def sendSuccessMessage(String userEmail, String title, String desc, String url) {
        AutoGitUtils.sendMessage("${userEmail}", "{\"status\":\"success\",\"title\":\"${title}\",\"desc\":\"${desc}\",\"url\":\"${url}\"}")
    }

    def sendFailureMessage(String userEmail, String title, String desc, String url) {
        AutoGitUtils.sendMessage("${userEmail}", "{\"status\":\"failure\",\"title\":\"${title}\",\"desc\":\"${desc}\",\"url\":\"${url}\"}")
    }

    def triggerBuild(String version) {
        try {
            def p = ['sh', '-c', "curl -X POST --data-urlencode \"version=${version}\" http://172.17.1.50:8080/view/MBD/job/mbd_trigger_build_retail_android_apub/buildWithParameters?token=token_mbd_trigger_build_retail_android_apub"].execute()
            p.waitFor()
        } catch (ignored) {
            println("-----------------trigger build error ----------------")
            throw new RuntimeException("trigger build error")
        }
    }
}