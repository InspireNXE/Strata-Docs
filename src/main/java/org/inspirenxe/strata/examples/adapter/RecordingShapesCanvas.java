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

// --8<-- [start:shapes]
import org.inspirenxe.strata.api.extension.Gradient;
import org.inspirenxe.strata.api.extension.Shape;
import org.inspirenxe.strata.api.extension.Stroke;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.visual.PixelBuffer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * The recording canvas for an engine that can draw shapes. An adapter whose platform reports
 * {@code Capabilities.drawsShapes} implements every call below, and core stops drawing shapes from fills.
 */
public final class RecordingShapesCanvas extends RecordingCanvas {
    private final Deque<Boolean> smoothing = new ArrayDeque<>();
    // The engine's copy of each pixel buffer drawn, as a texture would be, and the version it was copied at
    private final Map<PixelBuffer, Long> copies = new IdentityHashMap<>();
    private boolean smooth = true;

    @Override
    public void pushSmoothing(final boolean smoothed) {
        this.smoothing.push(this.smooth);
        this.smooth = smoothed;
    }

    @Override
    public void popSmoothing() {
        this.smooth = this.smoothing.pop();
    }

    @Override
    public void line(final float x1, final float y1, final float x2, final float y2, final Stroke stroke,
                     final int argb) {
        this.record("line " + this.placeX(x1) + "," + this.placeY(y1) + " to " + this.placeX(x2) + ","
                + this.placeY(y2) + this.pen(stroke, argb));
    }

    @Override
    public void polyline(final float[] points, final int count, final Stroke stroke, final int argb) {
        // The array is only valid during this call, so everything needed is read now
        final StringBuilder through = new StringBuilder();
        for (int i = 0; i < count; i++) {
            through.append(' ').append(this.placeX(points[i * 2])).append(',').append(this.placeY(points[i * 2 + 1]));
        }
        this.record("polyline" + through + this.pen(stroke, argb));
    }

    @Override
    public void fillRoundRect(final Rect rect, final float radius, final int argb) {
        this.record("roundrect " + this.place(rect) + " r" + radius * this.currentScale() + this.shade(argb));
    }

    @Override
    public void strokeRect(final Rect rect, final float radius, final Stroke stroke, final int argb) {
        // The outline lies inside the rectangle
        this.record("strokerect " + this.place(rect) + " r" + radius * this.currentScale() + this.pen(stroke, argb));
    }

    @Override
    public void fillEllipse(final Rect oval, final int argb) {
        this.record("ellipse " + this.place(oval) + this.shade(argb));
    }

    @Override
    public void strokeEllipse(final Rect oval, final Stroke stroke, final int argb) {
        this.record("strokeellipse " + this.place(oval) + this.pen(stroke, argb));
    }

    @Override
    public void strokeArc(final Rect oval, final float startRadians, final float sweepRadians, final Stroke stroke,
                          final int argb) {
        // Angles turn clockwise on screen from the right of the oval, in radians
        this.record("arc " + this.place(oval) + " from " + startRadians + " by " + sweepRadians + this.pen(stroke, argb));
    }

    @Override
    public void fillShape(final Shape shape, final int argb) {
        // Shapes never change, so an engine can keep its own copy of each one, found by identity
        this.record("shape " + System.identityHashCode(shape) + this.shade(argb));
    }

    @Override
    public void strokeShape(final Shape shape, final Stroke stroke, final int argb) {
        this.record("strokeshape " + System.identityHashCode(shape) + this.pen(stroke, argb));
    }

    @Override
    public void fillGradient(final Rect rect, final Gradient gradient) {
        this.record("anglegradient " + this.place(rect) + " " + gradient.angleRadians() + " " + gradient.stops().size()
                + " stops");
    }

    @Override
    public void shadow(final Rect rect, final float radius, final float blur, final int argb) {
        this.record("shadow " + this.place(rect) + " blur " + blur * this.currentScale() + this.shade(argb));
    }

    @Override
    public void pushRoundClip(final Rect rect, final float radius) {
        // Popped by popClip, like any clip
        this.pushClip(rect);
        this.record("roundclip r" + radius * this.currentScale());
    }

    @Override
    public void pixels(final PixelBuffer buffer, final Rect rect, final int tint) {
        // Copy the pixels again only when the buffer's version moved
        final Long copied = this.copies.put(buffer, buffer.version());
        this.record((copied == null || copied != buffer.version() ? "upload " : "reuse ") + this.place(rect));
    }

    private String shade(final int argb) {
        return " " + Integer.toHexString(this.faded(argb)) + (this.smooth ? "" : " hard");
    }

    private String pen(final Stroke stroke, final int argb) {
        return " width " + stroke.width() * this.currentScale() + " " + stroke.cap() + " " + stroke.join()
                + this.shade(argb);
    }
}
// --8<-- [end:shapes]
