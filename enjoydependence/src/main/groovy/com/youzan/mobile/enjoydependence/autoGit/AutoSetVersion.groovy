package com.youzan.mobile.enjoydependence.autoGit

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * 脚本设置app versionName及versionCode
 * 该文件只针对有赞零售有效，不具有通用性
 */
class AutoSetVersion extends DefaultTask {

    @Override
    String getGroup() {
        return "autoGit"
    }

    @Override
    String getDescription() {
        return "auto create & accept mr"
    }

    @TaskAction
    void setVersion() {
        def version = "0"
        int versionCode
        if (project.hasProperty("version") && project.version != "unspecified") {
            version = project.version
        }
        if (version == "0") {
            return
        }
        println("-----------------auto set version: ${version}----------------")

        def parentPath = project.path.replace(":", "/")
        File buildGradle = new File(project.rootProject.projectDir.absolutePath + "/" + parentPath + "/" + "build.gradle")
        if (!buildGradle.exists()) {
            println("-----------------build.gradle is not exist----------------")
            return
        }

        //新的文本，更改老文件中的version的相关信息
        List<String> newContent = new ArrayList<>()
        buildGradle.withReader("UTF-8") { reader ->
            reader.eachLine {
                if (it.contains("versionName getVerName")) {
                    //获取到版本号那一行
                    newContent.add("            versionName getVerName(\"${version}\")")
                } else if (it.contains("versionCode")) {
                    String[] temp = it.split(" ")
                    println(temp.toString())
                    if (temp.size() >= 2) {
                        versionCode = temp[temp.length - 1].toInteger() + 1
                    }
                    newContent.add("            versionCode ${versionCode}")
                } else if (it.contains("version =")) {
                    newContent.add("    version = \"${version}\"")
                } else {
                    newContent.add(it)
                }
            }
        }

        buildGradle.withWriter("UTF-8") { writer ->
            newContent.each {
                writer.write(it + "\n")
            }
        }
    }
}