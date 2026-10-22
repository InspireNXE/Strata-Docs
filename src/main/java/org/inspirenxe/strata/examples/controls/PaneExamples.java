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
package org.inspirenxe.strata.examples.controls;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.component.HasContextMenu;
import org.inspirenxe.strata.api.control.attribute.popup.Closable;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.display.InfoBar;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.menu.Flyout;
import org.inspirenxe.strata.api.control.menu.Menu;
import org.inspirenxe.strata.api.control.menu.RadioMenuItem;
import org.inspirenxe.strata.api.control.pane.Expander;
import org.inspirenxe.strata.api.control.pane.SplitPane;
import org.inspirenxe.strata.api.control.window.MessageBox;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.Orientation;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * The controls guide's examples for split panes, expanders, message boxes, menus, windows and flyouts.
 */
public final class PaneExamples {

    private PaneExamples() {
    }

    /**
     * Three panes: a sidebar, a document and a detail pane.
     *
     * @param ui the factory
     * @param sidebar the left pane
     * @param document the middle pane
     * @param details the right pane
     * @return the outer split pane, which holds another
     */
    // --8<-- [start:three-panes]
    public static SplitPane threePanes(final ComponentFactory ui, final Component<?> sidebar,
                                       final Component<?> document, final Component<?> details) {
        // The document and the details share the space right of the sidebar
        final SplitPane inner = ui.splitPane(Orientation.HORIZONTAL)
                .setFirst(document.setAnchor(Anchor.FILL))
                .setSecond(details.setAnchor(Anchor.FILL))
                .setMinSizes(200, 120)
                .setResizePolicy(SplitPane.ResizePolicy.KEEP_SECOND)
                .setCollapsible(false, true)
                .setPosition(0.7f);
        // The sidebar keeps its width when the window grows, never gets under 150 and collapses on a double click
        return ui.splitPane(Orientation.HORIZONTAL)
                .setFirst(sidebar.setAnchor(Anchor.FILL))
                .setSecond(inner.setAnchor(Anchor.FILL))
                .setMinSizes(150, 330)
                .setResizePolicy(SplitPane.ResizePolicy.KEEP_FIRST)
                .setCollapsible(true, false)
                .setPosition(0.2f);
    }
    // --8<-- [end:three-panes]

    /**
     * An expander whose header is a check box that turns its settings on.
     *
     * @param ui the factory
     * @param settings the settings the box turns on
     * @return the expander
     */
    // --8<-- [start:expander-header]
    public static Expander proxySettings(final ComponentFactory ui, final Component<?> settings) {
        final CheckBox use = ui.checkBox("Use a proxy");
        final Expander expander = ui.expander("Proxy").setHeader(use).setContent(settings);
        // Turning the box on opens the settings; the click on the box does not also flip the expander
        use.onEvent(CheckBox.TOGGLED, event -> expander.setExpanded(event.value()));
        return expander;
    }
    // --8<-- [end:expander-header]

    /**
     * A box that asks whether to save, with a button of its own for each answer.
     *
     * @param ui the factory
     * @param save runs when the user answers Save
     * @param discard runs when the user answers Don't save
     * @return the box, already shown as a dialog
     */
    // --8<-- [start:message-box-results]
    public static MessageBox saveChanges(final ComponentFactory ui, final Runnable save, final Runnable discard) {
        final MessageBox.Result saveResult = MessageBox.Result.of("save", "Save");
        final MessageBox.Result discardResult = MessageBox.Result.of("discard", "Don't save");
        final MessageBox box = ui.messageBox().setTitle("Unsaved changes").setText("Save the changes to this exchange?")
                .setButtons(List.of(saveResult, discardResult, MessageBox.Result.CANCEL), MessageBox.Result.CANCEL)
                .setSeverity(InfoBar.Severity.WARNING)
                .setContent(ui.checkBox("Don't ask again"));
        box.onEvent(MessageBox.ANSWERED, event -> {
            switch (event.value().id()) {
                case "save" -> save.run();
                case "discard" -> discard.run();
                default -> { }
            }
        });
        ui.overlays().dialog(box);
        return box;
    }
    // --8<-- [end:message-box-results]

    /**
     * A menu that picks how a list is sorted.
     *
     * @param ui the factory
     * @param sortedBy told the item the user picked
     * @return the menu
     */
    // --8<-- [start:radio-items]
    public static Menu sortMenu(final ComponentFactory ui, final Consumer<String> sortedBy) {
        final Menu menu = ui.menu();
        for (final String key : List.of("Name", "Date", "Size")) {
            // Items with the same group, compared with equals, are one choice; choosing one clears the others
            final RadioMenuItem item = ui.radioMenuItem("Sort by " + key.toLowerCase())
                    .setGroup("sort")
                    .setSelected(key.equals("Name"))
                    .setAction(() -> sortedBy.accept(key));
            menu.addItem(item);
        }
        return menu;
    }
    // --8<-- [end:radio-items]

    /**
     * A list whose rows have a menu of their own.
     *
     * @param ui the factory
     * @param names the rows
     * @param opened told the name whose menu opened
     * @return the list
     */
    // --8<-- [start:context-menu]
    public static ListView<String> rowMenus(final ComponentFactory ui, final List<String> names,
                                            final Consumer<String> opened) {
        // Each row has a menu, so a right click or the Menu key on a row has one to ask about
        final ListView<String> list = ui.<String>listView(name -> ui.label(name).setContextMenu(ui.menu()))
                .setItems(names);
        // The request reaches the list from every row; the control is the row whose menu is about to open
        list.onEvent(HasContextMenu.SHOW_CONTEXT_MENU, event -> {
            final String name = ((Label) event.control()).text().plain();
            opened.accept(name);
            event.setValue(ui.menu().addItem("Rename " + name, () -> { }).addItem("Delete " + name, () -> { }));
        });
        return list;
    }
    // --8<-- [end:context-menu]

    /**
     * A flyout that refuses to close while its form has unsaved changes.
     *
     * @param ui the factory
     * @param form the form it shows
     * @param unsaved whether the form has changes
     * @return the flyout
     */
    // --8<-- [start:flyout-close]
    public static Flyout formFlyout(final ComponentFactory ui, final Component<?> form, final BooleanSupplier unsaved) {
        final Flyout flyout = ui.flyout(form);
        // Escape and a press outside ask CLOSE first; canceling keeps the flyout open and uses up the key or press
        flyout.onEvent(Closable.CLOSE, event -> {
            if (unsaved.getAsBoolean()) {
                event.cancel();
            }
        });
        return flyout;
    }
    // --8<-- [end:flyout-close]

    /**
     * A window the user can maximize and collapse.
     *
     * @param ui the factory
     * @param content what the window shows
     * @param states told each change of the window's states
     * @return the window
     */
    // --8<-- [start:window-states]
    public static Window notesWindow(final ComponentFactory ui, final Component<?> content,
                                     final Consumer<String> states) {
        final Window window = ui.window("Notes")
                .setMaximizable(true)
                .setCollapsible(true)
                .add(content);
        window.onEvent(Window.MAXIMIZED, event -> states.accept(event.value() ? "maximized" : "restored"));
        window.onEvent(Window.COLLAPSED, event -> states.accept(event.value() ? "collapsed" : "expanded"));
        return window;
    }
    // --8<-- [end:window-states]
}
