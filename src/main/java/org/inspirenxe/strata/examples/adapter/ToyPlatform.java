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

// --8<-- [start:platform]
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.text.TextMetrics;
import org.inspirenxe.strata.spi.Capabilities;
import org.inspirenxe.strata.spi.Clipboard;
import org.inspirenxe.strata.spi.Clock;
import org.inspirenxe.strata.spi.Platform;
import org.inspirenxe.strata.spi.TextInputSink;
import org.inspirenxe.strata.spi.ThemeSource;

import java.util.Optional;

/**
 * The platform for the imaginary engine. It has an in-memory clipboard, needs no text input signal, and sends no
 * key repeats, so Strata makes them.
 */
public final class ToyPlatform implements Platform {
    // Constant for the platform's life, as the SPI requires
    private static final Capabilities CAPABILITIES = new Capabilities(false, false);

    private final TextMetrics metrics = new MonospaceTextMetrics();
    private final Clipboard clipboard = new Clipboard() {
        private String text;

        @Override
        public Optional<String> get() {
            return Optional.ofNullable(this.text);
        }

        @Override
        public void set(final String text) {
            this.text = text;
        }
    };
    private final TextInputSink textInput = new TextInputSink() {
        @Override
        public void start(final Rect caretArea) {
            // An engine with an on-screen keyboard would open it here, near caretArea
        }

        @Override
        public void stop() {
        }
    };

    @Override
    public TextMetrics textMetrics() {
        return this.metrics;
    }

    @Override
    public Clipboard clipboard() {
        return this.clipboard;
    }

    @Override
    public Clock clock() {
        return Clock.SYSTEM;
    }

    @Override
    public TextInputSink textInput() {
        return this.textInput;
    }

    @Override
    public Capabilities capabilities() {
        return CAPABILITIES;
    }

    @Override
    public ThemeSource themes() {
        return ThemeSource.NONE;
    }
}
// --8<-- [end:platform]
