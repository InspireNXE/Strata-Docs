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
package org.inspirenxe.strata.examples.adapter;

// --8<-- [start:themes]
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.geometry.Insets;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.inspirenxe.strata.api.theme.Theme;
import org.inspirenxe.strata.api.theme.ThemeId;
import org.inspirenxe.strata.api.theme.Themes;
import org.inspirenxe.strata.spi.ThemeDocument;
import org.inspirenxe.strata.spi.ThemeSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Themes for the imaginary engine. The engine's own look is a theme built in code from its widget images, and
 * every resource pack the player turns on may hold a theme file for any id.
 */
public final class ToyThemeSource implements ThemeSource {
    /** The engine's look, which screens open in when no theme is given. */
    public static final ThemeId ENGINE = ThemeId.of("toy", "widgets");

    private final Theme engine;
    private final AtomicLong version = new AtomicLong();
    // The engine loads packs on a thread of its own, so the list is replaced whole
    private volatile List<Pack> packs = List.of();

    public ToyThemeSource() {
        // Every required key comes from the flat theme; the engine's images replace the button faces
        final SpriteId button = SpriteId.of("toy", "widget/button");
        this.engine = Themes.flat().toBuilder()
                .paint(Button.STYLE_NORMAL, Paint.nineSlice(button, Insets.of(3f)))
                .paint(Button.STYLE_HOVERED, new Paint.NineSlice(button, Insets.of(3f), 0xFFE0E0FF))
                .build();
    }

    /**
     * A resource pack: its id and the text of its theme files by theme id.
     *
     * @param id the pack's id, which names its layer
     * @param themes the JSON of each theme file the pack holds
     */
    public record Pack(String id, Map<ThemeId, String> themes) {
    }

    /** Called by the engine after it loads packs, the lowest priority first. */
    public void reload(final List<Pack> packs) {
        this.packs = List.copyOf(packs);
        // Hosts compare the version on every layout and render, and resolve their themes again when it changes
        this.version.incrementAndGet();
    }

    @Override
    public Optional<Theme> base(final ThemeId id) {
        // Core registers strata:flat itself; this source adds only the engine's theme
        return id.equals(ToyThemeSource.ENGINE) ? Optional.of(this.engine) : Optional.empty();
    }

    @Override
    public List<ThemeDocument> documents(final ThemeId id) {
        // Lowest priority first, so a pack the player put above another wins per key
        final List<ThemeDocument> documents = new ArrayList<>();
        for (final Pack pack : this.packs) {
            final String json = pack.themes().get(id);
            if (json != null) {
                // The origin names the file in messages; the layer is what Theme.layer reports for its keys
                documents.add(new ThemeDocument(pack.id() + ":themes/" + id.path() + ".json", json, pack.id()));
            }
        }
        return documents;
    }

    @Override
    public ThemeId defaultTheme() {
        return ToyThemeSource.ENGINE;
    }

    @Override
    public long version() {
        return this.version.get();
    }
}
// --8<-- [end:themes]
