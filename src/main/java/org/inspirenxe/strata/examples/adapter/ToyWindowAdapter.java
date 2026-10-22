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

import org.inspirenxe.strata.api.event.cause.Device;
import org.inspirenxe.strata.api.input.Modifiers;
import org.inspirenxe.strata.api.input.MouseButton;
import org.inspirenxe.strata.api.transfer.Transfer;
import org.inspirenxe.strata.spi.InputContext;
import org.inspirenxe.strata.spi.ScreenStack;

import java.nio.file.Path;
import java.util.List;

/**
 * The parts of the imaginary engine's adapter that a window, a display and other applications bring: density,
 * closing, drops, touches and drawing on demand. It drives the same kind of stack as {@link ToyAdapter}.
 */
public final class ToyWindowAdapter {
    private final ScreenStack screens;
    private final float scale;

    /**
     * Creates the adapter.
     *
     * @param screens the stack the engine's window shows
     * @param scale pixels per UI unit
     */
    public ToyWindowAdapter(final ScreenStack screens, final float scale) {
        this.screens = screens;
        this.scale = scale;
    }

    // --8<-- [start:density]
    /** Tells Strata the density when the window moves to another display or the user zooms. */
    public void onScaleChanged(final int widthPixels, final int heightPixels, final float displayScale) {
        // Device pixels per unit: this adapter's zoom times what the display adds on top
        this.screens.setPixelsPerUnit(this.scale * displayScale);
        // The area in units changes with the density, so it is set again beside it
        this.screens.resize(widthPixels / this.scale, heightPixels / this.scale);
    }
    // --8<-- [end:density]

    // --8<-- [start:closing]
    /** The engine's window close button. The window closes only when this returns true. */
    public boolean onCloseButton() {
        return this.screens.requestClose();
    }

    /** What the engine shows in its title bar, compared with the one shown on each frame. */
    public String windowTitle() {
        return this.screens.title().plain();
    }
    // --8<-- [end:closing]

    // --8<-- [start:drops]
    /** Files dropped on the window from the desktop, which the engine reports without a drag before them. */
    public boolean onFilesDropped(final int x, final int y, final List<Path> files) {
        return this.screens.dropped(x / this.scale, y / this.scale, Transfer.files(files), Modifiers.NONE).isPresent();
    }
    // --8<-- [end:drops]

    // --8<-- [start:touch]
    /** A touch going down. A touch is a left button press from a device that does not hover. */
    public boolean onTouchDown(final int x, final int y, final int touchId) {
        return this.screens.pointerButton(x / this.scale, y / this.scale, MouseButton.LEFT, true, Modifiers.NONE,
                new InputContext(Device.TOUCH, touchId, 1f));
    }
    // --8<-- [end:touch]

    // --8<-- [start:frames]
    /** Wires on-demand drawing for an engine that repaints only when asked. */
    public void drawOnDemand(final Runnable scheduleRedraw) {
        // Runs once each time the stack goes from clean to dirty, on any thread, so it only schedules a redraw
        this.screens.setFrameRequester(scheduleRedraw);
    }

    /** Whether the engine should draw now: something changed, or a timed change such as a caret blink is due. */
    public boolean frameDue(final long nowNanos) {
        return this.screens.needsFrame() || nowNanos >= this.screens.nextFrameNanos();
    }
    // --8<-- [end:frames]
}
