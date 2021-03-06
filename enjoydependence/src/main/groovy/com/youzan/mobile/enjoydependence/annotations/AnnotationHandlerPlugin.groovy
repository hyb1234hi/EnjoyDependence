package com.youzan.mobile.enjoydependence.annotations

import com.android.build.gradle.LibraryExtension
import com.youzan.mobile.enjoydependence.MavenPublishExt
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 注解处理plugin
 */
class AnnotationHandlerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.afterEvaluate {
            if (project.name == "app" || project.name == "modules" || project.name == "enjoydependence") {
                return
            }

            def libExtension = project.extensions.findByType(LibraryExtension)
            if (!libExtension) {
                return
            }

            libExtension.registerTransform(new MediatorRegisterTransform(project))
            libExtension.registerTransform(new ExportTransform(project))

            MavenPublishExt publishExt = project.getExtensions().findByType(MavenPublishExt.class)
            if (publishExt.localPublish) {
                project.getTasks().create("makeJar", MakeJarTask).dependsOn(["assembleDebug"]).finalizedBy(["publishModuleExportPublicationToMavenLocal"])
            } else {
                project.getTasks().create("makeJar", MakeJarTask).dependsOn(["assembleDebug"]).finalizedBy(["publishModuleExportPublicationToMavenRepository"])
            }
        }
    }
}

