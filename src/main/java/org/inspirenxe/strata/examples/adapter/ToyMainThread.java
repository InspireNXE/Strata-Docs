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

// --8<-- [start:thread]
import org.inspirenxe.strata.api.text.TextMetrics;
import org.inspirenxe.strata.spi.Capabilities;
import org.inspirenxe.strata.spi.Clipboard;
import org.inspirenxe.strata.spi.Clock;
import org.inspirenxe.strata.spi.Conventions;
import org.inspirenxe.strata.spi.Platform;
import org.inspirenxe.strata.spi.TextInputSink;
import org.inspirenxe.strata.spi.ThemeSource;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

/**
 * The toy platform for an engine with a main thread that drains a task queue once per frame. It tells core which
 * thread is the UI thread and how to hand work to it.
 */
public final class ToyMainThread implements Platform {
    private final Platform toy = new ToyPlatform();
    private final Thread mainThread;
    private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();

    /**
     * Creates the platform.
     *
     * @param mainThread the engine's main thread, the one that calls the host
     */
    public ToyMainThread(final Thread mainThread) {
        this.mainThread = mainThread;
    }

    /** Called by the engine at the start of every frame, on the main thread. */
    public void runTasks() {
        Runnable task;
        while ((task = this.tasks.poll()) != null) {
            task.run();
        }
    }

    @Override
    public Optional<Executor> uiExecutor() {
        // Accepts tasks from any thread and runs them in order. A task handed over on the main thread waits for the
        // next runTasks, so it never runs inside a call into Strata.
        return Optional.of(this.tasks::add);
    }

    @Override
    public Optional<BooleanSupplier> uiThreadCheck() {
        // Cheap, answers from any thread and never blocks
        return Optional.of(() -> Thread.currentThread() == this.mainThread);
    }

    // The rest is the toy platform's
    @Override
    public TextMetrics textMetrics() {
        return this.toy.textMetrics();
    }

    @Override
    public Clipboard clipboard() {
        return this.toy.clipboard();
    }

    @Override
    public Clock clock() {
        return this.toy.clock();
    }

    @Override
    public TextInputSink textInput() {
        return this.toy.textInput();
    }

    @Override
    public Capabilities capabilities() {
        return this.toy.capabilities();
    }

    @Override
    public Conventions conventions() {
        return this.toy.conventions();
    }

    @Override
    public ThemeSource themes() {
        return this.toy.themes();
    }
}
// --8<-- [end:thread]
