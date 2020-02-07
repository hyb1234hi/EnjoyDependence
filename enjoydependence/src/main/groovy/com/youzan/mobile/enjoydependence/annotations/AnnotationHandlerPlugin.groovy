package com.youzan.mobile.enjoydependence.annotations


import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 注解处理plugin
 */
class AnnotationHandlerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.afterEvaluate {
            def libExtension = project.extensions.findByType(LibraryExtension)
            if (!libExtension) {
                return
            }

            libExtension.registerTransform(new MediatorRegisterTransform(project))
        }
    }
}

