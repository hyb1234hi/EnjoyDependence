package com.youzan.mobile.enjoydependence.auto.all

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 发布所有lib
 * 按照优先级发布
 */
class AutoPublishAllPlugin implements Plugin<Project> {

    def defaultVersion = ""

    @Override
    void apply(Project project) {
        if (project.name != "app") {
            return
        }
        if (project.hasProperty("version") && project.version != "unspecified") {
            defaultVersion = project.version
        }
        AutoPublishAllExt autoPublishAllExt = project.extensions.create("autoPublishAll", AutoPublishAllExt)
        project.afterEvaluate {
            if (autoPublishAllExt.padCommand != "") {
                project.getTasks().create("AutoPublishPadAll", AutoPublishPadAllTask.class).doFirst {
                    println("-------------------start publish pad all--------------------")
                }
            }

            if (autoPublishAllExt.phoneCommand != "") {
                //全自动发布phone aar
                project.getTasks().create("AutoPublishPhoneAll", AutoPublishPhoneAllTask.class).doFirst {
                    println("-------------------start publish phone all--------------------")
                }
            }
        }
    }
}