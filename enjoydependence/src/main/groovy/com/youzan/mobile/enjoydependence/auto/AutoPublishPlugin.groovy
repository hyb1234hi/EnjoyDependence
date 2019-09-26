package com.youzan.mobile.enjoydependence.auto

import org.gradle.api.Plugin
import org.gradle.api.Project


/**
 * 自动发布单个lib
 * 依赖WriteVersion、autoPublishExt.dependsOn、publish
 */
class AutoPublishPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        if (project.name == "app" || project.name == "modules" || project.name == "enjoydependence") {
            return
        }

        AutoPublishExt autoPublishExt = project.extensions.create("autoPublish", AutoPublishExt)

        project.afterEvaluate {
            if (project.getTasks().findByName("publish") && project.getTasks().find {
                autoPublishExt.dependsOn
            }) {
                project.getTasks().create("WriteVersion", WriteVersionTask.class)
                project.getTasks().create("AutoPublish", AutoPublishTask.class).dependsOn([autoPublishExt.dependsOn, "WriteVersion"])
                project.getTasks().find { autoPublishExt.dependsOn }.mustRunAfter("WriteVersion")
                project.getTasks().find { autoPublishExt.dependsOn }.finalizedBy("publish")
            }
        }
    }
}