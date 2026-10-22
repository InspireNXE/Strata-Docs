# Platform differences

Strata hides most engine differences, but the SPI leaves some decisions to each adapter. This page lists them, with what the Swing and Minecraft adapters do, so you can build a screen in Swing and know which parts may behave differently on another engine. Each adapter's own documentation says what it does in each case.

## At a glance

| Topic | The SPI allows | Swing | Minecraft |
|---|---|---|---|
| One UI unit | Whatever the adapter chooses, often one engine pixel times a user scale | `scale` pixels, changed with `setScale` | One GUI pixel, so it follows the GUI scale |
| Density (`pixelsPerUnit`) | Whatever device pixels a unit covers, told to the host with `setPixelsPerUnit`; 1 until set | The scale times the display's scale, followed when the window moves to another display | The GUI scale; the window's content scale is not multiplied in again |
| Rounding | Rounding to the engine's pixel grid, as long as drawing and clipping agree | None. Java2D draws fractional coordinates. | Each edge of a fill, sprite or clip moves down to a whole GUI pixel; text rounds to the nearest pixel |
| Font and text widths | Any font, with whole or fractional widths | An AWT font, Dialog 9 by default, with fractional widths | The player's font, in whole GUI pixels, with bold one pixel wider per character |
| Line height | Whatever the font reports | The AWT font's height | 9 for the default font |
| Text ascent (`TextMetrics.ascent`) | The font's own, or the default of four fifths of the line height | The font's real ascent | 7, vanilla's baseline |
| Monospaced text | A monospaced font, or the normal font when the engine has none (`Capabilities.monospaceFont`) | Monospaced | The normal font |
| Modifiers with pointer moves and scrolls | `NONE` when the engine does not report them | Reported | Shift, Control and Alt, read from the game |
| Modifiers with typed characters | `NONE` when the engine does not report them | Reported | Never reported (`NONE`) |
| Modifiers with button presses and keys | Reported | Reported | Reported |
| Shortcut modifier (`Conventions.shortcutModifier`) | `CONTROL`, or `SUPER` where shortcuts use Command | Command where the toolkit's menu shortcut key is Meta (macOS), else Control | Command on macOS, Control elsewhere |
| Mnemonics (`Conventions.mnemonics`) | On, or off for an engine that binds Alt for itself | On | On |
| Context menu key | `Key.MENU` where the engine has the key | Mapped | Mapped |
| Key repeat | Reported by the engine, or made by the host for navigation and delete keys | Held keys repeat at the system rate | Held keys repeat at the system rate |
| Typed characters | Only after the adapter's `TextInputSink` starts text input, if the engine needs that | Always | Only while a component that accepts text input has focus |
| Secret text input | Input methods and suggestions turned off where the engine allows, or nothing | Input methods off on the panel while a secret field has focus | Started like any field; the game has no mode for secrets |
| Horizontal scrolling | From the device, in Strata's direction | Shift plus the vertical wheel | From the device, flipped to Strata's direction, and Shift plus the wheel |
| Touch and pen input (`InputContext`) | Sent through the forms that take an `InputContext` | None, since AWT reports none | None; a controller mod could send buttons as keys |
| When buttons act | On the click, or on the press when the platform's `Conventions.activateOnPress` is on, for buttons, cycle buttons, checkboxes, radio buttons, toggle switches and tabs | On the click | On the press |
| Escape | Passed on to the engine when no component consumes it | Does nothing unless you handle it | Closes the screen unless consumed, once the screen's `closeRequested` agrees |
| Closing and the title | The engine's close button calls `requestClose` first; the engine shows `title()` | `StrataPanel.manageWindow` for a window's close button and title | Escape, `onClose` and the inventory key ask `closeRequested`; the title follows `setTitle` and narrates |
| Preferred size | `preferredSize` for a toolkit that asks, unused by an engine whose screens fill the window | The panel's preferred size, so `pack()` fits the screen | Not used |
| Clipboard | The engine's, text only or with files, images and other formats, or `Clipboard.NONE` | The system clipboard with text, files, images and MIME text; none when headless | Vanilla's, text only; other formats of a copy stay in the screen |
| Drops from other applications | The `InputSink` drag methods, or `dropped` alone | A drop target on the panel, with the full drag; none when headless | Files dropped on the window arrive as a single `DROP` with no drag before it |
| File dialogs | `Platform.fileDialogs()`, or none | The system's dialogs; none when headless | Not offered |
| Cursors | Any kinds the canvas shows (`Canvas.showsCursor`); the host walks `CursorKind.fallback()` for the rest | Every kind; headless, the custom ones (not allowed, progress, grab, grabbing, copy, no drop, help) fall back | All but grab, grabbing, copy, no-drop and help, which show their fallbacks |
| Shapes (`Capabilities.drawsShapes`) | Drawn by the engine, or by core from fills one device pixel high without anti-aliasing | Java2D, anti-aliased | Core's fallback; round clips are square |
| Opacity (`pushOpacity`) | Faded draws, or none; an engine that fades only some draws says which | Everything | Everything except item visuals |
| Pixel buffers | An engine copy of each buffer, refreshed when its version moves | An image sharing the array, scaled nearest-neighbor | A texture per buffer, sampled nearest |
| Frame scheduling | A frame every render, or on demand with `needsFrame`, `nextFrameNanos` and the frame requester | On demand; an idle panel does not paint | Every frame; scheduling is ignored |
| UI thread | The engine's UI or render thread, told to core with `uiExecutor` and `uiThreadCheck` | The event dispatch thread (`invokeLater` and `isEventDispatchThread`) | The client thread; `Strata.uiThread()` queues on the game's task queue, even from that thread |
| Right-to-left text (`Capabilities.shapesRightToLeft`) | Shaped by the engine, or reversed and mirrored by core, which leaves Arabic unjoined | Shaped by Java2D, letters joined | Reversed by core; Arabic draws unjoined |
| Plural rules (`Translator.plural`) | The engine's own rules, or core's CLDR rules for the translator's locale | Core's rules | Core's rules, since vanilla has none |
| Missing sprite | Anything visible | Magenta | Vanilla's missing-texture pattern |
| Visuals | The engine objects the adapter makes visuals of, and a visible placeholder for any other | `BufferedImage`s, through `SwingVisuals`; magenta for any other | Items and player faces; magenta for any other |
| Bold, italic, underline, strikethrough, shadow | The engine's own where it has them; an engine with one font weight may fake or ignore bold | Bold and italic fonts derived from the base font; the lines and the shadow drawn by the adapter | Vanilla's |
| Translations | Usually the engine's language files, through the adapter's `Translator` | The `ResourceBundle` or `Translator` given to `SwingTranslations.use`; until then English for Strata's keys and the key itself for others | The game's language files, so a mod's `lang` files and resource packs apply |
| Theme files | Any order the adapter chooses, later files winning per key, each named for its layer | Class path order, earlier entries winning; a file's layer is its jar's name or its directory's URL | The player's packs in the game's order, with mods' files above them |
| Sprite location | Wherever the adapter reads images from | `assets/<namespace>/sprites/<path>.png` on the class path, with `@2x` and `@1.5x` beside it | `assets/<namespace>/textures/gui/sprites/<path>.png`, through the gui atlas |
| Nine-slice slicing | The `border` argument, or slicing the engine stores with the image | From the `border` argument, in image pixels | From the sprite's `.png.mcmeta`; the `border` argument is ignored |
| Screen stack | `ScreenStack`, or one host per engine screen when the engine stacks screens itself | `StrataPanel.screens()` | Vanilla's, one `StrataScreen` per Strata screen |
| `Strata.screens()` | The adapter's controller, handed over when it starts | The stack of the panel marked with `StrataPanel.setDefault()`; throws until one is marked | Opens a new `StrataScreen`; works once the Strata mod has loaded |
| Default theme (`ThemeRef.platform()`) | The theme the adapter's `ThemeSource.defaultTheme()` names | `strata:flat`, or the theme `SwingThemeSource.setDefaultTheme` names | `minecraft:vanilla` |

The sections below cover the differences most likely to affect a screen.

## Units and rounding

Layout always runs in floats. An adapter for an engine that draws on whole pixels rounds when it draws, so a panel at x = 10.4 with width 20.2, which ends at 30.6, covers pixels 10 to 29 on an adapter that moves edges down to whole pixels, as the Minecraft adapter does, or 10 to 30 on one that rounds them to the nearest, and its exact fractional span in Swing. Design with whole numbers and the two look alike.

When an engine has a user scale setting, it changes how many screen pixels a unit is, which changes how many units fit across the screen. Use anchors, stretching and weighted grids rather than fixed positions, so layouts fit every size.

## Text

Fonts differ in width from one engine to the next, and some report whole-pixel widths. Text that just fits in Swing may wrap or overflow elsewhere. Never size components by guessing text widths. Let components measure, and leave slack in fixed widths around text.

## Modifier keys

Some engines pass no modifiers with pointer moves, scrolls or typed characters, and the adapter then reports `Modifiers.NONE`, unless it can read them elsewhere, as the Minecraft adapter does for Shift, Control and Alt on moves and scrolls. A Shift+scroll or Ctrl+drag check that works in Swing may never fire on an engine that reports nothing. Button presses and key events carry modifiers on every adapter. If you need modifier state during a drag, remember it from the `POINTER_PRESS` that started it.

Shortcuts and text fields' edit keys use the platform's shortcut modifier, `Conventions.shortcutModifier`: Control on most platforms and Command, sent as `Modifier.SUPER`, where the platform uses it. Use `KeyStroke.shortcut` for your own shortcuts and they follow.

## Typed characters

Some engines deliver characters only while the application reports a focused text field. Strata starts text input through the adapter's `TextInputSink` for components whose `acceptsTextInput()` is true. A custom component that reads `CHAR` without that override works in Swing and may receive nothing elsewhere.

## Scrolling

Adapters report horizontal scrolling from devices that have it, with the sign flipped where needed to Strata's convention: positive toward the left. Swing treats Shift plus the vertical wheel as horizontal scrolling. Some engines scale scroll amounts by a user sensitivity setting, so a notched wheel can arrive as fractional, `PRECISE` scrolls there.

## Escape and closing

An Escape that no component consumes goes back to the engine, and some engines close the current screen on it. Swing does nothing, while Minecraft closes the screen once the screen's `closeRequested` agrees. If your screen should close on Escape everywhere, handle it on the root and call `handle.requestClose()` or `handle.close()`. If it should never close on Escape, consume the key. A screen with unsaved changes refuses in `closeRequested`, which the engine's own close button and keys ask too, and is the one place that works on every adapter.

## Shapes, opacity and pixel buffers

Everything a `Painter` draws works on every adapter, and the optional parts differ in how they look. An engine that cannot draw shapes leaves them to core, which draws every line, outline, oval, arc, shape and angled gradient from fills one device pixel high, a shadow from a few rings of falling alpha, and a round clip as a clip to the whole rectangle. Those look stepped, without anti-aliasing, and a corner meant to be rounded draws square when it clips. Swing draws them with Java2D. Design for the fallback, and a screen looks right on both.

An engine that cannot fade some kinds of draw says so. On Minecraft, items stay opaque while the component that draws them fades, and an item stops showing when the component stops drawing it. Pixel buffers show everywhere, drawn with nearest sampling so pixel art stays sharp.

## Density

A line one device pixel wide is `1 / pixelsPerUnit()` units. Read the density from the environment or the painter instead of assuming 1, and hairlines stay thin on a display that doubles pixels and on a game at a high GUI scale. Sprites also depend on it: Swing draws a denser `@2x` image where a unit covers two device pixels, and Minecraft keeps one image per sprite, as its resource packs do.

## Threads and frames

Change a shown component only on the UI thread, and use `Strata.uiThread()` to move there from anywhere else; the thread is the event dispatch thread on Swing and the client thread on Minecraft. A state set on any thread is safe on both. The adapters also differ in when they draw: Swing draws when Strata asks for a frame, and Minecraft draws every frame. A custom component that changes something only `paint` reads must call `repaint()`, or the change shows only on an adapter that draws every frame. See the [extending guide](../guides/extending.md).

## Right to left and plurals

A screen runs right to left when the language is written that way, on every adapter. What differs is the text: Swing orders and joins a right-to-left run itself, and Minecraft's font draws one character at a time, so core reverses the run and mirrors its brackets, which reads correctly for Hebrew and draws Arabic with its letters unjoined. Plural text picks its form by CLDR rules for the translator's locale on both, unless an adapter's translator supplies its own.

## Nine-slices

A nine-slice without a size is sliced by the canvas: Swing uses the paint's `border`, in the image's own pixels, and stretches the edges and center. With a size, and for tiled sprites, Strata draws the pieces itself, so engines without stored slicing, Swing among them, all draw the same. An engine that stores slicing with the image (its platform reports `Capabilities.slicesSprites`) uses that instead and ignores the paint's border, size and tiling. To look the same everywhere, give the paint the size, border and tiling the image's own slicing has.

## Screens and stacks

In Swing, every screen sits on the panel's stack, which draws them and honors `hidesBackground()`. An engine with its own screen system may give each Strata screen its own host and engine screen instead. There, the engine decides what draws behind a screen and `hidesBackground()` has no effect. Dialogs drawn as [overlays](../guides/overlays.md) behave the same on every adapter, because they live inside one host.
