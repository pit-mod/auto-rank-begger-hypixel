package com.example.begger.scheduler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tick-based delayed task scheduler.
 * Replaces unsafe new Thread() + Thread.sleep() patterns.
 * All tasks execute on the main client thread via onTick().
 */
public class TickScheduler {

    private static class ScheduledTask {
        final Runnable task;
        final long executeAtMs;

        ScheduledTask(Runnable task, long executeAtMs) {
            this.task = task;
            this.executeAtMs = executeAtMs;
        }
    }

    private final List<ScheduledTask> pending = new ArrayList<>();

    /**
     * Schedule a task to run after a delay (in milliseconds).
     * The task will execute on the main client thread during the next tick
     * after the delay has elapsed.
     */
    public void schedule(Runnable task, long delayMs) {
        pending.add(new ScheduledTask(task, System.currentTimeMillis() + delayMs));
    }

    /**
     * Call this every tick from the orchestrator.
     * Executes and removes all tasks whose delay has elapsed.
     */
    public void tick(long now) {
        Iterator<ScheduledTask> it = pending.iterator();
        while (it.hasNext()) {
            ScheduledTask st = it.next();
            if (now >= st.executeAtMs) {
                try {
                    st.task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                it.remove();
            }
        }
    }

    /**
     * Cancel all pending tasks (e.g. on mod toggle-off).
     */
    public void clear() {
        pending.clear();
    }

    public boolean hasPending() {
        return !pending.isEmpty();
    }
}
