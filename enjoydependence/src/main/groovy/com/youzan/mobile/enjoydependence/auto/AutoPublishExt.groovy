package com.youzan.mobile.enjoydependence.auto

/**
 * 自动发布扩展节点
 */
class AutoPublishExt {
    String command = "assemble"//统一调用的gradle命令
    String dependsOn = "build"//依赖任务
    String[] excludeModules = ["app", "enjoydependence", "modules"]//autopublishall 排除的module
}