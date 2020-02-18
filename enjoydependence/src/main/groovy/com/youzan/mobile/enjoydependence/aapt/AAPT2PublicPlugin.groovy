package com.youzan.mobile.enjoydependence.aapt

import com.android.sdklib.BuildToolInfo
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.process.ExecSpec
import org.gradle.util.GFileUtils
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * aapt2.0 导出公共资源
 * aapt2.0 资源编译时，给公共资源增加Public标签，供其他module业务以@包名:资源类型/资源名方式进行引用，类似于android原始资源引用
 */
class AAPT2PublicPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.afterEvaluate {
            if (project.plugins.hasPlugin("com.android.application")) {
                def android = project.getExtensions().findByName('android')
                android.getApplicationVariants().all { def variant ->
                    boolean aapt2Enable = false

                    //资源打包任务
                    def processResourcesTask = project.tasks.getByName("process${variant.name.capitalize()}Resources")
                    if (!processResourcesTask) {
                        project.logger.error "processResourcesTask is not fount"
                        return
                    }

                    try {
                        aapt2Enable = processResourcesTask.isAapt2Enabled()
                    } catch (Exception e) {
                        project.logger.error "${e.getMessage()}"
                    }

                    def aaptOptions = processResourcesTask.aaptOptions
                    if (!aapt2Enable) {
                        project.logger.error "aapt2 is not enabled"
                        return
                    }

                    //aapt2.0 需要固定的资源文件导出，只要该文件存在，则再新增资源参与打包时不会影响该文件内的id
                    File publicTxtFile = project.rootProject.file('public.txt')

                    //public文件存在，则应用，不存在则生成
                    if (publicTxtFile.exists()) {
                        project.logger.error "${publicTxtFile} exists, apply it."
                        //aapt2添加--stable-ids参数应用
                        aaptOptions.additionalParameters("--stable-ids", "${publicTxtFile}")
                    } else {
                        project.logger.error "${publicTxtFile} not exists, generate it."
                        //aapt2添加--emit-ids参数生成
                        aaptOptions.additionalParameters("--emit-ids", "${publicTxtFile}")
                    }

                    //资源合并任务
                    def mergeResourceTask = project.tasks.findByName("merge${variant.getName().capitalize()}Resources")
                    if (mergeResourceTask) {
                        mergeResourceTask.doLast {
                            //目标转换文件，注意public.xml上级目录必须带values目录，否则aapt2执行时会报非法文件路径
                            File publicXmlFile = new File(project.buildDir, "intermediates/res/public/${variant.getDirName()}/values/public.xml")

                            if (!publicTxtFile.exists()) {
                                project.logger.error("public.txt is not exist")
                                return
                            }

                            //转换public.txt文件为publicXml文件
                            convertPublicTxtToPublicXml(project, publicTxtFile, publicXmlFile, false)

                            def variantData = variant.getMetaClass().getProperty(variant, 'variantData')
                            def mBuildToolInfo = variantData.getScope().getGlobalScope().getAndroidBuilder().getTargetInfo().getBuildTools()
                            Map<BuildToolInfo.PathId, String> mPaths = mBuildToolInfo.getMetaClass().getProperty(mBuildToolInfo, "mPaths") as Map<BuildToolInfo.PathId, String>

                            project.exec(new Action<ExecSpec>() {
                                @Override
                                void execute(ExecSpec execSpec) {
                                    execSpec.executable "${mPaths.get(BuildToolInfo.PathId.AAPT2)}"
                                    execSpec.args("compile")
                                    execSpec.args("--legacy")
                                    execSpec.args("-o")
                                    execSpec.args("${mergeResourceTask.outputDir}")
                                    execSpec.args("${publicXmlFile}")
                                }
                            })
                        }
                    }
                }
            }
        }
    }

    /**
     * 转换publicTxt为publicXml
     * 转换理念：
     * 1.public.txt中存在styleable类型资源，public.xml中不存在，因此转换过程中如果遇到styleable类型，需要忽略
     * 2.vector矢量图资源如果存在内部资源，也需要忽略，在aapt2中，它的名字是以$开头，然后是主资源名，紧跟着__数字递增索引，这些资源外部是无法引用到的，只需要固定id，不需要添加PUBLIC标记，并且$符号在public.xml中是非法的，因此忽略它即可
     * 3.由于aapt2有资源id的固定方式，因此转换过程中可直接丢掉id，简单声明即可
     * 4.aapt2编译的public.xml文件的上级目录必须是values文件夹，否则编译过程会报非法路径
     */
    @SuppressWarnings("GrMethodMayBeStatic")
    void convertPublicTxtToPublicXml(Project project, File publicTxtFile, File publicXmlFile, boolean withId) {
        if (publicTxtFile == null || publicXmlFile == null || !publicTxtFile.exists() || !publicTxtFile.isFile()) {
            throw new GradleException("publicTxtFile ${publicTxtFile} is not exist or not a file")
        }

        GFileUtils.deleteQuietly(publicXmlFile)
        GFileUtils.mkdirs(publicXmlFile.getParentFile())
        GFileUtils.touch(publicXmlFile)

        project.logger.info "convert publicTxtFile ${publicTxtFile} to publicXmlFile ${publicXmlFile}"

        publicXmlFile.append("<!-- AUTO-GENERATED FILE.  DO NOT MODIFY -->")
        publicXmlFile.append("\n")
        publicXmlFile.append("<resources>")
        publicXmlFile.append("\n")
        Pattern linePattern = Pattern.compile(".*?:(.*?)/(.*?)\\s+=\\s+(.*?)")

        publicTxtFile.eachLine { def line ->
            Matcher matcher = linePattern.matcher(line)
            if (matcher.matches() && matcher.groupCount() == 3) {
                String resType = matcher.group(1)
                String resName = matcher.group(2)
                if (resName.startsWith('$')) {
                    project.logger.info "ignore to public res ${resName} because it's a nested resource"
                } else if (resType.equalsIgnoreCase("styleable")) {
                    project.logger.info "ignore to public res ${resName} because it's a styleable resource"
                } else {
                    if (withId) {
                        publicXmlFile.append("\t<public type=\"${resType}\" name=\"${resName}\" id=\"${matcher.group(3)}\" />\n")
                    } else {
                        publicXmlFile.append("\t<public type=\"${resType}\" name=\"${resName}\" />\n")
                    }

                }
            }
        }

        publicXmlFile.append("</resources>")
    }
}