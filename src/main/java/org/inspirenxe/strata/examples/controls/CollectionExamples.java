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
import org.inspirenxe.strata.api.component.Focusable;
import org.inspirenxe.strata.api.control.attribute.item.CellEditing;
import org.inspirenxe.strata.api.control.attribute.item.ItemDragging;
import org.inspirenxe.strata.api.control.attribute.item.MultiSelection;
import org.inspirenxe.strata.api.control.attribute.item.SelectionMode;
import org.inspirenxe.strata.api.control.collection.GridView;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.collection.TableColumn;
import org.inspirenxe.strata.api.control.collection.TableView;
import org.inspirenxe.strata.api.control.collection.TreeView;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.event.input.InputEvents;
import org.inspirenxe.strata.api.input.Key;
import org.inspirenxe.strata.api.input.KeyAction;
import org.inspirenxe.strata.api.layout.GridLength;
import org.inspirenxe.strata.api.layout.ScrollPanel;
import org.inspirenxe.strata.api.state.State;
import org.inspirenxe.strata.api.transfer.Transfer;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The controls guide's examples for lists, tables, trees and grids.
 */
public final class CollectionExamples {

    private CollectionExamples() {
    }

    /**
     * An offer in a market.
     *
     * @param item what is for sale
     * @param seller who sells it
     * @param price what it costs
     */
    public record Offer(String item, String seller, int price) {
    }

    /**
     * A named setting.
     *
     * @param name the setting's name
     * @param value the setting's value
     */
    public record Setting(String name, String value) {
        public Setting withValue(final String value) {
            return new Setting(this.name, value);
        }
    }

    /**
     * A folder that is loaded when it opens.
     *
     * @param name the folder's name
     * @param hasChildren whether it holds anything
     */
    public record Folder(String name, boolean hasChildren) {
    }

    /**
     * Someone to pick from a list.
     *
     * @param name the person's name
     */
    public record Person(String name) {
    }

    // --8<-- [start:columns]
    public static TableView<Offer> offerTable(final ComponentFactory ui, final List<Offer> offers) {
        final TableColumn<Offer> item = ui.<Offer>tableColumn("Item")
                .setWidth(GridLength.weighted(1))
                .setWidthLimits(80, 300)
                .setCellText(Offer::item);
        final TableColumn<Offer> seller = ui.<Offer>tableColumn("Seller")
                .setWidth(GridLength.fixed(70))
                .setCellText(Offer::seller)
                .setMovable(false);
        final TableColumn<Offer> price = ui.<Offer>tableColumn("Price")
                .setWidth(GridLength.fixed(50))
                .setCellText(offer -> String.valueOf(offer.price()))
                .setComparator(Comparator.comparingInt(Offer::price))
                .setResizable(false);
        return ui.<Offer>tableView()
                .addColumn(item)
                .addColumn(seller)
                .addColumn(price)
                .setHorizontalPolicy(ScrollPanel.ScrollPolicy.AUTO)
                .setItems(offers)
                .sortBy(List.of(new TableView.Sort(seller, true), new TableView.Sort(price, false)));
    }
    // --8<-- [end:columns]

    // --8<-- [start:lazy-tree]
    public static TreeView<Folder> folderTree(final ComponentFactory ui, final Map<String, List<Folder>> disk,
                                       final List<String> asked) {
        return ui.<Folder>treeView(folder -> ui.label(folder.name()))
                .setHasChildren(Folder::hasChildren)
                .setChildren(folder -> {
                    asked.add(folder.name());
                    return disk.getOrDefault(folder.name(), List.of());
                })
                .setItems(disk.get("/"));
    }
    // --8<-- [end:lazy-tree]

    // --8<-- [start:list-editor]
    public static ListView<String> editableNames(final ComponentFactory ui, final List<String> names,
                                          final Consumer<String> renamed) {
        final ListView<String> list = ui.<String>listView()
                .setItems(names)
                .setCellEditor((factory, name, commit, cancel) -> {
                    final TextBox box = factory.textBox().setText(name);
                    box.onEvent(TextBox.SUBMITTED, event -> commit.accept(event.value()));
                    box.onEvent(InputEvents.KEY, event -> {
                        if (event.key() == Key.ESCAPE && event.action() == KeyAction.PRESS) {
                            cancel.run();
                            event.consume();
                        }
                    });
                    return box;
                });
        // The list leaves its items alone: store the new name, then show the items again
        list.onEvent(CellEditing.CELL_EDITED, event -> renamed.accept((String) event.value()));
        return list;
    }
    // --8<-- [end:list-editor]

    // --8<-- [start:table-editor]
    public static TableView<Setting> settingsTable(final ComponentFactory ui, final List<Setting> settings) {
        final TableView<Setting> table = ui.<Setting>tableView()
                .setKeyFunction(Setting::name)
                .addTextColumn("Setting", GridLength.fixed(100), Setting::name)
                .addColumn(ui.<Setting>tableColumn("Value")
                        .setCellText(Setting::value)
                        .setCellEditor((factory, setting, commit, cancel) -> factory.textBox()
                                .setText(setting.value())
                                .onEvent(TextBox.SUBMITTED, event -> commit.accept(setting.withValue(event.value())))
                                .onEvent(Focusable.FOCUS_LOST, event -> cancel.run())))
                .setItems(settings);
        table.onEvent(CellEditing.CELL_EDITED, event -> {
            final Setting edited = (Setting) event.value();
            settings.replaceAll(setting -> setting.name().equals(edited.name()) ? edited : setting);
            table.setItems(settings);
        });
        return table;
    }
    // --8<-- [end:table-editor]

    // --8<-- [start:reorder]
    public static ListView<String> reorderableList(final ComponentFactory ui, final State<List<String>> names) {
        return ui.<String>listView()
                .setSelectionMode(SelectionMode.MULTIPLE)
                .setItems(names.get())
                // Rows can be dragged to another application or component as text, one name to a line
                .setDragTransfer(rows -> Transfer.text(String.join("\n", rows)))
                // Dropping rows inside the list moves them; the list sets its items, and the state follows
                .onEvent(ItemDragging.MOVE_ITEMS, event -> names.set(event.value().apply(names.get())));
    }
    // --8<-- [end:reorder]

    // --8<-- [start:type-ahead]
    public static ListView<Person> peopleList(final ComponentFactory ui, final List<Person> people) {
        return ui.<Person>listView(person -> ui.label(person.name()))
                .setItems(people)
                // Typing "c" jumps to the first name that starts with c, and typing "c" again to the next one
                .setTypeAhead(Person::name);
    }
    // --8<-- [end:type-ahead]

    // --8<-- [start:grid]
    public static GridView<String> paletteGrid(final ComponentFactory ui, final List<String> colors,
                                        final Consumer<List<String>> chosen) {
        final GridView<String> grid = ui.<String>gridView(color -> ui.label(color.substring(0, 1)))
                .setColumns(4)
                .setSelectable(true)
                .setSelectionMode(SelectionMode.MULTIPLE)
                .setItems(colors)
                .setHeight(40);
        // The indexes count cells, and an empty cell is a null item
        grid.onEvent(MultiSelection.SELECTED_MANY, event -> chosen.accept(grid.selectedItems()));
        return grid;
    }
    // --8<-- [end:grid]
}
