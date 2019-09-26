package com.youzan.mobile.enjoydependence.auto

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction

class AutoPublishTask extends DefaultTask {

    String defaultVersion = ""

    @TaskAction
    void autoPublish() {
        defaultVersion = project.hasProperty("version") ? project.version : defaultVersion
        if (defaultVersion != "") {
            //写入到project的version.property
            def list = []
            File file = new File("version.properties")
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

            def versions = []
            File rootVersionFile = new File(project.rootProject.projectDir.absolutePath + "/" + "version.properties")
            if (rootVersionFile.exists()) {
                rootVersionFile.withReader('UTF-8') { reader ->
                    reader.eachLine {
                        if (it.contains(project.name)) {
                            it = "${project.name}=${defaultVersion}"
                            versions.add(it + "\n")
                        }
                    }
                }
                rootVersionFile.withWriter('UTF-8') { writer ->
                    versions.each {
                        writer.write(it)
                    }
                }
            }
        }
        println("version: ${defaultVersion}")
        println("-----------------auto publish finish-------------------")
    }

    @Override
    String getGroup() {
        return "CustomAuto"
    }

    @Override
    String getDescription() {
        return "Auto Publish AAR To Maven"
    }
}