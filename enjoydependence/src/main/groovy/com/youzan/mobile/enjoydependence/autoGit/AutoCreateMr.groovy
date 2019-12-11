package com.youzan.mobile.enjoydependence.autoGit

import groovy.json.JsonSlurper
import groovyx.net.http.FromServer
import groovyx.net.http.HttpBuilder
import groovyx.net.http.OkHttpBuilder
import groovyx.net.http.*
import static groovyx.net.http.MultipartContent.multipart
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * 自动创建git mr
 */
class AutoCreateMr extends DefaultTask {

    class MergeResult {
        String id;
        String iid;
        String state;
        String title;
    }

    @TaskAction
    void creatMergeRequest() {
        def httpBin = OkHttpBuilder.configure {
            request.uri = 'http://gitlab.qima-inc.com/api/v4/projects/8173/merge_requests'
            request.headers['PRIVATE-TOKEN'] = 'z7ve3ZFQgCqtvxPktuT9'
        }

        def result = httpBin.post {
            request.contentType = 'multipart/form-data'
            request.body = multipart {
                field 'source_branch', 'feature/automated_construction_1.2.2'
                field 'target_branch','master'
                field 'title', "Auto Create MR"
            }
            request.encoder 'multipart/form-data', OkHttpEncoders.&multipart
        }

        def head = httpBin.head {
            response.success { FromServer fs, Object body ->
                println(result.toString())
            }
        }
    }
}