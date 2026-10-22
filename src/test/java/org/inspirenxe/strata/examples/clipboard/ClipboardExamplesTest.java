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
package org.inspirenxe.strata.examples.clipboard;

import org.inspirenxe.strata.api.transfer.DataFormat;
import org.inspirenxe.strata.api.transfer.Transfer;
import org.inspirenxe.strata.examples.clipboard.ClipboardExamples.Listing;
import org.inspirenxe.strata.testing.ScreenTester;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The clipboard guide's examples, run.
 */
class ClipboardExamplesTest {

    @Test
    void aPasteInsideTheApplicationGetsTheObjects() {
        try (ScreenTester tester = ScreenTester.open(ui -> ui.stackPanel())) {
            final List<Listing> rows = List.of(new Listing("Ana", 12), new Listing("Ben", 7));

            ClipboardExamples.copy(tester.factory(), rows);

            assertEquals(rows, ClipboardExamples.paste(tester.factory()));
            final Transfer clipboard = tester.factory().clipboard().orElseThrow();
            assertTrue(clipboard.has(ClipboardExamples.LISTINGS));
            assertEquals(Optional.of("Ana\t12\nBen\t7\n"), clipboard.get(DataFormat.TEXT));
        }
    }

    @Test
    void textFromAnotherProgramIsReadAsLines() {
        try (ScreenTester tester = ScreenTester.open(ui -> ui.stackPanel())) {
            tester.factory().setClipboard(Transfer.text("Cleo,30\nDev,5"));

            assertEquals(List.of(new Listing("Cleo", 30), new Listing("Dev", 5)),
                    ClipboardExamples.paste(tester.factory()));
        }
    }
}
