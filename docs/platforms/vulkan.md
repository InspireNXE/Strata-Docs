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
- A game renders every frame. An application that renders only when something changes renders when `screens().needsFrame()` is true or the platform's clock reaches `screens().nextFrameNanos()`; see [Rendering on demand](writing-an-adapter.md#rendering-on-demand).
- The platform's UI thread is the thread that renders. Without an executor set on the platform's builder, tasks handed to `Strata.uiThread()` run at the start of the next frame.

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

## Other windows

With another windowing library, send input to `strata.screens()` yourself and give the view a `VulkanWindow`, which shows cursors and the title:

```java
--8<-- "vulkan/VulkanExamples.java:other"
```

The view calls `showCursor` and `showTitle` at the end of a frame, only when they change. Close buttons call `screens().requestClose()` and close the window only when it returns true.

## Drawing

The canvas draws every shape call itself, so `VulkanPlatform` reports `Capabilities.drawsShapes`. Rounded rectangles, outlines, ellipses and soft shadows are signed distance fields in the fragment shader, anti-aliased over one device pixel. Lines, polylines, arcs, shapes and their strokes are flattened, stroked with Java2D's joins and caps and its miter limit of 10, and rasterized on the host into a coverage mask by the nonzero rule. Gradients at an angle are split into bands between their stops. All of these turn hard-edged inside `smoothing(false)`.

Fills, gradients, sprites and pixel buffers snap their edges to the nearest device pixel, as clips do, so pieces that share an edge meet at any scale. Clips are scissor rectangles. Round clips are anti-aliased, and only the innermost one rounds its corners: inside another round clip, the outer one clips to its rectangle. Opacity multiplies into every draw, text and visuals included. Draws keep the order Strata asks for.

A pixel buffer becomes a texture, copied again when its version moves and dropped when no view has drawn it for a while. It is sampled nearest, so pixel art stays sharp.

## Fonts

Text is drawn with FreeType. `VulkanTextMetrics.defaults()` looks for the operating system's fonts at 9 units: Segoe UI and Consolas on Windows, Arial and Menlo on macOS, and DejaVu, Noto or Liberation on Linux, with the system's symbol and CJK fonts as fallbacks where it finds them. It throws `IllegalStateException` when none is installed. Any TrueType or OpenType file works through `VulkanFont`:

```java
--8<-- "vulkan/VulkanExamples.java:fonts"
```

A family has a regular face and optional bold, italic and bold italic faces; `collection(path)` takes them from a `.ttc` by their style flags. A missing style is made by thickening or slanting the nearest face, and a character no face has comes from the fallbacks in order, else draws as the regular face's missing glyph box. The size is the em square's height in UI units.

Widths are the fonts' unhinted advances plus the kerning between neighbors from the font's `kern` table, so text measures the same at every scale. Glyphs are drawn with light hinting at the size they land on screen, from a glyph atlas the renderer keeps. A synthetic bold glyph is a 24th of the em wider. Underline, strikethrough and the shadow, the text again one unit down and to the right at a quarter of its brightness, are drawn by the adapter and do not change widths.

The adapter does not shape text. Each character draws its own glyph, so there are no ligatures, and `VulkanPlatform` does not report `Capabilities.shapesRightToLeft`: core reverses right-to-left runs, which reads correctly for Hebrew and draws Arabic unjoined.

## Sprites and images

`VulkanSprites` loads images from the class path at `assets/<namespace>/sprites/<path>.png`, as the Swing adapter does, with denser images beside them as `@2x` and `@1.5x` files, chosen where one unit covers that many device pixels. A `SpriteResolver` names another layout, and a `SpriteSource` returns the bytes of an image file from anywhere. Files are decoded with stb_image, which reads PNG, JPEG, BMP, TGA and GIF. Images are read once and cached, a missing one included, until `clear()`, after which the renderer replaces their textures. A missing image draws as solid magenta. `StrataVulkan.setSprites` swaps an open view's images.

Sprites are sampled nearest and tints multiply each channel. A nine-slice's borders are in the plain image's pixels and shrink in proportion when the rectangle is smaller than them. Strata draws sized nine-slices and tiled sprites itself, from parts of the image.

`renderer.image(imageView, width, height)` makes a [visual](../guides/appearances.md#visuals-and-icons) of an image the application owns, such as a render target it draws a minimap or a preview into:

```java
--8<-- "vulkan/VulkanExamples.java:image"
```

The visual is live: each frame samples what the image holds when the frame executes. Keep the image in `VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL` whenever a frame that draws it runs. A second form takes a sampler of your own; the default samples linearly. The canvas draws a visual another renderer made as magenta.

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

## Testing

The adapter's tests draw on a real device into an image and read it back, and pass Strata's canvas, platform and text metrics contracts with the shape and pixel buffer checks on. On a machine without a GPU, Mesa's software rasterizer runs them.

## Not supported

- File dialogs. `ComponentFactory.fileDialogs()` reports them as not supported.
- Text shaping: ligatures, joined Arabic and other complex scripts.
- An input method or on-screen keyboard signal; `TextInputSink` is `NONE` unless the application gives one.
- Touch and pen input through the GLFW bridge, which sends mouse input only.
- Drags into the window with their contents before the drop, and drags out of it.
