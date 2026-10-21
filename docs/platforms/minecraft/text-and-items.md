# Text and items on Minecraft

Strata-Minecraft draws Strata [text](../../guides/text.md) with the game's font, reads translations from the game's language, and gives vanilla's items and players an [appearance](../../guides/appearances.md), so they show in lists, images and avatars with no Minecraft-specific Strata type. Everything on this page is in `org.inspirenxe.strata.minecraft` unless it says otherwise.

## Text

Text draws in the hosting screen's font, which follows the player's resource packs.

| Style | In game |
|---|---|
| Bold | Vanilla bold, one pixel wider per character |
| Italic | Vanilla italic |
| Underline, strikethrough | Vanilla's |
| Shadow | Vanilla's text shadow |
| Monospace | The normal font. Vanilla's default font has no monospaced form, so `Capabilities.monospaceFont` is false and monospaced text is measured and drawn in the default font. Columns of monospaced text do not line up. |

`MinecraftTextMetrics` measures with the font's `width` and `lineHeight`. Widths are whole GUI pixels and include vanilla's extra pixel for bold, and the line height is the font's, 9 for the default font. Color and shadow do not change widths. Text that just fits on Swing may wrap or overflow in game, so let components measure their text and leave room in fixed widths around it. Text positions round to the nearest GUI pixel.

Keep vanilla's `§` formatting codes out of plain Strata text. Strata does not interpret them and vanilla's text drawing may, so the same string would look different on Swing and in game. Wrap formatted text with `MinecraftText.of`, as the next section shows.

## Formatted text

`MinecraftText.of` wraps a vanilla `FormattedText`, usually a chat `Component`, or a string with `§` codes, as Strata `Text`. Anything that takes `Text` takes it: labels, button captions, titles, tooltips.

```java
// A chat component; parts that set no style draw in the label's style
final Label title = factory.label(MinecraftText.of(
        Component.translatable("container.inventory").withStyle(ChatFormatting.YELLOW)));

// Section codes over a gray base without a shadow
final Label status = factory.label(MinecraftText.of("§aOnline§r, 12 players"))
        .setStyle(MinecraftText.DEFAULT_STYLE.withColor(0xFFA0A0A0).withShadow(false));
```

The control resolves the text against its own style, its base, and each run of vanilla text is laid over that base:

| From vanilla | In Strata |
|---|---|
| Color | Replaces the base's color and keeps the base's alpha, since vanilla colors have none |
| Bold, italic, underline, strikethrough | Replace the base's where the vanilla style sets them, true or false; unset ones come from the base |
| `§` codes | Applied as vanilla applies them when it draws. A color code clears bold, italic, underline and strikethrough. `§r` goes back to the style of the component the code is in, or to the base in a plain string. Unknown codes and a trailing `§` are dropped, and so are the codes themselves. |
| Shadow and monospace | Always the base's |
| Obfuscated text | Plain text |
| Fonts, shadow colors, click and hover events, insertions | Dropped |

Neighboring runs in the same style merge into one span. A `\n` stays in the text, where a label starts a new line. Broken surrogate pairs become `U+FFFD`, as vanilla draws them.

Text from `MinecraftText.of` is converted again each time the control lays out, so a translatable component follows the player's language while the screen is open. Do not change a component after wrapping it. In the vanilla theme, labels draw in opaque white with a shadow, which `MinecraftText.DEFAULT_STYLE` also is.

`MinecraftText.spans(text)` and `MinecraftText.spans(text, base)` convert once to a list of `TextSpan`s, over `DEFAULT_STYLE` or the base you pass, for custom components that draw spans themselves. They resolve translations in the language of the moment of the call, and empty text gives an empty list.

## Translations

The game's language becomes Strata's translator when the first Strata screen opens, so `Text.translatable("examplemod.shop.title")` reads from your mod's `lang` files and resource packs can override it. When the player picks another language or resources reload, open screens lay their text out again.

Strata's own button keys, `strata.button.ok`, `strata.button.cancel`, `strata.button.yes` and `strata.button.no`, read vanilla's `gui.ok`, `gui.cancel`, `gui.yes` and `gui.no` when no `lang` file has the Strata key, so message boxes are in the player's language with no translation of their own. Vanilla has no keys to stand in for the others, so these show Strata's English until a `lang` file gives them:

- `strata.password.show` and `strata.password.hide`, the tooltips of a password box's eye
- `strata.date.placeholder`, `strata.time.placeholder` and `strata.datetime.placeholder`
- `strata.calendar.title` and `strata.calendar.decade`

[Translation keys](../../reference/translation-keys.md) lists every key with its English.

Dates and times follow the player's language too. The selected language's code becomes a Java locale: the part before the underscore is the language and the part after it the region, so `de_de` is German in Germany and `pt_br` Portuguese in Brazil. A region that is not a known country code is dropped. A code with no region, whose language the JDK has no data for, is cut to its first two letters when those name a language the JDK has, so `enws` reads as English. Any other language the JDK lacks, such as `lol_us`, reads as English. Month and weekday names, the first day of the week, the order of a date's parts and the 12 or 24 hour clock then come from the JDK's data for that locale. A `lang` file can replace any of them with the `strata.date.*` and `strata.time.*` keys in [Dates and times](../../reference/translation-keys.md#dates-and-times). Vanilla has none of these keys, so resource packs made for vanilla do not change them.

Today, as a calendar marks it, comes from the computer's clock in its time zone. The adapter does not read the world's time.

## Items

When the first Strata screen opens, Strata-Minecraft registers appearances for three vanilla types. A list of item stacks then needs no cell factory, and `factory.image(stack)` shows a stack with its tooltip:

| Type | Icon | Name | Tooltip |
|---|---|---|---|
| `ItemStack` | The item visual with every decoration | `getHoverName()`: the custom name, or else the item's name. Vanilla adds the rarity color only in the tooltip, so the name has none. | `MinecraftTooltips.item(stack)`, asked anew on each hover, so it follows F3+H |
| `GameProfile` | The player's face with the hat layer | The profile's name | None |
| `PlayerSkin` | The skin's face with the hat layer | `String.valueOf`, the default | None |

```java
// Each row shows the item, its name and, on hover, vanilla's tooltip
final ListView<ItemStack> stock = factory.<ItemStack>listView().setItems(stacks);
```

These appearances sit beneath anything a mod registers for the same type, so a mod that registers its own `ItemStack` appearance, such as one that leaves out the count, replaces them everywhere. Code that asks `Strata.appearances()` about these types before the first Strata screen has opened finds nothing for them.

`MinecraftVisuals.item(stack)` makes a [visual](../../guides/appearances.md#visuals-and-icons) of a stack, which an `Image` shows and a custom component draws with `Painter.visual`. `MinecraftTooltips.item(stack)` builds the tooltip vanilla shows for the stack in a slot:

```java
// The item with its count and damage bar, and vanilla's tooltip lines on hover
final Image icon = factory.image(MinecraftVisuals.item(stack))
        .setTooltip(MinecraftTooltips.item(stack));

// Twice the size, with no decorations
final Image large = factory.image(MinecraftVisuals.item(stack, MinecraftVisuals.ItemDecorations.NONE))
        .setImageSize(32f, 32f);
```

An item visual is 16 by 16 UI units, `MinecraftVisuals.ITEM_SIZE`, the size of an item in a slot, and stretches to the rectangle it is drawn in. Its position rounds to the nearest GUI pixel. It draws through vanilla's GUI item rendering with the local player and level, so items whose look depends on them, such as compasses and clocks, look as they do in the inventory. Enchanted items draw with their glint.

| Decorations | What draws over the item |
|---|---|
| `ItemDecorations.ALL`, the default | Vanilla's own slot decorations: the damage bar on damaged items, the cooldown overlay while the player has the item on cooldown, and the count when it is not 1 |
| `ItemDecorations.NONE` | Nothing |
| `new ItemDecorations(count, damageBar)` with one of them false | The other one, at vanilla's position and in vanilla's colors, without the cooldown overlay |

The stack is copied when the visual is made, so later changes to your stack do not show; make a new visual. Two item visuals are equal when vanilla's `ItemStack.matches` says their stacks are, meaning the same item, count and components, and their decorations are equal. Items cannot be tinted, so `Image.setTint` has no effect on an item visual except that a tint with an alpha of 0 hides it.

### Item tooltips

`MinecraftTooltips.item(stack)` asks vanilla's `Screen.getTooltipFromItem` for the stack's lines, the same ones a slot shows, built for the local player and level. It reads the advanced tooltips setting (F3+H) when it is called, so while that is on the lines include vanilla's extra ones, such as the item id. A stack that hides its tooltip gives a tooltip with no lines. `MinecraftTooltips.item(minecraft, stack)` does the same for a given `Minecraft` instance.

The lines become a Strata `Tooltip.lines` tooltip, one line per component, converted as `MinecraftText.of` converts text. The base of every line is the theme's `tooltip.text` style, which in the vanilla theme is opaque white with a shadow, so uncolored text is white and every run has a shadow, as in vanilla. Strata draws the tooltip with the theme's tooltip keys. The vanilla theme uses vanilla's `tooltip/background` and `tooltip/frame` sprites, which resource packs restyle. A theme without `tooltip.background` gets a near-black box with a purple edge.

A tooltip is built once. Build it again when the stack changes, or when the player toggles F3+H and you want the new lines. An `ItemDisplay`, below, and the `ItemStack` appearance do this for you.

`MinecraftTooltips.lines(components)` turns any list of vanilla text into a tooltip the same way, for tooltips of your own. `MinecraftTooltips.spans(text)` converts one line to spans over white with a shadow, `MinecraftTooltips.DEFAULT_COLOR`. For labels, use `MinecraftText`.

These tooltips hold text only. Tooltip images are not drawn, so a bundle shows its text lines without the grid of its contents, and the extra gap vanilla leaves below an item's name is missing. Slot tooltips in a [container screen](containers.md) are vanilla's own and do include tooltip images.

### Item displays

`ItemDisplay`, in `org.inspirenxe.strata.minecraft.control`, is a component that shows one stack, like vanilla's `ItemDisplayWidget`. It keeps the item and its tooltip in step, which an `Image` with an item visual leaves to you:

```java
// Vanilla's tooltip lines, made again each time the pointer arrives, with a price line added
final ItemDisplay display = new ItemDisplay(factory, stack)
        .setTooltipFunction(shown -> {
            final List<Component> lines = new ArrayList<>(Screen.getTooltipFromItem(Minecraft.getInstance(), shown));
            lines.add(Component.literal(price + " coins").withStyle(ChatFormatting.GOLD));
            return MinecraftTooltips.lines(lines);
        });

// Later, when the listing changes
display.setStack(newStack);
```

- `new ItemDisplay(factory)` makes one with no stack, and `new ItemDisplay(factory, stack)` one with a stack and every decoration.
- Its desired size is 16 by 16. In larger bounds the item draws as the largest square that fits, centered, so it keeps its shape.
- `setStack` copies the stack, and `stack()` returns a copy. An empty stack, such as `ItemStack.EMPTY`, draws nothing and has no tooltip.
- The tooltip is made each time the pointer enters the display, and again when the stack or the tooltip function changes while the pointer is over it, so it follows F3+H. The function gets a copy of the stack, never an empty one, and may return null for no tooltip. A tooltip with no lines counts as none.
- The default tooltip function asks the stack's appearance, as a list row does, so an `ItemStack` appearance a mod registers changes both. Strata-Minecraft's own appearance gives `MinecraftTooltips.item`.
- `setTooltipFunction(null)` removes the tooltip the display set and leaves one you set with `setTooltip` alone, so you manage the tooltip yourself from then on.
- `setDecorations` picks the count and damage bar, as for item visuals. `setTint` with an alpha of 0 hides the item, and other tints have no effect.
- It is not focusable, reads no theme keys and fires no events. Clicks go through to whatever holds it, such as a list row. For slots the player can click, use a [container screen](containers.md).

## Player faces

`MinecraftVisuals.playerFace` makes a visual of a player's head from their skin: the face, with the hat layer over it unless you pass `false` as the second argument, as vanilla's player list and social screen draw it. Show it with an `Image`:

```java
// A player in the level carries their skin already
final Image face = factory.image(MinecraftVisuals.playerFace(player.getGameProfile()))
        .setImageSize(16f, 16f);

// Only a name: the default skin shows until the real one has loaded
final Image seller = factory.image(MinecraftVisuals.playerFace(ResolvableProfile.createUnresolved("Ana")))
        .setImageSize(12f, 12f);
```

| Made from | Skin drawn |
|---|---|
| `GameProfile`, such as `player.getGameProfile()` | The profile's skin, through vanilla's player skin cache |
| `ResolvableProfile`, such as `ResolvableProfile.createUnresolved(name)` | Looked up in the background, in the server's player list first and then through Mojang's services. Until it arrives, the built-in skin vanilla picks for the profile's id shows. |
| `PlayerSkin`, such as one of `DefaultPlayerSkin`'s | That skin's texture |

For a profile, the canvas asks vanilla's skin cache for the skin every time it draws, so the face changes to the real skin by itself once it has loaded, with no new visual. A face is 8 by 8 UI units, `MinecraftVisuals.PLAYER_FACE_SIZE`, one skin pixel each, and stretches over the rectangle it is drawn in, snapped to whole GUI pixels. Set the image size for a larger face. Faces take the image's tint.

An `Avatar` shows faces with no more code. `factory.avatar(profile)` draws the face of a `GameProfile` over the avatar's square, since the appearance Strata-Minecraft registers for profiles gives the face as the icon and the player's name as the name.
