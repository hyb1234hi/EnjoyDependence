package com.youzan.mobile.enjoydependence

/**
 * config properties that uploadArchives pom file needed
 */
class MavenPublishExt {
    String flavor//phone or pad
    String version
    String artifactId
    String groupId
    String userName
    String password
    boolean localPublish//本地发布
    String releaseRepo
    String snapshotRepo
    String[] extension = [""]//带有aar标记的依赖，现暂且用这种方式
}