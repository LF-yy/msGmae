package tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CPUSampler
{
    private List<String> included;
    private static CPUSampler instance;
    private long interval;
    private SamplerThread sampler;
    private Map<StackTrace, Integer> recorded;
    private int totalSamples;
    
    public CPUSampler() {
        this.included = new LinkedList<String>();
        this.interval = 5L;
        this.sampler = null;
        this.recorded = new HashMap<StackTrace, Integer>();
        this.totalSamples = 0;
    }
    
    public static CPUSampler getInstance() {
        return CPUSampler.instance;
    }
    
    public void setInterval(final long millis) {
        this.interval = millis;
    }
    
    public void addIncluded(final String include) {
        for (final String alreadyIncluded : this.included) {
            if (include.startsWith(alreadyIncluded)) {
                return;
            }
        }
        this.included.add(include);
    }
    
    public void reset() {
        this.recorded.clear();
        this.totalSamples = 0;
    }
    
    public void start() {
        if (this.sampler == null) {
            (this.sampler = new SamplerThread()).start();
        }
    }
    
    public void stop() {
        if (this.sampler != null) {
            this.sampler.stop();
            this.sampler = null;
        }
    }
    
    public SampledStacktraces getTopConsumers() {
        final List<StacktraceWithCount> ret = new ArrayList<StacktraceWithCount>();
        final Set<Entry<StackTrace, Integer>> entrySet = this.recorded.entrySet();
        for (final Entry<StackTrace, Integer> entry : entrySet) {
            ret.add(new StacktraceWithCount((int)Integer.valueOf(entry.getValue()), (StackTrace)entry.getKey()));
        }
        Collections.sort(ret);
        return new SampledStacktraces(ret, this.totalSamples);
    }
    
    public void save(final Writer writer, final int minInvocations, final int topMethods) throws IOException {
        final SampledStacktraces topConsumers = this.getTopConsumers();
        final StringBuilder builder = new StringBuilder();
        builder.append("Top Methods:\n");
        for (int i = 0; i < topMethods && i < topConsumers.getTopConsumers().size(); ++i) {
            builder.append(((StacktraceWithCount)topConsumers.getTopConsumers().get(i)).toString(topConsumers.getTotalInvocations(), 1));
        }
        builder.append("\nStack Traces:\n");
        writer.write(builder.toString());
        writer.write(topConsumers.toString(minInvocations));
        writer.flush();
    }
    
    private void consumeStackTraces(final Map<Thread, StackTraceElement[]> traces) {
        for (final Entry<Thread, StackTraceElement[]> trace : traces.entrySet()) {
            final int relevant = this.findRelevantElement((StackTraceElement[])trace.getValue());
            if (relevant != -1) {
                final StackTrace st = new StackTrace((StackTraceElement[])trace.getValue(), relevant, ((Thread)trace.getKey()).getState());
                final Integer i = this.recorded.get((Object)st);
                ++this.totalSamples;
                if (i == null) {
                    this.recorded.put(st, Integer.valueOf(1));
                }
                else {
                    this.recorded.put(st, Integer.valueOf((int)i + 1));
                }
            }
        }
    }

    public static double getProcessCpuLoad() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        String osJson = JSON.toJSONString(operatingSystemMXBean);
        JSONObject jsonObject = JSON.parseObject(osJson);
        double processCpuLoad = jsonObject.getDouble("processCpuLoad") * 100.0;
        return processCpuLoad;
    }
    private int findRelevantElement(final StackTraceElement[] trace) {
        if (trace.length == 0) {
            return -1;
        }
        if (this.included.size() == 0) {
            return 0;
        }
        int firstIncluded = -1;
        for (final String myIncluded : this.included) {
            for (int i = 0; i < trace.length; ++i) {
                final StackTraceElement ste = trace[i];
                if (ste.getClassName().startsWith(myIncluded) && (i < firstIncluded || firstIncluded == -1)) {
                    firstIncluded = i;
                    break;
                }
            }
        }
        if (firstIncluded >= 0 && trace[firstIncluded].getClassName().equals((Object)"net.sf.odinms.tools.performance.CPUSampler$SamplerThread")) {
            return -1;
        }
        return firstIncluded;
    }
    
    static {
        CPUSampler.instance = new CPUSampler();
    }
    
    private static class StackTrace
    {
        private StackTraceElement[] trace;
        private State state;
        
        public StackTrace(final StackTraceElement[] trace, final int startAt, final State state) {
            this.state = state;
            if (startAt == 0) {
                this.trace = trace;
            }
            else {
                System.arraycopy((Object)trace, startAt, (Object)(this.trace = new StackTraceElement[trace.length - startAt]), 0, this.trace.length);
            }
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof StackTrace)) {
                return false;
            }
            final StackTrace other = (StackTrace)obj;
            if (other.trace.length != this.trace.length) {
                return false;
            }
            if (other.state != this.state) {
                return false;
            }
            for (int i = 0; i < this.trace.length; ++i) {
                if (!this.trace[i].equals((Object)other.trace[i])) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            int ret = 13 * this.trace.length + this.state.hashCode();
            for (final StackTraceElement ste : this.trace) {
                ret ^= ste.hashCode();
            }
            return ret;
        }
        
        public StackTraceElement[] getTrace() {
            return this.trace;
        }
        
        @Override
        public String toString() {
            return this.toString(-1);
        }
        
        public String toString(final int traceLength) {
            final StringBuilder ret = new StringBuilder("State: ");
            ret.append(this.state.name());
            if (traceLength > 1) {
                ret.append("\n");
            }
            else {
                ret.append(" ");
            }
            int i = 0;
            for (final StackTraceElement ste : this.trace) {
                if (++i > traceLength) {
                    break;
                }
                ret.append(ste.getClassName());
                ret.append("#");
                ret.append(ste.getMethodName());
                ret.append(" (Line: ");
                ret.append(ste.getLineNumber());
                ret.append(")\n");
            }
            return ret.toString();
        }
    }
    
    private class SamplerThread implements Runnable
    {
        private boolean running;
        private boolean shouldRun;
        private Thread rthread;
        
        private SamplerThread() {
            this.running = false;
            this.shouldRun = false;
        }
        
        public void start() {
            if (!this.running) {
                this.shouldRun = true;
                (this.rthread = new Thread((Runnable)this, "CPU Sampling Thread")).start();
                this.running = true;
            }
        }
        
        public void stop() {
            this.shouldRun = false;
            this.rthread.interrupt();
            try {
                this.rthread.join();
            }
            catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
        
        @Override
        public void run() {
            while (this.shouldRun) {
                CPUSampler.this.consumeStackTraces((Map<Thread, StackTraceElement[]>)(Map)Thread.getAllStackTraces());
                try {
                    Thread.sleep(interval);
                    continue;
                }
                catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
    
    public static class StacktraceWithCount implements Comparable<StacktraceWithCount>
    {
        private int count;
        private StackTrace trace;
        
        public StacktraceWithCount(final int count, final StackTrace trace) {
            this.count = count;
            this.trace = trace;
        }
        
        public int getCount() {
            return this.count;
        }
        
        public StackTraceElement[] getTrace() {
            return this.trace.getTrace();
        }
        
        @Override
        public int compareTo(final StacktraceWithCount o) {
            return -Integer.valueOf(this.count).compareTo(Integer.valueOf(o.count));
        }
        
        @Override
        public boolean equals(final Object oth) {
            if (!(oth instanceof StacktraceWithCount)) {
                return false;
            }
            final StacktraceWithCount o = (StacktraceWithCount)oth;
            return this.count == o.count;
        }
        
        @Override
        public String toString() {
            return this.count + " Sampled Invocations\n" + this.trace.toString();
        }
        
        private double getPercentage(final int total) {
            return (double)Math.round((double)this.count / (double)total * 10000.0) / 100.0;
        }
        
        public String toString(final int totalInvoations, final int traceLength) {
            return this.count + "/" + totalInvoations + " Sampled Invocations (" + this.getPercentage(totalInvoations) + "%) " + this.trace.toString(traceLength);
        }
    }
    
    public static class SampledStacktraces
    {
        List<StacktraceWithCount> topConsumers;
        int totalInvocations;
        
        public SampledStacktraces(final List<StacktraceWithCount> topConsumers, final int totalInvocations) {
            this.topConsumers = topConsumers;
            this.totalInvocations = totalInvocations;
        }
        
        public List<StacktraceWithCount> getTopConsumers() {
            return this.topConsumers;
        }
        
        public int getTotalInvocations() {
            return this.totalInvocations;
        }
        
        @Override
        public String toString() {
            return this.toString(0);
        }
        
        public String toString(final int minInvocation) {
            final StringBuilder ret = new StringBuilder();
            for (final StacktraceWithCount swc : this.topConsumers) {
                if (swc.getCount() >= minInvocation) {
                    ret.append(swc.toString(this.totalInvocations, Integer.MAX_VALUE));
                    ret.append("\n");
                }
            }
            return ret.toString();
        }
    }
}
