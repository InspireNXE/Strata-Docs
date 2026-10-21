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

// --8<-- [start:composite]
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.event.EventType;
import org.inspirenxe.strata.api.event.control.ValueEvent;
import org.inspirenxe.strata.api.extension.ArrangeContext;
import org.inspirenxe.strata.api.extension.CustomComposite;
import org.inspirenxe.strata.api.extension.MeasureContext;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.geometry.Size;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.VerticalAlignment;

/**
 * A number between a minus and a plus button. The buttons and the label are private parts: callers see one
 * component, cannot rearrange its parts, and hear {@link #CHANGED} instead of the buttons' own events.
 */
public final class Counter extends CustomComposite<Counter> {
    /**
     * Reported after the count changes, by the buttons or by {@link #setCount}.
     */
    public static final EventType<ValueEvent<Counter, Integer>> CHANGED =
            EventType.valueReport("examplemod.counter.changed");

    private static final float GAP = 2f;

    private final Label value;
    private int count;

    public Counter(final ComponentFactory factory) {
        super(factory);
        this.value = factory.label("0");
        this.value.setMinSize(24f, 0f);
        this.value.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
        // Button.PRESSED from these parts stops here, so a handler outside never sees which button it was
        this.addChild(factory.button("-").onEvent(Button.PRESSED, event -> this.setCount(this.count - 1)));
        this.addChild(this.value);
        this.addChild(factory.button("+").onEvent(Button.PRESSED, event -> this.setCount(this.count + 1)));
    }

    public int count() {
        return this.count;
    }

    public Counter setCount(final int count) {
        if (count != this.count) {
            final int previous = this.count;
            this.count = count;
            // New label text schedules a layout by itself
            this.value.setText(Integer.toString(count));
            this.report(CHANGED, count, previous);
        }
        return this.self();
    }

    @Override
    protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
        // A row of the three parts: widths add up, the tallest sets the height
        float width = 0f;
        float height = 0f;
        for (final Component<?> part : this.childList()) {
            final Size outer = context.measure(part, Float.POSITIVE_INFINITY, availableHeight);
            width += outer.width();
            height = Math.max(height, outer.height());
        }
        return new Size(width + GAP * (this.childList().size() - 1), height);
    }

    @Override
    protected void arrange(final ArrangeContext context, final Rect bounds) {
        // Each part gets the full height, and its own alignment places it inside
        float x = bounds.x();
        for (final Component<?> part : this.childList()) {
            final Size outer = context.outerSize(part);
            context.arrange(part, new Rect(x, bounds.y(), outer.width(), bounds.height()));
            x += outer.width() + GAP;
        }
    }
}
// --8<-- [end:composite]
