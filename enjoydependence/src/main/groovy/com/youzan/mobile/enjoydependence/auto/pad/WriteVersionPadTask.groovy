package com.youzan.mobile.enjoydependence.auto.pad

import com.youzan.mobile.enjoydependence.MavenPublishExt
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * 自动写入版本号
 */
class WriteVersionPadTask extends DefaultTask {

    String defaultVersion = ""

    @TaskAction
    void wtiteVersion() {
        MavenPublishExt publishExt = project.extensions.findByType(MavenPublishExt.class)
        if (publishExt != null) {
            defaultVersion = publishExt.version
        }
        if (project.hasProperty("version") && project.version != "unspecified") {
            defaultVersion = project.version
        }
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
            } else {
                file.createNewFile()
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
            }

            def oldVersion = []
            File rootVersionFile = new File(project.rootProject.projectDir.absolutePath + "/" + "version.properties")
            if (rootVersionFile.exists()) {
                rootVersionFile.withReader('UTF-8') { reader ->
                    reader.eachLine {
                        String[] temp = it.split("=")
                        String projectName = ""
                        if (temp.size() == 2) {
                            projectName = temp[0]
                        }
                        if (projectName == project.name) {
                            oldVersion.add(it)
                        }
                    }
                }

                oldVersion.each {
                    String fileText = rootVersionFile.text
                    String newVersions = fileText.replaceAll(it, "${project.name}=${defaultVersion}")
                    rootVersionFile.write(newVersions)
                }
            } else {
                rootVersionFile.createNewFile()
                if (rootVersionFile.exists()) {
                    rootVersionFile.withReader('UTF-8') { reader ->
                        reader.eachLine {
                            String[] temp = it.split("=")
                            String projectName = ""
                            if (temp.size() == 2) {
                                projectName = temp[0]
                            }
                            if (projectName.contains(project.name)) {
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