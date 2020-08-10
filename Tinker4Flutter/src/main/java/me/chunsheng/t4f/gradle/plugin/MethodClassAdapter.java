package me.chunsheng.t4f.gradle.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @description: 生成MethodClassAdapter类@
 * @author: weichunsheng
 * @version: 1.0
 */
public class MethodClassAdapter extends ClassVisitor implements Opcodes {

    private String mClassName;

    public MethodClassAdapter(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        this.mClassName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        //匹配FlutterMain
        if ("io/flutter/view/FlutterMain".equals(this.mClassName)) {
            if ("startInitialization".equals(name) && "(Landroid/content/Context;)V".equals(desc)) {
                //开始处理startInitialization
                return new FlutterMethodVisitor(mv);
            }
        }
        return mv;
    }
}
