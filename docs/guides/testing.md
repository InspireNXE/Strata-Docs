# Testing

An application can test its screens without an engine or a display. A test opens a screen, types 3 into the quantity box, clicks Buy and checks that the total reads 29.97. The test kit does what a person would do: input goes through the same path as the engine's input, requests fire, bindings run and layout happens, so a test fails when a person could not have done the thing.

The kit is `ScreenTester`, with `TesterOptions` and `Matching`, in the package `org.inspirenxe.strata.testing`. It ships in the test fixtures of `strata-core`.

## Add the kit

With Strata from Maven, depend on the test fixtures of `strata-core` from your test configuration. The kit's public API uses only `strata-api` and `strata-spi` types, and brings core in at run time.

```groovy
dependencies {
    testImplementation(testFixtures('org.inspirenxe:strata-core:0.1.0-SNAPSHOT'))
    testImplementation 'org.junit.jupiter:junit-jupiter:5.12.2'
}
```

The fixtures are the `test-fixtures` jar of `strata-core`. They declare the capability `org.inspirenxe:strata-core-test-fixtures` beside Gradle's own, as the fixtures of `strata-spi` do. In a build that includes the Strata checkout as a composite build, substitute the project, and write `testFixtures(project(':core'))` for the dependency. This site's build takes the Maven coordinates and lets the composite build substitute the checkout.

Nothing else is needed: no window, no engine, no `java.awt.headless` switch.

## A first test

This is the screen the guide tests. A quantity box, a Buy button that adds the order and shows the total, and a Cancel button that is disabled:

```java
--8<-- "testing/ShopScreen.java:screen"
```

`setId` gives a component a name the test can look it up by. The test types a quantity, clicks Buy and reads the total:

```java
--8<-- "testing/ShopScreenTest.java:total"
```

`ScreenTester.open(screen)` opens the screen in a fresh host, renders a first frame and returns the tester. Use it in a `try`-with-resources block: `close()` closes the screen, so its `closed()` runs and its tree detaches. Closing twice does nothing, and any other call on a closed tester throws `IllegalStateException`.

What opening does, in order: it sets the options' translator for the whole process, makes a host on the kit's own platform, sizes it, opens the screen and renders. If any of that fails, the translator is put back and the host closed before the exception reaches the test. `open` throws what a real host would: a `ThemeException` when the theme lacks a key the screen's controls need, and an `IllegalArgumentException` when the screen's `build` returns a component that is already in a tree or was made by another factory.

## Options

`ScreenTester.open(screen, options)` takes a `TesterOptions`. Start from `TesterOptions.DEFAULT` and change what the test needs:

| Option | Default | Change with |
|---|---|---|
| Size of the host | 800 by 600 units | `withSize(width, height)` |
| Theme | `Themes.flat()` | `withTheme(theme)` |
| Translator | `Translator.NONE` | `withTranslator(translator)` |
| Wall clock | noon UTC on 15 January 2026 | `withNow(zonedDateTime)` |

A size that is not positive and finite throws `IllegalArgumentException`. The wall clock never moves, so date and time controls take "today" from `now`, and a test about the first of the month does not depend on when it runs. The translator is the process-wide translator while the tester is open, and the tester puts the previous one back when it closes. Two testers open at once in one process share it, so tests that use translators should not run in parallel with each other.

```java
--8<-- "testing/ShopScreenTest.java:options"
```

The kit's platform measures text the way Strata's own tests do, so layout is the same on every machine: 6 units wide for each code point, 7 in a monospaced style, and 9 high for a line. Its clock stands still until the test calls `advance`. Its clipboard is held in memory. It has no engine text input, and it reports that the engine sends key repeats, so holding a key repeats nothing.

## Finding components

| Call | Returns |
|---|---|
| `find(type, match)` | The one component of `type` that matches. |
| `findAll(type, match)` | Every match in tree order, possibly none. |
| `byId(id, type)` | The one component of `type` with that id. |

`type` is usually a control interface such as `Button.class`. `match` is a predicate, and `Matching` has the common ones: `Matching.text("Buy")` for anything with text, `Matching.title("Settings")` for anything with a title, and `Matching.enabled()` for components that can take input. Combine them with `and`:

```java
tester.find(Button.class, Matching.<Button>text("Buy").and(Matching.enabled()));
```

Searching walks the screen's content and then each open overlay's content, parents before children. It goes through the parts a control builds for itself too, and it finds hidden and disabled components as well: finding is not acting. The tooltip is left out.

`find` and `byId` throw an `AssertionError` unless exactly one component matches. With none, the message lists every component of that type on the screen, and with several it lists the matches. Each component is described by class name, id, text and bounds, so a mistyped caption is easy to see.

### Ids

`id()` and `setId` exist on every component. An id is a free string that Strata never checks: it need not be unique, and there is no lookup by id in the API. `id()` returns an `Optional`, and `setId(null)` removes it. The test kit's `byId` is the lookup.

## Input

Every input goes through the host's own input methods, with no modifiers unless the test names some, so each runs in a user frame and requests fire as for a person.

| Call | Does |
|---|---|
| `click(target)` | Moves the pointer to the target's center, presses and releases the left button. |
| `doubleClick(target)` | Two presses and releases. |
| `rightClick(target)` | Presses and releases the right button. |
| `drag(target, dx, dy)` | Presses at the center, moves halfway and then the whole way, and releases there. A move shorter than the drag threshold starts no drag. |
| `dragTo(source, target)` | Drags the source onto the target, see below. |
| `drop(target, transfer)` | Drops a transfer from another application on the target. |
| `scroll(target, dy)` | Moves the pointer to the target's center and turns the wheel. Positive scrolls toward the top. |
| `type(text)` | Sends a typed character for each code point to the focused component. |
| `press(key, modifiers...)` | Presses and releases a key, holding the modifiers around it. |

Every call returns the tester, so calls chain. A test can focus a text box by clicking it, or by `press(Key.TAB)`, and check `tester.focused()`:

```java
--8<-- "testing/ShopScreenTest.java:keys"
```

### When a click refuses

Before acting on a component, the tester lays out and checks that a person could act at the target's center. If not, it throws an `AssertionError` that says why, so a test never passes by clicking something the user cannot reach. The reasons are:

- the component is not on this screen,
- it is hidden, or inside a hidden component,
- it is disabled, or inside a disabled component (a scroll passes on instead, as a disabled component does for a person),
- it has no size,
- its center is outside the screen,
- its center is clipped out of view, by an ancestor that clips,
- another component covers its center, or
- a dialog's backdrop is above it.

The check uses the same hit test as the pointer, so what a click would reach is what the test sees.

```java
--8<-- "testing/ShopScreenTest.java:refuse"
```

To test a component that is scrolled out of view, scroll first, then click.

### Two clicks

Two calls to `click` are two single clicks. When a click would continue a run of presses (same button, on the same spot, within the double-click time), the tester first moves the clock past that time, so a double click needs `doubleClick`.

## Time

The kit's clock stands still. `advance(millis)` moves it forward and renders a frame, so waiting tooltips show and toasts whose time ran out close:

```java
--8<-- "testing/ShopScreenTest.java:clock"
```

`settle()` runs frames 16 ms apart until no animation of the screen runs, so motion that a theme turns on, a toast's fade or the screen's own [animations](animation.md) come to rest. With nothing animating it runs no frame and leaves the clock alone. It throws an `AssertionError` if animations are still running after 60 seconds of test time, as when one keeps starting another. Use `advance` to look at a point in the middle of an animation.

`frame()` renders one frame without moving the clock: it rebuilds the screen if it asked to be, brings bound components up to date, lays out and draws. Every action ends with one, so what the test reads is current.

## Reading the result

- Components themselves: `text()`, `isEnabled()`, `bounds()`, `items()` and the rest of their API, on the objects `find` returned.
- `drawnText()` returns the text the last frame drew, one entry for each run: the screen's tree, then each overlay, then the tooltip. It is what the user would read, after translation, so it also proves that a translated key resolved.
- `focused()` is the component with keyboard focus, as an `Optional`.
- `overlays()` lists the open popups, dialogs, menus and toasts, bottom first.
- `factory()` is the host's `ComponentFactory`. It lets a test add components, open overlays, read `clipboard()` and call `componentAt`.

## Drag and drop and the clipboard

`dragTo(source, target)` presses the center of the source, moves past the drag threshold toward the target, and releases over the target's center. A source that starts a transfer, such as a list whose rows drag, hands it to the target as for a person. `drop(target, transfer)` enters at the target's center allowing every effect, drops there, and returns the effect the component under the point took, or empty when nothing took it. See [Drag and drop](drag-and-drop.md).

`tester.factory().clipboard()` and `setClipboard` reach the kit's in-memory clipboard. See [Clipboard](clipboard.md).

## What the kit does not do

The kit draws nothing. `drawnText()` records text, and the rest of a frame is discarded, so a test cannot compare pixels. For a test that does, render with the Swing adapter: `SwingSnapshot` draws a screen to an image without a display, and the test compares the image with a reference. See [Swing](../platforms/swing.md#rendering-without-a-display).

It has no engine, so engine-specific behavior is out of reach: IME input, drops from the desktop, native cursors and file dialogs. The platform reports none of them. Test an adapter with the contract tests described in [Writing an adapter](../platforms/writing-an-adapter.md).

## The kit's own tests

Core tests the kit with the same shop screen: closing once, `find` with no match and with several, finding in an overlay, refusing a disabled button, a component in a hidden panel, a button under a dialog or scrolled out of view, two clicks against a double click, a drag past the threshold, Tab and Ctrl+A through `press`, a tooltip after `advance`, and the options.
