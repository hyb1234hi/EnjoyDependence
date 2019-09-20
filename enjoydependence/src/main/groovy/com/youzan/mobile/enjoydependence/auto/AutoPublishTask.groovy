package com.youzan.mobile.enjoydependence.auto

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction

class AutoPublishTask extends DefaultTask {

    @TaskAction
    void autoPublish() {
        println("-----------------auto publish finish-------------------")
    }

    @Override
    String getGroup() {
        return "CustomAuto"
    }

    @Override
    String getDescription() {
        return "Auto Publish AAR To Maven"
    }
}