# Troubleshooting

## Errors

### `ThemeException: Theme '...' is missing style keys: ...`

The theme lacks keys that components in the tree require. The message lists every missing key. Add them to the theme, usually by deriving from a built-in theme with `toBuilder()`. For your own components, the keys are whatever `requiredStyleKeys()` returns. It is thrown when a screen opens, when a component is added to a tree on screen, and when the host's theme changes. See [Themes](../guides/theming.md#required-and-optional-keys).

### `IllegalArgumentException` when adding a component

`Container.add` rejects a child that:

- already has a parent: remove it from the old parent first;
- is a screen's root, the container itself, or one of the container's ancestors;
- was made by another host's factory: build each screen's components from the factory that screen received;
- has no Strata implementation: it implements a component interface directly instead of extending `CustomComponent`.

The same rules apply to the component `Screen.build` returns.

### `IllegalStateException: Expected one Strata provider on the class path, found 0`

A `Strata` method such as `Strata.screens()` or `Strata.texts()` could not find Strata's core. `strata-core` must be on the runtime class path; the Swing adapter brings it as a runtime dependency. "found 2" means two copies of core are on the class path, for example from two mods that each bundle it. Keep exactly one.

### `IllegalStateException: No screen controller is set`

`Strata.screens()` was called before the engine adapter handed Strata its screen controller. On Swing, call `setDefault()` on the `StrataPanel` that should show screens before opening any. On another engine, open screens only after the adapter has loaded; its documentation says when that is.

### `IllegalStateException: Screen ... has closed and cannot open another`

`open` was called on the handle of a screen that has closed, or whose host has since opened another screen. A stale handle ignores `invalidate` and `close`, but it cannot open a screen above a screen that is gone. Open from `Strata.screens()` instead, or keep only the handle of the current opening.

### `IllegalStateException: No snapshot was taken for this event`

A handler called `event.snapshot()` or `currentSnapshot()` on a request or input event that has no snapshot of that component, because the component joined the path during delivery. Post, paint and measure events have no snapshot methods at all; on a Post event, read `previous()` instead. See [Event snapshots](../guides/snapshots.md#when-there-is-no-snapshot).

### `IllegalStateException: Called on ..., but components shown by a host must be used on its UI thread`

A setter, `add` or another change reached a component from a thread that is not the host's UI thread. Hand the work to the UI thread with `Strata.uiThread().execute(...)` or the factory's `uiThread()`, or set a `State` the component is bound to. A component not yet in a host can be built on any thread. See [The UI thread](../guides/threading.md).

### `NullPointerException` from `setTooltip(null)`, `setPadding(null)` or `setPlaceholder(null)`

Setters that have overloads no longer take null. Call `clearTooltip()`, `clearPadding()` or `clearTextPadding()`, and pass empty text for no placeholder. See [Javadoc](../reference/javadoc.md).

### `IllegalStateException` calling `startTransfer`, `accept` or `reject`

`DragEvent.startTransfer` works only in a `DRAG_START` handler or `onDragStart`, once per drag, and `accept` and `reject` only in `DRAG_OVER` and `DROP`. See [Drag and drop](../guides/drag-and-drop.md).

### `IllegalArgumentException: Sprite id needs a namespace`

`SpriteId.parse` was given text without a colon. Write `"examplemod:button"`, not `"button"`.

### An exception from my handler crashes the game or the Swing thread

Strata does not catch exceptions from handlers or hooks. They propagate out of the input or frame call that caused them. Catch your own errors.

## Nothing shows up

| Check | Why |
|---|---|
| Is the root anchored with `Anchor.FILL`? | Without it, the root takes its desired size at the top left, which may be tiny. |
| Does the component have a size? | An empty container measures as just its padding. Give it children or a size. |
| Is it visible? | Hidden components take no space and are not drawn. |
| Is a clipping ancestor too small? | Children are clipped to their container's bounds by default. |
| Are your colors opaque? | `0xFF0000` has alpha 0 and draws nothing. Write `0xFFFF0000`. |
| On Swing, was the screen opened? | `Strata.screens().open(screen)` after `panel.setDefault()`, or `panel.screens().push(screen)`. |
| Did you show it on the UI thread? | Hosts are single-threaded. Hand work from other threads over with [`Strata.uiThread().execute`](../guides/threading.md). |

## Layout looks wrong

| Symptom | Fix |
|---|---|
| A stretched child is not stretching | Its parent has no width to give. Fix the width somewhere above it. |
| A child ignores its alignment in a `StackPanel` | Along the stack's direction, each child gets exactly its size. Give the child a [weight](../guides/layout.md#weights), or use a `GridPanel`. |
| A label does not wrap | Turn wrapping on and give it a width. See [Wrapping text](../guides/layout.md#wrapping-text). |
| Text is cut off on another engine but fine in Swing | The engine's font is wider in places, or rounds widths to whole pixels. Leave slack around text, or let it measure. |
| Things are a pixel off on another engine | The adapter may round fractional positions to whole pixels. Use whole numbers. |
| A `GridPanel` child lands in the wrong cell | Cells past the defined rows and columns move to the last ones. Define enough tracks. |
| A custom container's children have stale positions | It must measure and arrange every child, hidden ones included. See [Write a new layout](../guides/extending.md#write-a-new-layout). |
| A custom component looks stale until the mouse moves | A change that only `paint` reads needs `repaint()`, and one the clock brings needs `repaintAfter(millis)`. Adapters that draw only when asked show it. See [Asking for frames](../guides/extending.md#asking-for-frames). |
| Labels, a text box and a button in a row show their text at different heights | Give them `VerticalAlignment.BASELINE` in a horizontal stack panel, a wrap panel or a grid row. See [Lining up text](../guides/layout.md#lining-up-text). |
| A `WrapPanel` stays in one long line | It was offered unlimited width. Give it a fixed or maximum width. |
| A right to left screen is not mirrored | The translator's language reads left to right and nothing set a direction. Call `setFlowDirection(FlowDirection.RIGHT_TO_LEFT)` on the root, or set a language that reads right to left. See [Right to left](../guides/layout.md#right-to-left). |

## Input problems

| Symptom | Cause and fix |
|---|---|
| Clicks do nothing on a button | It is disabled, hidden, covered by a later sibling, or clipped away. Clicks on a disabled component go to its ancestor. |
| A container's handler misses clicks on its buttons | Buttons consume the clicks they act on. Handle them on the button. |
| Typing does nothing in a custom component on some engines | The engine sends characters only in text input mode. Override `acceptsTextInput()` to return true. |
| Typing does nothing at all | Nothing has focus, and characters only go to the focused component. Click the field or call `requestFocus()`. |
| `requestFocus()` returns false | The component is not attached, not focusable, hidden or disabled, or an ancestor is hidden or disabled. |
| Tab skips a component | It is not focusable and tabbable, or it or an ancestor is hidden or disabled. Check its tab index too. |
| A shortcut on the root never fires | A focused component consumed the key first. A text box consumes printable keys without Control or Alt. |
| Modifier checks never pass on another engine | Some engines send no modifiers with moves, scrolls or characters. See [Modifier keys](../platforms/differences.md#modifier-keys). |
| Escape closes the screen | The engine closes screens on an unconsumed Escape, and Strata asks the screen first through `closeRequested`. Return false from it to refuse, or consume Escape in a root handler. See [Closing](../guides/screens.md#closing). |
| A dragged item is never dropped | A target must call `event.accept(effect)` in `DRAG_OVER`, or have a default that does. A drag with a format the target does not take finds no target. See [Drag and drop](../guides/drag-and-drop.md). |
| A command's shortcut does nothing | The command is disabled, a focused component consumed the key first, or a modal dialog is open, which blocks the host's shortcuts. See [Commands and shortcuts](../guides/commands.md). |

## Sprites

| Symptom | Cause |
|---|---|
| Magenta square (Swing) | The image is not on the classpath at `assets/<namespace>/sprites/<path>.png`, or where your `SpriteResolver` points. |
| Another adapter's placeholder | The image is missing where that adapter looks. Its documentation gives the path. |
| Nine-slice looks different in Swing and another engine | See [Nine-slices](../platforms/differences.md#nine-slices). |

## Building Strata

### "Unable to establish loopback connection" on Windows

The JDK's NIO pipe falls back to Unix domain sockets in `%TEMP%`, which some machines block. Point it at a local directory before running Gradle:

```bash
export JAVA_TOOL_OPTIONS="-Djdk.net.unixdomain.tmpdir=$PWD/.gradle/udstmp"
```

### "Unsupported class file major version" or toolchain errors

Strata needs JDK 25. Set `JAVA_HOME` to a JDK 25 installation, or let Gradle's toolchain support download one.

### The build fails on a Javadoc warning

`strata-documented` runs Javadoc with every doclint check and fails on any warning, including a missing comment. Document the new public or protected member. The error names the file and line.

### The documentation site fails to build

`python -m mkdocs build` is strict. A link to a missing page, or a snippet that names a missing file or section, fails it. Run `./gradlew siteJavadoc` first so the Javadoc links resolve. See [Contributing](../project/contributing.md#the-documentation-site).
