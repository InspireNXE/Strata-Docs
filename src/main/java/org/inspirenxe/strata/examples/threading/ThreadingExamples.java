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
package org.inspirenxe.strata.examples.threading;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.text.HasText;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.event.cause.CauseFrame;
import org.inspirenxe.strata.api.event.cause.CauseKeys;
import org.inspirenxe.strata.api.event.cause.Origin;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.state.State;
import org.inspirenxe.strata.api.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The threading guide's examples.
 */
public final class ThreadingExamples {

    private ThreadingExamples() {
    }

    /**
     * Shows offers from a seller who is another person.
     */
    public static final class OffersScreen implements Screen {
        /** The count of offers, a state any thread may set. */
        public final State<Integer> count = State.of(0);
        private ListView<String> offers;
        private TextBox latest;
        private ComponentFactory ui;

        @Override
        public Component<?> build(final ComponentFactory factory) {
            this.ui = factory;
            this.offers = factory.<String>listView();
            this.latest = factory.textBox();
            return factory.stackPanel()
                    .add(factory.label().bind(HasText.TEXT,
                            () -> Text.of(this.count.get() + " offers")))
                    .add(this.latest)
                    .add(this.offers);
        }

        /**
         * Returns the box that shows the newest offer.
         *
         * @return the box
         */
        public TextBox latest() {
            return this.latest;
        }

        /**
         * Returns the list the offers show in.
         *
         * @return the list
         */
        public ListView<String> offers() {
            return this.offers;
        }

        /**
         * Returns the factory the screen was built with.
         *
         * @return the factory
         */
        public ComponentFactory factory() {
            return this.ui;
        }

        // --8<-- [start:update]
        /**
         * Applies an update that arrived on a network thread. Hand the work to the UI thread, and say whose change it
         * is so handlers see {@code Origin.Remote}.
         */
        public void onNetworkUpdate(final String seller, final List<String> rows) {
            this.ui.uiThread().execute(() -> {
                try (CauseFrame _ = this.ui.causes().pushFrame().put(CauseKeys.ORIGIN, new Origin.Remote(seller))) {
                    this.offers.setItems(rows);
                    this.latest.setText(rows.getFirst());       // its EDITED report says the seller did it
                }
            });
            // A state needs no hand-over: set it from any thread
            this.count.set(rows.size());
        }
        // --8<-- [end:update]

        // --8<-- [start:submit]
        /**
         * Reads the list from another thread: the task runs on the UI thread and the future completes there.
         */
        public CompletableFuture<Integer> offerCount() {
            return this.ui.uiThread().submit(() -> this.offers.items().size());
        }
        // --8<-- [end:submit]
    }

    // --8<-- [start:worker-tree]
    /**
     * Builds a panel on a worker thread. Nothing is attached yet, so nothing is checked.
     */
    public static StackPanel buildRows(final ComponentFactory ui, final List<String> names) {
        final StackPanel rows = ui.stackPanel();
        for (final String name : names) {
            rows.add(ui.label(name));
        }
        return rows;
    }

    /**
     * Adds the finished panel on the UI thread, where it becomes part of the screen.
     */
    public static void show(final ComponentFactory ui, final StackPanel parent, final StackPanel rows) {
        ui.uiThread().execute(() -> parent.add(rows));
    }
    // --8<-- [end:worker-tree]

    /**
     * Collects the labels of a panel, for tests.
     *
     * @param panel the panel
     * @return the plain text of each label in it
     */
    public static List<String> texts(final StackPanel panel) {
        final List<String> texts = new ArrayList<>();
        for (final Component<?> child : panel.children()) {
            if (child instanceof Label label) {
                texts.add(label.text().plain());
            } else if (child instanceof StackPanel inner) {
                texts.addAll(texts(inner));
            }
        }
        return texts;
    }
}
