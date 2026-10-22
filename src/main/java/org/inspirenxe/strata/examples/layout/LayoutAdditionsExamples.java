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
package org.inspirenxe.strata.examples.layout;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.extension.CustomComponent;
import org.inspirenxe.strata.api.extension.MeasureContext;
import org.inspirenxe.strata.api.extension.Painter;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.geometry.Size;
import org.inspirenxe.strata.api.layout.FlowDirection;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.layout.WrapPanel;
import org.inspirenxe.strata.api.text.TextStylePatch;

import java.util.List;

/**
 * The layout guide's examples of the wrap panel, baseline alignment, inherited text styles and right to left
 * layout.
 */
public final class LayoutAdditionsExamples {

    private LayoutAdditionsExamples() {
    }

    // --8<-- [start:wrap-panel]
    /**
     * Tags that wrap onto new rows as the panel narrows.
     */
    public static WrapPanel tags(final ComponentFactory ui, final List<String> names) {
        // 4 units between neighbors in a row, and 4 between rows
        final WrapPanel tags = ui.wrapPanel().setSpacing(4, 4);
        for (final String name : names) {
            tags.add(ui.button(name));
        }
        return tags;
    }
    // --8<-- [end:wrap-panel]

    // --8<-- [start:baseline]
    /**
     * A form row whose texts share one baseline, although the text box pads its text and the labels do not.
     */
    public static StackPanel priceRow(final ComponentFactory ui) {
        final StackPanel row = ui.stackPanel(Orientation.HORIZONTAL).setSpacing(4);
        row.add(ui.label("Price").setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.BASELINE));
        row.add(ui.textBox().setWidth(60).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.BASELINE));
        row.add(ui.label("per unit").setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.BASELINE));
        return row;
    }
    // --8<-- [end:baseline]

    // --8<-- [start:text-style]
    /**
     * A footer whose text, in every control inside it, is gray. The controls keep the rest of their style.
     */
    public static StackPanel footer(final ComponentFactory ui) {
        final StackPanel footer = ui.stackPanel(Orientation.HORIZONTAL).setSpacing(8);
        footer.add(ui.label("Updated today"));
        footer.add(ui.label("3 offers"));
        return footer.setTextStyle(TextStylePatch.of().color(0xFF808080));
    }
    // --8<-- [end:text-style]

    // --8<-- [start:direction]
    /**
     * A bar that fills from the start of its line: the left in English, the right in Arabic or Hebrew.
     */
    public static final class Meter extends CustomComponent<Meter> {
        private float fraction;

        public Meter(final ComponentFactory factory) {
            super(factory);
        }

        public Meter setFraction(final float fraction) {
            this.fraction = Math.max(0f, Math.min(1f, fraction));
            this.repaint();
            return this;
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
            return new Size(100, 8);
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            final float width = bounds.width() * this.fraction;
            // The component's own direction, else its parent's, else the language's. Bounds are real
            // coordinates, so a component that has parts places them itself.
            final float x = this.flowDirection() == FlowDirection.RIGHT_TO_LEFT ? bounds.right() - width : bounds.x();
            painter.fill(new Rect(x, bounds.y(), width, bounds.height()), 0xFF81C784);
        }
    }

    /**
     * A panel shown right to left whatever the language, whose rows and meter mirror with it.
     */
    public static StackPanel mirrored(final ComponentFactory ui) {
        final StackPanel panel = ui.stackPanel().setSpacing(4);
        panel.add(ui.label("Stock"));
        panel.add(new Meter(ui).setFraction(0.4f));
        return panel.setFlowDirection(FlowDirection.RIGHT_TO_LEFT);
    }
    // --8<-- [end:direction]
}
