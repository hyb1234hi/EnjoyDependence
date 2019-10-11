package com.youzan.mobile.enjoydependence.auto.all

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

/**
 * 自动发布所有的module,仅发布phone
 */
class AutoPublishPhoneAllTask extends DefaultTask {

    def defaultVersion = ""
    def flavor = ""
    Map<String, Project> projectMap = new HashMap<String, Project>()

    @TaskAction
    void publishAll() {
        AutoPublishAllExt autoPublishAllExt = project.extensions.findByType(AutoPublishAllExt.class)
        project.rootProject.subprojects.each { pro ->
            if (!autoPublishAllExt.excludeModules.contains(pro.name) && gitDiffModule().contains(pro.name)) {
                projectMap.put(pro.name, pro)
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
                def command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -x lint --daemon"
                if (defaultVersion != "" && flavor != "") {
                    command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -Pversion=${defaultVersion} -Pflavor=${flavor} -x lint --daemon"
                } else if (defaultVersion != "") {
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
                if (defaultVersion != "" && flavor != "") {
                    command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -Pversion=${defaultVersion} -Pflavor=${flavor} -x lint --daemon"
                } else if (defaultVersion != "") {
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
                if (defaultVersion != "" && flavor != "") {
                    command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -Pversion=${defaultVersion} -Pflavor=${flavor} -x lint --daemon"
                } else if (defaultVersion != "") {
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
            if (!autoPublishAllExt.firstPriority.contains(key) && !autoPublishAllExt.secondPriority.contains(key) && !autoPublishAllExt.thirdPriority.contains(key)) {
                println("---------------AutoBuild OtherPriority ${key}----------------")
                def command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -x lint --daemon"
                if (defaultVersion != "" && flavor != "") {
                    command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -Pversion=${defaultVersion} -Pflavor=${flavor} -x lint --daemon"
                } else if (defaultVersion != "") {
                    command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -Pversion=${defaultVersion} -x lint --daemon"
                }
                project.exec { execSpec ->
                    //配置闭包的内容
                    executable 'bash'
                    args '-c', command
                }
            }
        }
        gitPush()
        getLastCommitId()
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
    def gitDiffLog() {
        try {
            File glcFile = new File(project.rootProject.projectDir.absolutePath + "/" + ".glc")//git最后一次提交sort id记录文件
            def lastCommitId = ""
            if (glcFile.exists()) {
                glcFile.withReader('UTF-8') { reader ->
                    reader.eachLine {
                        lastCommitId = it
                    }
                }
            }
            if (lastCommitId == "") {
                return ""
            } else {
                println('git diff --name-only s%'[lastCommitId].execute().text.trim() + "22222")
                return 'git diff --name-only s%'[lastCommitId].execute().text.trim()
            }
        } catch (ignored) {
            return ""
        }
    }

    //获取本次提交涉及的module
    def gitDiffModule() {
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

    static def gitPush() {
        try {
            def p = ['sh', '-c', 'git add .'].execute()
            p.waitFor()
            p = ['sh', '-c', 'git commit -m"auto write versions" '].execute()
            p.waitFor()
            p = ['sh', '-c', 'git push'].execute()
            p.waitFor()
        } catch (ignored) {
            return ""
        }
    }

    def getLastCommitId() {
        try {
            def lastCommitId = ['sh', '-c', 'git rev-parse --short HEAD'].execute().text.trim()
            File glcFile = new File(project.rootProject.projectDir.absolutePath + "/" + ".glc")//git最后一次提交sort id记录文件
            if (!glcFile.exists()) {
                glcFile.createNewFile()
            }
            glcFile.withWriter('UTF-8') { writer ->
                writer.write(lastCommitId)
            }
        } catch(ignored) {
            return ""
        }
    }
}