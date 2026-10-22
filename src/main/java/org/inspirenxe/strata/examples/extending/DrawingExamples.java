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
package org.inspirenxe.strata.examples.extending;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.extension.CustomComponent;
import org.inspirenxe.strata.api.extension.Gradient;
import org.inspirenxe.strata.api.extension.MeasureContext;
import org.inspirenxe.strata.api.extension.Painter;
import org.inspirenxe.strata.api.extension.Shape;
import org.inspirenxe.strata.api.extension.Stroke;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.geometry.Size;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.inspirenxe.strata.api.visual.PixelBuffer;

import java.util.List;

/**
 * The extending guide's drawing examples: components that draw with the painter's lines, shapes, gradients, shadows,
 * round clips and pixel buffers.
 */
public final class DrawingExamples {

    private DrawingExamples() {
    }

    // --8<-- [start:chart]
    /**
     * A line chart. The grid lines are one device pixel thick at every display density, and the line is drawn from
     * one array that is refilled each frame.
     */
    public static final class LineChart extends CustomComponent<LineChart> {
        private static final Stroke LINE = new Stroke(1.5f, Stroke.Cap.ROUND, Stroke.Join.ROUND);

        private double[] values = new double[0];
        private float[] points = new float[0];

        public LineChart(final ComponentFactory factory) {
            super(factory);
        }

        public LineChart setValues(final double... values) {
            this.values = values.clone();
            this.points = new float[values.length * 2];
            // Only paint reads the values, so there is no layout to invalidate
            this.repaint();
            return this;
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
            return new Size(160, 80);
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            final float hairline = 1f / painter.pixelsPerUnit();
            for (int i = 1; i < 4; i++) {
                final float y = bounds.y() + bounds.height() * i / 4f;
                painter.fill(new Rect(bounds.x(), y, bounds.width(), hairline), 0x33FFFFFF);
            }
            final int count = this.values.length;
            for (int i = 0; i < count; i++) {
                this.points[2 * i] = bounds.x() + bounds.width() * i / Math.max(1, count - 1);
                this.points[2 * i + 1] = bounds.bottom() - (float) this.values[i] * bounds.height();
            }
            painter.polyline(this.points, count, LINE, 0xFF4FC3F7);
        }
    }
    // --8<-- [end:chart]

    // --8<-- [start:gauge]
    /**
     * A gauge: a three quarter ring with a round-capped arc over it.
     */
    public static final class Gauge extends CustomComponent<Gauge> {
        private static final Stroke RING = new Stroke(8f, Stroke.Cap.ROUND, Stroke.Join.MITER);
        // Angles are radians turning clockwise from 3 o'clock, so this starts at the lower left
        private static final float START = (float) (Math.PI * 0.75);
        private static final float SWEEP = (float) (Math.PI * 1.5);

        private float value;

        public Gauge(final ComponentFactory factory) {
            super(factory);
        }

        public Gauge setValue(final float value) {
            this.value = Math.max(0f, Math.min(1f, value));
            this.repaint();
            return this;
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
            return new Size(64, 64);
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            // A stroked arc lies inside its oval, so the ring never leaves the component's bounds
            painter.strokeArc(bounds, START, SWEEP, RING, 0xFF3A3D45);
            if (this.value > 0f) {
                painter.strokeArc(bounds, START, SWEEP * this.value, RING, 0xFF81C784);
            }
        }
    }
    // --8<-- [end:gauge]

    // --8<-- [start:card]
    /**
     * A card: a soft shadow, a rounded face and a header gradient that stays inside the rounded corners.
     */
    public static final class Card extends CustomComponent<Card> {
        private static final float RADIUS = 6f;
        private static final Stroke EDGE = Stroke.of(1f);
        // A gradient is a value: make it once, and every frame reuses it
        private static final Gradient HEADER = new Gradient((float) (Math.PI / 4), List.of(
                new Gradient.Stop(0f, 0xFF5C6BC0), new Gradient.Stop(0.5f, 0xFF7E57C2), new Gradient.Stop(1f, 0xFFEC407A)));

        public Card(final ComponentFactory factory) {
            super(factory);
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
            return new Size(120, 72);
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            // The shadow reaches 8 units past the card, so keep room around it, and draw it first
            painter.shadow(bounds.translate(0, 2), RADIUS, 8f, 0x66000000);
            painter.fillRoundRect(bounds, RADIUS, 0xFF2B2D31);
            final Rect header = new Rect(bounds.x(), bounds.y(), bounds.width(), 24);
            painter.clippedRound(bounds, RADIUS, () -> painter.fillGradient(header, HEADER));
            painter.strokeRect(bounds, RADIUS, EDGE, 0xFF4A4D55);
        }
    }
    // --8<-- [end:card]

    // --8<-- [start:shape]
    /**
     * A star and a ring, built once as shapes and drawn at any size by moving and scaling the painter.
     */
    public static final class Emblem extends CustomComponent<Emblem> {
        private static final Shape STAR = star();
        private static final Shape RING = ring();
        private static final Stroke OUTLINE = new Stroke(0.08f, Stroke.Cap.BUTT, Stroke.Join.ROUND);

        public Emblem(final ComponentFactory factory) {
            super(factory);
        }

        // A star around the origin with a radius of 1: ten points, outer and inner, starting at the top
        private static Shape star() {
            final Shape.Builder builder = Shape.builder();
            for (int i = 0; i < 10; i++) {
                final double angle = -Math.PI / 2 + i * Math.PI / 5;
                final float radius = i % 2 == 0 ? 1f : 0.4f;
                final float x = (float) (radius * Math.cos(angle));
                final float y = (float) (radius * Math.sin(angle));
                if (i == 0) {
                    builder.moveTo(x, y);
                } else {
                    builder.lineTo(x, y);
                }
            }
            return builder.close().build();
        }

        // Fills use the nonzero rule: the inner circle runs the other way, so it is a hole
        private static Shape ring() {
            return Shape.builder()
                    .arc(new Rect(-1f, -1f, 2f, 2f), 0f, (float) (2 * Math.PI)).close()
                    .arc(new Rect(-0.6f, -0.6f, 1.2f, 1.2f), 0f, (float) (-2 * Math.PI)).close()
                    .build();
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
            return new Size(48, 24);
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            final float half = bounds.height() / 2f;
            painter.translated(bounds.x() + half, bounds.y() + half, () -> painter.scaled(half, () -> {
                painter.fillShape(STAR, 0xFFFFD54F);
                painter.strokeShape(STAR, OUTLINE, 0xFF8D6E00);
            }));
            painter.translated(bounds.right() - half, bounds.y() + half, () -> painter.scaled(half,
                    () -> painter.fillShape(RING, 0xFF4FC3F7)));
        }
    }
    // --8<-- [end:shape]

    // --8<-- [start:pixels]
    /**
     * A heat map: pixels computed in code, written straight into a buffer's array and shown with hard edges.
     */
    public static final class HeatMap extends CustomComponent<HeatMap> {
        private final PixelBuffer buffer = PixelBuffer.of(16, 16);

        public HeatMap(final ComponentFactory factory) {
            super(factory);
        }

        /** Computes a new picture from a phase, on the UI thread. */
        public HeatMap update(final double phase) {
            final int[] pixels = this.buffer.array();
            for (int y = 0; y < this.buffer.height(); y++) {
                for (int x = 0; x < this.buffer.width(); x++) {
                    final double heat = 0.5 + 0.5 * Math.sin(phase + x * 0.5) * Math.cos(y * 0.4);
                    final int red = (int) (255 * heat);
                    pixels[y * this.buffer.width() + x] = 0xFF000000 | red << 16 | (255 - red) << 8;
                }
            }
            // Engines copy the pixels again only when the version moves
            this.buffer.changed();
            this.repaint();
            return this;
        }

        public long version() {
            return this.buffer.version();
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
            return new Size(64, 64);
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            // Pixel art stays sharp when it is not smoothed
            painter.smoothed(false, () -> painter.pixels(this.buffer, bounds, 0xFFFFFFFF));
        }
    }
    // --8<-- [end:pixels]

    // --8<-- [start:density]
    /**
     * An icon that picks a detailed sprite where the display has the pixels to show it. The size in units is the
     * same either way.
     */
    public static final class DetailIcon extends CustomComponent<DetailIcon> {
        private static final SpriteId PLAIN = SpriteId.of("examplemod", "icon/star");
        private static final SpriteId DETAILED = SpriteId.of("examplemod", "icon/star_detailed");

        public DetailIcon(final ComponentFactory factory) {
            super(factory);
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
            return new Size(16, 16);
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            // The icon is a fixed number of units wide: how many device pixels is that?
            final float pixels = bounds.width() * painter.pixelsPerUnit();
            painter.sprite(pixels >= 32 ? DETAILED : PLAIN, bounds, 0xFFFFFFFF);
        }
    }
    // --8<-- [end:density]
}
