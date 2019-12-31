package com.youzan.mobile.enjoydependence.autoGit.model

/**
 * git 自动化所需参数
 */
class AutoGitExt {
    String version //app版本
    String projectId = "5736"//默认是零售工程的id
    String token = "wZXtsneBmmx9xsDfKQD2"//robot的token
    String source_branch = "dev"
    String target_branch = "release/latest"
    String title = "Auto Create MR" //mr title
    String assignee_id //指定提醒人士
    String desc //mr描述
    String userEmail = ""//值班人邮箱
}