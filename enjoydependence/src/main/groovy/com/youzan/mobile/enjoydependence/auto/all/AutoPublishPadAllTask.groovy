package com.youzan.mobile.enjoydependence.auto.all

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import sun.tools.jar.CommandLine

/**
 * 自动发布所有的module,仅发布pad
 */
class AutoPublishPadAllTask extends DefaultTask {

    def defaultVersion = ""
    def flavor = ""
    boolean ignore = false
    Map<String, Project> projectMap = new HashMap<String, Project>()

    @TaskAction
    void publishAll() {
        AutoPublishAllExt autoPublishAllExt = project.extensions.findByType(AutoPublishAllExt.class)
        if (project.hasProperty("ignore") && project.ignore != "unspecified") {
            ignore = Boolean.valueOf(project.ignore)
        }
        project.rootProject.subprojects.each { pro ->
            if (ignore) {
                if (!autoPublishAllExt.excludeModules.contains(pro.name)) {
                    projectMap.put(pro.name, pro)
                }
            } else {
                if (!autoPublishAllExt.excludeModules.contains(pro.name) && gitDiffModule().contains(pro.name)) {
                    projectMap.put(pro.name, pro)
                }
            }
        }
        if (project.hasProperty("version") && project.version != "unspecified") {
            defaultVersion = project.version
        }
        if (project.hasProperty("flavor") && project.flavor != "unspecified") {
            flavor = project.flavor
        }

        projectMap.each { key, value ->
            if (autoPublishAllExt.firstPriority.contains(key)) {
                println("---------------AutoBuild FirstPriority ${key}----------------")
                def command = "../gradlew :modules:${key}:${autoPublishAllExt.padCommand} -x lint --daemon"
                if (defaultVersion != "" && flavor != "") {
                    command = "../gradlew :modules:${key}:${autoPublishAllExt.padCommand} -Pversion=${defaultVersion} -Pflavor=${flavor} -x lint --daemon"
                } else if (defaultVersion != "") {
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
                if (defaultVersion != "" && flavor != "") {
                    command = "../gradlew :modules:${key}:${autoPublishAllExt.padCommand} -Pversion=${defaultVersion} -Pflavor=${flavor} -x lint --daemon"
                } else if (defaultVersion != "") {
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
                if (defaultVersion != "" && flavor != "") {
                    command = "../gradlew :modules:${key}:${autoPublishAllExt.padCommand} -Pversion=${defaultVersion} -Pflavor=${flavor} -x lint --daemon"
                } else if (defaultVersion != "") {
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
            if (!autoPublishAllExt.firstPriority.contains(key) && !autoPublishAllExt.secondPriority.contains(key) && !autoPublishAllExt.thirdPriority.contains(key)) {
                println("---------------AutoBuild OtherPriority ${key}----------------")
                def command = "../gradlew :modules:${key}:${autoPublishAllExt.padCommand} -x lint --daemon"
                if (defaultVersion != "" && flavor != "") {
                    command = "../gradlew :modules:${key}:${autoPublishAllExt.padCommand} -Pversion=${defaultVersion} -Pflavor=${flavor} -x lint --daemon"
                } else if (defaultVersion != "") {
                    command = "../gradlew :modules:${key}:${autoPublishAllExt.padCommand} -Pversion=${defaultVersion} -x lint --daemon"
                }
                project.exec { execSpec ->
                    //配置闭包的内容
                    executable 'bash'
                    args '-c', command
                }
            }
        }
    }

    @Override
    String getGroup() {
        return "CustomAuto"
    }

    @Override
    String getDescription() {
        return "auto execute the same job"
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
            if (it.contains("modules") && !it.contains("version.properties")) {
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