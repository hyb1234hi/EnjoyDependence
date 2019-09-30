package com.youzan.mobile.enjoydependence.auto

import com.youzan.mobile.enjoydependence.DependenceResolveExt
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
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
            if (project.getTasks().findByName("publish") && project.getTasks().find {
                autoPublishExt.dependsOn
            }) {
                project.getTasks().create("WriteVersion", WriteVersionTask.class)
                project.getTasks().create("AutoPublish", AutoPublishTask.class).dependsOn([autoPublishExt.dependsOn])
                project.getTasks().find { autoPublishExt.dependsOn }.finalizedBy("publish")
                project.getTasks().find { "publish" }.finalizedBy("WriteVersion")

                project.getGradle().addListener(new TaskExecutionListener() {
                    @Override
                    void beforeExecute(Task task) {

                    }

                    @Override
                    void afterExecute(Task task, TaskState taskState) {
                        if (task.name == autoPublishExt.dependsOn && task.state.getFailure() != null) {
                            taskState.rethrowFailure()
                        }
                    }
                })
            }
        }
    }
}