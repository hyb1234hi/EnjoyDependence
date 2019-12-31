package com.youzan.mobile.enjoydependence.autoGit.util

import com.youzan.mobile.enjoydependence.autoGit.model.AutoMessage
import groovy.json.JsonSlurper
import groovyx.net.http.FromServer
import groovyx.net.http.OkHttpBuilder
import groovyx.net.http.OkHttpEncoders

import static groovyx.net.http.MultipartContent.multipart

class AutoGitUtils {

    /**
     * 发送消息
     * @param title 标题
     * @param desc 描述
     */
    public static void sendMessage(String userEmail, String desc) {
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