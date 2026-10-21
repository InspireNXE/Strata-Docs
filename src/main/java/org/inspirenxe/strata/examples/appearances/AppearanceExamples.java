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
package org.inspirenxe.strata.examples.appearances;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.Strata;
import org.inspirenxe.strata.api.appearance.Appearance;
import org.inspirenxe.strata.api.appearance.Appearances;
import org.inspirenxe.strata.api.component.Tooltip;
import org.inspirenxe.strata.api.control.collection.ComboBox;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.visual.Visual;

import java.util.List;
import java.util.Optional;

/**
 * The appearances guide's examples.
 */
public final class AppearanceExamples {

    private AppearanceExamples() {
    }

    /**
     * A crate of fruit for sale.
     *
     * @param name the fruit's name
     * @param icon the fruit's icon
     * @param stock how many are left
     */
    record Crate(String name, Visual icon, int stock) {
    }

    /**
     * A listing of a crate at a price.
     *
     * @param crate the crate
     * @param price the price
     */
    record Listing(Crate crate, int price) {
    }

    static void register() {
        // --8<-- [start:register]
        Strata.appearances().register(Crate.class, Appearance.<Crate>builder()
                .icon(Crate::icon)
                .name(crate -> Text.of(crate.name()))
                .tooltip(crate -> Tooltip.text(crate.stock() + " left"))
                .build());

        // A listing looks like its crate
        Strata.appearances().register(Listing.class, Appearance.as(Listing::crate));
        // --8<-- [end:register]
    }

    static ComboBox<Listing> use(final ComponentFactory ui, final List<Listing> listings) {
        // --8<-- [start:use]
        // Rows show each crate's icon, name and stock, with no cell factory
        final ListView<Listing> list = ui.<Listing>listView().setItems(listings);

        // Label functions take Text, a String, or a value with an appearance
        final ComboBox<Listing> cheapest = ui.<Listing>comboBox(listing -> Text.of(listing.price() + " coins"))
                .setItems(listings);
        // --8<-- [end:use]
        list.setSize(160, 120);
        return cheapest;
    }

    static Label lookup(final ComponentFactory ui, final Listing listing) {
        // --8<-- [start:lookup]
        // A heading for a listing: its icon and name in gold, with the same tooltip a list row would have
        final Appearances look = ui.appearances();
        final Text name = look.nameOf(listing).color(0xFFFFAA00);
        final Optional<Visual> icon = look.iconOf(listing);
        final Label heading = ui.label(icon.map(visual -> Text.visual(visual).append(" ").append(name)).orElse(name));
        look.tooltipOf(listing).ifPresent(tooltip -> heading.setTooltip(tooltip));
        // --8<-- [end:lookup]
        return heading;
    }
}
