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
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.input.Modifiers;
import org.inspirenxe.strata.api.input.MouseButton;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.api.theme.Themes;
import org.inspirenxe.strata.core.StrataCore;
import org.inspirenxe.strata.examples.adapter.RecordingCanvas;
import org.inspirenxe.strata.examples.adapter.ToyPlatform;
import org.inspirenxe.strata.examples.extending.DrawingExamples;
import org.inspirenxe.strata.examples.extending.HookExamples;
import org.inspirenxe.strata.spi.ScreenHost;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the extending guide's drawing, lifecycle, style state, drag and baseline examples.
 */
class HookExamplesTest {

    private static ScreenHost host(final float width, final float height) {
        final ScreenHost host = StrataCore.createScreenHost(new ToyPlatform(), Themes.flat());
        host.resize(width, height);
        return host;
    }

    private static List<String> frame(final ScreenHost host) {
        final RecordingCanvas canvas = new RecordingCanvas();
        canvas.begin();
        host.layout();
        host.render(canvas);
        return new ArrayList<>(canvas.commands());
    }

    @Test
    void theDrawingExamplesDrawOnAnEngineWithoutShapes() {
        final ScreenHost host = host(400, 300);
        final DrawingExamples.HeatMap[] map = new DrawingExamples.HeatMap[1];
        host.open(ui -> {
            map[0] = new DrawingExamples.HeatMap(ui).update(0);
            return ui.stackPanel().setSpacing(4).setPadding(8)
                    .add(new DrawingExamples.LineChart(ui).setValues(0.1, 0.8, 0.4, 0.9))
                    .add(new DrawingExamples.Gauge(ui).setValue(0.6f))
                    .add(new DrawingExamples.Card(ui))
                    .add(new DrawingExamples.Emblem(ui))
                    .add(map[0])
                    .add(new DrawingExamples.DetailIcon(ui));
        });
        final List<String> commands = frame(host);
        // The canvas draws no shapes itself, so core built every shape from fills
        assertTrue(commands.stream().filter(c -> c.startsWith("fill ")).count() > 50);
        assertTrue(commands.stream().anyMatch(c -> c.startsWith("placeholder")), "the pixel buffer's placeholder");
        assertTrue(commands.stream().anyMatch(c -> c.startsWith("sprite examplemod:icon/star")));
        final long before = map[0].version();
        map[0].update(1);
        assertNotEquals(before, map[0].version());
    }

    @Test
    void aDensityOfTwoDrawsTheHairlinesHalfAsThick() {
        final ScreenHost host = host(200, 100);
        host.open(ui -> ui.stackPanel().add(new DrawingExamples.LineChart(ui).setValues(0.5)));
        final String grid = "33ffffff";
        assertTrue(frame(host).stream().anyMatch(c -> c.endsWith(grid) && c.contains("height=1.0")));
        host.setPixelsPerUnit(2f);
        final List<String> dense = frame(host);
        assertTrue(dense.stream().anyMatch(c -> c.endsWith(grid) && c.contains("height=0.5")));
        assertFalse(dense.stream().anyMatch(c -> c.endsWith(grid) && c.contains("height=1.0")));
    }

    @Test
    void aChartListensToItsFeedOnlyWhileItIsShown() {
        final int[] open = {0};
        final DoubleConsumer[] listener = new DoubleConsumer[1];
        final HookExamples.PriceFeed feed = l -> {
            listener[0] = l;
            open[0]++;
            return () -> open[0]--;
        };
        final ScreenHost host = host(200, 100);
        final HookExamples.PriceChart[] chart = new HookExamples.PriceChart[1];
        host.open(ui -> {
            chart[0] = new HookExamples.PriceChart(ui, feed);
            return ui.stackPanel().add(chart[0]);
        });
        assertEquals(1, open[0]);
        listener[0].accept(0.75);
        assertEquals(0.75, chart[0].last());
        host.close();
        assertEquals(0, open[0]);
    }

    @Test
    void aStyleStatePicksTheThemesKeyForIt() {
        final var theme = Themes.flat().toBuilder()
                .paint("examplemod.card", Paint.solid(0xFF111111))
                .paint("examplemod.card.warning", Paint.solid(0xFFAA3300))
                .build();
        final ScreenHost host = StrataCore.createScreenHost(new ToyPlatform(), theme);
        host.resize(200, 100);
        final HookExamples.StatusCard[] card = new HookExamples.StatusCard[1];
        host.open(ui -> {
            card[0] = new HookExamples.StatusCard(ui, "Low stock");
            return ui.stackPanel().add(card[0]);
        });
        assertTrue(frame(host).stream().anyMatch(c -> c.endsWith("ff111111")));
        card[0].setWarning(true);
        final List<String> warned = frame(host);
        assertTrue(warned.stream().anyMatch(c -> c.endsWith("ffaa3300")));
        assertFalse(warned.stream().anyMatch(c -> c.endsWith("ff111111")));
    }

    @Test
    void aTileDraggedOntoABinLeavesItsName() {
        final ScreenHost host = host(300, 100);
        final HookExamples.Tile[] tile = new HookExamples.Tile[1];
        final HookExamples.Bin[] bin = new HookExamples.Bin[1];
        host.open(ui -> {
            tile[0] = new HookExamples.Tile(ui, "Ana");
            bin[0] = new HookExamples.Bin(ui);
            return ui.stackPanel(Orientation.HORIZONTAL).setSpacing(20).setAlignment(
                    HorizontalAlignment.LEFT, VerticalAlignment.TOP).add(tile[0]).add(bin[0]);
        });
        host.layout();
        final Rect from = tile[0].bounds();
        final Rect to = bin[0].bounds();
        final float fx = from.x() + from.width() / 2;
        final float fy = from.y() + from.height() / 2;
        host.pointerMoved(fx, fy, Modifiers.NONE);
        host.pointerButton(fx, fy, MouseButton.LEFT, true, Modifiers.NONE);
        host.pointerMoved(fx + 10, fy, Modifiers.NONE);
        host.pointerMoved(to.x() + to.width() / 2, to.y() + to.height() / 2, Modifiers.NONE);
        host.pointerButton(to.x() + to.width() / 2, to.y() + to.height() / 2, MouseButton.LEFT, false, Modifiers.NONE);
        assertEquals("Ana", bin[0].last());
    }

    @Test
    void aBaselineRowPutsTheTextOfDifferentChildrenOnOneLine() {
        final ScreenHost host = host(300, 100);
        final HookExamples.Tag[] tag = new HookExamples.Tag[1];
        final Label[] label = new Label[1];
        host.open(ui -> {
            tag[0] = new HookExamples.Tag(ui, "Ana");
            label[0] = ui.label("Price");
            final HookExamples.BaselineRow row = new HookExamples.BaselineRow(ui);
            row.add(label[0]).add(tag[0]);
            return row;
        });
        host.layout();
        // The tag pads its text by 3 units above and the label does not, so the label sits 3 units lower
        assertEquals(tag[0].bounds().y() + 3f, label[0].bounds().y(), 0.001f);
    }

    @Test
    void aStopwatchAsksForTheFrameWhereTheNextSecondStarts() {
        final ScreenHost host = host(200, 100);
        host.open(ui -> ui.stackPanel().add(new HookExamples.Stopwatch(ui)));
        frame(host);
        assertNotEquals(Long.MAX_VALUE, host.nextFrameNanos());
    }

    @Test
    void whileAttachedFollowsAFeedOnlyWhileShown() {
        final int[] open = {0};
        final DoubleConsumer[] listener = new DoubleConsumer[1];
        final HookExamples.PriceFeed feed = l -> {
            listener[0] = l;
            open[0]++;
            return () -> open[0]--;
        };
        final ScreenHost host = host(200, 100);
        final Label[] label = new Label[1];
        host.open(ui -> {
            label[0] = HookExamples.priceLabel(ui, feed);
            return ui.stackPanel().add(label[0]);
        });
        assertEquals(1, open[0]);
        listener[0].accept(1.5);
        host.flush();
        assertEquals("1.50", label[0].text().plain());
        host.close();
        assertEquals(0, open[0]);
    }
}
