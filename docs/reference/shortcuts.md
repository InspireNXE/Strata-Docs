# Keyboard shortcuts

The keys the hosts and the most keyboard-driven built-in controls respond to. Lists, trees, combo boxes, menus, text areas, spinners and the other controls describe their keys in [Controls](../guides/controls.md). Control means Control without Alt. On macOS, see the [note on Command](../platforms/differences.md#modifier-keys).

## Hosts

| Keys | Action |
|---|---|
| Tab | Focus the next tabbable component, wrapping at the end, unless something consumed the key. With a dialog open, only components in the dialog take part. Held Tab repeats. |
| Shift+Tab | Focus the previous one |
| Ctrl+Tab, Alt+Tab | Nothing; left to the engine |
| Escape | Close the top overlay that is not a toast, such as a popup, menu or dialog, unless something consumed the key (press only) |

Keys go to the focused component first. When nothing has focus they go to the top dialog's content, or to the screen's content, and the host acts only on what nothing consumed.

## Button, while focused

| Keys | Action |
|---|---|
| Enter, keypad Enter, Space | Press (not on repeat) |

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

## TextBox, while focused

| Keys | Action |
|---|---|
| Left, Right | Move one code point; with a selection and no Shift, collapse it |
| Home, End | Move to the start or end |
| Ctrl+Left, Ctrl+Right | Move by word |
| Shift with any movement | Extend the selection |
| Backspace, Delete | Delete the selection, or one code point |
| Ctrl+Backspace, Ctrl+Delete | Delete the selection, or one word |
| Ctrl+A | Select all |
| Ctrl+C | Copy |
| Ctrl+X | Cut |
| Ctrl+V | Paste, with line breaks removed |
| Enter, keypad Enter | Submit, consumed when a `SUBMIT` or `SUBMITTED` handler is on the box's path |

Not handled, so they bubble to ancestors and the engine: Tab, Escape, Up, Down, Page Up, Page Down, Enter while nothing hears `SUBMIT` or `SUBMITTED`, and every Control combination not listed above.

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
| Ctrl+C, Ctrl+X | Nothing |
| Ctrl+Left, Ctrl+Right, Ctrl+Backspace, Ctrl+Delete | Move or delete to the start or end, since the whole text counts as one word |
| Ctrl+V | Paste, unless the length limit or the code point filter would change the pasted text |
| Alt+F8 | Show or hide the text, while the box is revealable (not on repeat). Consumed even when it is not. |

A double click selects everything. The box consumes every typed character while it has focus.

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
| Double click | Select the word, or run of spaces, under the pointer |
| Triple click | Select everything |
| Drag | Select by character |
| Drag after a double click | Select by word |
