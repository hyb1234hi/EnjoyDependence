package com.youzan.mobile.enjoydependence

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.plugins.MavenPlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.internal.impldep.org.apache.maven.Maven
import org.gradle.plugins.signing.SigningPlugin

/**
 * a plugin use to uploadArchives
 */
class EnjoyMavenPublishPlugin implements Plugin<Project> {

    @Override
    void apply(Project targetProject) {

        if (targetProject == targetProject.rootProject || targetProject.plugins.hasPlugin("com.android.application") || targetProject.name == "app") {
            targetProject.logger.warn("only the library module will apply the plugin:EnjoyMavenPublishPlugin ")
            return
        }

        // add the needed plugin
        targetProject.plugins.apply(MavenPublishPlugin)

        MavenPublishExt publishExt = targetProject.extensions.create("mavenPublish", MavenPublishExt)

        targetProject.afterEvaluate {
            if (publishExt.version == "" || publishExt.version == null) {
                return
            }

            if (publishExt.localPublish) {
                println("publish to local maven")
                targetProject.publishing {
                    repositories {
                        mavenLocal()
                    }

                    publications {
                        maven(MavenPublication) {
                            artifact "${targetProject.buildDir}/libs/${targetProject.name}-sources.jar"
                            artifact "${targetProject.buildDir}/outputs/aar/${targetProject.name}-${publishExt.flavor}-release.aar"
                            groupId publishExt.groupId
                            artifactId getArtifactName(targetProject, publishExt.artifactId)
                            version publishExt.version
                        }
                    }
                }
            } else {
                println("publish to remote maven")
                String usernameTemp = publishExt.userName
                String pwd = publishExt.password
                String versionTemp = publishExt.version
                String artifactIdTemp = publishExt.artifactId
                String groupIdTemp = publishExt.groupId
                String urlPath
                if (versionTemp.contains("SNAPSHOT")) {
                    urlPath = publishExt.snapshotRepo
                } else {
                    urlPath = publishExt.releaseRepo
                }

                targetProject.publishing {
                    repositories {
                        maven {
                            credentials {
                                username usernameTemp // 仓库发布用户名
                                password pwd // 仓库发布用户密码
                            }
                            url urlPath // 仓库地址
                        }
                    }

                    publications {
                        maven(MavenPublication) {
                            groupId groupIdTemp
                            artifactId artifactIdTemp
                            version versionTemp
                            artifact "${targetProject.buildDir}/libs/${targetProject.name}-sources.jar"
                            artifact "${targetProject.buildDir}/outputs/aar/${targetProject.name}-${publishExt.flavor}-release.aar"
                        }
                    }
                }
            }
        }
    }

    static def getPackageType(Project project) {
        return EnjoyMavenPublishPlugin.isAndroidLibrary(project) ? "aar" : "jar"
    }

    static def isAndroidLibrary(project) {
        return project.getPlugins().hasPlugin('com.android.application') || project.getPlugins().hasPlugin('com.android.library')
    }

    static def isReleaseBuild(String version) {
        return version != null && !version.contains("SNAPSHOT")
    }

    static def getArtifactName(Project project, String name) {
        if (name == null || name == "") {
            return project.name
        }
        return name
    }
}