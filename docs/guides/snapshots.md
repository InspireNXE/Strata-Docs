# Event snapshots

A snapshot records a component's state at the moment an event fired, before any handler or default behavior ran. A handler can compare it with the live component to see what the event changed: the text before a key press, whether a box was checked before a click, where a component was before a drag.

## Getting a snapshot

There is nothing to ask for. Whenever an event's path has a handler for its type, Strata takes the snapshots before the first handler runs, and any handler on the path can read them:

```java
--8<-- "snapshots/SnapshotExamples.java:compare"
```

Inside the handler:

| Method | Returns |
|---|---|
| `event.snapshot()` | The target's snapshot |
| `event.currentSnapshot()` | The snapshot of the component whose handler is running |
| `event.snapshotOf(component)` | Any component's snapshot, as an `Optional` |

These methods belong to `SnapshotEvent`, the event interface that input events and a control's requests extend: `PointerEvent` and its subtypes (drags and transfers included), `KeyEvent`, `CharEvent`, `FocusEvent`, `ModifiersEvent` and `RequestEvent`. Other events have no snapshot methods at all, so a handler for one cannot ask. A Post event, such as `TextInput.EDITED`, fires after its change, so it carries the value from before the change as `previous()` instead:

```java
--8<-- "snapshots/SnapshotExamples.java:previous"
```

## What is captured, and when

Before an event is delivered, Strata walks its path: the target and, if the type bubbles, the ancestors it will reach. For a control's request, the path ends at the first composite above the control, as the request itself does (see [How far events travel](events.md#how-far-events-travel)). If no component on that path has a handler for this event type, nothing is captured and nothing is allocated. Otherwise Strata snapshots:

- the target, always; and
- every component on the path that has a handler for this type.

Nothing else is captured. All snapshots are taken before the first handler runs, so even a handler at the top of the tree sees the target as it was before the target's own handlers and default behavior changed it. Paint and measure events never take snapshots, `PAINT` included, although it is a Pre event: `PaintEvent.Pre` is `Cancelable` but not a `SnapshotEvent`.

### What "when the event fired" means

Snapshots are taken after the host has updated its own bookkeeping for the input and before the event reaches any handler. A few events change host state first, and their snapshots show the new state:

| Event | Already updated in the snapshot |
|---|---|
| `POINTER_PRESS` | Focus has moved to the target or its nearest focusable ancestor, and the target is pressed |
| `POINTER_RELEASE`, `CLICK`, `DRAG_END` | The target is no longer pressed, unless another button still holds it |
| `POINTER_ENTER`, `POINTER_EXIT` | The target is hovered, or no longer hovered |
| `FOCUS_GAINED`, `FOCUS_LOST`, `FOCUS_ENTER`, `FOCUS_EXIT` | Focus has already changed |

Everything a handler or a default behavior changes, such as text, caret, checked state or enabled state, is still in its old state.

### Shallow

Snapshots are shallow. A snapshot of a panel holds the panel's own state and nothing about its children. A child that was captured has its own snapshot.

```java
--8<-- "snapshots/SnapshotExamples.java:ancestor"
```

## What a snapshot holds

`ComponentSnapshot` is a record. It holds what the `Component` getters returned when it was taken:

| Group | Fields |
|---|---|
| Identity | `component()`, the live component |
| Flags | `visible`, `enabled`, `hovered`, `pressed`, `focused` |
| Layout inputs | `margin`, `minWidth`, `maxWidth`, `minHeight`, `maxHeight`, `horizontalAlignment`, `verticalAlignment` |
| Layout results | `bounds`, `desiredWidth`, `desiredHeight` |
| Kind-specific | `state()`, or `state(Class)` for a typed `Optional` |

```java
--8<-- "snapshots/SnapshotExamples.java:common"
```

The `state` is what the component's kind adds. The built-ins record a nested `State` record:

| Component | State record | Fields |
|---|---|---|
| `TextBox` | `TextBox.State` | `text`, `caret`, `anchor`, `maxLength`, plus `selectionStart()`, `selectionEnd()` and `hasSelection()` |
| `PasswordTextBox` | `PasswordTextBox.State` | `length`, `caret`, `anchor`, `maxLength`, `revealed`, plus `selectionStart()`, `selectionEnd()` and `hasSelection()`. Never the text. |
| `Button` | `Button.State` | `text` |
| `TextArea` | `TextArea.State` | `text`, `caret`, `anchor`, `maxLength` |
| `DropDownButton` | `DropDownButton.State` | `text`, `open` |
| `CheckBox` | `CheckBox.State` | `state` (a `CheckState`), `text` |
| `RadioButton`, `ToggleSwitch` | `RadioButton.State`, `ToggleSwitch.State` | `selected`, `text` |
| `Slider` | `Slider.State` | `value`, `min`, `max`, `step` |
| `Spinner` | `Spinner.State` | `value`, `min`, `max`, `step`, `decimals` |
| `ScrollBar` | `ScrollBar.State` | `value`, `min`, `max`, `visibleAmount` |
| `ColorPicker` | `ColorPicker.State` | `color`, `hue`, `saturation`, `brightness`, `alphaShown` |
| `CalendarView` | `CalendarView.State` | `date`, `shownMonth`, `zoom` |
| `DatePicker`, `TimePicker`, `DateTimePicker` | `DatePicker.State`, `TimePicker.State`, `DateTimePicker.State` | `date`, `time` or `dateTime`, and `open` |
| `Image` | `Image.State` | `sprite`, `visual`, `tint`, `imageWidth`, `imageHeight`, `scaling` |
| `Avatar` | `Avatar.State` | `value`, `name`, `initials`, `group`, `badge` |
| `InfoBar` | `InfoBar.State` | `severity`, `title`, `text`, `open` |
| `Label` | `Label.State` | `text`, `style`, `wraps`, `textAlignment` |
| `Flyout` | `Flyout.State` | `open` |
| `Expander` | `Expander.State` | `expanded`, `title` |
| `NavigationView` | `NavigationView.State` | `selectedIndex`, `shownMode`, `expanded` |
| `StackPanel` | `StackPanel.State` | `orientation`, `spacing`, `spread` |
| `GridPanel` | `GridPanel.State` | `columns`, `rows`, `columnSpacing`, `rowSpacing`. Not the children or their cells. |
| `ScrollPanel` | `ScrollPanel.State` | `scrollX`, `scrollY`, `maxScrollX`, `maxScrollY` |
| `AnchorPanel`, `AbsolutePanel` and the controls not listed | none | `state()` is null |

`state(Class)` returns empty when there is no state or it is of another type, so it is safe to call on any snapshot.

## Snapshots outlive the event

Unlike the event, a snapshot stays valid after delivery and never changes. Keep it for undo, logging or animation:

```java
--8<-- "snapshots/SnapshotExamples.java:keep"
```

A snapshot holds a reference to its live component, so keeping one keeps the component reachable.

## Your own components

A `CustomComponent` adds its state by overriding `snapshotState()`:

```java
@Override
protected Object snapshotState() {
    return new State(this.checked, this.text);
}
```

Strata calls it only when the component is captured, before any handler runs. Return an immutable value, such as a record, and copy any mutable data: a live list would change along with the component. Record only the component's own state; a container must not include its children, which are captured on their own. Do not change any component from it. The default returns null. The [switch example](extending.md#write-a-new-component) shows a complete component with a snapshot state.

## When there is no snapshot

| Situation | `snapshot()` | `currentSnapshot()` | `snapshotOf(c)` |
|---|---|---|---|
| A report, or a paint or measure event | does not exist: these events are not `SnapshotEvent`s | | |
| Any `SnapshotEvent` read inside a handler | works, since the target is always captured | works, since a handler is running on the current component | present for the target and every component on the path with a handler for the type |
| A `SnapshotEvent` read after delivery, or for a component that joined the path | throws `IllegalStateException` for the target or current component when none was taken | throws the same | empty |

A component that joins the path while the event is being delivered, for example because a handler moved the target into another container, has no snapshot.

## Cost

An event whose path has no handler for its type costs nothing extra. An event that does have one allocates one snapshot per captured component, plus whatever `snapshotState()` builds. Prefer narrow event types: a handler for `POINTER_MOVE` on a root panel snapshots the hovered component on every mouse move.

## Why snapshot whenever something listens?

Letting each handler ask for snapshots would need a second registration method, and a handler that forgot to ask would get an exception at the worst moment. A snapshot is a small record taken only on the paths that have a listener, never while painting, so taking it for every listener keeps the API to one method at a cost that only input with handlers pays. This is recorded in the [design](../design-implemented.md#8-decisions).
