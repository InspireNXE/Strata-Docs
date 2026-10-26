# Defaults and timings

Every number Strata picks for you. Where a theme [metric](../guides/theming.md#metrics) can change a number, the row names its key.

## Every component

| Setting | Default |
|---|---|
| Visible, enabled | yes |
| Focusable, tabbable | no, except the controls that are `Focusable`: buttons, toggle buttons, drop-down buttons, cycle buttons, checkboxes, radio buttons (one Tab stop for a whole group), toggle switches, sliders, text boxes, password boxes, text areas, spinners (one stop, the field), lists, tables and trees (one stop, the inner list), grid views, combo boxes, tab bars, expanders, split panes (the divider), color pickers, navigation views, calendar views and the date and time pickers. Scroll bars and scroll panels are `Focusable` but start with focus off. Menus and flyouts are focusable but not tabbable. Tab index 0. |
| Margin | `Insets.NONE` |
| Minimum size | 0 by 0 |
| Maximum size | `Float.POSITIVE_INFINITY` both ways |
| Fixed size | unset (`Float.NaN`) |
| Alignment | `LEFT`, `TOP`, except windows and message boxes (centered), separators (stretched along their line), info bars (stretched across, at the top), and spacers and navigation views (stretched both ways) |
| Z index | 0 |
| Opacity | 1 |
| Hit testing | on: the pointer can hit the component unless `setHitTestVisible(false)` |
| Focus shown | when focus came from the keyboard, or from code while the last press was a key's or nothing was pressed yet; a key pressed while focused shows it and a pointer press hides it |
| Id | none; Strata never checks it |
| Flow direction | the parent's; the top of a screen follows the translator's language, and overlays follow the screen |
| Context menu | none; text boxes, text areas and password boxes start with one |
| Bounds before the first layout | `Rect.ZERO` |

## Containers

| Setting | Default |
|---|---|
| Padding | `Insets.NONE` |
| Clips children | yes |
| Drawing order | the order children were added, until `raise`, `lower` or a z index changes it |
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
| `WrapPanel` orientation | `HORIZONTAL` from `wrapPanel()`: children fill rows |
| `WrapPanel` spacing, line spacing | 0, 0 |
| `WrapPanel` line alignment | `LEFT`, which is the top in columns |
| `Container` text style patch | none |
| `ScrollPanel` policies | horizontal `NEVER`, vertical `AUTO` |
| `ScrollPanel` scroll rate | 10 units a wheel notch or arrow key |
| `ScrollPanel` scroll bar | 6 wide, 2 from the viewport; thumb at least 32 long and at most the track less 8 |
| `Window` padding | the theme's `window.padding`, else 3 on every side |
| `Window` title bar | one line plus 3 above and below |
| `Window` close button | 12 wide, at the right end of the title bar |
| `Window` closable, movable | yes, yes |
| `Window` maximizable, collapsible | no, no |
| `Window` title buttons | `Window.BUTTON_WIDTH`, 12 wide each, one unit apart, left of the close button |
| `Window` active | while focus is within it; with focus in none of its sibling windows, the one pressed last, else the frontmost |
| `Window` resizable, edge resizable | no, no |
| `Window` raises on press | yes |
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
| | Mnemonic parsing | off |
| `Button` | Text padding | the theme's `button.padding`, else 8 left and right and 5 top and bottom; `button.padding.icon`, else 2 on every side, with an icon and no caption; none for a link |
| | Icon size, gap to the caption | 16 by 16, 4 |
| | Link | off; a link has no minimum size |
| | Minimum size | 20 wide; asks to be at least 20 tall, though a smaller height set on it applies |
| | Caption too wide | cut at the theme's `button.clip`, else the padding's left and right; scrolls when the theme's `button.text.scroll` is above 0 |
| | Acts on | the click, or the press when the platform's `Conventions.activateOnPress` is on |
| | Cursor | `HAND` |
| | Focus ring | one unit, inside the edge, if the theme has `button.focus`, while focus is shown |
| | Caption alignment | `CENTER`; toggle and drop-down buttons too |
| | Icon placement | `START`, before the caption |
| | Default, cancel | no, no |
| | Mnemonic | the letter after the first single underscore of the caption, with Alt |
| | Command | none |
| `ToggleButton` | Selected | off |
| | Caption | empty |
| | Padding, icon, icon placement, minimum size, caption too wide, acts on, cursor, focus ring | as `Button` |
| | Faces while on | the theme's `button.selected`, `button.selected.hovered` and `button.selected.disabled` |
| `DropDownButton` | Text padding, icon, minimum size, caption too wide, cursor, focus ring | as `Button` |
| | Arrow | 7 by 4, 4 after the caption, at the right end of the space inside the padding |
| | Popup | none |
| `Image` | Image size and desired size | the pixel buffer's size, one pixel to a unit, or the sprite's size where the engine knows it, or a visual's natural size; 16 by 16 for an unknown sprite or nothing; a size set by hand stays until a visual is set |
| | Made with `image()` | draws nothing until given a sprite or a visual |
| | Sampling | `NEAREST` |
| | Scaling | `STRETCH`; `FIT`, `COVER` and `NONE` are the others |
| | Tint | `0xFFFFFFFF` |
| | Pixels | none |
| `Avatar` | Size and desired size | 24 by 24 |
| | Made with `avatar()` | the background alone, or the theme's placeholder |
| | Initials | the first letter of the first and last words of the name, scaled by whole steps to at most two fifths of the size |
| | Badge | none; a count above 99 shows as `99+`; padding the theme's `badge.padding`, else 2 at the sides and 1 at the top and bottom |
| | Badge visual | a third of the size, at the bottom-right corner |
| | Corner radius | the theme's `avatar.radius`, else 0: square |
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
| | Acts on | the click, or the press when the platform's `Conventions.activateOnPress` is on; radio buttons and toggle switches too |
| `Slider` | Orientation | `HORIZONTAL` |
| | Desired size | 150 by 20, or 20 by 150 when vertical |
| | Label padding | the theme's `slider.padding`, else none; a label too wide is cut there, or scrolls when the theme's `slider.text.scroll` is above 0 |
| | Range | 0 to 1 |
| | Step | 0 (continuous) |
| | Key step | the step; with no step, the theme's `slider.key.travel` units of thumb travel when above 0, else a hundredth of the range |
| | Page Up and Page Down | 10 key steps |
| | Thumb | 8 along the track, the slider's full thickness across it |
| | Ticks | none; when set, major ticks 4 units long and minor ticks 2, left out where closer than 3 units |
| | Snap to ticks | off |
| | Adjusting | false, until a left press; true until the release |
| | Cursor | `HAND` |
| `TextBox` | Desired width | 120 |
| | Text padding | the theme's `textbox.padding`, else 4 left and right and 5 top and bottom |
| | Minimum size | 20 wide; asks to be at least 20 tall, though a smaller height set on it applies |
| | Maximum length | `Integer.MAX_VALUE` code points |
| | Placeholder | none |
| | Caret width | 1 unit |
| | Caret blink | the theme's `textbox.caret.blink`, else 500 ms shown and 500 ms hidden |
| | Cursor | `TEXT` |
| | Filter, read-only, select all on focus | none, off, off |
| | Suggestion provider | none |
| | Suggestions shown before scrolling | 8 |
| | Suggestion rows | one line plus 4, text 4 in from the left; the list at least as wide as the box |
| | Text alignment | `LEFT` |
| | Drop-down | off |
| | Undo history | 100 edits (`historyLimit`); typing merges into one entry until a space or a pause of 1000 ms |
| | Validation | valid, no validator |
| | Context menu | Undo, Redo, Cut, Copy, Paste, Delete and Select all |
| | Dragging | the selection can be dragged out, allowing a move and a copy, or only a copy when read-only |
| `PasswordTextBox` | Desired width | 120 |
| | Text padding | the theme's `textbox.padding`, else 4 left and right and 5 top and bottom |
| | Minimum size | 20 wide; asks to be at least 20 tall, though a smaller height set on it applies |
| | Mask | `*` |
| | Revealable | on |
| | Reveal shortcut | Alt+F8 |
| | Eye | a square one line tall at the right of the text, 2 units from it; shown while revealable, enabled and not empty |
| | Maximum length | `Integer.MAX_VALUE` code points |
| | Code point filter | none |
| | Placeholder | none |
| | Read-only, select all on focus | off, off |
| | Cursor | `TEXT`, and the arrow over the eye |
| | Text alignment | `LEFT` |
| | Undo history | none; Ctrl+Z is left to the engine |
| | Context menu | Paste, Delete and Select all |
| | Dragging | never a source or a target |
| `ListView` | Row height and spacing | 20 and 0 |
| | Selection | none, in `SINGLE` mode; pressing the selected row does not deselect it; a double click activates |
| | Smallest desired width | 40 |
| | Wheel | the theme's `list.scroll.rows`, else one, rows and their spacing a notch |
| | Row width | the theme's `list.row.width`, else the list's width; a narrower column is centered with its scroll bar `list.bar.gap`, else 2, to its right |
| | Type-ahead | on, by the item's appearance name; the prefix lasts the theme's `list.typeahead.timeout`, else 1000 ms, and a space never starts it |
| | Drag transfer | none: rows are not dragged |
| | Drag scrolling | on |
| | Cell editor | none |
| | Row backgrounds | drawn; the cells of selected rows and of the row under the pointer have the `selected` and `row_hovered` style states |
| | Drop line | 2 units high, in the theme's `list.drop` |
| `CycleButton` | Label, value label function | empty, the value's appearance name (`String.valueOf` for a value with no appearance) |
| | Selection | the first item once there are items |
| | Padding, minimum size, caption too wide, acts on, cursor | as `Button` |
| | Desired size | label plus widest value, with 8 and 5 of padding |
| `ComboBox` | Rows shown before scrolling | 8 |
| | Selection | the first option once there are options, but only for a box that never had a selection and that code has not cleared |
| | Label function, key function | the option's appearance name (`String.valueOf` for an option with no appearance), the option itself |
| | Padding | the theme's `combobox.padding`, else 4 at the sides and 3 at the top and bottom |
| | Desired size | the widest option plus the padding, a 4-unit gap and a 7-wide arrow; one line plus the padding tall |
| | Open list rows | one line plus 4 |
| | Placeholder | empty |
| | Type-ahead | on, by the label function |
| | Cursor | `HAND` |
| `MessageBox` | Message width | wraps at the theme's `messagebox.message.width`, else 280; lines start at the left unless `messagebox.message.center` is on |
| | Buttons | the theme's `messagebox.button.width`, else 64 wide, at the right, `messagebox.button.gap`, else 4 apart, the first focused |
| | Space above the buttons | the theme's `messagebox.spacing`, else 8 |
| | Close button | shown unless the theme's `messagebox.close` is 0 |
| | Padding | the theme's `messagebox.padding`, else 4 around the message and buttons |
| | Default result | the first result of the button set |
| | Severity, content | none, none |
| | Icon | a square of `MessageBox.ICON_SIZE`, 24 units, 8 units before the message; shown only with a severity and a theme icon |
| `TabBar` | Tab size | text plus 8 on each side; one line plus 7 |
| | Spacing | 0 |
| | Selected | the first tab added |
| | Text too wide | cut at the theme's `tab.clip`, else the padding's left and right; scrolls when the theme's `tab.text.scroll` is above 0 |
| | Acts on | the click, or the press when the platform's `Conventions.activateOnPress` is on |
| `Tab` | Enabled, tooltip | yes, none |
| `ProgressBar` | Desired size | the wider of 100 and its text plus padding; one line plus padding tall |
| | Padding | the theme's `progress.padding`, else 2 on every side |
| | Range, value | 0 to 1, 0 |
| | Orientation | `HORIZONTAL`; a vertical bar asks for 100 units of height |
| | Indeterminate | no; when on, a segment a quarter of the width (at least 8) crosses every 1.5 s |
| `BusyIndicator` | Dots | 3, each 4 square, 4 apart |
| | Lit dot | moves first, second, third, second, one step every 300 ms |
| | Desired size | 20 by 4; with a caption, the wider of it and 20, by one line plus 8 |
| `Separator` | Thickness | 1 |
| `RadioButton` | Box, gap and cursor | as `CheckBox` |
| | Mark inset | three tenths of the box size on each side |
| `RadioGroup` | Buttons, selection | none, none |
| | Tab stop | the selected button, else the first |
| `ToggleSwitch` | Track | a box one caption line tall and two wide, grown by the theme's `toggle.track.padding`, else 4 at the sides and 2 at the top and bottom |
| | Knob | a square in from the track's edges by the theme's `toggle.knob.padding`, else 2 |
| | Gap between track and caption | 4 |
| `Spinner` | Desired width | 60, of which the buttons take 12 |
| | Range, step, decimals, value | 0 to 100, 1, 0, 0 |
| | Page Up and Page Down | 10 steps |
| | Held step button | repeats after 400 ms, then every 33 ms |
| | Trackpad scrolling | one step for each 10 units of `PRECISE` scroll; a wheel notch is one step |
| | Text alignment | `LEFT` |
| `TextArea` | Desired size | 160 wide, 4 lines plus padding tall |
| | Text padding | the theme's `textarea.padding`, else `textbox.padding`, else 4 on each side |
| | Wrapping | on |
| | Wheel | 3 lines a notch |
| | Caret after `setText` | at the end, scrolled to the top |
| | Accepts Tab | no |
| | Text alignment | `LEFT` |
| | Undo history | 100 edits, as `TextBox` |
| | Placeholder | none |
| `TableView` | Header height | 20 |
| | Auto column width | title plus 8, and 11 more when sortable |
| | Text cells | 4 in from the column's edges, cut short with `...` |
| | Sort | the set order; text columns sortable, ignoring case |
| | Horizontal policy, scroll X | `NEVER`, 0 |
| | Sort order | empty |
| | Type-ahead | on, by the first column whose cells are text |
| `TableColumn` | Width, cells, sorting | weighted 1, the item's appearance name as text (`String.valueOf` for an item with no appearance), not sortable |
| | Width limits | 0 to infinite |
| | Resizable, movable, shown | yes, yes, yes |
| | Cell editor | none |
| `TreeView` | Indent | 10 a level |
| | Arrow column | 12 |
| | Children | none: every item is a leaf |
| | Has-children test | none: the children function decides |
| | Cell editor | none |
| `Menu` | Item height | one line plus 6 |
| | Gutters | the theme's `menu.item.padding` at each side of an item, else 14; `menu.padding` around the items, else 2 |
| | Separator height | 5 |
| | Smallest width | 60, or the width set with `setMinWidth`, or the drop-down button's width while one opened it, whichever is widest |
| `MenuItem` | Enabled, shortcut, action | yes, none, does nothing |
| `CheckMenuItem` | Checked, enabled, action | no, yes, does nothing |
| `RadioMenuItem` | Selected, group, enabled, action | no, none (items with no group form one group per menu), yes, does nothing |
| `SubmenuItem` | Menu, enabled | none, yes |
| `Flyout` | Content | none |
| | Padding | the theme's `flyout.padding`, else 4 on every side |
| | Light dismiss | on |
| | Size | the content's desired size plus the padding, no larger than the host |
| `MenuBar` | Title size | text plus 8 on each side; one line plus 6 |
| `Expander` | Header | one line plus 6 tall |
| | Expanded | no |
| | Header component | none: the title |
| `SplitPane` | Divider | 4 thick |
| | Position | 0.5 |
| | Minimum side when dragging | 20 |
| | Resize policy | `PROPORTIONAL` |
| | Collapsible sides, collapsed | neither, none |
| | Key step | `SplitPane.KEY_STEP`, 10 units; 1 with Shift |
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
| `NavigationItem` | Enabled, selectable, expanded | yes, yes, no |
| | Icon, badge, page, children, tooltip | none, 0, none, none, none |
| | Icon size | 16 by 16 |
| `ScrollBar` | Orientation | `VERTICAL` |
| | Desired size | 6 by 100, or 100 by 6 across; 2 thick when slim |
| | Range, visible amount, value | 0 to 0, 0, 0: nothing to scroll |
| | Scroll rate | 10 a notch or arrow key |
| | Arrows | hidden; shown, they are as long as the bar is thick and repeat while held |
| | Held track | pages again after 400 ms, then every 33 ms, until the thumb reaches the pointer |
| | Page | the visible amount less one scroll rate, at least one rate |
| | Thumb | at least 32 long (16 when slim), at most the track less 8 |
| | Focusable | no |
| | Cursor | `HAND` |
| `GridView` | Columns, cell size, spacing | 9, 18, 0 |
| | Content inset | the theme's `grid.cell.padding`, else 1 |
| | Selectable | no |
| | Activation | a left double click, or Enter or Space on the cursor cell |
| | Selection mode | `SINGLE` |
| | Scroll Y | 0; with a limited height the grid builds only the rows in view and draws its own bar |
| | Focusable | yes, one tab stop, with the cursor on the selected cell or the first |
| | Cell editor | none |
| `ColorPicker` | Color | `0xFFFFFFFF`, opaque white |
| | Alpha strip | hidden |
| | Square, strip width, spacing | 96 square, 10, 4 |
| | Checkerboard cells | 4, white and `0xFFC8C8C8` |
| | Cursor | `CROSSHAIR`, and `TEXT` over the hex field |
| | Focusable | yes, a tab stop before the hex field |
| | Key steps | 1 percent of saturation and brightness, 1 degree of hue; 10 with Shift |
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
| | Limits | 1 January 0001 to 31 December 9999; dates and range ends outside move to the nearer limit |
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
| Hold repeat of buttons and tracks | 400 ms, then every 33 ms, by the same timing |
| Long press | 500 ms of a first touch held within the drag threshold opens the context menu |
| Two-finger scroll | one `PRECISE` notch for each 10 units the midpoint of the two touches moves |
| Drag image | 8 units right of and below the pointer |
| Edge scrolling during a drag | within 16 units of a viewport's edge, by a line times how deep into the zone the pointer is (a scroll panel's scroll rate, a list's row step), from an eighth of a line to a whole one, each frame |
| Transfers a drag allows | `MOVE` and `COPY` from lists and text boxes, `COPY` only from a read-only box |
| Keys the host repeats itself | Left, Right, Up, Down, Backspace, Delete, Home, End, Page Up, Page Down |
| Tooltip delay | the adapter's, else the theme's `tooltip.delay`, else 500 ms (`Tooltip.DEFAULT_DELAY_MILLIS`); none when moving from one tooltip to another |
| Tooltip position | the theme's `tooltip.offset.x` and `tooltip.offset.y`, else 12 right of and 12 above the pointer floored to a whole unit, flipped left at the right edge, kept inside the host; just below the component when the theme's `tooltip.below` is on, with `tooltip.below.overlap`, else 3, and `tooltip.below.gap`, else 5 |
| Tooltip padding | the theme's `tooltip.padding`, else 4; 1 between lines |
| Toast time | 3000 ms for text toasts |
| Toast position | 4 in from the top and right edges, stacked 4 apart |
| Toast padding | the theme's `toast.padding`, else 6; text wraps at a third of the host's width |
| Toast layout | first line in `toast.title` when the theme sets it; lines `toast.line.gap` apart, else 0; at least `toast.width.minimum` wide, else no minimum; `toast.margin` from the top and right edges and between toasts, else `Overlays.TOAST_MARGIN` |
| Tab widths | equal; whole units when the room is whole, the spare units one each to the first tabs |

The host synthesizes repeats only for engines whose platform reports `sendsKeyRepeat == false`. Swing sends its own.

## Threading and frames

| Setting | Value |
|---|---|
| UI thread of a platform with no executor | tasks run at the start of the next flush, layout or render of any host of the platform |
| UI thread of a platform with no check | the first thread that drives a host, for good |
| A task that throws | logged as an error; the next one runs. `submit` fails its future instead. |
| `execute` on the UI thread | queues the task; it runs after the current dispatch |
| Cause frame around a task | none, so what it changes reports `Origin.CODE` unless it pushes a frame |
| Opening a screen from another thread | a handle at once; its calls wait for the screen, and `factory()` throws until it has opened |
| A host needs a frame | after input, a layout or binding change, a task handed over, a report, an overlay opening or closing, a theme, size or density change, `repaint()` and every running animation |
| Frame requester | called when a host turns from clean to dirty, possibly on any thread, and not again before the next render |
| Frame rate | 30 frames a second (`ScreenHost.DEFAULT_FRAME_RATE`), from 1 to 1000 with `setFrameRate` |
| Earliest next frame | one interval of the frame rate after the last render began |
| Sliding caption, animation, anything else that moves continuously | asks for the next frame the rate allows, one interval later |
| Caret, busy indicator, indeterminate progress bar | ask for their next change as they paint |
| Calendar's today | read again a minute after the last reading |

## Animation

| Setting | Value |
|---|---|
| Built-in motion | off: `toggle.animation`, `expander.animation`, `nav.animation`, `scroll.smooth`, `toast.fade` and `progress.animation` are 0 milliseconds |
| `Easing.LINEAR` | the identity |
| `Easing.EASE_IN` | cubic Bezier with control points (0.42, 0) and (1, 1) |
| `Easing.EASE_OUT` | cubic Bezier with control points (0, 0) and (0.58, 1) |
| `Easing.EASE_IN_OUT` | cubic Bezier with control points (0.42, 0) and (0.58, 1) |
| Easing of built-in motion | `EASE_OUT`; a toast's fade out uses `EASE_IN` |
| Next step of an animation | one interval of the host's frame rate after each step |
| An animation of 0 milliseconds | one step on the next frame |
| Cause of a step | the `Animation`, with `Origin.CODE`, so requests do not fire |
| Component opacity | 1 |
| Animations when a screen closes | stop where they are |

## Drawing

| Setting | Value |
|---|---|
| Density, `Environment.pixelsPerUnit()` | 1 until the adapter calls `setPixelsPerUnit`; the painter multiplies it by every `scaled` in progress |
| `Stroke.of(width)` | butt caps and miter joins |
| Radius of a rounded rectangle, a round clip or a shadow | limited to half the shorter side; below 0 counts as 0 |
| Fill rule of a shape | nonzero |
| Smoothing | on where the engine can; `smoothed(false, ...)` asks for hard edges |
| Rounded paint without a radius | a solid color |
| `PixelBuffer` | transparent black, version 0, at most 268,435,456 pixels |
| Shapes on an engine that draws none | rows one device pixel high (`1 / pixelsPerUnit`), scanned in at most 2048 rows at a time, from fills that merge with the row above when they match; gradients at an angle in at most 1024 columns; shadows in up to 6 rings; miter joins as bevels; no anti-aliasing; round clips square |
| `Capabilities.drawsShapes`, `shapesRightToLeft` | false unless the constructor says so; the two- and three-argument constructors give false for both |
| `Canvas.pushOpacity`, `popOpacity` | do nothing, so part-opaque components draw opaque |
| `Canvas.pushRoundClip` | `pushClip` |
| `Canvas.pixels` | the unknown visual's placeholder |
| `TextMetrics.ascent` | 0.8 of the line height |
| Cursor kinds a canvas shows | the eight older kinds (`showsCursor`); a kind it lacks falls back: the diagonals to `RESIZE_ALL`, `GRAB` and `GRABBING` to `HAND`, `NO_DROP` to `NOT_ALLOWED`, the others to `ARROW` |
| Input device of a form with no `InputContext` | `MOUSE` for pointer input, `KEYBOARD` for keys; pointer 0, pressure 1 |

## Screens

| Setting | Value |
|---|---|
| `Screen.title()` | empty text |
| `Screen.closeRequested` | true: the screen agrees |
| `ScreenHost.title()` | empty with no screen; the screen's title, or what `setTitle` set, while it is open |
| `ScreenHost.requestClose()` | true with no screen open |
| `ScreenHost.preferredSize`, `ScreenStack.preferredSize` | `Size.ZERO` with no screen |
| Escape closing a screen | not in core: an adapter whose engine does it calls `requestClose()` |

## Validation, converters and text editing

| Setting | Value |
|---|---|
| Validation of a control | `Validation.VALID`, with no validator |
| Outline of an error, of a warning | one unit inside the bounds, in the theme's `validation.error`, else solid `0xFFE04848`, and `validation.warning`, else solid `0xFFE0A030` |
| Number converters | written in the locale's digits without grouping; read with grouping allowed, spaces around the number ignored; rounded half up; `decimals` from 0 to 15 |
| Edit history | 100 edits per field; 0 keeps none |
| Typing merges into the entry before | while less than 1000 ms passed and no whitespace closed it |
| `setText`, from code or a binding | clears the history |
| Accepts Tab (text area) | no |
| Text alignment (text box, password box, text area, spinner) | `LEFT`; `STRETCH` counts as `LEFT` |
| Field text | grapheme clusters stay whole in moves, deletes, wrapping, ellipsis and `maxLength`, which counts code points |

## Internationalization

| Setting | Value |
|---|---|
| Flow direction | the parent's; the top of a screen follows `FlowDirection.of` the translator's locale, worked out again when the language changes |
| Plural categories | the CLDR cardinal rules of the translator's locale for whole numbers; a language Strata has no rule for uses English's |
| Plural text | `key.<category>`, else `key.other`, else `key.other` shown as itself with one warning |
| Numbers a spinner writes | in the locale's digits, decimal separator and minus sign, without grouping |
| Numbers a spinner reads | ASCII and the locale's digits, the locale's separator or a period, the locale's minus sign or a hyphen |

## The test kit

| Setting | Value |
|---|---|
| `TesterOptions.DEFAULT` | 800 by 600 units, `Themes.flat()`, `Translator.NONE`, and the wall clock at noon UTC on 15 January 2026 |
| Text | 6 units per code point, 7 in a monospaced style, 9 per line, at a default size of 9; other sizes in proportion |
| Clock | stands still until `advance` |
| Repeating tasks | run as `advance` passes them, once for each interval, on the test's thread |
| Key repeat | none: the platform reports that the engine sends its own |
| Pointer modifiers | none, unless `press` names some |
| `settle()` | gives up after a minute of animation |

## Binding

| Setting | Value |
|---|---|
| When a state change reaches its controls | at the start of the next layout or render, or on `flush()` |
| Rounds before a binding cycle is reported | 100; the error is logged through `System.Logger` and the remaining updates are dropped |
| Order a component sets its bound properties on attach | the order they were first bound |

## Text

| Setting | Value |
|---|---|
| `TextStyle.DEFAULT` | opaque white, no decoration, no shadow, the platform's default font (`font` null) at its default size (`size` 0) |
| A font the platform lacks | its default font, at the style's size |
| A theme file's text entry | the fields it leaves out keep the style a lower layer gave the key, else `TextStyle.DEFAULT` |
| `Text.sprite(id)` | a square as wide and tall as the line height of the style where it lands |
| `Text.visual(visual)` | the visual's natural size |
| Pictures in text | untinted |
| Translation with no translator and no English | the key itself, with one warning per key |
| Sprite paints from the factories | untinted, opaque white |
| Paint tint in a theme file | `#FFFFFFFF` |
| Swing default font | logical Dialog, 9 units, with logical Monospaced at the same size for monospaced styles |

## Adapters

| Setting | Value |
|---|---|
| `StrataPanel` preferred size | what the top screen asks for, at the panel's scale plus the insets; 960 by 600 pixels while no screen is open; a size set with `setPreferredSize` wins |
| `StrataPanel` background | `0xFF1E1F22` |
| Swing repeating tasks | a `javax.swing.Timer` each, on the event thread, coalescing missed runs; none in a snapshot |
| Vulkan repeating tasks | a `VulkanScheduler`, run as the view renders, dropping missed runs |
| Minecraft repeating tasks | Sponge's client scheduler where Sponge is present, else the client tick; intervals round up to 50 ms ticks |
| `StrataPanel` repainting | when its screens ask for a frame, and at the next timed change through a one-shot timer, so an idle panel with a blinking caret paints twice a second and one with nothing timed does not paint |
| Swing missing sprite | solid magenta |
| Swing visual from another adapter | solid magenta |
| Swing sprite path | `assets/<namespace>/sprites/<path>.png` on the adapter's class loader |
| Swing theme file path | `assets/<namespace>/themes/<path>.json` on the class path |
| Swing default theme | `strata:flat` |
| Swing UI thread | the event dispatch thread |
| Swing density | the panel's scale times the display's scale, followed to another display; denser sprites are `@2x.png` from 2 device pixels per unit and `@1.5x.png` from 1.5, falling back to the plain image |
| Swing cursors | native for the arrow, text, hand, crosshair, resize kinds and wait; drawn in code for not allowed, progress, grab, grabbing, copy, no drop and help, which do not exist headless and fall back there |
| Swing file dialogs | AWT's file dialog, and a file chooser for folders off macOS; none headless |
| Swing translations | none: Strata's keys in English, other keys as themselves |
| `SwingPlatform` capabilities | sends key repeats, has a monospaced font, stores no slicing with images, draws shapes, and shapes right to left text |
| `SwingPlatform` conventions | controls act on the click, mnemonics are on, and the shortcut modifier is Command where the menu shortcut mask is Meta (macOS) and Control elsewhere |
| `Conventions.DEFAULT` | controls act on release, the shortcut modifier is Control, mnemonics are on |
| `BitmapFont` tinted sheets kept | 256 colors |
| `SwingSnapshot` text and sprites | `SwingTextMetrics.defaults()`, images from the adapter's class loader, tooltips with no delay |
