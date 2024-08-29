package com.jeff_media.updatechecker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadScheduler {
    public static ScheduledExecutorService runTask(Runnable task) {
        return runTask(task, 1/20L, TimeUnit.MILLISECONDS);
    }

    public static ScheduledExecutorService runTask(Runnable task, long delay, TimeUnit timeUnit) {
        // Create a ScheduledExecutorService with a single thread
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.schedule(task, delay, timeUnit);

        return scheduler;
    }

    public static ScheduledExecutorService scheduleRepeatingTask(Runnable task, long delay, long period, TimeUnit timeUnit) {
        // Create a ScheduledExecutorService with a single thread
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(task, delay, period, timeUnit);

        return scheduler;
    }

    public static void stopScheduler(ScheduledExecutorService scheduler) {
        if(!scheduler.isShutdown()) scheduler.shutdown();
    }
}
