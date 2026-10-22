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
package org.inspirenxe.strata.examples.commands;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.command.Command;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.ToggleSwitch;
import org.inspirenxe.strata.api.control.menu.CheckMenuItem;
import org.inspirenxe.strata.api.control.menu.Menu;
import org.inspirenxe.strata.api.control.menu.MenuBar;
import org.inspirenxe.strata.api.event.Subscription;
import org.inspirenxe.strata.api.input.Key;
import org.inspirenxe.strata.api.input.KeyStroke;
import org.inspirenxe.strata.api.input.Modifier;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.state.State;
import org.inspirenxe.strata.api.text.Text;

/**
 * The commands guide's examples.
 */
public final class CommandExamples {

    private CommandExamples() {
    }

    /**
     * What the screens below edit.
     */
    public static final class Document {
        /** Whether there is anything to save. */
        public final State<Boolean> changed = State.of(false);
        /** Whether the editor draws its grid. */
        public final State<Boolean> grid = State.of(true);
        private int saves;

        /**
         * Writes the document.
         */
        public void save() {
            this.saves++;
            this.changed.set(false);
        }

        /**
         * Returns how many times the document was saved.
         *
         * @return the count
         */
        public int saves() {
            return this.saves;
        }
    }

    // --8<-- [start:save-command]
    /**
     * Makes the Save command: a caption with a mnemonic, Ctrl+S (Command+S where the platform uses Command), and
     * enabled while the document has changes.
     */
    public static Command.Action saveCommand(final Document document) {
        return Command.of("_Save", document::save)
                .withShortcut(KeyStroke.shortcut(Key.S))
                .withEnabled(document.changed);
    }
    // --8<-- [end:save-command]

    // --8<-- [start:save-screen]
    /**
     * A tool bar button and a File menu item that run the same command.
     */
    public static final class EditorScreen implements Screen {
        private final Document document;
        private Subscription shortcut;

        public EditorScreen(final Document document) {
            this.document = document;
        }

        @Override
        public Component<?> build(final ComponentFactory ui) {
            final Command.Action save = saveCommand(this.document);

            final MenuBar bar = ui.menuBar()
                    .addMenu("_File", ui.menu().addItem(ui.menuItem().setCommand(save)));
            final StackPanel toolbar = ui.stackPanel(Orientation.HORIZONTAL).setSpacing(4)
                    .add(ui.button().setCommand(save));

            // The menu bar already runs the shortcut of its items. A screen with no menu bar adds the command
            // itself, and a screen with both can add it too: the first match runs, once. Shortcuts stay until
            // their subscription is closed, so a screen that rebuilds closes the old one
            if (this.shortcut != null) {
                this.shortcut.close();
            }
            this.shortcut = ui.shortcuts().add(save);

            return ui.stackPanel().add(bar).add(toolbar);
        }

        @Override
        public void closed() {
            if (this.shortcut != null) {
                this.shortcut.close();
            }
        }
    }
    // --8<-- [end:save-screen]

    // --8<-- [start:toggle-screen]
    /**
     * A check menu item and a toggle switch that show and flip the same state.
     */
    public static final class ViewScreen implements Screen {
        private final Document document;

        public ViewScreen(final Document document) {
            this.document = document;
        }

        @Override
        public Component<?> build(final ComponentFactory ui) {
            final Command.Toggle grid = Command.toggle("Show _grid", this.document.grid)
                    .withShortcut(KeyStroke.shortcut(Key.G, Modifier.SHIFT));

            final CheckMenuItem item = ui.checkMenuItem().setCommand(grid);
            final MenuBar bar = ui.menuBar().addMenu("_View", ui.menu().addItem(item));
            final ToggleSwitch toggle = ui.toggleSwitch().setCommand(grid);
            final CheckBox box = ui.checkBox().setCommand(grid);

            return ui.stackPanel().add(bar).add(toggle).add(box);
        }
    }
    // --8<-- [end:toggle-screen]

    // --8<-- [start:scoped]
    /**
     * Gives a Ctrl+Enter shortcut to everything inside one panel, and shows the stroke as a hint.
     */
    public static Subscription sendFromThePanel(final ComponentFactory ui, final StackPanel composer,
                                                final Runnable send) {
        final KeyStroke stroke = KeyStroke.of(Key.ENTER, Modifier.CONTROL);
        final Text hint = ui.keyText(stroke);                  // "Ctrl+Enter" in core's English
        composer.add(ui.label(Text.of("Send with ").append(hint)));
        return ui.shortcuts().add(composer, Command.of("Send", send).withShortcut(stroke));
    }
    // --8<-- [end:scoped]

    // --8<-- [start:default]
    /**
     * A form whose Save button answers Enter and whose Cancel button answers Escape.
     */
    public static StackPanel form(final ComponentFactory ui, final Runnable save, final Runnable cancel) {
        return ui.stackPanel(Orientation.HORIZONTAL).setSpacing(4)
                .add(ui.button("_Save").setDefault(true).onEvent(
                        Button.PRESSED, event -> save.run()))
                .add(ui.button("Cancel").setCancel(true).onEvent(
                        Button.PRESSED, event -> cancel.run()));
    }
    // --8<-- [end:default]

    /**
     * Makes the menu the examples share, so tests can find it.
     *
     * @param ui the factory
     * @return an empty menu
     */
    static Menu emptyMenu(final ComponentFactory ui) {
        return ui.menu();
    }
}
