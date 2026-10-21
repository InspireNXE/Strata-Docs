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
package org.inspirenxe.strata.examples.adapter;

// --8<-- [start:startup]
import org.inspirenxe.strata.api.appearance.Appearance;
import org.inspirenxe.strata.api.screen.ScreenController;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.text.TextSpan;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.core.StrataCore;
import org.inspirenxe.strata.spi.TextSource;
import org.inspirenxe.strata.spi.Translator;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * What the imaginary engine's adapter does once, when the engine loads it and before any project code runs.
 */
public final class ToyStartup {

    private ToyStartup() {
    }

    /**
     * Hands core everything that is shared by the whole process.
     *
     * @param language the engine's current language
     * @param screens what {@code Strata.screens()} opens screens with, such as a screen stack
     */
    public static void start(final Language language, final ScreenController screens) {
        // One translator for the process; Strata's own keys fall back to English where it has none
        StrataCore.setTranslator(language);
        // Strata.screens() returns this from now on
        StrataCore.setScreenController(screens);
        // Engine items show in every list with their name, beneath whatever projects register for them
        StrataCore.registerDefaultAppearance(Item.class, Appearance.<Item>builder()
                .name(item -> Text.translatable(item.nameKey()))
                .build());
    }

    /**
     * Wraps an engine message as Strata text, which reads the message anew each time a control lays out.
     *
     * @param message the message
     * @return the text
     */
    public static Text text(final Message message) {
        return StrataCore.text(message);
    }

    /**
     * The engine's language, as Strata's translator. Calls may come from any thread.
     */
    public static final class Language implements Translator {
        private final AtomicLong version = new AtomicLong();
        private volatile Map<String, String> patterns = Map.of();
        private volatile Locale locale = Locale.US;

        /** Called by the engine when the player picks another language. */
        public void change(final Locale locale, final Map<String, String> patterns) {
            this.patterns = Map.copyOf(patterns);
            this.locale = locale;
            // Hosts lay everything out again, and dates and times follow the new locale
            this.version.incrementAndGet();
        }

        @Override
        public Optional<String> translate(final String key) {
            // The pattern as written, with its %s and %1$s; core formats the arguments
            return Optional.ofNullable(this.patterns.get(key));
        }

        @Override
        public long version() {
            return this.version.get();
        }

        @Override
        public Locale locale() {
            return this.locale;
        }
    }

    /**
     * An item of the engine's.
     *
     * @param nameKey the translation key of its name
     */
    public record Item(String nameKey) {
    }

    /**
     * A message in the engine's own text system: some text and an optional color.
     *
     * @param text the characters
     * @param argb the color, or 0 to take the base's
     */
    public record Message(String text, int argb) implements TextSource {
        @Override
        public List<TextSpan> resolve(final TextStyle base) {
            // Parts with no style of their own take the base; a part that sets the color changes only that
            return List.of(new TextSpan(this.text, this.argb == 0 ? base : base.withColor(this.argb)));
        }
    }
}
// --8<-- [end:startup]
