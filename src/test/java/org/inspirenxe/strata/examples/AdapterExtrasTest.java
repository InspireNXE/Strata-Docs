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

import org.inspirenxe.strata.api.component.CursorKind;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.theme.Themes;
import org.inspirenxe.strata.api.visual.PixelBuffer;
import org.inspirenxe.strata.examples.adapter.MonospaceTextMetrics;
import org.inspirenxe.strata.examples.adapter.RecordingCanvas;
import org.inspirenxe.strata.examples.adapter.RecordingShapesCanvas;
import org.inspirenxe.strata.examples.adapter.ToyMainThread;
import org.inspirenxe.strata.examples.adapter.ToyPlatform;
import org.inspirenxe.strata.examples.adapter.ToyWindowAdapter;
import org.inspirenxe.strata.examples.quickstart.SettingsScreen;
import org.inspirenxe.strata.core.StrataCore;
import org.inspirenxe.strata.spi.Canvas;
import org.inspirenxe.strata.spi.ScreenHost;
import org.inspirenxe.strata.spi.ScreenStack;
import org.inspirenxe.strata.spi.testing.CanvasContract;
import org.inspirenxe.strata.spi.testing.PlatformContract;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Holds the example adapter's newer parts to the SPI: shapes, opacity, the UI thread, frame scheduling and closing.
 */
class AdapterExtrasTest {

    // --8<-- [start:contract]
    @Nested
    class Shapes extends CanvasContract {
        @Override
        protected Surface surface(final int width, final int height) {
            final RecordingShapesCanvas canvas = new RecordingShapesCanvas();
            canvas.begin();
            return new Surface() {
                @Override
                public Canvas canvas() {
                    return canvas;
                }
            };
        }

        @Override
        protected boolean drawsShapes() {
            return true;
        }

        @Override
        protected boolean drawsPixels() {
            return true;
        }
    }
    // --8<-- [end:contract]

    @Nested
    class MainThreadPlatform extends PlatformContract {
        @Override
        protected org.inspirenxe.strata.spi.Platform platform() {
            return new ToyMainThread(Thread.currentThread());
        }
    }

    @Test
    void theAscentStaysWithinTheLine() {
        final MonospaceTextMetrics metrics = new MonospaceTextMetrics();
        assertTrue(metrics.ascent(TextStyle.DEFAULT) > 0f);
        assertTrue(metrics.ascent(TextStyle.DEFAULT) <= metrics.lineHeight(TextStyle.DEFAULT));
    }

    @Test
    void opacityMultipliesIntoTheAlphaOfEveryColor() {
        final RecordingCanvas canvas = new RecordingCanvas();
        canvas.begin();
        canvas.pushOpacity(0.5f);
        canvas.pushOpacity(0.5f);
        canvas.fill(new Rect(0, 0, 4, 4), 0xFFFF0000);
        canvas.popOpacity();
        canvas.fill(new Rect(0, 0, 4, 4), 0xFFFF0000);
        canvas.popOpacity();
        canvas.fill(new Rect(0, 0, 4, 4), 0xFFFF0000);
        assertEquals(List.of("fill Rect[x=0.0, y=0.0, width=4.0, height=4.0] 40ff0000",
                "fill Rect[x=0.0, y=0.0, width=4.0, height=4.0] 80ff0000",
                "fill Rect[x=0.0, y=0.0, width=4.0, height=4.0] ffff0000"), canvas.commands());
    }

    @Test
    void theCanvasShowsTheBusyCursorAndLeavesTheRestToTheFallbacks() {
        final RecordingCanvas canvas = new RecordingCanvas();
        assertTrue(canvas.showsCursor(CursorKind.ARROW));
        assertTrue(canvas.showsCursor(CursorKind.WAIT));
        assertFalse(canvas.showsCursor(CursorKind.GRAB));
        assertEquals(CursorKind.HAND, CursorKind.GRAB.fallback());
    }

    @Test
    void aPixelBufferIsCopiedAgainOnlyWhenItsVersionMoves() {
        final RecordingShapesCanvas canvas = new RecordingShapesCanvas();
        canvas.begin();
        final PixelBuffer buffer = PixelBuffer.of(2, 2);
        final Rect rect = new Rect(0, 0, 8, 8);
        canvas.pixels(buffer, rect, 0xFFFFFFFF);
        canvas.pixels(buffer, rect, 0xFFFFFFFF);
        buffer.set(0, 0, 0xFFFF0000).changed();
        canvas.pixels(buffer, rect, 0xFFFFFFFF);
        assertTrue(canvas.commands().get(0).startsWith("upload"));
        assertTrue(canvas.commands().get(1).startsWith("reuse"));
        assertTrue(canvas.commands().get(2).startsWith("upload"));
    }

    @Test
    void tasksFromOtherThreadsRunOnTheMainThreadInOrder() throws InterruptedException {
        final ToyMainThread platform = new ToyMainThread(Thread.currentThread());
        final ScreenHost host = StrataCore.createScreenHost(platform, Themes.flat());
        host.resize(200, 100);
        final List<String> ran = new ArrayList<>();
        final Thread worker = new Thread(() -> {
            host.factory().uiThread().execute(() -> ran.add("first on " + host.factory().uiThread().isCurrent()));
            host.factory().uiThread().execute(() -> ran.add("second"));
        });
        worker.start();
        worker.join();
        assertTrue(ran.isEmpty(), "nothing runs until the engine drains its queue");
        platform.runTasks();
        assertEquals(List.of("first on true", "second"), ran);
    }

    @Test
    void aTaskHandedOverOnTheMainThreadWaitsForTheNextFrame() {
        final ToyMainThread platform = new ToyMainThread(Thread.currentThread());
        final ScreenHost host = StrataCore.createScreenHost(platform, Themes.flat());
        final AtomicInteger runs = new AtomicInteger();
        host.factory().uiThread().execute(runs::incrementAndGet);
        assertEquals(0, runs.get());
        platform.runTasks();
        assertEquals(1, runs.get());
    }

    @Test
    void theAdapterAsksForFramesAndClosesOnlyWhenTheScreensAgree() {
        final ScreenStack stack = StrataCore.createScreenStack(new ToyPlatform(), Themes.flat());
        final ToyWindowAdapter adapter = new ToyWindowAdapter(stack, 2f);
        final AtomicInteger requests = new AtomicInteger();
        adapter.drawOnDemand(requests::incrementAndGet);
        stack.resize(320, 200);
        stack.push(new SettingsScreen());
        stack.render(new RecordingCanvas());
        assertFalse(adapter.frameDue(0L), "a fresh frame leaves nothing to draw");
        assertTrue(requests.get() > 0, "the requester ran when the stack went dirty");
        adapter.drawFrame(new RecordingCanvas(), 0L);
        stack.resize(300, 200);
        assertFalse(adapter.frameDue(10_000_000L), "a needed frame waits for the frame rate's interval");
        assertTrue(adapter.frameDue(stack.frameIntervalNanos()), "and draws once it has passed");
        assertEquals("", adapter.windowTitle());
        assertTrue(adapter.onCloseButton(), "a screen that does not object closes");
        assertTrue(stack.isEmpty());
    }

    @Test
    void theAdapterTellsTheDensityAndTakesDropsAndTouches() {
        final ScreenStack stack = StrataCore.createScreenStack(new ToyPlatform(), Themes.flat());
        final ToyWindowAdapter adapter = new ToyWindowAdapter(stack, 2f);
        stack.resize(320, 200);
        stack.push(new SettingsScreen());
        stack.render(new RecordingCanvas());
        adapter.onScaleChanged(1280, 800, 2f);
        stack.render(new RecordingCanvas());
        assertFalse(adapter.onFilesDropped(10, 10, List.of(Path.of("a.txt"))), "nothing takes files here");
        adapter.onTouchDown(2, 2, 1);
    }
}
