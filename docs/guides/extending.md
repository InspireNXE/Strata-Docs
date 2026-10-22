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

A part that shows the caller's content, such as a cell a caller's cell factory made, goes in with `addContent` instead. It is still private, but control events from it and from anything inside it pass the composite, so a button in a cell is heard around the composite. `CustomComposite` also has `removeChild`, `clearChildren`, `childList()` and the ordering methods below, and `setChildClipping(false)` lets parts draw outside its bounds. `CustomContainer` is a composite whose children are public: callers add and remove them through the `Container` methods.

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
| `acceptsTextInput()` | When it gains focus | false |
| `textInputKind()` | When it gains focus, if it accepts text input. Return `SECRET` for a secret, so the engine can turn off input methods and suggestions. | `TEXT` |
| `snapshotState()` | When an event with [snapshots](snapshots.md#your-own-components) captures it | null |
| `onPointerEnter`, `onPointerExit`, `onPointerMove`, `onPointerPress`, `onPointerRelease`, `onClick`, `onDragStart`, `onDrag`, `onDragEnd`, `onScroll`, `onKey`, `onChar` | The default-behavior step of the matching `InputEvents` type, after the component's handlers, unless one canceled it | Nothing |
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

### Rules

- Pass the factory of the host that will show the component. A component cannot move between hosts.
- `measure` must be pure layout. Compute and return a size, and cache what `paint` needs. Strata applies the minimum and any fixed size to what you return. The available size may be infinite, and a negative result counts as zero. Do not change other components or call `invalidateLayout()` from it. A composite may add and remove its own children during layout, as a list adds the rows that scroll into view; that schedules one more layout.
- `paint` draws inside the bounds it is given and must not change layout state. It runs every frame, so avoid allocating there. The painter is valid only during the call.
- Call `invalidateLayout()` whenever something `measure` or `measureMinimum` reads changes. Changes that only affect drawing need nothing: the next frame draws them.
- Read time, the clipboard and text sizes through `environment()`, never from `System.currentTimeMillis()` or AWT, so the component works on every engine and in tests.
- Event hooks run only while the component is attached, and a hidden or disabled component gets no new pointer or keyboard input.
- Consume the events you act on, so ancestors and the engine do not act on them too.
- Everything runs on the UI thread. Exceptions thrown from hooks propagate to the engine's call into Strata.

Every `Component` method is `final` on `CustomComponent` and delegates to Strata's half of the component, its peer, so your component takes part in layout, focus, hit testing and events like any other. Return `this.self()` from your own setters to keep chaining typed.

`CustomComponent` also has the methods of `Focusable`, `HasCursor` and `HasTooltip`. They take effect once your interface extends the matching one, as `ToggleSwitch` does above, and Strata ignores them otherwise. Declaring `Focusable` makes the component focusable and tabbable from the start.

### Helpers for your own code

These `protected` methods are for the subclass's own drawing, layout and input code:

| Method | Does |
|---|---|
| `self()` | Returns `this` as `T`, for returning from your own setters |
| `environment()` | The engine's text metrics, clock, clipboard and held modifiers; see [The environment](#the-environment) |
| `theme()` | The host's theme, or `null` before the first attach, for reading styles outside `onApplyTheme` |
| `isHovered()` | Whether the pointer is over this component itself, and not over a child |
| `isHoverWithin()` | Whether the pointer is over this component or anything inside it |
| `isPressed()` | Whether a button went down on this component and is still held, even if the pointer moved away |
| `offsetX()`, `offsetY()`, `setOffset(dx, dy)` | Moves the component and everything inside it from where layout put it, without a new measure. Make `setOffset` public to let callers move it, as a window does. |
| `background()`, `setBackground(paint)`, `border()`, `setBorder(border)` | Paint drawn over the bounds before `paint`, and lines along the inside of the edges. `CustomContainer` makes them public. |
| `invalidateLayout()` | Schedules a measure and arrange before the next frame |
| `invalidateArrangement()` | On a composite, schedules an arrange in the last bounds with no measure, for children that move without changing size, as in scrolling |
| `request(...)`, `report(...)`, `isHandled(type)` | Fire your own [events](#events-for-your-own-control) |

### Painting

The `Painter` a component draws with:

| Method | Draws |
|---|---|
| `fill(rect, argb)` | A solid rectangle |
| `fillGradient(rect, top, bottom)` | A vertical gradient |
| `sprite(id, rect, tint)` | An image stretched over the rectangle |
| `nineSlice(id, rect, border, tint)` | A nine-slice image |
| `nineSlice(id, rect, size, border, tiled, tint)` | A nine-slice whose image stands for `size` units, with its edges and center repeated when `tiled` |
| `tiled(id, rect, size, tint)` | An image repeated from the top left, one copy per `size` |
| `visual(visual, rect, tint)` | An engine visual, such as an image the adapter made |
| `text(span, x, y)` | One line of text, top-left at the point |
| `clipped(rect, draw)` | Runs `draw` with drawing limited to the rectangle, intersected with any clip in place |
| `translated(dx, dy, draw)` | Runs `draw` with every coordinate shifted |
| `scaled(scale, draw)` | Runs `draw` with every coordinate and size multiplied by `scale` |

Coordinates are host coordinates in UI units. Later calls draw over earlier ones. Clipping, translation and scaling take a callback, so the engine's state is always restored, even if the callback throws. Any `Paint` from a theme draws itself with `paint.paint(painter, rect)`.

### The environment

`environment()` returns an `Environment`, which gives a component what it may ask of the engine outside layout:

| Method | Returns |
|---|---|
| `text()` | The engine's text metrics, the same ones layout uses |
| `millis()` | The host's clock in milliseconds. Only differences mean anything. |
| `clipboardText()` | The clipboard text, or empty |
| `setClipboardText(text)` | Puts text on the clipboard. Engines without one ignore it. |
| `modifiers()` | The modifier keys held as of the latest input, or `NONE` before any |

It works before the component is attached.

## Write a new layout

Subclass `CustomContainer<T>`. It is a `CustomComponent` that also measures and places children. This flow layout wraps its children into rows like words in a paragraph:

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
| `MeasureContext` | `minimumSize(child)` | The child's smallest size, margin included: its `measureMinimum` raised to its minimum size and capped by its maximum, or its fixed size, or zero for a hidden child. It may be read before or after the child is measured. |
| `ArrangeContext` | `text()` | The engine's text metrics |
| `ArrangeContext` | `outerSize(child)` | What the child asked for in the last measure, margin included, or zero for a hidden child |
| `ArrangeContext` | `minimumSize(child)` | The same as on `MeasureContext` |
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
- Draw arbitrary geometry, such as lines or textured triangles. The painter offers rectangles, gradients, sprites and text only, because every engine can draw those. See [Status and limitations](../help/status.md).
- Use `ComponentFactory.createPeer`, `createContainerPeer`, `CustomComponent.peer()` or anything in `api.extension.peer`, such as `ComponentHooks`, `ComponentPeer` and `ContainerPeer`. They are plumbing between the base classes and core; see [How the base classes work](#how-the-base-classes-work).
