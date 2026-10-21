# Data binding

A screen usually shows data that lives somewhere else: settings, a player's balance, a list of offers. You can keep the two in step by hand, with a setter call wherever the data changes and a handler on every control that changes it. Binding does the same with one call per control. The data lives in states, and each control says which state, or which value computed from states, one of its properties follows.

Binding is optional. Setters and [events](events.md) work as they always have, and a screen can mix both.

## States

A `State<T>` (`org.inspirenxe.strata.api.state`) holds one value. Put states in a plain class, with no base class:

```java
--8<-- "binding/BindingExamples.java:model"
```

`get()` returns the value and `set(value)` replaces it. Setting a value equal to the current one, as `equals` says, does nothing. A state may hold null.

`State.of` asks the `StateFactory` that `Strata.states()` returns, which Strata's core provides, so a model class can make its states before any screen exists. It throws `IllegalStateException` if core is not on the class path. `State` and `StateFactory` are Strata's to implement, never yours.

## Binding a control

`bind(key, source)` on any component keeps one of its properties in step with a source, a `State` or a computed `Source`. It has the same shape as `onEvent` and returns the component, so it chains with the setters:

```java
--8<-- "binding/BindingExamples.java:bind"
```

The key names the property. Keys are constants beside the events of the attribute that has the property, so `Toggle.SELECTED` sits next to `Toggle.TOGGLED`, and they work on every control with that attribute:

| Key | Value | Two way through |
|---|---|---|
| `Component.ENABLED`, `Component.VISIBLE` | `Boolean` | |
| `HasText.TEXT` | `Text` | |
| `TextInput.TEXT` | `String` | `TextInput.EDITED` |
| `Titled.TITLE` | `Text` | |
| `Toggle.SELECTED` | `Boolean` | `Toggle.TOGGLED` |
| `Ranged.VALUE` | `Double` | `Ranged.CHANGED` |
| `Selection.SELECTED_INDEX` | `Integer` | `Selection.SELECTED` |
| `Selection.selectedItem()` | the entry type | `Selection.SELECTED` |
| `ItemSource.itemsKey()` | `List` of the item type | |

The last two are methods because a constant cannot carry a control's type parameter. `itemsKey()` is not called `items()` because that name is the getter's. Only components bind, so entries such as tabs, menu items and table columns have `HasText` or `Titled` but take no binding.

## One way and two ways

Whether a binding is two way depends on its source and key:

- A `State` bound with a key that has a report is two way. The control follows the state, and the user's changes go back into the state.
- Anything else is one way: the control follows the source and never writes. That covers keys with no report, such as `Component.ENABLED`, and computed sources.

In a two-way binding, a change the user makes still asks the control's request first, so a handler on `TextInput.EDIT` or `Ranged.CHANGE` can refuse it or rewrite it as before. The state gets the value the control accepted, when the control reports it. Changes made from code with a setter report too, so they also reach the state. The binding handles the report on the control after the control's own handlers, so those still see the state's old value, while handlers on its ancestors and code that runs once the report is over see the new one. Other controls bound to the same state catch up at the next frame.

A change to the state sets the control the way its setter does: the control reports the change and fires no request. The binding does not write back a report it caused itself, and never writes a value equal to the one already there, so a binding cannot loop. If the control adjusts a value as the binding sets it, such as a slider clamping 1.5 to its maximum of 1, the control keeps the adjusted value and the state keeps its own.

## Computed sources

A `Source<T>` is anything a property can follow: a state, or a lambda that computes a value. `Source` is a functional interface with one method, `get()`, so a lambda or a method reference such as `model::canSave` is a source. A computed source needs no list of what it depends on. While a binding runs it, every state whose `get()` it calls is recorded, and a change to any of them runs it again. A source that reads different states on different runs is followed by whatever it read last time.

```java
--8<-- "binding/BindingExamples.java:derived"
```

`map` and `combine` are there for code that wants its sources named. A computed source should only read states and compute; it must not set states or change components.

## Lists

`ItemSource.itemsKey()` binds a list control's items to a `State<List<T>>`. A change replaces the items as a whole through `setItems`, which keeps the selection on the item with the same [key](controls.md#listview) and builds only the rows in view. Set a new list to change the items; changing the list a state holds in place is not seen.

`Selection.selectedItem()` binds the selected entry, with null for no selection. Bind the items first: a component sets its bound properties in the order they were first bound, and a selection set before the items are there finds nothing to select. `Selection.SELECTED_INDEX` binds the index instead, but an index the control does not show throws as `select` does, so prefer `selectedItem()` where the items change too.

```java
--8<-- "binding/BindingExamples.java:list"
```

When a component is attached, the state wins over what the control held. A combo box selects its first item when its items arrive, but the bound selection replaces it before anything is written back.

## When updates happen

A state change does not reach its controls during `set`. It marks the bindings that read the state, and the host brings them up to date at the start of the next frame, before layout. Ten changes to a state in one frame cost one update of each control bound to it.

`set` may therefore be called from any thread, such as a network handler:

```java
--8<-- "binding/BindingExamples.java:threads"
```

A binding that sets a control can change more states, through that control's handlers, and those bindings run in the same step. If bindings are still changing after 100 rounds, the host takes it for a cycle, such as two handlers that keep setting each other's states. It logs an error naming the bindings, through `System.Logger`, and drops their updates until one of their states changes again.

Input calls such as `pointerButton` and `key` do not flush, so a state set in a handler reaches the other controls bound to it at the next frame.

In tests, call `host.flush()` to bring bindings up to date without laying out or drawing. `layout()` and `render()` flush first. If a source or a handler throws during a flush, the exception propagates out of the call, and the bindings that round had not reached yet stay queued for the next flush.

## Lifetime

A binding follows its sources only while its component is attached. When the component joins a shown tree, the binding reads its source and sets the property at once, so a screen opens with its bound values. Binding a component that is already attached sets the property during the `bind` call. When the component leaves the tree, the binding lets go of every state it read. Nothing needs to be unsubscribed, and a state that outlives a screen holds nothing of it.

A component holds one binding per key name. Binding the same key again replaces the earlier binding, which lets go of its states at once.

## Binding your own control

A control you write gets binding by declaring a `PropertyKey` for each property, beside its events, with `PropertyKey.of(name, reader, writer, report)`. Leave out the report for a property only code changes.

```java
--8<-- "extending/ToggleSwitch.java:events"
```

The writer must behave like the control's setter, reporting the change and firing no request, and the report must fire after every change of the property with the control as its target. [Extending Strata](extending.md#events-for-your-own-control) shows the rest of the control.
