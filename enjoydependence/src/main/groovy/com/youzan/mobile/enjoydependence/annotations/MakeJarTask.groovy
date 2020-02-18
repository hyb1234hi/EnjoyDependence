package com.youzan.mobile.enjoydependence.annotations

import org.gradle.jvm.tasks.Jar

class MakeJarTask extends Jar {

    MakeJarTask() {
        this.archiveName = "${project.name}-export-api.jar"
        def srcClassDir = [project.buildDir.absolutePath + "/moduleApiExport"]
        from srcClassDir
    }

    @Override
    String getGroup() {
        return "enjoydependence"
    }

    @Override
    String getDescription() {
        return "publish module's export api by jar"
    }
}