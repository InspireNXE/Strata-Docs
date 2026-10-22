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
package org.inspirenxe.strata.examples.testing;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.screen.Screen;

import java.util.Locale;

/**
 * The testing guide's screen: a quantity, a Buy button and a total.
 */
public final class ShopScreen implements Screen {
    private final double price;

    public ShopScreen(final double price) {
        this.price = price;
    }

    // --8<-- [start:screen]
    @Override
    public Component<?> build(final ComponentFactory ui) {
        final TextBox quantity = ui.textBox().setId("quantity").setPlaceholder("Quantity");
        final Label total = ui.label("0.00").setId("total");
        final Button buy = ui.button("Buy").setTooltip("Adds the order")
                .onEvent(Button.PRESSED, event -> {
                    final int count = quantity.text().isEmpty() ? 0 : Integer.parseInt(quantity.text());
                    total.setText(String.format(Locale.ROOT, "%.2f", count * this.price));
                    ui.overlays().toast("Order added");
                });
        final Button cancel = ui.button("Cancel").setEnabled(false);
        return ui.stackPanel().setSpacing(4).setPadding(8)
                .add(quantity).add(buy).add(cancel).add(total);
    }
    // --8<-- [end:screen]
}
