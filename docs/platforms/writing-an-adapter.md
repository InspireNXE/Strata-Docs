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

- Call hosts on one thread, the engine's UI or render thread. Strata does no locking.
- Coordinates are UI units with the origin at the top left of the host's area. Choose what a unit is, such as screen pixels or scaled pixels, and convert drawing and input the same way.
- Never let engine types into Strata calls. Map keys to `Key`, buttons to `MouseButton`, modifiers to `Modifiers`.
- Run the contract tests.

## Text metrics

`TextMetrics` measures text the way your engine draws it. Its requirements, from the Javadoc:

- A span drawn at x must end at x plus its measured width, give or take a unit of rounding. Text boxes place the caret from these numbers.
- The same span always gives the same result, with no side effects.
- Width is 0 for an empty span and never negative.
- Appending a code point never makes a span narrower.
- Line height is positive.
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
| `clipboard()` | Never throws. A busy or missing clipboard reads as empty and ignores writes. Use `Clipboard.NONE` if the engine has none. Text written reads back unchanged until something else changes it. |
| `clock()` | Monotonic, never backward, advancing in real time. `Clock.SYSTEM` is `System.nanoTime`. Click counting, caret blinking and synthesized key repeat stall on a clock that stops. `wall()` returns the `java.time.Clock` that today is read from, such as the day a calendar marks; the default is the system clock in the default zone. Return the engine's clock or zone if it has one, or a fixed clock in tests. Core reads it at most about once a minute per control and never accepts null. |
| `textInput()` | Told when a text field gains and loses focus. Core calls `start(area, kind)`, whose default forwards to `start(area)`; for `TextInputKind.SECRET`, turn off input method composition, prediction and on-screen keyboard suggestions where the engine allows, and never log what is typed. `start` and `stop` may arrive repeatedly; a second `start` moves the input area and may change the kind. After `start`, the adapter must forward typed characters. Use `TextInputSink.NONE` if the engine always sends characters. |
| `capabilities()` | Constant for the platform's life. `sendsKeyRepeat` is true when the adapter delivers `REPEAT` itself. `monospaceFont` is true when monospaced styles get a font whose characters all have the same width; otherwise they use the normal font, measured and drawn alike. `slicesSprites` is true when the engine stores slicing with its images and applies it in `sprite` as well as `nineSlice`; the host then passes sized nine-slices and tiled sprites to the canvas whole. When it is false, the host draws them from `spriteRegion` calls, one per piece. Its default draws the whole image with `sprite` inside a clip; override it to draw part of an image directly, which avoids seams between pieces at scales that are not whole numbers. |
| `conventions()` | Optional. How the engine's own controls behave, for the behaviors a theme does not reach, so Strata's controls match them. Constant for the platform's life; `Conventions.DEFAULT` unless overridden. `activateOnPress` is true when the engine's buttons act as soon as the left pointer button goes down on them; buttons, cycle buttons, checkboxes, radio buttons, toggle switches and tabs then act on the press. When false, they act on the click, when the button comes back up still over them, and moving off first cancels. Keyboard activation and drop-down buttons do not change. Looks belong in the adapter's theme files, which can set the [metric](../guides/theming.md#metrics) keys as well as paints, text styles and insets. |
| `themes()` | Where themes named by id come from; see [Themes](#themes). Use `ThemeSource.NONE` if the engine has no themes or packs. |
| `causes()` | Optional. The engine's own cause stack, when it has one Strata's [causes](../guides/events.md#who-caused-it) should follow; empty by default, and core then keeps its own, one per thread. A stack returned here keeps frames for the UI thread, closes them as `CauseFrame` states, and answers `current()` and `context` from the frames open at the call. Hosts push their input frames on it and developers get it from `ComponentFactory.causes()`. |
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

## Starting up

Some things are shared by the whole process, so the adapter hands them to core once, when the engine loads it and before any project code can run:

```java
--8<-- "adapter/ToyStartup.java:startup"
```

Translated text reads through one `Translator` for the whole process, which the adapter gives core with `StrataCore.setTranslator`. `translate(key)` returns the pattern for the current language as written, with its `%s`, `%1$s` and `%%`, or empty when there is none; core formats the arguments. `version()` changes whenever translations may read differently, and hosts lay everything out again when it does, so keep it cheap. Calls may come from any thread.

`locale()` returns the locale of the current language, which dates and times follow: month and weekday names, the first day of the week, the order of a date's parts and the 12 or 24 hour clock. The default is the JVM's default locale for formatting. Map the engine's language code to a `Locale` here, and change `version()` whenever the locale changes, since core caches what it reads per locale and version.

An adapter gives its engine's types an [appearance](../guides/appearances.md) with `StrataCore.registerDefaultAppearance(type, appearance)` when it starts, so lists and images show them with icons, names and tooltips. Defaults sit beneath anything a project registers for the same type, whichever comes first.

An engine with its own text type can wrap it as Strata text with `StrataCore.text(source)`. A `TextSource` resolves the engine text to spans over a base style each time a control lays out, so engine translations stay live, and every `Text` operation works on it. Parts with no style of their own take the base, and a part that sets a property changes only that property of the base. `resolve` may be called from any thread and must not change the engine text. Implement `equals` and `hashCode` by the engine text, as a record does, so equal engine texts make equal Strata text.

The controller handed to `StrataCore.setScreenController` is what `Strata.screens()` returns; [Opening screens](#opening-screens) says what it must do.

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
- `requestCursor` is called at most once per frame, after drawing, while the pointer is over a component. In a frame without a request, show the default cursor.
- `text` never gets a span that holds a [picture](../guides/text.md#pictures-in-text). Core measures pictures itself and draws them with `sprite` and `visual`, so the engine's metrics and canvas see only characters. Draw monospaced spans in the engine's monospaced font when `Capabilities.monospaceFont` is true.
- `spriteRegion` draws part of an image, given in fractions of the whole from 0 to 1. Core draws sized nine-slices and tiled sprites from it when `Capabilities.slicesSprites` is false. Its default draws the whole image inside a clip; override it when the engine can draw part of an image directly, or pieces drawn side by side may show seams at scales that are not whole numbers.

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

Then forward the engine's callbacks:

```java
--8<-- "adapter/ToyAdapter.java:adapter"
```

The host's duties for you:

- Call `resize` before the first `render`, and again whenever the area changes.
- Call `render` once per frame. It builds pending screens, synthesizes key repeats if needed, runs layout, draws and requests the cursor.
- Send every pointer movement, during drags too: drags are recognized from moves.
- Send each button's press and release. A release with no matching press is ignored.
- Send keys with the right `KeyAction`. If the engine reports repeats as more presses, use `HeldKeys` to turn the second and later presses into `REPEAT`, and call `HeldKeys.clear()` when the window loses focus.
- Send typed characters separately through `charTyped`, as code points, after the keyboard layout and input method. Never send control characters.
- Call `pointerLeft()` when the pointer leaves the area, and `close()` when the engine screen goes away so the Strata screen's `closed()` runs.
- Pass input on to the engine when the host returns false. See [What reaches the engine](../guides/input-focus.md#what-reaches-the-engine).
- Use Strata's directions: vertical scroll is positive away from the user, horizontal scroll positive toward the left.

## Opening screens

Code opens screens with `Strata.screens()`, which returns the controller the adapter hands to `StrataCore.setScreenController` when it starts. Hand it over before any project code can run, such as from the engine's plugin or mod entry point, and keep the controller cheap to create, since it may be asked for before the engine has a window.

- An engine without screens of its own can hand over a `ScreenStack`, which is a `ScreenController`: `open(screen, theme)` pushes a screen in a theme of its own.
- An engine with screens of its own implements `ScreenController.open(screen, theme)`: make an engine screen that holds a host for the Strata screen, show it, and return a `ScreenHandle` for it. Title the engine screen with the screen's `title()`. The default `open(screen)` passes `ThemeRef.platform()`.
- A screen's handle opens screens above it through the host's opener. Set one with `ScreenHost.setOpener(controller)` that puts the new screen above the host's screen and shows the host's screen again, unchanged, when the new one closes. A host with no opener opens through the platform's controller. A stack sets its own.
- Name the platform's default theme in `ThemeSource.defaultTheme()`. It must resolve against the source and must not be `strata:platform`, and changes show once the source's `version()` changes.
- `ScreenHost.handle()` returns the open screen's handle, for a controller that has to return one.

## Contract tests

The SPI's test fixtures hold `TextMetricsContract`, `PlatformContract` and `CanvasContract`. Extend each with your implementation:

```java
--8<-- "AdapterContractTests.java:contracts"
```

`CanvasContract` needs a `Surface` per check. A canvas that cannot read back its pixels, like this one, runs the checks that need no pixels: every call is accepted with ordinary and edge-case arguments, and deep clip and transform stacks unwind. A canvas that draws to readable pixels, such as an offscreen buffer, returns `readable() == true` and implements `argb(x, y)`. Then the pixel checks run too: fills cover exactly their rectangle, alpha 0 draws nothing, later draws cover earlier ones, clips limit and restore, nested clips intersect, translations shift and restore, gradients run top to bottom, missing sprites stay visible and inside their rectangle, nine-slices stay inside small rectangles, and text stays within its measured width. Override `metrics()` to enable the text check. Surfaces use one UI unit per pixel on a transparent background.

`PlatformContract` has `hasClipboard()` and `clockRunsOnItsOwn()` to override for a headless clipboard or a hand-driven clock.

## Naming

Strata's adapters name classes after the engine and the SPI role they fill (`SwingCanvas`, `SwingPlatform`), and the engine-native component a developer adds to their engine is `Strata` plus the engine's base class (`StrataPanel`, a Swing `JPanel`). Following that keeps adapters easy to recognize.
