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

import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.menu.Flyout;
import org.inspirenxe.strata.api.control.menu.Menu;
import org.inspirenxe.strata.api.control.menu.RadioMenuItem;
import org.inspirenxe.strata.api.control.pane.Expander;
import org.inspirenxe.strata.api.control.pane.SplitPane;
import org.inspirenxe.strata.api.control.window.MessageBox;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.input.Key;
import org.inspirenxe.strata.examples.controls.PaneExamples;
import org.inspirenxe.strata.testing.Matching;
import org.inspirenxe.strata.testing.ScreenTester;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that the pane, menu and window examples in the controls guide do what the guide says.
 */
@SuppressWarnings("unchecked")
class PaneExamplesTest {

    @Test
    void theSidebarKeepsItsWidthAndCollapsesOnEnterOnTheDivider() {
        final SplitPane[] outer = new SplitPane[1];
        try (ScreenTester tester = ScreenTester.open(ui -> outer[0] = PaneExamples.threePanes(ui, ui.label("Sidebar"),
                ui.label("Document"), ui.label("Details")))) {
            final SplitPane pane = outer[0];
            assertEquals(SplitPane.ResizePolicy.KEEP_FIRST, pane.resizePolicy());
            assertTrue(pane.isCollapsible(SplitPane.Side.FIRST));
            assertFalse(pane.isCollapsible(SplitPane.Side.SECOND));
            final List<SplitPane.Side> collapsed = new ArrayList<>();
            pane.onEvent(SplitPane.COLLAPSED, event -> collapsed.add(event.value()));
            pane.requestFocus();
            assertTrue(pane.isFocused());
            tester.press(Key.ENTER);
            assertEquals(java.util.Optional.of(SplitPane.Side.FIRST), pane.collapsed());
            tester.press(Key.ENTER);
            assertEquals(java.util.Optional.empty(), pane.collapsed());
            assertEquals(java.util.Arrays.asList(SplitPane.Side.FIRST, null), collapsed);
        }
    }

    @Test
    void aCheckBoxInTheHeaderOpensTheExpander() {
        try (ScreenTester tester = ScreenTester.open(ui -> PaneExamples.proxySettings(ui, ui.label("Host")))) {
            final Expander expander = tester.find(Expander.class, any -> true);
            final CheckBox use = tester.find(CheckBox.class, any -> true);
            assertFalse(expander.isExpanded());
            tester.click(use);
            assertTrue(use.isSelected());
            assertTrue(expander.isExpanded());
        }
    }

    @Test
    void aBoxWithCustomResultsReportsTheOneThatWasPressed() {
        final List<String> heard = new ArrayList<>();
        try (ScreenTester tester = ScreenTester.open(ui -> ui.label("Exchange"))) {
            PaneExamples.saveChanges(tester.factory(), () -> heard.add("save"), () -> heard.add("discard"));
            tester.frame();
            final MessageBox box = tester.find(MessageBox.class, any -> true);
            assertEquals(3, box.results().size());
            assertEquals(MessageBox.Result.CANCEL, box.dismissResult());
            assertEquals("save", box.defaultResult().id());
            tester.click(tester.find(Button.class, Matching.text("Don't save")));
            assertEquals(List.of("discard"), heard);
        }
    }

    @Test
    void radioItemsInOneGroupClearEachOther() {
        final List<String> picked = new ArrayList<>();
        final Menu[] menu = new Menu[1];
        try (ScreenTester tester = ScreenTester.open(ui -> {
            menu[0] = PaneExamples.sortMenu(ui, picked::add);
            return ui.label("List");
        })) {
            menu[0].openAt(10, 10);
            tester.press(Key.DOWN);
            tester.press(Key.DOWN);
            tester.press(Key.ENTER);
            assertEquals(List.of("Date"), picked);
            final RadioMenuItem name = (RadioMenuItem) menu[0].items().get(0);
            final RadioMenuItem date = (RadioMenuItem) menu[0].items().get(1);
            assertFalse(name.isSelected());
            assertTrue(date.isSelected());
        }
    }

    @Test
    void aRightClickOnARowOpensTheMenuOfThatRow() {
        final List<String> opened = new ArrayList<>();
        try (ScreenTester tester = ScreenTester.open(ui -> PaneExamples.rowMenus(ui, List.of("Ana", "Ben"), opened::add))) {
            tester.rightClick(tester.find(Label.class, Matching.text("Ben")));
            assertEquals(List.of("Ben"), opened);
            assertTrue(tester.drawnText().contains("Delete Ben"), tester.drawnText().toString());
            assertEquals(1, tester.overlays().size());
        }
    }

    @Test
    void escapeAsksCloseAndAFlyoutWithChangesRefusesIt() {
        final boolean[] unsaved = {true};
        final Flyout[] flyout = new Flyout[1];
        try (ScreenTester tester = ScreenTester.open(ui -> {
            flyout[0] = PaneExamples.formFlyout(ui, ui.label("Form"), () -> unsaved[0]);
            return ui.label("Screen");
        })) {
            flyout[0].openAt(10, 10);
            assertTrue(flyout[0].isOpen());
            tester.press(Key.ESCAPE);
            assertTrue(flyout[0].isOpen());
            unsaved[0] = false;
            tester.press(Key.ESCAPE);
            assertFalse(flyout[0].isOpen());
        }
    }

    @Test
    void aWindowReportsMaximizedAndCollapsed() {
        final List<String> states = new ArrayList<>();
        try (ScreenTester tester = ScreenTester.open(ui -> PaneExamples.notesWindow(ui, ui.label("Text"), states::add))) {
            final Window window = tester.find(Window.class, any -> true);
            window.setMaximized(true);
            window.setCollapsed(true);
            window.setCollapsed(false);
            window.setMaximized(false);
            assertEquals(List.of("maximized", "collapsed", "expanded", "restored"), states);
        }
    }
}
