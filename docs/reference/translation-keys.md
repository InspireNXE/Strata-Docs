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

`MessageBox.setButtonText` replaces a button's text with your own.

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
