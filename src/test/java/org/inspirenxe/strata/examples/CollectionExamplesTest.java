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

import org.inspirenxe.strata.api.control.collection.GridView;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.collection.TableView;
import org.inspirenxe.strata.api.control.collection.TreeView;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.input.Key;
import org.inspirenxe.strata.api.input.Modifier;
import org.inspirenxe.strata.api.state.State;
import org.inspirenxe.strata.examples.controls.CollectionExamples;
import org.inspirenxe.strata.testing.Matching;
import org.inspirenxe.strata.testing.ScreenTester;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that the collection examples in the controls guide do what the guide says.
 */
@SuppressWarnings("unchecked")
class CollectionExamplesTest {

    @Test
    void theOfferTableSortsByTwoColumnsAndKeepsItsColumnLimits() {
        final List<CollectionExamples.Offer> offers = List.of(new CollectionExamples.Offer("Axe", "Ana", 10),
                new CollectionExamples.Offer("Bow", "Ben", 5), new CollectionExamples.Offer("Cap", "Ana", 20));
        try (ScreenTester tester = ScreenTester.open(ui -> CollectionExamples.offerTable(ui, offers))) {
            final TableView<CollectionExamples.Offer> table = tester.find(TableView.class, any -> true);
            assertEquals(2, table.sortOrder().size());
            assertEquals(List.of(20, 10, 5), table.shownItems().stream().map(CollectionExamples.Offer::price).toList());
            assertEquals(offers, table.items());
            assertEquals(80f, table.columns().getFirst().minWidth());
            assertEquals(300f, table.columns().getFirst().maxWidth());
            assertTrue(!table.columns().get(1).isMovable() && !table.columns().get(2).isResizable());
        }
    }

    @Test
    void aTreeWithATestForChildrenAsksOnlyForTheFoldersThatOpen() {
        final Map<String, List<CollectionExamples.Folder>> disk = Map.of(
                "/", List.of(new CollectionExamples.Folder("docs", true), new CollectionExamples.Folder("empty", false)),
                "docs", List.of(new CollectionExamples.Folder("notes", false)));
        final List<String> asked = new ArrayList<>();
        try (ScreenTester tester = ScreenTester.open(ui -> CollectionExamples.folderTree(ui, disk, asked))) {
            final TreeView<CollectionExamples.Folder> tree = tester.find(TreeView.class, any -> true);
            assertEquals(List.of(), asked);
            tree.setExpanded(tree.items().getFirst(), true);
            assertEquals(List.of("docs"), asked);
            assertEquals(3, tree.shownItems().size());
        }
    }

    @Test
    void f2EditsARowOfAListAndEnterCommits() {
        final List<String> names = new ArrayList<>(List.of("Ana", "Ben", "Cleo"));
        final List<String> renamed = new ArrayList<>();
        try (ScreenTester tester = ScreenTester.open(ui -> CollectionExamples.editableNames(ui, names, renamed::add))) {
            tester.click(tester.find(Label.class, Matching.text("Ben")));
            tester.press(Key.F2);
            final TextBox editor = tester.find(TextBox.class, any -> true);
            assertEquals("Ben", editor.text());
            editor.setText("Bea");
            tester.press(Key.ENTER);
            assertEquals(List.of("Bea"), renamed);
            assertEquals(List.of("Ana", "Ben", "Cleo"), tester.find(ListView.class, any -> true).items());
        }
    }

    @Test
    void escapeCancelsTheEditOfAList() {
        final List<String> renamed = new ArrayList<>();
        try (ScreenTester tester = ScreenTester.open(ui -> CollectionExamples.editableNames(ui,
                List.of("Ana", "Ben"), renamed::add))) {
            tester.click(tester.find(Label.class, Matching.text("Ana")));
            tester.press(Key.F2);
            tester.press(Key.ESCAPE);
            assertEquals(List.of(), renamed);
            assertTrue(tester.findAll(TextBox.class, any -> true).isEmpty());
        }
    }

    @Test
    void aColumnEditorChangesTheSettingThroughTheReport() {
        final List<CollectionExamples.Setting> settings = new ArrayList<>(List.of(
                new CollectionExamples.Setting("Volume", "80"), new CollectionExamples.Setting("Distance", "12")));
        try (ScreenTester tester = ScreenTester.open(ui -> CollectionExamples.settingsTable(ui, settings))) {
            tester.click(tester.find(Label.class, Matching.text("Distance")));
            tester.press(Key.F2);
            tester.find(TextBox.class, any -> true).setText("16");
            tester.press(Key.ENTER);
            assertEquals(new CollectionExamples.Setting("Distance", "16"), settings.get(1));
            final TableView<CollectionExamples.Setting> table = tester.find(TableView.class, any -> true);
            assertEquals("16", table.shownItems().get(1).value());
        }
    }

    @Test
    void draggingARowInsideAListMovesItAndTheStateFollows() {
        final State<List<String>> names = State.of(List.of("Ana", "Ben", "Cleo"));
        try (ScreenTester tester = ScreenTester.open(ui -> CollectionExamples.reorderableList(ui, names))) {
            tester.dragTo(tester.find(Label.class, Matching.text("Ana")), tester.find(Label.class, Matching.text("Cleo")));
            assertEquals(3, names.get().size());
            assertTrue(!names.get().equals(List.of("Ana", "Ben", "Cleo")), names.get().toString());
            assertEquals(names.get(), tester.find(ListView.class, any -> true).items());
        }
    }

    @Test
    void typingJumpsToNamesThatStartWithTheLetters() {
        final List<CollectionExamples.Person> people = List.of(new CollectionExamples.Person("Ana"),
                new CollectionExamples.Person("Ben"), new CollectionExamples.Person("Cleo"),
                new CollectionExamples.Person("Cara"), new CollectionExamples.Person("Clara"),
                new CollectionExamples.Person("Dev"));
        try (ScreenTester tester = ScreenTester.open(ui -> CollectionExamples.peopleList(ui, people))) {
            final ListView<CollectionExamples.Person> list = tester.find(ListView.class, any -> true);
            list.requestFocus();
            tester.type("c");
            assertEquals("Cleo", list.selected().orElseThrow().name());
            tester.type("c");
            assertEquals("Cara", list.selected().orElseThrow().name());
            // A pause of a second ends the prefix, and "cl" starts a new one
            tester.advance(1500);
            tester.type("cl");
            assertEquals("Clara", list.selected().orElseThrow().name());
        }
    }

    @Test
    void controlAInAMultipleGridSelectsEveryCell() {
        final List<List<String>> chosen = new ArrayList<>();
        try (ScreenTester tester = ScreenTester.open(ui -> CollectionExamples.paletteGrid(ui,
                List.of("amber", "blue", "coral", "dune"), chosen::add))) {
            final GridView<String> grid = tester.find(GridView.class, any -> true);
            grid.requestFocus();
            tester.press(Key.A, Modifier.CONTROL);
            assertEquals(List.of("amber", "blue", "coral", "dune"), chosen.getLast());
        }
    }
}
