package com.youzan.mobile.enjoydependence.auto

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * 自动写入版本号
 */
class WriteVersionTask extends DefaultTask {

    String defaultVersion = ""

    @TaskAction
    void wtiteVersion() {
        defaultVersion = project.hasProperty("version") ? project.version : defaultVersion
        println("-----------------auto write ${project.name} version: ${defaultVersion}----------------")
        if (defaultVersion != "") {
            //写入到project的version.property
            def list = []
            def parentPath = project.path.replace(":", "/")
            File file = new File(project.rootProject.projectDir.absolutePath + "/" + parentPath + "/" + "version.properties")
            println(file.absolutePath)
            if (file.exists()) {
                file.withReader('UTF-8') { reader ->
                    reader.eachLine {
                        if (it.contains("versionName")) {
                            it = "versionName=${defaultVersion}"
                            list.add(it + "\n")
                        }
                    }
                }
                file.withWriter('UTF-8') { writer ->
                    list.each {
                        writer.write(it)
                    }
                }
            }

            def oldVersion = []
            File rootVersionFile = new File(project.rootProject.projectDir.absolutePath + "/" + "version.properties")
            if (rootVersionFile.exists()) {
                rootVersionFile.withReader('UTF-8') { reader ->
                    reader.eachLine {
                        if (it.contains(project.name)) {
                            oldVersion.add(it)
                        }
                    }
                }

                oldVersion.each {
                    String fileText = rootVersionFile.text
                    String newVersions = fileText.replaceAll(it, "${project.name}=${defaultVersion}")
                    rootVersionFile.write(newVersions)
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
        return "Auto Write version to version.properties"
    }
}