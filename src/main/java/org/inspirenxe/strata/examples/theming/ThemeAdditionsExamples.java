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
package org.inspirenxe.strata.examples.theming;

import org.inspirenxe.strata.api.component.Tooltip;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.text.TextStylePatch;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.api.theme.Theme;
import org.inspirenxe.strata.api.theme.ThemeOverrides;
import org.inspirenxe.strata.api.theme.ThemeRef;
import org.inspirenxe.strata.api.theme.Themes;

/**
 * The theming guide's examples of rounded paints, text style patches and typed metrics.
 */
public final class ThemeAdditionsExamples {

    private ThemeAdditionsExamples() {
    }

    /**
     * Returns the flat theme with a rounded window background.
     *
     * @return the theme
     */
    public static Theme roundedWindows() {
        // --8<-- [start:rounded]
        // A solid color drawn with rounded corners, 4 units in radius
        final Theme theme = Themes.flat().toBuilder()
                .paint(Window.STYLE_BACKGROUND, Paint.rounded(0xFF202024, 4f))
                .build();
        // --8<-- [end:rounded]
        return theme;
    }

    /**
     * Returns a ref that recolors labels and keeps the rest of their style.
     *
     * @return the ref
     */
    public static ThemeRef goldLabels() {
        // --8<-- [start:patch]
        // The color changes. Bold, the shadow and the other properties stay as the engine's theme has them.
        final ThemeRef gold = ThemeRef.platform()
                .override(Label.STYLE_TEXT, TextStylePatch.of().color(0xFFE0C060));
        // --8<-- [end:patch]
        return gold;
    }

    /**
     * Returns overrides with two patches of one key, which combine.
     *
     * @return the overrides
     */
    public static ThemeOverrides combinedPatches() {
        // --8<-- [start:combine]
        // Both apply: gold, and bold. Where the patches set the same property, the later one wins.
        final ThemeOverrides overrides = ThemeOverrides.of()
                .with(Label.STYLE_TEXT, TextStylePatch.of().color(0xFFE0C060))
                .with(Label.STYLE_TEXT, TextStylePatch.of().bold(true));
        // --8<-- [end:combine]
        return overrides;
    }

    /**
     * Returns a ref with a flag and a duration set in code.
     *
     * @return the ref
     */
    public static ThemeRef tooltipsBelow() {
        // --8<-- [start:flags]
        // A flag is stored as 1 or 0, a duration in milliseconds
        final ThemeRef ref = ThemeRef.platform()
                .override(Tooltip.STYLE_BELOW, true)
                .override(Tooltip.STYLE_DELAY, 250);
        // --8<-- [end:flags]
        return ref;
    }

    /**
     * Reads a flag and a duration back from a theme.
     *
     * @param theme the theme
     * @return whether tooltips show below, and the delay in milliseconds
     */
    public static String readTypedMetrics(final Theme theme) {
        // --8<-- [start:read]
        final boolean below = theme.flagOr(Tooltip.STYLE_BELOW, false);
        final long delay = theme.millisOr(Tooltip.STYLE_DELAY, 500);
        // --8<-- [end:read]
        return below + " " + delay;
    }
}
