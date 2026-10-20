# Keyboard shortcuts

Every key the built-in controls and hosts respond to. Control means Control without Alt. On macOS, see the [note on Command](../platforms/differences.md#modifier-keys).

## Hosts

| Keys | Action |
|---|---|
| Tab | Focus the next tabbable component, wrapping at the end, unless something consumed the key |
| Shift+Tab | Focus the previous one |
| Ctrl+Tab, Alt+Tab | Nothing; left to the engine |

## Button, while focused

| Keys | Action |
|---|---|
| Enter, keypad Enter, Space | Press (not on repeat) |

## DropDownButton, while focused

| Keys | Action |
|---|---|
| Enter, keypad Enter, Space, Alt+Down | Open the popup (not on repeat). A menu opened this way highlights its first item. |

## CheckBox, while focused

| Keys | Action |
|---|---|
| Enter, keypad Enter, Space | Toggle (not on repeat) |

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

Not handled, so they bubble to ancestors and the engine: Tab, Escape, Enter, Up, Down, Page Up, Page Down, and every Control combination not listed above.

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
| Alt+F8 | Show or hide the text, while the box is revealable |
| Enter, keypad Enter | Submit, consumed when a `SUBMIT` or `SUBMITTED` handler is on the box's path |

A double click selects everything. The box consumes every typed character while it has focus.

## CalendarView, while focused

| Keys | Action |
|---|---|
| Left, Right | Move the cursor a day; in the year and decade views, a month or a year |
| Up, Down | Move the cursor a week; in the year and decade views, four months or four years |
| Page Up, Page Down | Move the cursor a month; with Shift, a year |
| Home, End | Move the cursor to the first or last day of the month |
| Enter, Space | Pick the cursor's day; in the year and decade views, zoom in on its month or year |
| Ctrl+Up | Zoom out |
| Ctrl+Down | Zoom in on the cursor's month or year |

## DatePicker, TimePicker and DateTimePicker, while focused

| Keys | Action |
|---|---|
| Left, Right | Focus the part before or after |
| Home, End | Focus the first or last part |
| Up, Down | Step the focused part, wrapping within it; with the time popup open, set the value at once |
| Digits | Type into the focused part, moving on when no further digit fits |
| The separator | Move on to the next part |
| A letter | On the half of the day, pick the half whose name starts with it |
| Backspace | Empty the focused part |
| Delete | Clear the value, while the picker is clearable |
| Enter, keypad Enter | Take the draft when it makes a value; with the time popup open, also close it |
| Escape | Drop the draft, consumed only when there was one; with the time popup open, close it |
| Alt+Down, F4 | Open the popup, or close it |

The pickers consume every typed character while they have focus.

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
