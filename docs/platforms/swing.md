# Swing

The Swing adapter draws Strata with Java2D and takes input from AWT. It is the fastest way to work on a screen, since there is no game to start, and `SwingSnapshot` renders screens to images without a display for tests and documentation. Every image on this site was rendered that way.

## Showing screens

`StrataPanel` is a `JPanel` that holds a stack of Strata screens:

```java
--8<-- "swing/SwingExamples.java:window"
```

| Constructor argument | Meaning |
|---|---|
| `SwingPlatform` | Text metrics, clipboard, clock and [theme source](#themes). Its text metrics also draw the text. |
| `Theme` | The theme for every screen pushed |
| `scale` | Pixels per UI unit, which must be positive. `setScale` changes it later; see [Scale and density](#scale-and-density). |

`panel.screens()` is the [screen stack](../guides/screens.md#stacks-of-screens): push or open to show a screen, pop to close the top one. Only the top screen gets input. The stack is also the panel's `ScreenController`.

`panel.setDefault()` marks the panel whose stack `Strata.screens()` returns, so code that opens screens through the API opens them on that panel. Marking another panel moves the mark, and `isDefault()` says which panel has it. Swing has no screens of its own, so until a panel is marked, `Strata.screens()` throws `IllegalStateException` with a message saying to mark one. It does not open a window.

`push` shows a screen in the panel's theme, the one given to the constructor or set later with `screens().setTheme`. `open(screen)` shows it in the platform's default theme, which `ThemeRef.platform()` names. On Swing that is `strata:flat`, until `setDefaultTheme` on the platform's `SwingThemeSource` names another:

```java
SwingThemeSource themes = new SwingThemeSource(App.class.getClassLoader());
themes.setDefaultTheme(ThemeId.of("myapp", "dark"));
StrataPanel panel = new StrataPanel(new SwingPlatform(SwingTextMetrics.defaults(), themes), Themes.flat(), 2f);
```

The code that opens screens and works with the panel runs on the Swing event dispatch thread, as for any Swing component. The platform's [UI thread](../guides/threading.md) is the event thread: `SwingPlatform` supplies `SwingUtilities::invokeLater` as the executor and `SwingUtilities::isEventDispatchThread` as the test. Work from other threads goes through `Strata.uiThread()` or the factory's `uiThread()`, and a state set from any thread reaches its controls on the event thread. A screen opened from another thread with `Strata.screens().open` is handed to the event thread, and the call returns a handle at once. `SwingSnapshot` renders on the calling thread, since it runs no panel.

## Windows

`manageWindow` gives the panel a window to run:

| Window | What the panel does |
|---|---|
| Close button | Asks the screens with `requestClose` and disposes the window only if every screen agrees, so a screen with unsaved changes can refuse and ask. A `JFrame` or `JDialog` is set to do nothing on close, since the panel decides. |
| Title | A `Frame` or `Dialog` shows the top screen's title, translated, again whenever it changes (`Screen.title()` and `ScreenHandle.setTitle`). While no screen is open or the top one has no title, the window keeps the title it had when it was given. |
| Size | `pack()` fits the top screen: the panel's preferred size is what the screen's content asks for, in pixels at the panel's scale plus the insets, and 960 by 600 while no screen is open. A size set with `setPreferredSize` wins. The panel revalidates when the top screen changes, so a later `pack()` sees the new size. |

An application that runs its own window listener does the same by hand: it sets the window to do nothing on close, calls `screens().requestClose()` when the window is closing, and disposes the window only when that returns true.

Escape that no component uses does nothing in the panel. Handle it on the root and call `handle.close()`, or `handle.requestClose()` to let the screen refuse.

## How Swing maps onto Strata

- One UI unit is `scale` pixels. Drawing is scaled up and pointer positions scaled down by the same factor, and the screens are resized whenever the panel is.
- The panel repaints when Strata asks for a frame, and at the next timed change, such as a caret's blink. It sets itself as the stack's frame requester, which schedules the frame on the event thread from whatever thread changed something, and after each paint it waits for `nextFrameNanos` with a one-shot timer. Either way it waits until one interval of the stack's frame rate has passed since the last paint began, so it paints at most `screens().frameRate()` times a second, 30 unless set. Swing's own repaints, as when the panel is resized or uncovered, are not held to the rate. An idle panel with a blinking caret paints twice a second, and one with nothing timed does not paint. A timer stops when the panel is removed from its window. See [Rendering on demand](writing-an-adapter.md#rendering-on-demand).
- Its own background is `0xFF1E1F22` where no screen draws.
- It takes keyboard focus when clicked, and turns off Swing's own Tab handling so Strata's tab order applies.
- AWT reports held keys as repeated presses. The panel turns them into `REPEAT`s, and forgets held keys when it loses focus.
- Characters come from AWT's key-typed events, with surrogate pairs joined into one code point and control characters dropped.
- While a password box, or any field that asks for `TextInputKind.SECRET` text, has focus, the panel turns input methods off on itself, as Swing's own password field does, so no composition window shows what is typed. They come back on for the next ordinary field or when focus leaves the fields.
- Shift plus the vertical wheel scrolls horizontally. A whole wheel rotation counts as `WHEEL`, a fractional one as `PRECISE`, which a spinner adds up.
- Shortcuts use Command where the toolkit's menu shortcut key is Meta, as on macOS, and Control elsewhere. Meta is sent as `Modifier.SUPER`. In a headless JVM the operating system's name decides. Buttons act on release and mnemonics are on, so Alt with an underlined letter acts and Alt or F10 alone enters a menu bar. The context menu key is `Key.MENU`, and Shift+F10 opens a context menu too.
- The panel consumes Alt and F10 after Strata sees them, so they do not put the window into its native menu mode, which on Windows would swallow wheel input until the key is pressed again.
- AWT reports no touch or pen input, so every pointer is a mouse.
- Screens' [repeating tasks](../guides/screens.md#repeating-tasks) run on the event thread, each from a `javax.swing.Timer` that coalesces the runs it falls behind on into one. A snapshot draws one frame on a thread of its own, so its tasks never run.

## Scale and density

One UI unit is `scale` pixels, and `setScale` changes it while the panel is open, such as when the user zooms. The screens get the panel's size in the new units and the new density, lay out again and repaint, and the panel revalidates since its preferred size changes with the scale.

The density, the device pixels one unit covers, is the scale times the display's own scale, which Java2D applies on top of what the panel draws. `pixelsPerUnit()` returns it, and it is what components read from `Environment.pixelsPerUnit()` and what sprites choose their image by. The panel passes it to the screens when it is first shown, when `setScale` runs, and when it moves to a display with another scale.

```java
--8<-- "swing/SwingExamples.java:zoom"
```

Where one unit covers 2 device pixels or more, the canvas draws a sprite's denser image, and from 1.5 its 1.5 image, falling back to the plain image where a denser one is missing. The class path source finds them beside the plain file, with `@2x` or `@1.5x` before the extension:

```text
assets/examplemod/sprites/icon/star.png
assets/examplemod/sprites/icon/star@2x.png
```

A denser image draws at the same size in UI units, so the sprite only gets sharper, and a sized nine-slice keeps the size of its border in units. A `SpriteSource` of your own supplies denser images by overriding `loadDense`, which is asked with 2 or 1.5 and answers empty where it has none. `SwingSprites.clear()` drops the loaded images, and the panel's density changes do not need it.

```java
--8<-- "swing/SwingExamples.java:dense"
```

## Drawing

`SwingCanvas` draws every shape call with Java2D, so `SwingPlatform` reports `Capabilities.drawsShapes` and core never falls back to stepped rows. Lines, rounded rectangles, ovals, arcs, shapes, strokes with caps and joins, gradients at an angle, soft shadows and round clips are drawn by Java2D, anti-aliased unless a push of `smoothing(false)` turns it off. Strokes use pure stroke control, so they land where the contract puts them. A shadow is a rounded rectangle blurred three times with a box blur, and the 64 sizes and colors drawn last are cached. Round clips are hard-edged. Opacity is an `AlphaComposite`, so a fade covers fills, gradients, shapes, sprites, nine-slices, text, visuals and pixel buffers alike.

A pixel buffer is drawn from an image that shares its array, so a new version needs no copy. It scales with nearest-neighbor sampling so pixel art stays sharp, and a tinted copy is made again when the version moves.

## Cursors

Every `CursorKind` shows. `ARROW`, `TEXT`, `HAND`, `CROSSHAIR`, the resize kinds and `WAIT` are AWT's own cursors, with the two diagonals as the north-west and north-east resize cursors. `NOT_ALLOWED`, `PROGRESS`, `GRAB`, `GRABBING`, `COPY`, `NO_DROP` and `HELP` are custom cursors the adapter draws in code and makes with `Toolkit.createCustomCursor`, once. A headless toolkit makes no custom cursors, so there those kinds report that they cannot be shown, and the host asks for their fallback.

## Fonts

`SwingTextMetrics` picks the fonts. One base font is derived into plain, bold, italic and bold italic, and its size is in UI units:

```java
--8<-- "swing/SwingExamples.java:font"
```

`SwingTextMetrics.defaults()` is the logical Dialog font at 9 units, with the logical Monospaced font at the same size for monospaced styles. Widths are the advances of a `TextLayout` with antialiasing and fractional metrics on, and the canvas draws that same layout, so text measures exactly as wide as it draws and spans side by side meet. The line height is the font's height. Underline, strikethrough and the shadow are drawn by the adapter and do not change widths. The ascent that panels line text up by is the font's real ascent, in UI units, and never more than the line height.

`register` adds a family under the id styles name, as an AWT `Font` or any `SwingFont`, and `unregister` removes it. Register on the event thread. A registered family wins over `monospace`, and an id nobody registered draws in the normal font. Registering, replacing or removing a family changes `version()`, so open panels lay out again:

```java
--8<-- "swing/SwingExamples.java:register"
```

A style's size scales the chosen font from its own size, `SwingFont.size()`, and size 0 is the normal font's size (`defaultSize()`). The canvas draws a scaled span under a Java2D scale, so the glyphs are drawn from the font's outlines at that size. A registered font's own size does not matter, so a font read at 1 point with `Font.createFont` is fine.

Behind `SwingTextMetrics` are two `SwingFont`s, one for normal text and one for monospaced styles, and the registered ones. `AwtFont` is the AWT font described above. `BitmapFont` draws glyphs cut from images, to preview an engine whose font is a bitmap:

```java
--8<-- "swing/SwingExamples.java:bitmap"
```

Each sheet is split into a grid, one character per cell, row by row, with `U+0000` for an empty cell. A white glyph on a transparent ground draws in the text's color. A glyph advances by its rightmost column with any opaque pixel, plus one, scaled from the cell to the glyph's height and rounded, plus one unit of spacing; a cell with no opaque pixel advances one unit. `advance(codePoint, width)` gives a character, usually the space, a fixed advance with nothing drawn. Bold draws each glyph again one unit to the right and adds a unit to every advance, and italic slants glyphs. Characters with no glyph come from a fallback font, the logical Dialog font unless the builder is given another. The first sheet or advance that names a character wins.

Swing shapes right-to-left text itself, and `SwingPlatform` reports `Capabilities.shapesRightToLeft`. `SwingFont.drawRightToLeft` draws a run that reads right to left, which the canvas calls with the run in logical order. `AwtFont` lays it out with a right-to-left run direction, so Java2D orders it, mirrors its brackets and joins Arabic letters. `BitmapFont` draws the glyphs from the right with paired brackets mirrored, and does not join letters. A `SwingFont` of your own should override `drawRightToLeft` unless its drawing runs the bidirectional algorithm itself; the default calls `draw`.

Other adapters use other fonts with other widths. Layouts built on measured text adapt; layouts that assume widths will not.

## Translations

Swing has no language files of its own, so translated text reads through a translator you set for the whole process. `SwingTranslations.use(bundle)` reads a `ResourceBundle`, and `SwingTranslations.use(translator)` takes any `Translator`. Call it again with another bundle to change the language; open panels lay out again on their next frame. The bundle's locale becomes the translator's, so dates and times follow the bundle's language; a base bundle, whose locale is `Locale.ROOT`, leaves the JVM's default locale for formatting. Until one is set, Strata's own keys read in English and other keys read as themselves.

```java
SwingTranslations.use(ResourceBundle.getBundle("shop.messages", Locale.GERMAN));
```

## Appearances

The adapter registers an [appearance](../guides/appearances.md) for `BufferedImage` when the first `SwingPlatform` is made: the image as its own icon, at its pixel size. A list of images needs no cell factory, and `factory.image(bufferedImage)` shows one.

`SwingVisuals.image(image)` makes a [visual](../guides/appearances.md#visuals-and-icons) of an image at its pixel size, one UI unit per pixel, and `SwingVisuals.image(image, width, height)` one with a size of its own, for an image meant to draw smaller or larger than its pixels. These visuals are live: the image is kept without copying, and the canvas draws what it holds at each paint, so drawing into the image on the Swing event thread shows on the next frame. Two visuals are equal only when they hold the same image object at the same size. The canvas draws a visual another adapter made as magenta.

## Sprites

`SwingSprites` loads images from the classpath at `assets/<namespace>/sprites/<path>.png`. Pass a `SpriteResolver` to `SwingSprites`, and that to `StrataPanel` or `SwingSnapshot.render`, to read another layout. For images that are not on the class path at all, such as files in a folder or a zip, give `SwingSprites` a `SpriteSource`, which returns the image for an id. Images are loaded once and cached, including the fact that one is missing, until `SwingSprites.clear()`. Denser images sit beside the plain one as `@2x` and `@1.5x` files; see [Scale and density](#scale-and-density). A missing or unreadable image draws as solid magenta.

`StrataPanel.setSprites` swaps an open panel's images. With `screens().setTheme(...)` it changes a panel's look without closing its screens:

```java
--8<-- "swing/SwingExamples.java:source"
```

The panel and `SwingSnapshot` look images up through the class loader that loaded the Swing adapter. In an ordinary application, where everything shares one class path, that finds images in your own jar. Under a plugin system that gives each plugin its own class loader, it may not.

Tints multiply each channel of the image, and tinted copies are cached per sprite and tint. Sprite edges snap to the nearest device pixel, as fills and clips do, so pieces that share an edge meet at any scale. A nine-slice without a size has its borders in the image's pixels; borders wider than the rectangle shrink in proportion so nothing draws outside it, and Swing stretches its edges and center. Swing stores no slicing with its images, so Strata draws sized nine-slices and tiled sprites itself, from clipped copies of the image.

## Themes

`SwingThemeSource` is the platform's [theme source](../guides/theming.md#themes-by-id). `new SwingPlatform(metrics)` makes one on the adapter's class loader; pass your own to `new SwingPlatform(metrics, themes)` to use another class loader, register themes or name the default.

- Theme files are read from `assets/<namespace>/themes/<path>.json` on the class path, unless a `ThemePathResolver` names another place. Every class path entry that has the file adds one, and an entry earlier on the class path wins per key, as it does for any resource. Each file's layer is named after its entry: a jar's file name, or a directory's URL.
- `register(id, theme)` gives an id a base theme built in code, and `register(id, documents)` gives it [base files](../guides/theming.md#base-files). Each replaces whatever base the id had.
- `setDefaultTheme(id)` names the theme that `ThemeRef.platform()` and `open(screen)` use, `strata:flat` until then.
- Files are read once per id and cached. `reload()` drops the cache, and open panels resolve their themes again on their next frame, so a tool can call it after editing a theme file. Registering and `setDefaultTheme` reload too.
- `resolve(ref)` resolves a ref once into a fixed `Theme`, with the overrides added in code, as a panel would.

Every method may be called from any thread.

## Clipboard

`SwingPlatform` uses the system clipboard. It holds plain text, files, images and text in other MIME types, through `getTransfer` and `setTransfer`:

| Format | AWT flavor |
|---|---|
| `DataFormat.TEXT` | `DataFlavor.stringFlavor` |
| `DataFormat.FILES` | `DataFlavor.javaFileListFlavor` |
| `DataFormat.IMAGE` | `DataFlavor.imageFlavor`, an ARGB image holding a copy of the pixels |
| A `DataFormat.mime(...)` format | A text flavor of that MIME type whose value is a string |

Styled text goes out as its plain text, and formats that stay inside the process never reach the system clipboard. A custom MIME format reaches other programs only under Java's own flavor names. Reads and writes that fail, because another program holds the clipboard or it holds nothing Strata understands, read as empty and are dropped. In a headless JVM there is no clipboard at all.

## Drops from the desktop

`StrataPanel` is a drop target. Files, text and the other formats the clipboard holds can be dragged onto it from other applications, and the component under the pointer hears `DRAG_ENTER`, `DRAG_OVER`, `DRAG_EXIT` and `DROP` as for a drag inside the panel. Data is read when a component asks for it while the drag moves, where AWT allows, and all of it at the drop. AWT reports no modifier keys during a drag, so they are read from the drop action: Control when the user asks to copy what the source would move, and Control and Shift for a link. A headless JVM has no drag and drop. Drags out of the panel to other applications are not supported.

## File dialogs

`SwingPlatform.fileDialogs()` offers the system's dialogs, through `java.awt.FileDialog`, owned by the active window. A folder dialog is a `JFileChooser` except on macOS, where AWT's dialog can pick folders. Each dialog is shown from a later event on the event thread, so the call returns at once; the dialog's nested loop keeps the window painting while it is open, and the future completes when it closes. Titles are read through the translator at that time. Windows ignores filename filters, so there the file name starts as the filters' patterns, such as `*.csv;*.xlsx`, unless a name was suggested or a file given as the start. Headless there are none, and `ComponentFactory.fileDialogs().isSupported()` is false. See [File dialogs](../guides/files.md).

## Rendering without a display

`SwingSnapshot.render` opens a screen on a fresh stack, draws one frame into an image and closes it again:

```java
--8<-- "swing/SwingExamples.java:snapshot"
```

Any pixel the screen does not draw stays transparent. The screen's `closed()` runs before `render` returns, and a theme missing a required key throws `ThemeException`. Overloads take input to send before the frame is drawn, such as a click that opens a drop-down, with tooltips shown at once; a `SwingSprites`, for images from elsewhere; a `SwingPlatform`, for snapshots with other fonts; and a `ThemeRef`, resolved from that platform's [theme source](#themes). The scale is also the screen's density, so a snapshot at a scale of 2 shows the denser sprites. Set `-Djava.awt.headless=true` on build servers. The adapter's own tests and the gallery's `--snapshot` option use it.

Snapshots are a good way to test layouts: render a screen and compare pixels, or check that it renders at all with each of your themes.

## Hosting Strata in other Java2D code

`StrataPanel` is the usual way in. To draw Strata inside other Java2D code, such as a game loop of your own, use the pieces it is made of:

- `StrataCore.createScreenStack(platform, theme)` or `createScreenHost` from `core`, with a `SwingPlatform`.
- A `SwingCanvas` made with the platform's `SwingTextMetrics` and a `SwingSprites`. Call `begin(graphics)` at the start of every frame, with a `Graphics2D` whose transform maps UI units to pixels, then `render(canvas)` on the host, then show the cursor `requestedCursor()` returns.
- `SwingInput` maps AWT key events, mouse buttons and modifier masks to Strata's types, and `HeldKeys` from the SPI turns AWT's repeated presses into repeats.

[Writing an adapter](writing-an-adapter.md#driving-the-host) lists what the host expects of the code that drives it.

## Not supported

- Touch and pen input. AWT reports none, so the panel never sends an `InputContext` other than the mouse's.
- Closing a screen on an Escape that nothing consumed. The panel leaves Escape alone; the opt-in is not built.
- Drags out of the panel to other applications.

## The gallery

The Strata-Gallery repository runs on this adapter, and on the [Vulkan](vulkan.md) one. It gives every control a page, written against the API only, with a log of the events the control fires:

```bash
./gradlew runJava2d
./gradlew run --args="--snapshot gallery"
```

`runVulkan`, or `--renderer vulkan`, shows the same pages with the Vulkan adapter.
