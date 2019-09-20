package com.youzan.mobile.enjoydependence.auto

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import sun.tools.jar.CommandLine

/**
 * 自动发布所有的module
 */
class AutoPublishAllTask extends DefaultTask {

    @TaskAction
    void publishAll(){

    }

    @Override
    String getGroup() {
        return "CustomAuto"
    }

    @Override
    String getDescription() {
        return "publish all module aar to maven"
    }
}