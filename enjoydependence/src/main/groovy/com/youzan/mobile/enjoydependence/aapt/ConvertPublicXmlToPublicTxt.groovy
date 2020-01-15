package com.youzan.mobile.enjoydependence.aapt

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.util.GFileUtils
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * 如果之前是用aapt备份下来的public.xml，如果现在使用了aapt2，则需要将文件进行转换
 */
class ConvertPublicXmlToPublicTxt extends DefaultTask {

    @TaskAction
    void autoPublish() {
        //源public.xml
        File publicXmlFile = project.rootProject.file('backup/public.xml')
        //目标public.txt
        File publicTxtFile = project.rootProject.file('backup/generate_public.txt')
        //包名
        String applicationId = "com.youzan.mobile.enjoydependency"
        GFileUtils.deleteQuietly(publicTxtFile)
        GFileUtils.touch(publicTxtFile)
        def nodes = new XmlParser().parse(publicXmlFile)
        Pattern drawableGeneratePattern = Pattern.compile('^(.*?_)([0-9]{0,})$')
        nodes.each {
            project.logger.error "${it}"
            if ("drawable".equalsIgnoreCase("${it.@type}")) {
                //以'_数字'结尾的drawable资源，此类资源是aapt编译时生成的nested资源，如avd_hide_password_1, avd_hide_password_2
                //但是可能会有其他资源掺杂，如abc_btn_check_to_on_mtrl_000, abc_btn_check_to_on_mtrl_015
                //为了将此类资源过滤掉，将正则匹配到的数字转成int，对比原始数字部分匹配字符串，如果一致，则是aapt生成
                //重要：为了避免此类nested资源生成顺序发生改变，应该禁止修改此类资源
                //aapt生成的是以下表1开始，aapt2是以下标0开始，因此转换的过程需要-1
                Matcher matcher = drawableGeneratePattern.matcher(it.@name)
                if (matcher.matches() && matcher.groupCount() == 2) {
                    String number = matcher.group(2)
                    if (number.equalsIgnoreCase(Integer.parseInt(number).toString())) {
                        String prefixName = matcher.group(1)
                        publicTxtFile.append("${applicationId}:${it.@type}/\$${prefixName}_${Integer.parseInt(number) - 1} = ${it.@id}\n")
                        return
                    }
                }
            }

            publicTxtFile.append("${applicationId}:${it.@type}/${it.@name} = ${it.@id}\n")
        }
    }

    @Override
    String getGroup() {
        return "aapt"
    }

    @Override
    String getDescription() {
        return "convertPublicXmlToPublicTxt"
    }
}