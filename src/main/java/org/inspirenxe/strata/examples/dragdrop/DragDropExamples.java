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

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.item.ItemDragging;
import org.inspirenxe.strata.api.control.attribute.item.SelectionMode;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.event.input.DragEvent;
import org.inspirenxe.strata.api.event.input.InputEvents;
import org.inspirenxe.strata.api.event.input.TransferEvent;
import org.inspirenxe.strata.api.extension.CustomComponent;
import org.inspirenxe.strata.api.extension.MeasureContext;
import org.inspirenxe.strata.api.extension.Painter;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.geometry.Size;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.TextSpan;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.transfer.DataFormat;
import org.inspirenxe.strata.api.transfer.DropEffect;
import org.inspirenxe.strata.api.transfer.Transfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * The drag and drop guide's examples.
 */
public final class DragDropExamples {

    private DragDropExamples() {
    }

    // --8<-- [start:tile]
    /**
     * A square that can be dragged. It carries its name as text, and shows a small label under the pointer.
     */
    public static final class Tile extends CustomComponent<Tile> {
        private final String name;
        private final TextSpan span;

        public Tile(final ComponentFactory ui, final String name) {
            super(ui);
            this.name = name;
            this.span = new TextSpan(name, TextStyle.DEFAULT);
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
            return new Size(40f, 40f);
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            painter.fill(bounds, 0xFF4060A0);
            painter.text(this.span, bounds.x() + 4f, bounds.y() + 4f);
        }

        @Override
        protected void onDragStart(final DragEvent event) {
            // Only a press that moved past the drag threshold gets here. Copy and move are both allowed.
            event.startTransfer(Transfer.text(this.name), EnumSet.of(DropEffect.MOVE, DropEffect.COPY),
                    factory -> factory.label(this.name));
        }
    }
    // --8<-- [end:tile]

    // --8<-- [start:bin]
    /**
     * A box that takes text and keeps what it was given.
     */
    public static final class Bin extends CustomComponent<Bin> {
        private final List<String> taken = new ArrayList<>();
        private boolean over;

        public Bin(final ComponentFactory ui) {
            super(ui);
        }

        public List<String> taken() {
            return List.copyOf(this.taken);
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth, final float availableHeight) {
            return new Size(60f, 60f);
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            painter.fill(bounds, this.over ? 0xFF60A060 : 0xFF404040);
        }

        @Override
        protected void onDragEnter(final TransferEvent event) {
            this.over = event.transfer().has(DataFormat.TEXT);
            this.repaint();
        }

        @Override
        protected void onDragOver(final TransferEvent event) {
            // Take the drag with the effect the modifier keys ask for. Files and other formats are not taken.
            if (event.transfer().has(DataFormat.TEXT)) {
                event.accept();
            }
        }

        @Override
        protected void onDragExit(final TransferEvent event) {
            this.over = false;
            this.repaint();
        }

        @Override
        protected void onDrop(final TransferEvent event) {
            event.transfer().get(DataFormat.TEXT).ifPresent(this.taken::add);
        }
    }
    // --8<-- [end:bin]

    /**
     * Two lists that trade rows. The rows travel as text, one row to a line.
     */
    public static final class MoveRows implements Screen {
        private final List<String> leftRows;
        private final List<String> rightRows;
        private ListView<String> left;
        private ListView<String> right;

        public MoveRows(final List<String> leftRows, final List<String> rightRows) {
            this.leftRows = leftRows;
            this.rightRows = rightRows;
        }

        public ListView<String> left() {
            return this.left;
        }

        public ListView<String> right() {
            return this.right;
        }

        @Override
        public Component<?> build(final ComponentFactory ui) {
            // --8<-- [start:lists]
            this.left = MoveRows.rowList(ui, this.leftRows).setId("left");
            this.right = MoveRows.rowList(ui, this.rightRows).setId("right");
            MoveRows.trade(this.left, this.right);
            MoveRows.trade(this.right, this.left);
            // --8<-- [end:lists]
            return ui.stackPanel(Orientation.HORIZONTAL).setSpacing(8).setPadding(8)
                    .add(this.left).add(this.right);
        }

        // --8<-- [start:rowlist]
        private static ListView<String> rowList(final ComponentFactory ui, final List<String> rows) {
            final ListView<String> list = ui.<String>listView().setItems(rows);
            list.setSelectionMode(SelectionMode.MULTIPLE).setSize(100f, 80f);
            // Dragging rows carries them as text, one row to a line. Without this function rows are not dragged.
            list.setDragTransfer(selected -> Transfer.text(String.join("\n", selected)));
            return list;
        }
        // --8<-- [end:rowlist]

        // --8<-- [start:trade]
        /**
         * Lets {@code target} take rows dragged out of {@code from}, and has {@code from} remove them once they moved.
         */
        private static void trade(final ListView<String> from, final ListView<String> target) {
            // Rows dragged within one list are reordered by the list itself. Rows from the other list are the
            // project's to take.
            target.onEvent(InputEvents.DRAG_OVER, event -> {
                if (event.source().orElse(null) == from && event.transfer().has(DataFormat.TEXT)) {
                    event.accept();
                }
            });
            target.onEvent(InputEvents.DROP, event -> {
                if (event.source().orElse(null) != from) {
                    return;
                }
                final List<String> items = new ArrayList<>(target.items());
                items.addAll(MoveRows.rows(event.transfer()));
                target.setItems(items);
            });
            // The source hears how the drag ended. Remove the rows only for a move that the other list took.
            from.onEvent(InputEvents.TRANSFER_DONE, event -> {
                if (event.effect().orElse(null) == DropEffect.MOVE && MoveRows.tookRows(target, event.transfer())) {
                    final List<String> items = new ArrayList<>(from.items());
                    MoveRows.rows(event.transfer()).forEach(items::remove);
                    from.setItems(items);
                }
            });
        }
        // --8<-- [end:trade]

        private static boolean tookRows(final ListView<String> target, final Transfer transfer) {
            return target.items().containsAll(MoveRows.rows(transfer));
        }

        private static List<String> rows(final Transfer transfer) {
            return Arrays.asList(transfer.get(DataFormat.TEXT).orElse("").split("\n"));
        }
    }

    /**
     * Listens for reorders of a list.
     */
    public static void reorder(final ListView<String> list, final List<String> log) {
        // --8<-- [start:reorder]
        list.onEvent(ItemDragging.MOVE_ITEMS, event -> {
            final ItemDragging.ItemMove move = event.value();
            log.add("moving " + move.indexes() + " before " + move.before());
            // Cancel the event to refuse the move, or event.setValue(...) to change it
        });
        // --8<-- [end:reorder]
    }
}
