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
        Map<String, Project> projectMap = new HashMap<String, Project>()
        project.afterEvaluate {
            project.rootProject.subprojects.each { pro ->
                if (!autoPublishAllExt.excludeModules.contains(pro.name) && gitDiffModule().contains(pro.name)) {
                    projectMap.put(pro.name, pro)
                }
            }

            project.getTasks().create("AutoPublishPadAll", AutoPublishPadTask.class).doLast {
                projectMap.each { key, value ->
                    if (autoPublishAllExt.firstPriority.contains(key)) {
                        println("---------------AutoBuild FirstPriority ${key}----------------")
                        def command = "../gradlew :modules:${key}:${autoPublishAllExt.padCommand} -x lint --daemon"
                        if (defaultVersion != "") {
                            command = "../gradlew :modules:${key}:${autoPublishAllExt.padCommand} -Pversion=${defaultVersion} -x lint --daemon"
                        }
                        project.exec { execSpec ->
                            //配置闭包的内容
                            executable 'bash'
                            args '-c', command
                        }
                    }
                }
                projectMap.each { key, value ->
                    if (autoPublishAllExt.secondPriority.contains(key)) {
                        println("---------------AutoBuild SecondPriority ${key}----------------")
                        def command = "../gradlew :modules:${key}:${autoPublishAllExt.padCommand} -x lint --daemon"
                        if (defaultVersion != "") {
                            command = "../gradlew :modules:${key}:${autoPublishAllExt.padCommand} -Pversion=${defaultVersion} -x lint --daemon"
                        }
                        project.exec { execSpec ->
                            //配置闭包的内容
                            executable 'bash'
                            args '-c', command
                        }
                    }
                }
                projectMap.each { key, value ->
                    if (autoPublishAllExt.thirdPriority.contains(key)) {
                        println("---------------AutoBuild ThirdPriority ${key}----------------")
                        def command = "../gradlew :modules:${key}:${autoPublishAllExt.padCommand} -x lint --daemon"
                        if (defaultVersion != "") {
                            command = "../gradlew :modules:${key}:${autoPublishAllExt.padCommand} -Pversion=${defaultVersion} -x lint --daemon"
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
                    if (!autoPublishAllExt.firstPriority.contains(key) && !autoPublishAllExt.secondPriority.contains(key) && !autoPublishAllExt.thirdPriority.contains(key)) {
                        def command = "../gradlew :modules:${key}:${autoPublishAllExt.padCommand} -x lint --daemon"
                        if (defaultVersion != "") {
                            command = "../gradlew :modules:${key}:${autoPublishAllExt.padCommand} -Pversion=${defaultVersion} -x lint --daemon"
                        }
                        project.exec { execSpec ->
                            //配置闭包的内容
                            executable 'bash'
                            args '-c', command
                        }
                    }
                }
            }.doFirst {
                println("-------------------start publish pad all--------------------")
            }

            //全自动发布phone aar
            project.getTasks().create("AutoPublishPhoneAll", AutoPublishPhoneTask.class).doLast {
                projectMap.each { key, value ->
                    if (autoPublishAllExt.firstPriority.contains(key)) {
                        println("---------------AutoBuild FirstPriority ${key}----------------")
                        def command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -x lint --daemon"
                        if (defaultVersion != "") {
                            command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -Pversion=${defaultVersion} -x lint --daemon"
                        }
                        project.exec { execSpec ->
                            //配置闭包的内容
                            executable 'bash'
                            args '-c', command
                        }
                    }
                }
                projectMap.each { key, value ->
                    if (autoPublishAllExt.secondPriority.contains(key)) {
                        println("---------------AutoBuild SecondPriority ${key}----------------")
                        def command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -x lint --daemon"
                        if (defaultVersion != "") {
                            command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -Pversion=${defaultVersion} -x lint --daemon"
                        }
                        project.exec { execSpec ->
                            //配置闭包的内容
                            executable 'bash'
                            args '-c', command
                        }
                    }
                }
                projectMap.each { key, value ->
                    if (autoPublishAllExt.thirdPriority.contains(key)) {
                        println("---------------AutoBuild ThirdPriority ${key}----------------")
                        def command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -x lint --daemon"
                        if (defaultVersion != "") {
                            command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -Pversion=${defaultVersion} -x lint --daemon"
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
                    if (!autoPublishAllExt.firstPriority.contains(key) && !autoPublishAllExt.secondPriority.contains(key) && !autoPublishAllExt.thirdPriority.contains(key)) {
                        def command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -x lint --daemon"
                        if (defaultVersion != "") {
                            command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -Pversion=${defaultVersion} -x lint --daemon"
                        }
                        project.exec { execSpec ->
                            //配置闭包的内容
                            executable 'bash'
                            args '-c', command
                        }
                    }
                }
            }.doFirst {
                println("-------------------start publish phone all--------------------")
            }

            if (project.getTasks().find { "AutoPublishPhoneAll" } && project.getTasks().find {
                "AutoPublishPadAll"
            }) {
                project.getTasks().create("AutoPublishAll", AutoPublishAllTask.class).dependsOn(["AutoPublishPhoneAll", "AutoPublishPadAll"])
                project.getTasks().find{"AutoPublishPhoneAll"}.mustRunAfter("AutoPublishPadAll")
            }
        }
    }

    //获取本次提交记录
    static def gitDiffLog() {
        try {
            return 'git diff --name-only HEAD~ HEAD'.execute().text.trim()
        }
        catch (ignored) {
            return ""
        }
    }

    //获取本次提交涉及的module
    static def gitDiffModule() {
        def changeModules = []
        gitDiffLog().eachLine {
            if (it.contains("modules")) {
                String[] temp = it.split("/")
                if (temp.size() > 2) {
                    String moduleName = temp[1]
                    if (!changeModules.contains(moduleName)) {
                        changeModules.add(moduleName)
                    }
                }
            }
        }
        return changeModules
    }
}