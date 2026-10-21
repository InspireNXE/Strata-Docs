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
package org.inspirenxe.strata.examples.text;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Tooltip;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.inspirenxe.strata.api.visual.Visual;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * The text guide's examples.
 */
public final class TextExamples {
    private static final int GOLD = 0xFFFFAA00;
    private static final int RED = 0xFFFF5555;
    private static final int GRAY = 0xFFAAAAAA;
    private static final int AQUA = 0xFF55FFFF;

    private TextExamples() {
    }

    static Label styled(final ComponentFactory ui) {
        // --8<-- [start:styled]
        // "Price: 64 each", with the number in gold and bold
        final Text price = Text.of("Price: ")
                .append(Text.of("64").color(GOLD).bold())
                .append(" each");

        // The same line with the builder, which reads better once a line has many parts
        final Text built = Text.builder()
                .append("Price: ")
                .color("64", GOLD)
                .append(" each")
                .build();
        // --8<-- [end:styled]
        return ui.label(price).setTooltip(built);
    }

    static Label translated(final ComponentFactory ui, final boolean canAfford) {
        // --8<-- [start:translated]
        // shop.price is "Price: %s each" in English and "Preis: %s pro Stück" in German
        final Text amount = Text.of("64").color(GOLD).bold();
        final Text price = Text.translatable("shop.price", amount);

        // Red when the player cannot afford it; the gold amount keeps its own color
        final Label label = ui.label(canAfford ? price : price.color(RED));
        // --8<-- [end:translated]
        return label;
    }

    static Tooltip tooltip(final String seller, final int stock, final boolean expiresSoon) {
        // --8<-- [start:tooltip]
        final List<Text> lines = new ArrayList<>();
        lines.add(Text.translatable("shop.listing.seller", Text.of(seller).color(AQUA)));
        lines.add(Text.translatable("shop.listing.stock", stock).color(GRAY));
        if (expiresSoon) {
            lines.add(Text.translatable("shop.listing.expires").color(RED).italic());
        }
        final Tooltip tooltip = Tooltip.lines(lines);
        // --8<-- [end:tooltip]
        return tooltip;
    }

    static Label highlighted(final ComponentFactory ui, final Text name, final String query) {
        // --8<-- [start:matches]
        // Bold every match of what the player typed, in whatever language the name is in
        final Pattern pattern = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE);
        final Label row = ui.cellLabel(name.styleMatches(pattern, style -> style.withBold(true).withUnderline(true)));
        // --8<-- [end:matches]
        return row;
    }

    static Text sellers(final List<String> names) {
        // --8<-- [start:join]
        // "Ana, Ben and Cleo are selling this"
        final List<Text> parts = names.stream().map(name -> Text.of(name).color(AQUA)).toList();
        final Text sellers = Text.translatable("shop.sellers",
                Text.join(Text.of(", "), parts.subList(0, parts.size() - 1)),
                parts.getLast());
        // --8<-- [end:join]
        return sellers;
    }

    static Label priced(final ComponentFactory ui, final Visual lantern, final int price) {
        // --8<-- [start:inline]
        // The lantern's picture at its own size, then the price with a coin as tall as the text
        final Text cost = Text.of(price + " ").append(Text.sprite(SpriteId.of("shop", "coin")));
        final Label row = ui.label(Text.concat(Text.visual(lantern), Text.of(" Lantern  "), cost));
        // --8<-- [end:inline]
        return row;
    }

    static Text shouted(final Text line) {
        // --8<-- [start:inline-spans]
        // Capitals for the characters; spans that hold a picture pass through unchanged
        final Text shouted = line.mapSpans(span -> span.isInline()
                ? span
                : span.withText(span.text().toUpperCase(Locale.ROOT)));
        // --8<-- [end:inline-spans]
        return shouted;
    }
}
