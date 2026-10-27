# Vulkan

The Vulkan adapter draws Strata screens into a frame that a Java application renders with Vulkan through LWJGL. It owns no window and no swapchain. The application gives it its device and queue, begins a render pass or dynamic rendering as it does for its own drawing, and asks Strata to record its draws there. That makes it fit a game engine, a tool with a 3D view, or any other Vulkan renderer, with the UI drawn over the scene in the same pass. An optional bridge connects the adapter to a GLFW window for input, cursors, the title and the close button.

## Showing screens

Three objects come into play: a `VulkanRenderer` for the device, a `VulkanPlatform` with the fonts, and a `StrataVulkan` view that holds a stack of screens. With a GLFW window:

```java
--8<-- "vulkan/VulkanExamples.java:setup"
```

| Constructor argument | Meaning |
|---|---|
| `VulkanPlatform` | Text metrics, clipboard, clock and [theme source](#themes-and-translations). Its text metrics also draw the text. |
| `VulkanRenderer` | The device resources, for the target of the passes the view draws in |
| `Theme` | The theme for every screen pushed |
| `scale` | Framebuffer pixels per UI unit, which must be positive. `setScale` changes it later; the GLFW bridge sets it from the zoom and the window's content scale. |

`strata.screens()` is the [screen stack](../guides/screens.md#stacks-of-screens) and the view's `ScreenController`, as on Swing. `strata.setDefault()` marks the view whose stack `Strata.screens()` returns. Until a view is marked, `Strata.screens()` throws `IllegalStateException`. `open(screen)` shows a screen in the platform's default theme, `strata:flat` until the platform's `VulkanThemeSource` names another.

Then, every frame, inside the pass:

```java
--8<-- "vulkan/VulkanExamples.java:frame"
```

`render` builds pending screens, lays them out and draws them, submits the texture and glyph uploads the frame needs to the renderer's queue, and records the draws into the command buffer. Submit that command buffer to the same queue afterwards. Before closing, wait for the device to finish the frames, then close the view and the renderer:

```java
--8<-- "vulkan/VulkanExamples.java:shutdown"
```

## The renderer

`VulkanRenderer` takes the device, a graphics queue and its family, a `VulkanTarget` and the number of frames the application keeps in flight. The device must be created through LWJGL, so its functions are loaded, and it needs no extension or feature beyond dynamic rendering for a dynamic target. One renderer serves any number of views that draw into passes of the same target.

| What the application promises | Why |
|---|---|
| The queue is not used on another thread while `render` runs | Strata submits its uploads to it |
| The command buffer Strata recorded into is submitted to the same queue | Submission order makes the uploads land before the draws that sample them |
| When a view renders for the n-th time, the device has finished that view's frame from `framesInFlight` renders before | Each frame in flight has its own vertex, coverage and staging buffers, reused without waiting. Waiting on a frame's fence before recording it again, as most renderers do, keeps this. |
| The pass covers the view's bounds | Strata draws only inside them |

`VulkanTarget` describes the pass, and the renderer builds its one pipeline for it:

```java
--8<-- "vulkan/VulkanExamples.java:targets"
```

`VulkanTarget.dynamic(format)` is dynamic rendering with one color attachment, and its other form names depth and stencil formats the pass has, which Strata does not test or write but its pipeline must name. `VulkanTarget.renderPass(renderPass, subpass, srgb)` is a subpass of a render pass. Strata draws to color attachment 0 and blends premultiplied colors over what is there. When the attachment encodes to sRGB, Strata converts its colors to linear first, so they come out as given; a dynamic target reads this from the format, and a render pass target is told.

`render` leaves Strata's pipeline, viewport, scissor, vertex buffer, descriptor sets and push constants bound, so bind your own again before drawing after it.

## How Vulkan maps onto Strata

- The view fills a rectangle of the framebuffer, set with `setBounds(x, y, width, height)` in framebuffer pixels. One UI unit is `scale` framebuffer pixels, which is also the screens' density, so components and sprites see the display's real pixels.
- Input goes to `strata.screens()`, which takes positions in UI units. `strata.unitX` and `unitY` convert framebuffer pixels. Input Strata does not use comes back as `false`, so the application can pass it on to its own controls or camera.
- A game renders every frame. An application that renders only when something changes renders when `screens().needsFrame()` is true or the platform's clock reaches `strata.nextFrameNanos()`, which also counts the next [repeating task](#repeating-tasks), and no sooner than `screens().frameIntervalNanos()` after its last render began, so the screens draw at most `screens().frameRate()` times a second, 30 unless set; see [Rendering on demand](writing-an-adapter.md#rendering-on-demand). Such an application can also redraw only what changed, below.
- The platform's UI thread is the thread that renders. Without an executor set on the platform's builder, tasks handed to `Strata.uiThread()` run at the start of the next frame.

## Redrawing only what changed

An application that keeps its image between frames, such as a tool or an editor, need not redraw the whole view when one control changes. `prepare()` draws the screens and compares the frame with the view's last one. The `VulkanFrame` it returns holds the damage, at most four rectangles of framebuffer pixels that do not overlap, and records only the draws that touch them:

```java
--8<-- "vulkan/VulkanExamples.java:kept"
```

- Call `prepare()` before the pass begins, since the damage decides the render area. When nothing changed, the damage is empty and the frame takes nothing from the device: record, submit and present nothing.
- Otherwise draw over the kept image, loaded rather than cleared, with the damage's bounds as the render area. If you draw a background under Strata, clear the damage rectangles to it first with `vkCmdClearAttachments`. Then `record` the frame and submit it to the renderer's queue, as with `render`.
- Vulkan does not promise that a swapchain image keeps its pixels from one present to the next, so copy the kept image to the swapchain image before presenting. Where the device has `VK_KHR_incremental_present`, name the damage rectangles in `VkPresentRegionsKHR`, so the compositor updates only those.
- A primitive changes when anything Strata draws it with changes: its place, color, texture or clip, or the pixels of its texture, such as a pixel buffer's. An image of the application's own, made with `renderer.image`, can change at any time, so its area is damaged every frame.
- The first frame, the frame after the bounds or the scale change, and the frame after a `render` damage the whole view. Call `invalidate()` when the kept image is lost, such as after making the swapchain again.
- Record a frame once, before the view prepares or renders another.

An application that clears its image every frame, as most games do, gains nothing from this and calls `render`.

The gallery draws this way: on its progress bar page at 1750 by 1125 pixels, a frame redraws the moving bars and leaves the rest of the window alone.

## GLFW

`StrataGlfw.attach(window, strata, zoom)` connects a view to a GLFW window:

| Window | What the bridge does |
|---|---|
| Keys, characters, pointer, buttons, wheel | Sent to the screens, positions converted from window coordinates to framebuffer pixels to UI units. GLFW reports key repeats itself, which `StrataGlfw.platform` tells the platform. Control characters are dropped, and Shift with the vertical wheel scrolls horizontally. |
| Callbacks the window had | Kept. Input Strata does not use goes on to them; pointer movement, the pointer leaving, size and scale always do. Install your callbacks before attaching, or they replace Strata's. |
| Framebuffer size and content scale | The view covers the framebuffer, and its scale is the zoom times the content scale, followed when the window changes display |
| Files dropped on the window | One `DROP` at the pointer, with no drag before it |
| Cursors | GLFW's standard cursors, for the kinds GLFW has; the others show their fallback |
| Title and close button | After `manageWindow(title)`, the title follows the top screen's and the close button asks the screens with `requestClose` first. The title given is shown while the top screen has none. |
| Clipboard | `GlfwClipboard`, text only, which `StrataGlfw.platform` gives the platform |

`close()` puts back the callbacks the window had. An application that keeps its own callbacks makes the bridge with `create` and forwards what Strata should see:

```java
--8<-- "vulkan/VulkanExamples.java:forward"
```

The bridge needs `org.lwjgl:lwjgl-glfw`, which the adapter does not bring.

The bridge also installs a window focus callback: while the window is in the background its screens are inactive, so focus leaves the focused component and comes back with the window. See [When the host is inactive](../guides/input-focus.md#when-the-host-is-inactive).

## Other windows

With another windowing library, send input to `strata.screens()` yourself and give the view a `VulkanWindow`, which shows cursors and the title:

```java
--8<-- "vulkan/VulkanExamples.java:other"
```

The view calls `showCursor` and `showTitle` at the end of a frame, only when they change. Close buttons call `screens().requestClose()` and close the window only when it returns true.

## Drawing

The canvas draws every shape call itself, so `VulkanPlatform` reports `Capabilities.drawsShapes`. Rounded rectangles, outlines, ellipses and soft shadows are signed distance fields in the fragment shader, anti-aliased over one device pixel. Lines, polylines, arcs, shapes and their strokes are flattened, stroked with Java2D's joins and caps and its miter limit of 10, and rasterized on the host into a coverage mask by the nonzero rule. Gradients at an angle are split into bands between their stops. All of these turn hard-edged inside `smoothing(false)`.

Fills, gradients, sprites and pixel buffers snap their edges to the nearest device pixel, as clips do, so pieces that share an edge meet at any scale. Clips are scissor rectangles. Round clips are anti-aliased, and only the innermost one rounds its corners: inside another round clip, the outer one clips to its rectangle. Opacity multiplies into every draw, text and visuals included. Draws keep the order Strata asks for.

A pixel buffer becomes a texture, copied again when its version moves and dropped when no view has drawn it for a while. It is sampled nearest, so pixel art stays sharp, and linearly inside `sampled(Sampling.LINEAR, ...)` or for an image set to `LINEAR`, through a second descriptor set the texture makes the first time.

### Frosted glass

`StrataVulkan.setBackdrop(image)` gives the view a `VulkanImage` of the application's, such as its scene rendered to a texture, stretched over the whole view, and `VulkanPlatform` reports `Capabilities.blursBackdrop`. A [backdrop](../guides/theming.md#frosted-glass) samples the image under its rectangle in the fragment shader, 13 by 13 taps of a Gaussian spread over the blur, cuts it to the rounded corners, fades it with the opacity and fills the tint over it as a rounded rectangle. The view records into the application's pass and never reads what it drew itself, so the glass blurs the image only, not Strata's own panels behind it. The image is sampled as it is when the frame executes, so a scene that moves shows through; frames damage the glass's area every time, as for any application image, and a new image keeps the view from repeating its last frame. Without an image, a backdrop draws its tint. Keep the image in `VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL` while frames that draw it run, and close it only after giving the view another or `null`.

The blur reads the image by its [encoding](#sprites-and-images), as a drawn image is read, so the glass is as bright as the scene under it. A scene tone-mapped into an image of the swapchain's `_SRGB` format is `ImageEncoding.LINEAR`; made without an encoding, its glass comes out far darker than its tint explains.

```java
VulkanImage scene = renderer.image(sceneView, ImageEncoding.forFormat(sceneFormat), width, height);
strata.setBackdrop(scene);
panel.setBackground(Paint.backdrop(8, 0xA0181A1F, 6));
```

## Fonts

`VulkanTextMetrics` measures and draws with a `VulkanFont` for ordinary text, one for monospaced text, and any the application registers under a `FontId`. A font is a `FreeTypeFont`, read from font files with FreeType, or a `BitmapFont`, cut from images.

`VulkanTextMetrics.defaults()` looks for the operating system's fonts at 9 units: Segoe UI and Consolas on Windows, Arial and Menlo on macOS, and DejaVu, Noto or Liberation on Linux, with the system's symbol and CJK fonts as fallbacks where it finds them. It throws `IllegalStateException` when none is installed. Any TrueType or OpenType file works through `FreeTypeFont`:

```java
--8<-- "vulkan/VulkanExamples.java:fonts"
```

### Registered families and sizes

An application, or a plugin it loads later, registers a family under the id its styles name, at any time on the render thread:

```java
--8<-- "vulkan/VulkanExamples.java:register"
```

`register` returns the family it replaced, and `unregister` the one it removed; the metrics never close a font, so close it when nothing draws with it any more. A registered family wins over `monospace`. A style that names an id nobody registered draws in the ordinary font, or the monospaced one for a monospaced style. Registering, replacing or removing a family changes `version()`, so open screens lay out again.

Every font has its own size, the one it was built at. A style's size scales the chosen font from it, and size 0 is the default size, the ordinary font's (`defaultSize()`), so a registered family built at 12 units draws at 9 until a style asks for another size. The canvas draws a scaled span under a scale, so FreeType renders the glyphs at the size they land on screen, sharp, and the shadow offset and the lines grow with the text. A `BitmapFont`'s size is its line height.

### Faces, widths and bitmap fonts

A family has a regular face and optional bold, italic and bold italic faces; `collection(path)` takes them from a `.ttc` by their style flags. A missing style is made by thickening or slanting the nearest face, and a character no face has comes from the fallbacks in order, else draws as the regular face's missing glyph box. The size is the em square's height in UI units.

Widths are the fonts' unhinted advances plus the kerning between neighbors from the font's `kern` table, so text measures the same at every scale. Glyphs are drawn with light hinting at the size they land on screen, from a glyph atlas the renderer keeps. A synthetic bold glyph is a 24th of the em wider. Underline, strikethrough and the shadow, the text again one unit down and to the right at a quarter of its brightness, are drawn by the adapter and do not change widths.

`BitmapFont` measures and draws as the Swing adapter's bitmap font does, so a look built on a pixel font lays out the same on both adapters. A sheet is an image split into a grid, one character per cell, given as an image file's bytes or a `PixelBuffer`:

```java
--8<-- "vulkan/VulkanExamples.java:bitmap"
```

A glyph advances by its rightmost column with any opaque pixel, plus one, scaled from the cell to the glyph's height and rounded, plus one unit of spacing; a cell with no opaque pixel advances one unit. `advance(codePoint, width)` gives a character a fixed advance with nothing drawn. When several sheets or advances name a character, the first one added wins, so add a font's parts in the order its own files list them. Glyphs draw in the text's color through their pixels' alpha, scaled without smoothing to the size they land on screen, with each cell's top snapped to the pixel grid. Bold draws each glyph again one unit to the right and adds a unit to every advance, and italic slants glyphs. Characters with no glyph come from the fallback, the system's sans-serif font at the line height unless the builder is given another.

The adapter does not shape text. Each character draws its own glyph, so there are no ligatures, and `VulkanPlatform` does not report `Capabilities.shapesRightToLeft`: core reverses right-to-left runs, which reads correctly for Hebrew and draws Arabic unjoined.

## Sprites and images

`VulkanSprites` loads images from the class path at `assets/<namespace>/sprites/<path>.png`, as the Swing adapter does, with denser images beside them as `@2x` and `@1.5x` files, chosen where one unit covers that many device pixels. A `SpriteResolver` names another layout, and a `SpriteSource` returns the bytes of an image file from anywhere. Files are decoded with stb_image, which reads PNG, JPEG, BMP, TGA and GIF. Images are read once and cached, a missing one included, until `clear()`, after which the renderer replaces their textures. A missing image draws as solid magenta. `StrataVulkan.setSprites` swaps an open view's images.

Sprites are sampled nearest, or linearly when asked as pixel buffers are, and tints multiply each channel. A view gives its screens `VulkanSprites.size`, the plain image's size in pixels, so an [image](../guides/controls.md#image) showing a sprite takes that size until one is set, and gives it again after `clear()`. A nine-slice's borders are in the plain image's pixels and shrink in proportion when the rectangle is smaller than them. Strata draws sized nine-slices and tiled sprites itself, from parts of the image.

`renderer.image(imageView, width, height)` makes a [visual](../guides/appearances.md#visuals-and-icons) of an image the application owns, such as a render target it draws a minimap or a preview into:

```java
--8<-- "vulkan/VulkanExamples.java:image"
```

The visual is live: each frame samples what the image holds when the frame executes. Keep the image in `VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL` whenever a frame that draws it runs. A second form takes a sampler of your own; the default samples linearly. The canvas draws a visual another renderer made as magenta.

Strata needs to know what the shader reads when it samples the image, since it blends in linear light on an sRGB target and in encoded values on any other. A `_UNORM` image holding sRGB bytes gives back encoded values, `ImageEncoding.ENCODED`, which is what the forms of `renderer.image` without an encoding assume. The device decodes a `_SRGB` image as it samples it, so that image gives back linear values, `ImageEncoding.LINEAR`, and so does a floating-point image of linear light. Pass the encoding to `renderer.image(imageView, encoding, width, height)` or to the form that also takes a sampler; `ImageEncoding.forFormat(format)` gives the usual one for a view's format. Taken as encoded, a `_SRGB` image is decoded twice and shows too dark on an sRGB target, and a linear image taken as encoded shows too light on a `_UNORM` target. The textures Strata makes for sprites, pixel buffers and glyphs are `_UNORM` images of sRGB bytes, which Strata always reads as encoded.

`VulkanVisuals.image(pixels)` makes a visual of a `PixelBuffer`, at one unit per pixel or at a size of its own, as `SwingVisuals.image` does for a `BufferedImage`. Any renderer draws it, as a pixel buffer:

```java
--8<-- "vulkan/VulkanExamples.java:visuals"
```

## Rendering without a window

`VulkanSnapshot.render` opens a screen on a fresh stack, draws one frame on a Vulkan device of the adapter's own with no window, and closes it again, as `SwingSnapshot` does with Java2D:

```java
--8<-- "vulkan/VulkanExamples.java:snapshot"
```

It returns the frame as a `PixelBuffer`. Any pixel the screen does not draw stays transparent, and the screen's `closed()` runs before `render` returns. Overloads take input to send before the frame is drawn, with tooltips shown at once; a `VulkanPlatform` and `VulkanSprites`, for other fonts and images; and a `ThemeRef`, resolved from the platform's theme source. The first snapshot finds a device with graphics and dynamic rendering, a discrete one where there is one, and keeps it for the life of the process. `VulkanSnapshot.isSupported()` says whether the machine has one; Mesa's software rasterizer counts, so build servers can take snapshots too.

## Themes and translations

`VulkanThemeSource` is the platform's [theme source](../guides/theming.md#themes-by-id), and works as the Swing adapter's: theme files from `assets/<namespace>/themes/<path>.json` on the class path, `register` for base themes and base files, `setDefaultTheme`, `reload` and `resolve`. Pass your own to the platform's builder with `themes(...)`.

`VulkanTranslations.use(bundle)` reads translated text from a `ResourceBundle`, and `use(translator)` takes any `Translator`, as on Swing.

## The platform

`VulkanPlatform.builder(metrics)` sets what Vulkan cannot know:

| Setting | Default |
|---|---|
| `clipboard` | `Clipboard.NONE`; `GlfwClipboard` with GLFW |
| `sendsKeyRepeat` | false, so the host makes repeats; true with GLFW |
| `clock` | `Clock.SYSTEM` |
| `textInput` | `TextInputSink.NONE` |
| `conventions` | Command for shortcuts on macOS, Control elsewhere; buttons act on release; mnemonics on |
| `themes` | A `VulkanThemeSource` on the adapter's class loader |
| `uiExecutor`, `uiThreadCheck` | None: tasks run at the start of the next frame, and the UI thread is the first one that renders |
| `scheduler` | A `VulkanScheduler`, which runs screens' repeating tasks as the views render |

## Repeating tasks

Vulkan has no loop of its own, so by default a `VulkanScheduler` runs the tasks of `ScreenHandle.every`: `render` and `prepare` run the tasks that are due, by the platform's clock, before they draw. `strata.nextFrameNanos()` counts the next task's time, so an application that renders on demand wakes for it. When frames come further apart than a task's interval, the runs it missed are dropped and the next comes one interval after the one that ran.

An application with a scheduler of its own, such as a game loop's, passes it to `VulkanPlatform.builder(metrics).scheduler(...)`. It must run tasks on the render thread and follow the SPI's `Scheduler` rules, and the view then leaves tasks to it.

## Testing

The adapter's tests draw on a real device into an image and read it back, and pass Strata's canvas, platform and text metrics contracts with the shape and pixel buffer checks on. On a machine without a GPU, Mesa's software rasterizer runs them.

## The gallery

Strata-Gallery runs on this adapter as well as on Java2D. `./gradlew runVulkan` opens it in a GLFW window drawn with Vulkan, the whole host an application writes around Strata: an instance, a surface, a device, a swapchain and a loop that renders when the screens need a frame. `--renderer vulkan --snapshot <directory>` renders every page through `VulkanSnapshot`.

## Not supported

- File dialogs. `ComponentFactory.fileDialogs()` reports them as not supported.
- Text shaping: ligatures, joined Arabic and other complex scripts.
- An input method or on-screen keyboard signal; `TextInputSink` is `NONE` unless the application gives one.
- Touch and pen input through the GLFW bridge, which sends mouse input only.
- Drags into the window with their contents before the drop, and drags out of it.
