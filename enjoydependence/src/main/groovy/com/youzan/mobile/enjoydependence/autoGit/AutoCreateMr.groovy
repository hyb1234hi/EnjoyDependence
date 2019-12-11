package com.youzan.mobile.enjoydependence.autoGit

import groovyx.net.http.HttpBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * 自动创建git mr
 */
class AutoCreateMr extends DefaultTask {

    def httpBin = HttpBuilder.configure {
        request.uri = 'http://httpbin.org/'
    }

    def result = httpBin.get {
        request.uri.path = '/get'
    }

    @TaskAction
    void creatMergeRequest() {

    }
}