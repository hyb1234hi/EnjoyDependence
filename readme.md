# EnjoyDependence Plugin集成说明
## 项目背景：
在日常开发中或多或少大家都发布过sdk 或者 lib，也经常会依赖某些二方或者三方包。在enjoydependence出现之前我们是通过拷贝maven.gradle来实现maven发布，这种方式有诸多弊端：
1.    通过拷贝来实现，操作成本高，有时还有学习成本；
2. 不便于统一管理
3. 不利于推广
## EnjoyDependence是什么？
    EnjoyDependence 是个gradle plugin，它提供了maven本地发布、远端发布及project依赖替换等功能。特点是集成方便、简单易用
## EnjoyDependence如何使用？

### 1.依赖引入
在项目root build.gradle中写入
```
buildscript {
    repositories {
        mavenLocal()
        maven { url 'http://maven.qima-inc.com/content/repositories/releases/' }
       ...
    }
    dependencies {
       ...
        classpath 'com.youzan.mobile.gradle.plugin:enjoydependence:1.0.0'
    }
}

allprojects {
    repositories {
        mavenLocal()
       maven { url 'http://maven.qima-inc.com/content/repositories/releases/' }
        ...
    }
}

apply plugin: 'enjoy-dependence'
```

### 2.在需要发布的lib or sdk的build.gradle添加必要gradle节点

```
mavenPublish {
    Properties properties = new Properties()
    InputStream inputStream = project.file('local.properties').newDataInputStream()
    properties.load(inputStream)

    localPublish = properties.getProperty("mavenLocal")//是否发布到localMaven
    version = properties.getProperty("sdk_version") // 如果不包含SNAPSHOT，则发布到release的仓库
    groupId = properties.getProperty("sdk_groupId")
    artifactId = properties.getProperty("sdk_artifactId")
    userName = properties.getProperty("user_name")
    password = properties.getProperty("user_pwd")
    snapshotRepo = properties.getProperty("SNAPSHOT_REPOSITORY_URL")
    releaseRepo = properties.getProperty("RELEASE_REPOSITORY_URL")
}
```
然后执行gradle命令 **publish**

### 3.在各个需要进行动态依赖替换的module的build.gradle文件中添加以下配置

```
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'androidx.appcompat:appcompat:1.1.0'
    implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
    implementation project(path: ':modules:liba')
    implementation project(path: ':modules:libb')
    implementation project(path: ':modules:lib_common')
}

dynamicDependency {
    Properties properties = new Properties()
    InputStream inputStream = project.rootProject.file('local.properties').newDataInputStream()
    properties.load(inputStream)

    //这个key对应的是本地模块的名称（注意这个本地模块应该在依赖当中有配置,如上所示）
    lib_common {
        //如果是true，则使用本地模块作为依赖参与编译，否则使用下面的配置获取远程的构件作为依赖参与编译
        debuggable = properties.getProperty("lib_common") ? false : true
        groupId = "com.youzan.mobile"
        artifactId = "lib_common" // 默认使用模块的名称作为其值
        version = properties.getProperty("lib_common")
    }
    liba {
        //如果是true，则使用本地模块作为依赖参与编译，否则使用下面的配置获取远程的构件作为依赖参与编译
        debuggable = properties.getProperty("liba") ? false : true
        groupId = "com.youzan.mobile"
        artifactId = "liba" // 默认使用模块的名称作为其值
        version = properties.getProperty("liba")
    }
    libb {
        //如果是true，则使用本地模块作为依赖参与编译，否则使用下面的配置获取远程的构件作为依赖参与编译
        debuggable = properties.getProperty("libb") ? false : true
        groupId = "com.youzan.mobile"
        artifactId = "libb" // 默认使用模块的名称作为其值
        version = properties.getProperty("libb")
    }
}
```
配置好后，sync一次即可实现依赖替换


