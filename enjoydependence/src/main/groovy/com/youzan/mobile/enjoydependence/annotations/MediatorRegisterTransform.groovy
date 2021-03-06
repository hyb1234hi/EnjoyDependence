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
import javassist.CtMethod
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.AttributeInfo
import javassist.bytecode.SourceFileAttribute
import javassist.bytecode.annotation.Annotation
import javassist.bytecode.annotation.StringMemberValue
import jdk.internal.org.objectweb.asm.ClassReader
import org.gradle.api.Project

class MediatorRegisterTransform extends Transform {

    private Project mProject
    private ClassPool mClassPool = ClassPool.getDefault()
    private List<String> mTargetCtClasses = new ArrayList<>()//注册类
    private Map<String, AnnotationClassInfo> mAnnotationCtClasses = new HashMap<>()
//携带MediatorRegister注解的类

    MediatorRegisterTransform(Project project) {
        this.mProject = project
    }

    @Override
    String getName() {
        return "mediatorRegisterTransform"
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
        return true
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider,
                   boolean isIncremental) throws IOException, TransformException, InterruptedException {
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
        if (tempCls == null) {
            return
        }

        String[] interfaces = tempCls.classFile.interfaces
        def isTarget = false
        for (String ctInterfaceName : interfaces) {
            if (ctInterfaceName.contains("IApplication")) {
                isTarget = true
            }
        }
        if (isTarget) {
            mProject.logger.error("target class is : $tempCls.name")
            mTargetCtClasses.add(tempCls.name)

            for (String pluginName : mAnnotationCtClasses.keySet()) {
                if (mAnnotationCtClasses.get(pluginName).hasRegister) {
                    continue
                }
                if (tempCls.isFrozen()) {
                    tempCls.defrost()
                }
                CtMethod ctMethod = tempCls.getDeclaredMethod("onCreate")
                String register = "{com.youzan.mobile.lib_common.Register register = com.youzan.mobile.lib_common.Register.register;" +
                        "register.regis(\"${pluginName}\", new ${mAnnotationCtClasses.get(pluginName).className}());}"

                ctMethod.insertAfter(register)
                tempCls.writeFile(directory.absolutePath)
                tempCls.detach()
                mAnnotationCtClasses.get(pluginName).hasRegister = true
            }
        }

        def hasModified = false
        def annotation = tempCls.hasAnnotation("com.youzan.mobile.lib_common.annotation.MediatorRegister")
        if (annotation) {
            mProject.logger.error("annotation class is : ${file.absolutePath}")

            def attribute = tempCls.classFile.getAttributes()
            mProject.logger.error(attribute.toString())
            Annotation mediatorRegister = null
            for (AttributeInfo att : attribute) {
                if (att instanceof AnnotationsAttribute) {
                    AnnotationsAttribute aatt = att as AnnotationsAttribute
                    for (Annotation ann : aatt.getAnnotations()) {
                        if (ann.typeName.contains("MediatorRegister")) {
                            mediatorRegister = ann
                            mProject.logger.error("get MediatorRegister annotation")
                            break
                        }
                    }
                }
            }

            def pluginName = null
            if (mediatorRegister != null) {
                pluginName = ((StringMemberValue) mediatorRegister.getMemberValue("pluginName")).getValue()
                mProject.logger.error("pluginName is ${pluginName}")
            }

            if (pluginName != null) {
                //注解类存储
                mAnnotationCtClasses.put(pluginName, new AnnotationClassInfo(pluginName, tempCls.name, false))
                //动态注册
                for (String targetClassName : mTargetCtClasses) {
                    def targetCtclass = mClassPool.get(targetClassName)
                    if (targetCtclass.isFrozen()) {
                        targetCtclass.defrost()
                    }
                    if (mediatorRegister != null) {
                        CtMethod ctMethod = targetCtclass.getDeclaredMethod("onCreate")
                        String register = "{com.youzan.mobile.lib_common.Register register = com.youzan.mobile.lib_common.Register.register;" +
                                "register.regis(\"${pluginName}\", new $tempCls.name());}"

                        ctMethod.insertAfter(register)
                        targetCtclass.writeFile(directory.absolutePath)
                        targetCtclass.detach()
                        mAnnotationCtClasses.get(pluginName).hasRegister = true
                    }
                }
            }
        }
    }
}