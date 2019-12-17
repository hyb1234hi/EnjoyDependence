package com.youzan.mobile.enjoydependence.autoGit

import groovy.json.JsonSlurper
import jdk.nashorn.internal.parser.JSONParser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import groovyx.net.http.OkHttpBuilder
import groovyx.net.http.*
import static groovyx.net.http.MultipartContent.multipart

/**
 * 在企业微信中, 发送message给对应的同事
 */
class AutoSendMsg extends DefaultTask {

    @Override
    String getGroup() {
        return "autoGit"
    }

    @Override
    String getDescription() {
        return "auto send a message to "
    }

    @TaskAction
    void sendMsg() {
        def userEmail = null
        def message = null
        if (project.hasProperty("userEmail") && project.userEmail != "unspecified") {
            userEmail = project.userEmail
        }
        if (project.hasProperty("message") && project.message != "unspecified") {
            message = project.message
        }
//        sendMessage("liuyang_ly@youzan.com", "{\"status\":\"success\",\"title\":\"构造进行中\",\"desc\":\"具体请查看MBD\",\"url\":\"www.baidu.com\"}")
        sendMessage(userEmail, message)
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