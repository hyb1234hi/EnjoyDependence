package com.youzan.mobile.enjoydependence.auto.all

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * 自动发布所有的module
 */
class AutoPublishPhoneTask extends DefaultTask {

    @TaskAction
    void publishAll(){

    }

    @Override
    String getGroup() {
        return "CustomAuto"
    }

    @Override
    String getDescription() {
        return "auto execute the same job"
    }
}