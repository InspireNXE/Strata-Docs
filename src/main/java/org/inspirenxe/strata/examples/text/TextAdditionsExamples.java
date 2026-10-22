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
package org.inspirenxe.strata.examples.text;

import org.inspirenxe.strata.api.text.PluralCategory;
import org.inspirenxe.strata.api.text.Text;

import java.util.Locale;

/**
 * The text guide's examples of plural text.
 */
public final class TextAdditionsExamples {

    private TextAdditionsExamples() {
    }

    /**
     * Makes the count of offers a seller has.
     *
     * @param count how many offers
     * @return text that reads "1 offer" or "3 offers" in English
     */
    public static Text offerCount(final int count) {
        // --8<-- [start:plural]
        // Picks shop.offers.one, shop.offers.few and so on by the language's rules for the count, and formats the
        // chosen translation with the arguments. The count is the first argument too, for "%s offers".
        final Text offers = Text.plural("shop.offers", count, count);
        // --8<-- [end:plural]
        return offers;
    }

    /**
     * Returns the category a count falls in for a language.
     *
     * @param locale the language
     * @param count the count
     * @return the category
     */
    public static PluralCategory categoryOf(final Locale locale, final long count) {
        // --8<-- [start:category]
        final PluralCategory category = PluralCategory.of(locale, count);
        // --8<-- [end:category]
        return category;
    }
}
