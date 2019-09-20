package com.youzan.mobile.enjoydependence.auto

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 发布所有lib
 */
class AutoPublishAllPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (project.name != "app") {
            return
        }
        AutoPublishExt autoPublishExt = project.extensions.create("autoPublish", AutoPublishExt)
        Map<String, Project> projectMap = new HashMap<String, Project>()
        project.afterEvaluate {
            project.rootProject.subprojects.each { pro ->
                if (pro.name != "app" && pro.name != "enjoydependence" && pro.name != "modules") {
                    projectMap.put(pro.name, pro)
                }
            }

            project.getTasks().create("AutoPublishAll", AutoPublishAllTask.class).doLast {
                projectMap.each {key, value ->
                    def command = "../gradlew :modules:${key}:${autoPublishExt.dependsOn} -x lint --daemon"
                    project.exec { execSpec ->
                        //配置闭包的内容
                        executable 'bash'
                        args '-c', command
                    }
                }
            }.doFirst {
                println("-------------------start publish all--------------------")
            }
        }
    }
}