package server;

import java.io.File;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import tools.FilePrinter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public abstract class Timer
{
    private ScheduledThreadPoolExecutor ses;
    protected  String file;
    protected String name;
    
//    public void start() {
//        if (this.ses != null && !this.ses.isShutdown() && !this.ses.isTerminated()) {
//            return;
//        }
//        this.file = "Logs/Log_" + this.name + "_Except.rtf";
//        final String tname = this.name + Randomizer.nextInt();
//        final ThreadFactory thread = new ThreadFactory() {
//            private final AtomicInteger threadNumber = new AtomicInteger(1);
//
//            @Override
//            public Thread newThread(final Runnable r) {
//                final Thread t = new Thread(r);
//                t.setName(tname + "-Worker-" + this.threadNumber.getAndIncrement());
//                return t;
//            }
//        };
//        ses = new ScheduledThreadPoolExecutor(7, thread);
//        ses.setKeepAliveTime(10L, TimeUnit.MINUTES);
//        ses.allowCoreThreadTimeOut(true);
//        ses.setCorePoolSize(7);
//        ses.setMaximumPoolSize(14);
//        ses.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
//    }
public void start() {
    synchronized (this) {
        if (ses != null && !ses.isShutdown() && !ses.isTerminated()) {
            return;
        }

        // 确保旧的线程池已关闭
        if (ses != null) {
            ses.shutdownNow();
        }

        // 使用常量或配置文件管理文件路径
        this.file = "Logs/Log_" + this.name + "_Except.rtf";

        final String tname = this.name + "-" + UUID.randomUUID().toString().substring(0, 8);
        final ThreadFactory thread = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(final Runnable r) {
                final Thread t = new Thread(r);
                t.setName(tname + "-Worker-" + this.threadNumber.getAndIncrement());
                return t;
            }
        };

        try {
            ses = new ScheduledThreadPoolExecutor(7, thread);
            ses.setKeepAliveTime(10L, TimeUnit.MINUTES);
            ses.allowCoreThreadTimeOut(true);
            ses.setCorePoolSize(7);
            ses.setMaximumPoolSize(14);
            ses.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        } catch (Exception e) {
            // 记录异常日志
            System.err.println("Failed to initialize ScheduledThreadPoolExecutor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
    public void stop() {
        if (this.ses == null || this.ses.isShutdown() || this.ses.isTerminated()) {
            return;
        }

        try {
            // 请求关闭线程池，等待所有任务完成
            this.ses.shutdown();

            // 等待60秒，确保所有任务完成
            if (!this.ses.awaitTermination(5, TimeUnit.SECONDS)) {
                // 如果超时，则强制关闭线程池
                this.ses.shutdownNow();

                // 再次等待几秒钟以确保所有任务都已终止
                if (!this.ses.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("ScheduledThreadPoolExecutor did not terminate");
                }
            }
        } catch (InterruptedException e) {
            // 恢复被中断的状态
            Thread.currentThread().interrupt();
            // 强制关闭线程池
            this.ses.shutdownNow();
            FilePrinter.printError("Timer.txt", e);
        } catch (Exception e) {
            FilePrinter.printError("Timer.txt", e);
        }
    }
    public void shutdown() {
        if (ses != null) {
            ses.shutdown();
            try {
                if (!ses.awaitTermination(60, TimeUnit.SECONDS)) {
                    ses.shutdownNow();
                }
            } catch (InterruptedException e) {
                ses.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    public ScheduledFuture<?> register(final Runnable r, final long repeatTime, final long delay) {
        if (this.ses == null) {
            return null;
        }
        return this.ses.scheduleAtFixedRate((Runnable)new LoggingSaveRunnable(r, new File(this.file)), delay, repeatTime, TimeUnit.MILLISECONDS);
    }
    
    public ScheduledFuture<?> register(final Runnable r, final long repeatTime) {
        if (this.ses == null) {
            return null;
        }
        return this.ses.scheduleAtFixedRate((Runnable)new LoggingSaveRunnable(r, new File(this.file)), 0L, repeatTime, TimeUnit.MILLISECONDS);
    }

//    public ScheduledFuture<?> schedule(Runnable r, long delay) {
//        return this.ses == null ? null : this.ses.schedule(new LoggingSaveRunnable(r, this.file), delay, TimeUnit.MILLISECONDS);
//    }
public ScheduledFuture<?> schedule(Runnable r, long delay) {
    if (r == null) {
        throw new IllegalArgumentException("Runnable cannot be null");
    }
    if (delay < 0) {
        throw new IllegalArgumentException("Delay cannot be negative");
    }

    if (ses == null) {
      //  logger.warn("ScheduledExecutorService is not initialized, cannot schedule task.");
        FilePrinter.printError("ScheduledExecutorService.txt", "ScheduledExecutorService is not initialized, cannot schedule task.");

        return null;
    }

    return ses.schedule(new LoggingSaveRunnable(r, new File(this.file)), delay, TimeUnit.MILLISECONDS);
}
    public ScheduledFuture<?> scheduleAtTimestamp(final Runnable r, final long timestamp) {
        return this.schedule(r, timestamp - System.currentTimeMillis());
    }
    
    public static class WorldTimer extends Timer
    {
        private static final WorldTimer instance;
        
        private WorldTimer() {
            this.name = "Worldtimer";
        }
        
        public static WorldTimer getInstance() {
            return instance;
        }
        
        static {
            instance = new WorldTimer();
        }
    }
    
    public static class MapTimer extends Timer
    {
        private static final MapTimer instance;
        
        private MapTimer() {
            this.name = "Maptimer";
        }
        
        public static MapTimer getInstance() {
            return instance;
        }
        
        static {
            instance = new MapTimer();
        }
    }
    
    public static class BuffTimer extends Timer
    {
        private static final BuffTimer instance;
        
        private BuffTimer() {
            this.name = "Bufftimer";
        }
        
        public static BuffTimer getInstance() {
            return instance;
        }
        
        static {
            instance = new BuffTimer();
        }
    }
    
    public static class EventTimer extends Timer
    {
        private static final EventTimer instance;
        
        private EventTimer() {
            this.name = "Eventtimer";
        }
        
        public static EventTimer getInstance() {
            return instance;
        }
        
        static {
            instance = new EventTimer();
        }
    }
    
    public static class RespawnTimer extends Timer
    {
        private static RespawnTimer instance;
        
        private RespawnTimer() {
            this.name = "RespawnTimer";
        }
        
        public static RespawnTimer getInstance() {
            return instance;
        }
        
        static {
            instance = new RespawnTimer();
        }
    }
    
    public static class CloneTimer extends Timer
    {
        private static final CloneTimer instance;
        
        private CloneTimer() {
            this.name = "Clonetimer";
        }
        
        public static CloneTimer getInstance() {
            return instance;
        }
        
        static {
            instance = new CloneTimer();
        }
    }
    
    public static class EtcTimer extends Timer
    {
        private static final EtcTimer instance;
        
        private EtcTimer() {
            this.name = "Etctimer";
        }
        
        public static EtcTimer getInstance() {
            return instance;
        }
        
        static {
            instance = new EtcTimer();
        }
    }
    
    public static class MobTimer extends Timer
    {
        private static final MobTimer instance;
        
        private MobTimer() {
            this.name = "Mobtimer";
        }
        
        public static MobTimer getInstance() {
            return instance;
        }
        
        static {
            instance = new MobTimer();
        }
    }
    
    public static class CheatTimer extends Timer
    {
        private static final CheatTimer instance;
        
        private CheatTimer() {
            this.name = "Cheattimer";
        }
        
        public static CheatTimer getInstance() {
            return instance;
        }
        
        static {
            instance = new CheatTimer();
        }
    }
    
    public static class PingTimer extends Timer
    {
        private static final PingTimer instance;
        
        private PingTimer() {
            this.name = "Pingtimer";
        }
        
        public static PingTimer getInstance() {
            return instance;
        }
        
        static {
            instance = new PingTimer();
        }
    }

    public static class GuiTimer extends Timer {

        private static GuiTimer instance = new GuiTimer();

        private GuiTimer() {
            name = "GuiTimer";
        }

        public static GuiTimer getInstance() {
            return instance;
        }
    }
    
//    private static class LoggingSaveRunnable implements Runnable
//    {
//        Runnable r;
//        String file;
//
//        public LoggingSaveRunnable(final Runnable r, final String file) {
//            this.r = r;
//            this.file = file;
//        }
//
//        @Override
//        public void run() {
//            try {
//                this.r.run();
//            }
//            catch (Exception t) {
//                t.printStackTrace();
//                FilePrinter.printError("Timer.txt", t);
//            }
//        }
//    }
private static class LoggingSaveRunnable implements Runnable {
    private final Runnable runnable;
    private final File file;

    public LoggingSaveRunnable(Runnable runnable, File file) {
        this.runnable = Objects.requireNonNull(runnable, "Runnable cannot be null");
        this.file = Objects.requireNonNull(file, "File cannot be null");
    }

    @Override
    public void run() {
        try {
            runnable.run();
        } catch (Exception e) {
            FilePrinter.printError("ScheduledExecutorService.txt", "Error occurred while running task:{}"+e.getMessage());

        }
    }
}
}
