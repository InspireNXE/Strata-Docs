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

import org.inspirenxe.strata.api.control.attribute.popup.Closable;
import org.inspirenxe.strata.api.control.menu.Flyout;

import java.util.function.BooleanSupplier;

/**
 * The overlays guide's example of a popup that refuses to close.
 */
public final class PopupCloseExamples {

    private PopupCloseExamples() {
    }

    /**
     * Keeps a flyout open while its form has changes nobody has saved.
     *
     * @param flyout the flyout
     * @param unsaved whether the form holds unsaved changes
     */
    public static void keepOpenWhileUnsaved(final Flyout flyout, final BooleanSupplier unsaved) {
        // --8<-- [start:keep-open]
        flyout.onEvent(Closable.CLOSE, event -> {
            if (unsaved.getAsBoolean()) {
                event.cancel();
            }
        });
        // --8<-- [end:keep-open]
    }
}
