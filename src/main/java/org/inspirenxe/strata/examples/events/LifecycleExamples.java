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
package org.inspirenxe.strata.examples.events;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.state.State;

import java.util.List;

/**
 * The events guide's examples of attaching and detaching.
 */
public final class LifecycleExamples {

    private LifecycleExamples() {
    }

    /**
     * A feed of prices that a network thread sets.
     */
    public static final class PriceFeed {
        public final State<Double> latest = State.of(0.0);
    }

    /**
     * A label that shows the feed's price only while it is shown.
     *
     * @param ui the factory
     * @param feed the feed
     * @return the label
     */
    public static Label priceLabel(final ComponentFactory ui, final PriceFeed feed) {
        // --8<-- [start:while-attached]
        final Label price = ui.label("-");
        // Starts when the label joins a shown tree, stops when it leaves, and starts again if it comes back.
        // The feed calls the listener on its own thread, so the label changes on the UI thread.
        price.whileAttached(() -> feed.latest.subscribe(
                value -> ui.uiThread().execute(() -> price.setText("%.2f".formatted(value)))));
        // --8<-- [end:while-attached]
        return price;
    }

    /**
     * Writes down when each part of a tree joins and leaves.
     *
     * @param log receives one line per event
     * @param components the components to watch, in the order they are nested
     */
    public static void order(final List<String> log, final List<Component<?>> components) {
        // --8<-- [start:order]
        for (final Component<?> component : components) {
            final String name = component.id().orElse("?");
            component.onEvent(Component.ATTACHED, event -> log.add("attached " + name));
            component.onEvent(Component.DETACHED, event -> log.add("detached " + name));
        }
        // --8<-- [end:order]
    }

    /**
     * A panel that holds a label, for the order example.
     *
     * @param ui the factory
     * @return the panel
     */
    public static StackPanel panelWithLabel(final ComponentFactory ui) {
        return ui.stackPanel().setId("panel").add(ui.label("Inside").setId("label"));
    }
}
