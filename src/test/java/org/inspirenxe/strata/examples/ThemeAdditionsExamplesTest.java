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

import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.api.theme.Theme;
import org.inspirenxe.strata.api.theme.ThemeRef;
import org.inspirenxe.strata.api.theme.Themes;
import org.inspirenxe.strata.examples.theming.ThemeAdditionsExamples;
import org.inspirenxe.strata.swing.SwingThemeSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the theming guide's rounded paint, text patch and typed metric examples.
 */
class ThemeAdditionsExamplesTest {

    private final SwingThemeSource source = new SwingThemeSource(ThemeAdditionsExamplesTest.class.getClassLoader());

    @Test
    void aRoundedPaintHoldsItsRadius() {
        assertEquals(Paint.rounded(0xFF202024, 4f),
                ThemeAdditionsExamples.roundedWindows().paint(Window.STYLE_BACKGROUND).orElseThrow());
    }

    @Test
    void aPatchChangesOnlyTheColor() {
        final TextStyle base = this.source.resolve(ThemeRef.of(Themes.FLAT)).textStyleOr(Label.STYLE_TEXT, TextStyle.DEFAULT);
        final Theme gold = this.source.resolve(ThemeAdditionsExamples.goldLabels());
        assertEquals(base.withColor(0xFFE0C060), gold.textStyleOr(Label.STYLE_TEXT, TextStyle.DEFAULT));
    }

    @Test
    void twoPatchesOfOneKeyCombine() {
        final Theme theme = this.source.resolve(
                ThemeRef.of(Themes.FLAT).override(ThemeAdditionsExamples.combinedPatches()));
        final TextStyle style = theme.textStyleOr(Label.STYLE_TEXT, TextStyle.DEFAULT);
        assertEquals(0xFFE0C060, style.color());
        assertTrue(style.bold());
    }

    @Test
    void flagsAndDurationsReadBack() {
        final Theme theme = this.source.resolve(ThemeAdditionsExamples.tooltipsBelow());
        assertEquals("true 250", ThemeAdditionsExamples.readTypedMetrics(theme));
        assertEquals("false 500", ThemeAdditionsExamples.readTypedMetrics(Themes.flat()));
    }
}
