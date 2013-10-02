package tk.ivybits.profiler;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import tk.ivybits.agent.AgentLoader;
import tk.ivybits.agent.Tools;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

import static tk.ivybits.profiler.ProfilerContext.MethodRecord;


/**
 * Profiling test class.
 *
 * @author Tudor
 */
public class Profiler {

    public static final int AGENT = 0x1, LOADER = 0x02;

    static {
        Tools.loadAgentLibrary();
    }

    public static boolean isRunning() {
        return ProfilerContext.agent != null;
    }

    public static void start() throws AgentInitializationException, AgentLoadException, AttachNotSupportedException, IOException, NoSuchFieldException, IllegalAccessException {
        start(AGENT | LOADER);
    }

    public static void start(int flag) throws AgentInitializationException, AgentLoadException, AttachNotSupportedException, IOException, NoSuchFieldException, IllegalAccessException {
        if (isRunning()) {
            throw new IllegalStateException("agent is already running");
        }
        ProfilerContext.records.clear();
        if ((flag & AGENT) > 0) {
            AgentLoader.attachAgentToJVM(Tools.getCurrentPID(), ProfilingAgent.class, AgentLoader.class);
            ProfilerContext.agent.kill();
        }
    }

    private static Map<String, MethodRecord> sortByComparator(Map<String, MethodRecord> unsortMap) {
        List<Map.Entry<String, MethodRecord>> list = new LinkedList<Map.Entry<String, MethodRecord>>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, MethodRecord>>() {
            public int compare(Map.Entry<String, MethodRecord> o1, Map.Entry<String, MethodRecord> o2) {
                return new Long(o2.getValue().totalTime).compareTo(o1.getValue().totalTime);
            }
        });

        Map<String, MethodRecord> sortedMap = new LinkedHashMap<String, MethodRecord>();
        for (Map.Entry<String, MethodRecord> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public static void stop(OutputStream stream) {
        if (!isRunning())
            throw new IllegalStateException("agent is not running");
        ProfilerContext.agent.kill();
        ProfilerContext.agent = null;
        PrintStream out = new PrintStream(stream, false);
        out.printf("\n%-40s%-20s%-15s%s\n", "Method", "Time (μs)", "Calls", "Time (μs) / Call");
        out.println("-------------------------------------------------------------------------------------------");
        Map<String, MethodRecord> times = sortByComparator(ProfilerContext.records);

        for (Map.Entry<String, MethodRecord> rec : times.entrySet()) {
            MethodRecord record = rec.getValue();
            long t = record.totalTime / 1000;
            int calls = record.calls;
            out.printf("%-40s%-20d%-15d%.2f\n", rec.getKey().replace("/", "."), t, calls, t / (double) calls);
        }
        out.flush();

        ProfilerContext.records.clear();
    }

    public static void stop() {
        stop(System.out);
    }
}