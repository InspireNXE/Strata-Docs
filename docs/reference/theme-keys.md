# Theme keys

Every style key the built-in controls read. A theme lacking a required key cannot show a tree that uses the control; see [Themes](../guides/theming.md#required-and-optional-keys). On a theme resolved from a `ThemeRef`, `theme.layer(key)` names the [layer](../guides/theming.md#layers) that set a key. [Theme files](../guides/theming.md#theme-files) use the same keys, each under `paints`, `text`, `insets` or `metrics` by its kind, and so do `ThemeOverrides` and `ThemeRef.override`. A [metric](../guides/theming.md#metrics) key's fallback is the value the control uses when the theme sets none, and `Themes.flat()` sets no metrics.

## Button

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `button.normal` | `Button.STYLE_NORMAL` | paint | yes | |
| `button.hovered` | `Button.STYLE_HOVERED` | paint | yes | |
| `button.pressed` | `Button.STYLE_PRESSED` | paint | yes | |
| `button.disabled` | `Button.STYLE_DISABLED` | paint | yes | |
| `button.focus` | `Button.STYLE_FOCUS` | paint | no | No focus ring |
| `button.text` | `Button.STYLE_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `button.text.disabled` | `Button.STYLE_TEXT_DISABLED` | text style | no | `button.text` |
| `button.link.text` | `Button.STYLE_LINK_TEXT` | text style | no | `button.text` |
| `button.link.text.hovered` | `Button.STYLE_LINK_TEXT_HOVERED` | text style | no | `button.link.text` underlined |
| `button.link.text.disabled` | `Button.STYLE_LINK_TEXT_DISABLED` | text style | no | `button.text.disabled` |
| `button.padding` | `Button.STYLE_PADDING` | insets | no | 8, 5, 8 and 5 |
| `button.padding.icon` | `Button.STYLE_PADDING_ICON` | insets | no | 2 on every side |
| `button.clip` | `Button.STYLE_CLIP` | insets | no | The padding's left and right, none at the top and bottom |
| `button.text.scroll` | `Button.STYLE_TEXT_SCROLL` | metric | no | 0, which cuts a caption too wide instead of scrolling it |
| `button.text.scroll.minimum` | `Button.STYLE_TEXT_SCROLL_MINIMUM` | metric | no | 0 |

The `button.link.*` keys apply to a button made a link, which reads no face keys. A `CycleButton` reads the same keys as a button.

A caption too wide for its button is cut at the clip area, `button.clip` in from the edges. When `button.text.scroll` is above 0, it slides between its two ends instead, and one sweep there and back takes that many milliseconds per unit of overflow, and at least `button.text.scroll.minimum`. A caption that fits is centered and kept inside the clip area.

## DropDownButton

A drop-down button reads the `Button` keys above except the link keys, the clip and scroll keys included, with the same requirements, and one of its own.

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `dropdown.arrow` | `DropDownButton.STYLE_ARROW` | paint | no | The arrow in the `button.text` color; a disabled button always uses the `button.text.disabled` color |

## Label

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `label.text` | `Label.STYLE_TEXT` | text style | no | `TextStyle.DEFAULT` |

A style set with `Label.setStyle` overrides the key.

## CheckBox

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `checkbox.normal` | `CheckBox.STYLE_NORMAL` | paint | yes | |
| `checkbox.hovered` | `CheckBox.STYLE_HOVERED` | paint | yes | |
| `checkbox.checked` | `CheckBox.STYLE_CHECKED` | paint | yes | |
| `checkbox.checked.hovered` | `CheckBox.STYLE_CHECKED_HOVERED` | paint | yes | |
| `checkbox.disabled` | `CheckBox.STYLE_DISABLED` | paint | no | `checkbox.normal` |
| `checkbox.checked.disabled` | `CheckBox.STYLE_CHECKED_DISABLED` | paint | no | `checkbox.checked` |
| `checkbox.mark` | `CheckBox.STYLE_MARK` | paint | no | No mark |
| `checkbox.mark.disabled` | `CheckBox.STYLE_MARK_DISABLED` | paint | no | `checkbox.mark` |
| `checkbox.mark.mixed` | `CheckBox.STYLE_MARK_MIXED` | paint | no | `checkbox.mark` |
| `checkbox.mark.mixed.disabled` | `CheckBox.STYLE_MARK_MIXED_DISABLED` | paint | no | `checkbox.mark.disabled`, then `checkbox.mark.mixed` |
| `checkbox.focus` | `CheckBox.STYLE_FOCUS` | paint | no | No focus ring |
| `checkbox.text` | `CheckBox.STYLE_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `checkbox.text.disabled` | `CheckBox.STYLE_TEXT_DISABLED` | text style | no | `checkbox.text` |

## Slider

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `slider.track` | `Slider.STYLE_TRACK` | paint | yes | |
| `slider.track.focused` | `Slider.STYLE_TRACK_FOCUSED` | paint | no | `slider.track` |
| `slider.track.disabled` | `Slider.STYLE_TRACK_DISABLED` | paint | no | `slider.track` |
| `slider.thumb` | `Slider.STYLE_THUMB` | paint | yes | |
| `slider.thumb.hovered` | `Slider.STYLE_THUMB_HOVERED` | paint | yes | |
| `slider.thumb.disabled` | `Slider.STYLE_THUMB_DISABLED` | paint | no | `slider.thumb` |
| `slider.text` | `Slider.STYLE_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `slider.text.disabled` | `Slider.STYLE_TEXT_DISABLED` | text style | no | `slider.text` |
| `slider.padding` | `Slider.STYLE_PADDING` | insets | no | None |
| `slider.text.scroll` | `Slider.STYLE_TEXT_SCROLL` | metric | no | 0, which cuts a label too wide instead of scrolling it |
| `slider.text.scroll.minimum` | `Slider.STYLE_TEXT_SCROLL_MINIMUM` | metric | no | 0 |
| `slider.key.travel` | `Slider.STYLE_KEY_TRAVEL` | metric | no | 0, which moves a slider with no step by a hundredth of the range |

A label too wide for the slider is cut at the left and right of `slider.padding`, or scrolls as a [button's caption](#button) does. `slider.key.travel` is in units of the thumb's travel along the track and applies only to a slider with no step.

## TextBox

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `textbox.normal` | `TextBox.STYLE_NORMAL` | paint | yes | |
| `textbox.focused` | `TextBox.STYLE_FOCUSED` | paint | yes | |
| `textbox.disabled` | `TextBox.STYLE_DISABLED` | paint | yes | |
| `textbox.text` | `TextBox.STYLE_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `textbox.text.disabled` | `TextBox.STYLE_TEXT_DISABLED` | text style | no | `textbox.text` |
| `textbox.text.readonly` | `TextBox.STYLE_TEXT_READ_ONLY` | text style | no | `textbox.text.disabled` |
| `textbox.placeholder` | `TextBox.STYLE_PLACEHOLDER` | text style | no | `textbox.text.disabled` |
| `textbox.padding` | `TextBox.STYLE_PADDING` | insets | no | 4, 5, 4 and 5 |
| `textbox.selection` | `TextBox.STYLE_SELECTION` | paint | no | `0x806A9FFF`, translucent blue |
| `textbox.caret` | `TextBox.STYLE_CARET` | paint | no | `0xFFE0E0E0`, light gray |
| `textbox.suggestions` | `TextBox.STYLE_SUGGESTIONS` | paint | no | `combobox.popup` |
| `textbox.suggestion.text` | `TextBox.STYLE_SUGGESTION_TEXT` | text style | no | `textbox.text` |
| `textbox.text.selected` | `TextBox.STYLE_TEXT_SELECTED` | text style | no | None; selected text keeps its own style |
| `textbox.selection.extend` | `TextBox.STYLE_SELECTION_EXTEND` | metric | no | 0 |
| `textbox.caret.extend` | `TextBox.STYLE_CARET_EXTEND` | metric | no | 0 |
| `textbox.caret.blink` | `TextBox.STYLE_CARET_BLINK` | metric | no | 500 |
| `textbox.caret.underscore` | `TextBox.STYLE_CARET_UNDERSCORE` | metric | no | 0, a bar |
| `textbox.placeholder.hide` | `TextBox.STYLE_PLACEHOLDER_HIDE` | metric | no | 0, shown while focused |

The suggestion list also uses the `ListView` and `ScrollPanel` keys.

`textbox.selection.extend` and `textbox.caret.extend` are how many units the selection and the caret reach above and below the line. `textbox.caret.blink` is how many milliseconds the caret stays shown and then hidden. With `textbox.caret.underscore` set to any value but 0, a caret at the end of the text is an underscore in the text's style, 1 unit after the text. With `textbox.placeholder.hide` set to any value but 0, the placeholder hides while the box has focus. Password boxes read all six keys. Text areas read all but `textbox.caret.underscore` and always draw the caret as a bar.

## PasswordTextBox

A password box reads the `TextBox` keys above for its face, text, selection, caret, placeholder and padding, the metrics included. Its own keys are optional.

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `passwordbox.mask` | `PasswordTextBox.STYLE_MASK` | text style | no | `textbox.text`; a disabled or read-only box uses `textbox.text.disabled` or `textbox.text.readonly` instead |
| `passwordbox.reveal` | `PasswordTextBox.STYLE_REVEAL` | paint | no | An eye drawn in the placeholder's color |
| `passwordbox.reveal.hovered` | `PasswordTextBox.STYLE_REVEAL_HOVERED` | paint | no | `passwordbox.reveal`, else the eye in the text color |
| `passwordbox.reveal.active` | `PasswordTextBox.STYLE_REVEAL_ACTIVE` | paint | no | The eye with a slash, in the text color |
| `passwordbox.reveal.disabled` | `PasswordTextBox.STYLE_REVEAL_DISABLED` | paint | no | `passwordbox.reveal`, else the eye in the disabled text color |

`reveal` and `reveal.hovered` draw while the text is hidden, offering to show it; `reveal.active` draws while it shows, offering to hide it; `reveal.disabled` draws while the user cannot reveal text that code set. `Themes.flat()` sets none of them.

## ScrollPanel

`ListView` and `ComboBox` use these keys for their scroll bars too.

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `scroll.thumb` | `ScrollPanel.STYLE_THUMB` | paint | yes | |
| `scroll.thumb.hovered` | `ScrollPanel.STYLE_THUMB_HOVERED` | paint | no | `scroll.thumb` |
| `scroll.track` | `ScrollPanel.STYLE_TRACK` | paint | no | Nothing |

## ScrollBar

A standard scroll bar reads the `scroll.*` keys above (`ScrollBar.STYLE_TRACK`, `STYLE_THUMB` and `STYLE_THUMB_HOVERED` are the same strings), plus these.

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `scroll.thumb.disabled` | `ScrollBar.STYLE_THUMB_DISABLED` | paint | no | `scroll.thumb` |
| `scroll.slim.track` | `ScrollBar.STYLE_SLIM_TRACK` | paint | no | `scroll.track` |
| `scroll.slim.thumb` | `ScrollBar.STYLE_SLIM_THUMB` | paint | no | `scroll.thumb` |
| `scroll.slim.thumb.hovered` | `ScrollBar.STYLE_SLIM_THUMB_HOVERED` | paint | no | `scroll.slim.thumb` |
| `scroll.slim.thumb.disabled` | `ScrollBar.STYLE_SLIM_THUMB_DISABLED` | paint | no | `scroll.slim.thumb` |

## ListView

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `list.background` | `ListView.STYLE_BACKGROUND` | paint | no | Nothing |
| `list.row.selected` | `ListView.STYLE_ROW_SELECTED` | paint | yes | |
| `list.row` | `ListView.STYLE_ROW` | paint | no | Nothing |
| `list.row.hovered` | `ListView.STYLE_ROW_HOVERED` | paint | no | `list.row` |
| `list.placeholder` | `ListView.STYLE_PLACEHOLDER` | text style | no | `TextStyle.DEFAULT` |
| `list.cell.padding` | `ListView.STYLE_CELL_PADDING` | insets | no | 4, 0, 4 and 0 |
| `list.row.selected.unfocused` | `ListView.STYLE_ROW_SELECTED_UNFOCUSED` | paint | no | `list.row.selected` |
| `list.row.ring` | `ListView.STYLE_ROW_RING` | paint | no | Nothing |
| `list.row.ring.unfocused` | `ListView.STYLE_ROW_RING_UNFOCUSED` | paint | no | Nothing |
| `list.row.width` | `ListView.STYLE_ROW_WIDTH` | metric | no | 0, rows as wide as the list |
| `list.bar.gap` | `ListView.STYLE_BAR_GAP` | metric | no | 2 |
| `list.scroll.rows` | `ListView.STYLE_SCROLL_ROWS` | metric | no | 1 |
| `list.edge` | `ListView.STYLE_EDGE` | paint | no | Nothing |
| `list.edge.outer` | `ListView.STYLE_EDGE_OUTER` | paint | no | Nothing |
| `list.padding` | `ListView.STYLE_PADDING` | insets | no | None |

The ring is one unit wide, just inside the selected row; `list.row.ring` draws while the list has focus and `list.row.ring.unfocused` while it does not. `list.scroll.rows` is how many rows one wheel notch scrolls. A list wider than `list.row.width` centers a column of rows that wide and puts its scroll bar `list.bar.gap` units right of it. `list.edge` is a one-unit line just outside the list's top and bottom edges, and `list.edge.outer` another beyond it. `list.padding` puts space above the first row and below the last, which scrolls with the rows; only its top and bottom are read. Row width, padding and edges apply only to lists the factory makes. The lists inside tables, trees, combo boxes, text box suggestions and time pickers keep full-width rows and draw no edges, and read the other list keys.

## ComboBox

The open list also uses the `ListView` and `ScrollPanel` keys.

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `combobox.normal` | `ComboBox.STYLE_NORMAL` | paint | yes | |
| `combobox.popup` | `ComboBox.STYLE_POPUP` | paint | yes | |
| `combobox.hovered` | `ComboBox.STYLE_HOVERED` | paint | no | `combobox.normal` |
| `combobox.disabled` | `ComboBox.STYLE_DISABLED` | paint | no | `combobox.normal` |
| `combobox.text` | `ComboBox.STYLE_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `combobox.text.disabled` | `ComboBox.STYLE_TEXT_DISABLED` | text style | no | `combobox.text` |
| `combobox.padding` | `ComboBox.STYLE_PADDING` | insets | no | 4, 3, 4 and 3 |

## Window

`MessageBox` uses these keys and those of `Label` and `Button`, and its own `messagebox.*` keys, the last rows.

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `window.background` | `Window.STYLE_BACKGROUND` | paint | yes | |
| `window.title` | `Window.STYLE_TITLE_BAR` | paint | no | Nothing |
| `window.title.text` | `Window.STYLE_TITLE_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `window.close` | `Window.STYLE_CLOSE` | text style | no | `window.title.text` |
| `window.close.hovered` | `Window.STYLE_CLOSE_HOVERED` | text style | no | `window.close` |
| `window.grip` | `Window.STYLE_GRIP` | paint | no | Nothing |
| `window.grip.hovered` | `Window.STYLE_GRIP_HOVERED` | paint | no | `window.grip` |
| `window.title.padding` | `Window.STYLE_TITLE_PADDING` | insets | no | 3 on every side |
| `window.padding` | `Window.STYLE_PADDING` | insets | no | 3 on every side |
| `messagebox.padding` | `MessageBox.STYLE_PADDING` | insets | no | 4 on every side |
| `messagebox.spacing` | `MessageBox.STYLE_SPACING` | metric | no | 8 |
| `messagebox.message.width` | `MessageBox.STYLE_MESSAGE_WIDTH` | metric | no | 280 |
| `messagebox.message.center` | `MessageBox.STYLE_MESSAGE_CENTER` | metric | no | 0, lines start at the left |
| `messagebox.button.width` | `MessageBox.STYLE_BUTTON_WIDTH` | metric | no | 64 |
| `messagebox.button.width.maximum` | `MessageBox.STYLE_BUTTON_WIDTH_MAXIMUM` | metric | no | 0, no limit |
| `messagebox.button.gap` | `MessageBox.STYLE_BUTTON_GAP` | metric | no | 4 |
| `messagebox.close` | `MessageBox.STYLE_CLOSE` | metric | no | 1, the close button shows |

`messagebox.spacing` is the space between the message and the buttons, and `messagebox.message.width` where the message wraps. With `messagebox.button.width` above 0, each button is that wide and the row sits at the right. At 0, the box is as wide as the message width and the buttons share it evenly, each at most `messagebox.button.width.maximum` wide, centered. `messagebox.close` at 0 hides the window's close button, and Escape still answers.

## TabBar

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `tab.normal` | `TabBar.STYLE_NORMAL` | paint | yes | |
| `tab.selected` | `TabBar.STYLE_SELECTED` | paint | yes | |
| `tab.hovered` | `TabBar.STYLE_HOVERED` | paint | no | `tab.normal` |
| `tab.disabled` | `TabBar.STYLE_DISABLED` | paint | no | `tab.normal` |
| `tab.selected.disabled` | `TabBar.STYLE_SELECTED_DISABLED` | paint | no | `tab.disabled` |
| `tab.text` | `TabBar.STYLE_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `tab.text.selected` | `TabBar.STYLE_TEXT_SELECTED` | text style | no | `tab.text` |
| `tab.text.disabled` | `TabBar.STYLE_TEXT_DISABLED` | text style | no | `tab.text` |
| `tab.padding` | `TabBar.STYLE_PADDING` | insets | no | 8, 4, 8 and 3 |
| `tab.padding.selected` | `TabBar.STYLE_PADDING_SELECTED` | insets | no | `tab.padding` |
| `tab.selected.hovered` | `TabBar.STYLE_SELECTED_HOVERED` | paint | no | `tab.selected`. Also drawn while the bar has focus the keyboard gave it, not a click |
| `tab.selected.fill` | `TabBar.STYLE_SELECTED_FILL` | paint | no | Nothing |
| `tab.selected.fill.insets` | `TabBar.STYLE_SELECTED_FILL_INSETS` | insets | no | None |
| `tab.underline` | `TabBar.STYLE_UNDERLINE` | paint | no | Nothing |
| `tab.underline.disabled` | `TabBar.STYLE_UNDERLINE_DISABLED` | paint | no | `tab.underline` |
| `tab.clip` | `TabBar.STYLE_CLIP` | insets | no | The padding's left and right, none at the top and bottom |
| `tab.text.scroll` | `TabBar.STYLE_TEXT_SCROLL` | metric | no | 0, which cuts text too wide instead of scrolling it |
| `tab.text.scroll.minimum` | `TabBar.STYLE_TEXT_SCROLL_MINIMUM` | metric | no | 0 |

`tab.selected.fill` is a second layer over the selected tab's face, `tab.selected.fill.insets` in from its edges. The underline is 1 unit tall, its bottom 1 unit above the tab's bottom, as wide as the text but at most the tab less 2 units at each side, and centered. A tab's text too wide for it is cut or scrolls as a [button's caption](#button) does, with the tab keys.

## ProgressBar and Separator

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `progress.track` | `ProgressBar.STYLE_TRACK` | paint | yes | |
| `progress.fill` | `ProgressBar.STYLE_FILL` | paint | yes | |
| `progress.text` | `ProgressBar.STYLE_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `progress.segment` | `ProgressBar.STYLE_SEGMENT` | paint | no | `progress.fill` |
| `progress.padding` | `ProgressBar.STYLE_PADDING` | insets | no | 2 on every side |
| `separator` | `Separator.STYLE_LINE` | paint | yes | |
| `separator.shadow` | `Separator.STYLE_SHADOW` | paint | no | Nothing |

`separator.shadow` is a second line as thick as the first, below a horizontal separator's line or right of a vertical one, so a theme that sets it makes every separator twice as thick.

## BusyIndicator

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `busy.dot` | `BusyIndicator.STYLE_DOT` | paint | yes | |
| `busy.dot.active` | `BusyIndicator.STYLE_DOT_ACTIVE` | paint | yes | |
| `busy.text` | `BusyIndicator.STYLE_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `busy.dots.text` | `BusyIndicator.STYLE_DOTS_TEXT` | text style | no | None; the dots use the dot paints |

With `busy.dots.text` set, the dots are the letters `o o o` in that style, the lit one a capital `O`, one line tall and centered where the dots would be.

## Avatar

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `avatar.background` | `Avatar.STYLE_BACKGROUND` | paint | yes | |
| `avatar.frame` | `Avatar.STYLE_FRAME` | paint | no | Nothing |
| `avatar.initials` | `Avatar.STYLE_INITIALS` | text style | no | `TextStyle.DEFAULT` |
| `avatar.placeholder` | `Avatar.STYLE_PLACEHOLDER` | paint | no | Nothing |
| `avatar.placeholder.group` | `Avatar.STYLE_PLACEHOLDER_GROUP` | paint | no | `avatar.placeholder` |
| `badge.background` | `Avatar.STYLE_BADGE` | paint | no | solid `0xFFD32F2F`, a red fill |
| `badge.text` | `Avatar.STYLE_BADGE_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `badge.padding` | `Avatar.STYLE_BADGE_PADDING` | insets | no | 2, 1, 2 and 1 |

The `badge.*` keys are shared with the items of a `NavigationView`, so a theme styles every count at once.

## InfoBar

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `infobar.background` | `InfoBar.STYLE_BACKGROUND` | paint | yes | |
| `infobar.background.info`, `.success`, `.warning`, `.error` | `InfoBar.STYLE_BACKGROUND_INFO` and the others | paint | no | `infobar.background` |
| `infobar.accent.info`, `.success`, `.warning`, `.error` | `InfoBar.STYLE_ACCENT_INFO` and the others | paint | no | Nothing |
| `infobar.icon.info`, `.success`, `.warning`, `.error` | `InfoBar.STYLE_ICON_INFO` and the others | paint | no | No icon |
| `infobar.title` | `InfoBar.STYLE_TITLE` | text style | no | `infobar.text` made bold |
| `infobar.text` | `InfoBar.STYLE_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `infobar.close` | `InfoBar.STYLE_CLOSE` | text style | no | `window.close`, then `infobar.text` |
| `infobar.close.hovered` | `InfoBar.STYLE_CLOSE_HOVERED` | text style | no | `window.close.hovered`, then `infobar.close` |
| `infobar.padding` | `InfoBar.STYLE_PADDING` | insets | no | 6, 4, 6 and 4 |

The close button draws `button.focus` as a ring while it has focus.

## RadioButton

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `radio.normal` | `RadioButton.STYLE_NORMAL` | paint | yes | |
| `radio.hovered` | `RadioButton.STYLE_HOVERED` | paint | yes | |
| `radio.selected` | `RadioButton.STYLE_SELECTED` | paint | yes | |
| `radio.selected.hovered` | `RadioButton.STYLE_SELECTED_HOVERED` | paint | yes | |
| `radio.disabled` | `RadioButton.STYLE_DISABLED` | paint | no | `radio.normal` |
| `radio.selected.disabled` | `RadioButton.STYLE_SELECTED_DISABLED` | paint | no | `radio.selected` |
| `radio.mark` | `RadioButton.STYLE_MARK` | paint | no | No mark |
| `radio.mark.disabled` | `RadioButton.STYLE_MARK_DISABLED` | paint | no | `radio.mark` |
| `radio.focus` | `RadioButton.STYLE_FOCUS` | paint | no | No focus ring |
| `radio.text` | `RadioButton.STYLE_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `radio.text.disabled` | `RadioButton.STYLE_TEXT_DISABLED` | text style | no | `radio.text` |

## ToggleSwitch

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `toggle.track` | `ToggleSwitch.STYLE_TRACK` | paint | yes | |
| `toggle.track.on` | `ToggleSwitch.STYLE_TRACK_ON` | paint | yes | |
| `toggle.knob` | `ToggleSwitch.STYLE_KNOB` | paint | yes | |
| `toggle.track.disabled` | `ToggleSwitch.STYLE_TRACK_DISABLED` | paint | no | `toggle.track` |
| `toggle.knob.hovered` | `ToggleSwitch.STYLE_KNOB_HOVERED` | paint | no | `toggle.knob` |
| `toggle.knob.disabled` | `ToggleSwitch.STYLE_KNOB_DISABLED` | paint | no | `toggle.knob` |
| `toggle.focus` | `ToggleSwitch.STYLE_FOCUS` | paint | no | No focus ring |
| `toggle.text` | `ToggleSwitch.STYLE_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `toggle.text.disabled` | `ToggleSwitch.STYLE_TEXT_DISABLED` | text style | no | `toggle.text` |
| `toggle.track.padding` | `ToggleSwitch.STYLE_TRACK_PADDING` | insets | no | 4, 2, 4 and 2 |
| `toggle.knob.padding` | `ToggleSwitch.STYLE_KNOB_PADDING` | insets | no | 2 on every side |

## Spinner and TextArea

A spinner's field and a text area read the `TextBox` keys, and a text area the `ScrollPanel` keys for its bar. A text area's padding has its own key, which falls back to `textbox.padding`.

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `spinner.button` | `Spinner.STYLE_BUTTON` | paint | no | `button.normal` |
| `spinner.button.hovered` | `Spinner.STYLE_BUTTON_HOVERED` | paint | no | `button.hovered` |
| `spinner.button.disabled` | `Spinner.STYLE_BUTTON_DISABLED` | paint | no | `button.disabled` |
| `textarea.padding` | `TextArea.STYLE_PADDING` | insets | no | `textbox.padding`, else 4 on every side |

The spinner's arrows use the colors of `button.text` and `button.text.disabled`.

## TableView and TreeView

Both read the `ListView` and `ScrollPanel` keys for their rows.

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `table.header` | `TableView.STYLE_HEADER` | paint | yes | |
| `table.header.hovered` | `TableView.STYLE_HEADER_HOVERED` | paint | no | `table.header` |
| `table.header.text` | `TableView.STYLE_HEADER_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `table.grid` | `TableView.STYLE_GRID` | paint | no | Nothing |
| `table.header.padding` | `TableView.STYLE_HEADER_PADDING` | insets | no | 4, 0, 4 and 0 |
| `tree.arrow` | `TreeView.STYLE_ARROW` | paint | no | solid `0xFFA0A0A0` |

## Menu and MenuBar

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `menu.background` | `Menu.STYLE_BACKGROUND` | paint | yes | |
| `menu.item.hovered` | `Menu.STYLE_ITEM_HOVERED` | paint | yes | |
| `menu.text` | `Menu.STYLE_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `menu.text.disabled` | `Menu.STYLE_TEXT_DISABLED` | text style | no | `menu.text` |
| `menu.shortcut` | `Menu.STYLE_SHORTCUT` | text style | no | `menu.text.disabled` |
| `menu.separator` | `Menu.STYLE_SEPARATOR` | paint | no | `separator` |
| `menu.item.padding` | `Menu.STYLE_ITEM_PADDING` | insets | no | 14, 3, 14 and 3 |
| `menu.padding` | `Menu.STYLE_PADDING` | insets | no | 2 on every side |
| `menubar.background` | `MenuBar.STYLE_BACKGROUND` | paint | no | Nothing |
| `menubar.item.hovered` | `MenuBar.STYLE_ITEM_HOVERED` | paint | no | `menu.item.hovered` |
| `menubar.text` | `MenuBar.STYLE_TEXT` | text style | no | `menu.text` |
| `menubar.title.padding` | `MenuBar.STYLE_TITLE_PADDING` | insets | no | 8, 3, 8 and 3 |

## Flyout

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `flyout.background` | `Flyout.STYLE_BACKGROUND` | paint | no | `combobox.popup` |
| `flyout.padding` | `Flyout.STYLE_PADDING` | insets | no | 4 on every side |

## NavigationView

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `nav.pane` | `NavigationView.STYLE_PANE` | paint | no | Nothing |
| `nav.pane.overlay` | `NavigationView.STYLE_PANE_OVERLAY` | paint | no | `nav.pane`, then `combobox.popup` |
| `nav.item.hovered` | `NavigationView.STYLE_ITEM_HOVERED` | paint | no | `list.row.hovered` |
| `nav.item.selected` | `NavigationView.STYLE_ITEM_SELECTED` | paint | yes | |
| `nav.indicator` | `NavigationView.STYLE_INDICATOR` | paint | no | Nothing |
| `nav.item.text` | `NavigationView.STYLE_ITEM_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `nav.item.text.selected` | `NavigationView.STYLE_ITEM_TEXT_SELECTED` | text style | no | `nav.item.text` |
| `nav.item.text.disabled` | `NavigationView.STYLE_ITEM_TEXT_DISABLED` | text style | no | `textbox.text.disabled`, then `nav.item.text` |
| `nav.header.text` | `NavigationView.STYLE_HEADER_TEXT` | text style | no | `nav.item.text.disabled` |
| `nav.title.text` | `NavigationView.STYLE_TITLE_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `nav.separator` | `NavigationView.STYLE_SEPARATOR` | paint | no | `separator` |
| `nav.toggle` | `NavigationView.STYLE_TOGGLE` | paint | no | Nothing |
| `nav.toggle.hovered` | `NavigationView.STYLE_TOGGLE_HOVERED` | paint | no | `nav.item.hovered` |
| `nav.focus` | `NavigationView.STYLE_FOCUS` | paint | no | `button.focus` |
| `nav.item.padding` | `NavigationView.STYLE_ITEM_PADDING` | insets | no | 6, 5, 6 and 5 |
| `nav.pane.padding` | `NavigationView.STYLE_PANE_PADDING` | insets | no | 2 on every side |

The toggle's three lines are drawn in the `nav.item.text` color over `nav.toggle`. Badges use the `badge.*` keys, and the scrolling items the `scroll.*` keys.

## Expander, SplitPane and GridView

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `expander.header` | `Expander.STYLE_HEADER` | paint | no | Nothing |
| `expander.header.hovered` | `Expander.STYLE_HEADER_HOVERED` | paint | no | `expander.header` |
| `expander.text` | `Expander.STYLE_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `expander.text.disabled` | `Expander.STYLE_TEXT_DISABLED` | text style | no | `expander.text` |
| `expander.header.padding` | `Expander.STYLE_HEADER_PADDING` | insets | no | 4, 3, 4 and 3 |
| `split.divider` | `SplitPane.STYLE_DIVIDER` | paint | no | `separator` |
| `split.divider.hovered` | `SplitPane.STYLE_DIVIDER_HOVERED` | paint | no | `split.divider` |
| `grid.cell` | `GridView.STYLE_CELL` | paint | yes | |
| `grid.cell.hovered` | `GridView.STYLE_CELL_HOVERED` | paint | no | Nothing |
| `grid.cell.selected` | `GridView.STYLE_CELL_SELECTED` | paint | no | `grid.cell.hovered` |
| `grid.cell.padding` | `GridView.STYLE_CELL_PADDING` | insets | no | 1 on every side |
| `grid.cell.hovered.back` | `GridView.STYLE_CELL_HOVERED_BACK` | paint | no | Nothing |
| `grid.cell.highlight` | `GridView.STYLE_CELL_HIGHLIGHT` | insets | no | None, the whole cell |

`grid.cell.hovered.back` is drawn under the hovered cell's content, over the cell's own paint. `grid.cell.highlight` says where the hovered and selected paints go, in from the cell's edges.

## ColorPicker

A color picker's hex field uses the `TextBox` keys.

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `colorpicker.frame` | `ColorPicker.STYLE_FRAME` | paint | no | Nothing |
| `colorpicker.marker` | `ColorPicker.STYLE_MARKER` | paint | no | `0xFFFFFFFF`, white |
| `colorpicker.marker.outline` | `ColorPicker.STYLE_MARKER_OUTLINE` | paint | no | `0xFF000000`, black |
| `colorpicker.disabled` | `ColorPicker.STYLE_DISABLED` | paint | no | Nothing |

## CalendarView

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `calendar.background` | `CalendarView.STYLE_BACKGROUND` | paint | no | Nothing; in a picker's popup, `picker.popup` is behind it |
| `calendar.header.text` | `CalendarView.STYLE_HEADER_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `calendar.header.hovered` | `CalendarView.STYLE_HEADER_HOVERED` | paint | no | `list.row.hovered` |
| `calendar.weekday.text` | `CalendarView.STYLE_WEEKDAY_TEXT` | text style | no | `calendar.day.text.outside` |
| `calendar.day.text` | `CalendarView.STYLE_DAY_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `calendar.day.text.outside` | `CalendarView.STYLE_DAY_TEXT_OUTSIDE` | text style | no | `calendar.day.text.disabled` |
| `calendar.day.text.disabled` | `CalendarView.STYLE_DAY_TEXT_DISABLED` | text style | no | `textbox.text.disabled`, else `TextStyle.DEFAULT` |
| `calendar.day.text.selected` | `CalendarView.STYLE_DAY_TEXT_SELECTED` | text style | no | `calendar.day.text` |
| `calendar.day.hovered` | `CalendarView.STYLE_DAY_HOVERED` | paint | no | `list.row.hovered` |
| `calendar.day.selected` | `CalendarView.STYLE_DAY_SELECTED` | paint | yes | |
| `calendar.day.today` | `CalendarView.STYLE_DAY_TODAY` | paint | no | A one-unit outline in the `calendar.day.text` color |
| `calendar.day.marker` | `CalendarView.STYLE_DAY_MARKER` | paint | no | The `calendar.day.text` color |
| `calendar.arrow` | `CalendarView.STYLE_ARROW` | paint | no | Arrows in the `calendar.header.text` color, and the `calendar.day.text.disabled` color at the ends of the range |
| `calendar.focus` | `CalendarView.STYLE_FOCUS` | paint | no | `button.focus` |
| `calendar.padding` | `CalendarView.STYLE_PADDING` | insets | no | 4 on every side |
| `calendar.cell.padding` | `CalendarView.STYLE_CELL_PADDING` | insets | no | 3, 2, 3 and 2 |

## Date and time pickers

`DatePicker`, `TimePicker` and `DateTimePicker` share these keys, and their constants are on each of the three. The fields also read `textbox.normal`, `textbox.focused` and `textbox.disabled`, which they require, and the `textbox.*` text and placeholder styles. A date picker requires `calendar.day.selected` for its calendar, and a time picker requires `list.row.selected` and `scroll.thumb` for its columns; a date and time picker requires all three.

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `picker.segment.focused` | `DatePicker.STYLE_SEGMENT_FOCUSED` | paint | no | `textbox.selection` |
| `picker.button` | `DatePicker.STYLE_BUTTON` | paint | no | `spinner.button`, else `button.normal` |
| `picker.button.hovered` | `DatePicker.STYLE_BUTTON_HOVERED` | paint | no | `spinner.button.hovered`, else `button.hovered` |
| `picker.button.disabled` | `DatePicker.STYLE_BUTTON_DISABLED` | paint | no | `spinner.button.disabled`, else `button.disabled` |
| `picker.icon.date` | `DatePicker.STYLE_ICON`, `DateTimePicker.STYLE_ICON` | paint | no | The combo box's down arrow in the text color |
| `picker.icon.time` | `TimePicker.STYLE_ICON` | paint | no | The combo box's down arrow in the text color |
| `picker.popup` | `DatePicker.STYLE_POPUP` | paint | no | `combobox.popup` |
| `picker.padding` | `DatePicker.STYLE_PADDING` | insets | no | `textbox.padding` |

## Overlays and tooltips

| Key | Constant | Kind | Required | Fallback |
|---|---|---|---|---|
| `overlay.backdrop` | `Overlays.STYLE_BACKDROP` | paint | no | Nothing |
| `tooltip.background` | `Tooltip.STYLE_BACKGROUND` | paint | no | `0xF0100010` with a one-unit `0x805000FF` edge |
| `tooltip.text` | `Tooltip.STYLE_TEXT` | text style | no | `TextStyle.DEFAULT` |
| `tooltip.padding` | `Tooltip.STYLE_PADDING` | insets | no | 4 on every side |
| `tooltip.frame` | `Tooltip.STYLE_FRAME` | paint | no | Nothing |
| `tooltip.outset` | `Tooltip.STYLE_OUTSET` | insets | no | None |
| `tooltip.delay` | `Tooltip.STYLE_DELAY` | metric | no | 500 |
| `tooltip.offset.x` | `Tooltip.STYLE_OFFSET_X` | metric | no | 12 |
| `tooltip.offset.y` | `Tooltip.STYLE_OFFSET_Y` | metric | no | -12 |
| `tooltip.below` | `Tooltip.STYLE_BELOW` | metric | no | 0, beside the pointer |
| `tooltip.below.overlap` | `Tooltip.STYLE_BELOW_OVERLAP` | metric | no | 3 |
| `tooltip.below.gap` | `Tooltip.STYLE_BELOW_GAP` | metric | no | 5 |
| `toast.background` | `Overlays.STYLE_TOAST` | paint | no | `tooltip.background` |
| `toast.text` | `Overlays.STYLE_TOAST_TEXT` | text style | no | `tooltip.text` |
| `toast.padding` | `Overlays.STYLE_TOAST_PADDING` | insets | no | 6 on every side |
| `toast.title` | `Overlays.STYLE_TOAST_TITLE` | text style | no | None: the first line is drawn like the rest |
| `toast.line.gap` | `Overlays.STYLE_TOAST_LINE_GAP` | metric | no | 0 |
| `toast.width.minimum` | `Overlays.STYLE_TOAST_WIDTH_MINIMUM` | metric | no | 0 |
| `toast.margin` | `Overlays.STYLE_TOAST_MARGIN` | metric | no | `Overlays.TOAST_MARGIN` |

`tooltip.frame` is a second layer over `tooltip.background`, and `tooltip.outset` how far the two reach past the box, for images with a margin of their own; the box alone is placed and kept inside the host. `tooltip.delay` is in milliseconds, and a delay the adapter sets with `ScreenHost.setTooltipDelay` wins. The offsets put the box's top-left corner that many units right of and below the pointer's position floored to a whole unit, so a negative `tooltip.offset.y` puts it above. A box flipped to the pointer's left has its right edge `tooltip.offset.x` units left of the pointer. With `tooltip.below` set to any value but 0, the box sits just below its component instead and moves a little with the pointer's height, by an amount `tooltip.below.overlap` and `tooltip.below.gap` shape, and goes above the pointer when the host has no room below. Its left edge still follows `tooltip.offset.x`. The `Tooltip.STYLE_BELOW` Javadoc gives the formula.

## Image and panels

`Image` and the four plain panels read no keys. Give a panel a background with `setBackground` and a border with `setBorder`, and put an image in a panel to frame it.

## Values in the built-in themes

### `Themes.flat()`

| Key | Value |
|---|---|
| `button.normal` | solid `0xFF3C3F44` |
| `button.hovered` | solid `0xFF4E525A` |
| `button.pressed` | solid `0xFF2B2D31` |
| `button.disabled` | solid `0xFF2A2A2A` |
| `button.focus` | solid `0xFF6A9FFF` |
| `button.text` | `0xFFF0F0F0` |
| `button.text.disabled` | `0xFF808080` |
| `button.link.text` | `0xFF6AA8FF` |
| `button.link.text.hovered` | `0xFF9CC4FF` underlined |
| `button.link.text.disabled` | `0xFF808080` |
| `label.text` | `0xFFE0E0E0` |
| `checkbox.normal`, `checkbox.checked` | solid `0xFF1B1C1F` |
| `checkbox.hovered`, `checkbox.checked.hovered` | solid `0xFF2E3136` |
| `checkbox.disabled`, `checkbox.checked.disabled` | solid `0xFF2A2A2A` |
| `checkbox.mark` | solid `0xFF6A9FFF` |
| `checkbox.mark.disabled` | solid `0xFF505050` |
| `checkbox.focus` | solid `0xFF6A9FFF` |
| `checkbox.text` | `0xFFE0E0E0` |
| `checkbox.text.disabled` | `0xFF707070` |
| `slider.track` | solid `0xFF1B1C1F` |
| `slider.track.focused` | solid `0xFF24262A` |
| `slider.track.disabled` | solid `0xFF2A2A2A` |
| `slider.thumb` | solid `0xFF5A5E66` |
| `slider.thumb.hovered` | solid `0xFF6A9FFF` |
| `slider.thumb.disabled` | solid `0xFF3A3A3A` |
| `slider.text` | `0xFFF0F0F0` with shadow |
| `slider.text.disabled` | `0xFF707070` |
| `textbox.normal` | solid `0xFF1B1C1F` |
| `textbox.focused` | solid `0xFF24262A` |
| `textbox.disabled` | solid `0xFF2A2A2A` |
| `textbox.selection` | solid `0x806A9FFF` |
| `textbox.caret` | solid `0xFFE0E0E0` |
| `textbox.text` | `0xFFE0E0E0` |
| `textbox.text.disabled` | `0xFF707070` |
| `textbox.placeholder` | `0xFF7A7F87` |
| `textbox.text.readonly` | `0xFFC5C5C5` |
| `scroll.thumb` | solid `0xFF5A5E66` |
| `scroll.thumb.hovered` | solid `0xFF7A7F88` |
| `scroll.track` | solid `0xFF1B1C1F` |
| `scroll.thumb.disabled` | solid `0xFF3A3A3A` |
| `scroll.slim.track` | solid `0xFF2B2D31` |
| `scroll.slim.thumb` | solid `0xFF8A8F98` |
| `scroll.slim.thumb.hovered` | solid `0xFFE0E0E0` |
| `scroll.slim.thumb.disabled` | solid `0xFF3A3A3A` |
| `list.background` | solid `0xFF18191C` |
| `list.row` | solid `0xFF1F2024` |
| `list.row.hovered` | solid `0xFF2E3136` |
| `list.row.selected` | solid `0xFF3A4D6E` |
| `list.placeholder` | `0xFF808080` |
| `combobox.normal` | solid `0xFF1B1C1F` |
| `combobox.hovered` | solid `0xFF2E3136` |
| `combobox.disabled` | solid `0xFF2A2A2A` |
| `combobox.popup` | solid `0xFF16171A` |
| `combobox.text` | `0xFFE0E0E0` |
| `combobox.text.disabled` | `0xFF707070` |
| `window.background` | solid `0xF0141518` |
| `window.title` | solid `0xFF2B2D31` |
| `window.title.text` | `0xFFFFFFFF` |
| `window.close` | `0xFFA0A0A0` |
| `window.close.hovered` | `0xFFFFFFFF` |
| `window.grip` | solid `0xFF5A5E66` |
| `window.grip.hovered` | solid `0xFF6A9FFF` |
| `tab.normal` | solid `0xFF24262A` |
| `tab.hovered` | solid `0xFF3C3F44` |
| `tab.selected` | solid `0xFF3A4D6E` |
| `tab.disabled` | solid `0xFF1B1C1F` |
| `tab.selected.disabled` | solid `0xFF2A3140` |
| `tab.text` | `0xFFA0A0A0` |
| `tab.text.selected` | `0xFFFFFFFF` |
| `tab.text.disabled` | `0xFF606060` |
| `progress.track` | solid `0xFF1B1C1F` |
| `progress.fill` | solid `0xFF2E7D32` |
| `progress.text` | `0xFFF0F0F0` |
| `progress.segment` | solid `0xFF43A047` |
| `busy.dot` | solid `0xFF5A5E66` |
| `busy.dot.active` | solid `0xFFE0E0E0` |
| `busy.text` | `0xFFE0E0E0` |
| `separator` | solid `0xFF3C3F44` |
| `radio.normal`, `radio.selected` | solid `0xFF1B1C1F` |
| `radio.hovered`, `radio.selected.hovered` | solid `0xFF2E3136` |
| `radio.disabled`, `radio.selected.disabled` | solid `0xFF2A2A2A` |
| `radio.mark`, `radio.focus` | solid `0xFF6A9FFF` |
| `radio.mark.disabled` | solid `0xFF505050` |
| `radio.text` | `0xFFE0E0E0` |
| `radio.text.disabled` | `0xFF707070` |
| `toggle.track` | solid `0xFF3C3F44` |
| `toggle.track.on` | solid `0xFF3A4D6E` |
| `toggle.track.disabled` | solid `0xFF2A2A2A` |
| `toggle.knob` | solid `0xFF8A8F98` |
| `toggle.knob.hovered` | solid `0xFFE0E0E0` |
| `toggle.knob.disabled` | solid `0xFF505050` |
| `toggle.focus` | solid `0xFF6A9FFF` |
| `toggle.text` | `0xFFE0E0E0` |
| `toggle.text.disabled` | `0xFF707070` |
| `table.header` | solid `0xFF2B2D31` |
| `table.header.hovered` | solid `0xFF3C3F44` |
| `table.header.text` | `0xFFC8C8C8` |
| `table.grid` | solid `0xFF2E3136` |
| `tree.arrow` | solid `0xFFA0A0A0` |
| `menu.background` | solid `0xFF16171A` |
| `menu.item.hovered` | solid `0xFF3A4D6E` |
| `menu.text` | `0xFFE0E0E0` |
| `menu.text.disabled` | `0xFF707070` |
| `menu.shortcut` | `0xFF909090` |
| `menu.separator` | solid `0xFF3C3F44` |
| `menubar.background` | solid `0xFF24262A` |
| `menubar.item.hovered` | solid `0xFF3C3F44` |
| `menubar.text` | `0xFFE0E0E0` |
| `expander.header` | solid `0xFF24262A` |
| `expander.header.hovered` | solid `0xFF2E3136` |
| `expander.text` | `0xFFE0E0E0` |
| `expander.text.disabled` | `0xFF707070` |
| `split.divider` | solid `0xFF2B2D31` |
| `split.divider.hovered` | solid `0xFF6A9FFF` |
| `grid.cell` | solid `0xFF1B1C1F` |
| `grid.cell.hovered` | solid `0x40FFFFFF` |
| `grid.cell.selected` | solid `0x606A9FFF` |
| `textbox.suggestions` | solid `0xFF16171A` |
| `textbox.suggestion.text` | `0xFFE0E0E0` |
| `colorpicker.frame` | solid `0xFF3C3F44` |
| `colorpicker.marker` | solid `0xFFFFFFFF` |
| `colorpicker.marker.outline` | solid `0xFF000000` |
| `colorpicker.disabled` | solid `0xA0141518` |
| `calendar.day.selected` | solid `0xFF3A4D6E` |
| `calendar.header.text` | `0xFFF0F0F0` |
| `calendar.weekday.text` | `0xFF909090` |
| `calendar.day.text` | `0xFFE0E0E0` |
| `toast.background` | solid `0xF02B2D31` |
| `toast.text` | `0xFFF0F0F0` |
| `overlay.backdrop` | solid `0x60000000` |
| `tooltip.text` | `0xFFF0F0F0` |
| `avatar.background` | solid `0xFF3C3F44` |
| `avatar.initials` | `0xFFF0F0F0` |
| `infobar.background` | solid `0xFF24262A` |
| `infobar.accent.info` | solid `0xFF4C86E0` |
| `infobar.accent.success` | solid `0xFF43A047` |
| `infobar.accent.warning` | solid `0xFFE8A33D` |
| `infobar.accent.error` | solid `0xFFE05050` |
| `nav.pane` | solid `0xFF1F2024` |
| `nav.item.selected` | solid `0xFF3A4D6E` |
| `nav.indicator` | solid `0xFF6A9FFF` |
| `nav.item.text` | `0xFFE0E0E0` |

Text styles are otherwise plain: no bold, italic, underline, strikethrough or shadow. `tooltip.background` is not set, so tooltips use the fallback box, and neither are `dropdown.arrow`, `flyout.background` and `flyout.padding`, so drop-down arrows take the button text's color and flyouts the combo box's popup paint. The `badge.*` keys are not set either, so badges are white numbers on a red fill.

## Naming your own keys

Prefix keys for your own components with your project id, such as `examplemod.switch.track`, so they never collide with Strata's keys or another project's.
