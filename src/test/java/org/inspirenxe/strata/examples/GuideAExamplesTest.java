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
package org.inspirenxe.strata.examples;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.component.Validation;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.window.MessageBox;
import org.inspirenxe.strata.api.input.Key;
import org.inspirenxe.strata.api.input.KeyAction;
import org.inspirenxe.strata.api.input.Modifiers;
import org.inspirenxe.strata.api.component.Container;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.screen.ScreenHandle;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.state.Converters;
import org.inspirenxe.strata.api.state.Conversion;
import org.inspirenxe.strata.api.theme.Themes;
import org.inspirenxe.strata.core.StrataCore;
import org.inspirenxe.strata.examples.adapter.ToyPlatform;
import org.inspirenxe.strata.examples.binding.ConverterExamples;
import org.inspirenxe.strata.examples.events.LifecycleExamples;
import org.inspirenxe.strata.examples.input.FocusExamples;
import org.inspirenxe.strata.examples.screens.ClosingExamples;
import org.inspirenxe.strata.spi.ScreenHost;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the examples of the binding, events, screens and input guides.
 */
class GuideAExamplesTest {

    private static ScreenHost host() {
        final ScreenHost host = StrataCore.createScreenHost(new ToyPlatform(), Themes.flat());
        host.resize(400, 300);
        return host;
    }

    @Test
    void theQuantityFieldKeepsTheLastValidNumberAndDisablesSave() {
        final ScreenHost host = host();
        final ConverterExamples.OrderModel order = new ConverterExamples.OrderModel();
        final List<String> saves = new ArrayList<>();
        final StackPanel[] form = new StackPanel[1];
        host.open(factory -> form[0] = ConverterExamples.quantityForm(factory, order, () -> saves.add("save")));
        host.layout();
        final TextBox quantity = (TextBox) form[0].children().get(0);
        final Button save = (Button) form[0].children().get(2);

        // The text follows the state when the screen opens
        assertEquals("1", quantity.text());
        assertTrue(save.isEnabled());

        quantity.setText("12");
        host.flush();
        assertEquals(12, order.quantity.get());

        // Text that does not convert leaves the state alone, marks the field and turns Save off
        quantity.setText("abc");
        host.flush();
        assertEquals(12, order.quantity.get());
        assertFalse(order.quantityCheck.get().isValid());
        assertFalse(save.isEnabled());

        quantity.setText("99");
        host.flush();
        assertEquals(12, order.quantity.get());
        assertFalse(save.isEnabled());

        // A change in the model reaches the field and clears the error
        order.quantity.set(5);
        host.flush();
        assertEquals("5", quantity.text());
        assertTrue(order.quantityCheck.get().isValid());
        assertTrue(save.isEnabled());
    }

    @Test
    void watchAndSubscribeHearChangesAndUnbindStopsFollowing() {
        final ScreenHost host = host();
        final ConverterExamples.OrderModel order = new ConverterExamples.OrderModel();
        final List<Integer> totals = new ArrayList<>();
        final List<Integer> saved = new ArrayList<>();
        final TextBox[] field = new TextBox[1];
        host.open(factory -> {
            ConverterExamples.watching(factory, order, totals::add, saved::add);
            field[0] = factory.textBox().bind(org.inspirenxe.strata.api.control.attribute.text.TextInput.TEXT
                    .convert(Converters.integer()), order.quantity);
            return field[0];
        });
        host.layout();

        order.quantity.set(2);
        order.quantity.set(3);
        // The listener of subscribe ran at once, for each change, and watch has not run yet
        assertEquals(List.of(2, 3), saved);
        assertEquals(List.of(), totals);
        host.flush();
        // One call for the two changes, and none for the value it started with
        assertEquals(List.of(9), totals);

        ConverterExamples.unbinding(field[0]);
        order.quantity.set(7);
        host.flush();
        assertEquals("3", field[0].text());
    }

    @Test
    void aHandWrittenConverterTurnsFractionsIntoPercentages() {
        final var percent = ConverterExamples.percent();
        assertEquals("25%", percent.toView(0.25));
        assertEquals(Conversion.valid(0.5), percent.toModel(" 50 % "));
        assertFalse(percent.toModel("200").isValid());
        assertFalse(percent.toModel("lots").isValid());
    }

    @Test
    void aLabelFollowsTheFeedOnlyWhileItIsShown() {
        final ScreenHost host = host();
        final LifecycleExamples.PriceFeed feed = new LifecycleExamples.PriceFeed();
        final StackPanel[] root = new StackPanel[1];
        final Label[] price = new Label[1];
        host.open(factory -> {
            price[0] = LifecycleExamples.priceLabel(factory, feed);
            root[0] = factory.stackPanel();
            return root[0];
        });
        host.layout();
        // Not attached yet: the label has not subscribed
        feed.latest.set(1.0);
        host.flush();
        assertEquals("-", price[0].text().plain());

        root[0].add(price[0]);
        feed.latest.set(2.5);
        host.flush();
        assertEquals("2.50", price[0].text().plain());

        root[0].remove(price[0]);
        feed.latest.set(9.0);
        host.flush();
        assertEquals("2.50", price[0].text().plain());

        // Added again, it listens again
        root[0].add(price[0]);
        feed.latest.set(3.0);
        host.flush();
        assertEquals("3.00", price[0].text().plain());
    }

    @Test
    void parentsAttachFirstAndChildrenDetachFirst() {
        final ScreenHost host = host();
        final List<String> log = new ArrayList<>();
        final StackPanel[] root = new StackPanel[1];
        final StackPanel[] panel = new StackPanel[1];
        host.open(factory -> {
            root[0] = factory.stackPanel();
            panel[0] = LifecycleExamples.panelWithLabel(factory);
            LifecycleExamples.order(log, List.<Component<?>>of(panel[0], panel[0].children().getFirst()));
            return root[0];
        });
        host.layout();
        root[0].add(panel[0]);
        assertEquals(List.of("attached panel", "attached label"), log);
        log.clear();
        root[0].remove(panel[0]);
        assertEquals(List.of("detached label", "detached panel"), log);
    }

    @Test
    void theNoteScreenAsksBeforeDiscardingAndClosesOnYes() {
        final ScreenHost host = host();
        final ClosingExamples.NoteScreen screen = new ClosingExamples.NoteScreen();
        final Component<?>[] root = new Component<?>[1];
        final Screen recording = new Screen() {
            @Override
            public Component<?> build(final ComponentFactory factory) {
                return root[0] = screen.build(factory);
            }

            @Override
            public Text title() {
                return screen.title();
            }

            @Override
            public void opened(final ScreenHandle handle) {
                screen.opened(handle);
            }

            @Override
            public boolean closeRequested(final ScreenHandle handle) {
                return screen.closeRequested(handle);
            }
        };
        host.open(recording);
        host.layout();
        assertEquals("Note", host.title().plain());

        // Nothing changed: the window's close button gets its way
        assertTrue(host.requestClose());
        host.open(recording);
        host.layout();

        final Container<?> inner = (Container<?>) ((Container<?>) root[0]).children().getFirst();
        final TextBox note = (TextBox) inner.children().getFirst();
        note.setText("hello");
        assertEquals("Note (edited)", host.title().plain());
        assertFalse(host.requestClose());
        assertEquals(1, host.factory().overlays().open().size());
        assertTrue(host.screen().isPresent());

        // The box focuses its default button, Yes, and Enter answers it
        host.key(Key.ENTER, KeyAction.PRESS, Modifiers.NONE);
        assertTrue(host.screen().isEmpty());
        assertEquals("", host.title().plain());
    }

    @Test
    void aSectionHearsFocusLeaveOnceNotBetweenItsFields() {
        final ScreenHost host = host();
        final TextBox[] fields = new TextBox[2];
        final Button[] outside = new Button[1];
        host.open(factory -> {
            fields[0] = factory.textBox().setText("Ana");
            fields[1] = factory.textBox().setText("ana.example");
            outside[0] = factory.button("Next");
            return factory.stackPanel()
                    .add(FocusExamples.contactSection(factory, fields[0], fields[1]))
                    .add(outside[0]);
        });
        host.layout();
        fields[0].requestFocus();
        fields[1].requestFocus();
        assertEquals(Validation.VALID, fields[0].validation());
        assertEquals(Validation.VALID, fields[1].validation());

        outside[0].requestFocus();
        assertTrue(fields[0].validation().isValid());
        assertFalse(fields[1].validation().isValid());
    }
}
