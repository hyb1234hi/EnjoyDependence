package com.youzan.mobile.enjoydependence

import com.android.build.gradle.api.BaseVariant
import com.youzan.mobile.enjoydependence.androidPublish.AndroidVariantLibrary
import com.youzan.mobile.enjoydependence.androidPublish.DefaultPublishConfiguration
import com.youzan.mobile.enjoydependence.androidPublish.VariantPublishConfiguration
import groovy.util.logging.Slf4j
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.attributes.ImmutableAttributesFactory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.AppliedPlugin
import com.android.build.gradle.LibraryExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.bundling.AbstractArchiveTask

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
        if (project == project.rootProject || project.plugins.hasPlugin("com.android.application") || project.name == "app") {
            project.logger.warn("only the library module will apply the plugin:EnjoyMavenPublishPlugin ")
            return
        }

        if (project.name == "app" || project.name == "modules") {
            return
        }

        project.plugins.apply(MavenPublishPlugin)
        project.pluginManager.withPlugin('com.android.library', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {
                addSoftwareComponents(project)
            }
        })
        project.getTasks().find { "publish" }.doLast {
            println("-----------------auto publish finish-------------------")
        }
        MavenPublishExt publishExt = project.extensions.create("mavenPublish", MavenPublishExt)
        project.afterEvaluate {
            def androidTemp = project.extensions.findByType(LibraryExtension)
            if (androidTemp == null) {
                return
            }
            //没有配置groupId或者artifactId的认为不需要接入此插件
            if (publishExt.groupId == null || publishExt.groupId == "") {
                return
            }
            if (publishExt.artifactId == null || publishExt.artifactId == "") {
                return
            }
            if (project.hasProperty("localPublish") && project.localPublish != "unspecified") {
                publishExt.localPublish = project.localPublish
            }
            if (publishExt.localPublish) {
                project.publishing {
                    //版本号优先采用编译传入参数，为空时采用version.properties中设定版本号
                    String defaultVersion = publishExt.version
                    if (project.hasProperty("version") && project.version != "unspecified") {
                        defaultVersion = project.version
                    }
                    String defaultFlavor = publishExt.flavor
                    if (project.hasProperty("flavor") && project.flavor != "unspecified") {
                        defaultFlavor = project.flavor
                    }
                    String tempArtifactId = publishExt.artifactId
                    if (defaultFlavor != "" && defaultFlavor != null) {
                        tempArtifactId = publishExt.artifactId + "-" + defaultFlavor
                    }
                    repositories {
                        mavenLocal()
                    }
                    publications {
                        def android = project.extensions.getByType(LibraryExtension)
                        "moduleExport"(MavenPublication) {
                            artifact "${project.buildDir}/libs/${project.name}-export-api.jar"
                            groupId publishExt.groupId
                            artifactId tempArtifactId + "-module-api"
                            version defaultVersion
                        }
                        android.libraryVariants.all { variant ->
                            if (variant.name.capitalize().endsWith("Debug")) {
                                "maven${variant.name.capitalize()}Aar"(MavenPublication) {
                                    from project.components.findByName("android${variant.name.capitalize()}")
                                    groupId publishExt.groupId
                                    artifactId tempArtifactId
                                    version defaultVersion
                                }
                            } else if (variant.name.capitalize().endsWith("Release")) {
                                "maven${variant.name.capitalize()}Aar"(MavenPublication) {
                                    from project.components.findByName("android${variant.name.capitalize()}")
                                    groupId publishExt.groupId
                                    artifactId tempArtifactId
                                    version defaultVersion
                                }
                            }
                        }
                    }
                }
            } else {
                project.publishing {
                    //版本号优先采用编译传入参数，为空时采用version.properties中设定版本号
                    String defaultVersion = publishExt.version
                    if (project.hasProperty("version") && project.version != "unspecified") {
                        defaultVersion = project.version
                    }
                    String defaultFlavor = publishExt.flavor
                    if (project.hasProperty("flavor") && project.flavor != "unspecified") {
                        defaultFlavor = project.flavor
                    }
                    String tempArtifactId = publishExt.artifactId
                    if (defaultFlavor != "" && defaultFlavor != null) {
                        tempArtifactId = publishExt.artifactId + "-" + defaultFlavor
                    }
                    String urlPath
                    if (defaultVersion == null || defaultVersion.contains("SNAPSHOT")) {
                        urlPath = publishExt.snapshotRepo
                    } else {
                        urlPath = publishExt.releaseRepo
                    }

                    repositories {
                        maven {
                            credentials {
                                username publishExt.userName // 仓库发布用户名
                                password publishExt.password // 仓库发布用户密码
                            }
                            url urlPath // 仓库地址
                        }
                    }
                    publications {
                        def android = project.extensions.getByType(LibraryExtension)
                        "moduleExport"(MavenPublication) {
                            artifact "${project.buildDir}/libs/${project.name}-export-api.jar"
                            groupId publishExt.groupId
                            artifactId tempArtifactId + "-module-api"
                            version defaultVersion
                        }
                        android.libraryVariants.all { variant ->
                            if (variant.name.capitalize().endsWith("Debug")) {
                                "maven${variant.name.capitalize()}Aar"(MavenPublication) {
                                    from project.components.findByName("android${variant.name.capitalize()}")
                                    groupId publishExt.groupId
                                    artifactId tempArtifactId
                                    version defaultVersion
                                }
                            } else if (variant.name.capitalize().endsWith("Release")) {
                                "maven${variant.name.capitalize()}Aar"(MavenPublication) {
                                    from project.components.findByName("android${variant.name.capitalize()}")
                                    groupId publishExt.groupId
                                    artifactId tempArtifactId
                                    version defaultVersion
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void addSoftwareComponents(Project project) {
        def android = project.extensions.getByType(LibraryExtension)
        def configurations = project.configurations
        android.libraryVariants.all { v ->
            def publishConfig = new VariantPublishConfiguration(v)
            project.components.add(new AndroidVariantLibrary(objectFactory, configurations, attributesFactory, publishConfig))
        }
        // For default publish config
        def defaultPublishConfig = new DefaultPublishConfiguration(project)
        project.components.add(new AndroidVariantLibrary(objectFactory, configurations, attributesFactory, defaultPublishConfig))
    }
}