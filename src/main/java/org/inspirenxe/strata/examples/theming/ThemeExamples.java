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

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.Strata;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.event.Subscription;
import org.inspirenxe.strata.api.geometry.Insets;
import org.inspirenxe.strata.api.geometry.Size;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.inspirenxe.strata.api.theme.Theme;
import org.inspirenxe.strata.api.theme.ThemeException;
import org.inspirenxe.strata.api.theme.ThemeId;
import org.inspirenxe.strata.api.theme.ThemeOverrides;
import org.inspirenxe.strata.api.theme.ThemeRef;
import org.inspirenxe.strata.api.theme.Themes;
import org.inspirenxe.strata.examples.quickstart.SettingsScreen;
import org.inspirenxe.strata.swing.SwingSnapshot;

import java.util.List;

/**
 * The theming guide's examples.
 */
public final class ThemeExamples {

    private ThemeExamples() {
    }

    /**
     * A variant of the flat theme with green buttons.
     *
     * @return the theme
     */
    public static Theme derived() {
        // --8<-- [start:derive]
        final Theme green = Themes.flat().toBuilder()
                .paint(Button.STYLE_NORMAL, Paint.solid(0xFF2E7D32))
                .paint(Button.STYLE_HOVERED, Paint.solid(0xFF388E3C))
                .paint(Button.STYLE_PRESSED, Paint.solid(0xFF1B5E20))
                .textStyle(Button.STYLE_TEXT, TextStyle.DEFAULT.withColor(0xFFFFFFFF).withBold(true))
                .build();
        // --8<-- [end:derive]
        return green;
    }

    /**
     * A theme built from nothing, with every required key and a few optional ones.
     *
     * @return the theme
     */
    public static Theme fromScratch() {
        // --8<-- [start:scratch]
        final Theme light = Theme.builder("light")
                // Required by Button
                .paint(Button.STYLE_NORMAL, Paint.gradient(0xFFFFFFFF, 0xFFE0E0E0))
                .paint(Button.STYLE_HOVERED, Paint.gradient(0xFFFFFFFF, 0xFFD0E4FF))
                .paint(Button.STYLE_PRESSED, Paint.solid(0xFFC0D4F0))
                .paint(Button.STYLE_DISABLED, Paint.solid(0xFFEEEEEE))
                // Required by TextBox
                .paint(TextBox.STYLE_NORMAL, Paint.solid(0xFFFFFFFF))
                .paint(TextBox.STYLE_FOCUSED, Paint.solid(0xFFF4F8FF))
                .paint(TextBox.STYLE_DISABLED, Paint.solid(0xFFEEEEEE))
                // Optional keys
                .paint(Button.STYLE_FOCUS, Paint.solid(0xFF3B78E7))
                .textStyle(Button.STYLE_TEXT, TextStyle.DEFAULT.withColor(0xFF202020))
                .textStyle(Button.STYLE_TEXT_DISABLED, TextStyle.DEFAULT.withColor(0xFF9E9E9E))
                .textStyle(Label.STYLE_TEXT, TextStyle.DEFAULT.withColor(0xFF202020))
                .textStyle(TextBox.STYLE_TEXT, TextStyle.DEFAULT.withColor(0xFF202020))
                .paint(TextBox.STYLE_CARET, Paint.solid(0xFF202020))
                .build();
        // --8<-- [end:scratch]
        return light;
    }

    static Theme sprites() {
        // --8<-- [start:sprites]
        // assets/examplemod/sprites/button.png, with a 4-pixel frame
        final SpriteId button = SpriteId.of("examplemod", "button");
        final SpriteId buttonHover = SpriteId.parse("examplemod:button_hover");
        final Theme textured = Themes.flat().toBuilder()
                .paint(Button.STYLE_NORMAL, Paint.nineSlice(button, Insets.of(4f)))
                .paint(Button.STYLE_HOVERED, Paint.nineSlice(buttonHover, Insets.of(4f)))
                .paint(Button.STYLE_PRESSED, new Paint.NineSlice(buttonHover, Insets.of(4f), 0xFFC0C0C0))
                .build();
        // --8<-- [end:sprites]
        return textured;
    }

    static Theme sized() {
        // --8<-- [start:sized]
        // A 400 by 40 image that stands for 200 by 20 units, with a 4-unit frame
        final SpriteId button = SpriteId.of("examplemod", "button");
        final SpriteId pattern = SpriteId.of("examplemod", "pattern");
        final Theme sized = Themes.flat().toBuilder()
                .paint(Button.STYLE_NORMAL, Paint.nineSlice(button, new Size(200, 20), Insets.of(4f)))
                // Edges and center repeat at their size instead of stretching
                .paint(Button.STYLE_HOVERED, Paint.tiledNineSlice(button, new Size(200, 20), Insets.of(4f)))
                // One copy every 16 units, the last row and column cropped
                .paint(Button.STYLE_PRESSED, Paint.tiled(pattern, new Size(16, 16)))
                .build();
        // --8<-- [end:sized]
        return sized;
    }

    static void missing() {
        // --8<-- [start:missing]
        try {
            // The settings screen has a button and a text box, whose required keys an empty theme lacks
            SwingSnapshot.render(new SettingsScreen(), Theme.builder("empty").build(), 320, 200, 1f);
        } catch (final ThemeException e) {
            // "Theme 'empty' is missing style keys: ...", naming every key the button and the text box require
            System.out.println(e.getMessage());
            final java.util.Set<String> keys = e.missingKeys();
        }
        // --8<-- [end:missing]
    }

    /**
     * Green buttons on every screen that shows the flat theme, and a theme of the example mod's own on top of it.
     *
     * @return the subscriptions, the overrides first; close them to take the changes back
     */
    public static List<Subscription> fromCode() {
        // --8<-- [start:registry]
        // Every screen that shows the flat theme gets green buttons, until the subscription is closed
        final ThemeOverrides green = ThemeOverrides.of()
                .with(Button.STYLE_NORMAL, Paint.solid(0xFF2E7D32))
                .with(Button.STYLE_HOVERED, Paint.solid(0xFF388E3C));
        final Subscription everywhere = Strata.themes().override(Themes.FLAT, green);

        // A theme of your own that starts from another; packs can restyle it like any theme
        final Subscription shop = Strata.themes().define(ThemeId.of("examplemod", "shop"), Themes.FLAT,
                ThemeOverrides.of().with(Window.STYLE_BACKGROUND, Paint.solid(0xF0101010)));

        // The same overrides on one screen only
        final ThemeRef oneScreen = ThemeRef.of(Themes.FLAT).override(green);
        // --8<-- [end:registry]
        return List.of(everywhere, shop);
    }

    static void perComponent(final ComponentFactory factory) {
        // --8<-- [start:local]
        // Backgrounds and label styles can be set per component, outside the theme
        final StackPanel card = factory.stackPanel().setPadding(6);
        card.setBackground(Paint.gradient(0xFF3A3D43, 0xFF2B2D31));
        card.add(factory.label("Warning").setStyle(TextStyle.DEFAULT.withColor(0xFFFFB74D).withBold(true)));
        // --8<-- [end:local]
    }

    /**
     * The same controls, to render with several themes.
     *
     * @param background the panel color behind the controls
     * @return the screen
     */
    public static Screen sampler(final int background) {
        return factory -> {
            final TextBox field = factory.textBox().setText("Some text");
            final StackPanel column = factory.stackPanel().setSpacing(4).setPadding(8)
                    .add(factory.label("A label"))
                    .add(factory.button("A button"))
                    .add(factory.button("Disabled").setEnabled(false))
                    .add(field);
            column.setBackground(Paint.solid(background));
            return factory.anchorPanel().add(column.setAnchor(Anchor.CENTER)).setAnchor(Anchor.FILL);
        };
    }
}
