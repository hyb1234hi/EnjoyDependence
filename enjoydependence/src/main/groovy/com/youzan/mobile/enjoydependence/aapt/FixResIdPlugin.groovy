package com.youzan.mobile.enjoydependence.aapt

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.GFileUtils
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * aapt1.0 & aapt2.0资源固定的方式
 * 资源固定对于热修复和插件化宿主资源依赖非常重要
 * 利用aapt1.0生成基础的public.xml用于对应版本的热修复or插件化资源固定
 * 需要禁用aapt2.0才能生效
 */
class FixResIdPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.afterEvaluate {
            if (project.plugins.hasPlugin("com.android.application")) {
                def android = project.extensions.getByName("android")
                android.applicationVariants.all { def variant ->
                    boolean aapt2Enable = false

                    def processResourcesTask = project.tasks.getByName("process${variant.name.capitalize()}Resources")
                    if (processResourcesTask) {
                        try {
                            //判断aapt2是否开启，低版本不存在这个方法，因此需要捕获异常
                            aapt2Enable = processResourcesTask.isAapt2Enabled()
                        } catch (Exception e) {
                            project.logger.error "${e.getMessage()}"
                        }

                        def aaptOptions = processResourcesTask.aaptOptions
                        //aapt2开启走此流程
                        if (aapt2Enable) {
                            project.logger.error "aapt2 is enabled"
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
                        } else {
                            //aapt2禁用走此流程
                            project.logger.error "aapt2 is disabled"
                            File publicXmlFile = project.rootProject.file('public.xml')
                            //public文件存在则应用，不存在则生成
                            if (publicXmlFile.exists()) {
                                //aapt的应用需要将文件拷贝到对应的目录
                                //aapt public.xml文件的应用并不是只是拷贝public.xml文件那么简单，还要根据生成的public.xml生成ids.xml文件，并将ids.xml中与values.xml中重复定义的id去除
                                def mergeResourcesTask = variant.variantData.mergeResourcesTask
                                //资源merge的task存在则在其merge完资源后拷贝public.xml并生成ids.xml
                                if (mergeResourcesTask) {
                                    mergeResourcesTask.doLast {
                                        //拷贝public.xml文件
                                        File toDir = new File(mergeResourcesTask.outputDir, "values")
                                        project.copy {
                                            project.logger.error "${variant.name}:copy from ${publicXmlFile.getAbsolutePath()} to ${toDir}/public.xml"
                                            from(publicXmlFile.getParentFile()) {
                                                include "public.xml"
                                                rename "public.xml", "public.xml"
                                            }
                                            into(toDir)
                                        }
                                        //生成ids.xml文件
                                        File valuesFile = new File(toDir, "values.xml")
                                        File idsFile = new File(toDir, "ids.xml")
                                        if (valuesFile.exists() && publicXmlFile.exists()) {
                                            //记录在values.xml中存在的id定义
                                            def valuesNodes = new XmlParser().parse(valuesFile)
                                            Set<String> existIdItems = new HashSet<String>()
                                            valuesNodes.each {
                                                if ("id".equalsIgnoreCase("${it.@type}")) {
                                                    existIdItems.add("${it.@name}")
                                                }
                                            }
                                            GFileUtils.deleteQuietly(idsFile)
                                            GFileUtils.touch(idsFile)

                                            idsFile.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                                            idsFile.append("\n")
                                            idsFile.append("<resources>")
                                            idsFile.append("\n")

                                            def publicXMLNodes = new XmlParser().parse(publicXmlFile)
                                            Pattern drawableGeneratePattern = Pattern.compile('^(.*?_)([0-9]{0,})$')
                                            publicXMLNodes.each {
                                                //获取public.xml中定义的id类型item
                                                if ("id".equalsIgnoreCase("${it.@type}")) {
                                                    //如果在values.xml中没有定义，则添加到ids.xml中
                                                    //如果已经在values.xml中定义，则忽略它
                                                    if (!existIdItems.contains("${it.@name}")) {
                                                        idsFile.append("\t<item type=\"id\" name=\"${it.@name}\" />\n")
                                                    } else {
                                                        project.logger.error "already exist id item ${it.@name}, ignore it"
                                                    }
                                                } else if ("drawable".equalsIgnoreCase("${it.@type}")) {
                                                    //以'_数字'结尾的drawable资源，此类资源是aapt编译时生成的nested资源，如avd_hide_password_1, avd_hide_password_2
                                                    //但是可能会有其他资源掺杂，如abc_btn_check_to_on_mtrl_000, abc_btn_check_to_on_mtrl_015
                                                    //为了将此类资源过滤掉，将正则匹配到的数字转成int，对比原始数字部分匹配字符串，如果一致，则是aapt生成
                                                    //重要：为了避免此类nested资源生成顺序发生改变，应该禁止修改此类资源
                                                    Matcher matcher = drawableGeneratePattern.matcher(it.@name)
                                                    if (matcher.matches() && matcher.groupCount() == 2) {
                                                        String number = matcher.group(2)
                                                        if (number.equalsIgnoreCase(Integer.parseInt(number).toString())) {
                                                            idsFile.append("\t<item type=\"drawable\" name=\"${it.@name}\" />\n")
                                                        }
                                                    }
                                                }
                                            }
                                            idsFile.append("</resources>")
                                        }
                                    }
                                }
                            } else {
                                //不存在则生成
                                project.logger.error "${publicXmlFile} not exists, generate it"
                                //aapt 添加-P参数生成
                                aaptOptions.additionalParameters("-P", "${publicXmlFile}")
                            }
                        }
                    }
                }
            }

            project.getTasks().create("convertPublicXmlToPublicTxt", ConvertPublicXmlToPublicTxt.class)
        }
    }
}