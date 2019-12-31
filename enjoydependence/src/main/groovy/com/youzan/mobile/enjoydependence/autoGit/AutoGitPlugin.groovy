package com.youzan.mobile.enjoydependence.autoGit

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * git自动化plugin
 */
class AutoGitPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (project.name != "app") {
            return
        }

        AutoGitExt autoGitExt = project.extensions.create("autoGit", AutoGitExt.class)

        project.afterEvaluate {
            project.getTasks().create("autoSetVersion", AutoSetVersion.class).doLast {
                println("-----------------auto setVersion over ----------------")
            }
            project.getTasks().create("sendMsg", AutoSendMsg.class)
            project.getTasks().create("autoMerge", AutoMerge.class)
            project.getTasks().create("autoMergeAndTriggerMBDBuild", AutoMergeAndTriggerMBDBuild.class)
        }
    }
}