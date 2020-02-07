package com.youzan.mobile.enjoydependence.annotations

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.AppPlugin
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

            libExtension.registerTransform(new AnnotationTransform(project))
        }
    }
}

