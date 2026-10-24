/*
 * Copyright (c) InspireNXE <https://github.com/InspireNXE>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.inspirenxe.strata.examples.adapter;

// --8<-- [start:scheduler]
import org.inspirenxe.strata.api.event.Subscription;
import org.inspirenxe.strata.spi.Scheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs screens' repeating tasks for an engine whose main loop calls {@link #runDue} once per frame, on the main
 * thread. An engine with a scheduler of its own wraps that instead.
 */
public final class ToyScheduler implements Scheduler {
    private final List<Task> tasks = new ArrayList<>();

    @Override
    public Subscription every(final long millis, final Runnable task) {
        if (millis <= 0L) {
            throw new IllegalArgumentException("A task's interval must be above 0 ms: " + millis);
        }
        final Task scheduled = new Task(millis * 1_000_000L, System.nanoTime() + millis * 1_000_000L, task);
        this.tasks.add(scheduled);
        // Closing removes the task, so it never runs again
        return () -> this.tasks.remove(scheduled);
    }

    /** Called by the engine once per frame, on the main thread. */
    public void runDue() {
        final long now = System.nanoTime();
        // A task may close itself while it runs
        for (final Task task : List.copyOf(this.tasks)) {
            if (this.tasks.contains(task) && task.due <= now) {
                // Runs missed while the engine was busy are dropped: the next one is an interval from now
                task.due = Math.max(task.due + task.interval, now + 1L);
                task.run.run();
            }
        }
    }

    private static final class Task {
        private final long interval;
        private final Runnable run;
        private long due;

        Task(final long interval, final long due, final Runnable run) {
            this.interval = interval;
            this.due = due;
            this.run = run;
        }
    }
}
// --8<-- [end:scheduler]
