# Event types

Every event type in Strata's API, with the interface its handlers receive. Input types are constants in `InputEvents`, the focus events are on `Focusable`, drawing and sizing are on `Component`, and each control's events are on the attribute or control that declares them. See [Events](../guides/events.md) for how delivery works. All positions in pointer events are in host coordinates, in UI units.

## Pointer

| Type | Interface | Bubbles | Fired at | When |
|---|---|---|---|---|
| `POINTER_ENTER` | `PointerEvent` | no | The newly hovered component | It became the deepest component under the pointer, including when it appears under a still pointer after a layout change, on the next pointer input |
| `POINTER_EXIT` | `PointerEvent` | no | The previously hovered component | It stopped being the deepest one, or the pointer left the host |
| `POINTER_MOVE` | `PointerEvent` | yes | The hovered component | The pointer moved, after any enter or exit for the same move |
| `POINTER_PRESS` | `PointerButtonEvent` | yes | The deepest component under the pointer | A button went down. Focus has already moved. A press that closed a popup fires nothing. |
| `POINTER_RELEASE` | `PointerButtonEvent` | yes | The component the press started on | That button came up, wherever the pointer is |
| `CLICK` | `PointerButtonEvent` | yes | The same | After the release, if the pointer is still over it, it is enabled, and no drag happened |
| `DRAG_START` | `DragEvent` | yes | The component the press started on | The pointer moved more than 4 units on either axis with the button held. Positions are the press's. A handler may call `startTransfer` to make the drag carry a payload. |
| `DRAG` | `DragEvent` | yes | The same | Every move after `DRAG_START`, wherever the pointer is |
| `DRAG_END` | `DragEvent` | yes | The same | The button came up. Replaces the `CLICK`. |
| `DRAG_ENTER` | `TransferEvent` | no | The deepest component under a drag that carries a transfer, or its nearest enabled ancestor | The drag moved onto it |
| `DRAG_OVER` | `TransferEvent` | yes | The same, then up the ancestors until a handler accepts or rejects | Every move of such a drag, after any `DRAG_EXIT` and `DRAG_ENTER`. A list accepts its own dragged rows, and an editable text box accepts text. |
| `DRAG_EXIT` | `TransferEvent` | no | The component the drag was over | The drag left it, the pointer left the host, or the drag ended |
| `DROP` | `TransferEvent` | yes | The component that accepted last | The button came up over it. The drop takes the event's effect unless a handler calls `reject()`. |
| `TRANSFER_DONE` | `TransferEvent` | no | The source that started the transfer | The drag ended, after any `DROP`. Its effect is what the target took, empty for none; after `MOVE` the source removes what it gave away. |
| `SCROLL` | `ScrollEvent` | yes | The component under the pointer | A wheel or trackpad scrolled |

## Keyboard and focus

| Type | Interface | Bubbles | Fired at | When |
|---|---|---|---|---|
| `KEY` | `KeyEvent` | yes | The focused component, or else the top dialog's content, or else the screen's content | A key went down, repeated or came up. If nothing consumes a press, the host tries the commands' shortcuts, a mnemonic, the context menu key and the default or cancel button, then closes the top overlay on Escape or moves focus on Tab (see [Keyboard shortcuts](shortcuts.md)). |
| `CHAR` | `CharEvent` | yes | The focused component only; dropped if none | A character was typed |
| `FOCUS_GAINED` | `FocusEvent` | no | The component gaining focus | After focus changed, after the old component's `FOCUS_LOST` and the `FOCUS_ENTER` events. A control that keeps focus on an inner part, such as a spinner, a table or a tree, fires it on itself after the part. |
| `FOCUS_LOST` | `FocusEvent` | no | The component losing focus | After focus changed |
| `FOCUS_EXIT` | `FocusEvent` | no | Each ancestor of the component that lost focus that is not an ancestor of the one that gained it, innermost first | After `FOCUS_LOST`. `other()` is the component that gained focus. |
| `FOCUS_ENTER` | `FocusEvent` | no | Each ancestor of the component that gained focus that is not an ancestor of the one that lost it, outermost first | After every `FOCUS_EXIT` and before `FOCUS_GAINED`. `other()` is the component that lost focus. |
| `MODIFIERS_CHANGED` | `ModifiersEvent` | no | The screen's content and each open overlay's content | The held modifiers changed, by a modifier key or by input arriving with different modifiers |

Both focus events fire on any component, panels included, and only on one with a handler for them. `Component.isFocusWithin()` tells whether the component or a descendant has focus.

## Lifecycle

These reach only the component's own handlers, fire only when it has one, take no snapshots and do not bubble.

| Type | Interface | When |
|---|---|---|
| `Component.ATTACHED` | `LifecycleEvent` | The component joined a tree a host is showing: it was added to an attached container or composite, its screen opened or was rebuilt, or its overlay opened. Every component of the subtree hears it, each parent before its children, once the subtree's bindings have set their properties. A kept component that joins again hears it again. |
| `Component.DETACHED` | `LifecycleEvent` | The component left a tree a host is showing: it or an ancestor was removed (an expander does this with its content when it collapses, and a navigation view with the page it stops showing), its overlay closed, or its screen closed or was rebuilt. Every component of the subtree hears it, each child before its parent, while it is still attached and before its bindings stop. |

## Drawing and sizing

These reach only the component's own handlers, fire only when it has one, and take no snapshots.

| Type | Interface | Kind | When |
|---|---|---|---|
| `Component.PAINT` | `PaintEvent.Pre` | Pre | Before the component draws. Canceling it skips the component's background, border and content; its children still draw. |
| `Component.PAINTED` | `PaintEvent` | Post | After the component and its children have drawn |
| `Component.MEASURED` | `MeasureEvent` | Post | After the component measured its content; a handler may change the size it asks for |

## Control events by attribute

An attribute's events serve every control that has the attribute. Requests (`RequestEvent`, `ValueRequestEvent`) fire only for the user's actions and can be canceled. Reports (`ControlEvent`, `ValueEvent`) fire after every change, from the user or from code, unless the table says "user only". All of them bubble, and stop at the first composite above the control unless they come from content the composite holds.

| Type | Interface | Value | Controls with it |
|---|---|---|---|
| `Toggle.TOGGLE` | `ValueRequestEvent` | `Boolean`, the state about to be set | `CheckBox`, `RadioButton`, `ToggleSwitch`, `ToggleButton` |
| `Toggle.TOGGLED` | `ValueEvent` | `Boolean`, the new state | The same |
| `Ranged.CHANGE` | `ValueRequestEvent` | `Double`, already in range and on a step | `Slider`, `Spinner`, `ScrollBar`; a `ProgressBar` never fires it |
| `Ranged.CHANGED` | `ValueEvent` | `Double`, the new value | The same, and `ProgressBar` |
| `HasDate.PICK_DATE` | `ValueRequestEvent` | `LocalDate`, or null when clearing | `CalendarView`, `DatePicker`, `DateTimePicker` |
| `HasDate.DATE_PICKED` | `ValueEvent` | `LocalDate`, null when empty | The same |
| `HasTime.PICK_TIME` | `ValueRequestEvent` | `LocalTime`, or null when clearing | `TimePicker`, `DateTimePicker` |
| `HasTime.TIME_PICKED` | `ValueEvent` | `LocalTime`, null when empty | The same |
| `TextEntry.MODIFY` | `RequestEvent` | none; the control still holds the old text | `TextBox`, `TextArea`, `PasswordTextBox` |
| `TextEntry.MODIFIED` | `ValueEvent` | `Integer`, the new length in code points | The same |
| `TextEntry.CARET_MOVED` | `ValueEvent` | `TextEntry.Caret`: the anchor and the caret now, and where they were as the previous value. Fires once for each input or call that moved them, after `EDITED` and `MODIFIED`, from the user or from code. A report only; there is no request. | The same |
| `TextInput.EDIT` | `ValueRequestEvent` | `String`, the whole text the edit would produce; never null | `TextBox`, `TextArea` |
| `TextInput.EDITED` | `ValueEvent` | `String`, the new text | The same |
| `Selection.SELECT` | `ValueRequestEvent` | `Integer`, the index about to be current, or -1 | The controls below, except a `RadioGroup`, whose buttons ask through `Toggle.TOGGLE` |
| `Selection.SELECTED` | `ValueEvent` | `Integer`, the current index, or -1 | `ListView`, `TableView`, `TreeView`, `GridView`, `ComboBox`, `CycleButton`, `TabBar`, `MenuBar`, `NavigationView`, `RadioGroup` |
| `MultiSelection.SELECT_MANY` | `ValueRequestEvent` | `List<Integer>`, the indexes about to be selected | `ListView`, `TableView`, `TreeView`, `GridView`, in `MULTIPLE` mode |
| `MultiSelection.SELECTED_MANY` | `ValueEvent` | `List<Integer>`, the selected indexes | `ListView`, `TableView`, `TreeView`, `GridView` |
| `Activatable.ACTIVATE` | `ValueRequestEvent` | `Integer`, the entry's index | `ListView`, `TableView`, `TreeView`, `GridView`, `NavigationView` |
| `Activatable.ACTIVATED` | `ValueEvent` | `Integer`, the entry's index; user only | The same |
| `Openable.OPEN` | `RequestEvent` | none | `ComboBox`, `DropDownButton`, `DatePicker`, `TimePicker`, `DateTimePicker`. `open()` from code asks nothing. |
| `Openable.OPENED` | `ControlEvent` | none | The same, and `InfoBar`, `Menu` and `Flyout`, which only code or another control opens |
| `Closable.CLOSE` | `RequestEvent` | none | `Window` and `InfoBar` when the user closes them, and every popup (`Menu`, `Flyout`, the lists of `ComboBox` and the pickers) before Escape or a press outside closes it. Canceling it keeps the popup open, and the key or press is used up. `close()` from code, choosing a menu item and a submenu closing with its parent ask nothing. |
| `Closable.CLOSED` | `ControlEvent` | none | `Window`, the controls with `Openable`, and `Overlay` |
| `Validatable.VALIDATED` | `ValueEvent` | `Validation`, the new validation, and the one before as the previous value; neither is null. A report only; there is no request. | `TextBox`, `TextArea`, `PasswordTextBox`, `Spinner`, `Slider`, `ComboBox`, `CheckBox`, `DatePicker`, `TimePicker`, `DateTimePicker`, `ColorPicker` |
| `HasContextMenu.SHOW_CONTEXT_MENU` | `ValueRequestEvent` | `Popup`, the menu about to open; a handler may cancel it or set another popup, and null opens nothing. Fires only for the user's actions. | `Container` and every control that has a tooltip |
| `CellEditing.EDIT_CELL` | `ValueRequestEvent` | The item about to be edited; a value a handler sets is ignored | `ListView`, `TreeView`, `TableView`, `GridView`, when they have a cell editor |
| `CellEditing.CELL_EDITED` | `ValueEvent` | The item the editor committed, and the item that was edited as the previous value. The control has not changed its items. | The same |
| `ItemDragging.MOVE_ITEMS` | `ValueRequestEvent` | `ItemMove`: the ascending indexes of the rows and the index they would go before. A handler may cancel it or rewrite the move; if none cancels, the list moves its items with `setItems`. | `ListView` |

## Control events by control

| Type | Interface | Value |
|---|---|---|
| `Button.PRESS` | `RequestEvent` | none |
| `Button.PRESSED` | `ControlEvent` | none |
| `CheckBox.CHANGE` | `ValueRequestEvent` | `CheckState` the change would leave; fires before `Toggle.TOGGLE` |
| `CheckBox.CHANGED` | `ValueEvent` | `CheckState`, the new state |
| `Slider.COMMITTED` | `ValueEvent` | `Double`, the value once the user is done: on the release that ends a press or drag, after each key that changed it, and after a change from code while no press is under way. The previous value is the last committed one. Never fires while `isAdjusting()` is true. |
| `TextBox.SUBMIT` | `RequestEvent` | none; Enter or keypad Enter, not on repeat |
| `TextBox.SUBMITTED` | `ValueEvent` | `String`, the text, with `previous()` equal to it; user only |
| `TextBox.COMPLETE` | `ValueRequestEvent` | `TextBox.Suggestion` the user accepted; never null |
| `TextBox.COMPLETED` | `ValueEvent` | `TextBox.Suggestion` that was applied, with `previous()` equal to it; user only |
| `PasswordTextBox.SUBMIT` | `RequestEvent` | none; Enter or keypad Enter, not on repeat |
| `PasswordTextBox.SUBMITTED` | `ControlEvent` | none; read the box with `withPassword`; user only |
| `PasswordTextBox.REVEAL` | `ValueRequestEvent` | `Boolean`, true to show the text; null keeps the state |
| `PasswordTextBox.REVEALED` | `ValueEvent` | `Boolean`, true while the text shows |
| `ColorPicker.PICK` | `ValueRequestEvent` | `Integer`, the new ARGB color; never null |
| `ColorPicker.PICKED` | `ValueEvent` | `Integer`, the new ARGB color |
| `CalendarView.BROWSE` | `ValueRequestEvent` | `YearMonth` about to show |
| `CalendarView.BROWSED` | `ValueEvent` | `YearMonth` now shown |
| `DateTimePicker.PICK` | `ValueRequestEvent` | `LocalDateTime`, or null when clearing |
| `DateTimePicker.PICKED` | `ValueEvent` | `LocalDateTime`, null when empty |
| `TableView.SORT` | `ValueRequestEvent` | `TableView.Sort` about to apply |
| `TableView.SORTED` | `ValueEvent` | `TableView.Sort` now applied; only when the column or direction changed |
| `TableView.SORT_MANY` | `ValueRequestEvent` | `List<TableView.Sort>`, the order about to apply, first sort first; empty for the set order. Asked for a click with Shift on a title. |
| `TableView.SORTED_MANY` | `ValueEvent` | `List<TableView.Sort>`, the new order, and the one before as the previous value; fires before `SORTED` when the first sort changed too |
| `TableView.RESIZE_COLUMN` | `ValueRequestEvent` | `TableView.ColumnWidth`: the column and the width it is about to get, already within its limits. Asked on each move of a drag on a title's right edge, and for a double click there. |
| `TableView.COLUMN_RESIZED` | `ValueEvent` | `TableView.ColumnWidth`, the new width; the previous value has the old one. A width set from code fires nothing. |
| `TableView.MOVE_COLUMN` | `ValueRequestEvent` | `TableView.ColumnMove`: the column and the index in `columns()` it is about to have |
| `TableView.COLUMN_MOVED` | `ValueEvent` | `TableView.ColumnMove`, the new index; the previous value has the old one. Adding and removing columns from code fires nothing. |
| `TreeView.EXPAND` | `ValueRequestEvent` | `TreeView.Expansion`: the item and whether it is about to expand |
| `TreeView.EXPANDED` | `ValueEvent` | `TreeView.Expansion`: the item and its new state |
| `GridView.CLICK` | `ValueRequestEvent` | `GridView.Click`: the cell's index, its item, the button and the modifiers |
| `GridView.CLICKED` | `ValueEvent` | `GridView.Click` as the request ended; user only |
| `Menu.CHOOSE` | `ValueRequestEvent` | `MenuEntry` chosen, a `MenuItem` or `CheckMenuItem` |
| `Menu.CHOSEN` | `ValueEvent` | `MenuEntry`, once the menus closed and the action ran; user only |
| `Expander.EXPAND` | `ValueRequestEvent` | `Boolean`, true to show the content; null is ignored |
| `Expander.EXPANDED` | `ValueEvent` | `Boolean`, true when shown |
| `NavigationView.EXPAND` | `ValueRequestEvent` | `Boolean`, whether the pane over the content would be open |
| `NavigationView.EXPANDED` | `ValueEvent` | `Boolean`, whether the pane is open |
| `NavigationView.EXPAND_ITEM` | `ValueRequestEvent` | `NavigationView.Expansion`: the item and whether it would expand |
| `NavigationView.ITEM_EXPANDED` | `ValueEvent` | `NavigationView.Expansion`: the item and its new state |
| `SplitPane.MOVE` | `ValueRequestEvent` | `Float`, the fraction of the space the first side is about to get |
| `SplitPane.MOVED` | `ValueEvent` | `Float`, the new fraction |
| `SplitPane.COLLAPSE` | `ValueRequestEvent` | `SplitPane.Side` about to collapse, or null to restore the collapsed side. Asked for a double click on the divider, Enter while it has focus, and a drag on the divider of a collapsed pane. |
| `SplitPane.COLLAPSED` | `ValueEvent` | `SplitPane.Side` now collapsed, null when none is; fires for changes from code too |
| `Window.RESIZE` | `ValueRequestEvent` | `Size` the window is about to take, without the margin |
| `Window.RESIZED` | `ValueEvent` | `Size`, the new size; user only |
| `Window.MAXIMIZE` | `ValueRequestEvent` | `Boolean`, true to maximize and false to restore. Asked for the maximize button and a double click on the title bar. |
| `Window.MAXIMIZED` | `ValueEvent` | `Boolean`, the new state, from the user or from code |
| `Window.COLLAPSE` | `ValueRequestEvent` | `Boolean`, true to collapse to the title bar and false to expand. Asked for the collapse button. |
| `Window.COLLAPSED` | `ValueEvent` | `Boolean`, the new state, from the user or from code |
| `MessageBox.ANSWER` | `ValueRequestEvent` | `MessageBox.Result`, the answer. Results are records equal by their ids. |
| `MessageBox.ANSWERED` | `ValueEvent` | `MessageBox.Result`, once, before the overlay closes |
| `ScrollPanel.SCROLLED` | `ValueEvent` | `ScrollPanel.State` after the change; there is no request |

Each constant's Javadoc says exactly when it fires, what a handler may set and how the control checks it. "User only" marks a report that code has no method to cause.

## What the events carry

| Interface | Methods |
|---|---|
| `Event` | `target()`, `current()`, `cause()`, `origin()`, `stopPropagation()`, `isPropagationStopped()` |
| `Cancelable` | Event, plus `cancel()`, `isCancelled()`, `consume()`, `isHandled()`. Every input event and every request is one. |
| `SnapshotEvent` | Cancelable, plus `snapshot()`, `currentSnapshot()` and `snapshotOf(component)`, which returns an `Optional`. Every input event and every request is one; reports, paint and measure events have no snapshot methods. |
| `LifecycleEvent` | Event, with nothing more. `ATTACHED` and `DETACHED` carry it. |
| `PointerEvent` | `x()`, `y()`, `localX()`, `localY()`, `modifiers()`, `pointerId()` (0 for the mouse, a pen and the first touch) and `pressure()` (0 to 1; 1 for a mouse) |
| `PointerButtonEvent` | Pointer, plus `button()`, `clicks()` (1, 2 or 3) |
| `DragEvent` | Pointer, plus `button()`, `clicks()`, `startX()`, `startY()`, `deltaX()`, `deltaY()` and `startTransfer(transfer, allowed)`, with a form that also takes a function that builds the drag image |
| `TransferEvent` | Pointer, plus `transfer()`, `allowed()`, `source()` (empty for a drag from another application), `preferred()` (from the modifiers), `effect()`, `accept(effect)`, `accept()` and `reject()` |
| `ScrollEvent` | Pointer, plus `scrollX()` (positive toward the left), `scrollY()` (positive away from the user), `kind()` (a `ScrollKind`) |
| `KeyEvent` | `key()`, `action()`, `modifiers()`, `is(key, required, policy)`, `is(keyStroke)` and `shortcutModifier()` |
| `CharEvent` | `codePoint()`, `text()`, `modifiers()` |
| `FocusEvent` | `other()`: an `Optional` of the component on the other side of the move |
| `ModifiersEvent` | `modifiers()`: the modifiers now held |
| `RequestEvent<C>` | Cancelable, plus `control()` |
| `ValueRequestEvent<C, V>` | Request, plus `value()` and `setValue(value)` |
| `ControlEvent<C>` | Event, plus `control()` |
| `ValueEvent<C, V>` | Control event, plus `value()` and `previous()` |
| `PaintEvent` | Event, plus `painter()` and `bounds()`. `PaintEvent.Pre` is also Cancelable. |
| `MeasureEvent` | Event, plus `context()`, `availableWidth()`, `availableHeight()`, `width()`, `height()`, `setWidth(width)`, `setHeight(height)` |

## Default behavior of the built-ins

| Type | `Button` | `CheckBox` | `Slider` | `TextBox` |
|---|---|---|---|---|
| `POINTER_PRESS` | | | Moves the value to the pointer on a left press and consumes it | Places the caret, or selects a word (2 clicks) or everything (3 clicks). Shift extends. |
| `CLICK` | Presses on a left click and consumes it | Toggles on a left click and consumes it | | |
| `DRAG_START`, `DRAG_END` | | | Consumes left-button drags | |
| `DRAG` | | | Follows the pointer and consumes it | Extends the selection, by word if the press was a double click |
| `KEY` | Presses on Enter, keypad Enter or Space (`PRESS` only) and consumes it | Toggles on Enter, keypad Enter or Space (`PRESS` only) and consumes it | Moves on arrows, Page Up, Page Down, Home and End, and consumes them | Edits and moves the caret; consumes what it uses and every printable key without Control or Alt |
| `CHAR` | | | | Inserts the character and consumes it |
| `FOCUS_GAINED` | | | | Moves the caret to the end, or selects everything after `setSelectAllOnFocus(true)` |

`Label`, `Image`, `Avatar` and the plain panels have no default behavior. The other controls document theirs in the [controls guide](../guides/controls.md): a `ScrollPanel` scrolls on `SCROLL`, unused keys and drags of its bar; a `ListView` selects on `POINTER_PRESS`, activates on a double press or Enter, and moves on keys; a `ComboBox` opens on `CLICK` and changes on keys; a `DropDownButton` opens on `CLICK` and on keys; a `Flyout` moves focus on Tab; an `InfoBar`'s close button closes the bar on `CLICK` and on Enter or Space; a `NavigationView` picks items, expands them and opens its pane on `CLICK`, and moves its cursor on keys; a `Window` moves on drags of its title bar and closes on Escape; a `TabBar` selects on clicks and Left and Right; a `ScrollBar` pages on `POINTER_PRESS` on its track, follows drags of its thumb, and moves on `SCROLL` and, when made focusable, on keys. Each consumes what it acts on.

Anything not listed does nothing by default and is not consumed. Disabled controls do nothing.
