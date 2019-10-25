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
    boolean ignore = false
    boolean  aarBuild = false
    AutoPublishAllExt autoPublishAllExt
    Map<String, Project> projectMap = new HashMap<String, Project>()

    @TaskAction
    void publishAll() {
        autoPublishAllExt = project.extensions.findByType(AutoPublishAllExt.class)
        if (project.hasProperty("ignore") && project.ignore != "unspecified") {
            ignore = Boolean.valueOf(project.ignore)
        }
        if (project.hasProperty("version") && project.version != "unspecified") {
            defaultVersion = project.version
        }
        if (project.hasProperty("flavor") && project.flavor != "unspecified") {
            flavor = project.flavor
        }
        if (project.hasProperty("aarBuild") && project.aarBuild != "unspecified") {
            aarBuild = Boolean.valueOf(project.aarBuild)
        }
        projectMap.clear()
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

        projectMap.each { key, value ->
            println("need build modules:" + key)
        }

        projectMap.each { key, value ->
            if (autoPublishAllExt.firstPriority.contains(key)) {
                println("---------------AutoBuild FirstPriority ${key}----------------")
                def command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -PaarBuild=${aarBuild} -x lint"
                if (defaultVersion != "" && flavor != "") {
                    command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -Pversion=${defaultVersion} -Pflavor=${flavor} -PaarBuild=${aarBuild} -x lint"
                } else if (defaultVersion != "") {
                    command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -Pversion=${defaultVersion} -PaarBuild=${aarBuild} -x lint"
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
                def command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -PaarBuild=${aarBuild} -x lint"
                if (defaultVersion != "" && flavor != "") {
                    command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -Pversion=${defaultVersion} -Pflavor=${flavor} -PaarBuild=${aarBuild} -x lint"
                } else if (defaultVersion != "") {
                    command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -Pversion=${defaultVersion} -PaarBuild=${aarBuild} -x lint"
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
                def command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -PaarBuild=${aarBuild} -x lint"
                if (defaultVersion != "" && flavor != "") {
                    command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -Pversion=${defaultVersion} -Pflavor=${flavor} -PaarBuild=${aarBuild} -x lint"
                } else if (defaultVersion != "") {
                    command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -Pversion=${defaultVersion} -PaarBuild=${aarBuild} -x lint"
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
                def command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -PaarBuild=${aarBuild} -x lint"
                if (defaultVersion != "" && flavor != "") {
                    command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -Pversion=${defaultVersion} -Pflavor=${flavor} -PaarBuild=${aarBuild} -x lint"
                } else if (defaultVersion != "") {
                    command = "../gradlew :modules:${key}:${autoPublishAllExt.phoneCommand} -Pversion=${defaultVersion} -PaarBuild=${aarBuild} -x lint"
                }
                project.exec { execSpec ->
                    //配置闭包的内容
                    executable 'bash'
                    args '-c', command
                }
            }
        }

        //有文件变更的lib module 才执行git push
        if (projectMap.size() > 0) {
//            gitPush()
//            getLastCommitId()
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
            File glcFile = new File(autoPublishAllExt.glcParentPath + "/" + ".glc")
//git最后一次提交sort id记录文件
            if (glcFile.exists()) {
                glcFile.withReader('UTF-8') { reader ->
                    def lastCommitId = reader.text.trim()
                    println("---------------lastCommitId: ${lastCommitId}----------------")
                    if (lastCommitId == "") {
                        return ""
                    } else {
                        return "git diff --name-only ${lastCommitId}".execute().text.trim()
                    }
                }
            } else {
                println("---------------glcPath: ${glcFile.absolutePath}----------------")
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
        println("defaultVersion：${defaultVersion}")
        if (!defaultVersion.contains("snapshot") && !defaultVersion.contains("SNAPSHOT")) {
            println("---------------start add snapshot change----------------")
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

    def gitPush() {
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
            File glcFile = new File(autoPublishAllExt.glcParentPath + "/" + ".glc")
//git最后一次提交sort id记录文件
            if (!glcFile.exists()) {
                glcFile.createNewFile()
            }
            glcFile.withWriter('UTF-8') { writer ->
                writer.write(lastCommitId)
            }
        } catch (ignored) {
            return ""
        }
    }
}