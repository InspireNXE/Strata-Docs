# Extending Strata

There are three ways to extend Strata. Pick the lightest one that does the job.

| Way | Use it to | Cost |
|---|---|---|
| [Handle a built-in's events](#change-a-built-in-through-its-events) | Add drawing, change sizes, or filter, rewrite or veto what the user does to any control | A lambda |
| [Compose](#compose-built-ins) | Package several components as one reusable piece | A plain class |
| [Write a new component](#write-a-new-component) or [layout](#write-a-new-layout) | Make something that does not exist yet | A subclass of `CustomComponent`, `CustomComposite` or `CustomContainer` |

A subclass gets the same hooks Strata's own controls and panels are written with, so you can build anything they do. [Strata's components and yours](#stratas-components-and-yours) says how the two kinds differ.

![A custom switch and a custom wrapping layout](../assets/images/extending-custom.png)

## Change a built-in through its events

Everything a control does goes through [events](events.md), so most changes to a built-in need no new class, only a handler:

- To stop or change what the user does, handle the control's Pre event: cancel it, or set another value.
- To react, handle its Post event.
- To draw on top, handle `Component.PAINTED`; to draw instead, cancel `Component.PAINT`; to change the size it asks for, handle `Component.MEASURED`.

Filter what a text box accepts, whether the user types or pastes:

```java
--8<-- "extending/ExtendingExamples.java:filter"
```

Rewrite a value on its way in:

```java
--8<-- "extending/ExtendingExamples.java:rewrite"
```

Ask before a window closes. Calling `window.close()` from code fires no `Closable.CLOSE`, so the dialog can close it without asking again:

```java
--8<-- "extending/ExtendingExamples.java:veto"
```

Draw on top of a button:

```java
--8<-- "extending/ExtendingExamples.java:paint"
```

Change how big a label wants to be:

```java
--8<-- "extending/ExtendingExamples.java:measure"
```

A handler you want on many controls is a plain field or method, so reuse costs nothing:

```java
--8<-- "extending/ExtendingExamples.java:reuse"
```

Each control lists its events with its theme keys in [Controls](controls.md), and every event type's Javadoc lists the input that fires it.

## Compose built-ins

When the piece is just existing components arranged a certain way, you need no Strata base class at all. Build the parts in a container and hand out the container:

```java
--8<-- "extending/ExtendingExamples.java:compose"
```

The class is not a `Component`, so you add `field.component()` rather than `field`. To make a composite that is a component itself, so it can be passed anywhere a component can, extend `CustomComposite`.

### A composite of private parts

A `CustomComposite` holds child components the way a container does, but only the composite can reach them. It adds them with `addChild`, lays them out in `measure` and `arrange` as a [layout](#write-a-new-layout) does, and callers see one component. This counter is a label between two buttons:

```java
--8<-- "extending/Counter.java:composite"
```

The control events of parts added with `addChild`, such as the buttons' `Button.PRESSED`, stop at the composite, so nothing outside hears them. The composite reports what the outside should hear with an event type of its own, here `Counter.CHANGED`, which bubbles like any other:

```java
--8<-- "extending/ExtendingExamples.java:composite-use"
```

A part that shows the caller's content, such as a cell a caller's cell factory made, goes in with `addContent` instead. It is still private, but control events from it and from anything inside it pass the composite, so a button in a cell is heard around the composite. `CustomComposite` also has `removeChild`, `clearChildren`, `childList()` and the ordering methods below, and `setChildClipping(false)` lets parts draw outside its bounds. There is no `clipsChildren()` to override: the setting is yours to make, and a `CustomContainer` reports it through its public `clipsChildren()`. `CustomContainer` is a composite whose children are public: callers add and remove them through the `Container` methods.

## Write a new component

Subclass `CustomComponent<T>`. To let other code see your component as a type of its own, declare an interface that extends `Component<YourInterface>` and implement it. This example builds a small switch that works much like Strata's own [`ToggleSwitch`](controls.md#toggleswitch).

```java
--8<-- "extending/ToggleSwitch.java:interface"
```

```java
--8<-- "extending/ToggleSwitchImpl.java:impl"
```

Using it looks like using a built-in:

```java
--8<-- "extending/ExtendingExamples.java:use"
```

Its required keys must be in the theme:

```java
--8<-- "extending/ExtendingExamples.java:theme"
```

### Events for your own control

Declare your control's events as constants on its interface, a Pre request for each change the user can make and a Post report for each change from any source, and fire them from the implementation:

```java
--8<-- "extending/ToggleSwitch.java:events"
```

```java
--8<-- "extending/ToggleSwitchImpl.java:events"
```

| Made with | Kind | Fire it with |
|---|---|---|
| `EventType.request(name)` | Pre, no value | `request(type)`, which returns whether to go ahead |
| `EventType.valueRequest(name)` | Pre, with a value | `request(type, value, apply)`, which calls `apply` with the final value unless canceled |
| `EventType.report(name)` | Post, no value | `report(type)` |
| `EventType.valueReport(name)` | Post, with a value | `report(type, value, previous)` |

Declare a `PropertyKey` beside the events for each property, as `ON` is above, and the control can be [bound](binding.md#binding-your-own-control) to a state like a built-in. Fire requests only for what the user does, and reports after every change, whoever made it, as the built-ins do. With no handler for a type on the path, `request` and `report` fire nothing and allocate nothing, and `request(type, value, apply)` just calls `apply`. `isHandled(type)` tells you whether anything listens, for work only an event needs. If your component is a `CustomComposite`, the events of the controls you add with `addChild` stop at it, so fire your own types for what the outside should hear. Events from content added with `addContent` pass through.

### The hooks

Every hook is a `protected` method you override, and Strata calls each one at the time the table gives.

| Hook | Called | Default |
|---|---|---|
| `measure(context, availableWidth, availableHeight)` | During layout, when the component or something inside it changed, or it is offered a different size. Return the content's natural size. Required. | |
| `measureMinimum(context)` | During layout, the first time a parent that shares space asks after the component changed. Return the smallest size the content can work in, such as the longest word of wrapping text. A stack's weighted children and a grid's weighted tracks get no less. | `Size.ZERO` |
| `arrange(context, bounds)` | On a `CustomComposite` or `CustomContainer`, after `measure`, with the component's final bounds. Give every child its slot. Required there. | |
| `paint(painter, bounds)` | Every frame, after the background and border and before a composite's children | Draws nothing |
| `paintOverChildren(painter, bounds)` | On a composite, every frame after its children, outside their clip | Draws nothing |
| `childClip()` | On a composite, while painting and hit testing, if it clips its children. Return a rectangle worked out during layout. | The bounds |
| `requiredStyleKeys()` | When the component joins a host, and when the theme changes | None |
| `onApplyTheme(theme)` | On attach and whenever the theme changes, before the next layout | Nothing |
| `onAttached()` | Every time the component joins a tree a host is showing, after `onApplyTheme` and the bindings, before its `Component.ATTACHED` handlers. See [Attach and detach](#attach-and-detach). | Nothing |
| `onDetached()` | Every time it leaves such a tree, after its `Component.DETACHED` handlers and while its bindings still run | Nothing |
| `baseline()` | During the parent's layout, after `measure`, if the parent lines up text. Return the distance from the top of the bounds to the first line's baseline. See [Lining up text](#lining-up-text). | NaN, for none |
| `acceptsTextInput()` | When it gains focus | false |
| `textInputKind()` | When it gains focus, if it accepts text input. Return `SECRET` for a secret, so the engine can turn off input methods and suggestions. | `TEXT` |
| `snapshotState()` | When an event with [snapshots](snapshots.md#your-own-components) captures it | null |
| `onPointerEnter`, `onPointerExit`, `onPointerMove`, `onPointerPress`, `onPointerRelease`, `onClick`, `onDragStart`, `onDrag`, `onDragEnd`, `onScroll`, `onKey`, `onChar` | The default-behavior step of the matching `InputEvents` type, after the component's handlers, unless one canceled it | Nothing |
| `onDragEnter`, `onDragOver`, `onDragExit`, `onDrop`, `onTransferDone` | The same, for the five events of a drag that carries data. See [Dragging data](#dragging-data). | Nothing |
| `onFocusGained`, `onFocusLost` | The default-behavior step of `Focusable.FOCUS_GAINED` and `FOCUS_LOST` | Nothing |
| `onOtherEvent(type, event)` | The default-behavior step of an input event type with no hook of its own, such as `InputEvents.MODIFIERS_CHANGED` on the screen's root. Control events, the requests and reports, have no default-behavior step. | Nothing |
| `onChildRemoved(child)` | On a composite, after a child is removed. Drop what you kept about it. | Nothing |
| `onDescendantFocused(descendant)` | On a composite, after focus moves to a component inside it, innermost composite first | Nothing |

`measure` receives the space its parent offered with the margin already taken off. Where the component has a fixed width or height, that size is offered in place of the parent's, and the maximum size caps both. A component that did not change and is offered the same space as last time keeps its last size without `measure` running, so do not count frames or poll for changes in `measure`.

`measureMinimum` takes no space: return what the content needs wherever it is, and read children's minimums with `context.minimumSize(child)` rather than measuring them. Strata raises the result to the minimum size, caps it by the maximum size, puts a fixed size in its place, and keeps it until the next `invalidateLayout()`. It does not make the component itself larger: `measure` can still be offered less, inside a fixed size for example. A negative or non-finite result counts as zero.

### When hooks run

A host does its work for a frame in this order, and only the steps something asked for do anything:

1. A host given its theme as a `ThemeRef` resolves it again if a theme file or an override in code changed. A screen that asked for a rebuild is built again, and bound properties are updated from their states. Expired toasts close, and a tooltip whose wait is over opens.
2. Layout: from the root down, each component that changed, or is offered a different size, is measured, then each one that was measured or given a different slot is arranged. Components that asked only to be arranged again, through `invalidateArrangement()`, are arranged in their last slot.
3. Painting: for each component, its `Component.PAINT` handlers run, then its background, border and `paint`, then its children in [drawing order](components.md#drawing-order), then `paintOverChildren`, then its `Component.PAINTED` handlers. Canceling `PAINT` skips the component's own drawing, `paintOverChildren` included, but not its children.

Input arrives between frames. A pointer event runs the layout first if something changed, so hit testing never sees stale bounds. Then the event fires: on each component of its path, the handlers run and then the component's `on...` hook, unless a handler canceled it. `onApplyTheme` runs when a component is attached and when the theme changes, before the next layout.

### Attach and detach

A component is attached when it is in a tree a host shows, and detached when it leaves one. Adding it to an attached container attaches it, removing it detaches it, and so do an expander showing or hiding its content, a navigation view swapping pages, an overlay opening or closing, and a screen opening, closing or being rebuilt. The same two steps run in every case:

1. On attach, the host applies the theme (`onApplyTheme`), sets the properties of the bindings that joined, and then calls `onAttached()` and fires `Component.ATTACHED` on each component, parents before their children. The component's hook runs before its handlers.
2. On detach, the host first fires `Component.DETACHED` and calls `onDetached()` on each component, children before their parents, while the tree is still attached and its bindings still run. The handlers run before the component's hook. Then focus, hover and the tooltip are cleared and the tree is detached.

Each attach a component hears is followed by exactly one detach. A throwing handler or hook does not stop the walk; the first exception is thrown at the end. The events are made only when a component has a handler for them, and they do not bubble. [Events](events.md#attaching-and-detaching) covers them from the handler's side.

Start what a component needs only while it is shown in `onAttached()` and stop it in `onDetached()`. This chart listens to a feed of prices only while it is on a screen:

```java
--8<-- "extending/HookExamples.java:lifecycle"
```

Code outside a component does the same with `whileAttached(start)`, which calls the supplier at once if the component is attached, again on every attach, and closes the returned `Subscription` on every detach:

```java
--8<-- "extending/HookExamples.java:while-attached"
```

### Rules

- Pass the factory of the host that will show the component. A component cannot move between hosts.
- `measure` must be pure layout. Compute and return a size, and cache what `paint` needs. Strata applies the minimum and any fixed size to what you return. The available size may be infinite, and a negative result counts as zero. Do not change other components or call `invalidateLayout()` from it. A composite may add and remove its own children during layout, as a list adds the rows that scroll into view; that schedules one more layout.
- `paint` draws inside the bounds it is given and must not change layout state. It runs every frame, so avoid allocating there. The painter is valid only during the call.
- Call `invalidateLayout()` whenever something `measure`, `measureMinimum` or `baseline` reads changes. A change that only `paint` reads needs `repaint()` instead, and a change the clock brings needs `repaintAfter(millis)` or `repaintNextFrame()`; see [Asking for frames](#asking-for-frames).
- Read time, the clipboard and text sizes through `environment()`, never from `System.currentTimeMillis()` or AWT, so the component works on every engine and in tests.
- Event hooks run only while the component is attached, and a hidden or disabled component gets no new pointer or keyboard input.
- Consume the events you act on, so ancestors and the engine do not act on them too.
- Everything runs on the UI thread, and a component that a host shows may be changed only there; see [The UI thread](threading.md). `repaint()` is the exception. Exceptions thrown from hooks propagate to the engine's call into Strata.

Every `Component` method is `final` on `CustomComponent` and delegates to Strata's half of the component, its peer, so your component takes part in layout, focus, hit testing and events like any other. Return `this.self()` from your own setters to keep chaining typed.

`CustomComponent` also has the methods of `Focusable`, `HasCursor` and `HasTooltip`. They take effect once your interface extends the matching one, as `ToggleSwitch` does above, and Strata ignores them otherwise. Declaring `Focusable` makes the component focusable and tabbable from the start.

### Helpers for your own code

These `protected` methods are for the subclass's own drawing, layout and input code:

| Method | Does |
|---|---|
| `self()` | Returns `this` as `T`, for returning from your own setters |
| `environment()` | The engine's text metrics, clock, clipboard and held modifiers; see [The environment](#the-environment) |
| `theme()` | The host's theme as an `Optional`, empty before the first attach, for reading styles outside `onApplyTheme`. It has the [text style patches](layout.md#inherited-text-styles) of the containers around the component applied. |
| `setStyleState(state, on)`, `stylePaint(key, fallback)`, `styleText(key, fallback)` | Name your own style states and look up the theme's paints and text styles for them; see [Style states](#style-states) |
| `repaint()`, `repaintAfter(millis)`, `repaintNextFrame()` | Ask the host for a frame; see [Asking for frames](#asking-for-frames) |
| `isHovered()` | Whether the pointer is over this component itself, and not over a child |
| `isHoverWithin()` | Whether the pointer is over this component or anything inside it |
| `isPressed()` | Whether a button went down on this component and is still held, even if the pointer moved away |
| `offsetX()`, `offsetY()`, `setOffset(dx, dy)` | Moves the component and everything inside it from where layout put it, without a new measure. Make `setOffset` public to let callers move it, as a window does. |
| `background()`, `setBackground(paint)`, `border()`, `setBorder(border)` | Paint drawn over the bounds before `paint`, and lines along the inside of the edges. `CustomContainer` makes them public. |
| `invalidateLayout()` | Schedules a measure and arrange before the next frame |
| `invalidateArrangement()` | On a composite, schedules an arrange in the last bounds with no measure, for children that move without changing size, as in scrolling |
| `request(...)`, `report(...)`, `isHandled(type)` | Fire your own [events](#events-for-your-own-control) |

The public methods follow the same rules as the built-in controls'. A getter whose value can be absent returns an `Optional`: `parent()`, `cursor()`, `tooltip()`, `contextMenu()` and `theme()`. `setCursor` and `setContextMenu` take null to clear; `setTooltip` never takes null, and `clearTooltip()` removes the tooltip. Every `CustomComponent` has `clearTooltip()` whether or not its interface extends `HasTooltip`, and like the other tooltip methods it has an effect only when it does.

### Painting

The `Painter` a component draws with:

| Method | Draws |
|---|---|
| `fill(rect, argb)` | A solid rectangle |
| `fillGradient(rect, top, bottom)` | A vertical gradient |
| `fillGradient(rect, gradient)` | A gradient at any angle, with any number of stops |
| `sprite(id, rect, tint)` | An image stretched over the rectangle |
| `nineSlice(id, rect, border, tint)` | A nine-slice image |
| `nineSlice(id, rect, size, border, tiled, tint)` | A nine-slice whose image stands for `size` units, with its edges and center repeated when `tiled` |
| `tiled(id, rect, size, tint)` | An image repeated from the top left, one copy per `size` |
| `visual(visual, rect, tint)` | An engine visual, such as an image the adapter made |
| `text(span, x, y)` | One line of text, top-left at the point |
| `clipped(rect, draw)` | Runs `draw` with drawing limited to the rectangle, intersected with any clip in place |
| `translated(dx, dy, draw)` | Runs `draw` with every coordinate shifted |
| `scaled(scale, draw)` | Runs `draw` with every coordinate and size multiplied by `scale` |
| `pixelsPerUnit()` | How many device pixels one unit covers where the painter draws now |

The lines, shapes, shadows and pixels the painter also draws are in [Lines, shapes and pixels](#lines-shapes-and-pixels).

Coordinates are host coordinates in UI units. Later calls draw over earlier ones. Clipping, translation and scaling take a callback, so the engine's state is always restored, even if the callback throws. Any `Paint` from a theme draws itself with `paint.paint(painter, rect)`.

### Lines, shapes and pixels

A dashboard needs a line chart, a gauge, cards with rounded corners and soft shadows, and connectors between boxes. The painter draws all of them on every engine, with the engine's own shape drawing where it has one and from rectangles where it has not.

| Method | Draws |
|---|---|
| `line(x1, y1, x2, y2, stroke, argb)` | A straight line, centered on the segment |
| `polyline(points, count, stroke, argb)` | Connected lines through `count` points, given as x and y pairs in one array |
| `fillRoundRect(rect, radius, argb)` | A rectangle with rounded corners |
| `strokeRect(rect, radius, stroke, argb)` | The outline of a rectangle, inside it |
| `fillEllipse(oval, argb)`, `strokeEllipse(oval, stroke, argb)` | An ellipse, and its outline inside the rectangle it fits |
| `strokeArc(oval, startRadians, sweepRadians, stroke, argb)` | Part of an ellipse's outline, inside the rectangle, with the stroke's caps |
| `fillShape(shape, argb)`, `strokeShape(shape, stroke, argb)` | A `Shape` made of lines, curves and arcs, and its outline centered on the path |
| `shadow(rect, radius, blur, argb)` | The soft shadow of a rounded rectangle |
| `clippedRound(rect, radius, draw)` | Runs `draw` limited to a rectangle with rounded corners |
| `smoothed(smooth, draw)` | Runs `draw` with anti-aliasing on or off |
| `pixels(buffer, rect, tint)` | A `PixelBuffer` stretched over the rectangle |

Colors are ARGB, and a color with alpha 0 draws nothing. The values these calls take are plain objects you make once and keep in fields, so a frame allocates nothing for them:

- `Stroke` has a width, a `Cap` (`BUTT`, `ROUND`, `SQUARE`) and a `Join` (`MITER`, `ROUND`, `BEVEL`). `Stroke.of(width)` has butt caps and miter joins, and a width of 0 draws nothing.
- `Gradient` has an angle in radians and at least two stops with offsets from 0 to 1 that do not go down. Angle 0 runs left to right and a quarter turn runs top to bottom. The stops span the whole rectangle at any angle, and the colors before the first stop and after the last hold.
- `Shape` is built once with `Shape.builder()`: `moveTo`, `lineTo`, `quadTo`, `cubicTo`, `arc` and `close`. It never changes after `build()`. A fill uses the nonzero rule, so an inner subpath that runs the other way is a hole.
- `PixelBuffer` holds ARGB pixels in the array `array()` returns. Write the pixels on the UI thread and call `changed()`; an engine copies the buffer again only when its `version()` moved.

A chart draws its grid and a line through its values. The grid lines are one device pixel thick at any density, and the line comes from one array that every frame refills:

```java
--8<-- "extending/DrawingExamples.java:chart"
```

A gauge is two arcs. An angle is in radians and turns clockwise on the screen from 3 o'clock, so this ring starts at the lower left and sweeps three quarters of a turn:

```java
--8<-- "extending/DrawingExamples.java:gauge"
```

A card draws its shadow first, then its face, then a gradient at an angle that `clippedRound` keeps inside the rounded corners, and last its outline:

```java
--8<-- "extending/DrawingExamples.java:card"
```

A `Shape` kept in a static field draws at any size when the painter is moved and scaled; the second shape here has a hole:

```java
--8<-- "extending/DrawingExamples.java:shape"
```

Pixels you compute go into a buffer. Hard edges keep pixel art sharp:

```java
--8<-- "extending/DrawingExamples.java:pixels"
```

The rules for all of them:

- Lines, polylines and shape outlines are centered on their path. Outlines of rectangles, ellipses and arcs lie inside their rectangle or oval, with the stroke's outer edge on it, so a border drawn over a component's bounds stays inside them. Their caps and joins do not apply, except an arc's caps. A stroke wider than half the rectangle fills it.
- A radius below 0 counts as 0, and one larger than half the shorter side counts as half of it.
- Angles are radians. Angle 0 is at the right of an oval and angles turn clockwise. A sweep of a full turn or more draws the whole outline.
- A shadow is the color over the rectangle, fading to nothing over `blur` units outside its edges. A blur of 0 or less gives a hard edge. Draw it before what casts it, at an offset if the light comes from a side.
- `smoothed(false, ...)` asks for hard edges. Shapes are anti-aliased where the engine can.
- A round clip limits drawing only. The component's hit area stays its rectangle.
- A `polyline` array is read during the call and not kept. A count below 0 or larger than the array throws `IllegalArgumentException`, and fewer than two points draw nothing.
- An engine that draws only rectangles still draws everything here, built from fills one device pixel high. Its shapes look stepped, its round clips are square, and its shadows are a few rings of falling alpha. An engine that cannot draw pixels shows a placeholder for a `PixelBuffer`. [Platform differences](../platforms/differences.md) lists which adapters draw shapes themselves.

### Display density

A display at 150 or 200 percent has more device pixels than units. `painter.pixelsPerUnit()` says how many device pixels one unit covers where the painter draws now: the host's density times every `scaled` call in progress. A line `1 / painter.pixelsPerUnit()` units thick is one device pixel thick, which the chart above uses for its grid. `environment().pixelsPerUnit()` is the host's density, for work outside `paint`. It is 1 until the adapter says otherwise, and a change lays everything out again.

An adapter may have denser images for a sprite, and draws the densest one the device pixels can show at the same size in units. A component can also choose between two sprites itself:

```java
--8<-- "extending/DrawingExamples.java:density"
```

### Asking for frames

Some adapters draw only when a host says it needs a frame, which saves power in an application that sits idle. The built-in controls and layout invalidation already ask. Three calls are for a component whose look changes in a way Strata cannot see:

- `repaint()` says the look changed and the layout did not, as when a setter changes only what `paint` reads. The gauge and the chart above call it from their setters. Any thread may call it.
- `repaintAfter(millis)` says the look changes by itself after a time, such as a blinking cursor or a clock's next second. Call it from `paint` for the next change only, and again on the next paint. The same deadline registered twice counts once. The delay is by the clock `environment().millis()` reads, and the call must be on the UI thread.
- `repaintNextFrame()` says the look keeps moving with the clock, such as a needle easing toward its value or a pattern that scrolls. Call it from `paint` while the motion lasts. It asks for the next frame the host's frame rate allows, 30 a second unless the adapter sets another, so paint from the clock rather than counting frames: a lower rate then shows the same motion in fewer steps. Any thread may call it.

```java
--8<-- "extending/HookExamples.java:clock"
```

All three do nothing while the component is detached. On an engine that draws every frame they cost nothing, so a component that forgets them looks right there and stale elsewhere, until the next input.

### The environment

`environment()` returns an `Environment`, which gives a component what it may ask of the engine outside layout:

| Method | Returns |
|---|---|
| `text()` | The engine's text metrics, the same ones layout uses |
| `millis()` | The host's clock in milliseconds. Only differences mean anything. |
| `clipboardText()` | The clipboard text, or empty |
| `setClipboardText(text)` | Puts text on the clipboard. Engines without one ignore it. |
| `modifiers()` | The modifier keys held as of the latest input, or `NONE` before any |
| `pixelsPerUnit()` | The host's device pixels per UI unit, 1 until the adapter sets it; see [Display density](#display-density) |
| `clipboard()`, `setClipboard(transfer)` | The clipboard with every format; see [Clipboard](clipboard.md) |

It works before the component is attached.

### Style states

A built-in control looks up a theme key for each of its states, such as `button.hovered`. A component of yours can do the same, with states of its own. Name a state with `setStyleState(state, on)`, and read the theme with `stylePaint(key, fallback)` and `styleText(key, fallback)`:

```java
--8<-- "extending/HookExamples.java:states"
```

Strata sets `disabled`, `pressed`, `hovered` and `focused` itself, from the component's own state, so those four names, and an empty one, throw `IllegalArgumentException` when you set them. A component can have up to 60 states of its own. A lookup tries the keys in this order, and the first one the theme has wins:

1. `key.disabled`, when the component or an ancestor is disabled.
2. `key.<state>` for each of the component's own states that is on, in the order each was first set.
3. `key.pressed`, `key.hovered` and `key.focused`, for those that are on.
4. `key`.

So with `warning` on, `examplemod.card.warning` wins over `examplemod.card`, and a disabled card shows `examplemod.card.disabled`. The fallback is returned when the theme has none of the keys, and when the component was never attached. The result for each combination of states is kept until the theme changes, so a lookup in `paint` allocates nothing after the first one with the same states.

Changing a state marks nothing for layout. Call `invalidateLayout()` when a state's style changes the size, and `repaint()` when only the look changes. List the plain key in `requiredStyleKeys()` if the component cannot draw without it, and leave the state keys optional.

### Dragging data

A drag that carries data, set up with `startTransfer` from a `DRAG_START` handler or `onDragStart`, sends five events to the components it passes over. A component handles them with handlers, or with the matching hooks: `onDragEnter` and `onDragExit` when the drag arrives at and leaves the component, `onDragOver` on every move, `onDrop` when it is released on the component that accepted last, and `onTransferDone` on the source when the drag ends. In `onDragOver`, call `event.accept(effect)` to take the drop or `event.reject()` to refuse it. [Drag and drop](drag-and-drop.md) covers the events, effects and formats.

A tile starts a drag that carries its name as text, and a bin takes text:

```java
--8<-- "extending/HookExamples.java:drag"
```

### Lining up text

A parent that lines up its children's text asks each child where its baseline is. Override `baseline()` to return the distance from the top of your bounds to the baseline of your first line of text. The ascent the text metrics report is the distance from the top of a line to its baseline, so a component that draws one line of text at a top of `y` has its baseline at `y` plus the ascent, as the tag below does. The hook runs after `measure`, for the component at its desired height, so read the ascent in `measure` and keep it. The default returns NaN, for no baseline, and a parent then lines up the component's bottom edge instead.

A container of yours reads children's baselines with `context.baseline(child)` on the `MeasureContext` and the `ArrangeContext`. It returns the baseline from the top of the child's margin, or NaN for a hidden child or one with none. This row puts the baselines of its children on one line:

```java
--8<-- "extending/HookExamples.java:baseline"
```

Strata's horizontal stack panel, the rows of a wrap panel and single-row grid cells do the same for children aligned `VerticalAlignment.BASELINE`; see [Layout](layout.md#lining-up-text).

## Write a new layout

Subclass `CustomContainer<T>`. It is a `CustomComponent` that also measures and places children. This flow layout wraps its children into rows like words in a paragraph. Strata has a [`WrapPanel`](layout.md#wrappanel) of its own with spacing, line alignment and baselines, so write one like this only to learn how a layout works or to place children differently:

```java
--8<-- "extending/WrapPanel.java:wrap"
```

Everything required of a custom component applies, plus:

- In `measure`, measure every child, hidden or not, with `context.measure(child, width, height)`. It returns the child's desired size plus its margin, or zero for a hidden child. A child you do not measure keeps a stale size.
- In `arrange`, give every child a slot with `context.arrange(child, slot)`, hidden children included. The slot includes the child's margin; the child shrinks it by the margin and aligns itself inside. `context.outerSize(child)` returns what the child asked for in the last measure. A child you do not arrange keeps stale bounds.
- Handle the padding yourself: take it off the space you offer children, add it to the size you return, and place children inside `contentBox()`.
- Keep per-child data, such as a grid cell, in your own map, and drop it in `onChildRemoved`.
- Leave drawing the children to Strata. Your `paint` draws under them and `paintOverChildren` over them. While `clipsChildren()` is true they are clipped to `childClip()`, your bounds by default.
- Expect callers to add and remove children at any time outside layout. Each change schedules a new layout.

### The layout contexts

`measure` receives a `MeasureContext` and `arrange` an `ArrangeContext`. Both are valid only during the call they were passed to.

| Context | Method | Does |
|---|---|---|
| `MeasureContext` | `text()` | The engine's text metrics, for measuring text in your own content |
| `MeasureContext` | `measure(child, width, height)` | Measures a child in the space offered, margin included. Either size may be infinite. Returns the child's desired size plus its margin, or zero for a hidden child. |
| `MeasureContext` | `baseline(child)` | The child's baseline from the top of its margin, or NaN if it is hidden or has none. See [Lining up text](#lining-up-text). |
| `MeasureContext` | `minimumSize(child)` | The child's smallest size, margin included: its `measureMinimum` raised to its minimum size and capped by its maximum, or its fixed size, or zero for a hidden child. It may be read before or after the child is measured. |
| `ArrangeContext` | `text()` | The engine's text metrics |
| `ArrangeContext` | `outerSize(child)` | What the child asked for in the last measure, margin included, or zero for a hidden child |
| `ArrangeContext` | `minimumSize(child)` | The same as on `MeasureContext` |
| `ArrangeContext` | `baseline(child)` | The same as on `MeasureContext` |
| `ArrangeContext` | `arrange(child, slot)` | Gives the child its slot in host coordinates, margin included. A slot smaller than the child is allowed; the child overflows it unless it stretches. |
| `ArrangeContext` | `measure(child, width, height)` | Measures a child added during `arrange`, such as a row that scrolled into view, before you give it a slot |

Slots and bounds are in host coordinates, so place children relative to `bounds` or `contentBox()`, never from 0.

Callers order a container's children with `insert`, `move`, `raise`, `lower` and z indexes (see [Drawing order](components.md#drawing-order)), and Strata keeps both orders for you: iterate `children()` in `measure` and `arrange`, and leave the drawing order to Strata. A `CustomComposite`, whose children are private, orders them itself with the protected `insertChild(index, child)`, `moveChild(child, index)`, `raiseChild(child)` and `lowerChild(child)`, which work as the `Container` methods do. A progress tree, for example, can raise the node under the pointer so it draws over its neighbors.

## Share your components

A component you write can be made by anyone with a `ComponentFactory`, without them knowing your classes or your entry points. Register it with the component registry, under the type callers see it as and an id:

```java
--8<-- "extending/SwitchLibrary.java:library"
```

List the class in `META-INF/services/org.inspirenxe.strata.api.component.registry.ComponentLibrary`, one name per line:

```text
com.example.switches.SwitchLibrary
```

Strata finds it the first time anyone uses the registry, so the people who use your library never call an init method. On an engine that gives each plugin its own class loader, the engine's adapter tells Strata about each plugin's loader with `Strata.components().load(loader)`. If `register` throws, Strata removes what it registered, logs the error and goes on without your library. You can also register from code at any time with `Strata.components().register(...)`, which returns a `Subscription` that removes the registration.

The type is the key, so your implementation class can stay package-private. Every component the maker returns must be an instance of that type, made with the factory the maker is given; the factory checks both and throws `IllegalStateException` naming your registration if either is wrong. Strata's own control types and the `strata` namespace are closed to registration.

### Making a registered component

```java
--8<-- "extending/ExtendingExamples.java:create"
```

| Call | Returns | When nothing is registered |
|---|---|---|
| `create(Type.class)` | A new component of that type | Throws `IllegalStateException` |
| `tryCreate(Type.class)` | The component, or empty | Empty |
| `create(id)` | A new `Component<?>` | Throws `IllegalStateException` |
| `tryCreate(id)` | The component, or empty | Empty |

A type with no registration of its own finds the newest registration of a subtype, so you can ask for an interface the registered type extends, or for an attribute such as `Ranged`. Strata's controls are registered too, under their interfaces and ids such as `strata:button` and `strata:list_view`, so `factory.create(Slider.class)` is the same as `factory.slider()`. A later registration for the same type or id hides an earlier one until it is removed.

### Using a component you do not compile against

Attributes own their events, so a component made by id can be used through every attribute it implements, with nothing from its library on your class path:

```java
--8<-- "extending/ExtendingExamples.java:attributes"
```

This is the reason to implement every [attribute](controls.md#attributes) that fits a component you share: a slider-like component should be `Ranged`, an on/off one `Toggle`, a list one `SingleSelection`. Code written against the attributes then works with your component and with Strata's alike.

## Strata's components and yours

Strata provides the components `ComponentFactory` makes. You see them through the interfaces in `api.control` and `api.layout`, such as `Button`, `ListView` and `StackPanel`, and their implementations stay in core.

You write subclasses of `CustomComponent`, `CustomComposite` and `CustomContainer`, and so does any other library that adds components. Strata's own components do not extend these classes, so a component that is an instance of `CustomComponent` was written outside Strata, and anything the factory's named methods (`button()`, `listView()` and the rest) made is Strata's. `create` makes whatever is [registered](#share-your-components), which can be either. Both kinds go into the same containers and lay out, draw and fire events the same way, and either can hold the other. The parts and content of a `CustomComposite` have the composite as their `parent()`, so code inside them that needs it can find it with `ancestor(YourComposite.class)` (see [The tree](components.md#the-tree)).

### How the base classes work

The constructor of `CustomComponent` asks the factory you pass for a peer: Strata's half of the component, which holds its bounds, flags, layout inputs, handlers and bindings. `ComponentFactory.createPeer` makes a `ComponentPeer` for a leaf, and `createContainerPeer` a `ContainerPeer`, which adds children, for a `CustomComposite` or `CustomContainer`. That is why a component needs its factory before it can do anything, and why it belongs to that factory's host.

Every `Component` method on the base class is `final` and calls the peer. In the other direction, the base class hands the peer a private `ComponentHooks` object, and the peer calls your `protected` hooks only through it, so no other code can call them. Strata's own controls use the same kind of node behind the scenes, which is why both kinds behave alike.

All of this is in `api.extension.peer` and is plumbing. You never call these methods or implement these interfaces; extending the base class sets everything up.

## Things you cannot do

- Implement `Component`, `Container` or a control interface directly. Strata rejects such classes.
- Subclass the built-in controls. Their implementations are hidden in `core`. Handle their events, compose them, or write your own.
- Draw textured triangles, radial gradients or dashed strokes. The painter draws rectangles, lines, shapes, linear gradients, shadows, sprites, pixels and text. See [Status and limitations](../help/status.md).
- Use `ComponentFactory.createPeer`, `createContainerPeer`, `CustomComponent.peer()` or anything in `api.extension.peer`, such as `ComponentHooks`, `ComponentPeer` and `ContainerPeer`. They are plumbing between the base classes and core; see [How the base classes work](#how-the-base-classes-work).
