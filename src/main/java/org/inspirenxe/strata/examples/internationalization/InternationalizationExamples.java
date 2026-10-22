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
package org.inspirenxe.strata.examples.internationalization;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.Spinner;
import org.inspirenxe.strata.api.extension.CustomComponent;
import org.inspirenxe.strata.api.extension.MeasureContext;
import org.inspirenxe.strata.api.extension.Painter;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.geometry.Size;
import org.inspirenxe.strata.api.layout.FlowDirection;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.spi.Translator;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * The internationalization guide's examples.
 */
public final class InternationalizationExamples {

    private InternationalizationExamples() {
    }

    /**
     * A translator over a map, for a language the examples need. Applications read their translations from files.
     */
    public record MapTranslator(Locale locale, Map<String, String> translations) implements Translator {
        @Override
        public Optional<String> translate(final String key) {
            return Optional.ofNullable(this.translations.get(key));
        }

        @Override
        public long version() {
            return 1L;
        }
    }

    // --8<-- [start:plural]
    /**
     * A count that reads "1 offer" and "3 offers". The translator picks the form from the language's rules.
     */
    public static final class OffersScreen implements Screen {
        @Override
        public Component<?> build(final ComponentFactory ui) {
            final Label summary = ui.label(Text.plural("shop.offers", 1, 1)).setId("summary");
            final Spinner count = ui.spinner().setRange(0, 100).setValue(1).setDecimals(0)
                    .onEvent(Spinner.CHANGED, event -> summary.setText(
                            Text.plural("shop.offers", Math.round(event.value()), Math.round(event.value()))));
            return ui.stackPanel().setSpacing(4).setPadding(8).add(count).add(summary);
        }
    }
    // --8<-- [end:plural]

    /**
     * The translations the plural example reads, in English.
     */
    public static Map<String, String> english() {
        // --8<-- [start:english]
        return Map.of(
                "shop.offers.one", "%s offer",
                "shop.offers.other", "%s offers");
        // --8<-- [end:english]
    }

    /**
     * The same keys for Russian, which has three forms for whole numbers.
     */
    public static Map<String, String> russian() {
        // --8<-- [start:russian]
        return Map.of(
                "shop.offers.one", "%s predlozhenie",
                "shop.offers.few", "%s predlozheniya",
                "shop.offers.many", "%s predlozheniy",
                "shop.offers.other", "%s predlozheniya");
        // --8<-- [end:russian]
    }

    // --8<-- [start:direction]
    /**
     * A back and a next button. In a right-to-left language the row mirrors by itself: Back sits at the right.
     */
    public static final class PagerScreen implements Screen {
        @Override
        public Component<?> build(final ComponentFactory ui) {
            final StackPanel row = ui.stackPanel(Orientation.HORIZONTAL).setSpacing(4).setPadding(8)
                    .add(ui.button("Back").setId("back"))
                    .add(ui.button("Next").setId("next"));
            // To force a direction for this row whatever the language is, set it. Everything inside follows.
            // row.setFlowDirection(FlowDirection.LEFT_TO_RIGHT);
            return row;
        }
    }
    // --8<-- [end:direction]

    // --8<-- [start:own]
    /**
     * A chevron that points toward the end of the line: right in a left-to-right panel and left in a right-to-left one.
     */
    public static final class Chevron extends CustomComponent<Chevron> {
        public Chevron(final ComponentFactory ui) {
            super(ui);
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
            return new Size(8f, 12f);
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            final boolean rightToLeft = this.flowDirection().isRightToLeft();
            // Rows of one unit: the tip is at the end of the line, the wide side at the start
            final int steps = 6;
            for (int i = 0; i < steps; i++) {
                final float width = 2f + i;
                final float x = rightToLeft ? bounds.x() : bounds.right() - width;
                painter.fill(new Rect(x, bounds.y() + i, width, 1f), 0xFF202020);
                painter.fill(new Rect(x, bounds.bottom() - 1f - i, width, 1f), 0xFF202020);
            }
        }
    }
    // --8<-- [end:own]

    /**
     * Forces a direction on a component.
     */
    public static void force(final Button button) {
        button.setFlowDirection(FlowDirection.RIGHT_TO_LEFT);
    }
}
