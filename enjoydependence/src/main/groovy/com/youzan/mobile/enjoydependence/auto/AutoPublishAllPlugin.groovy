package com.youzan.mobile.enjoydependence.auto

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
        AutoPublishExt autoPublishExt = project.extensions.create("autoPublish", AutoPublishExt)
        Map<String, Project> projectMap = new HashMap<String, Project>()
        project.afterEvaluate {
            project.rootProject.subprojects.each { pro ->
                if (!autoPublishExt.excludeModules.contains(pro.name)) {
                    projectMap.put(pro.name, pro)
                }
            }

            project.getTasks().create("AutoPublishAll", AutoPublishAllTask.class).doLast {
                projectMap.each { key, value ->
                    if (autoPublishExt.firstPriority.contains(key)) {
                        println("---------------AutoBuild FirstPriority ${key}----------------")
                        def command = "../gradlew :modules:${key}:${autoPublishExt.command} -x lint --daemon"
                        if (defaultVersion != "") {
                            command = "../gradlew :modules:${key}:${autoPublishExt.command} -Pversion=${defaultVersion} -x lint --daemon"
                        }
                        project.exec { execSpec ->
                            //配置闭包的内容
                            executable 'bash'
                            args '-c', command
                        }
                    }
                }
                projectMap.each { key, value ->
                    if (autoPublishExt.secondPriority.contains(key)) {
                        println("---------------AutoBuild SecondPriority ${key}----------------")
                        def command = "../gradlew :modules:${key}:${autoPublishExt.command} -x lint --daemon"
                        if (defaultVersion != "") {
                            command = "../gradlew :modules:${key}:${autoPublishExt.command} -Pversion=${defaultVersion} -x lint --daemon"
                        }
                        project.exec { execSpec ->
                            //配置闭包的内容
                            executable 'bash'
                            args '-c', command
                        }
                    }
                }
                projectMap.each { key, value ->
                    if (autoPublishExt.thirdPriority.contains(key)) {
                        println("---------------AutoBuild ThirdPriority ${key}----------------")
                        def command = "../gradlew :modules:${key}:${autoPublishExt.command} -x lint --daemon"
                        if (defaultVersion != "") {
                            command = "../gradlew :modules:${key}:${autoPublishExt.command} -Pversion=${defaultVersion} -x lint --daemon"
                        }
                        project.exec { execSpec ->
                            //配置闭包的内容
                            executable 'bash'
                            args '-c', command
                        }
                    }
                }
                projectMap.each { key, value ->
                    println("---------------AutoBuild OtherPriority ${key}----------------")
                    if (!autoPublishExt.firstPriority.contains(key) && !autoPublishExt.secondPriority.contains(key) && !autoPublishExt.thirdPriority.contains(key)) {
                        def command = "../gradlew :modules:${key}:${autoPublishExt.command} -x lint --daemon"
                        if (defaultVersion != "") {
                            command = "../gradlew :modules:${key}:${autoPublishExt.command} -Pversion=${defaultVersion} -x lint --daemon"
                        }
                        project.exec { execSpec ->
                            //配置闭包的内容
                            executable 'bash'
                            args '-c', command
                        }
                    }
                }
            }.doFirst {
                println("-------------------start publish all--------------------")
            }
        }
    }
}