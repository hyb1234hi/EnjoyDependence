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
                if (gitDiffModule().size() == 1 && gitDiffModule()[0] == "-1") {
                    if (!autoPublishAllExt.excludeModules.contains(pro.name)) {
                        projectMap.put(pro.name, pro)
                    }
                } else {
                    if (!autoPublishAllExt.excludeModules.contains(pro.name) && gitDiffModule().contains(pro.name)) {
                        projectMap.put(pro.name, pro)
                    }
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
        return "enjoyDependence"
    }

    @Override
    String getDescription() {
        return "auto execute the same job"
    }

    //获取本次提交记录
    def gitDiffLog() {
        try {
            File glcFile = new File(project.rootProject.projectDir.absolutePath + "/" + ".glc")//git最后一次提交sort id记录文件
            if (glcFile.exists()) {
                glcFile.withReader('UTF-8') { reader ->
                    def lastCommitId = reader.text.trim()
                    if (lastCommitId == "") {
                        return ""
                    } else {
                        return "git diff --name-only ${lastCommitId}".execute().text.trim()
                    }
                }
            } else {
                return ""
            }
        } catch (ignored) {
            return ""
        }
    }

    //获取本次提交涉及的module
    def gitDiffModule() {
        def changeModules = []
        if (gitDiffLog() == "") {
            return ["-1"]
        }
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
        //发布release包需要先统计到snapshot包有哪些
        if (!defaultVersion.contains("snapshot") || !defaultVersion.contains("SNAPSHOT")) {
            File rootVersionFile = new File(project.rootProject.projectDir.absolutePath + "/" + "version.properties")
            if (rootVersionFile.exists()) {
                rootVersionFile.withReader('UTF-8') { reader ->
                    reader.eachLine {
                        String[] temp = it.split("=")
                        String projectName = ""
                        String tempVersion = ""
                        if (temp.size() == 2) {
                            projectName = temp[0]
                            tempVersion = temp[1]
                        }
                        if (tempVersion.contains("snapshot") || tempVersion.contains("SNAPSHOT")) {
                            if (!changeModules.contains(projectName)) {
                                changeModules.add(projectName)
                            }
                        }
                    }
                }
            }
        }
        return changeModules
    }
}