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
package org.inspirenxe.strata.examples.overlays;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Tooltip;
import org.inspirenxe.strata.api.control.attribute.popup.Closable;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.window.MessageBox;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.event.input.InputEvents;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.overlay.Overlay;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.Paint;

import java.util.List;

/**
 * The overlays guide's examples, and the message box in the controls guide.
 */
public final class OverlayExamples {

    private OverlayExamples() {
    }

    static void confirmDelete(final ComponentFactory factory, final Runnable delete) {
        // --8<-- [start:message-box]
        factory.overlays().dialog(factory.messageBox("Delete", "Delete this exchange?", MessageBox.Buttons.YES_NO)
                .onEvent(MessageBox.ANSWERED, event -> {
                    if (event.value() == MessageBox.Result.YES) {
                        delete.run();
                    }
                }));
        // --8<-- [end:message-box]
    }

    static Overlay popup(final ComponentFactory factory, final Button more) {
        // --8<-- [start:popup]
        final AnchorPanel menu = factory.anchorPanel().add(factory.label("Popup content"));
        menu.setPadding(4).setBackground(Paint.solid(0xF0101010));
        final Overlay popup = factory.overlays().popup(menu, more.bounds());
        popup.onEvent(Closable.CLOSED, event -> more.setText("More"));
        // --8<-- [end:popup]
        return popup;
    }

    static void quantity(final ComponentFactory factory, final long max) {
        // --8<-- [start:dialog]
        final TextBox amount = factory.textBox().setText("1").setSelectAllOnFocus(true);
        final Button buy = factory.button("Buy");
        final Window window = factory.window("Enter a Quantity").add(amount);
        window.setWidth(130);
        amount.onEvent(TextBox.SUBMITTED, event -> buy.press());
        buy.onEvent(Button.PRESSED, event -> {
            // ... buy Long.parseLong(amount.text()) ...
            window.close();
        });
        factory.overlays().dialog(window);
        // --8<-- [end:dialog]
    }

    static Label tooltips(final ComponentFactory factory) {
        // --8<-- [start:tooltips]
        final Label price = factory.label("125.50/ea").setTooltip("Total: 1,506");

        price.setTooltip(Tooltip.lines(List.of(
                Text.of("Diamond"),
                Text.of("Example Mod").color(0xFF5555FF).italic())));
        // --8<-- [end:tooltips]
        return price;
    }

    static void modifiers(final AnchorPanel screen, final Button buyOne) {
        // --8<-- [start:modifiers]
        screen.onEvent(InputEvents.MODIFIERS_CHANGED, event ->
                buyOne.setText(event.modifiers().control() ? "Delist" : "Buy 1"));
        // --8<-- [end:modifiers]
    }
}
