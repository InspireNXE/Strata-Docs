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

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.command.Command;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.component.Validation;
import org.inspirenxe.strata.api.control.attribute.text.TextEntry;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.attribute.value.Ranged;
import org.inspirenxe.strata.api.control.attribute.value.Toggle;
import org.inspirenxe.strata.api.control.attribute.value.Validatable;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.IconPlacement;
import org.inspirenxe.strata.api.control.button.RadioButton;
import org.inspirenxe.strata.api.control.button.RadioGroup;
import org.inspirenxe.strata.api.control.button.ToggleButton;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.Slider;
import org.inspirenxe.strata.api.control.field.TextArea;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.input.Key;
import org.inspirenxe.strata.api.input.KeyStroke;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.state.Converters;
import org.inspirenxe.strata.api.state.State;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.inspirenxe.strata.api.visual.PixelBuffer;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;

/**
 * The controls guide's examples for buttons, validation, text editing, lines in a text area, the editable combo box,
 * sliders with ticks and radio groups that are rebuilt.
 */
public final class FieldExamples {

    private FieldExamples() {
    }

    // --8<-- [start:pixels]
    static Image heatMap(final ComponentFactory ui, final PixelBuffer cells) {
        // The image is drawn at 64 by 64 units whatever the size of the buffer
        return ui.image().setPixels(cells).setImageSize(64, 64);
    }

    static void recolor(final PixelBuffer cells, final IntUnaryOperator shade) {
        // Write the array directly, then say the buffer changed so engines copy it again
        final int[] argb = cells.array();
        for (int i = 0; i < argb.length; i++) {
            argb[i] = shade.applyAsInt(i);
        }
        cells.changed();
    }
    // --8<-- [end:pixels]

    // --8<-- [start:defaultbuttons]
    static StackPanel signInRow(final ComponentFactory ui, final Runnable signIn, final Runnable cancel) {
        // Enter in the text box presses Sign in, and Escape presses Cancel, wherever focus is on this screen
        return ui.stackPanel(Orientation.HORIZONTAL).setSpacing(4)
                .add(ui.textBox().setWidth(120))
                .add(ui.button("_Sign in").setDefault(true).onEvent(Button.PRESSED, event -> signIn.run()))
                .add(ui.button("Cancel").setCancel(true).onEvent(Button.PRESSED, event -> cancel.run()));
    }
    // --8<-- [end:defaultbuttons]

    // --8<-- [start:mnemonic]
    static StackPanel nameField(final ComponentFactory ui) {
        // Alt+N moves focus to the text box after the label
        final Label caption = ui.label("_Name").setMnemonicParsing(true);
        return ui.stackPanel(Orientation.HORIZONTAL).setSpacing(4).add(caption).add(ui.textBox().setWidth(120));
    }
    // --8<-- [end:mnemonic]

    // --8<-- [start:command]
    static Button saveButton(final ComponentFactory ui, final State<Boolean> canSave, final Runnable save) {
        // The button takes the command's text, follows its enabled source and runs it after it is pressed
        final Command.Action command = Command.of("_Save", save)
                .withShortcut(KeyStroke.shortcut(Key.S))
                .withEnabled(canSave);
        return ui.button().setCommand(command);
    }
    // --8<-- [end:command]

    // --8<-- [start:togglebuttons]
    static StackPanel formatBar(final ComponentFactory ui, final Consumer<String> log) {
        final ToggleButton bold = ui.toggleButton("Bold")
                .setIcon(SpriteId.of("examplemod", "icon/star"))
                .onEvent(Toggle.TOGGLED, event -> log.accept("bold " + event.value()));

        // Two toggle buttons used as a pair. Turning one on turns the other off, and the one that is on refuses to
        // turn itself off, so one of them is always on.
        final ToggleButton list = ui.toggleButton("List").setSelected(true);
        final ToggleButton grid = ui.toggleButton("Grid");
        list.onEvent(Toggle.TOGGLE, event -> {
            if (!event.value()) {
                event.cancel();
            }
        });
        grid.onEvent(Toggle.TOGGLE, event -> {
            if (!event.value()) {
                event.cancel();
            }
        });
        list.onEvent(Toggle.TOGGLED, event -> {
            if (event.value()) {
                grid.setSelected(false);
            }
        });
        grid.onEvent(Toggle.TOGGLED, event -> {
            if (event.value()) {
                list.setSelected(false);
            }
        });
        return ui.stackPanel(Orientation.HORIZONTAL).setSpacing(4).add(bold).add(list).add(grid)
                .add(FieldExamples.ribbonButton(ui));
    }
    // --8<-- [end:togglebuttons]

    // --8<-- [start:ribbon]
    static Button ribbonButton(final ComponentFactory ui) {
        // The icon sits above the caption, as a ribbon draws it
        return ui.button("Paste")
                .setIcon(SpriteId.of("examplemod", "icon/star"))
                .setIconSize(24, 24)
                .setIconPlacement(IconPlacement.TOP);
    }
    // --8<-- [end:ribbon]

    // --8<-- [start:validation]
    static StackPanel orderForm(final ComponentFactory ui, final State<Integer> quantity, final Runnable place) {
        // The text follows the quantity through a converter. Text that is not a whole number from 1 to 64 marks the
        // box as an error with the converter's message and leaves the quantity alone.
        final State<Validation> quantityCheck = State.of(Validation.VALID);
        final TextBox amount = ui.textBox().setWidth(60)
                .bind(TextInput.TEXT.convert(Converters.integer(1, 64)), quantity)
                .bind(Validatable.VALIDATION, quantityCheck);

        // A validator checks a box after each change of its text
        final TextBox name = ui.textBox().setPlaceholder("Name")
                .setValidator(box -> box.text().isBlank()
                        ? Validation.error(Text.of("Enter a name"))
                        : Validation.VALID);

        final Button order = ui.button("Order")
                .bind(Component.ENABLED, quantityCheck.map(Validation::isValid))
                .onEvent(Button.PRESSED, event -> place.run());
        return ui.stackPanel().setSpacing(4).add(name).add(amount).add(order);
    }
    // --8<-- [end:validation]

    // --8<-- [start:undo]
    static StackPanel noteField(final ComponentFactory ui) {
        final TextBox note = ui.textBox().setWidth(160);
        final Button mention = ui.button("@Ana").onEvent(Button.PRESSED, event -> note.insert("@Ana "));
        final Button undo = ui.button("Undo").onEvent(Button.PRESSED, event -> note.undo());
        final Button redo = ui.button("Redo").onEvent(Button.PRESSED, event -> note.redo());

        // The buttons follow the history, which changes with every edit
        final Runnable refresh = () -> {
            undo.setEnabled(note.canUndo());
            redo.setEnabled(note.canRedo());
        };
        note.onEvent(TextInput.EDITED, event -> refresh.run());
        refresh.run();
        return ui.stackPanel().setSpacing(4).add(note)
                .add(ui.stackPanel(Orientation.HORIZONTAL).setSpacing(4).add(mention).add(undo).add(redo));
    }
    // --8<-- [end:undo]

    // --8<-- [start:lines]
    static StackPanel scriptEditor(final ComponentFactory ui, final String script) {
        final TextArea editor = ui.textArea().setText(script).setWrap(false).setAcceptsTab(true).setVisibleLines(10);
        final Label status = ui.label("Line 1, column 1");

        // The caret moves for typing, clicks, keys and code alike
        editor.onEvent(TextEntry.CARET_MOVED, event -> status.setText(
                "Line " + (editor.lineOf(event.value().caret()) + 1)
                        + ", column " + (editor.columnOf(event.value().caret()) + 1)));

        final Button goTo = ui.button("Go to line 20").onEvent(Button.PRESSED, event -> {
            final int start = editor.lineStart(Math.min(19, editor.lineCount() - 1));
            editor.select(start, start);
            editor.scrollToCaret();
            editor.requestFocus();
        });
        final Button top = ui.button("Top").onEvent(Button.PRESSED, event -> editor.setScrollY(0));
        return ui.stackPanel().setSpacing(4).add(editor)
                .add(ui.stackPanel(Orientation.HORIZONTAL).setSpacing(4).add(status).add(goTo).add(top));
    }
    // --8<-- [end:lines]

    // --8<-- [start:combo]
    static TextBox countryBox(final ComponentFactory ui) {
        // A text box with an arrow that lists every word, and completes as the user types. The user may still type
        // a country that is not in the list.
        return ui.textBox().setWidth(120).setDropDown(true)
                .setSuggestionProvider(TextBox.SuggestionProvider.words(
                        List.of("Canada", "Chile", "China", "Croatia", "Denmark")));
    }
    // --8<-- [end:combo]

    // --8<-- [start:ticks]
    static StackPanel volume(final ComponentFactory ui, final Consumer<Double> preview, final Consumer<Double> save) {
        // CHANGED reports every move of a drag. COMMITTED reports where the user stopped, so only that is saved.
        final Slider slider = ui.slider().setRange(0, 100).setValue(50)
                .setTicks(10, 1)
                .setSnapToTicks(true)
                .onEvent(Ranged.CHANGED, event -> preview.accept(event.value()))
                .onEvent(Slider.COMMITTED, event -> save.accept(event.value()));
        return ui.stackPanel().setSpacing(4).add(ui.label("Volume")).add(slider);
    }
    // --8<-- [end:ticks]

    // --8<-- [start:radios]
    static void showChoices(final RadioGroup<String> group, final StackPanel column, final List<String> choices) {
        // The group leaves its buttons where they are, so take them out of the container too
        for (final RadioButton button : group.buttons()) {
            column.remove(button);
        }
        group.clear();
        for (final String choice : choices) {
            column.add(group.add(choice, choice));
        }
    }
    // --8<-- [end:radios]
}
