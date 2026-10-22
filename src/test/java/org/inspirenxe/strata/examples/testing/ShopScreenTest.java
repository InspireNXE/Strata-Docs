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
package org.inspirenxe.strata.examples.testing;

import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.input.Key;
import org.inspirenxe.strata.api.input.Modifier;
import org.inspirenxe.strata.spi.Translator;
import org.inspirenxe.strata.testing.Matching;
import org.inspirenxe.strata.testing.ScreenTester;
import org.inspirenxe.strata.testing.TesterOptions;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The testing guide's tests. They need no engine and no display.
 */
class ShopScreenTest {

    // --8<-- [start:total]
    @Test
    void buyingThreeShowsTheTotal() {
        try (ScreenTester tester = ScreenTester.open(new ShopScreen(9.99))) {
            tester.click(tester.byId("quantity", TextBox.class)).type("3")
                    .click(tester.find(Button.class, Matching.text("Buy")));

            assertEquals("29.97", tester.byId("total", Label.class).text().plain());
            assertTrue(tester.drawnText().contains("29.97"));
        }
    }
    // --8<-- [end:total]

    // --8<-- [start:refuse]
    @Test
    void aDisabledButtonCannotBeClicked() {
        try (ScreenTester tester = ScreenTester.open(new ShopScreen(9.99))) {
            final Button cancel = tester.find(Button.class, Matching.text("Cancel"));

            final AssertionError error = assertThrows(AssertionError.class, () -> tester.click(cancel));

            assertTrue(error.getMessage().contains("is disabled"), error.getMessage());
        }
    }
    // --8<-- [end:refuse]

    // --8<-- [start:keys]
    @Test
    void tabMovesFocusAndModifiersApplyToAKey() {
        try (ScreenTester tester = ScreenTester.open(new ShopScreen(1))) {
            final TextBox quantity = tester.byId("quantity", TextBox.class);

            tester.press(Key.TAB);
            assertEquals(Optional.of(quantity), tester.focused());

            tester.type("12").press(Key.A, Modifier.CONTROL).type("7");
            assertEquals("7", quantity.text());
        }
    }
    // --8<-- [end:keys]

    // --8<-- [start:clock]
    @Test
    void theClockMovesOnlyWhenTheTestSaysSo() {
        try (ScreenTester tester = ScreenTester.open(new ShopScreen(1))) {
            tester.click(tester.byId("quantity", TextBox.class)).type("2")
                    .click(tester.find(Button.class, Matching.text("Buy")));
            assertTrue(tester.drawnText().contains("Order added"));
            assertEquals(1, tester.overlays().size());

            tester.advance(3_500);

            assertTrue(tester.overlays().isEmpty(), "the toast closed after its three seconds");
        }
    }
    // --8<-- [end:clock]

    // --8<-- [start:options]
    @Test
    void theOptionsSetTheSizeAndTheLanguage() {
        final Translator shouting = new Translator() {
            @Override
            public Optional<String> translate(final String key) {
                return Optional.of(key.toUpperCase(Locale.ROOT));
            }

            @Override
            public long version() {
                return 1L;
            }
        };
        final TesterOptions options = TesterOptions.DEFAULT.withSize(320, 200).withTranslator(shouting);

        try (ScreenTester tester = ScreenTester.open(new TranslatedScreen(), options)) {
            assertTrue(tester.drawnText().contains("SHOP.BUY"));
        }
    }
    // --8<-- [end:options]
}
