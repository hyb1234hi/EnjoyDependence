package com.youzan.mobile.enjoydependence.autoGit

import groovy.json.JsonSlurper
import groovyx.net.http.FromServer
import groovyx.net.http.HttpBuilder
import groovyx.net.http.OkHttpBuilder
import groovyx.net.http.OkHttpEncoders
import org.gradle.api.Plugin
import org.gradle.api.Project

import static groovyx.net.http.ContentTypes.JSON
import static groovyx.net.http.MultipartContent.multipart

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

    //零售git工程
    def projectUrl = "http://gitlab.qima-inc.com/normandy-android/NewRetail/merge_requests"
    //零售HD MBD构建任务
    def newRetailHDMBDUrl = "https://mbd.qima-inc.com/#/integration/3901"
    //零售 MBD构建任务
    def newRetailMBDUrl = "https://mbd.qima-inc.com/#/integration/3900"
    //默认userEmail
    def userEmail = "liuyang_ly@youzan.com"

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
            if (project.hasProperty("userEmail") && project.userEmail != "unspecified") {
                userEmail = project.userEmail
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
                                    sendSuccessMessage("${userEmail}", "恭喜你，第一阶段成功完成，即将开始第二阶段", "成功触发构建提测包，请继续跟进","${newRetailHDMBDUrl}")
                                }
                                response.exception { t ->
                                    println("-----------------accept mr error: ${t.getMessage()}----------------")
                                    sendFailureMessage("${userEmail}", "Accept MR Failure", "merge request合并失败", "${body.web_url}")
                                    throw new RuntimeException(t)
                                }
                            }
                        } else {
                            println("-----------------body is null or iid is null----------------")
                            sendFailureMessage("${userEmail}", "Create MR Failure", "自动创建merge request失败", "${projectUrl}")
                            throw new RuntimeException("body is null or iid is null")
                        }
                    }

                    response.exception { t ->
                        println("-----------------creat mr error: ${t.getMessage()}----------------")
                        sendFailureMessage("${userEmail}", "Create MR Failure", "自动创建merge request失败", "${projectUrl}")
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

    def sendSuccessMessage(String userEmail, String title, String desc, String url) {
        sendMessage("${userEmail}", "{\"status\":\"success\",\"title\":\"${title}\",\"desc\":\"${desc}\",\"url\":\"${url}\"}")
    }

    def sendFailureMessage(String userEmail, String title, String desc, String url) {
        sendMessage("${userEmail}", "{\"status\":\"failure\",\"title\":\"${title}\",\"desc\":\"${desc}\",\"url\":\"${url}\"}")
    }

    /**
     * 发送消息
     * @param title 标题
     * @param desc 描述
     */
    private void sendMessage(String userEmail, String desc) {
        if (userEmail == null || userEmail.isEmpty()) {
            return
        }
        if (desc == null || desc.isEmpty()) {
            return
        }
        AutoMessage autoMessage = new JsonSlurper().parseText(desc)
        String msg
        if (autoMessage.status == "success") {
            msg = "[SUCCESS] 当前进度正常，请继续关注： <a href=${autoMessage.url}>${autoMessage.title}</a> , ${autoMessage.desc}"
        } else {
            msg = "[FAILURE] 构造失败，请跟进： <a href=${autoMessage.url}>${autoMessage.title}</a> , ${autoMessage.desc}"
        }

        OkHttpBuilder.configure {
            request.uri = 'http://retail.prod.qima-inc.com/wechat/notification/post'
        }.post {
            request.contentType = 'multipart/form-data'
            request.body = multipart {
                field 'touser', "${userEmail}"
                part 'content', "${msg}"
            }
            request.encoder 'multipart/form-data', OkHttpEncoders.&multipart
            response.success { FromServer fs, Object body ->
                println("-----------------send message success: ${body.desc}----------------")
            }
            response.exception { t ->
                println("-----------------send message error: ${t.getMessage()}----------------")
                throw new RuntimeException(t)
            }
        }
    }
}