# Keyboard shortcuts

The keys the hosts and the most keyboard-driven built-in controls respond to. Lists, trees, combo boxes, menus, text areas, spinners and the other controls describe their keys in [Controls](../guides/controls.md). Ctrl means Control held without Alt, and Shortcut means the platform's shortcut modifier held without Alt: Control on most platforms, Command where the platform's conventions name it (`Conventions.shortcutModifier`). Word moves and word deletes use Ctrl; select all, copy, cut, paste, undo and redo use Shortcut. On macOS, see the [note on Command](../platforms/differences.md#modifier-keys).

## Hosts

| Keys | Action |
|---|---|
| Tab | Focus the next tabbable component, wrapping at the end, unless something consumed the key. With a dialog open, only components in the dialog take part. Held Tab repeats. |
| Shift+Tab | Focus the previous one |
| Ctrl+Tab, Alt+Tab | Nothing; left to the engine. A text area that accepts tabs moves focus with Ctrl+Tab. |
| Escape | Close the top overlay that is not a toast, such as a popup, menu or dialog, after asking it `Closable.CLOSE`, unless something consumed the key (press only) |
| Escape during a drag that carries a transfer | End the drag with no effect, before anything else hears the key |

Keys go to the focused component first. When nothing has focus they go to the top dialog's content, or to the screen's content, and the host acts only on what nothing consumed. For a press or repeat that nothing consumed, the host tries these in order and stops at the first that acts:

1. The shortcut of an enabled command: the commands scoped to the key's target and each of its ancestors, innermost first; then the host's own while no dialog is open; then the commands of the menu bars in the top dialog, or in the content when there is no dialog. See [Commands](../guides/commands.md).
2. Alt with a letter or a number-row digit, Shift allowed and no Control or Super: the control whose caption marks that mnemonic, searched from the top overlay down to the content and stopping at a dialog.
3. F10 alone: the keys go to the first visible, enabled menu bar, as for Alt alone below.
4. The Menu key, or Shift+F10: the context menu of the key's target or its nearest ancestor that has one, below it.
5. Enter or keypad Enter without modifiers: the default button of the key's scope. Escape without modifiers: the cancel button of the scope.
6. Escape: the top overlay that is not a toast closes.
7. Tab, or Shift+Tab, without Control or Alt: focus moves.

The scope of a default or cancel button is the nearest window that holds the key's target, else the top layer. Alt, Alt with a letter and F10 do nothing on a platform whose `Conventions.mnemonics` is false.

## Context menus

| Input | Action |
|---|---|
| Right click, or a right press where the conventions act on press | Open the context menu of the pressed component or its nearest ancestor that has one, at the pointer |
| Menu key, Shift+F10 | Open the key target's menu below it |
| A first touch held for 500 ms without moving | Open the menu at that point, and the release clicks nothing |

Before it opens, the owner asks `HasContextMenu.SHOW_CONTEXT_MENU`, which a handler can cancel or rewrite. A text box, text area and password box start with a menu of their own.

## Menu bar, Alt and F10

Alt pressed and released alone, with no other key or press between, and F10 give the keys to the first visible, enabled menu bar in the top dialog or the content. Focus does not move to the bar.

| Keys | Action |
|---|---|
| Alt+letter | Open the menu of the title whose caption marks that letter |
| Left, Right | Move the highlight to the previous or next title that opens a menu, wrapping |
| Down, Enter, Space | Open the highlighted title's menu from the keyboard |
| A title's mnemonic letter | Open that title's menu |
| Escape, Alt alone again | Let go of the keys |
| Any other key | Let go, and the key goes on to its usual target |

A pointer press, a focus change or the bar leaving the tree also lets go.

## Menu, while open

| Keys | Action |
|---|---|
| Up, Down | Move the highlight past separators and disabled items, wrapping (repeats) |
| Home, End | Move the highlight to the first or last usable item |
| Right | Open the highlighted submenu with its first item highlighted; at an item with no submenu, open the next menu bar title's menu |
| Left | Close a submenu; in the bar's own menu, open the previous title's menu |
| Enter, keypad Enter, Space | Choose the highlighted item, or open a submenu as Right does |
| A letter or digit, with or without Alt | Choose the one usable item whose mnemonic it is, or highlight the items that share it in turn. A letter no item claims moves to the next item whose text starts with it. |
| Escape | Close the menu, after asking `Closable.CLOSE`; focus goes back to the menu it opened from |

An open menu underlines the mnemonics once the keyboard opened it or moved through it. With Alt and a letter that no item claims, the key goes on to the root, so Alt+E in the File menu opens the Edit menu.

## Button and ToggleButton, while focused

| Keys | Action |
|---|---|
| Enter, keypad Enter, Space | Press, or flip a toggle button (not on repeat) |
| Alt+letter | From anywhere in the same layer: focus the button whose caption marks that letter, and press it |

A default button also takes the Enter that nothing else consumed anywhere in its scope, and a cancel button the Escape. Where the conventions act on press, a left press does what the click does.

## DropDownButton, while focused

| Keys | Action |
|---|---|
| Enter, keypad Enter, Space, Alt+Down | Open the popup (not on repeat). A menu opened this way highlights its first item. |

## InfoBar's close button, while focused

| Keys | Action |
|---|---|
| Enter, keypad Enter, Space | Close the bar, after asking `Closable.CLOSE` (not on repeat) |

## CheckBox, while focused

| Keys | Action |
|---|---|
| Enter, keypad Enter, Space | Toggle (not on repeat) |

## ToggleSwitch, while focused

| Keys | Action |
|---|---|
| Enter, keypad Enter, Space | Flip it (not on repeat) |

## RadioButton, while focused

| Keys | Action |
|---|---|
| Enter, keypad Enter, Space | Select it (not on repeat) |
| Up, Left | Select and focus the previous usable button of the group, wrapping (repeats; not with Control or Alt) |
| Down, Right | Select and focus the next one |

A radio group is one stop for Tab and Shift+Tab: the selected button, or the first button while none is selected.

## CycleButton, while focused

| Keys | Action |
|---|---|
| Enter, keypad Enter, Space | Select the next value, wrapping (not on repeat) |
| Shift+Enter, Shift+Space | Select the previous value |

## Expander, while its header has focus

| Keys | Action |
|---|---|
| Enter, keypad Enter, Space | Expand or collapse it (not on repeat). Keys pressed in the content do not. |

## Slider, while focused

| Keys | Action |
|---|---|
| Left, Down | Lower by the key step (repeats) |
| Right, Up | Raise by the key step (repeats) |
| Page Down, Page Up | Lower or raise by ten key steps |
| Home, End | Go to the minimum or maximum |

With `setSnapToTicks(true)` the arrow keys move by a tick, and with no step a slider moves by a hundredth of the range, or by the theme's `slider.key.travel`.

## Spinner, while its field has focus

| Keys | Action |
|---|---|
| Up, Down | Step by one step (repeats; not with Control or Alt) |
| Page Up, Page Down | Step ten steps |
| Enter | Take the typed text as the value |

The wheel steps once for each notch. A trackpad's precise scrolls add up, one step for each 10 units. A left press on a step button steps once, and holding it steps again after 400 ms and then every 33 ms. The spinner is one Tab stop, at its field.

## SplitPane, while its divider has focus

| Keys | Action |
|---|---|
| Left, Right (side by side), Up, Down (one above the other) | Move the divider by `SplitPane.KEY_STEP`, 10 units, or 1 with Shift (repeats) |
| Home, End | Move the divider until the first or the second side is at its minimum |
| Enter | Collapse or restore a side, where the pane has a collapsible one |

Each move asks `SplitPane.MOVE`. The divider is a Tab stop between the two sides. A double click on it collapses or restores a side too, and the keys do nothing while a side is collapsed.

## ColorPicker, while focused

| Keys | Action |
|---|---|
| Left, Right | Move the point in the square by 1 percent of saturation, or 10 with Shift (repeats) |
| Up, Down | Move it by 1 percent of brightness, or 10 with Shift |
| Page Up, Page Down | Move the hue by 1 degree toward the top or bottom of its strip, or 10 with Shift |
| Home, End | Make the color opaque or clear, while the alpha strip shows |

Each key asks `ColorPicker.PICK`. The picker is a Tab stop before its hex field.

## ScrollPanel, while focus is inside it

These keys scroll the panel when no focused descendant consumed them.

| Keys | Action |
|---|---|
| Up, Down | Scroll vertically by the scroll rate, 10 units by default (repeats) |
| Page Up, Page Down | Scroll vertically by the viewport less one rate |
| Home, End | Scroll to the top or bottom |
| Left, Right | Scroll sideways by the scroll rate, while the panel scrolls sideways |
| Shift with the wheel | Scroll sideways, where the engine sends the wheel as a vertical scroll |

The panel leaves a key alone with Control or Alt held, and consumes one only when the offset changed.

## Lists, tables, trees, grids and combo boxes

Up, Down, Page Up, Page Down, Home, End, Enter and the other keys of these controls are in [Controls](../guides/controls.md). These apply to several of them.

| Input | Action |
|---|---|
| Typed characters | Jump to the next item whose name starts with them. The prefix resets after `list.typeahead.timeout`, 1000 ms by default; the same letter again cycles through the matches; a space never starts a prefix. `setTypeAhead(null)` turns it off. |
| Shortcut+click, Shift+click | In `MULTIPLE` mode, add or remove a row, or select the range from the anchor |
| Shortcut+A | In `MULTIPLE` mode, select every row |
| F2 | Edit the current row in place, where the control has a cell editor |
| Double press | Edit the row or cell under it where the control has a cell editor, otherwise activate it |

Keys with Alt are left alone, and so are keys with Control or Shortcut other than those listed.

## TableView, with the pointer

| Input | Action |
|---|---|
| Click on a sortable title | Sort by that column ascending, then descending, then back to the set order |
| Shift+click on a title | Add the column to the sort order, or step it on through the same three states |
| Drag the right edge of a resizable title | Resize the column |
| Double click on that edge | Fit the column to its title and the cells in view |
| Drag a movable title | Move the column; a mark shows where it lands |
| Right click on the header | Show a menu that shows and hides columns |

A horizontal wheel notch of a table with a horizontal bar scrolls 10 units.

## GridView, while focused

| Keys | Action |
|---|---|
| Left, Right | Move the cursor one cell, going on across the ends of rows |
| Up, Down | Move the cursor one row in the same column |
| Home, End | Move to the ends of the cursor's row; with Control, to the first and last cell |
| Page Up, Page Down | Move by the rows in view in the same column |
| Enter, Space | Activate the cursor cell (not on repeat) |
| Shortcut+A | In `MULTIPLE` mode, select every cell |
| F2, a typed character | Edit the cursor cell in place, where the grid has a cell editor |

While the grid is selectable the cursor selects as it moves, and Shift extends the range in `MULTIPLE` mode. Keys with Alt are left alone.

## Dragging

During a drag that carries a transfer, the modifiers pick the effect among those the source allows.

| Keys | Effect |
|---|---|
| none, or Shift | Move |
| Ctrl | Copy |
| Ctrl+Shift | Link |

Near the edge of a scroll panel or list, a drag scrolls it. Escape ends the drag.

## MessageBox, while open as a dialog

| Keys | Action |
|---|---|
| Enter, keypad Enter | Press the default result's button, the first result unless `setDefaultResult` named another |
| Escape | Answer with the dismiss result, through its cancel button when it has one |

## TextBox, while focused

| Keys | Action |
|---|---|
| Left, Right | Move one character, a grapheme cluster; with a selection and no Shift, collapse it. Right to left, Left moves toward the end of the text. |
| Home, End | Move to the start or end |
| Ctrl+Left, Ctrl+Right | Move by word. Words stop at runs of punctuation and skip spaces, so `path/to/file` takes five steps. |
| Shift with any movement | Extend the selection |
| Backspace, Delete | Delete the selection, or one character |
| Ctrl+Backspace, Ctrl+Delete | Delete the selection, or one word |
| Shortcut+A | Select all |
| Shortcut+C | Copy |
| Shortcut+X | Cut |
| Shortcut+V | Paste, with line breaks and other control characters removed |
| Shortcut+Z | Undo, consumed only when there was something to undo |
| Shortcut+Y, Shortcut+Shift+Z | Redo, consumed only when there was something to redo |
| Menu key, Shift+F10 | Open the box's context menu |
| Enter, keypad Enter | Submit, consumed when a `SUBMIT` or `SUBMITTED` handler is on the box's path |

A key held with Alt neither edits nor moves the caret, and reaches the engine. A key with Super held, as Command+S is on a platform whose shortcut modifier is Control, passes through the box to the host's shortcuts. An undo or redo by key asks `MODIFY` and `EDIT` as an edit does, and does nothing in a read-only box.

Not handled, so they bubble to ancestors and the engine: Tab, Escape, Up, Down, Page Up, Page Down, Enter while nothing hears `SUBMIT` or `SUBMITTED`, and every Control or Shortcut combination not listed above.

With a suggestion provider, Ctrl+Space shows the suggestions. While the suggestion list is open:

| Keys | Action |
|---|---|
| Up, Down | Move the highlight, wrapping at either end |
| Tab, Enter, keypad Enter | Accept the highlighted suggestion |
| Escape | Close the list |

## PasswordTextBox, while focused

The `TextBox` keys above, with these differences:

| Keys | Action |
|---|---|
| Shortcut+C, Shortcut+X | Nothing |
| Shortcut+Z, Shortcut+Y, Shortcut+Shift+Z | Nothing; the box keeps no history, so the keys go to the engine |
| Left, Right, Backspace, Delete | Move or delete one code point |
| Ctrl+Left, Ctrl+Right, Ctrl+Backspace, Ctrl+Delete | Move or delete to the start or end, since the whole text counts as one word |
| Shortcut+V | Paste with line breaks removed, unless the length limit or the code point filter would change the pasted text, or it still holds a control character |
| Alt+F8 | Show or hide the text, while the box is revealable (not on repeat). Consumed even when it is not. |

A double click selects everything. The box consumes every typed character while it has focus.

## TextArea, while focused

The `TextBox` keys, with these differences.

| Keys | Action |
|---|---|
| Enter, keypad Enter | Insert a line break |
| Up, Down, Page Up, Page Down | Move the caret by rows, keeping its column on screen |
| Tab | Type a tab, in an editable area that accepts tabs (`setAcceptsTab(true)`); otherwise the key moves focus |
| Shift+Tab | Nothing, in an area that accepts tabs |
| Ctrl+Tab, Ctrl+Shift+Tab | Move focus, in an area that accepts tabs |
| Shortcut+V | Paste, turning CR, NEL, U+2028 and U+2029 into line breaks, keeping tabs and dropping other control characters |

## CalendarView, while focused

| Keys | Action |
|---|---|
| Left, Right | Move the cursor a day; in the year and decade views, a month or a year (repeats) |
| Up, Down | Move the cursor a week; in the year and decade views, four months or four years (repeats) |
| Page Up, Page Down | Move the cursor a month, or with Shift a year; in the year and decade views, a year or ten years (repeats) |
| Home, End | Move the cursor to the first or last day of the month, in the month view only |
| Enter, keypad Enter, Space | Pick the cursor's day (not on repeat); in the year and decade views, zoom in on its month or year |
| Ctrl+Up | Zoom out |
| Ctrl+Down | Zoom in on the cursor's month or year |

Other keys with Control or Alt are not handled.

## DatePicker, TimePicker and DateTimePicker, while focused

| Keys | Action |
|---|---|
| Left, Right | Focus the part before or after; not consumed at the first or last part, unless the time popup is open (repeats) |
| Home, End | Focus the first or last part |
| Up, Down | Step the focused part, wrapping within it except for the year; with the time popup open, set the value at once (repeats) |
| Digits | Type into the focused part, moving on when no further digit fits |
| The separator | Move on to the next part |
| A letter | On the half of the day, pick the half whose name starts with it |
| Backspace | Empty the focused part |
| Delete | Drop the draft and clear the value, while the picker is clearable |
| Enter, keypad Enter | Take the draft when it makes a value, consumed only when there was a draft; with the time popup open, take it and close the popup (not on repeat) |
| Escape | Drop the draft, consumed only when there was one; with the time popup open, drop it and close the popup |
| Alt+Down, F4 without Control or Alt | Open the popup, or close the time popup (not on repeat). Alt+F4 is left alone, for the window. |

These keys, other than Alt+Down and F4, need Control and Alt released. While the time popup of a `TimePicker` or `DateTimePicker` is open, Home, End, Backspace and Delete do nothing. A `DatePicker`'s calendar takes focus when it opens, so the [CalendarView](#calendarview-while-focused) keys apply there, and Escape closes it. The pickers consume every typed character while they have focus.

## NavigationView, while focused

| Keys | Action |
|---|---|
| Up, Down | Move the cursor to the item above or below, skipping headers, separators, disabled items and hidden children, without wrapping (repeats) |
| Home, End | Move the cursor to the first or last item |
| Enter, keypad Enter, Space | Pick the cursor's item as a click does, activating it even with `setActivateOnClick(false)`; with the pane hidden in `MINIMAL`, open it (not on repeat) |
| Right | Expand the cursor's item, or open its menu of children in the column of icons |
| Left | Collapse the cursor's item, or move the cursor to its parent; nothing in the column of icons |
| Escape | Close the pane over the content, unless something consumed the key |

## Flyout, while focus is inside it

| Keys | Action |
|---|---|
| Tab | Focus the next tabbable component in the flyout, wrapping at the end; focus stays in the flyout |
| Shift+Tab | Focus the previous one |
| Escape | Close it, unless something in it consumed the key, and give focus back |

## Mouse in a TextBox

| Input | Action |
|---|---|
| Click | Put the caret at the nearest character boundary |
| Shift+click | Extend the selection to there |
| Double click | Select the word, run of punctuation or run of spaces under the pointer |
| Triple click | Select everything |
| Drag | Select by character |
| Drag after a double click | Select by word |
| Press inside the selection | Place the caret when the button comes up, unless a drag started |
| Drag from inside the selection | Carry the selected text out; drop it in a box to move it, with Ctrl to copy it |
| Right click | Open the context menu |
