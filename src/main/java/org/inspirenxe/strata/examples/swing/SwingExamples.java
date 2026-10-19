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

import org.inspirenxe.strata.api.theme.Themes;
import org.inspirenxe.strata.examples.quickstart.SettingsScreen;
import org.inspirenxe.strata.swing.StrataPanel;
import org.inspirenxe.strata.swing.SwingPlatform;
import org.inspirenxe.strata.swing.SwingSnapshot;
import org.inspirenxe.strata.swing.SwingTextMetrics;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
            panel.screens().push(new SettingsScreen());

            final JFrame frame = new JFrame("Settings");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(panel);
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
}
