package com.youzan.mobile.enjoydependence.auto.pad

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class AutoPublishPadTask extends DefaultTask {

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