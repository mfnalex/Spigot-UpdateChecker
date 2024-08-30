package com.jeff_media.updatechecker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadScheduler {
    // Create a ScheduledExecutorService with a single thread
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static ScheduledExecutorService runTask(Runnable task) {
        // Divide 1s -> 1000ms by the amount of ticks per seconds to execute this task every tick
        return runTask(task, 1000/20L, TimeUnit.MILLISECONDS);
    }

    /**
     * Run a task after a delay using {@link ThreadScheduler#scheduler}
     * @param task The task to run from the scheduler
     * @param delay The delay before the {@link Runnable} is run
     * @param timeUnit The time unit used for the delay
     * @return The used scheduler instance
     */
    public static ScheduledExecutorService runTask(Runnable task, long delay, TimeUnit timeUnit) {
        makeSchedulerIfDown();
        scheduler.schedule(task, delay, timeUnit);

        return scheduler;
    }

    /**
     * Repeat a task after a delay for a given period using {@link ThreadScheduler#scheduler}
     * @param task The task to run from the scheduler
     * @param delay The delay before the {@link Runnable} is run
     * @param period The period between each execution
     * @param timeUnit The time unit used for the delay and the period
     * @return The used scheduler instance
     */
    public static ScheduledExecutorService scheduleRepeatingTask(Runnable task, long delay, long period, TimeUnit timeUnit) {
        makeSchedulerIfDown();
        scheduler.scheduleAtFixedRate(task, delay, period, timeUnit);

        return scheduler;
    }

    /**
     * Stop the given scheduler
     * @param scheduler The scheduler to stop
     */
    public static void stopScheduler(ScheduledExecutorService scheduler) {
        if(!scheduler.isShutdown()) scheduler.shutdown();
    }

    private static void makeSchedulerIfDown() {
        if(scheduler.isShutdown()) scheduler = Executors.newScheduledThreadPool(1);
    }
}
