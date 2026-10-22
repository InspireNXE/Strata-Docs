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
package org.inspirenxe.strata.examples.input;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Validation;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.event.input.InputEvents;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.text.Text;

/**
 * The input and focus guide's examples.
 */
public final class FocusExamples {

    private FocusExamples() {
    }

    /**
     * A section of a form that checks its fields once the user has finished with all of them.
     *
     * @param ui the factory
     * @param name the name field, filled in by the caller
     * @param email the email field, filled in by the caller
     * @return the section
     */
    public static StackPanel contactSection(final ComponentFactory ui, final TextBox name, final TextBox email) {
        // --8<-- [start:focus-exit]
        final StackPanel section = ui.stackPanel().setSpacing(4).add(name).add(email);
        // Fires when focus leaves the section, not when it moves from the name to the email
        section.onEvent(InputEvents.FOCUS_EXIT, event -> {
            name.setValidation(name.text().isBlank() ? Validation.error(Text.of("Enter a name")) : Validation.VALID);
            email.setValidation(email.text().contains("@") ? Validation.VALID : Validation.error(Text.of("Enter an email address")));
        });
        // --8<-- [end:focus-exit]
        return section;
    }
}
