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
package org.inspirenxe.strata.examples.binding;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.component.Validation;
import org.inspirenxe.strata.api.control.attribute.text.HasText;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.attribute.value.Validatable;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.event.Subscription;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.state.Converter;
import org.inspirenxe.strata.api.state.Conversion;
import org.inspirenxe.strata.api.state.Converters;
import org.inspirenxe.strata.api.state.Source;
import org.inspirenxe.strata.api.state.State;
import org.inspirenxe.strata.api.text.Text;

import java.util.function.Consumer;

/**
 * The binding guide's examples of converters, validation, watching and unbinding.
 */
public final class ConverterExamples {

    private ConverterExamples() {
    }

    // --8<-- [start:model]
    public static final class OrderModel {
        public final State<Integer> quantity = State.of(1);
        public final State<Validation> quantityCheck = State.of(Validation.VALID);
    }
    // --8<-- [end:model]

    /**
     * A quantity field whose text follows a whole number.
     *
     * @param ui the factory
     * @param order the model
     * @param save called when the Save button is pressed
     * @return the form
     */
    public static StackPanel quantityForm(final ComponentFactory ui, final OrderModel order, final Runnable save) {
        // --8<-- [start:quantity]
        final TextBox quantity = ui.textBox().setWidth(60)
                .bind(TextInput.TEXT.convert(Converters.integer(1, 64)), order.quantity)
                .bind(Validatable.VALIDATION, order.quantityCheck);
        final Label hint = ui.label().bind(HasText.TEXT, order.quantityCheck.map(Validation::message));
        final Button saveButton = ui.button("Save")
                .bind(Component.ENABLED, order.quantityCheck.map(Validation::isValid))
                .onEvent(Button.PRESSED, event -> save.run());
        // --8<-- [end:quantity]
        return ui.stackPanel().setSpacing(4).add(quantity).add(hint).add(saveButton);
    }

    /**
     * Save is on while every field of the form is valid.
     *
     * @param order the model
     * @param nameCheck the validation of a second field
     * @return the source to bind to Save's enabled state
     */
    public static Source<Boolean> allValid(final OrderModel order, final State<Validation> nameCheck) {
        // --8<-- [start:all-valid]
        final Source<Boolean> allValid = () -> nameCheck.get().isValid() && order.quantityCheck.get().isValid();
        // --8<-- [end:all-valid]
        return allValid;
    }

    /**
     * A converter written by hand: a percentage the model keeps as a fraction.
     *
     * @return the converter
     */
    public static Converter<Double, String> percent() {
        // --8<-- [start:converter]
        return new Converter<>() {
            @Override
            public String toView(final Double fraction) {
                return fraction == null ? "" : Math.round(fraction * 100) + "%";
            }

            @Override
            public Conversion<Double> toModel(final String text) {
                try {
                    final int percent = Integer.parseInt(text.replace("%", "").strip());
                    return percent >= 0 && percent <= 100
                            ? Conversion.valid(percent / 100.0)
                            : Conversion.invalid(Text.of("Enter 0 to 100"));
                } catch (final NumberFormatException e) {
                    return Conversion.invalid(Text.of("Enter a percentage"));
                }
            }
        };
        // --8<-- [end:converter]
    }

    /**
     * Code outside the screen that follows a state, and a screen that reacts to one.
     *
     * @param ui the factory
     * @param order the model
     * @param total receives the order's total, on the UI thread
     * @param saved receives each quantity, on the thread that set the state
     * @return the subscription that stops the second
     */
    public static Subscription watching(final ComponentFactory ui, final OrderModel order, final Consumer<Integer> total,
                                        final Consumer<Integer> saved) {
        // --8<-- [start:watch]
        // Called on the UI thread before the next frame, once for several changes, and not for the value it starts with
        ui.watch(() -> order.quantity.get() * 3, total);
        // Called at once, on whatever thread sets the state, until the subscription is closed
        final Subscription saving = order.quantity.subscribe(saved);
        // --8<-- [end:watch]
        return saving;
    }

    /**
     * Takes a binding off a field.
     *
     * @param quantity the bound field
     */
    public static void unbinding(final TextBox quantity) {
        // --8<-- [start:unbind]
        // The text stays as it is, and the state no longer follows it
        quantity.unbind(TextInput.TEXT);
        // --8<-- [end:unbind]
    }
}
