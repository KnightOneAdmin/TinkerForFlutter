package me.chunsheng.plugin.t4f

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import me.chunsheng.t4f.gradle.plugin.MethodClassAdapter
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

/**
 * @description: 自定义Transform实现@
 * @author: weichunsheng
 * @version: 1.0
 */
class Tinker4FlutterTransform extends Transform {

    private static Project project
    private static final String NAME = "Tinker4FlutterTransform"

    Tinker4FlutterTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return NAME
    }

    /**
     * 需要处理的数据类型，有两种枚举类型
     * CLASSES 代表处理的 java 的 class 文件
     * RESOURCES 代表要处理 java 的资源
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 指 Transform 要操作内容的范围，官方文档 Scope 有 7 种类型：
     * 1. EXTERNAL_LIBRARIES        只有外部库
     * 2. PROJECT                   只有项目内容
     * 3. PROJECT_LOCAL_DEPS        只有项目的本地依赖(本地jar)
     * 4. PROVIDED_ONLY             只提供本地或远程依赖项
     * 5. SUB_PROJECTS              只有子项目。
     * 6. SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
     * 7. TESTED_CODE               由当前变量(包括依赖项)测试的代码
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    static void start() {
        println("******************************************************************************")
        println("******                                                                  ******")
        println("******                欢迎使用 Tinker For Flutter  编译插件               ******")
        println("******                                                                  ******")
        println("******************************************************************************")
    }

    static void find() {
        println("******                 找到Hook类,开始执行插桩逻辑                   ******")
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        start()
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()
        // Transform 的 inputs 有两种类型，一种是目录，一种是 jar 包，要分开遍历
        transformInvocation.inputs.each { TransformInput input ->
            input.jarInputs.each { JarInput jarInput ->
                // 处理jar
                processJarInput(jarInput, outputProvider)
            }
            input.directoryInputs.each { DirectoryInput directoryInput ->
                // 处理源码文件
                processDirectoryInput(directoryInput, outputProvider)
            }
        }

    }

    static void processJarInput(JarInput jarInput, TransformOutputProvider outputProvider) {
        //重命名输出文件（同目录copyFile会冲突）
        def jarName = jarInput.name
        def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
        if (jarName.endsWith(".jar")) {
            jarName = jarName.substring(0, jarName.length() - 4)
        }
        File dest = outputProvider.getContentLocation(
                jarName + md5Name,
                jarInput.getContentTypes(),
                jarInput.getScopes(),
                Format.JAR
        )
        //将 input 的目录复制到 output 指定目录
        FileUtils.copyFile(jarInput.getFile(), dest)
    }

    static void processDirectoryInput(DirectoryInput directoryInput, TransformOutputProvider outputProvider) {
        File dest = outputProvider.getContentLocation(
                directoryInput.getName(),
                directoryInput.getContentTypes(),
                directoryInput.getScopes(),
                Format.DIRECTORY
        )
        //开始遍历Flutter初始化代码，插入加载Tinker so文件逻辑
        //是否是目录
        if (directoryInput.file.isDirectory()) {
            //列出目录所有文件（包含子文件夹，子文件夹内文件）,这里我们主要寻找FlutterMain这个类
            directoryInput.file.eachFileRecurse { File file ->
                def name = file.name
                if (name.endsWith(".class") && !name.startsWith("R\$")
                        && "R.class" != name && "BuildConfig.class" != name
                        && "FlutterMain.class" == name) {
                    find()
                    ClassReader classReader = new ClassReader(file.bytes)
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    ClassVisitor cv = new MethodClassAdapter(classWriter)
                    classReader.accept(cv, 0)
                    byte[] code = classWriter.toByteArray()
                    FileOutputStream fos = new FileOutputStream(
                            file.parentFile.absolutePath + File.separator + name)
                    fos.write(code)
                    fos.close()
                }
            }
        }
        //将 input 的目录复制到 output 指定目录
        FileUtils.copyDirectory(directoryInput.getFile(), dest)
    }
}