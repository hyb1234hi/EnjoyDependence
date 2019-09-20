package com.youzan.mobile.enjoydependence

import com.youzan.mobile.enjoydependence.auto.AutoPublishPlugin
import com.youzan.mobile.enjoydependence.build.BuildTimeCostPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class EnjoyDependencePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (project != project.rootProject) {
            throw new IllegalStateException("please apply plugin easy-dependency to root project")
        }
        // apply the maven publish plugin and dynamic dependency resolve plugin to all the sub projects
        project.subprojects {
            it.plugins.apply(EnjoyMavenPublishPlugin)
            it.plugins.apply(DependenceResolvePlugin)
            it.plugins.apply(BuildTimeCostPlugin)
            it.plugins.apply(AutoPublishPlugin)
        }
    }
}