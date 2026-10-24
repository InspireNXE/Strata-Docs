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
package org.inspirenxe.strata.examples.swing;

import org.inspirenxe.strata.api.Strata;
import org.inspirenxe.strata.api.text.FontId;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.inspirenxe.strata.api.theme.Themes;
import org.inspirenxe.strata.examples.quickstart.SettingsScreen;
import org.inspirenxe.strata.swing.AwtFont;
import org.inspirenxe.strata.swing.BitmapFont;
import org.inspirenxe.strata.swing.SpriteSource;
import org.inspirenxe.strata.swing.StrataPanel;
import org.inspirenxe.strata.swing.SwingPlatform;
import org.inspirenxe.strata.swing.SwingSnapshot;
import org.inspirenxe.strata.swing.SwingSprites;
import org.inspirenxe.strata.swing.SwingTextMetrics;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Showing Strata screens with the Swing adapter.
 */
public final class SwingExamples {

    private SwingExamples() {
    }

    // --8<-- [start:window]
    public static void main(final String[] args) {
        // Swing components must be created and used on the event dispatch thread
        SwingUtilities.invokeLater(() -> {
            final SwingPlatform platform = new SwingPlatform(SwingTextMetrics.defaults());
            // 2 pixels per UI unit
            final StrataPanel panel = new StrataPanel(platform, Themes.flat(), 2f);
            // Strata.screens() opens screens on the default panel
            panel.setDefault();
            Strata.screens().open(new SettingsScreen());

            final JFrame frame = new JFrame("Settings");
            frame.setContentPane(panel);
            // The close button asks the screens first, and the title follows the top screen's
            panel.manageWindow(frame);
            // The size comes from what the top screen asks for
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
    // --8<-- [end:window]

    static void snapshot() throws IOException {
        // --8<-- [start:snapshot]
        // 640 by 400 pixels at 2 pixels per unit: the screen is laid out in 320 by 200 units
        final BufferedImage image = SwingSnapshot.render(new SettingsScreen(), Themes.flat(), 640, 400, 2f);
        ImageIO.write(image, "png", new File("settings.png"));
        // --8<-- [end:snapshot]
    }

    static StrataPanel customFont() {
        // --8<-- [start:font]
        // The font size is in UI units; bold and italic are derived from this font
        final SwingTextMetrics metrics = new SwingTextMetrics(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        final StrataPanel panel = new StrataPanel(new SwingPlatform(metrics), Themes.flat(), 1.5f);
        // --8<-- [end:font]
        return panel;
    }

    static void registerFont(final SwingTextMetrics metrics, final InputStream ttf) throws IOException,
            FontFormatException {
        // --8<-- [start:register]
        // A font file read at 1 point: its size does not matter, since styles choose the size
        final Font serif = Font.createFont(Font.TRUETYPE_FONT, ttf);
        metrics.register(FontId.of("app", "serif"), serif);
        // --8<-- [end:register]
    }

    static StrataPanel bitmapFont() throws IOException {
        // --8<-- [start:bitmap]
        // A 128 by 64 sheet of 16 by 8 cells, each 8 pixels square, drawn 8 units tall with its top 7 above the baseline
        final BufferedImage sheet = ImageIO.read(new File("font/ascii.png"));
        final BitmapFont font = BitmapFont.builder(9, 7)
                .sheet(sheet, 8, 7, List.of(" !\"#$%&'()*+,-./", "0123456789:;<=>?", "@ABCDEFGHIJKLMNO",
                        "PQRSTUVWXYZ[\\]^_", "`abcdefghijklmno", "pqrstuvwxyz{|}~\u0000", "\u0000".repeat(16),
                        "\u0000".repeat(16)))
                .advance(' ', 4)
                .build();
        final SwingTextMetrics metrics = new SwingTextMetrics(font,
                new AwtFont(new Font(Font.MONOSPACED, Font.PLAIN, 9)));
        final StrataPanel panel = new StrataPanel(new SwingPlatform(metrics), Themes.flat(), 2f);
        // --8<-- [end:bitmap]
        return panel;
    }

    static float zoom(final StrataPanel panel) {
        // --8<-- [start:zoom]
        // The user zooms: the screens get the new size in units and the new density, and lay out again
        panel.setScale(3f);
        // Device pixels per UI unit: the scale times the display's own scale, 6 on a display that doubles pixels
        final float density = panel.pixelsPerUnit();
        // --8<-- [end:zoom]
        return density;
    }

    static SpriteSource denseSource(final Path folder) {
        // --8<-- [start:dense]
        // Beside art/<namespace>/<path>.png, art/<namespace>/<path>@2x.png holds the same sprite at twice the pixels
        return new SpriteSource() {
            @Override
            public Optional<BufferedImage> load(final SpriteId sprite) {
                return read(folder.resolve(sprite.namespace()).resolve(sprite.path() + ".png"));
            }

            @Override
            public Optional<BufferedImage> loadDense(final SpriteId sprite, final float density) {
                // Asked at 2 where a unit covers 2 device pixels or more, and at 1.5 from there; empty uses the plain one
                final String suffix = density == 2f ? "@2x" : "@1.5x";
                return read(folder.resolve(sprite.namespace()).resolve(sprite.path() + suffix + ".png"));
            }
        };
        // --8<-- [end:dense]
    }

    private static Optional<BufferedImage> read(final Path file) {
        try {
            return Files.isRegularFile(file) ? Optional.ofNullable(ImageIO.read(file.toFile())) : Optional.empty();
        } catch (final IOException e) {
            return Optional.empty();
        }
    }

    static void source(final StrataPanel panel) {
        // --8<-- [start:source]
        // Images from a folder instead of the class path; a file that is missing or broken reads as empty
        final Path folder = Path.of("art");
        final SpriteSource source = sprite -> {
            final Path file = folder.resolve(sprite.namespace()).resolve(sprite.path() + ".png");
            try {
                return Files.isRegularFile(file) ? Optional.ofNullable(ImageIO.read(file.toFile())) : Optional.empty();
            } catch (final IOException e) {
                return Optional.empty();
            }
        };
        // Swap the images of an open panel; its screens stay open
        panel.setSprites(new SwingSprites(source));
        // --8<-- [end:source]
    }
}
