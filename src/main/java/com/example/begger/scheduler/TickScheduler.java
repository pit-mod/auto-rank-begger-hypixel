package com.example.begger.scheduler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    public void schedule(Runnable task, long delayMs) {
        pending.add(new ScheduledTask(task, System.currentTimeMillis() + delayMs));
    }

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

    public void clear() {
        pending.clear();
    }
}
