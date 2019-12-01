package com.liang.example.plugin

import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project

class MyGroovyPlugin implements Plugin<Project> {
    private void test1(Project project) {
        if (!project.android) {
            throw new IllegalStateException('Must apply \'com.android.application\' or \'com.android.library\' first!');
        }
        project.android.applicationVariants.all { variant ->
            variant.outputs.all { outputFileName = "${variant.name}-${variant.versionName}.apk" }
        }  // 会生成 debug-1.0.apk 或者 release-1.0.apk 这样的形式
        // project.extensions.getByType(com.android.build.gradle.AppExtension)
    }

    private void test2(Project project) {
        project.dependencies {
            api "org.aspectj:aspectjrt:1.8.9"  // ${versions.aspectj}
            final def log = project.logger
            log.error "========================";
            log.error "Aspectj切片开始编织Class!";
            log.error "========================";
            project.android.applicationVariants.all { variant ->
                def javaCompile = variant.javaCompile
                javaCompile.doLast {
                    String[] args = ["-showWeaveInfo",
                                     "-1.8",
                                     "-inpath", javaCompile.destinationDir.toString(),
                                     "-aspectpath", javaCompile.classpath.asPath,
                                     "-d", javaCompile.destinationDir.toString(),
                                     "-classpath", javaCompile.classpath.asPath,
                                     "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)]
                    log.debug "ajc args: " + Arrays.toString(args)
                    MessageHandler handler = new MessageHandler(true);
                    new Main().run(args, handler);
                    for (IMessage message : handler.getMessages(null, true)) {
                        switch (message.getKind()) {
                            case IMessage.ABORT:
                            case IMessage.ERROR:
                            case IMessage.FAIL:
                                log.error message.message, message.thrown
                                break;
                            case IMessage.WARNING:
                                log.warn message.message, message.thrown
                                break;
                            case IMessage.INFO:
                                log.info message.message, message.thrown
                                break;
                            case IMessage.DEBUG:
                                log.debug message.message, message.thrown
                                break;
                        }
                    }
                }
            }
        }
    }

    private void test3(Project project) {
        // TODO: lastestVersionPlugin -- 检查每个依赖的最老版本
    }

    private void test4(Project project) {
        // TODO: 生成 versions.gradle
    }

    // https://www.jianshu.com/p/dfc4681f8090
    private void test5(Project project) {
        println('------------------ begin my-groovy-plugin-javassist ------------------')
        project.android.registerTransform(new JavassistTransform(project))
        println('------------------ end my-groovy-plugin-javassist ------------------')
    }

    @Override
    void apply(Project project) {
        test5(project)
    }
}
