package com.youzan.mobile.enjoydependence.auto.all

/**
 * 全量自动发布bean
 */
class AutoPublishAllExt {
    String padCommand = ""//统一调用的gradle命令,用于pad aar的发布
    String phoneCommand = ""//统一调用的gradle命令,用于phone aar的发布
    String[] excludeModules = ["app", "enjoydependence", "modules"]//autopublishall 排除的module
    String[] firstPriority = [""]//优先级最高lib，最先编译，要求彼此不能相互依赖，否则只能手动打包
    String[] secondPriority = [""]//优先级次之，第二批编译，要求彼此不能相互依赖，否则只能手动打包
    String[] thirdPriority = [""]//第三优先级
    String glcParentPath = ""//commit 提交记录文件glc的父路径
}