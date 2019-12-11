package com.youzan.mobile.enjoydependence.autoGit

import groovy.json.JsonSlurper
import groovyx.net.http.FromServer
import groovyx.net.http.HttpBuilder
import groovyx.net.http.OkHttpBuilder
import groovyx.net.http.*
import static groovyx.net.http.ContentTypes.JSON
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * 自动创建git mr & 自动accept mr
 */
class AutoCreateMrTask extends DefaultTask {

    @Override
    String getGroup() {
        return "autoGit"
    }

    @Override
    String getDescription() {
        return "auto create & accept mr"
    }

    @TaskAction
    void creatMergeRequest() {
//        def mrResult = HttpBuilder.configure {
//            request.uri = 'http://gitlab.qima-inc.com/api/v4/projects/8173/merge_requests'
//            request.contentType = JSON[0]
//            response.parser('application/json') { config, resp ->
//                new JsonSlurper().parse(resp.inputStream)
//            }
//            request.headers['PRIVATE-TOKEN'] = 'z7ve3ZFQgCqtvxPktuT9'
//        }.post {
//            request.body = [
//                    'source_branch': 'feature/automated_construction_1.2.2',
//                    "target_branch": 'master', 'title': 'Auto Create MR'
//            ]
//        }
//
//        println "Your request id is (${mrResult.id}) & iid is (${mrResult.iid})."
//
//        def acceptMr = HttpBuilder.configure {
//            request.uri = "http://gitlab.qima-inc.com/api/v4/projects/8173/merge_requests/${mrResult.iid}/merge"
//            request.contentType = JSON[0]
//            response.parser('application/json') { config, resp ->
//                new JsonSlurper().parse(resp.inputStream)
//            }
//            request.headers['PRIVATE-TOKEN'] = 'z7ve3ZFQgCqtvxPktuT9'
//        }.put()
    }
}