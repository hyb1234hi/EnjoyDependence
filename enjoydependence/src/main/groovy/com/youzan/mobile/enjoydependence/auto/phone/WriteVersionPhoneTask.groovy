package com.youzan.mobile.enjoydependence.auto.phone

import com.youzan.mobile.enjoydependence.MavenPublishExt
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * 自动写入版本号
 */
class WriteVersionPhoneTask extends DefaultTask {

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
            def parentPath = project.path.replace(":", "/")
            File file = new File(project.rootProject.projectDir.absolutePath + "/" + parentPath + "/" + "version.properties")
            if (!file.exists()) {
                file.createNewFile()
            }
            file.withWriter('UTF-8') { writer ->
                writer.write("versionName=${defaultVersion}\n")
            }

            //写入到根目录的version.properties
            File rootVersionFile = new File(project.rootProject.projectDir.absolutePath + "/" + "version.properties")
            if (rootVersionFile.exists()) {
                List<String> oldVersion = new ArrayList<>()
                rootVersionFile.withReader('UTF-8') { reader ->
                    reader.eachLine {
                        String[] temp = it.split("=")
                        String projectName = ""
                        if (temp.size() == 2) {
                            projectName = temp[0]
                        }
                        if (projectName != project.name) {
                            oldVersion.add(it)
                        }
                    }
                }

                oldVersion.add("${project.name}=${defaultVersion}")
                oldVersion.sort()
                rootVersionFile.withWriter('UTF-8') { writer ->
                    oldVersion.each {
                        writer.write(it + "\n")
                    }
                }
            } else {
                rootVersionFile.createNewFile()
                if (rootVersionFile.exists()) {
                    rootVersionFile.write("${project.name}=${defaultVersion}\n")
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
        return "Auto Write version to version.properties"
    }
}