package tk.ivybits.profiler;

import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class ProfilingTransformer {
    public static byte[] transform(String className, byte[] classBuffer) {
        byte[] result = classBuffer;
        try {
            // Create class reader from buffer
            ClassReader reader = new ClassReader(classBuffer);
            // Make writer
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            ClassAdapter profiler = new ProfileClassAdapter(writer, className);
            // Add the class adapter as a modifier
            reader.accept(profiler, 0);
            result = writer.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Profiling class adapter.
     */
    public static class ProfileClassAdapter extends ClassAdapter {

        private String undecorated;

        public ProfileClassAdapter(ClassVisitor visitor, String clazz) {
            super(visitor);
            try {
                undecorated = Class.forName(undecorated = clazz.replace("/", ".")).getSimpleName();
            } catch (ClassNotFoundException e) {
                undecorated = clazz;
            }
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new ProfileMethodAdapter(super.visitMethod(access, name, desc, signature, exceptions), undecorated + "/" + name);
        }
    }

    // The method adapter
    public static class ProfileMethodAdapter extends MethodAdapter {
        private String name;

        public ProfileMethodAdapter(MethodVisitor visitor, String name) {
            super(visitor);
            this.name = name;
        }

        public void visitCode() {
            // Push values onto stack, then invoke the profile function
            visitLdcInsn(name);
            visitMethodInsn(INVOKESTATIC,
                    "tk/ivybits/profiler/ProfilerContext",
                    "enterMethod",
                    "(Ljava/lang/String;)V");
            super.visitCode();
        }

        public void visitInsn(int inst) {
            switch (inst) {
                // Match all return codes
                case ARETURN:
                case DRETURN:
                case FRETURN:
                case IRETURN:
                case LRETURN:
                case RETURN:
                case ATHROW:
                    visitLdcInsn(name);
                    visitMethodInsn(INVOKESTATIC,
                            "tk/ivybits/profiler/ProfilerContext",
                            "exitMethod",
                            "(Ljava/lang/String;)V");
                default:
                    break;
            }
            super.visitInsn(inst);
        }
    }
}
