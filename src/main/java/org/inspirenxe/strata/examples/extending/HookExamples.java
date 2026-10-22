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
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.event.Subscription;
import org.inspirenxe.strata.api.event.input.DragEvent;
import org.inspirenxe.strata.api.event.input.TransferEvent;
import org.inspirenxe.strata.api.extension.ArrangeContext;
import org.inspirenxe.strata.api.extension.CustomComponent;
import org.inspirenxe.strata.api.extension.CustomContainer;
import org.inspirenxe.strata.api.extension.MeasureContext;
import org.inspirenxe.strata.api.extension.Painter;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.geometry.Size;
import org.inspirenxe.strata.api.text.TextSpan;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.api.transfer.DataFormat;
import org.inspirenxe.strata.api.transfer.DropEffect;
import org.inspirenxe.strata.api.transfer.Transfer;

import java.util.Set;
import java.util.function.DoubleConsumer;

/**
 * The extending guide's examples of the lifecycle, style state, drag and baseline hooks.
 */
public final class HookExamples {

    private HookExamples() {
    }

    // --8<-- [start:lifecycle]
    /**
     * Something that pushes prices while anyone listens.
     */
    public interface PriceFeed {
        Subscription subscribe(DoubleConsumer listener);
    }

    /**
     * A chart that listens to a feed only while it is shown.
     */
    public static final class PriceChart extends CustomComponent<PriceChart> {
        private final PriceFeed feed;
        private Subscription subscription;
        private volatile double last;

        public PriceChart(final ComponentFactory factory, final PriceFeed feed) {
            super(factory);
            this.feed = feed;
        }

        @Override
        protected void onAttached() {
            // Runs every time the component joins a shown tree, after its theme and bindings are set
            this.subscription = this.feed.subscribe(price -> {
                this.last = price;
                // The feed may call from any thread, and repaint may be called from any thread
                this.repaint();
            });
        }

        @Override
        protected void onDetached() {
            // Runs every time it leaves, so a collapsed expander or a closed screen stops the feed
            if (this.subscription != null) {
                this.subscription.unsubscribe();
                this.subscription = null;
            }
        }

        public double last() {
            return this.last;
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
            return new Size(120, 40);
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            final float height = (float) Math.max(0, Math.min(1, this.last)) * bounds.height();
            painter.fill(new Rect(bounds.x(), bounds.bottom() - height, bounds.width(), height), 0xFF81C784);
        }
    }
    // --8<-- [end:lifecycle]

    // --8<-- [start:while-attached]
    /**
     * A label that follows a feed while it is shown, without a class of its own.
     */
    public static Label priceLabel(final ComponentFactory ui, final PriceFeed feed) {
        final Label label = ui.label("-");
        // Called now if the label is attached, on every attach, and closed on every detach
        return label.whileAttached(() -> feed.subscribe(price -> ui.uiThread().execute(
                () -> label.setText(String.format("%.2f", price)))));
    }
    // --8<-- [end:while-attached]

    // --8<-- [start:states]
    /**
     * A card whose look comes from the theme, for each of the states it can be in. A theme that has
     * {@code examplemod.card.warning} shows it while the card is in the warning state, and so on.
     */
    public static final class StatusCard extends CustomComponent<StatusCard> {
        private static final String CARD = "examplemod.card";
        private static final String CARD_TEXT = "examplemod.card.text";

        private final TextSpan span;

        public StatusCard(final ComponentFactory factory, final String text) {
            super(factory);
            this.span = TextSpan.of(text);
        }

        public StatusCard setWarning(final boolean warning) {
            // The look changes, and the size does not, so a repaint is enough
            this.setStyleState("warning", warning);
            this.repaint();
            return this;
        }

        public StatusCard setSelected(final boolean selected) {
            this.setStyleState("selected", selected);
            this.repaint();
            return this;
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
            return new Size(context.text().width(this.span) + 16, context.text().lineHeight(this.span.style()) + 8);
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            // Disabled, the component's own states, pressed, hovered and focused are tried before the plain key
            this.stylePaint(CARD, Paint.solid(0xFF2B2D31)).paint(painter, bounds);
            final TextStyle style = this.styleText(CARD_TEXT, TextStyle.DEFAULT);
            painter.text(this.span.withStyle(style), bounds.x() + 8, bounds.y() + 4);
        }
    }
    // --8<-- [end:states]

    // --8<-- [start:drag]
    /**
     * A tile that can be dragged out as text.
     */
    public static final class Tile extends CustomComponent<Tile> {
        private final String name;

        public Tile(final ComponentFactory factory, final String name) {
            super(factory);
            this.name = name;
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
            return new Size(32, 32);
        }

        @Override
        protected void onDragStart(final DragEvent event) {
            // The drag now carries the name. Targets hear DRAG_OVER and DROP, and this tile hears TRANSFER_DONE.
            event.startTransfer(Transfer.text(this.name),
                    Set.of(DropEffect.COPY, DropEffect.MOVE));
        }

        @Override
        protected void onTransferDone(final TransferEvent event) {
            // The effect is empty when nothing took the drop
        }
    }

    /**
     * A bin that takes text and keeps the last name it was given.
     */
    public static final class Bin extends CustomComponent<Bin> {
        private String last = "";
        private boolean hot;

        public Bin(final ComponentFactory factory) {
            super(factory);
        }

        public String last() {
            return this.last;
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
            return new Size(48, 48);
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            painter.fill(bounds, this.hot ? 0xFF4A4D55 : 0xFF2B2D31);
        }

        @Override
        protected void onDragEnter(final TransferEvent event) {
            this.hot = event.transfer().has(DataFormat.TEXT);
            this.repaint();
        }

        @Override
        protected void onDragOver(final TransferEvent event) {
            // Accept only what the bin can use; a drag with other formats finds no target here
            if (event.transfer().has(DataFormat.TEXT)) {
                event.accept(DropEffect.COPY);
            } else {
                event.reject();
            }
        }

        @Override
        protected void onDragExit(final TransferEvent event) {
            this.hot = false;
            this.repaint();
        }

        @Override
        protected void onDrop(final TransferEvent event) {
            event.transfer().get(DataFormat.TEXT).ifPresent(text -> this.last = text);
            this.hot = false;
            this.repaint();
            event.consume();
        }
    }
    // --8<-- [end:drag]

    // --8<-- [start:baseline]
    /**
     * A tag that draws one line of text inside some padding and tells its parent where the text sits.
     */
    public static final class Tag extends CustomComponent<Tag> {
        private static final float PADDING = 3f;

        private final TextSpan span;
        private float ascent;

        public Tag(final ComponentFactory factory, final String text) {
            super(factory);
            this.span = TextSpan.of(text);
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
            // Cache the ascent here, for baseline() to use
            this.ascent = context.text().ascent(this.span.style());
            return new Size(context.text().width(this.span) + 2 * PADDING,
                    context.text().lineHeight(this.span.style()) + 2 * PADDING);
        }

        @Override
        protected float baseline() {
            return PADDING + this.ascent;
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            painter.fill(bounds, 0xFF3A3D45);
            painter.text(this.span, bounds.x() + PADDING, bounds.y() + PADDING);
        }
    }

    /**
     * A row that puts the baselines of its children on one line, whatever their heights.
     */
    public static final class BaselineRow extends CustomContainer<BaselineRow> {
        private float above;

        public BaselineRow(final ComponentFactory factory) {
            super(factory);
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
            float width = 0f;
            float above = 0f;
            float below = 0f;
            for (final Component<?> child : this.children()) {
                final Size outer = context.measure(child, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
                // The baseline is from the top of the child's margin, or NaN for none: use the bottom then
                final float baseline = Float.isNaN(context.baseline(child)) ? outer.height() : context.baseline(child);
                above = Math.max(above, baseline);
                below = Math.max(below, outer.height() - baseline);
                width += outer.width();
            }
            this.above = above;
            return new Size(width + this.padding().horizontal(), above + below + this.padding().vertical());
        }

        @Override
        protected void arrange(final ArrangeContext context, final Rect bounds) {
            final Rect content = this.contentBox();
            float x = content.x();
            for (final Component<?> child : this.children()) {
                final Size outer = context.outerSize(child);
                final float baseline = Float.isNaN(context.baseline(child)) ? outer.height() : context.baseline(child);
                context.arrange(child, new Rect(x, content.y() + this.above - baseline, outer.width(), outer.height()));
                x += outer.width();
            }
        }
    }
    // --8<-- [end:baseline]

    // --8<-- [start:clock]
    /**
     * A clock face that shows the seconds since it was made. Its look changes with the clock, so it asks for the
     * frame at which the next second starts.
     */
    public static final class Stopwatch extends CustomComponent<Stopwatch> {
        private long started = -1;

        public Stopwatch(final ComponentFactory factory) {
            super(factory);
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
            return new Size(context.text().width(TextSpan.of("00:00")), context.text().lineHeight(TextStyle.DEFAULT));
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            // Read time through the environment, never from the system, so tests and every engine agree
            final long now = this.environment().millis();
            if (this.started < 0) {
                this.started = now;
            }
            final long elapsed = now - this.started;
            final long seconds = elapsed / 1000;
            painter.text(TextSpan.of(String.format("%02d:%02d", seconds / 60, seconds % 60)), bounds.x(), bounds.y());
            // A host that draws only when asked draws again when the next second starts. Ask again on that frame.
            this.repaintAfter(1000 - elapsed % 1000);
        }
    }
    // --8<-- [end:clock]
}
