package tk.ivybits.profiler;

import tk.ivybits.agent.Tools;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class ProfilingAgent implements ClassFileTransformer {

    private static Instrumentation instrumentation = null;
    private static ProfilingAgent transformer;

    public ProfilingAgent() {
        ProfilerContext.agent = this;
    }

    public static void agentmain(String string, Instrumentation instrument) {
        transformer = new ProfilingAgent();
        instrumentation = instrument;
        instrumentation.addTransformer(transformer);

        Class[] loaded = instrumentation.getInitiatedClasses(ClassLoader.getSystemClassLoader());

        for (Class c : loaded) {
            if (!c.isArray()) {
                try {
                    instrumentation.redefineClasses(new ClassDefinition(c, Tools.getBytesFromClass(c)));
                } catch (Exception ignored) {
                    // Some classes just aren't meant to be redefined.
                }
            }
        }
    }

    /**
     * Kills this agent
     */
    public void kill() {
        instrumentation.removeTransformer(transformer);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined, ProtectionDomain protectionDomain, byte[] classBuffer)
            throws IllegalClassFormatException {
        // We can only profile classes that we can see. If a class uses a custom
        // ClassLoader we will not be able to see it and crash if we try to
        // profile it.
        if (loader != ClassLoader.getSystemClassLoader()) {
            return classBuffer;
        }

        // Don't profile yourself, otherwise you'll die in a StackOverflow.
        if (className.startsWith("tk/ivybits/profiler") || className.startsWith("tk/ivybits/agent")) {
            return classBuffer;
        }

        return ProfilingTransformer.transform(className, classBuffer);
    }
}