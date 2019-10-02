package com.youzan.mobile.enjoydependence

import groovy.util.logging.Slf4j
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.attributes.ImmutableAttributesFactory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.AppliedPlugin
import com.android.build.gradle.LibraryExtension
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin

import javax.inject.Inject

@Slf4j
class EnjoyPublishPlugin implements Plugin<Project> {

    private ObjectFactory objectFactory
    private ImmutableAttributesFactory attributesFactory

    @Inject
    public EnjoyPublishPlugin(ObjectFactory objectFactory, ImmutableAttributesFactory attributesFactory) {
        this.objectFactory = objectFactory
        this.attributesFactory = attributesFactory
    }

    @Override
    void apply(Project project) {
        project.plugins.apply(MavenPublishPlugin)
        project.pluginManager.withPlugin('com.android.library', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {
                addSoftwareComponents(project)
            }
        })
    }

    private void addSoftwareComponents(Project project) {
        def android = project.extensions.getByType(LibraryExtension)
        def configurations = project.configurations
        android.libraryVariants.all { v ->
            def publishConfig = new VariantPublishConfiguration(v)
            project.components.add(new AndroidVariantLibrary(objectFactory, configurations, attributesFactory, publishConfig))
            // For default publish config
            def defaultPublishConfig = new DefaultPublishConfiguration(project)
            project.components.add(new AndroidVariantLibrary(objectFactory, configurations, attributesFactory, defaultPublishConfig))
        }
    }
}