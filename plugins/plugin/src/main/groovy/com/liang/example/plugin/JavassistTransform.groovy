package com.liang.example.plugin

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils

// import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project
import org.gradle.internal.impldep.org.apache.commons.codec.digest.DigestUtils

class JavassistTransform extends Transform {
    private Project project

    JavassistTransform(Project project) {
        this.project = project
    }

    // transform的名称
    // transformClassesWithMyClassTransformForDebug 运行时的名字
    // transformClassesWith + getName() + For + Debug或Release
    @Override
    String getName() {
        return "JavassistTransform"
    }

    // 需要处理的数据类型，有两种枚举类型
    // CLASSES和RESOURCES，CLASSES代表处理的java的class文件，RESOURCES代表要处理java的资源
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    // 指Transform要操作内容的范围，官方文档Scope有7种类型：
    // EXTERNAL_LIBRARIES                   只有外部库
    // SCOPE_FULL_PROJECT                   只有项目内容
    // SCOPE_FULL_PROJECT_LOCAL_DEPS        只有项目的本地依赖(本地jar)
    // SCOPE_FULL_PROVIDED_ONLY             只提供本地或远程依赖项
    // SCOPE_FULL_SUB_PROJECTS              只有子项目。
    // SCOPE_FULL_SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
    // SCOPE_FULL_TESTED_CODE               由当前变量(包括依赖项)测试的代码
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    // 指明当前Transform是否支持增量编译
    @Override
    boolean isIncremental() {
        return false
    }

    // Transform中的核心方法，
    // inputs中是传过来的输入流，其中有两种格式，一种是jar包格式一种是目录格式。
    // outputProvider 获取到输出目录，最后将修改的文件复制到输出目录，这一步必须做不然编译会报错
    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        // transformInvocation.context
        // transformInvocation.inputs
        // transformInvocation.outputProvider
        // transformInvocation.isIncremental()
        // transformInvocation.referencedInputs
        // transformInvocation.secondaryInputs
        super.transform(transformInvocation)
        println("你愁啥----------------进入transform了--------------")
        // 遍历input
        transformInvocation.inputs.each { TransformInput input ->
            // 遍历文件夹
            input.directoryInputs.each { DirectoryInput directoryInput ->
                // 注入代码
                MyInjects.inject(directoryInput.file.absolutePath, mProject)  // todo
                // 获取output目录
                def dest = transformInvocation.outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
            // 遍历jar文件，对jar不操作，但是要输出到out路径
            input.jarInputs.each { JarInput jarInput ->
                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                println("jar = " + jarInput.file.getAbsolutePath())
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
        println("瞅你咋地--------------结束transform了----------------")
    }
}