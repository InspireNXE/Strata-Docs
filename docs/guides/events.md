# Events

Everything a component does reaches your code as an event: input such as clicks and keys, a control's changes such as an edit or a close, and even drawing and sizing. There is one way to listen to all of them, `onEvent`. This page covers registering handlers, the two kinds of event, the event interfaces and what they carry, the order things run in, cancellation and bubbling, and how input becomes events. For comparing state before and after an event, see [Event snapshots](snapshots.md).

## Registering a handler

```java
component.onEvent(InputEvents.CLICK, event -> { ... });                         // priority 0
component.onEvent(InputEvents.CLICK, -100, event -> { ... });                   // runs before priority 0
Subscription drag = component.subscribe(InputEvents.DRAG, 0, event -> { ... }); // removable later
```

`onEvent` and `subscribe` come from `EventTarget`, which every component implements. Two things that are not components are event targets too. A `RadioGroup` reports `SingleSelection.SELECTED` when its selected button changes, and an `Overlay` reports `Closable.CLOSED` when it closes. Their events reach only the handlers registered on the group or the overlay.

Event types are typed keys. `InputEvents.CLICK` is an `EventType<PointerButtonEvent>`, so the handler receives a `PointerButtonEvent` without a cast. Input types are the constants in `InputEvents`, and the focus events are on `Focusable`. A control's events are constants on the [attributes](controls.md#attributes) it has, such as `TextEntry.MODIFIED` for every field the user types into and `Selection.SELECTED` for every list, table, tree and other control that selects, or on the control itself for what only it does, such as `Button.PRESS`. Every component has `Component.PAINT`, `Component.PAINTED` and `Component.MEASURED`. Always use the constants, because types are compared by identity. The [event type reference](../reference/event-types.md) lists every type.

A handler is an `EventHandler`, a functional interface with one method, `handle(event)`. A lambda or a method reference fits, and one handler kept in a variable can be registered on several components:

```java
--8<-- "events/EventExamples.java:handler"
```

`onEvent` returns the component, so registration chains with the setters:

```java
--8<-- "events/EventExamples.java:chain"
```

Handlers run on the UI thread, as everything that touches components does.

### Priority

On one component, handlers for a type run in ascending priority, and handlers with the same priority run in the order they were registered. A handler registered without a priority has priority 0, and the highest number runs last. Use a negative number to see an event before other handlers, for example to veto first, and a positive one to see what everyone before you decided, for example the final value of a request. A handler may register or remove handlers while it runs; the change applies from the next event.

## Pre and Post

Events come in two kinds.

| Kind | What it says | Can cancel | Fires for | Examples |
|---|---|---|---|---|
| Pre | "About to do this" | Yes, and requests with a value can be rewritten with `setValue` | Input, and the user's actions on a control | `KEY`, `CLICK`, `Button.PRESS`, `TextInput.EDIT`, `Closable.CLOSE`, `Component.PAINT` |
| Post | "This happened" | No | Every change, whether the user or code made it | `Button.PRESSED`, `TextInput.EDITED`, `Closable.CLOSED`, `Component.PAINTED` |

A control's Pre events are its requests and its Post events are its reports. To stop or change something, handle its request. To react to it, handle its report.

```java
--8<-- "events/EventExamples.java:veto"
```

```java
--8<-- "events/EventExamples.java:rewrite"
```

```java
--8<-- "events/EventExamples.java:react"
```

Changes made from code, through setters such as `setText` and methods such as `select` or `close`, fire no request: code does not need permission. Neither do changes a [binding](binding.md) writes or that another person made through the server. They all still fire reports, so a label or a binding hears about every change, and each report says [who caused it](#who-caused-it).

A rewritten value goes through the same checks as one set from code, so a slider still clamps a value a handler set out of range, and a text box still applies its length limit. Each event type's Javadoc says what it accepts and whether null is allowed.

A control fires a request or a report only when something on its path has a handler for that type. Otherwise it makes the change without creating an event, so the events you do not listen to cost nothing.

## Who caused it

Every event has an origin, which says who caused it:

| Origin | Who | Put by |
|---|---|---|
| `Origin.User` | The person at this screen, through input. Its `identity()` is the platform's object for them, such as a user profile, or null where the platform names nobody. | The host, for every input it dispatches |
| `Origin.Remote` | Another person, whose change reached this screen through a server or another channel | Your code, around the change it applies |
| `Origin.Binding` | A [data binding](binding.md), with the source it reads | The binding, while it writes |
| `Origin.CODE` | A plain call from code | Nobody: it is what an event reports when no frame put an origin |

`event.origin()` reads it. Requests fire only when the origin is the user, so a handler on a request can rely on that. Reports fire for every origin, which is where telling them apart pays off:

```java
--8<-- "events/EventExamples.java:origin"
```

The origin is one value of the event's cause. `event.cause()` gives the rest: a chain of the objects the change passed through, most recent first, and a context of values under `CauseKeys`. For input the host puts the device (`DEVICE`, `MOUSE` or `KEYBOARD`), the modifiers held, the screen, and for pointer presses and releases the button and the click count, so every event one click causes shares them. A control that acts on another pushes itself on the chain: when a radio group deselects a button, the button's `TOGGLED` still reports the user's click as its origin, and the group is on its chain.

```java
--8<-- "events/EventExamples.java:chain-of-causes"
```

Strata makes the cause only when a handler calls `cause()`, and `origin()` reads the origin without making it, so neither costs anything when nobody asks.

### Applying a change for someone else

Code that applies a change on another person's behalf, such as a market update the server sent, opens a frame with that person as the origin. Every event fired inside it reports them, and the controls' requests stay quiet:

```java
--8<-- "events/EventExamples.java:remote"
```

A frame belongs to the UI thread. Work from another thread, such as a network handler, opens its frame inside the task it hands to the UI thread, where the change is applied. Close frames in a `try`-with-resources block, so they close however the block ends; closing a frame also closes any frame opened inside it that is still open. Objects you `push` go on the chain, and you can put values of your own under keys made with `CauseKey.of`.

On an engine with a cause stack of its own, the adapter can make Strata's stack the engine's: frames you open are then the engine's frames, objects you push are on the cause of the engine's own events fired inside them, and a Strata event's chain is the engine's current cause.

## Event interfaces

Each event type names the interface its handlers receive. They all extend `Event`, and every Pre event is also `Cancelable`:

| Interface | Kind | Adds | Used by |
|---|---|---|---|
| `Event` | Both | `target()`, `current()`, `origin()`, `cause()`, `stopPropagation()`, `isPropagationStopped()`, and the [snapshots](snapshots.md) | Every event |
| `Cancelable` | Pre | `cancel()`, `isCancelled()`, `consume()`, `isHandled()` | Input and requests |
| `PointerEvent` and its subtypes, `KeyEvent`, `CharEvent`, `FocusEvent`, `ModifiersEvent` | Pre | What the user did; see [Clicks](#clicks) and below | `InputEvents`, `Focusable.FOCUS_GAINED`, `Focusable.FOCUS_LOST` |
| `RequestEvent<C>` | Pre | `control()` | Requests with no value, such as `Button.PRESS` and `Closable.CLOSE` |
| `ValueRequestEvent<C, V>` | Pre | `control()`, `value()`, `setValue(value)` | Requests with a value, such as `TextInput.EDIT` and `Toggle.TOGGLE` |
| `ControlEvent<C>` | Post | `control()` | Reports with no value, such as `Button.PRESSED` and `Closable.CLOSED` |
| `ValueEvent<C, V>` | Post | `control()`, `value()`, `previous()` | Reports with a value, such as `TextInput.EDITED` and `Toggle.TOGGLED` |
| `PaintEvent` | Post | `painter()`, `bounds()` | `Component.PAINTED` |
| `PaintEvent.Pre` | Pre | The same, and it can be canceled | `Component.PAINT` |
| `MeasureEvent` | Post | `context()`, `availableWidth()`, `availableHeight()`, `width()`, `height()`, `setWidth`, `setHeight` | `Component.MEASURED` |

In a control's events, `C` is the type that declares the event. For a control's own event it is the control, as in `RequestEvent<Button>`. For an attribute's event it is the attribute, as in `ValueEvent<Toggle<?>, Boolean>`, because the same type serves check boxes, radio buttons and switches. `control()` returns the control that fired the event, typed as `C`. To reach the concrete control, test `target()` with `instanceof`.

```java
--8<-- "events/EventExamples.java:kinds"
```

`value()` of a report is the value after the change, and `previous()` the value before it. A report of an action rather than a change, such as `TextBox.SUBMITTED` or `Menu.CHOSEN`, has `previous()` equal to `value()`.

## What runs, in what order

For an input event, at every component the event reaches:

1. The component's handlers, in priority order.
2. The component's default behavior, such as a button pressing or a text box inserting a character, unless the event has been canceled.

A bubbling event then moves to the parent and repeats, unless something stopped it. A direct event stays at its target.

A control's request is different. The control fires it through its whole path first, so any handler on the way can refuse it, and acts afterwards with the final value, unless the request was canceled. If it acts, it then fires the matching report. Requests and reports have no default behavior at the components they pass; only the control that fired them acts.

### How far events travel

An input event climbs from its target to the top of the tree it is in: the screen's content, or the content of the overlay it happened in. An overlay's content has no parent, so input inside a popup never reaches the screen below it.

A control's requests and reports climb the same way, except that they stop at the first composite above the control. A composite is a control built from other controls, such as a table, whose inner list and rows are its private parts. The composite's handlers still run, and the event goes no further, so a handler on your form for `Selection.SELECTED` hears the table and never the list inside it.

Content that a composite holds for you does not count as a private part. Events from a component in a table cell, an expander's content, a split pane's panels or a navigation view's page pass the composite and every composite above it, so a handler around the table hears a button in one of its cells. A `CustomComposite` you write behaves the same way: events from parts added with `addChild` stop at it, and events from content added with `addContent` pass through. See [Extending Strata](extending.md#events-for-your-own-control).

### Handling a whole form

Control events bubble, so one handler on a panel hears the controls inside it:

```java
--8<-- "events/EventExamples.java:form"
```

Where a handler only copies a value between a control and your data, a [binding](binding.md) says the same in one call. Each attribute declares keys beside its events, such as `Toggle.SELECTED` beside `Toggle.TOGGLED`, and a two-way binding still goes through the control's request, so the handlers on this page keep working:

```java
music.bind(Toggle.SELECTED, model.music);       // instead of a TOGGLED handler and a setSelected call
```

## Canceling and stopping

| Call | On | Effect |
|---|---|---|
| `cancel()` | Pre events | For input, skips the default behavior of the current component and of every ancestor the event still reaches. For a request, the control does not make the change. For `PAINT`, the component skips its own drawing. Handlers keep running, and the event keeps bubbling. |
| `stopPropagation()` | All events | The event goes no further up. The current component's remaining handlers and its default behavior still run. |
| `consume()` | Pre events | Both: "handled here". |

`isCancelled()`, `isPropagationStopped()` and `isHandled()` (either one) report the state. Pre events implement `Cancelable`; Post events have no `cancel()`, because what they report already happened.

When an input event finishes, the host reports it to the adapter as consumed if it was canceled or stopped, and the adapter keeps that input from the engine. A handler that acts on input should call `consume()`, so ancestors and the engine do not act on it too.

!!! warning "Built-in controls consume the input they act on"
    A button consumes the clicks and keys that press it, and a text box consumes the keys and characters it edits with. Consuming stops propagation, so that input never reaches the control's ancestors, and a handler on a panel does not see clicks on the buttons inside it. To hear about the buttons, handle `Button.PRESSED` on the panel; control events still bubble.

## Target and current

`target()` is the component the event was fired at. It is fixed for the whole delivery. `current()` is the component whose handlers or default behavior are running now. It starts equal to the target and moves to each ancestor as the event bubbles. A control's events also have `control()`, the control that fired them. It is the target, except for the two event targets that are not components: a radio group's `SELECTED` has the newly selected button as its target, or the one that lost the selection when it was cleared, and an overlay's `CLOSED` has the overlay's content.

`PointerEvent.localX()` and `localY()` are relative to the current component, so they change as the event bubbles. `x()` and `y()` are in host coordinates and do not change.

### Handling input on a container

One handler on a container can serve all of its children, because input from children bubbles up to it. This works for children that do not consume the event, such as labels:

```java
--8<-- "events/EventExamples.java:delegate"
```

The `instanceof Label` test skips clicks on the panel's own padding and spacing, where the panel itself is the target.

## Event objects are short-lived

An event object is valid only while it is being delivered. Do not keep it to read later: Strata reuses paint and measure events every frame, and `current()` changes as an event bubbles. To keep the state a component had when the event fired, take a [snapshot](snapshots.md), which stays valid.

## Removing handlers

`onEvent` handlers stay as long as their component. For one that must go earlier, such as a handler that follows a drag, register it with `subscribe`, which returns a `Subscription`:

```java
--8<-- "events/EventExamples.java:remove"
```

A `Subscription` is `AutoCloseable`, so it also works in try-with-resources. `close()` and `unsubscribe()` do the same thing, and calling either twice does nothing.

Removing a component from its tree does not remove its handlers. That rarely matters, because a closed screen's tree is dropped along with them. It matters if you keep a component and reuse it.

## Exceptions

A handler that throws is not caught by Strata. The handlers after it for the same event do not run, and the exception propagates out of the input call that caused the event, into the adapter and the engine. Catch your own errors in handlers.

## Clicks

A press, a release and a click are three events:

1. `POINTER_PRESS` fires at the deepest component under the pointer. Just before it fires, focus moves to that component or its nearest focusable ancestor, or is cleared if there is none.
2. `POINTER_RELEASE` fires at the component the press started on, wherever the pointer is now.
3. `CLICK` fires after the release, at the same component, if the pointer is still over it, the component is enabled, and no drag happened in between.

Each mouse button is tracked separately, so a right-button press during a left-button drag is its own press.

Open overlays come first. A press outside a popup closes the popup and goes no further: focus stays where it was, and no `POINTER_PRESS` fires. A flyout that turned off closing on a press outside lets the press through. While a dialog is open, nothing below it can be hovered or pressed. See [Overlays](overlays.md#popups).

```java
--8<-- "events/EventExamples.java:buttons"
```

### Click counting

`clicks()` says where a press falls in a run of quick presses: 1 for a single click, 2 for a double, 3 for a triple, and then it starts again at 1. A press continues the run when it hits the same component with the same button within 500 ms and within 4 units on each axis of the previous press. A release and its click carry the count of their press.

Strata delivers every press immediately with its count. It never holds a single click back to wait and see whether a double follows, so a double click produces a click with `clicks() == 1` and then one with `clicks() == 2`. React to the count you care about.

## Drags

A press becomes a drag once the pointer moves more than 4 units from where the button went down, measured on either axis.

| Event | When | Positions |
|---|---|---|
| `DRAG_START` | The pointer first passes the threshold | Those of the original press. Deltas are zero. |
| `DRAG` | Every move after that, wherever the pointer is | The current position, with the movement since the previous drag event in `deltaX()` and `deltaY()` |
| `DRAG_END` | The button comes up | The release position. It replaces the `CLICK` that release would otherwise cause. |

Drag events go to the component the press started on, even when the pointer leaves it. `startX()` and `startY()` give the press position, and `clicks()` the press's click count, so a drag after a double click can select by word as text boxes do.

![A label that can be dragged](../assets/images/events-drag.png)

```java
--8<-- "events/EventExamples.java:drag"
```

If the pointer leaves the host during a drag, the press stays tracked, and the drag continues when the pointer comes back.

## Hover

Only the deepest component under the pointer is hovered. Hit testing respects clipping, visibility and enabled state: the pointer cannot hit a hidden or disabled component, or the part of a child cut off by a clipping parent.

| Event | Bubbles | When |
|---|---|---|
| `POINTER_ENTER` | no | The component became the deepest one under the pointer |
| `POINTER_EXIT` | no | It stopped being the deepest one, or the pointer left the host |
| `POINTER_MOVE` | yes | The pointer moved over it. Fired after any enter or exit for the same movement. |

Enter and exit do not bubble, so moving from a panel onto a button inside it gives the panel an exit and the button an enter. When layout moves a component under a still pointer, the enter and exit fire on the next pointer input, not at the moment of the layout. A scroll that something consumed is the exception: the host lays out at once and works out hover again, since the content moved under the pointer.

## Scrolling

`SCROLL` fires at the component under the pointer and bubbles, so an outer container receives scrolls its children do not consume.

| Value | Meaning |
|---|---|
| `scrollY()` | Positive when the wheel turns away from the user, which scrolls toward the top of the content |
| `scrollX()` | Positive toward the left, the start of a row |
| `kind()` | A `ScrollKind`: `WHEEL` for a notched wheel (1 per notch), `PRECISE` for trackpads and smooth wheels, which report fractions |

Amounts are in wheel notches. A component that scrolls by whole steps, such as a list moving by rows, can use `kind()` to tell a wheel's whole notches from a trackpad's fractions. Engines differ in how horizontal scrolling arrives; see [Platform differences](../platforms/differences.md#scrolling).

## Keys and characters

`KEY` carries a physical key (`key()`), an action (`PRESS`, `REPEAT` or `RELEASE`) and the held modifiers. It fires at the focused component and bubbles. When nothing has focus, it fires at the content of the top dialog, or at the screen's content when no dialog is open. If nothing consumes it, the host acts on two keys: an Escape press closes the top overlay that is not a toast, and a Tab press or repeat without Control or Alt moves focus to the next tabbable component, or the previous one with Shift.

`CHAR` carries a typed character as a code point, after the keyboard layout and any input method. It fires only at the focused component and is dropped when nothing has focus. It bubbles.

A key handler on the screen's content sees every key that nothing focused consumed, which makes it the place for screen-wide shortcuts:

```java
--8<-- "events/EventExamples.java:shortcut"
```

[Input and focus](input-focus.md) covers keys, modifiers, shortcut matching and key repeat in detail.

## Focus events

`Focusable.FOCUS_LOST` and `Focusable.FOCUS_GAINED` are direct, and fire only at components that are `Focusable`. When focus moves from one component to another, the old one gets `FOCUS_LOST` first, then the new one gets `FOCUS_GAINED`, and both fire after the focus has already changed. `other()` is the component on the other side of the move, or empty when focus came from or went to nothing. Canceling a focus event cannot keep focus where it was; it only skips the component's default behavior, such as a text box moving its caret to the end.

## Drawing and sizing

Drawing and sizing are events too, so you can change how any component looks without writing a new one.

| Type | Interface | When |
|---|---|---|
| `Component.PAINT` | `PaintEvent.Pre` | Before the component draws its background. Canceling it skips the component's background, border and content; its children still draw. |
| `Component.PAINTED` | `PaintEvent` | After the component and its children have drawn, so what a handler draws lands on top |
| `Component.MEASURED` | `MeasureEvent` | After the component measured its content. `setWidth` and `setHeight` change the size it asks for. |

A paint event gives the `painter()` to draw with, valid only for this frame, and the component's `bounds()` in host coordinates:

```java
--8<-- "events/EventExamples.java:paint"
```

A measure event gives the space offered for the content in `availableWidth()` and `availableHeight()`, after the margin and capped by any fixed or maximum size, and possibly infinite. `width()` and `height()` start as the size the component asked for. A fixed size set on the component, and its minimum and maximum, still apply to what a handler sets, and negative values count as 0. `context()` measures text and children for the pass in progress. Do not change layout from a measure handler.

```java
--8<-- "events/EventExamples.java:measure"
```

None of the three bubble, and none take snapshots. They reach only the component's own handlers. A component fires them only when it has a handler for them, and reuses one event object every frame, so a component you do not decorate pays nothing and one you do allocates nothing for the event. Keep paint handlers free of allocation where you can, as in a custom component's `paint`.

## Your own events

A control you write declares its events the way the built-ins do, as constants made with `EventType.request`, `EventType.valueRequest`, `EventType.report` and `EventType.valueReport`, and fires them with `CustomComponent`'s `request` and `report` methods. An event type compares by identity: each call to a factory makes a type equal only to itself, even when another has the same name and event class, so two libraries that both declare `EventType.valueReport("changed")` get two types and a handler for one never hears the other. The name is only for debugging. Keep each type in a constant, and register handlers and fire events with that constant. [Extending Strata](extending.md#events-for-your-own-control) shows a complete example.

## Input event types

| Type | Event interface | Bubbles | Fired at |
|---|---|---|---|
| `POINTER_ENTER` | `PointerEvent` | no | The newly hovered component |
| `POINTER_EXIT` | `PointerEvent` | no | The previously hovered component |
| `POINTER_MOVE` | `PointerEvent` | yes | The hovered component |
| `POINTER_PRESS` | `PointerButtonEvent` | yes | The component under the pointer |
| `POINTER_RELEASE` | `PointerButtonEvent` | yes | The component the press started on |
| `CLICK` | `PointerButtonEvent` | yes | The same, if still under the pointer and enabled, with no drag |
| `DRAG_START`, `DRAG`, `DRAG_END` | `DragEvent` | yes | The component the press started on |
| `SCROLL` | `ScrollEvent` | yes | The component under the pointer |
| `KEY` | `KeyEvent` | yes | The focused component, or else the top dialog's content, or else the screen's content |
| `CHAR` | `CharEvent` | yes | The focused component only |
| `FOCUS_GAINED`, `FOCUS_LOST` | `FocusEvent` | no | The component gaining or losing focus |
| `MODIFIERS_CHANGED` | `ModifiersEvent` | no | The screen's content and each open overlay's content |

The [event type reference](../reference/event-types.md) adds each type's default behavior in the built-in controls, and lists every control event type with its value. Each control's events are also described with the control in [Controls](controls.md).
