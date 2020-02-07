package com.youzan.mobile.enjoydependence.annotations

import com.android.build.gradle.AppExtension
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

/**
 * 借助 Javassit 操作 Class 文件
 */
class InjectByJavassit {

    private static final ClassPool sClassPool = ClassPool.getDefault()

    /**
     * 插入一段Toast代码
     * @param path
     * @param project
     */
    static void injectToast(String path, Project project) {
        if (path == null) {
            return
        }
        // 加入当前路径
        sClassPool.appendClassPath(path)
        sClassPool.appendSystemPath()
        def bootClasses = project.getExtensions().findByType(AppExtension).bootClasspath
        bootClasses.forEach { file ->
            sClassPool.appendClassPath(file.absolutePath)
        }
        // 引入android.os.Bundle包，因为onCreate方法参数有Bundle
        sClassPool.importPackage('android.os.Bundle')

        File dir = new File(path)
        if (dir.isDirectory()) {
            // 遍历文件夹
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                println("filePath: $filePath")

                if (file.name == 'MainActivity.class') {
                    // 获取Class
                    // 这里的MainActivity就在app模块里
                    CtClass ctClass = sClassPool.getCtClass('com.youzan.mobile.enjoydependency.MainActivity')
                    println("ctClass: $ctClass")

                    // 解冻
                    if (ctClass.isFrozen()) {
                        ctClass.defrost()
                    }

                    // 获取Method
                    CtMethod ctMethod = ctClass.getDeclaredMethod('onCreate')
                    println("ctMethod: $ctMethod")

                    String toastStr = """ android.widget.Toast.makeText(this,"我是被插入的Toast代码~!!",android.widget.Toast.LENGTH_SHORT).show();  
                                      """

                    // 方法尾插入
                    ctMethod.insertAfter(toastStr)
                    ctClass.writeFile(path)
                    ctClass.detach() //释放
                }
            }
        }
    }
}