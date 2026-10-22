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

import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.ToggleSwitch;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.input.Key;
import org.inspirenxe.strata.api.input.Modifier;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.examples.commands.CommandExamples;
import org.inspirenxe.strata.examples.commands.CommandExamples.Document;
import org.inspirenxe.strata.testing.ScreenTester;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that the commands guide's examples do what the page says.
 */
class CommandExamplesTest {

    @Test
    void saveFollowsItsEnabledStateAndRunsOnceFromTheButtonAndTheShortcut() {
        final Document document = new Document();
        try (ScreenTester tester = ScreenTester.open(new CommandExamples.EditorScreen(document))) {
            final Button save = tester.find(Button.class, button -> true);
            assertEquals("_Save", save.text().plain());
            assertFalse(save.isEnabled());

            // A disabled command runs nothing, from the key or the button
            tester.press(Key.S, Modifier.CONTROL);
            assertEquals(0, document.saves());

            document.changed.set(true);
            tester.frame();
            assertTrue(save.isEnabled());
            // The shortcut is in the host's shortcuts and in the menu bar's item, and still runs once
            tester.press(Key.S, Modifier.CONTROL);
            assertEquals(1, document.saves());
            assertFalse(document.changed.get());

            document.changed.set(true);
            tester.frame();
            tester.click(save);
            assertEquals(2, document.saves());
        }
    }

    @Test
    void theMenuBarRunsItsItemsShortcutsWithoutBeingAskedTo() {
        final Document document = new Document();
        try (ScreenTester tester = ScreenTester.open(new CommandExamples.ViewScreen(document))) {
            final ToggleSwitch toggle = tester.find(ToggleSwitch.class, item -> true);
            final CheckBox box = tester.find(CheckBox.class, item -> true);
            assertTrue(toggle.isSelected());
            assertTrue(box.isSelected());

            tester.press(Key.G, Modifier.CONTROL, Modifier.SHIFT);
            assertFalse(document.grid.get());
            assertFalse(toggle.isSelected());
            assertFalse(box.isSelected());

            // The state is the only truth: flipping it from code moves every control that shows it
            document.grid.set(true);
            tester.frame();
            assertTrue(toggle.isSelected());
            assertTrue(box.isSelected());

            tester.click(toggle);
            assertFalse(document.grid.get());
            assertFalse(box.isSelected());
        }
    }

    @Test
    void aScopedShortcutRunsOnlyFromInsideItsComponent() {
        final List<String> sent = new ArrayList<>();
        final TextBox[] inside = new TextBox[1];
        final Button[] outside = new Button[1];
        try (ScreenTester tester = ScreenTester.open(ui -> {
            final StackPanel composer = ui.stackPanel();
            inside[0] = ui.textBox();
            composer.add(inside[0]);
            CommandExamples.sendFromThePanel(ui, composer, () -> sent.add("sent"));
            outside[0] = ui.button("Elsewhere");
            return ui.stackPanel().add(composer).add(outside[0]);
        })) {
            assertEquals(List.of("Send with Ctrl+Enter"), tester.drawnText().stream()
                    .filter(text -> text.startsWith("Send")).toList());

            inside[0].requestFocus();
            tester.press(Key.ENTER, Modifier.CONTROL);
            assertEquals(List.of("sent"), sent);

            outside[0].requestFocus();
            tester.press(Key.ENTER, Modifier.CONTROL);
            assertEquals(List.of("sent"), sent);
        }
    }

    @Test
    void enterAndEscapePressTheDefaultAndCancelButtons() {
        final List<String> log = new ArrayList<>();
        try (ScreenTester tester = ScreenTester.open(ui -> CommandExamples.form(ui, () -> log.add("save"),
                () -> log.add("cancel")))) {
            tester.press(Key.ENTER);
            tester.press(Key.ESCAPE);
            assertEquals(List.of("save", "cancel"), log);
        }
    }
}
