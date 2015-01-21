package com.easyinsight.userupload;

import com.easyinsight.util.TimeoutThreadPoolExecutor;

import java.util.concurrent.*;

/**
 * User: jamesboe
 * Date: 2/11/14
 * Time: 9:21 AM
 */
public class ReportThreadPool {
    private ThreadPoolExecutor tpe;
    private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();

    private static ReportThreadPool instance;

    public static void initialize() {
        ReportThreadPool pool = new ReportThreadPool();
        pool.tpe = new TimeoutThreadPoolExecutor(5, 5, 5, TimeUnit.MINUTES, pool.queue, 10, TimeUnit.MINUTES);
        instance = pool;
    }

    public void addActivity(Runnable runnable) {
        tpe.execute(runnable);
    }

    public void shutdown() {
        System.out.println("Shutting down report thread pool...");
        try {
            tpe.shutdown();
            tpe.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            // ignore
        }
        System.out.println("Shut down report thread pool.");
    }

    public void interruptActivity() {

    }

    public static ReportThreadPool instance() {
        return instance;
    }
}
