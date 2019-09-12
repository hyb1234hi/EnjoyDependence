package com.youzan.mobile.enjoydependence

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.plugins.MavenPlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
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
        targetProject.plugins.apply(MavenPlugin)
        targetProject.plugins.apply(SigningPlugin)
        targetProject.plugins.apply(MavenPublishPlugin)

        MavenPublishExt publishExt = targetProject.extensions.create("mavenPublish", MavenPublishExt)
        targetProject.afterEvaluate {
            // 添加上传构件的task，并定义task的依赖关系
            targetProject.uploadArchives {
                println("publish to remote maven!!!")
                repositories {
                    mavenDeployer {
                        beforeDeployment {
                            if (publishExt.version == "" || publishExt.version == null) {
                                throw new IllegalArgumentException("the version property in mavenPublish must not be null")
                            }
                            { MavenDeployment deployment -> signing.signPom(deployment) }
                        }
                        pom.groupId = publishExt.groupId
                        pom.artifactId = EnjoyMavenPublishPlugin.getArtifactName(targetProject, publishExt.artifactId)
                        pom.version = publishExt.version

                        repository(url: publishExt.releaseRepo) {
                            authentication(userName: publishExt.userName, password: publishExt.password)
                        }
                        snapshotRepository(url: publishExt.snapshotRepo) {
                            authentication(userName: publishExt.userName, password: publishExt.password)
                        }

                        pom.project {
                            name EnjoyMavenPublishPlugin.getArtifactName(targetProject, publishExt.artifactId)
                            packaging EnjoyMavenPublishPlugin.getPackageType(targetProject)
                        }
                    }
                }
            }

            targetProject.artifacts {
                println("我执行了")
//                archives androidSourcesJar
            }

            targetProject.publishing {
                print("publish to local maven!!!")
                repositories {
                    mavenLocal()
                }

                publications {
                    maven(MavenPublication) {
                        artifact "${targetProject.buildDir}/outputs/aar/${targetProject.name}-release.aar"
                        groupId  publishExt.groupId
                        artifactId getArtifactName(targetProject, publishExt.artifactId)
                        version publishExt.version
                    }
                }
            }

            targetProject.signing {
                required {
                    isReleaseBuild(publishExt.version) && targetProject.gradle.taskGraph.hasTask("uploadArchives")
                }
                sign targetProject.configurations.archives
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