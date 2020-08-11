package me.chunsheng.t4f.gradle.plugin;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.ASM4;

/**
 * @author: weichunsheng
 * @version: 1.0
 */
public class FlutterMethodVisitor extends MethodVisitor {

    public FlutterMethodVisitor(MethodVisitor methodVisitor) {
        super(ASM4, methodVisitor);
    }

    @Override
    public void visitLineNumber(int line, Label label) {
        super.visitLineNumber(line, label);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
                                String desc, boolean itf) {
        super.visitMethodInsn(opcode, owner, name,
                desc, itf);
        if (name.equals("startInitialization")) {
            System.out.println("> Tinker4Flutter visitMethodInsn Done >_<");
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    "me/chunsheng/hookflutter/HookFlutter",
                    "hook",
                    "(Ljava/lang/Object;)V",
                    false);
        }
    }

}
