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
package org.inspirenxe.strata.examples.dragdrop;

import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.transfer.DropEffect;
import org.inspirenxe.strata.api.transfer.Transfer;
import org.inspirenxe.strata.testing.ScreenTester;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The drag and drop guide's examples, run.
 */
class DragDropExamplesTest {

    @Test
    void aRowMovesFromOneListToTheOther() {
        final DragDropExamples.MoveRows screen =
                new DragDropExamples.MoveRows(List.of("Ana", "Ben", "Cleo"), List.of("Dev"));
        try (ScreenTester tester = ScreenTester.open(screen)) {
            // The rows are components too: a cell shows its row's text in a label
            final Label ben = tester.find(Label.class, label -> label.text().plain().equals("Ben"));

            tester.dragTo(ben, screen.right());

            assertEquals(List.of("Ana", "Cleo"), screen.left().items());
            assertEquals(List.of("Dev", "Ben"), screen.right().items());
        }
    }

    @Test
    void aRowDroppedOnItsOwnListIsReorderedByTheList() {
        final DragDropExamples.MoveRows screen =
                new DragDropExamples.MoveRows(List.of("Ana", "Ben", "Cleo"), List.of());
        try (ScreenTester tester = ScreenTester.open(screen)) {
            final Label ana = tester.find(Label.class, label -> label.text().plain().equals("Ana"));
            final Label cleo = tester.find(Label.class, label -> label.text().plain().equals("Cleo"));

            tester.dragTo(ana, cleo);

            // Every row is still there: the list moved Ana, and the move did not count as leaving for another list
            assertEquals(List.of("Ana", "Ben", "Cleo"), screen.left().items().stream().sorted().toList());
            assertEquals(List.of(), screen.right().items());
        }
    }

    @Test
    void aTileDroppedOnABinIsTakenAsText() {
        final DragDropExamples.Tile[] tile = new DragDropExamples.Tile[1];
        final DragDropExamples.Bin[] bin = new DragDropExamples.Bin[1];
        final Screen screen = ui -> {
            tile[0] = new DragDropExamples.Tile(ui, "Ana");
            bin[0] = new DragDropExamples.Bin(ui);
            return ui.absolutePanel().add(tile[0], 10, 10).add(bin[0], 200, 100);
        };
        try (ScreenTester tester = ScreenTester.open(screen)) {
            tester.dragTo(tile[0], bin[0]);

            assertEquals(List.of("Ana"), bin[0].taken());
        }
    }

    @Test
    void filesFromAnotherApplicationAreNotTakenByTheBin() {
        final DragDropExamples.Bin[] bin = new DragDropExamples.Bin[1];
        final Screen screen = ui -> {
            bin[0] = new DragDropExamples.Bin(ui);
            return ui.absolutePanel().add(bin[0], 20, 20);
        };
        try (ScreenTester tester = ScreenTester.open(screen)) {
            final Optional<DropEffect> taken = tester.drop(bin[0], Transfer.files(List.of(Path.of("notes.txt"))));

            assertEquals(Optional.empty(), taken);
            assertEquals(List.of(), bin[0].taken());
        }
    }

    @Test
    void textFromAnotherApplicationIsTaken() {
        final DragDropExamples.Bin[] bin = new DragDropExamples.Bin[1];
        final Screen screen = ui -> {
            bin[0] = new DragDropExamples.Bin(ui);
            return ui.absolutePanel().add(bin[0], 20, 20);
        };
        try (ScreenTester tester = ScreenTester.open(screen)) {
            final Optional<DropEffect> taken = tester.drop(bin[0], Transfer.text("hello"));

            assertEquals(Optional.of(DropEffect.MOVE), taken);
            assertEquals(List.of("hello"), bin[0].taken());
        }
    }
}
