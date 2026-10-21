# Defaults and timings

Every number Strata picks for you.

## Every component

| Setting | Default |
|---|---|
| Visible, enabled | yes |
| Focusable, tabbable | no, except buttons, drop-down buttons, checkboxes, radio buttons, toggle switches, sliders, text boxes, text areas, lists (also inside tables and trees), combo boxes, tab bars and expanders, the controls that are `Focusable`. Scroll bars and scroll panels are `Focusable` but start with focus off. Menus and flyouts are focusable but not tabbable. A color picker is neither, but its hex field is. |
| Margin | `Insets.NONE` |
| Minimum size | 0 by 0 |
| Maximum size | `Float.POSITIVE_INFINITY` both ways |
| Fixed size | unset (`Float.NaN`) |
| Alignment | `LEFT`, `TOP`, except windows and message boxes (centered), separators (stretched along their line) and spacers (stretched both ways) |
| Bounds before the first layout | `Rect.ZERO` |

## Containers

| Setting | Default |
|---|---|
| Padding | `Insets.NONE` |
| Clips children | yes |
| Background | `Paint.NONE` |
| Border | `Border.NONE` |
| `StackPanel` orientation | `VERTICAL` from `stackPanel()` |
| `StackPanel` spacing | 0 |
| `StackPanel` spreading | off |
| `StackPanel` child weight | 0 (the child gets its desired size) |
| `Spacer` size | 0 by 0 from `spacer()` |
| `GridPanel` rows and columns | none, which acts as one auto cell |
| `GridPanel` spacing | 0 by 0 |
| `AbsolutePanel` position from plain `add` | (0, 0) |
| `GridPanel` cell from plain `add` | row 0, column 0, spanning 1 by 1 |
| `ScrollPanel` policies | horizontal `NEVER`, vertical `AUTO` |
| `ScrollPanel` scroll rate | 10 units a wheel notch or arrow key |
| `ScrollPanel` scroll bar | 6 wide, 2 from the viewport; thumb at least 32 long and at most the track less 8 |
| `Window` padding | the theme's `window.padding`, else 3 on every side |
| `Window` title bar | one line plus 3 above and below |
| `Window` close button | 12 wide, at the right end of the title bar |
| `Window` closable, movable | yes, yes |
| `Window` resizable, edge resizable | no, no |
| `Window` resize grip | 10 by 10 in the bottom-right corner, drawn as six 2 by 2 squares one unit in from the edges |
| `Window` resizing edges | 3 in from each edge; within 10 of the next edge they resize both ways |
| `Window` smallest size by resizing | the title bar's width, and the bar's height plus 10, unless the minimum size is larger |

## Controls

| Control | Setting | Default |
|---|---|---|
| `Label` | Wrapping | off |
| | Text alignment | `LEFT` |
| | Style | none, so the theme's `label.text` |
| | Spans | none: plain text |
| | Text scale | 1 |
| | Overflow | `VISIBLE` |
| `Button` | Text padding | the theme's `button.padding`, else 8 left and right and 5 top and bottom; `button.padding.icon`, else 2 on every side, with an icon and no caption; none for a link |
| | Icon size, gap to the caption | 16 by 16, 4 |
| | Link | off; a link has no minimum size |
| | Minimum size | 20 by 20 |
| | Cursor | `HAND` |
| | Focus ring | one unit, inside the edge, if the theme has `button.focus` |
| `DropDownButton` | Text padding, icon, minimum size, cursor, focus ring | as `Button` |
| | Arrow | 7 by 4, 4 after the caption, at the right end of the space inside the padding |
| | Popup | none |
| `Image` | Image size and desired size | 16 by 16 |
| | Made with `image()` | draws nothing until given a sprite or a visual |
| | Scaling | `STRETCH` |
| | Tint | `0xFFFFFFFF` |
| `Avatar` | Size and desired size | 24 by 24 |
| | Made with `avatar()` | the background alone, or the theme's placeholder |
| | Initials | the first letter of the first and last words of the name, scaled by whole steps to at most two fifths of the size |
| | Badge | none; a count above 99 shows as `99+`; padding the theme's `badge.padding`, else 2 at the sides and 1 at the top and bottom |
| | Badge visual | a third of the size, at the bottom-right corner |
| | Focusable | no |
| `InfoBar` | Severity, title, message, action | `INFO`, none, none, none |
| | Open, closable, icon shown | yes, yes, yes |
| | Alignment | stretched across its slot, at the top |
| | Padding | the theme's `infobar.padding`, else 6 at the sides and 4 at the top and bottom |
| | Accent strip | 3 wide |
| | Gaps | 4 between the icon, the title, the message, the action and the close button |
| | Close button | 12 wide and a line tall, at the right end; in the tab order |
| | Closed | measures zero |
| `CheckBox` | Box size | caption line height plus 8 (17 with a 9-unit line height) |
| | Gap between box and caption | 4 |
| | Mark inset | a fifth of the box size on each side |
| | Cursor | `HAND` |
| `Slider` | Orientation | `HORIZONTAL` |
| | Desired size | 150 by 20, or 20 by 150 when vertical |
| | Label padding | the theme's `slider.padding`, else none |
| | Range | 0 to 1 |
| | Step | 0 (continuous) |
| | Key step | the step, or a hundredth of the range |
| | Page Up and Page Down | 10 key steps |
| | Thumb | 8 along the track, the slider's full thickness across it |
| | Cursor | `HAND` |
| `TextBox` | Desired width | 120 |
| | Text padding | the theme's `textbox.padding`, else 4 left and right and 5 top and bottom |
| | Minimum size | 20 by 20 |
| | Maximum length | `Integer.MAX_VALUE` code points |
| | Placeholder | none |
| | Caret width | 1 unit |
| | Caret blink | every 500 ms |
| | Cursor | `TEXT` |
| | Filter, read-only, select all on focus | none, off, off |
| | Suggestion provider | none |
| | Suggestions shown before scrolling | 8 |
| | Suggestion rows | one line plus 4, text 4 in from the left; the list at least as wide as the box |
| `PasswordTextBox` | Desired width | 120 |
| | Text padding | the theme's `textbox.padding`, else 4 left and right and 5 top and bottom |
| | Minimum size | 20 by 20 |
| | Mask | `*` |
| | Revealable | on |
| | Reveal shortcut | Alt+F8 |
| | Eye | a square one line tall at the right of the text, 2 units from it; shown while revealable, enabled and not empty |
| | Maximum length | `Integer.MAX_VALUE` code points |
| | Code point filter | none |
| | Placeholder | none |
| | Read-only, select all on focus | off, off |
| | Cursor | `TEXT`, and the arrow over the eye |
| `ListView` | Row height and spacing | 20 and 0 |
| | Selection | none; deselecting by pressing off; activation by double click |
| | Smallest desired width | 40 |
| | Wheel | one row and its spacing a notch |
| `CycleButton` | Label, value label function | empty, `String::valueOf` |
| | Selection | the first item once there are items |
| | Padding, minimum size, cursor | as `Button` |
| | Desired size | label plus widest value, with 8 and 5 of padding |
| `ComboBox` | Rows shown before scrolling | 8 |
| | Selection | the first option once there are options |
| | Label function, key function | `toString`, the option itself |
| | Desired size | widest option plus 4, 4 and 4 of padding and a 7-wide arrow; one line plus 6 |
| | Open list rows | one line plus 4 |
| | Cursor | `HAND` |
| `MessageBox` | Message width | wraps at 280 |
| | Buttons | 64 wide, 4 apart, the first focused |
| | Padding | the theme's `messagebox.padding`, else 4 around the message and buttons |
| `TabBar` | Tab size | text plus 8 on each side; one line plus 7 |
| | Spacing | 0 |
| | Selected | the first tab added |
| `ProgressBar` | Desired size | the wider of 100 and its text plus padding; one line plus padding tall |
| | Padding | the theme's `progress.padding`, else 2 on every side |
| | Range, value | 0 to 1, 0 |
| | Indeterminate | no; when on, a segment a quarter of the width (at least 8) crosses every 1.5 s |
| `BusyIndicator` | Dots | 3, each 4 square, 4 apart |
| | Lit dot | moves first, second, third, second, one step every 300 ms |
| | Desired size | 20 by 4; with a caption, the wider of it and 20, by one line plus 8 |
| `Separator` | Thickness | 1 |
| `RadioButton` | Box, gap and cursor | as `CheckBox` |
| | Mark inset | three tenths of the box size on each side |
| `ToggleSwitch` | Track | a box one caption line tall and two wide, grown by the theme's `toggle.track.padding`, else 4 at the sides and 2 at the top and bottom |
| | Knob | a square in from the track's edges by the theme's `toggle.knob.padding`, else 2 |
| | Gap between track and caption | 4 |
| `Spinner` | Desired width | 60, of which the buttons take 12 |
| | Range, step, decimals, value | 0 to 100, 1, 0, 0 |
| | Page Up and Page Down | 10 steps |
| `TextArea` | Desired size | 160 wide, 4 lines plus padding tall |
| | Text padding | the theme's `textarea.padding`, else `textbox.padding`, else 4 on each side |
| | Wrapping | on |
| | Wheel | 3 lines a notch |
| | Caret after `setText` | at the start, scrolled to the top |
| `TableView` | Header height | 20 |
| | Auto column width | title plus 8, and 11 more when sortable |
| | Text cells | 4 in from the column's edges, cut short with `...` |
| | Sort | the set order; text columns sortable, ignoring case |
| `TableColumn` | Width, cells, sorting | weighted 1, `String.valueOf(item)` as text, not sortable |
| `TreeView` | Indent | 10 a level |
| | Arrow column | 12 |
| | Children | none: every item is a leaf |
| `Menu` | Item height | one line plus 6 |
| | Gutters | the theme's `menu.item.padding` at each side of an item, else 14; `menu.padding` around the items, else 2 |
| | Separator height | 5 |
| | Smallest width | 60, or the width set with `setMinWidth`, or the drop-down button's width while one opened it, whichever is widest |
| `Flyout` | Content | none |
| | Padding | the theme's `flyout.padding`, else 4 on every side |
| | Light dismiss | on |
| | Size | the content's desired size plus the padding, no larger than the host |
| `MenuBar` | Title size | text plus 8 on each side; one line plus 6 |
| `Expander` | Header | one line plus 6 tall |
| | Expanded | no |
| `SplitPane` | Divider | 4 thick |
| | Position | 0.5 |
| | Minimum side when dragging | 20 |
| `NavigationView` | Display mode | `AUTO`: `EXPANDED` from 400 units wide, `COMPACT` from 240, `MINIMAL` below |
| | Pane width | 120 |
| | Column of icons | an item's height plus the pane padding wide, 23 with a 9-unit line |
| | Bar of `MINIMAL` | an item's height plus the pane padding tall, 23 with a 9-unit line |
| | Item | a line plus the theme's `nav.item.padding`, else 6 at the sides and 5 at the top and bottom; 19 tall with a 9-unit line |
| | Icon | a square one line tall; children indented by a line and 4 |
| | Gaps | 4 between an item's icon, text, badge and arrow, and between the toggle and the title |
| | Separator | a one-unit line with 4 above and below |
| | Pane padding | the theme's `nav.pane.padding`, else 2 on every side |
| | Selection, title, content | none, none, none |
| | Toggle shown, activate on click | yes, yes |
| | Alignment | stretched over its slot |
| | Focusable | yes, one tab stop |
| `ScrollBar` | Orientation | `VERTICAL` |
| | Desired size | 6 by 100, or 100 by 6 across; 2 thick when slim |
| | Range, visible amount, value | 0 to 0, 0, 0: nothing to scroll |
| | Scroll rate | 10 a notch or arrow key |
| | Page | the visible amount less one scroll rate, at least one rate |
| | Thumb | at least 32 long (16 when slim), at most the track less 8 |
| | Focusable | no |
| | Cursor | `HAND` |
| `GridView` | Columns, cell size, spacing | 9, 18, 0 |
| | Content inset | the theme's `grid.cell.padding`, else 1 |
| | Selectable | no |
| | Activation | a left double click |
| `ColorPicker` | Color | `0xFFFFFFFF`, opaque white |
| | Alpha strip | hidden |
| | Square, strip width, spacing | 96 square, 10, 4 |
| | Checkerboard cells | 4, white and `0xFFC8C8C8` |
| | Cursor | `CROSSHAIR`, and `TEXT` over the hex field |
| `CalendarView` | Date | none |
| | Month in view | today's |
| | Zoom | `MONTH` |
| | Range, filter, mark filter | none, every day, no day |
| | First day of the week | the language's |
| | Days of other months | shown |
| | Padding, cell padding | the theme's `calendar.padding`, else 4; `calendar.cell.padding`, else 3 at the sides and 2 at the top and bottom |
| | Cells | a line plus the cell padding tall, at least as wide as tall; seven across and eight down |
| | Today | read from the wall clock at most once a minute |
| | Focusable | yes |
| `DatePicker`, `TimePicker`, `DateTimePicker` | Value | none |
| | Placeholder | "Pick a date", "Pick a time" or "Pick a date and time", from `strata.date.placeholder`, `strata.time.placeholder` and `strata.datetime.placeholder` |
| | Clearable | yes |
| | Text padding | the theme's `picker.padding`, else `textbox.padding`, else 4 left and right and 5 top and bottom |
| | Button | 12 wide |
| | Width | the widest text the parts can show, or the placeholder if wider, plus the padding and the button |
| | Range, filter, first day of the week | none, every day, the language's |
| | Clock format, seconds, minute step | the language's, hidden, 1 |
| | Default time (`DateTimePicker`) | midnight |
| | Time columns | rows a line plus 4 tall, at most 7 showing |
| | Focusable | yes |

## Input

| Rule | Value |
|---|---|
| Multi-click time | 500 ms between presses, inclusive |
| Multi-click distance | 4 units on each axis |
| Most clicks in a run | 3, then back to 1 |
| Drag threshold | more than 4 units on either axis |
| Synthesized key repeat delay | 400 ms |
| Synthesized key repeat interval | 33 ms |
| Keys the host repeats itself | Left, Right, Up, Down, Backspace, Delete, Home, End, Page Up, Page Down |
| Tooltip delay | 500 ms (`Tooltip.DEFAULT_DELAY_MILLIS`), none when moving from one tooltip to another |
| Tooltip position | 12 right of and 12 above the pointer, flipped left at the right edge, kept inside the host |
| Tooltip padding | the theme's `tooltip.padding`, else 4; 1 between lines |
| Toast time | 3000 ms for text toasts |
| Toast position | 4 in from the top and right edges, stacked 4 apart |
| Toast padding | the theme's `toast.padding`, else 6; text wraps at a third of the host's width |

The host synthesizes repeats only for engines whose platform reports `sendsKeyRepeat == false`. Swing sends its own.

## Text

| Setting | Value |
|---|---|
| `TextStyle.DEFAULT` | opaque white, no decoration, no shadow |
| Swing default font | logical Dialog, 9 units |

## Adapters

| Setting | Value |
|---|---|
| `StrataPanel` preferred size | 960 by 600 pixels |
| `StrataPanel` background | `0xFF1E1F22` |
| `StrataPanel` repaint rate | about 60 per second (every 16 ms) |
| Swing missing sprite | solid magenta |
