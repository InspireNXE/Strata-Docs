# Writing an adapter

An adapter runs Strata on an engine. It implements the SPI's `Platform`, with the clipboard, clock, text input sink and theme source a platform returns, and its `Canvas`. When it starts it hands core a `Translator` and a screen controller, then it creates a host from `core` and forwards the engine's frames and input to it. This page builds a complete adapter for an imaginary engine. Its code is compiled and passes Strata's contract tests as part of this site's build.

You need this only to bring Strata to a new engine: a UI toolkit such as JavaFX, LWJGL directly, libGDX, a game of your own, or an ImGui backend. To use Strata with an adapter that already exists, see the [Platforms](index.md) page instead.

## What an adapter depends on

```groovy
dependencies {
    implementation 'org.inspirenxe:strata-api:0.1.0-SNAPSHOT'
    implementation 'org.inspirenxe:strata-spi:0.1.0-SNAPSHOT'
    implementation 'org.inspirenxe:strata-core:0.1.0-SNAPSHOT'
    // The contract tests
    testImplementation testFixtures('org.inspirenxe:strata-spi:0.1.0-SNAPSHOT')
}
```

`core` is the one place an adapter touches the implementation, through `StrataCore`. Screens written for your engine still compile against `api` only.

## Rules for every adapter

- Call hosts on one thread, the engine's UI or render thread, and tell core which thread that is (see [The UI thread](#the-ui-thread)). Strata does no locking.
- Coordinates are UI units with the origin at the top left of the host's area. Choose what a unit is, such as screen pixels or scaled pixels, and convert drawing and input the same way.
- Never let engine types into Strata calls. Map keys to `Key`, buttons to `MouseButton`, modifiers to `Modifiers`.
- Tell hosts how many device pixels a unit covers whenever that changes (see [Density](#density)).
- Run the contract tests.

## Text metrics

`TextMetrics` measures text the way your engine draws it. Its requirements, from the Javadoc:

- A span drawn at x must end at x plus its measured width, give or take a unit of rounding. Text boxes place the caret from these numbers.
- The same span always gives the same result, with no side effects.
- Width is 0 for an empty span and never negative.
- Appending a code point never makes a span narrower.
- Line height is positive.
- `ascent(style)` is positive and no more than the line height. It is the distance from the top of a line to its baseline, the line the letters sit on, and panels line up the text of their children by it (`VerticalAlignment.BASELINE`). The default is four fifths of the line height, which suits most fonts; return the font's own ascent where the engine exposes it, so a font from a resource pack or a style that changes the size follows. The contract checks that it is positive and within the line.
- Be fast, or cache: layout measures every label line, and a text box measures every prefix of its text.

```java
--8<-- "adapter/MonospaceTextMetrics.java:metrics"
```

## The platform

`Platform` is everything a host needs besides drawing:

```java
--8<-- "adapter/ToyPlatform.java:platform"
```

| Method | Requirements |
|---|---|
| `textMetrics()` | Must match what the canvas's `text` draws |
| `clipboard()` | Never throws. A busy or missing clipboard reads as empty and ignores writes. Use `Clipboard.NONE` if the engine has none. Text written reads back unchanged until something else changes it. An engine that holds more than text also overrides `getTransfer()` and `setTransfer(Transfer)`; see [The clipboard](#the-clipboard). |
| `clock()` | Monotonic, never backward, advancing in real time. `Clock.SYSTEM` is `System.nanoTime`. Click counting, caret blinking and synthesized key repeat stall on a clock that stops. `wall()` returns the `java.time.Clock` that today is read from, such as the day a calendar marks; the default is the system clock in the default zone. Return the engine's clock or zone if it has one, or a fixed clock in tests. Core reads it at most about once a minute per control and never accepts null. |
| `textInput()` | Told when a text field gains and loses focus. Core calls `start(area, kind)`, whose default forwards to `start(area)`; for `TextInputKind.SECRET`, turn off input method composition, prediction and on-screen keyboard suggestions where the engine allows, and never log what is typed. `start` and `stop` may arrive repeatedly; a second `start` moves the input area and may change the kind. After `start`, the adapter must forward typed characters. Use `TextInputSink.NONE` if the engine always sends characters. |
| `capabilities()` | Constant for the platform's life. `sendsKeyRepeat` is true when the adapter delivers `REPEAT` itself. `monospaceFont` is true when monospaced styles get a font whose characters all have the same width; otherwise they use the normal font, measured and drawn alike. `slicesSprites` is true when the engine stores slicing with its images and applies it in `sprite` as well as `nineSlice`; the host then passes sized nine-slices and tiled sprites to the canvas whole. When it is false, the host draws them from `spriteRegion` calls, one per piece. Its default draws the whole image with `sprite` inside a clip; override it to draw part of an image directly, which avoids seams between pieces at scales that are not whole numbers. `drawsShapes` is true when the canvas implements every shape call; see [Shapes](#shapes). `shapesRightToLeft` is true when the engine orders and joins right-to-left text itself; see [Right-to-left text](#right-to-left-text). The two-, three- and four-argument constructors leave the later flags false. |
| `conventions()` | Optional. How the engine's own controls behave, for the behaviors a theme does not reach, so Strata's controls match them. Constant for the platform's life; `Conventions.DEFAULT` is `(false, CONTROL, true)` unless overridden. `activateOnPress` is true when the engine's buttons act as soon as the left pointer button goes down on them; buttons, cycle buttons, checkboxes, radio buttons, toggle switches and tabs then act on the press. When false, they act on the click, when the button comes back up still over them, and moving off first cancels. Keyboard activation and drop-down buttons do not change. `shortcutModifier` is the modifier that `KeyStroke.shortcut` strokes hold and that text fields use for copy, cut, paste, select all, undo and redo: `Modifier.CONTROL` on most platforms, `Modifier.SUPER` where shortcuts use Command, which the adapter must then send as `Modifier.SUPER` in its modifiers. An adapter that cannot tell reports Control. `mnemonics` is true when holding Alt underlines the marked letters of captions, Alt with a letter acts on the control that has it, and Alt or F10 alone moves to a menu bar. An engine that binds Alt for its own use reports false, and captions then show their text without the markers. Looks belong in the adapter's theme files, which can set the [metric](../guides/theming.md#metrics) keys as well as paints, text styles and insets. |
| `themes()` | Where themes named by id come from; see [Themes](#themes). Use `ThemeSource.NONE` if the engine has no themes or packs. |
| `causes()` | Optional. The engine's own cause stack, when it has one Strata's [causes](../guides/events.md#who-caused-it) should follow; empty by default, and core then keeps its own, one per thread. A stack returned here keeps frames for the UI thread, closes them as `CauseFrame` states, and answers `current()` and `context` from the frames open at the call. Hosts push their input frames on it and developers get it from `ComponentFactory.causes()`. |
| `uiExecutor()` | Optional. The engine's executor for its UI thread; see [The UI thread](#the-ui-thread). |
| `uiThreadCheck()` | Optional. A cheap test that answers whether the calling thread is the UI thread; see [The UI thread](#the-ui-thread). |
| `fileDialogs()` | Optional. The engine's file dialogs as a `FileDialogSource`; see [File dialogs](#file-dialogs). Empty by default, and core then reports file dialogs as not supported. |
| `user()` | Optional. The engine's object for the person at the screen, such as a user profile, which hosts put in every `Origin.User` so handlers can tell who acted. Empty by default, and the identity is then null. Return the same object every time. |

Return the same objects every time; hosts may keep what they get. Hosts may also share one platform: a screen stack hands its own to the host of every screen in it.

### Themes

`ThemeSource` only finds things. Core owns the theme file format, the parent chain, the layers and the merge, and the overrides code adds with `Strata.themes()`. Its methods:

| Method | Returns |
|---|---|
| `base(id)` | A theme the adapter builds in code, such as the engine's look drawn with its widget images. Core registers `strata:flat` itself. |
| `bases(id)` | The [base files](../guides/theming.md#base-files) of a theme the adapter writes as JSON, lowest priority first. Empty by default. |
| `documents(id)` | Every theme file the engine's packs hold for the id, lowest priority first, each a `ThemeDocument` with its origin for messages, its JSON and its layer name |
| `defaultTheme()` | The theme screens open in when none is given, `strata:flat` by default |
| `version()` | A number that changes whenever any of the above may return something new |

This source gives the engine's look an id, reads theme files from the user's packs, and makes the engine's look the default:

```java
--8<-- "adapter/ToyThemeSource.java:themes"
```

Never throw for an id you know nothing of; return empty. Leave out a file you cannot read. An id must not have both a `base` and base files, and core throws `ThemeException` when it resolves one that has. A base file that cannot be parsed is a bug in the adapter, so it makes the theme fail to resolve, while a broken document from a pack is logged and skipped. Core applies the documents in the order returned, so the adapter decides where the player's packs and each project's files fall. Keep `version()` in a volatile or atomic field, since the engine may reload on another thread, and keep it cheap, since hosts read it on every layout and render. `StrataCore.resolveTheme(source, ref)` resolves a ref once, which is handy in tests.

### The UI thread

Components change on the UI thread, the one thread that calls the host. Core checks the thread of changes to attached components and hands work from other threads to it through `UiThread` (`Strata.uiThread()` and the factory's `uiThread()`; see [Threading](../guides/threading.md)). It asks the platform for two things, and reads both once, on whichever thread first makes a host for the platform:

```java
--8<-- "adapter/ToyMainThread.java:thread"
```

- `uiExecutor()` accepts a task from any thread, runs tasks on the UI thread in the order given, and runs a task given on the UI thread after the current call into Strata returns, never inside it. Core wraps each task so that one that throws is logged. Without an executor, core queues the tasks and runs them at the start of the next `flush`, `layout` or `render` of any host of the platform, which suits every adapter that renders often. Supply one when work should run while nothing renders.
- `uiThreadCheck()` must be cheap, answer from any thread and not block, because core calls it on every change to an attached component. Without a test, core takes the first thread that dispatches input to, flushes, lays out or renders a host of the platform to be the UI thread, and until then treats every thread as passing the check and none as current.

A screen controller that is not a stack or host made by `StrataCore` must name its platform, so that `Strata.uiThread()` knows which platform's thread to use: call `StrataCore.setScreenController(controller, platform)` in place of the one-argument form, which finds the platform of a core stack or host by itself. `StrataCore.clearScreenController()` removes the controller and its platform, after which `Strata.screens()` and `Strata.uiThread()` throw `IllegalStateException`.

### The clipboard

An engine whose clipboard holds only text implements `get()` and `set(String)`. One that holds more also overrides the two transfer methods, which carry a `Transfer`: the same value that drags carry, with a value for each `DataFormat`.

- `setTransfer(Transfer)` puts the formats the engine can hold on the clipboard, replacing what it held. Core never passes a local format, which is one that stays inside the process. Styled text may go out as its plain text. Failures are ignored, as for `set`.
- `getTransfer()` reads every format the clipboard holds that the engine can read. A value that is costly to read, such as an image, can be supplied with `Transfer.Builder.supply` and read when a component asks.
- `get()` must still answer with the text `setTransfer` wrote. Core compares it with the text it saw after the write to tell whether the clipboard still holds what it set, and while it does, core hands back the whole transfer, local formats included. This is why an engine with a text-only clipboard keeps rows copied from a list for a paste in the same process.

Without the overrides, `setTransfer` puts the transfer's text on the clipboard with `set`, and `getTransfer` returns what `get` read as text.

### File dialogs

`fileDialogs()` returns a `FileDialogSource` for an engine with native dialogs, such as `java.awt.FileDialog` on a desktop. Its four methods, `open`, `openMany`, `save` and `folder`, each take a `FileDialog` with the title, the start, the suggested name and the filters, and return a `CompletableFuture`.

- Core calls them on the UI thread, so return at once: show the dialog without blocking the thread, or on another thread, and complete the future when the user chooses.
- The future may complete on any thread. Core hands its result to the UI thread before the application sees it.
- Complete with an empty result when the user cancels, and exceptionally only when the dialog failed.
- Honor the title, start, filters and the suggested name where the engine can. Filters the engine cannot show may be left out.

Return `Optional.empty()` for an engine with no dialogs, the default. `ComponentFactory.fileDialogs()` then answers `isSupported()` false and completes every request empty. See [File dialogs](../guides/files.md).

## Starting up

Some things are shared by the whole process, so the adapter hands them to core once, when the engine loads it and before any project code can run:

```java
--8<-- "adapter/ToyStartup.java:startup"
```

Translated text reads through one `Translator` for the whole process, which the adapter gives core with `StrataCore.setTranslator`. `translate(key)` returns the pattern for the current language as written, with its `%s`, `%1$s` and `%%`, or empty when there is none; core formats the arguments. `version()` changes whenever translations may read differently, and hosts lay everything out again when it does, so keep it cheap. Calls may come from any thread.

`locale()` returns the locale of the current language, which dates and times follow: month and weekday names, the first day of the week, the order of a date's parts and the 12 or 24 hour clock. Numbers follow it too, in the locale's digits and signs, for spinners and the number converters, and so do plural categories and the direction screens run in: a screen whose root sets none runs right to left when the language is written that way. The default is the JVM's default locale for formatting. Map the engine's language code to a `Locale` here, and change `version()` whenever the locale changes, since core caches what it reads per locale and version.

`plural(long)` gives the plural category of a count, which `Text.plural` picks its translation by (`key.one`, `key.few`, `key.other` and the like). The default applies the Unicode CLDR cardinal rules to `locale()`. Override it only when the engine's own text system has plural rules that Strata's text should agree with. Core calls it on every resolve, from any thread, and it must not return null.

An adapter gives its engine's types an [appearance](../guides/appearances.md) with `StrataCore.registerDefaultAppearance(type, appearance)` when it starts, so lists and images show them with icons, names and tooltips. Defaults sit beneath anything a project registers for the same type, whichever comes first.

An engine with its own text type can wrap it as Strata text with `StrataCore.text(source)`. A `TextSource` resolves the engine text to spans over a base style each time a control lays out, so engine translations stay live, and every `Text` operation works on it. Parts with no style of their own take the base, and a part that sets a property changes only that property of the base. `resolve` may be called from any thread and must not change the engine text. Implement `equals` and `hashCode` by the engine text, as a record does, so equal engine texts make equal Strata text.

The controller handed to `StrataCore.setScreenController` is what `Strata.screens()` returns; [Opening screens](#opening-screens) says what it must do. A controller that is not a core stack or host also names its platform, as [The UI thread](#the-ui-thread) says.

## The canvas

`Canvas` is what the engine draws for Strata:

```java
--8<-- "adapter/RecordingCanvas.java:canvas"
```

Requirements:

- Coordinates are UI units in the current transform. Round to the engine's pixel grid if it needs whole pixels, the same way for drawing and clipping.
- Colors are ARGB. Alpha 0 draws nothing; partial alpha blends over what is already drawn.
- Later calls draw over earlier ones. An engine that batches or sorts must keep this order, at least across `nextLayer()`.
- Calls may be recorded and replayed later. Nothing requires immediate drawing.
- Clips and transforms are stacks. Strata balances every push with a pop within a frame, in reverse order, even when a component throws. A clip applies in the transform current when it was pushed and intersects the clip already in place.
- Transforms are translations and uniform scales. A scale multiplies every later coordinate and size about the current origin, translations pushed inside it included, and text drawn while scaled comes out at the scaled size. Labels use it for their text scale.
- Never throw for missing resources. Draw something visible instead, so the problem shows on screen.
- `visual` draws the visuals your adapter makes, such as images or engine objects, and a placeholder for any other. Offer a public class that makes them, as `SwingVisuals` does, so code with an engine object in hand can show it through the API.
- A nine-slice smaller than its borders shrinks the borders rather than drawing outside its rectangle. An engine that knows a sprite's slicing natively may ignore the `border` argument.
- `text` puts the top-left of the line box at the point, not the baseline, applies every style the engine supports, and lines up with the platform's text metrics.
- `requestCursor` is called at most once per frame, after drawing, while the pointer is over a component or a drag carries a transfer. In a frame without a request, show the default cursor. See [Cursors](#cursors).
- A sprite id names one sprite, which an adapter may hold in several densities; see [Density](#density).
- `text` never gets a span that holds a [picture](../guides/text.md#pictures-in-text). Core measures pictures itself and draws them with `sprite` and `visual`, so the engine's metrics and canvas see only characters. Draw monospaced spans in the engine's monospaced font when `Capabilities.monospaceFont` is true.
- `spriteRegion` draws part of an image, given in fractions of the whole from 0 to 1. Core draws sized nine-slices and tiled sprites from it when `Capabilities.slicesSprites` is false. Its default draws the whole image inside a clip; override it when the engine can draw part of an image directly, or pieces drawn side by side may show seams at scales that are not whole numbers.

### Cursors

`showsCursor(kind)` says whether the canvas shows a `CursorKind` as itself. The host asks it before `requestCursor`, and asks for the kind's `fallback()` when the answer is false, walking down to `ARROW`, so every kind shows something close on every engine. `requestCursor` itself must accept every kind without throwing, and `showsCursor` must answer true for `ARROW`.

| Kind | Fallback |
|---|---|
| `RESIZE_DIAGONAL_DOWN`, `RESIZE_DIAGONAL_UP` | `RESIZE_ALL` |
| `GRAB`, `GRABBING` | `HAND` |
| `NO_DROP` | `NOT_ALLOWED` |
| The others, `WAIT`, `PROGRESS`, `COPY` and `HELP` among them | `ARROW` |

The default `showsCursor` accepts the eight kinds Strata started with: `ARROW`, `TEXT`, `HAND`, `CROSSHAIR`, `RESIZE_HORIZONTAL`, `RESIZE_VERTICAL`, `RESIZE_ALL` and `NOT_ALLOWED`. Override it, as the example canvas does for `WAIT`, to add the kinds the engine has.

### Density

The host's area is in UI units, and an adapter chooses what a unit is. Components and painters also need to know how many device pixels a unit covers, so a hairline can stay one device pixel wide and a chart can snap to the pixel grid. Call `setPixelsPerUnit(density)` on the host or the stack with `resize`, whenever either changes: when the window moves to another display, the user zooms, or a game's GUI scale changes. The density is 1 until set, and a change lays every component out again. Pass the device pixels per unit: a game adapter its GUI scale times the window's content scale, unless the framebuffer already holds the display's own pixels, and a desktop adapter its zoom times the display's scale. It must be finite and greater than 0.

```java
--8<-- "adapter/ToyWindowAdapter.java:density"
```

The canvas draws the densest image the device pixels under a rectangle can show. An adapter that keeps a sprite in several densities, such as `@2x` files, picks the denser one at 2 device pixels per unit and above, and draws it at the same size in UI units, so a sprite stays sharp. A sized nine-slice's border is given in units and keeps its size.

### Opacity

`pushOpacity(factor)` multiplies the alpha of every later color and tint by a factor, from 0 to 1 exclusive, until the matching `popOpacity()`. Factors multiply when nested. Core pushes one around each component whose opacity is between 0 and 1, so a fade covers everything the component and its children draw: fills, gradients, sprites, nine-slices, text and visuals. Both methods do nothing by default, so a canvas that does not override them draws faded components opaque and a fade shows as a jump at its end. An engine that can fade only some kinds of draw says so in its canvas documentation. The example canvas multiplies the factor into the alpha of the colors it records.

### Shapes

Lines, polylines, outlines, rounded rectangles, ellipses, arcs, shapes, gradients at an angle and shadows are optional. Report `Capabilities.drawsShapes` and implement every call:

```java
--8<-- "adapter/RecordingShapesCanvas.java:shapes"
```

When `drawsShapes` is false, core never calls them. It draws each shape from `fill` and vertical `fillGradient` calls one device pixel high, a gradient at an angle from columns of vertical gradients, a shadow from a few rings of falling alpha and a round clip as a clip to the whole rectangle. That works on every canvas and looks stepped.

When it is true, the defaults throw `UnsupportedOperationException`, so implement all of these. What core passes:

- Radii are limited to half the shorter side of their rectangle and are not negative, and a stroke of width 0 is not drawn.
- Angles are radians, 0 at the right of the oval, turning clockwise on screen. A sweep is less than a full turn either way; core draws a full turn with `strokeEllipse`.
- `polyline` gets a count of at least 2, and the array is valid only during the call.
- `shadow` gets a blur greater than 0; a hard shadow comes as `fillRoundRect`.
- The outline of a rectangle or an oval lies inside it, so the stroke's outer edge is the shape's edge. Lines and shapes are centered on their path, with the stroke's joins at corners and its caps on the ends of open subpaths. Fills use the nonzero winding rule, and every subpath is closed.
- Shapes never change. Keep the engine's own copy of a `Shape` by identity.
- `pushSmoothing(boolean)` and `popSmoothing()` nest like clips, and shapes are anti-aliased outside any push. Shapes must obey them, and sprites and text may.
- `pushRoundClip(rect, radius)` is popped by `popClip()` like a rectangle clip. Its default clips to the whole rectangle. An engine whose clips are hard-edged may keep them so.

### Pixel buffers

`pixels(buffer, rect, tint)` draws a `PixelBuffer`, the pixels an `Image` shows after `setPixels`. Keep one engine copy of each buffer drawn, such as a texture, copy the pixels again only when the buffer's `version()` differs from the one copied, and drop copies of buffers no longer drawn. Draw with nearest sampling so pixel art stays sharp. Core calls it on every canvas; the default draws the placeholder of a visual the canvas did not make.

### Right-to-left text

Core runs the bidirectional algorithm on every line and places the runs in visual order. What the canvas receives depends on `Capabilities.shapesRightToLeft`:

- When it is false, which is the default, core passes each right-to-left run to `text(span, x, y)` with its characters in reverse order and its brackets mirrored. That reads correctly for Hebrew and draws Arabic with its letters unjoined, which is what an engine that draws one code point at a time can do.
- When it is true, core passes each right-to-left run to `text(span, x, y, rightToLeft)` with `rightToLeft` true and the characters in logical order. The engine must order and join the run and draw it with its left edge at `x`, as wide as the platform's `TextMetrics.width` measures the span. Left-to-right runs still come through the three-argument form.

## Driving the host

Create a host with `StrataCore`:

| Factory | For |
|---|---|
| `createScreenHost(platform, theme)` | One screen at a time. A screen that asks to close is closed by the host. |
| `createScreenHost(platform, theme, closeRequested)` | The same, but you decide what closing means, such as closing the engine screen that holds the host. |
| `createScreenStack(platform, theme)` | A stack of screens, for engines with no screen system |

`createScreenHost(platform, theme, closeRequested)` and `createScreenStack(platform, theme)` also take a `ThemeRef`. A host given a ref also compares the version of the overrides code adds with `Strata.themes()`, which core keeps for every engine, and resolves the ref again when it changes. `StrataCore.resolveTheme` resolves a ref once, without a host.

### Input

`ScreenHost` and `ScreenStack` are both an `InputSink`, which takes the engine's input in UI units and Strata's types. Each method but `pointerLeft` returns true when Strata used the input; pass the rest on to the engine.

| Method | Send | Returns true when |
|---|---|---|
| `pointerMoved(x, y, modifiers)` | Every pointer movement, during drags too | The pointer is over Strata's UI or dragging in it |
| `pointerButton(x, y, button, pressed, modifiers)` | Each button's press and release | A component received the press, or the release ended a press that started on one |
| `scroll(x, y, scrollX, scrollY, kind, modifiers)` | Wheel and trackpad scrolling, in notches | A component consumed the scroll |
| `pointerLeft()` | The pointer leaving the host's area | |
| `key(key, action, modifiers)` | Key presses, repeats and releases | A component consumed the key, or Tab moved focus |
| `charTyped(codePoint, modifiers)` | Typed characters, never control characters | The focused component consumed it |

Send Command as `Modifier.SUPER`, and the key that opens a context menu as `Key.MENU`. Each method also has a form that takes an `InputContext`, for an engine that tells devices apart; see [Devices](#devices). Drags and drops from other applications use four more methods; see [Drops from other applications](#drops-from-other-applications).

Then forward the engine's callbacks:

```java
--8<-- "adapter/ToyAdapter.java:adapter"
```

The host's duties for you:

- Call `resize` before the first `render`, and again whenever the area changes. Call `setPixelsPerUnit` beside it whenever the density changes; see [Density](#density).
- Call `render` once per frame. It builds pending screens, synthesizes key repeats if needed, runs layout, draws and requests the cursor.
- Send every pointer movement, during drags too: drags are recognized from moves.
- Send each button's press and release. A release with no matching press is ignored.
- Send keys with the right `KeyAction`. If the engine reports repeats as more presses, use `HeldKeys` to turn the second and later presses into `REPEAT`, and call `HeldKeys.clear()` when the window loses focus.
- Send typed characters separately through `charTyped`, as code points, after the keyboard layout and input method. Never send control characters.
- Call `pointerLeft()` when the pointer leaves the area, and `close()` when the engine screen goes away so the Strata screen's `closed()` runs.
- When the user asks to close the engine screen, with its close button or a key it closes screens with, call `requestClose()` first and close the engine screen only when it returns true. A screen with unsaved changes can refuse. `close()` never asks, so use it for closes the engine makes for its own reasons.
- Show `title()` if the engine shows a title, and compare it with the one shown on each frame, since the screen can change it with `ScreenHandle.setTitle`. The text is returned as it is, so resolve it again when translations change.
- A toolkit that asks an embedded widget for its preferred size, such as Swing's `pack()`, can ask `preferredSize(availableWidth, availableHeight)`, in UI units with infinity for no limit. It builds a pending screen and measures its content without arranging it, and returns `Size.ZERO` when no screen is open. An engine whose screens fill the window has no use for it.
- Pass input on to the engine when the host returns false. See [What reaches the engine](../guides/input-focus.md#what-reaches-the-engine).
- Use Strata's directions: vertical scroll is positive away from the user, horizontal scroll positive toward the left.

The example adapter's close button and title bar, which an engine with a window of its own would call:

```java
--8<-- "adapter/ToyWindowAdapter.java:closing"
```

### Rendering on demand

An engine that draws every frame, as a game does, calls `render` every frame and can ignore this section. A toolkit that draws only when asked, such as Swing, can render on demand with three calls:

```java
--8<-- "adapter/ToyWindowAdapter.java:frames"
```

- `needsFrame()` is true when something changed since the last `render`: input the host dispatched, a layout invalidation, a binding queued, a task handed to the UI thread, an overlay opening or closing, a change of look, or a screen opening or closing. A new host needs a frame. Any thread may call it.
- `nextFrameNanos()` is when the next timed change is due, as a reading of the platform's clock: a caret's next blink, a tooltip's delay running out, a toast's expiry, the next synthesized key repeat, a caption or progress bar that moves with the clock, or an animation's next step. It is `Long.MAX_VALUE` when nothing timed is waiting. Call it on the UI thread after a render. An adapter that renders on demand renders when the clock reaches it, even when `needsFrame()` is false, and uses the platform's clock, the one core reads.
- `setFrameRequester(requester)` sets what runs when `needsFrame()` turns from false to true, and runs it at once if a frame is already needed. It runs once per change from clean to dirty, not again before the next `render`, and it can run on any thread, such as the thread that set a state. It must only schedule a frame, for example by asking the engine to repaint, and must not call into the host.

An adapter renders when `needsFrame()` is true or the clock has reached `nextFrameNanos()`. The first frame after a host is made is always due. Core asks for the next frame 16 ms after each step of a sliding caption or an animation, so something that moves continuously draws at about 60 frames a second.

### Devices

An engine that tells devices apart sends input through the forms that take an `InputContext`: the device (`MOUSE`, `KEYBOARD`, `CONTROLLER`, `TOUCH`, `PEN` or `NARRATOR`), which pointer of it, and how hard it presses, from 0 to 1. Use the same pointer id from a touch's press to its release, since each pointer id has its own presses, drags and click counts. A touch going down is a left button press. Only mouse and pen pointers hover, so a touch shows no hover face and no tooltip. A long press of a first touch opens the context menu, and two touches that move together scroll. A controller's buttons arrive as keys, with `new InputContext(Device.CONTROLLER, 0, 1f)`.

```java
--8<-- "adapter/ToyWindowAdapter.java:touch"
```

The forms without a context pass `InputContext.MOUSE` for pointer input and `InputContext.KEYBOARD` for keys and typed characters, so an engine with only a mouse and a keyboard changes nothing. The device goes into the input's cause, where handlers read it.

### Drops from other applications

A drag from another application enters the host with `dragEntered(x, y, transfer, allowed, modifiers)`, which carries what the drag holds in the formats the engine can name, and the effects the other application allows. An engine that can read the values only at the drop passes a transfer whose values are supplied, and they read as empty until then. Each return value is the effect a component would take, which the adapter shows and reports to the other application, or empty when nothing would take it.

- `dragMoved(x, y, modifiers)` for each move.
- `dragExited()` when the drag leaves or is given up.
- `dropped(x, y, transfer, modifiers)` at the drop, with the values readable. It returns the effect the target took, or empty.

A drop an engine reports without a drag before it, such as files dropped on a game window, goes to `dropped` alone, with `Transfer.files(paths)`; the component under the pointer hears `DRAG_OVER` and `DROP` together. The defaults do nothing, so an input sink that takes no drags needs no code.

```java
--8<-- "adapter/ToyWindowAdapter.java:drops"
```

 Drags out of the host to other applications are not part of the SPI.

## Opening screens

Code opens screens with `Strata.screens()`, which returns the controller the adapter hands to `StrataCore.setScreenController` when it starts. Hand it over before any project code can run, such as from the engine's plugin or mod entry point, and keep the controller cheap to create, since it may be asked for before the engine has a window.

- An engine without screens of its own can hand over a `ScreenStack`, which is a `ScreenController`: `open(screen, theme)` pushes a screen in a theme of its own.
- An engine with screens of its own implements `ScreenController.open(screen, theme)`: make an engine screen that holds a host for the Strata screen, show it, and return a `ScreenHandle` for it. Title the engine screen with the screen's `title()`. The default `open(screen)` passes `ThemeRef.platform()`.
- A screen's handle opens screens above it through the host's opener. Set one with `ScreenHost.setOpener(controller)` that puts the new screen above the host's screen and shows the host's screen again, unchanged, when the new one closes. A host with no opener opens through the platform's controller. A stack sets its own.
- Name the platform's default theme in `ThemeSource.defaultTheme()`. It must resolve against the source and must not be `strata:platform`, and changes show once the source's `version()` changes.
- `ScreenHost.handle()` returns the open screen's handle, for a controller that has to return one. A controller that wraps it for its own screen forwards each call: `requestClose()` to the host's handle (`host.handle().map(ScreenHandle::requestClose).orElse(false)`), `isOpen()` to whether the host has a handle while this opening is the current one, and `setTitle(title)`, returning the wrapper.
- A controller that opens screens from other threads hands the opening to the UI thread and returns a handle at once, whose calls wait for the screen. `ScreenStack` does this itself.
- The runnable given to `createScreenHost(platform, theme, closeRequested)` runs when the screen's handle closes it, through `close()` or through a `requestClose()` the screen agreed to. It does not run for `ScreenHost.requestClose()`, which the adapter calls itself and then closes its own screen when it returns true.
- A `ScreenStack` has `requestClose()` too: it asks the top screen, removes it if it agrees, then the next, and stops at the first screen that refuses. `title()` is the top screen's.

## Contract tests

The SPI's test fixtures hold `TextMetricsContract`, `PlatformContract` and `CanvasContract`. Extend each with your implementation:

```java
--8<-- "AdapterContractTests.java:contracts"
```

`CanvasContract` needs a `Surface` per check. A canvas that cannot read back its pixels, like this one, runs the checks that need no pixels: every call is accepted with ordinary and edge-case arguments, and deep clip and transform stacks unwind. A canvas that draws to readable pixels, such as an offscreen buffer, returns `readable() == true` and implements `argb(x, y)`. Then the pixel checks run too: fills cover exactly their rectangle, alpha 0 draws nothing, later draws cover earlier ones, clips limit and restore, nested clips intersect, translations shift and restore, gradients run top to bottom, missing sprites stay visible and inside their rectangle, nine-slices stay inside small rectangles, and text stays within its measured width. Override `metrics()` to enable the text check. Surfaces use one UI unit per pixel on a transparent background.

`CanvasContract` has two more hooks, `drawsShapes()` and `drawsPixels()`, which return false by default. Override them to return true for a canvas that implements the shape calls and draws pixel buffers itself. The shape checks then run: every call is accepted with ordinary and edge-case arguments, and on a canvas that can read its pixels, lines, outlines, ellipses, arcs, shapes, gradients at an angle, shadows and round clips land where the contract says. Every canvas is checked against all of the cursor kinds: `showsCursor` answers for each and true for the arrow, and `requestCursor` accepts each.

`PlatformContract` has `hasClipboard()` and `clockRunsOnItsOwn()` to override for a headless clipboard or a hand-driven clock, and `translator()` to override to run the translator checks. It also checks that the platform's file dialogs are present or absent without throwing, that transfers put on the clipboard never throw, and that a transfer's text reads back through `get()`. The example adapter's shapes canvas runs the contract with the shape checks on:

```java
--8<-- "AdapterExtrasTest.java:contract"
```

## Naming

Strata's adapters name classes after the engine and the SPI role they fill (`SwingCanvas`, `SwingPlatform`), and the engine-native component a developer adds to their engine is `Strata` plus the engine's base class (`StrataPanel`, a Swing `JPanel`). Following that keeps adapters easy to recognize.
