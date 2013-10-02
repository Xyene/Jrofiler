package tk.ivybits.profiler;

import java.util.HashMap;

public class ProfilerContext {
    static ProfilingAgent agent;
    static HashMap<String, MethodRecord> records = new HashMap<String, MethodRecord>();

    public static void enterMethod(String member) {
        MethodRecord record = records.get(member);
        if (record == null) {
            records.put(member, record = new MethodRecord());
        }
        record.startNano = System.nanoTime();
        record.calls++;
    }

    public static void exitMethod(String member) {
        MethodRecord record = records.get(member);
        long delta = System.nanoTime() - record.startNano;
        record.totalTime += delta;
        record.startNano = 0;
    }

    public static class MethodRecord {
        public long startNano;
        public long totalTime;
        public int calls;
    }
}
