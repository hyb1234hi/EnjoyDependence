package com.youzan.mobile.enjoydependence

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
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

        if (targetProject.name == "app" || targetProject.name == "modules") {
            return
        }

        // add the needed plugin
        targetProject.plugins.apply(MavenPublishPlugin)
        targetProject.getTasks().find { "publish" }.doLast {
            println("-----------------auto publish finish-------------------")
        }

        MavenPublishExt publishExt = targetProject.extensions.create("mavenPublish", MavenPublishExt)

        targetProject.afterEvaluate {
            if (publishExt.version == "" || publishExt.version == null) {
                return
            }

            if (publishExt.localPublish) {
                targetProject.publishing {
                    println("publish to local maven")

                    //工程名拼接
                    def projectName = targetProject.name
                    def flavor = publishExt.flavor
                    if (flavor != null) {
                        projectName = projectName + "-" + flavor
                    }

                    //版本号优先采用编译传入参数，为空时采用version.properties中设定版本号
                    String defaultVersion = targetProject.hasProperty("version") ? targetProject.version : publishExt.version

                    //本地lib依赖过滤
                    Map<String, Project> projectMap = new HashMap<String, Project>()
                    targetProject.rootProject.subprojects.each { pro ->
                        projectMap.put(pro.name, pro)
                    }

                    //依赖聚合
                    def configs = ["api", "releaseApi", "phoneApi", "padApi", "implementation", "debugApi"]
                    def workDependencies = []//有效依赖
                    def runtimeDependencies = []//runtime scope依赖
                    targetProject.configurations.all { Configuration configuration ->
                        if (!configuration.name.contains("test") && !configuration.name.contains("Test") && !configuration.name.contains("kapt") && configuration.dependencies.size() > 0) {
                            println("configuration name: ${configuration.name}")
                            configuration.dependencies.withType(ModuleDependency).all { ModuleDependency dp ->
                                if (dp.version != "unspecified" && !projectMap.containsKey(dp.name)) {
                                    println(dp.toString())
                                    workDependencies.add(dp)
                                    if (configuration.name == "implementation") {
                                        runtimeDependencies.add(dp)
                                    }
                                }
                            }
                        }
                    }

                    //遍历project的build.gradle,收集aar及jar
                    def aarList = []
                    def jarList = []
                    def parentPath = targetProject.path.replace(":", "/")
                    File file = new File(targetProject.rootProject.projectDir.absolutePath + "/" + parentPath + "/" + "build.gradle")
                    if (file.exists()) {
                        file.withReader("UTF-8") { reader ->
                            reader.eachLine {
                                if (it.contains("@aar")) {
                                    aarList.add(it.trim())
                                }
                                if (it.contains("@jar")) {
                                    jarList.add(it.trim())
                                }
                            }
                        }
                        aarList.unique()
                        jarList.unique()
                        println("aarList: ${aarList}\njarList: ${jarList}")
                    }

                    repositories {
                        mavenLocal()
                    }

                    publications {
                        maven(MavenPublication) {
                            artifact "${targetProject.buildDir}/outputs/aar/${projectName}-release.aar"
                            groupId publishExt.groupId
                            artifactId getArtifactName(targetProject, publishExt.artifactId)
                            version defaultVersion

                            //源码发布
                            if (targetProject.getTasks().findByName("sourcesJar")) {
                                if (targetProject.getTasks().findByName("sourcesJar") instanceof AbstractArchiveTask) {
                                    def task = targetProject.getTasks().findByName("sourcesJar") as AbstractArchiveTask
                                    artifact(task.getArchivePath().path) {
                                        classifier = 'sources'
                                    }
                                }
                            }

                            //依赖文件POM信息生成
                            pom.withXml {
                                def dependenciesNode = asNode().appendNode('dependencies')
                                workDependencies.each { ModuleDependency dp ->
                                    println(dp.toString())
                                    def dependencyNode = dependenciesNode.appendNode('dependency')
                                    dependencyNode.appendNode('groupId', dp.group)
                                    dependencyNode.appendNode('artifactId', dp.name)
                                    dependencyNode.appendNode('version', dp.version)
                                    if (runtimeDependencies.find { ModuleDependency dependency ->
                                        dependency.name == dp.name
                                    }) {
                                        dependencyNode.appendNode('scope', 'runtime')
                                    } else {
                                        dependencyNode.appendNode('scope', 'compile')
                                    }
                                    aarList.each {
                                        if (it.contains("${dp.group}") && it.contains("${dp.name}")) {
                                            dependencyNode.appendNode('type', 'aar')
                                        }
                                    }
                                    jarList.each {
                                        if (it.contains("${dp.group}") && it.contains("${dp.name}")) {
                                            dependencyNode.appendNode('type', 'jar')
                                        }
                                    }

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
            } else {
                targetProject.publishing {
                    println("publish to remote maven")

                    //工程名拼接
                    def projectName = targetProject.name
                    def flavor = publishExt.flavor
                    if (flavor != null) {
                        projectName = projectName + "-" + flavor
                    }

                    //版本号优先采用编译传入参数，为空时采用version.properties中设定版本号
                    String defaultVersion = targetProject.hasProperty("version") ? targetProject.version : publishExt.version

                    //本地lib依赖过滤
                    Map<String, Project> projectMap = new HashMap<String, Project>()
                    targetProject.rootProject.subprojects.each { pro ->
                        projectMap.put(pro.name, pro)
                    }

                    String urlPath
                    if (versionTemp.contains("SNAPSHOT")) {
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
                        maven(MavenPublication) {
                            groupId publishExt.groupId
                            artifactId publishExt.artifactId
                            version defaultVersion
                            artifact "${targetProject.buildDir}/outputs/aar/${projectName}-release.aar"

                            if (targetProject.getTasks().findByName("sourcesJar")) {
                                if (targetProject.getTasks().findByName("sourcesJar") instanceof AbstractArchiveTask) {
                                    def task = targetProject.getTasks().findByName("sourcesJar") as AbstractArchiveTask
                                    artifact(task.getArchivePath().path) {
                                        classifier = 'sources'
                                    }
                                }
                            }

                            //依赖文件POM信息生成
                            pom.withXml {
                                def dependenciesNode = asNode().appendNode('dependencies')
                                targetProject.configurations.implementation.allDependencies.withType(ModuleDependency) { ModuleDependency dp ->
                                    println("${dp.toString()}")
                                    if (dp.version != "unspecified" && !projectMap.containsKey(dp.name)) {
                                        // 过滤项目内library引用
                                        def dependencyNode = dependenciesNode.appendNode('dependency')
                                        dependencyNode.appendNode('groupId', dp.group)
                                        dependencyNode.appendNode('artifactId', dp.name)
                                        dependencyNode.appendNode('version', dp.version)
                                        dependencyNode.appendNode('scope', 'compile')
                                        aarList.each {
                                            if (it.contains("${dp.group}") && it.contains("${dp.name}")) {
                                                dependencyNode.appendNode('type', 'aar')
                                            }
                                        }
                                        jarList.each {
                                            if (it.contains("${dp.group}") && it.contains("${dp.name}")) {
                                                dependencyNode.appendNode('type', 'jar')
                                            }
                                        }

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