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

import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.WrapPanel;
import org.inspirenxe.strata.api.theme.Themes;
import org.inspirenxe.strata.core.StrataCore;
import org.inspirenxe.strata.examples.adapter.RecordingCanvas;
import org.inspirenxe.strata.examples.adapter.ToyPlatform;
import org.inspirenxe.strata.examples.layout.LayoutAdditionsExamples;
import org.inspirenxe.strata.spi.ScreenHost;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the layout guide's wrap panel, baseline, text style and direction examples.
 */
class LayoutAdditionsExamplesTest {

    private static ScreenHost host() {
        final ScreenHost host = StrataCore.createScreenHost(new ToyPlatform(), Themes.flat());
        host.resize(300, 200);
        return host;
    }

    @Test
    void tagsWrapWhenThePanelIsNarrow() {
        final ScreenHost host = host();
        final WrapPanel[] tags = new WrapPanel[1];
        host.open(ui -> {
            tags[0] = LayoutAdditionsExamples.tags(ui, List.of("red", "large", "on sale", "new", "used")).setWidth(90);
            return ui.anchorPanel().add(tags[0]);
        });
        host.layout();
        final Set<Float> rows = new HashSet<>();
        for (final Component<?> tag : tags[0].children()) {
            rows.add(tag.bounds().y());
        }
        assertTrue(rows.size() > 1, "the tags are on more than one row");
        // The second row starts 4 units under the tallest button of the first
        final float firstBottom = tags[0].children().get(0).bounds().bottom();
        assertTrue(rows.contains(firstBottom + 4f));
    }

    @Test
    void aWideWrapPanelKeepsEveryTagInOneRow() {
        final ScreenHost host = host();
        final WrapPanel[] tags = new WrapPanel[1];
        host.open(ui -> {
            tags[0] = LayoutAdditionsExamples.tags(ui, List.of("red", "large")).setWidth(250);
            return ui.anchorPanel().add(tags[0]);
        });
        host.layout();
        assertEquals(tags[0].children().get(0).bounds().y(), tags[0].children().get(1).bounds().y());
    }

    @Test
    void theBaselineRowLowersTheLabelsToTheTextBoxText() {
        final ScreenHost host = host();
        final StackPanel[] row = new StackPanel[1];
        host.open(ui -> {
            row[0] = LayoutAdditionsExamples.priceRow(ui);
            return ui.anchorPanel().add(row[0]);
        });
        host.layout();
        final Component<?> label = row[0].children().get(0);
        final Component<?> box = row[0].children().get(1);
        // The text box pads its text, so the labels start lower than the box to meet its baseline
        assertTrue(label.bounds().y() > box.bounds().y());
    }

    @Test
    void aPanelsTextStyleReachesTheLabelsInsideIt() {
        final ScreenHost host = host();
        final StackPanel[] footer = new StackPanel[1];
        host.open(ui -> {
            footer[0] = LayoutAdditionsExamples.footer(ui);
            return ui.anchorPanel().add(footer[0]);
        });
        host.layout();
        final RecordingCanvas canvas = new RecordingCanvas();
        canvas.begin();
        host.render(canvas);
        assertTrue(canvas.commands().stream().anyMatch(c -> c.contains("Updated today")));
        assertEquals(0xFF808080, ((Label) footer[0].children().get(0)).style().color());
    }

    @Test
    void aMirroredPanelPutsTheMeterAtTheRight() {
        final ScreenHost host = host();
        final StackPanel[] panel = new StackPanel[1];
        host.open(ui -> {
            panel[0] = LayoutAdditionsExamples.mirrored(ui);
            return ui.anchorPanel().add(panel[0]);
        });
        host.layout();
        final RecordingCanvas canvas = new RecordingCanvas();
        canvas.begin();
        host.render(canvas);
        final Component<?> meter = panel[0].children().get(1);
        final String expected = "fill Rect[x=" + (meter.bounds().right() - 40f);
        assertTrue(canvas.commands().stream().anyMatch(c -> c.startsWith(expected)), canvas.commands().toString());
    }
}
