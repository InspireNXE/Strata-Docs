# Data binding

A screen usually shows data that lives somewhere else: settings, an account's balance, a list of offers. You can keep the two in step by hand, with a setter call wherever the data changes and a handler on every control that changes it. Binding does the same with one call per control. The data lives in states, and each control says which state, or which value computed from states, one of its properties follows.

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
| `Component.OPACITY` | `Float`, from 0 to 1 | |
| `HasTooltip.TOOLTIP` | `Tooltip`, null for none | |
| `HasText.TEXT` | `Text` | |
| `TextEntry.READ_ONLY` | `Boolean` | |
| `TextInput.TEXT` | `String` | `TextInput.EDITED` |
| `Titled.TITLE` | `Text` | |
| `Validatable.VALIDATION` | `Validation` | `Validatable.VALIDATED` |
| `Toggle.SELECTED` | `Boolean` | `Toggle.TOGGLED` |
| `Ranged.VALUE` | `Double` | `Ranged.CHANGED` |
| `HasDate.DATE` | `LocalDate`, null for none | `HasDate.DATE_PICKED` |
| `HasTime.TIME` | `LocalTime`, null for none | `HasTime.TIME_PICKED` |
| `Selection.SELECTED_INDEX` | `Integer` | `Selection.SELECTED` |
| `Selection.selectedItem()` | the entry type, null for none | `Selection.SELECTED` |
| `MultiSelection.SELECTED_INDEXES` | `List<Integer>` | `MultiSelection.SELECTED_MANY` |
| `MultiSelection.selectedItemsKey()` | `List` of the entry type | `MultiSelection.SELECTED_MANY` |
| `ItemSource.itemsKey()` | `List` of the item type | |

The keys written as methods are methods because a constant cannot carry a control's type parameter. `itemsKey()` and `selectedItemsKey()` are not called `items()` and `selectedItems()` because those names are the getters'. Only components bind, so entries such as tabs, menu items and table columns have `HasText` or `Titled` but take no binding.

A control has keys of its own for what no attribute covers, beside its own events:

| Key | Value | Two way through |
|---|---|---|
| `CheckBox.STATE` | `CheckState`, for a box that can be mixed | `CheckBox.CHANGED` |
| `ColorPicker.COLOR` | `Integer`, ARGB | `ColorPicker.PICKED` |
| `CalendarView.SHOWN_MONTH` | `YearMonth` | `CalendarView.BROWSED` |
| `DateTimePicker.DATE_TIME` | `LocalDateTime`, null for none | `DateTimePicker.PICKED` |
| `PasswordTextBox.IS_REVEALED` | `Boolean` | `PasswordTextBox.REVEALED` |
| `TableView.SORT_ORDER` | `TableView.Sort`, null for the set order | `TableView.SORTED` |
| `Expander.IS_EXPANDED` | `Boolean` | `Expander.EXPANDED` |
| `NavigationView.IS_EXPANDED` | `Boolean`, the pane open over the content | `NavigationView.EXPANDED` |
| `SplitPane.POSITION` | `Float`, from 0 to 1 | `SplitPane.MOVED` |
| `InfoBar.IS_OPEN` | `Boolean` | `Openable.OPENED` and `Closable.CLOSED` |
| `InfoBar.SEVERITY` | `InfoBar.Severity` | |
| `ProgressBar.INDETERMINATE` | `Boolean` | |
| `Image.VALUE`, `Avatar.VALUE` | any value, null for none | |
| `Image.SPRITE` | `SpriteId` | |
| `Avatar.NAME` | `Text` | |
| `Avatar.BADGE` | `Integer` | |

A key has the property's name, as `SELECTED` does. Where the report already has that name, as `Expander.EXPANDED` does, the key has the getter's name instead: `IS_EXPANDED`, `IS_REVEALED`, `IS_OPEN`.

An info bar bound with `IS_OPEN` writes false when the user closes it, so a state can show a message until it is read:

```java
--8<-- "binding/BindingExamples.java:info"
```

Some properties have no key on purpose. Margins, sizes, label styles and the other layout and appearance setters have none, since a screen's data seldom drives them. A range would clamp the value between setting its two ends. Combo boxes, drop-down buttons and the pickers have no open key, since their popups close on a press outside and a state holding them open would fight the user. A window's size and position report only the user's drags, and a password box's text never goes into a state.

## One way and two ways

Whether a binding is two way depends on its source and key:

- A `State` bound with a key that has a report is two way. The control follows the state, and the user's changes go back into the state.
- Anything else is one way: the control follows the source and never writes. That covers keys with no report, such as `Component.ENABLED`, and computed sources.

In a two-way binding, a change the user makes still asks the control's request first, so a handler on `TextInput.EDIT` or `Ranged.CHANGE` can refuse it or rewrite it as before. The state gets the value the control accepted, when the control reports it. Changes made from code with a setter report too, so they also reach the state. The binding handles the report on the control after the control's own handlers, so those still see the state's old value, while handlers on its ancestors and code that runs once the report is over see the new one. Other controls bound to the same state catch up at the next frame.

A change to the state sets the control the way its setter does: the control reports the change and fires no request. The report's [origin](events.md#who-caused-it) is `Origin.Binding`, with the state as its source. The binding does not write back a report it caused itself, and never writes a value equal to the one already there, so a binding cannot loop. If the control adjusts a value as the binding sets it, such as a slider clamping 1.5 to its maximum of 1, the control keeps the adjusted value and the state keeps its own.

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

A list, table or tree in `SelectionMode.MULTIPLE` binds its whole selection with `MultiSelection.selectedItemsKey()`, a `List` of the selected items. A change to the state selects those items through `selectItems`, which finds each one by key and skips items the control does not hold; a tree opens the branches the items are in. Null clears the selection, and so does an empty list. `MultiSelection.SELECTED_INDEXES` binds the indexes instead, with the same warning as `SELECTED_INDEX`.

```java
--8<-- "binding/BindingExamples.java:multi"
```

When a component is attached, the state wins over what the control held. A combo box selects its first item when its items arrive, but the bound selection replaces it before anything is written back.

## When updates happen

A state change does not reach its controls during `set`. It marks the bindings that read the state, and the host brings them up to date at the start of the next frame, before layout. Ten changes to a state in one frame cost one update of each control bound to it.

`set` may therefore be called from any thread, such as a network handler:

```java
--8<-- "binding/BindingExamples.java:threads"
```

Setting a state is the one change to a screen's data that needs no hand-over to the UI thread. Changing a component directly from another thread throws; [The UI thread](threading.md) shows how to hand the work over.

A binding that sets a control can change more states, through that control's handlers, and those bindings run in the same step. If bindings are still changing after 100 rounds, the host takes it for a cycle, such as two handlers that keep setting each other's states. It logs an error naming the bindings, through `System.Logger`, and drops their updates until one of their states changes again.

Input calls such as `pointerButton` and `key` do not flush, so a state set in a handler reaches the other controls bound to it at the next frame.

In tests, call `host.flush()` to bring bindings up to date without laying out or drawing. `layout()` and `render()` flush first. If a source or a handler throws during a flush, the exception propagates out of the call, and the bindings that round had not reached yet stay queued for the next flush.

## Lifetime

A binding follows its sources only while its component is attached. When the component joins a shown tree, the binding reads its source and sets the property at once, so a screen opens with its bound values. Binding a component that is already attached sets the property during the `bind` call. When the component leaves the tree, the binding lets go of every state it read. Nothing needs to be unsubscribed, and a state that outlives a screen holds nothing of it.

A component holds one binding per key name. Binding the same key again replaces the earlier binding, which lets go of its states at once.

## Unbinding

`unbind(key)` takes a binding off and returns the component. The binding lets go of every state it read at once, and the property keeps the value it has. Calling it for a key that is not bound does nothing. It matches by the key's name, as `bind` does when it replaces a binding, so a converted key removes a binding made with the plain key, and the other way round.

```java
--8<-- "binding/ConverterExamples.java:unbind"
```

Like `bind`, it must run on the UI thread when the component is attached.

## Watching a state

Sometimes code has to act on a change instead of showing it: save a setting, send a volume to a server. There are two ways, and they differ in thread and lifetime.

```java
--8<-- "binding/ConverterExamples.java:watch"
```

| | `ComponentFactory.watch(source, listener)` | `State.subscribe(listener)` |
|---|---|---|
| Source | A state or a computed `Source`, tracked as a binding is | One state |
| Called | On the UI thread, before the next frame is laid out, with the bindings | At once, on the thread that called `set` |
| Several changes in one frame | One call, with the last value, if it differs from the last one called | One call each |
| The first value | Read once when the watch is made, and not delivered | Not delivered |
| Stops | When the screen closes or is built again, or when you close the subscription | Only when you close the subscription |

Use `watch` in screen code: it needs no cleanup, a listener may change components, and a computed source that comes out the same calls nothing. Use `subscribe` in a model or service that outlives screens, such as one that writes settings to disk. The state holds a subscribed listener until it is closed, so a listener that points at a screen keeps the screen alive. A listener of `subscribe` runs on whatever thread set the state, so it must not touch components; hand that work to the UI thread. Setting an equal value calls neither.

`watch` throws `IllegalStateException` for a factory with no host. A watch made while no screen is open stops when the next screen closes, and one whose source throws on its first read is dropped.

## Converters

A text box holds a `String` and a quantity is an `int`. A key made with `convert` joins the two, so a field's text can follow a `State<Integer>`:

```java
--8<-- "binding/ConverterExamples.java:quantity"
```

`TextInput.TEXT.convert(Converters.integer(1, 64))` is a new key with the same name and reports as `TextInput.TEXT`, so it replaces and removes bindings as that key does. The binding then does the following:

- It writes a state's value to the control as text, with `toView`, unless the text already converts to that value. A field bound to 5 that holds "05" is left as typed.
- When the user changes the text, it converts it with `toModel` and sets the state only if the text is valid. The state keeps its last valid value while the text is wrong.
- A control that is `Validatable`, which Strata's text fields are, shows the failed conversion at once: its validation becomes an error with the converter's message, which draws the invalid look and shows in the tooltip. The error clears as soon as the text converts, when the state changes, or when the key is unbound or bound again. It wins over a validator and over `setValidation` while it lasts.
- A converted key can be converted again, to stack one converter on another.

`Converters` makes the converters for numbers and one for anything else:

| Converter | Model | Accepts | Writes |
|---|---|---|---|
| `Converters.integer()` | `Integer` | Whole numbers that fit an `int` | The digits, with no grouping |
| `Converters.integer(min, max)` | `Integer` | Whole numbers in the range | The same |
| `Converters.decimal(decimals)` | `Double` | Any finite number | `decimals` digits after the separator, from 0 to 15 |
| `Converters.decimal(min, max, decimals)` | `Double` | Numbers in the range, checked after rounding | The same |
| `Converters.of(toView, toModel)` | Any | Whatever `toModel` returns, never invalid | Whatever `toView` returns |

The number converters read the translator's locale each time they run, so a German screen shows 2,5 and accepts it. They write the language's digits and signs without grouping, and read a number with or without grouping and with spaces around it. An integer converter takes "2.0" as 2 and rejects "2.5". A decimal converter rounds half up to its digits, and turns -0 into 0. A null model value is empty text, and empty text is invalid. A range with its minimum above its maximum, or decimals outside 0 to 15, throws `IllegalArgumentException`. The messages are the translation keys `strata.validation.integer`, `.integer_range`, `.decimal` and `.decimal_range`, with core's English, such as "Enter a whole number from 1 to 64". See the [translation keys](../reference/translation-keys.md).

To write your own, implement `Converter<M, V>`: `toView` turns the model's value into the control's, and `toModel` turns the control's back into a `Conversion`, which is `Conversion.valid(value)` or `Conversion.invalid(message)`:

```java
--8<-- "binding/ConverterExamples.java:converter"
```

A converter is not tied to text. `Converters.of` fits a combo box whose items are objects while the model keeps an id.

## Validation

`Validatable.VALIDATION` binds a control's validation to a `State<Validation>`, two way through `Validatable.VALIDATED`. Giving each field of a form a state of its own brings their results together where a computed source can read them. The model of the quantity form has one:

```java
--8<-- "binding/ConverterExamples.java:model"
```

The quantity form above binds `Validatable.VALIDATION` to `order.quantityCheck` beside the converted text. Its hint label shows the validation's message, which is the converter's message while the text is wrong and empty text when valid, and Save is enabled while the validation is valid. A second field's state can join in:

```java
--8<-- "binding/ConverterExamples.java:all-valid"
```

A `Validation` is a severity and a message: `Validation.VALID`, `Validation.warning(text)` or `Validation.error(text)`. Only an error makes `isValid()` false. [Controls](controls.md#validation) covers what a control does with its validation, including validators, the look and the tooltip.

## Animated properties

A binding on a property that an [animation](animation.md) moves does not write the state on every step. The binding waits and writes the value where the property comes to rest, when the animation ends or when it is closed. The state hears one change, and other controls bound to it do not follow the intermediate values. `Component.OPACITY` is the property a fade animates.

## Binding your own control

A control you write gets binding by declaring a `PropertyKey` for each property, beside its events, with `PropertyKey.of(name, reader, writer, report)`. Leave out the report for a property only code changes, and pass two reports where two events between them tell of every change, as an info bar's opening and closing do.

```java
--8<-- "extending/ToggleSwitch.java:events"
```

The writer must behave like the control's setter, reporting the change and firing no request, and between them the reports must fire after every change of the property with the control as their target. [Extending Strata](extending.md#events-for-your-own-control) shows the rest of the control.
