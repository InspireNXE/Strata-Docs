# Translation keys

Strata's built-in controls show these keys through [translated text](../guides/text.md#translated-text). Strata has English for each, used when the platform's translator has none. The translator is asked first, so a language file that has a key replaces the English. A key that neither has, such as one of your own with no translation yet, shows as itself, with one warning per key in the log.

| Key | English | Shown by |
|---|---|---|
| `strata.button.ok` | OK | Message box buttons |
| `strata.button.cancel` | Cancel | Message box buttons |
| `strata.button.yes` | Yes | Message box buttons |
| `strata.button.no` | No | Message box buttons |
| `strata.button.close` | Close | Message box buttons, and the tooltip of an info bar's close button |
| `strata.password.show` | Show password | The password box's eye while the text is hidden |
| `strata.password.hide` | Hide password | The password box's eye while the text shows |
| `strata.date.placeholder` | Pick a date | An empty date picker |
| `strata.time.placeholder` | Pick a time | An empty time picker |
| `strata.datetime.placeholder` | Pick a date and time | An empty date and time picker |
| `strata.calendar.title` | `%1$s %2$s` | The calendar's month view title: the month name, then the year. A language can put the year first with `%2$s %1$s`. |
| `strata.calendar.decade` | `%1$s - %2$s` | The calendar's decade view title: the first and last year of the decade |
| `strata.validation.integer` | Enter a whole number | The message of a field bound through `Converters.integer()` that holds text that is not a whole number |
| `strata.validation.integer_range` | `Enter a whole number from %1$s to %2$s` | The same for `Converters.integer(min, max)`, with the ends formatted in the translator's locale |
| `strata.validation.decimal` | Enter a number | The message of a field bound through `Converters.decimal(decimals)` that holds text that is not a number |
| `strata.validation.decimal_range` | `Enter a number from %1$s to %2$s` | The same for `Converters.decimal(min, max, decimals)` |
| `strata.edit.undo` | Undo | The default context menu of text boxes, text areas and password boxes |
| `strata.edit.redo` | Redo | The same, except in a password box |
| `strata.edit.cut` | Cut | The same, except in a password box |
| `strata.edit.copy` | Copy | The same, except in a password box |
| `strata.edit.paste` | Paste | The same |
| `strata.edit.delete` | Delete | The same |
| `strata.edit.select_all` | Select all | The same |

`MessageBox.setButtonText` replaces a button's text with your own.

## Key names

`ComponentFactory.keyText(KeyStroke)` names a key stroke for a hint, and a menu item whose command has a stroke and no hint text draws it that way. Modifiers come first, in the order Control, Alt, Shift, Super, each followed by the separator. Strata has English for every key, so an adapter's translator can replace any of them, such as `strata.modifier.super` with the Command symbol.

| Key | English | Names |
|---|---|---|
| `strata.modifier.control` | Ctrl | The Control modifier |
| `strata.modifier.alt` | Alt | The Alt modifier |
| `strata.modifier.shift` | Shift | The Shift modifier |
| `strata.modifier.super` | Super | The Super modifier |
| `strata.modifier.separator` | + | What goes between the parts of a stroke |
| `strata.key.keypad` | `Num %1$s` | A keypad key, with the key's own name |
| `strata.key.unknown` | ? | A key Strata has no name for |
| `strata.key.space` | Space | Space |
| `strata.key.escape` | Esc | Escape |
| `strata.key.enter` | Enter | Enter |
| `strata.key.tab` | Tab | Tab |
| `strata.key.backspace` | Backspace | Backspace |
| `strata.key.insert` | Ins | Insert |
| `strata.key.delete` | Del | Delete |
| `strata.key.left`, `.right`, `.up`, `.down` | Left, Right, Up, Down | The arrow keys |
| `strata.key.page_up` | PgUp | Page Up |
| `strata.key.page_down` | PgDn | Page Down |
| `strata.key.home` | Home | Home |
| `strata.key.end` | End | End |
| `strata.key.caps_lock` | Caps Lock | Caps Lock |
| `strata.key.num_lock` | Num Lock | Num Lock |
| `strata.key.scroll_lock` | Scroll Lock | Scroll Lock |
| `strata.key.print_screen` | PrtSc | Print Screen |
| `strata.key.pause` | Pause | Pause |
| `strata.key.left_shift`, `.right_shift` | Shift | The Shift keys |
| `strata.key.left_control`, `.right_control` | Ctrl | The Control keys |
| `strata.key.left_alt`, `.right_alt` | Alt | The Alt keys |
| `strata.key.left_super`, `.right_super` | Super | The Super keys |
| `strata.key.menu` | Menu | The Menu key |

Letters, digits, F1 to F12 and the punctuation keys are named by what they show on a US keyboard and have no key, and a keypad key is `strata.key.keypad` around the name of the key it stands for, such as "Num 5" or "Num +". Any other key is `strata.key.` followed by its `Key` constant in lower case.

## Plurals

`Text.plural(key, count, args)` asks the translator for `key.<category>`, where the category is `zero`, `one`, `two`, `few`, `many` or `other` for the translator's language and the count, and falls back to `key.other`. These keys belong to your project, so Strata has no English for them. A language file lists the forms its language uses and always `other`.

## Dates and times

Month and weekday names, the first day of the week, the order of a date's parts, the 12 or 24 hour clock and the names of the halves of the day come from the JDK's data for the translator's locale (`Translator.locale()`). Each can be replaced with a key, which Strata asks the translator for first, so a language the JDK lacks, or a pack that disagrees with it, can fix any of them. Unlike the keys above, these have no English in Strata, since English would hide the locale's own data: a key the translator lacks goes straight to the locale. A value that does not parse is ignored.

| Key | Value | Replaces |
|---|---|---|
| `strata.date.month.1` to `.12` | A name | Full month names, used in the calendar's title |
| `strata.date.month.short.1` to `.12` | A name | Short month names, used in the calendar's year view |
| `strata.date.weekday.short.1` to `.7` | A name | Short weekday names over the calendar's days. Monday is 1, as in `java.time.DayOfWeek`. |
| `strata.date.first_weekday` | A number from 1 to 7 | The first day of the week |
| `strata.date.order` | The letters `d`, `m` and `y` in any order, such as `dmy`, `mdy` or `ymd` | The order of a date's parts in the pickers |
| `strata.date.separator` | Any text | What goes between a date's parts, such as `/` or `.` |
| `strata.time.clock` | `12` or `24` | Whether hours count to 12 or 24 |
| `strata.time.am`, `strata.time.pm` | A name | The halves of the day on a 12 hour clock |

The separator between hours, minutes and seconds comes from the locale, `:` where it has none, and has no key. A missing date or time key is not logged, since most languages need none of them.

Code can still set a control's first day of the week and clock format with `setFirstDayOfWeek` and `setClockFormat`. The order of the parts and the names follow the language only.
