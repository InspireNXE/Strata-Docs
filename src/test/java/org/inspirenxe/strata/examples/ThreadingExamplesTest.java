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
package org.inspirenxe.strata.examples;

import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.event.cause.Origin;
import org.inspirenxe.strata.examples.threading.ThreadingExamples;
import org.inspirenxe.strata.examples.threading.ThreadingExamples.OffersScreen;
import org.inspirenxe.strata.testing.ScreenTester;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that the threading guide's examples do what the page says.
 */
class ThreadingExamplesTest {

    /**
     * Runs a task on a new thread and waits for it.
     */
    private static void onWorker(final Runnable task) {
        final Thread worker = new Thread(task, "worker");
        worker.start();
        try {
            worker.join();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void aChangeFromAnotherThreadThrowsAndTheHandOverRunsOnTheNextFrame() {
        final OffersScreen screen = new OffersScreen();
        try (ScreenTester tester = ScreenTester.open(screen)) {
            final AtomicReference<RuntimeException> failure = new AtomicReference<>();
            onWorker(() -> {
                try {
                    screen.offers().setItems(List.of("Bread"));
                } catch (final RuntimeException e) {
                    failure.set(e);
                }
            });
            assertInstanceOf(IllegalStateException.class, failure.get());
            assertTrue(failure.get().getMessage().contains("worker"));
            assertTrue(failure.get().getMessage().contains("UiThread.execute"));
            // The check comes at the first step that needs the UI thread, which can be after the data changed,
            // so a component that threw is not trusted: put it back from the UI thread
            screen.offers().setItems(List.of());

            // The hand-over queues the task, and the next frame runs it with the origin the task chose
            final List<Object> origins = new ArrayList<>();
            screen.latest().onEvent(TextInput.EDITED, event -> origins.add(event.cause().origin()));
            onWorker(() -> screen.onNetworkUpdate("Ana", List.of("Bread", "Milk")));
            assertEquals(List.of(), screen.offers().items());
            assertEquals(2, screen.count.get());
            tester.frame();
            assertEquals(List.of("Bread", "Milk"), screen.offers().items());
            assertEquals("Bread", screen.latest().text());
            assertEquals(List.of(new Origin.Remote("Ana")), origins);
        }
    }

    @Test
    void submitCompletesOnTheUiThreadWithTheResult() {
        final OffersScreen screen = new OffersScreen();
        try (ScreenTester tester = ScreenTester.open(screen)) {
            screen.offers().setItems(List.of("Bread", "Milk", "Eggs"));
            final var count = screen.offerCount();
            assertTrue(!count.isDone());
            tester.frame();
            assertEquals(3, count.join());
        }
    }

    @Test
    void aTreeBuiltOnAWorkerIsAddedOnTheUiThread() {
        final StackPanel[] parent = new StackPanel[1];
        try (ScreenTester tester = ScreenTester.open(ui -> parent[0] = ui.stackPanel())) {
            final StackPanel[] rows = new StackPanel[1];
            onWorker(() -> rows[0] = ThreadingExamples.buildRows(tester.factory(), List.of("Ana", "Ben")));
            ThreadingExamples.show(tester.factory(), parent[0], rows[0]);
            tester.frame();
            assertEquals(List.of("Ana", "Ben"), ThreadingExamples.texts(parent[0]));
        }
    }
}
