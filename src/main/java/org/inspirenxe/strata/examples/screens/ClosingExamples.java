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
package org.inspirenxe.strata.examples.screens;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.window.MessageBox;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.screen.ScreenHandle;
import org.inspirenxe.strata.api.text.Text;

/**
 * The screens guide's examples of closing and titles.
 */
public final class ClosingExamples {

    private ClosingExamples() {
    }

    // --8<-- [start:editor]
    /**
     * A note editor that asks before it throws away what was typed.
     */
    public static final class NoteScreen implements Screen {
        private ScreenHandle handle;
        private boolean changed;

        @Override
        public Component<?> build(final ComponentFactory factory) {
            final TextBox note = factory.textBox().setWidth(200)
                    .onEvent(TextInput.EDITED, event -> {
                        this.changed = true;
                        this.handle.setTitle(Text.of("Note (edited)"));
                    });
            // Cancel asks the screen first, as the window's close button would
            final Button cancel = factory.button("Cancel").onEvent(Button.PRESSED, event -> this.handle.requestClose());
            return factory.anchorPanel()
                    .add(factory.stackPanel().setSpacing(4).add(note).add(cancel).setAnchor(Anchor.CENTER))
                    .setAnchor(Anchor.FILL);
        }

        @Override
        public Text title() {
            return Text.of("Note");
        }

        @Override
        public void opened(final ScreenHandle handle) {
            this.handle = handle;
        }

        @Override
        public boolean closeRequested(final ScreenHandle handle) {
            if (!this.changed) {
                return true;
            }
            // Refuse for now, ask, and close for good if the user agrees. close() never asks again.
            handle.factory().overlays().dialog(handle.factory()
                    .messageBox("Discard changes?", "The note has not been saved.", MessageBox.Buttons.YES_NO)
                    .onEvent(MessageBox.ANSWERED, event -> {
                        if (event.value().equals(MessageBox.Result.YES)) {
                            handle.close();
                        }
                    }));
            return false;
        }
    }
    // --8<-- [end:editor]
}
