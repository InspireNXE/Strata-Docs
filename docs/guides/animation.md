# Animation

A progress bar that eases to its new value, a card that fades out, a color that blends to red: each is a value that moves over a few hundred milliseconds instead of jumping. `ComponentFactory.animator()` returns the host's `Animator`, which moves a property, or any number you like, over time. Strata's own controls use the same animator for their built-in motion, which a theme turns on.

## Moving a property

`animate` takes a target, a `PropertyKey` that names one of its properties, the value to end on, a duration in milliseconds and an `Easing`:

```java
--8<-- "animation/AnimationExamples.java:property"
```

The animator reads the key's current value when the animation is made, writes each step's value with the key's writer, which works like the property's setter, and writes the end value itself on the last step. Property keys are the constants that sit beside each attribute's events (`Ranged.VALUE`, `Component.OPACITY`, `ColorPicker.COLOR`). See [Data binding](binding.md) for the list.

A key whose value is not a double needs an `Interpolator` that finds the values between two:

| Interpolator | For |
|---|---|
| `Interpolator.DOUBLE`, `Interpolator.FLOAT` | Numbers |
| `Interpolator.ARGB` | A color, channel by channel, rounded and kept from 0 to 255 |
| `Interpolator.INSETS` | Insets, side by side, never below 0 |

```java
--8<-- "animation/AnimationExamples.java:color"
```

An interpolator is a functional interface, `V between(V from, V to, float t)`, so one for your own type is a lambda. It must return `from` at exactly 0 and `to` at exactly 1.

A second animation of the same property of the same target stops the first where it is. Properties with different names, or on different targets, run side by side.

## Any number

`run` gives your code the eased fraction on each frame and leaves the rest to you:

```java
--8<-- "animation/AnimationExamples.java:run"
```

## Easings

An `Easing` turns the fraction of the time that has gone into the fraction of the way to the end. It returns 0 for 0 and 1 for 1, and it may leave the range between them, which makes the value overshoot and settle.

| Easing | Shape |
|---|---|
| `Easing.LINEAR` | Even speed |
| `Easing.EASE_IN` | Slow start, full speed at the end (CSS `ease-in`, `cubicBezier(0.42, 0, 1, 1)`) |
| `Easing.EASE_OUT` | Full speed at the start, slow end (CSS `ease-out`, `cubicBezier(0, 0, 0.58, 1)`). The usual choice for something that answers the user |
| `Easing.EASE_IN_OUT` | Slow at both ends (`cubicBezier(0.42, 0, 0.58, 1)`) |
| `Easing.cubicBezier(x1, y1, x2, y2)` | Your own curve, defined as CSS's `cubic-bezier()` is. `x1` and `x2` are times from 0 to 1; `y1` and `y2` are progress and may go outside 0 to 1 |

An easing runs every frame of every animation that uses it, on the UI thread, so keep it quick and allocation free.

## Stopping and finishing

`animate` and `run` return an `Animation`:

| Call | Does |
|---|---|
| `isRunning()` | False once it reached its end, was finished or was closed |
| `finish()` | Runs the last step at once, exactly 1, then the finished actions. Does nothing when the animation is not running |
| `close()` | Stops it where it is. The property keeps its last value and the finished actions never run |
| `onFinished(action)` | Runs the action after the last step, whether it came by time or by `finish()` |

Finished actions run in the order added. An action added after the animation reached its end runs at once. An action added after it was closed never runs. A screen's animations stop where they are when the screen closes.

```java
--8<-- "animation/AnimationExamples.java:fade"
```

## When it moves

- Animations step once a frame, by the platform's clock, on the UI thread. The step comes after the tasks handed over with `UiThread.execute` and before data bindings are brought up to date, so a binding sees where an animation is. See [Threads](threading.md).
- An animation's time starts when it is made. Its first step comes on the next frame and its last step is exactly 1 whatever the easing returns near the end. A duration of 0 takes that one step, 1, on the next frame. A negative duration throws `IllegalArgumentException`.
- Starting an animation asks the host for a frame. While any animation runs, the host asks for another 16 ms after each step, which matters to adapters that render on demand. See [Writing an adapter](../platforms/writing-an-adapter.md).
- Every animator call must come from the UI thread and throws `IllegalStateException` elsewhere.
- A step that throws stops its own animation. The others still step, and the exception goes on to the host's caller.

## Causes and reports

Each step runs inside a cause frame that has the `Animation` pushed on it and `Origin.CODE` put. Requests fire only for the user, so motion never fires requests, and the reports it causes say `Origin.CODE`. A handler can tell motion from other changes:

```java
--8<-- "animation/AnimationExamples.java:cause"
```

`Origin.CODE` is put explicitly, so a `finish()` called from a click handler does not run the last step under the user's origin and fire requests.

## Bindings

A state bound two ways to an animated property does not follow every step. All steps but the last carry a marker that tells the binding to wait. The binding hands itself to the animation, and writes the state once the animation ends or is closed. A state therefore hears the place where the property came to rest, including where a closed animation stopped. The control's own reports still fire on each step. See [Data binding](binding.md).

## Fading

`Component.OPACITY` is the property key of `opacity()` and `setOpacity(float)`, a value from 0 to 1 that multiplies down the tree and is 1 by default. A component at 0 is not drawn, which works on every adapter. One between 0 and 1 is drawn through the canvas's opacity calls. An adapter that cannot fade some drawing, such as pictures the engine renders itself, says so in its platform notes. Opacity changes drawing only: a transparent component still takes its room, the pointer and focus. See [Components](components.md).

## Built-in motion

Strata's controls move through the same animator when a theme gives them a duration. Every duration is 0 by default, so nothing moves and a theme matches its engine until its owner chooses. `Themes.flat()` sets none either.

| Control | Motion | Metric (milliseconds) |
|---|---|---|
| `ToggleSwitch` | The knob slides. The track and the state change at once | `toggle.animation` |
| `Expander` | The content grows and shrinks, clipped | `expander.animation` |
| `NavigationView` | The pane over the content slides in from the view's left edge | `nav.animation` |
| `ScrollPanel`, lists, text areas | Wheel steps, keys and scrolling to a row or into view glide | `scroll.smooth` |
| Toasts | They fade in and out | `toast.fade` |
| `ProgressBar` | The fill eases to a new value. The value changes at once | `progress.animation` |

Built-in motion eases out, and a toast's fade out eases in. The control applies each value through its own setter, so clamping, reports and layout still hold. Where the table says the state or value changes at once, only the drawing moves. A control that is not attached applies the end value at once. See [Theme keys](../reference/theme-keys.md) for the keys and [Themes](theming.md) for setting a metric from a theme file or from code.

The caption sweep of a long label, the busy indicator and an indeterminate progress bar follow the clock by hand with their own looks and ask for their frames, so they need no metric.

## Testing

The test kit's tester has a clock that stands still until the test moves it. `advance(millis)` moves it and renders a frame, which steps every animation, and `settle()` runs frames 16 ms apart until nothing of the screen animates. With nothing animating it runs no frame. `settle()` fails after a minute of animation, as when one animation keeps starting another. See [Testing](testing.md).

## Defaults

| What | Default |
|---|---|
| Built-in motion durations | 0 (no motion) |
| Component opacity | 1 |
| Frame request while animating | 16 ms after each step |
| Built-in easing | `EASE_OUT`; a toast's fade out `EASE_IN` |
