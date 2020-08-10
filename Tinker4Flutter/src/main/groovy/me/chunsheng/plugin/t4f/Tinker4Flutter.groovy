package me.chunsheng.plugin.t4f

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @description: 插件入口类@
 * @author: weichunsheng
 * @version: 1.0
 */
class Tinker4Flutter implements Plugin<Project> {
    void apply(Project project) {
        try {
            def android = project.extensions.getByType(AppExtension.class)
            if (android == null) {
                println('> Tinker4Flutter Register Fail ')
            } else {
                android.registerTransform(new Tinker4FlutterTransform(project))
                println('> Tinker4Flutter Register Success ')
            }

        } catch (Exception e) {
            println('> Tinker4Flutter Register Exception ')
            e.printStackTrace()
        }
    }
}