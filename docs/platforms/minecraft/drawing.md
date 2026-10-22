# Drawing and themes on Minecraft

This page covers how Strata-Minecraft draws a screen in game, where sprites come from, how nine-slices map onto vanilla's sprite metadata, the vanilla theme that makes Strata screens look like the game, and how resource packs and mods restyle it.

## Units and drawing

One UI unit is one GUI pixel: screen pixels divided by the player's GUI scale. A 200-unit panel is 200 GUI pixels wide and grows with the GUI scale setting, as vanilla's widgets do. The host is resized whenever vanilla lays the screen out again, such as when the window is resized. A screen at GUI scale 4 has half as many units across as at GUI scale 2, so use anchors, stretching and weighted grids rather than fixed positions.

The adapter tells Strata the GUI scale as the density, so `Environment.pixelsPerUnit()` and `Painter.pixelsPerUnit()` are 2 at GUI scale 2, and a line `1 / pixelsPerUnit()` units wide is one screen pixel wide. The game asks SDL for a framebuffer of high pixel density, so on a Retina or other scaled display the framebuffer already holds the display's own pixels and the GUI scale counts them. The window's content scale is not multiplied in again. A change of GUI scale or window size lays the screen out again.

`MinecraftCanvas` records Strata's drawing into vanilla's `GuiGraphicsExtractor`, which renders it later in the frame. `StrataScreen` makes the canvas and points it at each frame's graphics, so a mod never touches it.

| Strata draws | On Minecraft |
|---|---|
| Fills and gradients | `fill` and `fillGradient`. Each edge of the rectangle moves down to the whole GUI pixel it lies in, as vanilla's integer coordinates would put it, so a rectangle of whole size keeps that size wherever it sits. A fill less than one GUI pixel across or down, such as a hairline or a row of a shape, is drawn in screen pixels instead when the GUI scale is above 1, so it neither vanishes nor swells to a whole GUI pixel. A fill with alpha 0 draws nothing. |
| Sprites | `blitSprite` with the `GUI_TEXTURED` pipeline, from the `minecraft:gui` sprite atlas, with edges moved down to whole pixels as for fills. The paint's tint is passed as vanilla's sprite color. The sprite's `.png.mcmeta` decides whether it stretches, tiles or nine-slices. |
| Nine-slices | The same as sprites. Vanilla applies the scaling in the sprite's `.png.mcmeta`, and the paint's border is ignored. |
| Sized nine-slices and tiled sprites | The same as sprites. The platform reports `Capabilities.slicesSprites`, so core hands these paints to the canvas whole instead of drawing them from pieces, and the `.png.mcmeta` decides. |
| Text | A chat component in the screen's font, at the position rounded to the nearest pixel, with vanilla's bold, italic, underline, strikethrough and shadow. Empty text and text with alpha 0 draw nothing. See [Text and items](text-and-items.md). |
| Clips | Vanilla's scissor stack, which intersects nested clips and follows the pose. The edges move down to whole pixels. |
| Translations and scales | The pose matrix, pushed and popped |
| Layers, such as overlays and the tooltip | A new vanilla stratum, so what draws after it covers what drew before |
| Shapes (lines, rounded rectangles, ovals, arcs, gradients at an angle, shadows) | Vanilla's GUI renderer draws only rectangles and sprites, so the platform reports `Capabilities.drawsShapes` as false and Strata's core draws each shape from fills, in rows one screen pixel high, without anti-aliasing. Round clips stay rectangular. |
| Opacity, such as a fading card or toast | The alpha of every fill, gradient, text color, sprite tint and player face is multiplied by it. Item visuals do not fade: the game's item renderer takes no color, so an item stays opaque until its component stops drawing it. |
| Cursors | `requestCursor` with vanilla's cursor types for the arrow, I-beam, hand, crosshair, the horizontal, vertical and four-way resize arrows and the "not allowed" sign, and SDL's own system cursors, which vanilla does not name, for the diagonal resize arrows, the busy cursor and the arrow with a busy sign. SDL has no grab, grabbing, copy, no-drop or help cursor, so `showsCursor` answers false for them and the host asks for their fallback: the hand for the grab cursors, the "not allowed" sign for no-drop, and the arrow for the rest. |
| Pixel buffers (`Image.setPixels`) | A `DynamicTexture` per buffer, drawn with `blit` stretched over the rectangle, sampled nearest so pixel art stays sharp, and tinted. The pixels are copied to the texture again only when the buffer's version moved, after `changed()`. A texture is released once its buffer has gone undrawn for 600 frames or the screen closes. |
| Item and player face visuals | Vanilla's GUI item rendering, and the face from the player's skin. See [Text and items](text-and-items.md). |
| Other visuals | A magenta box, `MinecraftCanvas.MISSING_VISUAL_COLOR` (`0xFFFF00FF`) |

A sprite the gui atlas does not have draws vanilla's missing-texture pattern.

Layout itself stays in floats, and only drawing snaps. A panel at x = 10.4 with width 20.2 has its edges at 10.4 and 30.6, which move down to 10 and 30, so it covers the 20 pixels from 10 to 29. In Swing the same panel covers its exact fractional span. Design with whole numbers and the two look alike. [Platform differences](../differences.md#units-and-rounding) has more on this.

## Your own sprites

Put images in your mod's resources at `assets/<modid>/textures/gui/sprites/<path>.png` and name them `SpriteId.of("<modid>", "<path>")`. Vanilla's gui atlas collects every image under `textures/gui/sprites` from every namespace in every loaded pack, mods' resources included, so no registration is needed.

```java
// Reads assets/examplemod/textures/gui/sprites/widget/fancy_button.png from the gui atlas
SpriteId fancy = SpriteId.of("examplemod", "widget/fancy_button");

// The vanilla look as packs restyle it, with this mod's button on top
ThemeRef theme = ThemeRef.platform()
        .override(Button.STYLE_NORMAL, Paint.nineSlice(fancy, Insets.of(4f)));
Strata.screens().open(new ShopScreen(), theme);
```

Add a `.png.mcmeta` beside an image that should slice or tile, as the next section describes. Resource packs can replace your images like any others. New or changed images need a resource reload (F3+T) in a running game.

The [Swing adapter](../swing.md#sprites) reads `assets/<namespace>/sprites/<path>.png` by default. To preview the same images in Swing, give `SwingSprites` a `SpriteResolver` for Minecraft's layout, and pass the `SwingSprites` to `StrataPanel` or `SwingSnapshot.render`:

```java
SwingSprites sprites = new SwingSprites(App.class.getClassLoader(),
        id -> "assets/" + id.namespace() + "/textures/gui/sprites/" + id.path() + ".png");
```

Swing reads no `.png.mcmeta`, so it slices by the paint, which the next section shows how to match.

## Nine-slices

Vanilla stores the scaling of a gui sprite in its `.png.mcmeta` file and applies it every time it draws the sprite. Strata-Minecraft leaves the slicing to vanilla, so on Minecraft the `.png.mcmeta` decides and the paint's border, size and tiling are ignored. Other adapters, Swing among them, slice by the paint. Keep the two in agreement so every engine draws the same.

This is the format, as vanilla 26.3 reads it:

```json
{
  "gui": {
    "scaling": {
      "type": "nine_slice",
      "width": 200,
      "height": 20,
      "border": 4
    }
  }
}
```

`type` is `stretch`, `tile` or `nine_slice`. `width` and `height` are the size, in GUI pixels, that the whole image stands for, whatever its pixel size. A `nine_slice` `border` is either one positive number for all four sides or an object with `left`, `top`, `right` and `bottom`. The left and right borders together must be less than `width`, and the top and bottom less than `height`. Vanilla tiles the edges and the center of a nine-slice; `"stretch_inner": true` stretches them instead. A `tile` sprite has a `width` and `height` and no border.

An image without a `.png.mcmeta` stretches over the rectangle like a plain sprite, so a nine-slice paint for an image with no `.png.mcmeta` looks the same as a sprite paint in game.

Each kind of scaling has a paint that draws the same everywhere:

| `.png.mcmeta` scaling | Paint | In a theme file |
|---|---|---|
| None, or `"type": "stretch"` | `Paint.sprite(id)` | `{ "sprite": "ns:path" }` |
| `"type": "tile"` with `width` and `height` | `Paint.tiled(id, new Size(width, height))` | `{ "tiled": "ns:path", "size": [width, height] }` |
| `"type": "nine_slice"` | `Paint.tiledNineSlice(id, new Size(width, height), border)` | `{ "nine_slice": "ns:path", "size": [width, height], "border": ..., "tiled": true }` |
| `"type": "nine_slice"` with `"stretch_inner": true` | `Paint.nineSlice(id, new Size(width, height), border)` | `{ "nine_slice": "ns:path", "size": [width, height], "border": ... }` |

For the `.png.mcmeta` above:

```java
// { "type": "nine_slice", "width": 200, "height": 20, "border": 4 }: edges and center tiled
Paint button = Paint.tiledNineSlice(SpriteId.of("examplemod", "button"), new Size(200, 20), Insets.of(4f));
// { "type": "tile", "width": 16, "height": 16 }
Paint background = Paint.tiled(SpriteId.of("examplemod", "background"), new Size(16, 16));
```

With a size, the border is in units of that size, as it is in the `.png.mcmeta`, so a pack's higher-resolution image draws the same borders. `Paint.nineSlice(id, border)` without a size takes the border in the image's own pixels and stretches. The [theming guide](../../guides/theming.md#nine-slices) covers nine-slice paints in general.

## The vanilla theme

`MinecraftThemeSource.VANILLA` (`minecraft:vanilla`) is vanilla's look as a theme. It is the platform's default theme: `MinecraftThemeSource.defaultTheme()` returns it, so `ThemeRef.platform()` resolves to it, and `Strata.screens().open(screen)`, which uses that ref, opens screens in it.

The theme is written as a theme file, `strata/minecraft/themes/vanilla.json` in Strata-Minecraft's jar. `MinecraftThemeSource` reads it from the class path and returns it as the theme's [base file](../../guides/theming.md#base-files), with the layer `vanilla` (`MinecraftThemeSource.VANILLA_LAYER`), the vanilla pack's id. The file sits outside `assets/`, so the resource manager never reads it as a pack file. It names `strata:flat` as its parent and sets every paint, text style and insets key that `Themes.flat()` sets, so no built-in control takes those keys from the flat theme. The parent only fills in keys that controls gain later, until the file sets them.

`MinecraftThemeSource.register(id, theme)` gives another id a base theme built in code. It throws `IllegalArgumentException` for `minecraft:vanilla`, whose base is the file; change its keys with `Strata.themes().override` instead.

The theme names vanilla's own gui sprites, so it follows any resource pack that replaces them:

| Control | Sprites |
|---|---|
| Button, combo box, expander header, table header, picker drop button, navigation toggle | `widget/button`, `widget/button_highlighted` when hovered or pressed, `widget/button_disabled` |
| Text box, password box | `widget/text_field`, `widget/text_field_highlighted` when focused. A disabled box uses `widget/text_field`. |
| Checkbox, radio button | `widget/checkbox` and `widget/checkbox_selected`, in every state. The check mark is part of the sprites. |
| Slider, toggle switch | `widget/slider` for the track in every state, `widget/slider_handle` for the thumb or knob, `widget/slider_handle_highlighted` when hovered |
| Scroll bar | `widget/scroller` for the thumb, `widget/scroller_background` for the track |
| Selected list row, menu item, menu bar item, navigation item and calendar day | `widget/text_field_highlighted`, a black box with a white one-pixel ring |
| Combo box list, menu, flyout, picker popup | `widget/text_field` |
| Tab bar | `widget/tab`, `widget/tab_highlighted`, `widget/tab_selected`, `widget/tab_selected_highlighted` |
| Window and message box | `popup/background` |
| Tooltip | `tooltip/background` with `tooltip/frame` over it |
| Toast | `toast/system` |
| Info bar | `toast/now_playing`, with an accent in a chat color for each severity |
| Grid view cell, avatar | `container/slot` |
| Container slots | `container/slot`, and `container/slot_highlight_back` and `container/slot_highlight_front` around the hovered slot in Strata mode. These are Strata-Minecraft's own `slot.*` keys; see [Containers](containers.md). |

The nine-slice borders in the file are the borders each sprite's `.png.mcmeta` declares in 26.3. Minecraft reads the `.png.mcmeta`; the borders are there for other engines, such as Swing. Vanilla's checkbox sprites and `container/slot` have no `.png.mcmeta`, so they stretch over their rectangle.

Vanilla's gui sprites have no pressed button and no disabled checkbox, slider, tab or text field, so those keys reuse the nearest sprite. Progress bars, separators, the color picker, the window's resize grip and the slim scroll bar are plain colors. Vanilla's buttons, checkboxes, cycle buttons and tabs act as the mouse button goes down, so `MinecraftPlatform.conventions()` turns on `Conventions.activateOnPress` and Strata's controls do the same, and `button.pressed` is the highlighted sprite.

Focus shows through the hovered sprites on buttons and toggle switches, whose `button.focus` and `toggle.focus` are `none`. Hovering a checkbox or radio button changes nothing, and focus draws a white ring (`checkbox.focus`, `radio.focus`). A mixed checkbox draws a square in `0xFFE0E0E0` over the empty box, `0xFFA0A0A0` when disabled.

The theme sets none of the password box's own keys. `passwordbox.mask` falls back to `textbox.text`, so a pack that recolors text boxes recolors the mask with them. The reveal eye falls back to core's drawn eye, in the text box's colors: the placeholder's `0xFF555555`, `0xFFE0E0E0` when hovered or while the text shows, and `0xFF707070` while the user cannot reveal the text. Vanilla 26.3 has no eye among its gui sprites. A pack or a mod that has one can set `passwordbox.reveal`, `passwordbox.reveal.hovered`, `passwordbox.reveal.active` and `passwordbox.reveal.disabled` in a theme file. The theme also sets no `avatar.placeholder` and no `infobar.icon.*` keys.

Looks for the controls and features Strata has gained since the first theme file:

- A toggle button that is on draws the button sprite tinted darker, so it looks pressed in, and the highlighted sprite lightly tinted while hovered (`button.selected`). Vanilla has no toggle button. A default button looks like any other (`button.default` is the normal sprite), since vanilla's screens mark none.
- Slider ticks are light gray marks over the bottom of the dark track, with darker gray minor ticks (`slider.tick`, `slider.tick.minor`). Vanilla's slider has no ticks.
- `button.focus` is `none` here, so the keys that fall back to it are set. The split pane's divider and the color picker get the white one-unit ring a focused checkbox has (`splitpane.focus`, `colorpicker.focus`). Vanilla has no keyboard cursor over slots, and a grid draws its focus paint over the whole cell, so the cursor's cell is washed with `0x40FFFFFF` (`grid.focus`), twice the white vanilla lays over a hovered slot.
- A list's drop line during a drag is white, like vanilla's selection (`list.drop`).
- An invalid field keeps its vanilla face and draws a one-unit outline inside its edge, red for an error and gold for a warning, the colors of chat formatting (`validation.error`, `validation.warning`). The message shows in the tooltip. Every vanilla widget the fields use has a one-pixel ring at its edge, so the outline takes its place. The theme sets no `textbox.invalid`, `slider.invalid`, `checkbox.invalid` or `combobox.invalid`: a face would replace the focused look as the outline does, and a check box's mark is part of its sprite, so an invalid face would hide it.
- Table headers are buttons, 20 tall with their titles 6 down (`table.header.height`, `table.header.padding`). A table scrolls sideways with `setHorizontalPolicy(AUTO)`, and a grid taller than its room scrolls with a bar of its own. Both use the `scroll.*` keys, so they show vanilla's scroller. Vanilla's creative inventory draws its own scroller sprite, `container/creative_inventory/scroller`, which a grid screen can set as `scroll.thumb` in a theme override.
- Motion stays off. `toggle.animation`, `expander.animation`, `nav.animation`, `scroll.smooth`, `toast.fade` and `progress.animation` are 0, since vanilla has no switch, expander or navigation pane, its lists and text fields do not glide, and its progress bars jump. Vanilla's toasts slide in from the right, which `toast.fade` does not model, so they appear at once.
- The theme leaves these unset on purpose: the invalid faces above; the scroll bar arrows (`scroll.arrow` and its direction forms), since vanilla's scroll bars have none and a bar shown with arrows draws triangles in the scroller's paint; the message box icons (`messagebox.icon.*`), which fall back to `infobar.icon.*`, since vanilla's popups have no icons; the type-ahead pause (`list.typeahead.timeout`), which keeps its default of 1000 milliseconds; and the avatar radius (`avatar.radius`), because avatars sit on the square slot sprite. Rounded color paints work in theme files, drawn with stepped corners, but vanilla's look is all sprites.

Some values copy vanilla's metrics where Strata has a key for them:

- Captions too wide for a button, tab or slider are cut at `button.clip`, `tab.clip` or the slider's padding, and slide back and forth at 500 milliseconds per unit of overflow, each sweep at least 3000 milliseconds.
- Buttons and text boxes are a line plus 11 tall with their text 6 down (`button.padding`, `textbox.padding`), so 20 tall with vanilla's 9-unit font.
- Text boxes draw the caret in the text color, blinking every 300 milliseconds, as an underscore after the text when it is at the end, and hide the placeholder while focused. The selection is white with the text in `0xFF1F1FFF`.
- Lists draw `0x70000000` behind their rows, center a column of rows 220 wide with the scroll bar 8 to its right, scroll half a row per wheel notch, and draw two one-unit lines above and below them, `0xBF000000` next to the list and `0x33FFFFFF` beyond it. A selected row without focus is black with a `0xFF808080` ring.
- Tooltips show at once, with the text 3 inside the box, the sprites reaching 9 past it, and the box just below the component.
- Message boxes wrap the message at 214, center it, and share the width among the buttons, each at most 150 wide and 6 apart. They have no close button; Escape still answers.
- Text toasts have a yellow title and white text without a shadow, are at least 190 wide, and sit flush in the top-right corner.

[Theme keys](../../reference/theme-keys.md) says what each key does and what it falls back to.

### Every key the theme sets

Borders and insets with four numbers are in the order left, top, right and bottom. Metrics are in the units their keys state in [Theme keys](../../reference/theme-keys.md).

#### Paints

| Key | Value |
|---|---|
| `button.normal` | nine-slice `minecraft:widget/button`, border 3 |
| `button.hovered` | nine-slice `minecraft:widget/button_highlighted`, border 3 |
| `button.pressed` | nine-slice `minecraft:widget/button_highlighted`, border 3 |
| `button.disabled` | nine-slice `minecraft:widget/button_disabled`, border 1 |
| `button.focus` | `none` |
| `button.default` | nine-slice `minecraft:widget/button`, border 3 |
| `button.selected` | nine-slice `minecraft:widget/button`, border 3, tint `0xFFA0A0A0` |
| `button.selected.hovered` | nine-slice `minecraft:widget/button_highlighted`, border 3, tint `0xFFC0C0C0` |
| `button.selected.disabled` | nine-slice `minecraft:widget/button_disabled`, border 1 |
| `checkbox.normal` | sprite `minecraft:widget/checkbox` |
| `checkbox.hovered` | sprite `minecraft:widget/checkbox` |
| `checkbox.checked` | sprite `minecraft:widget/checkbox_selected` |
| `checkbox.checked.hovered` | sprite `minecraft:widget/checkbox_selected` |
| `checkbox.disabled` | sprite `minecraft:widget/checkbox` |
| `checkbox.checked.disabled` | sprite `minecraft:widget/checkbox_selected` |
| `checkbox.mark` | `none` |
| `checkbox.mark.disabled` | `none` |
| `checkbox.mark.mixed` | solid `0xFFE0E0E0` |
| `checkbox.mark.mixed.disabled` | solid `0xFFA0A0A0` |
| `checkbox.focus` | solid `0xFFFFFFFF` |
| `slider.track` | nine-slice `minecraft:widget/slider`, border 1 |
| `slider.track.focused` | nine-slice `minecraft:widget/slider`, border 1 |
| `slider.track.disabled` | nine-slice `minecraft:widget/slider`, border 1 |
| `slider.thumb` | nine-slice `minecraft:widget/slider_handle`, border 2, 2, 2 and 3 |
| `slider.thumb.hovered` | nine-slice `minecraft:widget/slider_handle_highlighted`, border 2, 2, 2 and 3 |
| `slider.thumb.disabled` | nine-slice `minecraft:widget/slider_handle`, border 2, 2, 2 and 3 |
| `slider.tick` | solid `0xFFA0A0A0` |
| `slider.tick.minor` | solid `0xFF6F6F6F` |
| `textbox.normal` | nine-slice `minecraft:widget/text_field`, border 1 |
| `textbox.focused` | nine-slice `minecraft:widget/text_field_highlighted`, border 1 |
| `textbox.disabled` | nine-slice `minecraft:widget/text_field`, border 1 |
| `textbox.selection` | solid `0xFFFFFFFF` |
| `textbox.caret` | solid `0xFFE0E0E0` |
| `scroll.thumb` | nine-slice `minecraft:widget/scroller`, border 1 |
| `scroll.thumb.hovered` | nine-slice `minecraft:widget/scroller`, border 1 |
| `scroll.thumb.disabled` | nine-slice `minecraft:widget/scroller`, border 1 |
| `scroll.track` | nine-slice `minecraft:widget/scroller_background`, border 1 |
| `list.background` | solid `0x70000000` |
| `list.row` | `none` |
| `list.row.hovered` | `none` |
| `list.row.selected` | nine-slice `minecraft:widget/text_field_highlighted`, border 1 |
| `list.row.selected.unfocused` | solid `0xFF000000` |
| `list.row.ring.unfocused` | solid `0xFF808080` |
| `list.drop` | solid `0xFFFFFFFF` |
| `list.edge` | solid `0xBF000000` |
| `list.edge.outer` | solid `0x33FFFFFF` |
| `combobox.normal` | nine-slice `minecraft:widget/button`, border 3 |
| `combobox.hovered` | nine-slice `minecraft:widget/button_highlighted`, border 3 |
| `combobox.disabled` | nine-slice `minecraft:widget/button_disabled`, border 1 |
| `combobox.popup` | nine-slice `minecraft:widget/text_field`, border 1 |
| `window.background` | nine-slice `minecraft:popup/background`, border 6 |
| `window.title` | `none` |
| `window.title.inactive` | `none` |
| `tab.normal` | nine-slice `minecraft:widget/tab`, border 2, 2, 2 and 0 |
| `tab.hovered` | nine-slice `minecraft:widget/tab_highlighted`, border 2, 2, 2 and 0 |
| `tab.selected` | nine-slice `minecraft:widget/tab_selected`, border 2, 2, 2 and 0 |
| `tab.disabled` | nine-slice `minecraft:widget/tab`, border 2, 2, 2 and 0 |
| `tab.selected.disabled` | nine-slice `minecraft:widget/tab`, border 2, 2, 2 and 0 |
| `tab.selected.hovered` | nine-slice `minecraft:widget/tab_selected_highlighted`, border 2, 2, 2 and 0 |
| `tab.selected.fill` | solid `0x40000000` |
| `tab.underline` | solid `0xFFFFFFFF` |
| `tab.underline.disabled` | solid `0xFFA0A0A0` |
| `progress.track` | solid `0xFF000000` |
| `progress.fill` | solid `0xFF008200` |
| `progress.segment` | solid `0xFF008200` |
| `separator` | solid `0x33FFFFFF` |
| `separator.shadow` | solid `0xBF000000` |
| `radio.normal` | sprite `minecraft:widget/checkbox` |
| `radio.hovered` | sprite `minecraft:widget/checkbox` |
| `radio.selected` | sprite `minecraft:widget/checkbox_selected` |
| `radio.selected.hovered` | sprite `minecraft:widget/checkbox_selected` |
| `radio.disabled` | sprite `minecraft:widget/checkbox` |
| `radio.selected.disabled` | sprite `minecraft:widget/checkbox_selected` |
| `radio.mark` | `none` |
| `radio.mark.disabled` | `none` |
| `radio.focus` | solid `0xFFFFFFFF` |
| `toggle.track` | nine-slice `minecraft:widget/slider`, border 1 |
| `toggle.track.on` | nine-slice `minecraft:widget/slider`, border 1 |
| `toggle.track.disabled` | nine-slice `minecraft:widget/slider`, border 1 |
| `toggle.knob` | nine-slice `minecraft:widget/slider_handle`, border 2, 2, 2 and 3 |
| `toggle.knob.hovered` | nine-slice `minecraft:widget/slider_handle_highlighted`, border 2, 2, 2 and 3 |
| `toggle.knob.disabled` | nine-slice `minecraft:widget/slider_handle`, border 2, 2, 2 and 3 |
| `toggle.focus` | `none` |
| `table.header` | nine-slice `minecraft:widget/button`, border 3 |
| `table.header.hovered` | nine-slice `minecraft:widget/button_highlighted`, border 3 |
| `table.grid` | `none` |
| `tree.arrow` | solid `0xFFA0A0A0` |
| `menu.background` | nine-slice `minecraft:widget/text_field`, border 1 |
| `menu.item.hovered` | nine-slice `minecraft:widget/text_field_highlighted`, border 1 |
| `menu.separator` | solid `0x33FFFFFF` |
| `menubar.background` | `none` |
| `menubar.item.hovered` | nine-slice `minecraft:widget/text_field_highlighted`, border 1 |
| `expander.header` | nine-slice `minecraft:widget/button`, border 3 |
| `expander.header.hovered` | nine-slice `minecraft:widget/button_highlighted`, border 3 |
| `split.divider` | solid `0x33FFFFFF` |
| `split.divider.hovered` | solid `0x80FFFFFF` |
| `splitpane.focus` | solid `0xFFFFFFFF` |
| `grid.cell` | sprite `minecraft:container/slot` |
| `grid.cell.hovered` | solid `0x20FFFFFF` |
| `grid.cell.hovered.back` | solid `0x60FFFFFF` |
| `grid.cell.selected` | solid `0x20FFFFFF` |
| `grid.focus` | solid `0x40FFFFFF` |
| `scroll.slim.track` | `none` |
| `scroll.slim.thumb` | solid `0xFF3333AA` |
| `scroll.slim.thumb.hovered` | solid `0xFFCCCCCC` |
| `scroll.slim.thumb.disabled` | solid `0xFF555555` |
| `busy.dot` | solid `0xFF404040` |
| `busy.dot.active` | solid `0xFF808080` |
| `textbox.suggestions` | solid `0xD0000000` |
| `colorpicker.frame` | solid `0xFFA0A0A0` |
| `colorpicker.marker` | solid `0xFFFFFFFF` |
| `colorpicker.marker.outline` | solid `0xFF000000` |
| `colorpicker.disabled` | solid `0xA0000000` |
| `colorpicker.focus` | solid `0xFFFFFFFF` |
| `picker.button` | nine-slice `minecraft:widget/button`, border 3 |
| `picker.button.hovered` | nine-slice `minecraft:widget/button_highlighted`, border 3 |
| `picker.button.disabled` | nine-slice `minecraft:widget/button_disabled`, border 1 |
| `picker.popup` | nine-slice `minecraft:widget/text_field`, border 1 |
| `dropdown.arrow` | solid `0xFFFFFFFF` |
| `flyout.background` | nine-slice `minecraft:widget/text_field`, border 1 |
| `calendar.day.selected` | nine-slice `minecraft:widget/text_field_highlighted`, border 1 |
| `calendar.day.hovered` | nine-slice `minecraft:widget/text_field`, border 1 |
| `calendar.focus` | solid `0xFFFFFFFF` |
| `avatar.background` | sprite `minecraft:container/slot` |
| `badge.background` | `none` |
| `infobar.background` | nine-slice `minecraft:toast/now_playing`, border 4 |
| `infobar.accent.info` | solid `0xFF5555FF` |
| `infobar.accent.success` | solid `0xFF55FF55` |
| `infobar.accent.warning` | solid `0xFFFFAA00` |
| `infobar.accent.error` | solid `0xFFFF5555` |
| `nav.pane` | solid `0x70000000` |
| `nav.pane.overlay` | solid `0xF0101010` |
| `nav.item.selected` | nine-slice `minecraft:widget/text_field_highlighted`, border 1 |
| `nav.indicator` | `none` |
| `nav.toggle` | nine-slice `minecraft:widget/button`, border 3 |
| `nav.toggle.hovered` | nine-slice `minecraft:widget/button_highlighted`, border 3 |
| `nav.focus` | solid `0xFF808080` |
| `nav.separator` | solid `0x33FFFFFF` |
| `window.grip` | solid `0xFFA0A0A0` |
| `window.grip.hovered` | solid `0xFFFFFFFF` |
| `overlay.backdrop` | gradient `0xC0101010` to `0xD0101010` |
| `toast.background` | nine-slice `minecraft:toast/system`, border 17, 30, 4 and 4 |
| `slot.background` | sprite `minecraft:container/slot` |
| `slot.highlight_back` | sprite `minecraft:container/slot_highlight_back` |
| `slot.highlight_front` | sprite `minecraft:container/slot_highlight_front` |
| `tooltip.background` | nine-slice `minecraft:tooltip/background`, border 9 |
| `tooltip.frame` | nine-slice `minecraft:tooltip/frame`, border 10 |
| `validation.error` | solid `0xFFFF5555` |
| `validation.warning` | solid `0xFFFFAA00` |

#### Text styles

| Key | Value |
|---|---|
| `button.text` | `0xFFFFFFFF` with shadow |
| `button.text.disabled` | `0xFFA0A0A0` with shadow |
| `button.link.text` | `0xFFFFFFFF` with shadow |
| `button.link.text.hovered` | `0xFFFFFF55` with shadow, not underlined |
| `button.link.text.disabled` | `0xFFA0A0A0` with shadow |
| `label.text` | `0xFFFFFFFF` with shadow |
| `checkbox.text` | `0xFFFFFFFF` with shadow |
| `checkbox.text.disabled` | `0xFFFFFFFF` with shadow |
| `slider.text` | `0xFFFFFFFF` with shadow |
| `slider.text.disabled` | `0xFFA0A0A0` with shadow |
| `textbox.text` | `0xFFE0E0E0` with shadow |
| `textbox.text.disabled` | `0xFF707070` with shadow |
| `textbox.text.readonly` | `0xFF707070` with shadow |
| `textbox.text.selected` | `0xFF1F1FFF` without shadow |
| `textbox.placeholder` | `0xFF555555` with shadow |
| `list.placeholder` | `0xFFA0A0A0` with shadow |
| `combobox.text` | `0xFFFFFFFF` with shadow |
| `combobox.text.disabled` | `0xFFA0A0A0` with shadow |
| `calendar.header.text` | `0xFFFFFFFF` with shadow |
| `calendar.weekday.text` | `0xFFA0A0A0` with shadow |
| `calendar.day.text` | `0xFFE0E0E0` with shadow |
| `window.title.text` | `0xFFFFFFFF` with shadow, bold |
| `window.close` | `0xFFA0A0A0` with shadow |
| `window.close.hovered` | `0xFFFFFFFF` with shadow |
| `tab.text` | `0xFFFFFFFF` with shadow |
| `tab.text.selected` | `0xFFFFFFFF` with shadow |
| `tab.text.disabled` | `0xFFA0A0A0` with shadow |
| `progress.text` | `0xFFFFFFFF` with shadow |
| `radio.text` | `0xFFFFFFFF` with shadow |
| `radio.text.disabled` | `0xFFA0A0A0` with shadow |
| `toggle.text` | `0xFFFFFFFF` with shadow |
| `toggle.text.disabled` | `0xFFA0A0A0` with shadow |
| `table.header.text` | `0xFFFFFFFF` with shadow |
| `menu.text` | `0xFFFFFFFF` with shadow |
| `menu.text.disabled` | `0xFFA0A0A0` with shadow |
| `menu.shortcut` | `0xFFA0A0A0` with shadow |
| `menubar.text` | `0xFFFFFFFF` with shadow |
| `expander.text` | `0xFFFFFFFF` with shadow |
| `expander.text.disabled` | `0xFFA0A0A0` with shadow |
| `busy.text` | `0xFFFFFFFF` with shadow |
| `busy.dots.text` | `0xFF808080` with shadow |
| `textbox.suggestion.text` | `0xFFAAAAAA` with shadow |
| `toast.text` | `0xFFFFFFFF` without shadow |
| `toast.title` | `0xFFFFFF00` without shadow |
| `tooltip.text` | `0xFFFFFFFF` with shadow |
| `avatar.initials` | `0xFFFFFFFF` with shadow |
| `badge.text` | `0xFFFFFFFF` with shadow |
| `infobar.text` | `0xFFFFFFFF` without shadow |
| `nav.item.text` | `0xFFFFFFFF` with shadow |
| `nav.header.text` | `0xFFA0A0A0` with shadow |
| `nav.title.text` | `0xFFFFFFFF` with shadow |

#### Insets

| Key | Value |
|---|---|
| `tab.padding` | 8, 10, 8 and 5 |
| `tab.padding.selected` | 8, 8, 8 and 7 |
| `tab.clip` | 1, 0, 1 and 0 |
| `tab.selected.fill.insets` | 2, 2, 2 and 0 |
| `button.padding` | 8, 6, 8 and 5 |
| `button.clip` | 2, 0, 2 and 0 |
| `textbox.padding` | 4, 6, 4 and 5 |
| `combobox.padding` | 4, 6, 4 and 5 |
| `expander.header.padding` | 4, 6, 4 and 5 |
| `textarea.padding` | 4 on every side |
| `slider.padding` | 2, 6, 2 and 5 |
| `flyout.padding` | 4 on every side |
| `badge.padding` | 0 on every side |
| `infobar.padding` | 6, 6, 6 and 5 |
| `window.title.padding` | 18, 18, 18 and 12 |
| `window.padding` | 18, 0, 18 and 18 |
| `messagebox.padding` | 0 on every side |
| `tooltip.padding` | 3 on every side |
| `tooltip.outset` | 9 on every side |
| `grid.cell.highlight` | 1 on every side |
| `toast.padding` | 18, 7, 12 and 4 |
| `list.padding` | 0, 2, 0 and 2 |
| `table.header.padding` | 4, 6, 4 and 5 |

#### Metrics

| Key | Value |
|---|---|
| `button.text.scroll` | 500 |
| `button.text.scroll.minimum` | 3000 |
| `tab.text.scroll` | 500 |
| `tab.text.scroll.minimum` | 3000 |
| `slider.text.scroll` | 500 |
| `slider.text.scroll.minimum` | 3000 |
| `slider.key.travel` | 1 |
| `tooltip.delay` | 0 |
| `tooltip.offset.x` | 9 |
| `tooltip.offset.y` | -15 |
| `tooltip.below` | 1 |
| `textbox.caret.blink` | 300 |
| `textbox.caret.extend` | 1 |
| `textbox.caret.underscore` | 1 |
| `textbox.selection.extend` | 1 |
| `textbox.placeholder.hide` | 1 |
| `list.row.width` | 220 |
| `list.bar.gap` | 8 |
| `list.scroll.rows` | 0.5 |
| `messagebox.spacing` | 12 |
| `messagebox.message.width` | 214 |
| `messagebox.message.center` | 1 |
| `messagebox.button.width` | 0 |
| `messagebox.button.width.maximum` | 150 |
| `messagebox.button.gap` | 6 |
| `messagebox.close` | 0 |
| `toast.line.gap` | 3 |
| `toast.width.minimum` | 190 |
| `toast.margin` | 0 |
| `table.header.height` | 20 |
| `toggle.animation` | 0 |
| `expander.animation` | 0 |
| `nav.animation` | 0 |
| `scroll.smooth` | 0 |
| `toast.fade` | 0 |
| `progress.animation` | 0 |

## Themes from resource packs

A `StrataScreen` given a `ThemeRef` resolves it when it opens, again after every resource reload (F3+T, or a change of packs), and again whenever code adds or closes overrides with `Strata.themes()`. It keeps the ref's own overrides each time. A `StrataScreen` given a `Theme` keeps that theme, and packs never change it. Screens opened without a theme use `ThemeRef.platform()`, the vanilla theme.

Images need nothing else. The vanilla theme names sprites in the gui atlas, so a pack that replaces `widget/button.png` restyles Strata's buttons too, with the slicing from the pack's `.png.mcmeta`. For what images cannot carry, such as text colors, a different sprite for a key, or paints for keys vanilla has no sprite for, a pack adds a theme file at:

```text
assets/<namespace>/strata/themes/<path>.json
```

The file applies to the theme `<namespace>:<path>`. This one, at `assets/minecraft/strata/themes/vanilla.json`, restyles every screen that uses the vanilla theme:

```json
{
  "paints": {
    "list.row.selected": "#5A3A1A",
    "progress.fill": { "sprite": "fancypack:widget/progress" }
  },
  "text": {
    "button.text": { "color": "#FFD700" }
  }
}
```

A theme file's text entry changes only the fields it names, so `button.text` keeps its shadow. Metrics may be written as flags and durations: `"tooltip.below": true` and `"tooltip.delay": "0ms"` (or `"0.5s"`) read the same as `1` and `0`. The [theming guide](../../guides/theming.md#theme-files) has the whole file format. The vanilla theme has a base, so a pack file's `parent` is ignored for it: a pack can change its keys and cannot swap the whole theme for another.

A mod can name a theme of its own, such as `ThemeRef.of(ThemeId.of("examplemod", "shop"))`, and ship `assets/examplemod/strata/themes/shop.json` with `"parent": "minecraft:vanilla"` and its own keys. Its screens then follow every pack's changes to the vanilla theme for the keys the file leaves alone. A player's pack can ship the same file too. It goes below the mod's file, as the next section explains, so it can set the keys the mod's file leaves alone and cannot change the ones it sets.

To change a theme for every screen that uses it, or to define a theme on top of another, a mod uses `Strata.themes()`. Its keys stay in place across every reload until the subscription is closed:

```java
// Every Strata screen that shows the vanilla theme gets this mod's button, over every pack's theme files
SpriteId fancy = SpriteId.of("examplemod", "widget/fancy_button");
Subscription buttons = Strata.themes().override(MinecraftThemeSource.VANILLA, ThemeOverrides.of()
        .with(Button.STYLE_NORMAL, Paint.nineSlice(fancy, Insets.of(4f))));

// A theme of the mod's own, which follows packs for every key it leaves alone
Subscription dark = Strata.themes().define(ThemeId.of("examplemod", "dark"), MinecraftThemeSource.VANILLA,
        ThemeOverrides.of().with(Window.STYLE_BACKGROUND, Paint.solid(0xF0101010)));
```

Theme files for a defined id, such as `assets/examplemod/strata/themes/dark.json` in a pack, still apply above the definition. [Overrides from code](../../guides/theming.md#overrides-from-code) in the theming guide covers both methods.

A theme built in code ignores packs:

```java
Theme theme = Themes.flat().toBuilder()
        .paint(Button.STYLE_NORMAL, Paint.solid(0xFF2E7D32))
        .build();
```

A theme file with an error is logged and skipped, and the other packs still apply. A reload whose result lacks a key the open screen needs is logged too, and the screen keeps its old theme until the next change.

### Layers

A theme comes together from [layers](../../guides/theming.md#layers), lowest first, and a later layer wins per key. For the vanilla theme they are:

| Layer | What `Theme.layer(key)` reports |
|---|---|
| The base file, `vanilla.json`, over `strata:flat` | `vanilla` |
| Theme files from the player's packs, in the game's pack order, so a pack higher in the player's list wins. Vanilla's built-in packs, such as `programmer_art` and `high_contrast`, and a server's or world's pack are among them. | The pack's id, such as `file/fancy_pack` |
| Theme files from mods' own resources, in the game's order of those packs | The pack's id, such as `mod/examplemod` on NeoForge or `examplemod` on Fabric |
| Overrides added with `Strata.themes().override`, in the order they were added | `code` |
| The ref's own overrides | `ref` |

`Theme.layer(key)` names the layer that set a key, which helps find where a color or a sprite came from. A theme of a mod's own with `minecraft:vanilla` as its parent resolves the vanilla theme in full, all of these layers included, and then puts its own theme files, overrides and ref on top.

### Mod packs and player packs

Mods' theme files go above the player's packs, whatever order the game stacks the packs in, so a resource pack cannot change a key that a mod's own resources set. Within each group the game's order holds. That order applies only to the theme files Strata reads. The game's pack stack is left as it is, so sprites and every other asset keep the game's order: a theme file decides which sprite a key uses, and the highest pack that has the image decides how it looks.

`MinecraftThemeSource` tells a mod's pack from the player's by the pack's known pack info, which vanilla's `Resource.knownPackInfo()` returns. A pack counts as a mod's when it has known pack info whose namespace is not `minecraft`. The pack source cannot tell them apart, because NeoForge marks mods' packs `PackSource.DEFAULT`, the same source as the resource pack folder.

| Pack | Known pack namespace | Pack id | Counts as |
|---|---|---|---|
| Vanilla and its built-in packs | `minecraft` | `vanilla`, `programmer_art`, `high_contrast` | The player's |
| The resource pack folder | None | `file/<name>` | The player's |
| A server's pack, or a world's | None | `server/<serial>/<id>` | The player's |
| A mod's resources on NeoForge 26.3.0.83 | `neoforge` | `mod/<modid>`, one pack per mod file, with the ids of every mod in the file joined by commas | A mod's |
| A mod's resources on Fabric API 0.163.0 | `vanilla` | The mod id | A mod's |

Vanilla's built-in packs count with the player's, since the player chooses them. A pack a mod adds through a repository source of its own counts as the player's unless it carries known pack info outside the `minecraft` namespace.
