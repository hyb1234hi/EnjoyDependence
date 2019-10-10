package com.youzan.mobile.enjoydependence.auto

import com.youzan.mobile.enjoydependence.auto.pad.AutoPublishPadTask
import com.youzan.mobile.enjoydependence.auto.pad.WriteVersionPadTask
import com.youzan.mobile.enjoydependence.auto.phone.AutoPublishPhoneTask
import com.youzan.mobile.enjoydependence.auto.phone.WriteVersionPhoneTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState

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
            //自动发布pad
            if (project.getTasks().find {
                autoPublishExt.padDependOn
            }) {
                project.getTasks().create("WriteVersionPad", WriteVersionPadTask.class).dependsOn(autoPublishExt.padDependOn)
                project.getTasks().create("AutoPublishPad", AutoPublishPadTask.class).dependsOn("WriteVersionPad")

                project.getGradle().addListener(new TaskExecutionListener() {
                    @Override
                    void beforeExecute(Task task) {

                    }

                    @Override
                    void afterExecute(Task task, TaskState taskState) {
                        if (task.state.getFailure() != null) {
                            taskState.rethrowFailure()
                        }
                    }
                })
            }

            //自动发布phone
            if (project.getTasks().find {
                autoPublishExt.phoneDependOn
            }) {
                project.getTasks().create("WriteVersionPhone", WriteVersionPhoneTask.class).dependsOn(autoPublishExt.phoneDependOn)
                project.getTasks().create("AutoPublishPhone", AutoPublishPhoneTask.class).dependsOn("WriteVersionPhone")

                project.getGradle().addListener(new TaskExecutionListener() {
                    @Override
                    void beforeExecute(Task task) {

                    }

                    @Override
                    void afterExecute(Task task, TaskState taskState) {
                        if (task.state.getFailure() != null) {
                            taskState.rethrowFailure()
                        }
                    }
                })
            }
        }
    }
}