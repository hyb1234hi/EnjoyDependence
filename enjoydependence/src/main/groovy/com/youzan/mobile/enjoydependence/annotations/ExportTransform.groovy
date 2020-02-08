package com.youzan.mobile.enjoydependence.annotations

import com.android.build.api.transform.Context
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.google.common.collect.ImmutableSet
import javassist.ClassPool
import javassist.CtClass
import jdk.internal.org.objectweb.asm.ClassReader
import org.gradle.api.Project

/**
 * 导出带有Export注解的类
 */
class ExportTransform extends Transform {

    private Project mProject
    private ClassPool mClassPool = ClassPool.getDefault()

    ExportTransform(Project project) {
        this.mProject = project
    }

    @Override
    String getName() {
        return "exportTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        def name = QualifiedContent.Scope.PROJECT_LOCAL_DEPS.name()
        def deprecated = QualifiedContent.Scope.PROJECT_LOCAL_DEPS.getClass()
                .getField(name).getAnnotation(Deprecated.class)

        if (deprecated == null) {
            println "cannot find QualifiedContent.Scope.PROJECT_LOCAL_DEPS Deprecated.class "
            return ImmutableSet.<QualifiedContent.Scope> of(QualifiedContent.Scope.PROJECT
                    , QualifiedContent.Scope.PROJECT_LOCAL_DEPS
                    , QualifiedContent.Scope.EXTERNAL_LIBRARIES
                    , QualifiedContent.Scope.SUB_PROJECTS
                    , QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS)
        } else {
            println "find QualifiedContent.Scope.PROJECT_LOCAL_DEPS Deprecated.class "
            return ImmutableSet.<QualifiedContent.Scope> of(QualifiedContent.Scope.PROJECT)
        }
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        mClassPool.appendSystemPath()

        List<File> libBootClasses = mProject.getExtensions().findByType(LibraryExtension).bootClasspath
        libBootClasses.forEach { file ->
            mClassPool.appendClassPath(file.absolutePath)
        }

        inputs.forEach { input ->
            input.jarInputs.forEach {
                mClassPool.appendClassPath(it.file.absolutePath)
            }
            input.directoryInputs.forEach {
                mClassPool.appendClassPath(it.file.absolutePath)
            }
        }

        inputs.forEach { input ->
            input.directoryInputs.each { directory ->
                File dir = new File(directory.file.absolutePath)
                if (dir.isDirectory()) {
                    dir.eachFileRecurse { File file ->
                        String filePath = file.absolutePath
                        if (filePath.contains("youzan")) {
                            handleClass(dir, file)
                        }
                    }
                }

                File output = outputProvider.getContentLocation(directory.name, directory.contentTypes, directory.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(directory.file, output)
            }

            input.jarInputs.each { jar ->
                File output = outputProvider.getContentLocation(jar.file.absolutePath, jar.contentTypes, jar.scopes, Format.JAR)
                FileUtils.copyFile(jar.file, output)
            }
        }
    }

    private void handleClass(File directory, File file) {
        if (!file.path.endsWith(".class")) {
            return
        }

        if (file.path.endsWith("/R.class")) {
            return
        }

        def inputStream = new FileInputStream(file)
        def reader = new ClassReader(inputStream)
        def className = reader.className.replace('/', '.')
        CtClass tempCls = mClassPool.get(className)
        def hasExport = tempCls.hasAnnotation("com.youzan.mobile.lib_common.annotation.Export")
        if (hasExport) {
            mProject.logger.error("exportClassPath:" + reader.className)
            File toDir = new File(mProject.buildDir.absolutePath, "moduleApiExport/${reader.className.substring(0, reader.className.lastIndexOf("/"))}")
            if (!toDir.exists()) {
                toDir.mkdir()
            }
            def targetFileName = tempCls.name.substring(tempCls.name.lastIndexOf(".") + 1, tempCls.name.length())
            File targetFile = new File(toDir, targetFileName)
            if (targetFile.exists()) {
                targetFile.delete()
            }
            mProject.copy {
                from(file.parentFile) {
                    include "${targetFileName}.class"
                }
                into(toDir)
            }
        }
    }
}