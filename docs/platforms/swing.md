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
| `scale` | Pixels per UI unit, which must be positive |

`panel.screens()` is the [screen stack](../guides/screens.md#stacks-of-screens): push or open to show a screen, pop to close the top one. Only the top screen gets input. The stack is also the panel's `ScreenController`.

`panel.setDefault()` marks the panel whose stack `Strata.screens()` returns, so code that opens screens through the API opens them on that panel. Marking another panel moves the mark, and `isDefault()` says which panel has it. Swing has no screens of its own, so until a panel is marked, `Strata.screens()` throws `IllegalStateException` with a message saying to mark one. It does not open a window.

`push` shows a screen in the panel's theme, the one given to the constructor or set later with `screens().setTheme`. `open(screen)` shows it in the platform's default theme, which `ThemeRef.platform()` names. On Swing that is `strata:flat`, until `setDefaultTheme` on the platform's `SwingThemeSource` names another:

```java
SwingThemeSource themes = new SwingThemeSource(App.class.getClassLoader());
themes.setDefaultTheme(ThemeId.of("myapp", "dark"));
StrataPanel panel = new StrataPanel(new SwingPlatform(SwingTextMetrics.defaults(), themes), Themes.flat(), 2f);
```

Use the panel on the Swing event dispatch thread only, the same as any Swing component.

## How Swing maps onto Strata

- One UI unit is `scale` pixels. Drawing is scaled up and pointer positions scaled down by the same factor, and the screens are resized whenever the panel is.
- The panel repaints about 60 times a second while it is shown, and stops when it is removed from its window.
- Its preferred size is 960 by 600 pixels, and its own background is `0xFF1E1F22` where no screen draws.
- It takes keyboard focus when clicked, and turns off Swing's own Tab handling so Strata's tab order applies.
- AWT reports held keys as repeated presses. The panel turns them into `REPEAT`s, and forgets held keys when it loses focus.
- Characters come from AWT's key-typed events, with surrogate pairs joined into one code point and control characters dropped.
- While a password box, or any field that asks for `TextInputKind.SECRET` text, has focus, the panel turns input methods off on itself, as Swing's own password field does, so no composition window shows what is typed. They come back on for the next ordinary field or when focus leaves the fields.
- Shift plus the vertical wheel scrolls horizontally. A whole wheel rotation counts as `WHEEL`, a fractional one as `PRECISE`.
- The panel consumes Alt and F10 after Strata sees them, so they do not put the window into its native menu mode, which on Windows would swallow wheel input until the key is pressed again.
- Cursors map to AWT's predefined cursors. AWT has no "not allowed" cursor, so `NOT_ALLOWED` shows the default arrow.

## Fonts

`SwingTextMetrics` picks the fonts. One base font is derived into plain, bold, italic and bold italic, and its size is in UI units:

```java
--8<-- "swing/SwingExamples.java:font"
```

`SwingTextMetrics.defaults()` is the logical Dialog font at 9 units, with the logical Monospaced font at the same size for monospaced styles. Widths are the advances of a `TextLayout` with antialiasing and fractional metrics on, and the canvas draws that same layout, so text measures exactly as wide as it draws and spans side by side meet. The line height is the font's height. Underline, strikethrough and the shadow are drawn by the adapter and do not change widths.

Behind `SwingTextMetrics` are two `SwingFont`s, one for normal text and one for monospaced styles. `AwtFont` is the AWT font described above. `BitmapFont` draws glyphs cut from images, to preview an engine whose font is a bitmap:

```java
--8<-- "swing/SwingExamples.java:bitmap"
```

Each sheet is split into a grid, one character per cell, row by row, with `U+0000` for an empty cell. A white glyph on a transparent ground draws in the text's color. A glyph advances by its rightmost column with any opaque pixel, plus one, scaled from the cell to the glyph's height and rounded, plus one unit of spacing; a cell with no opaque pixel advances one unit. `advance(codePoint, width)` gives a character, usually the space, a fixed advance with nothing drawn. Bold draws each glyph again one unit to the right and adds a unit to every advance, and italic slants glyphs. Characters with no glyph come from a fallback font, the logical Dialog font unless the builder is given another. The first sheet or advance that names a character wins.

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

`SwingSprites` loads images from the classpath at `assets/<namespace>/sprites/<path>.png`. Pass a `SpriteResolver` to `SwingSprites`, and that to `StrataPanel` or `SwingSnapshot.render`, to read another layout. For images that are not on the class path at all, such as files in a folder or a zip, give `SwingSprites` a `SpriteSource`, which returns the image for an id. Images are loaded once and cached, including the fact that one is missing, until `SwingSprites.clear()`. A missing or unreadable image draws as solid magenta.

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

`SwingPlatform` uses the system clipboard. Reads and writes that fail, because another program holds the clipboard or it holds no text, read as empty and are dropped. In a headless JVM there is no clipboard at all.

## Rendering without a display

`SwingSnapshot.render` opens a screen on a fresh stack, draws one frame into an image and closes it again:

```java
--8<-- "swing/SwingExamples.java:snapshot"
```

Any pixel the screen does not draw stays transparent. The screen's `closed()` runs before `render` returns, and a theme missing a required key throws `ThemeException`. Overloads take input to send before the frame is drawn, such as a click that opens a drop-down, with tooltips shown at once; a `SwingSprites`, for images from elsewhere; a `SwingPlatform`, for snapshots with other fonts; and a `ThemeRef`, resolved from that platform's [theme source](#themes). Set `-Djava.awt.headless=true` on build servers. The adapter's own tests and the gallery's `--snapshot` option use it.

Snapshots are a good way to test layouts: render a screen and compare pixels, or check that it renders at all with each of your themes.

## Hosting Strata in other Java2D code

`StrataPanel` is the usual way in. To draw Strata inside other Java2D code, such as a game loop of your own, use the pieces it is made of:

- `StrataCore.createScreenStack(platform, theme)` or `createScreenHost` from `core`, with a `SwingPlatform`.
- A `SwingCanvas` made with the platform's `SwingTextMetrics` and a `SwingSprites`. Call `begin(graphics)` at the start of every frame, with a `Graphics2D` whose transform maps UI units to pixels, then `render(canvas)` on the host, then show the cursor `requestedCursor()` returns.
- `SwingInput` maps AWT key events, mouse buttons and modifier masks to Strata's types, and `HeldKeys` from the SPI turns AWT's repeated presses into repeats.

[Writing an adapter](writing-an-adapter.md#driving-the-host) lists what the host expects of the code that drives it.

## The gallery

The Strata-Gallery repository runs on this adapter. It gives every control a page, written against the API only, with a log of the events the control fires:

```bash
./gradlew run
./gradlew run --args="--snapshot gallery"
```
