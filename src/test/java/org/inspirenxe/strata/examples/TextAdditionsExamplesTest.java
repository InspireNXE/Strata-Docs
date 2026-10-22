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

import org.inspirenxe.strata.api.text.PluralCategory;
import org.inspirenxe.strata.examples.text.TextAdditionsExamples;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Checks the text guide's plural examples.
 */
class TextAdditionsExamplesTest {

    @Test
    void pluralTextIsEqualByKeyCountAndArguments() {
        assertEquals(TextAdditionsExamples.offerCount(3), TextAdditionsExamples.offerCount(3));
        assertNotEquals(TextAdditionsExamples.offerCount(3), TextAdditionsExamples.offerCount(4));
    }

    @Test
    void languagesHaveTheirOwnCategories() {
        assertEquals(PluralCategory.ONE, TextAdditionsExamples.categoryOf(Locale.ENGLISH, 1));
        assertEquals(PluralCategory.OTHER, TextAdditionsExamples.categoryOf(Locale.ENGLISH, 0));
        assertEquals(PluralCategory.FEW, TextAdditionsExamples.categoryOf(Locale.forLanguageTag("ru"), 3));
        assertEquals(PluralCategory.MANY, TextAdditionsExamples.categoryOf(Locale.forLanguageTag("ru"), 5));
        assertEquals(PluralCategory.OTHER, TextAdditionsExamples.categoryOf(Locale.JAPANESE, 1));
        assertEquals(PluralCategory.ZERO, TextAdditionsExamples.categoryOf(Locale.forLanguageTag("ar"), 0));
    }
}
