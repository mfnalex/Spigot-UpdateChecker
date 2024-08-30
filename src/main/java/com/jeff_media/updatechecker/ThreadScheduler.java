package com.jeff_media.updatechecker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadScheduler {
    // Create a ScheduledExecutorService with a single thread
    public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static ScheduledExecutorService runTask(Runnable task) {
        // Divide 1s -> 1000ms by the amount of ticks per seconds
        return runTask(task, 1000/20L, TimeUnit.MILLISECONDS);
    }

    public static ScheduledExecutorService runTask(Runnable task, long delay, TimeUnit timeUnit) {
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
