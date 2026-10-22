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
package org.inspirenxe.strata.examples.internationalization;

import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.Spinner;
import org.inspirenxe.strata.api.layout.FlowDirection;
import org.inspirenxe.strata.examples.internationalization.InternationalizationExamples.MapTranslator;
import org.inspirenxe.strata.testing.Matching;
import org.inspirenxe.strata.testing.ScreenTester;
import org.inspirenxe.strata.testing.TesterOptions;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The internationalization guide's examples, run.
 */
class InternationalizationExamplesTest {

    private static TesterOptions in(final Locale locale, final Map<String, String> translations) {
        return TesterOptions.DEFAULT.withTranslator(new MapTranslator(locale, translations));
    }

    // --8<-- [start:test]
    @Test
    void englishHasTwoFormsAndRussianThree() {
        try (ScreenTester tester = ScreenTester.open(new InternationalizationExamples.OffersScreen(),
                InternationalizationExamplesTest.in(Locale.ENGLISH, InternationalizationExamples.english()))) {
            final Spinner count = tester.find(Spinner.class, any -> true);
            final Label summary = tester.byId("summary", Label.class);
            assertEquals("1 offer", summary.text().plain());

            count.setValue(3);
            assertEquals("3 offers", summary.text().plain());
        }
        final Locale russian = Locale.forLanguageTag("ru");
        try (ScreenTester tester = ScreenTester.open(new InternationalizationExamples.OffersScreen(),
                InternationalizationExamplesTest.in(russian, InternationalizationExamples.russian()))) {
            final Spinner count = tester.find(Spinner.class, any -> true);
            final Label summary = tester.byId("summary", Label.class);

            count.setValue(2);
            assertEquals("2 predlozheniya", summary.text().plain());
            count.setValue(5);
            assertEquals("5 predlozheniy", summary.text().plain());
            count.setValue(21);
            assertEquals("21 predlozhenie", summary.text().plain());
        }
    }
    // --8<-- [end:test]

    @Test
    void aRightToLeftLanguageMirrorsTheRow() {
        try (ScreenTester tester = ScreenTester.open(new InternationalizationExamples.PagerScreen(),
                InternationalizationExamplesTest.in(Locale.forLanguageTag("he"), Map.of()))) {
            final Button back = tester.byId("back", Button.class);
            final Button next = tester.byId("next", Button.class);

            assertEquals(FlowDirection.RIGHT_TO_LEFT, back.flowDirection());
            assertTrue(back.bounds().x() > next.bounds().x(), "Back sits at the right");
        }
        try (ScreenTester tester = ScreenTester.open(new InternationalizationExamples.PagerScreen())) {
            assertTrue(tester.byId("back", Button.class).bounds().x() < tester.byId("next", Button.class).bounds().x());
        }
    }
}
