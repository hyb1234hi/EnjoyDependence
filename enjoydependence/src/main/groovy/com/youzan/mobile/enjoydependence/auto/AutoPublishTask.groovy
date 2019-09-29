package com.youzan.mobile.enjoydependence.auto

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class AutoPublishTask extends DefaultTask {

    @TaskAction
    void autoPublish() {
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