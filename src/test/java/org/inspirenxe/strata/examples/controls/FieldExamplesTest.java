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
package org.inspirenxe.strata.examples.controls;

import org.inspirenxe.strata.api.component.Validation;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.RadioGroup;
import org.inspirenxe.strata.api.control.button.ToggleButton;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.Slider;
import org.inspirenxe.strata.api.control.field.TextArea;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.attribute.value.Validatable;
import org.inspirenxe.strata.api.input.Key;
import org.inspirenxe.strata.api.input.KeyAction;
import org.inspirenxe.strata.api.input.Modifier;
import org.inspirenxe.strata.api.input.Modifiers;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.state.State;
import org.inspirenxe.strata.api.theme.Themes;
import org.inspirenxe.strata.api.visual.PixelBuffer;
import org.inspirenxe.strata.core.StrataCore;
import org.inspirenxe.strata.examples.adapter.ToyPlatform;
import org.inspirenxe.strata.spi.ScreenHost;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs the controls guide's examples for buttons, validation and text editing.
 */
class FieldExamplesTest {

    private static ScreenHost host(final float width, final float height) {
        final ScreenHost host = StrataCore.createScreenHost(new ToyPlatform(), Themes.flat());
        host.resize(width, height);
        return host;
    }

    private static void press(final ScreenHost host, final Key key, final Modifiers modifiers) {
        host.key(key, KeyAction.PRESS, modifiers);
        host.key(key, KeyAction.RELEASE, modifiers);
    }

    @Test
    void aPixelBufferShowsAtTheImageSizeAndMovesItsVersionWhenRecolored() {
        final ScreenHost host = host(200, 200);
        final PixelBuffer cells = PixelBuffer.of(8, 8);
        final Image[] image = new Image[1];
        host.open(ui -> image[0] = FieldExamples.heatMap(ui, cells));
        host.layout();
        assertEquals(64f, image[0].bounds().width());
        assertSame(cells, image[0].pixels().orElseThrow());
        assertTrue(image[0].sprite().isEmpty());
        final long before = cells.version();
        FieldExamples.recolor(cells, i -> 0xFF000000 | i);
        assertEquals(before + 1, cells.version());
        assertEquals(0xFF000000 | 9, cells.get(1, 1));
    }

    @Test
    void enterPressesTheDefaultButtonAndEscapeTheCancelButton() {
        final ScreenHost host = host(400, 100);
        final List<String> log = new ArrayList<>();
        final StackPanel[] row = new StackPanel[1];
        host.open(ui -> row[0] = FieldExamples.signInRow(ui, () -> log.add("sign in"), () -> log.add("cancel")));
        host.layout();
        final TextBox box = (TextBox) row[0].children().getFirst();
        box.requestFocus();
        press(host, Key.ENTER, Modifiers.NONE);
        assertEquals(List.of("sign in"), log);
        press(host, Key.ESCAPE, Modifiers.NONE);
        assertEquals(List.of("sign in", "cancel"), log);
    }

    @Test
    void aLabelsMnemonicFocusesTheBoxAfterIt() {
        final ScreenHost host = host(400, 100);
        final StackPanel[] row = new StackPanel[1];
        host.open(ui -> row[0] = FieldExamples.nameField(ui));
        host.layout();
        final TextBox box = (TextBox) row[0].children().get(1);
        assertFalse(box.isFocused());
        press(host, Key.N, Modifiers.of(Modifier.ALT));
        assertTrue(box.isFocused());
    }

    @Test
    void aCommandGivesTheButtonItsTextItsEnabledStateAndItsAction() {
        final ScreenHost host = host(400, 100);
        final State<Boolean> canSave = State.of(false);
        final List<String> log = new ArrayList<>();
        final Button[] button = new Button[1];
        host.open(ui -> button[0] = FieldExamples.saveButton(ui, canSave, () -> log.add("saved")));
        host.layout();
        host.flush();
        assertEquals("Save", button[0].text().plain().replace("_", ""));
        assertFalse(button[0].isEnabled());
        canSave.set(true);
        host.flush();
        assertTrue(button[0].isEnabled());
        button[0].press();
        assertEquals(List.of("saved"), log);
    }

    @Test
    void aPairOfToggleButtonsAlwaysHasOneOn() {
        final ScreenHost host = host(400, 100);
        final List<String> log = new ArrayList<>();
        final StackPanel[] bar = new StackPanel[1];
        host.open(ui -> bar[0] = FieldExamples.formatBar(ui, log::add));
        host.layout();
        final ToggleButton bold = (ToggleButton) bar[0].children().get(0);
        final ToggleButton list = (ToggleButton) bar[0].children().get(1);
        final ToggleButton grid = (ToggleButton) bar[0].children().get(2);
        assertTrue(list.isSelected());

        bold.requestFocus();
        press(host, Key.SPACE, Modifiers.NONE);
        assertEquals(List.of("bold true"), log);

        grid.requestFocus();
        press(host, Key.SPACE, Modifiers.NONE);
        assertTrue(grid.isSelected());
        assertFalse(list.isSelected());

        // The one that is on refuses to turn itself off
        press(host, Key.SPACE, Modifiers.NONE);
        assertTrue(grid.isSelected());
    }

    @Test
    void aQuantityThatDoesNotConvertMarksTheBoxAndKeepsTheState() {
        final ScreenHost host = host(400, 200);
        final State<Integer> quantity = State.of(1);
        final List<String> orders = new ArrayList<>();
        final StackPanel[] form = new StackPanel[1];
        host.open(ui -> form[0] = FieldExamples.orderForm(ui, quantity, () -> orders.add("order")));
        host.layout();
        host.flush();
        final TextBox name = (TextBox) form[0].children().get(0);
        final TextBox amount = (TextBox) form[0].children().get(1);
        final Button order = (Button) form[0].children().get(2);
        assertEquals("1", amount.text());
        assertTrue(order.isEnabled());
        assertFalse(name.validation().isValid());

        amount.setText("abc");
        host.flush();
        assertEquals(Validation.Severity.ERROR, amount.validation().severity());
        assertEquals(1, quantity.get());
        assertFalse(order.isEnabled());

        amount.setText("200");
        host.flush();
        assertFalse(amount.validation().isValid());
        assertTrue(amount.validation().message().plain().contains("1"), amount.validation().message().plain());
        assertEquals(1, quantity.get());

        amount.setText("12");
        host.flush();
        assertTrue(amount.validation().isValid());
        assertEquals(12, quantity.get());
        assertTrue(order.isEnabled());

        name.setText("Ana");
        assertTrue(name.validation().isValid());
        final List<Validation> reported = new ArrayList<>();
        name.onEvent(Validatable.VALIDATED, event -> reported.add(event.value()));
        name.setText("");
        assertEquals(1, reported.size());
        assertEquals("Enter a name", reported.getFirst().message().plain());
    }

    @Test
    void typedTextUndoesByWordAndTheButtonsFollowTheHistory() {
        final ScreenHost host = host(400, 200);
        final StackPanel[] panel = new StackPanel[1];
        host.open(ui -> panel[0] = FieldExamples.noteField(ui));
        host.layout();
        final TextBox note = (TextBox) panel[0].children().get(0);
        final StackPanel buttons = (StackPanel) panel[0].children().get(1);
        final Button mention = (Button) buttons.children().get(0);
        final Button undo = (Button) buttons.children().get(1);
        final Button redo = (Button) buttons.children().get(2);
        assertFalse(undo.isEnabled());

        note.requestFocus();
        "ab cd".codePoints().forEach(codePoint -> host.charTyped(codePoint, Modifiers.NONE));
        assertEquals("ab cd", note.text());
        assertTrue(undo.isEnabled());
        assertFalse(redo.isEnabled());

        press(host, Key.Z, Modifiers.of(Modifier.CONTROL));
        assertEquals("ab ", note.text());
        press(host, Key.Z, Modifiers.of(Modifier.CONTROL));
        assertEquals("", note.text());
        assertTrue(redo.isEnabled());
        press(host, Key.Y, Modifiers.of(Modifier.CONTROL));
        assertEquals("ab ", note.text());

        mention.press();
        assertEquals("ab @Ana ", note.text());
        undo.press();
        assertEquals("ab ", note.text());
    }

    @Test
    void theStatusLineFollowsTheCaretAndGoToLineScrollsThere() {
        final ScreenHost host = host(400, 300);
        final StringBuilder script = new StringBuilder();
        for (int line = 1; line <= 30; line++) {
            script.append("line ").append(line).append(line < 30 ? "\n" : "");
        }
        final StackPanel[] panel = new StackPanel[1];
        host.open(ui -> panel[0] = FieldExamples.scriptEditor(ui, script.toString()));
        host.layout();
        final TextArea editor = (TextArea) panel[0].children().get(0);
        final StackPanel row = (StackPanel) panel[0].children().get(1);
        final Label status = (Label) row.children().get(0);
        final Button goTo = (Button) row.children().get(1);

        editor.select(5, 5);
        assertEquals("Line 1, column 6", status.text().plain());
        goTo.press();
        assertEquals("Line 20, column 1", status.text().plain());
        assertEquals(30, editor.lineCount());
        assertTrue(editor.acceptsTab());
    }

    @Test
    void theArrowListsEveryWord() {
        final ScreenHost host = host(400, 200);
        final TextBox[] box = new TextBox[1];
        host.open(ui -> box[0] = FieldExamples.countryBox(ui));
        host.layout();
        box[0].requestFocus();
        press(host, Key.DOWN, Modifiers.of(Modifier.ALT));
        assertEquals(5, box[0].shownSuggestions().size());
    }

    @Test
    void aSliderCommitsWhereTheUserStopped() {
        final ScreenHost host = host(400, 200);
        final List<Double> previewed = new ArrayList<>();
        final List<Double> saved = new ArrayList<>();
        final StackPanel[] panel = new StackPanel[1];
        host.open(ui -> panel[0] = FieldExamples.volume(ui, previewed::add, saved::add));
        host.layout();
        final Slider slider = (Slider) panel[0].children().get(1);
        slider.setValue(35);
        assertEquals(35, slider.value());
        assertEquals(List.of(35.0), previewed);
        assertEquals(List.of(35.0), saved);
        assertEquals(10.0, slider.majorTicks());
        assertEquals(1, slider.minorTicks());
    }

    @Test
    void rebuildingARadioGroupReportsOnceAndLeavesNothingSelected() {
        final ScreenHost host = host(400, 300);
        final StackPanel[] column = new StackPanel[1];
        final List<RadioGroup<String>> groups = new ArrayList<>();
        host.open(ui -> {
            final RadioGroup<String> made = ui.radioGroup();
            groups.add(made);
            column[0] = ui.stackPanel();
            FieldExamples.showChoices(made, column[0], List.of("Easy", "Hard"));
            return column[0];
        });
        host.layout();
        final RadioGroup<String> group = groups.getFirst();
        group.select(1);
        final List<Integer> reported = new ArrayList<>();
        group.onEvent(Selection.SELECTED, event -> reported.add(event.value()));
        FieldExamples.showChoices(group, column[0], List.of("Easy", "Normal", "Hard"));
        assertEquals(List.of(-1), reported);
        assertEquals(3, group.buttons().size());
        assertEquals(3, column[0].children().size());
        assertEquals(-1, group.selectedIndex());
        assertTrue(group.selected().isEmpty());
    }
}
