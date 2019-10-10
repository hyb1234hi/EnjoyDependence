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
}