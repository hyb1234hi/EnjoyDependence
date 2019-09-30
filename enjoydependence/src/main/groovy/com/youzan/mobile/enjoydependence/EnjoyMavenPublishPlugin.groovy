package com.youzan.mobile.enjoydependence

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ExcludeRule
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.bundling.AbstractArchiveTask

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
        targetProject.getTasks().find {"publish"}.doLast {
            println("-----------------auto publish finish-------------------")
        }

        MavenPublishExt publishExt = targetProject.extensions.create("mavenPublish", MavenPublishExt)

        targetProject.afterEvaluate {
            if (publishExt.version == "" || publishExt.version == null) {
                return
            }

            String defaultVersion = ""
            Map<String, Project> projectMap = new HashMap<String, Project>()
            targetProject.rootProject.subprojects.each { pro ->
                projectMap.put(pro.name, pro)
            }

            def projectName = targetProject.name
            def flavor = publishExt.flavor
            if (flavor != null) {
                projectName = projectName + "-" + flavor
            }

            if (publishExt.localPublish) {
                println("publish to local maven")
                targetProject.publishing {
                    repositories {
                        mavenLocal()
                    }

                    publications {
                        maven(MavenPublication) {
                            defaultVersion = targetProject.hasProperty("version") ? targetProject.version : publishExt.version
                            artifact "${targetProject.buildDir}/outputs/aar/${projectName}-release.aar"
                            groupId publishExt.groupId
                            artifactId getArtifactName(targetProject, publishExt.artifactId)
                            version defaultVersion
                            if (targetProject.getTasks().findByName("sourcesJar")) {
                                if (targetProject.getTasks().findByName("sourcesJar") instanceof AbstractArchiveTask) {
                                    def task = targetProject.getTasks().findByName("sourcesJar") as AbstractArchiveTask
                                    artifact(task.getArchivePath().path) {
                                        classifier = 'sources'
                                    }
                                }
                            }
                            pom.withXml {
                                def dependenciesNode = asNode().appendNode('dependencies')
                                targetProject.configurations.implementation.allDependencies.withType(ModuleDependency) { ModuleDependency dp ->
                                    println("dependencies ${dp.name} ${dp.version}")
                                    if (dp.version != "unspecified" && !projectMap.containsKey(dp.name)) { // 过滤项目内library引用
                                        def dependencyNode = dependenciesNode.appendNode('dependency')
                                        dependencyNode.appendNode('groupId', dp.group)
                                        dependencyNode.appendNode('artifactId', dp.name)
                                        dependencyNode.appendNode('version', dp.version)

                                        // for exclusions
                                        if (dp.excludeRules.size() > 0) {
                                            def exclusions = dependencyNode.appendNode('exclusions')
                                            dp.excludeRules.each { ExcludeRule ex ->
                                                def exclusion = exclusions.appendNode('exclusion')
                                                exclusion.appendNode('groupId', ex.group)
                                                exclusion.appendNode('artifactId', ex.module)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                println("publish to remote maven")
                defaultVersion = targetProject.hasProperty("version") ? targetProject.version : publishExt.version
                String usernameTemp = publishExt.userName
                String pwd = publishExt.password
                String versionTemp = defaultVersion
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
                            artifact "${targetProject.buildDir}/outputs/aar/${projectName}-release.aar"
                            if (targetProject.getTasks().findByName("sourcesJar")) {
                                if (targetProject.getTasks().findByName("sourcesJar") instanceof AbstractArchiveTask) {
                                    def task = targetProject.getTasks().findByName("sourcesJar") as AbstractArchiveTask
                                    artifact(task.getArchivePath().path) {
                                        classifier = 'sources'
                                    }
                                }
                            }
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