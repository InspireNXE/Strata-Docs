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
package org.inspirenxe.strata.examples.clipboard;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.transfer.DataFormat;
import org.inspirenxe.strata.api.transfer.Transfer;

import java.util.List;
import java.util.Optional;

/**
 * The clipboard guide's examples.
 */
public final class ClipboardExamples {

    private ClipboardExamples() {
    }

    /**
     * A row of the application's own data.
     */
    public record Listing(String name, int price) {
    }

    // --8<-- [start:formats]
    /**
     * Listings as objects. A local format never leaves the process, so no other program sees it.
     */
    public static final DataFormat<List<Listing>> LISTINGS = ClipboardExamples.listingsFormat();

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static DataFormat<List<Listing>> listingsFormat() {
        return (DataFormat) DataFormat.local("examplemod.listings", List.class);
    }

    private static final DataFormat<String> CSV = DataFormat.mime("text/csv");
    // --8<-- [end:formats]

    // --8<-- [start:copy]
    /**
     * Copies listings for a spreadsheet as text and CSV, and for a paste inside the application as the objects.
     */
    public static void copy(final ComponentFactory ui, final List<Listing> selected) {
        final StringBuilder tabs = new StringBuilder();
        final StringBuilder csv = new StringBuilder();
        for (final Listing listing : selected) {
            tabs.append(listing.name()).append('\t').append(listing.price()).append('\n');
            csv.append(listing.name()).append(',').append(listing.price()).append('\n');
        }
        ui.setClipboard(Transfer.builder()
                .add(DataFormat.TEXT, tabs.toString())
                .add(CSV, csv.toString())
                .add(LISTINGS, List.copyOf(selected))
                .build());
    }
    // --8<-- [end:copy]

    // --8<-- [start:paste]
    /**
     * Pastes listings if the clipboard holds them as objects, else reads lines of "name,price" from its text.
     */
    public static List<Listing> paste(final ComponentFactory ui) {
        final Optional<Transfer> clipboard = ui.clipboard();
        final Optional<List<Listing>> objects = clipboard.flatMap(transfer -> transfer.get(LISTINGS));
        if (objects.isPresent()) {
            return objects.get();
        }
        return clipboard.flatMap(transfer -> transfer.get(DataFormat.TEXT)).stream()
                .flatMap(text -> text.lines())
                .map(line -> line.split(","))
                .filter(parts -> parts.length == 2)
                .map(parts -> new Listing(parts[0], Integer.parseInt(parts[1].trim())))
                .toList();
    }
    // --8<-- [end:paste]
}
