# Internationalization

Strata handles five things that go wrong for users of other languages: screens mirror for languages written from the right, mixed Hebrew and English text draws in the right order, "1 offer" and "3 offers" can have translations of their own, a spinner takes "2,5" from a German user, and labels and length limits never cut a letter from its accent. Most need no code. This page says what happens, what you can set, and what your own components must do.

Translated text itself is described in [Text](text.md#translated-text), and the keys Strata's controls use are listed in [Translation keys](../reference/translation-keys.md).

## The language

Everything on this page follows the translator's locale. A translator is the platform's source of translations (`Translator` in the SPI, set on Swing with `SwingTranslations.use`), and `Translator.locale()` is its language. When the language changes, the translator's `version()` changes, and every host lays out again on its next frame, so a screen follows the change without code of its own. Direction, plural forms, number formats and date formats are all worked out from that one locale again at that point.

## Right to left

`FlowDirection` (`org.inspirenxe.strata.api.layout`) is `LEFT_TO_RIGHT` or `RIGHT_TO_LEFT`.

| Call | Does |
|---|---|
| `component.flowDirection()` | The component's own direction if it has one, else the nearest ancestor's. |
| `component.setFlowDirection(direction)` | Sets the component's own direction. `null` follows the parent again. |
| `FlowDirection.of(locale)` | The direction a locale is written in. |
| `direction.isRightToLeft()` | Whether it is `RIGHT_TO_LEFT`. |

`FlowDirection.of` is right to left for a locale whose script is Arabic, Hebrew, Thaana, Syriac, N'Ko, Adlam, Hanifi Rohingya, Mandaic, Samaritan, Mende Kikakui or Yezidi. With no script in the locale, it goes by the language: Arabic, Hebrew, Persian, Urdu, Yiddish, Pashto, Sindhi, Sorani, Uyghur, Dhivehi and a few more are right to left. So `az-Arab` is right to left and `az` is not.

Nothing needs to be set for the common case. At the top of a screen the direction is the one of the screen's content if you set one, and otherwise the direction of the translator's language. Overlays follow the screen: a popup, a dialog or a toast opened from a right-to-left screen is right to left. Set a direction on one component to force it for that part, such as a code sample that must stay left to right inside an Arabic screen. Setting it lays out the component and everything in it again.

```java
--8<-- "internationalization/InternationalizationExamples.java:direction"
```

With Hebrew as the translator's language, that row puts Back at the right. The test for this example checks the positions.

### How layout mirrors

Layout works out each child's place as for left to right, then mirrors the result inside the parent's bounds when the parent runs right to left. Every panel therefore mirrors with no code of its own. `LEFT` and `RIGHT` alignment, margins, padding and the positions in an `AbsolutePanel` mean the start and the end. An offset is added after the mirror and is not mirrored. Bounds stay real coordinates, so hit testing and painting need nothing new. See [Right to left](layout.md#right-to-left) in the layout guide for the rules.

Text, sprites and visuals draw unmirrored. A picture of an arrow stays as drawn, and a label's letters are not flipped.

### What the controls do

The built-in controls mirror their insides as well:

| Control | Right to left |
|---|---|
| `ScrollPanel` | The viewport and both bars mirror. The horizontal thumb starts at the right, an offset of 0 shows the content's right edge, and Left, Right and a sideways wheel move toward the side they name. Shift with the wheel still moves toward the end. |
| `Label` | `LEFT` text alignment puts lines at the right, so `LEFT` means the start. |
| `Button`, `DropDownButton` | The icon sits after the caption, and the drop-down arrow is at the left. |
| `CheckBox`, `RadioButton`, `ToggleSwitch` | The box is at the right edge and the caption before it. A switch is on at the left. |
| `ComboBox` | The text is at the right and the arrow at the left. |
| `TextBox`, `PasswordTextBox`, `TextArea`, the spinner's field | `LEFT` alignment is the start, and the text padding mirrors. Left moves the caret toward the end of the text and Right toward its start. A password box's eye moves to the left, and a text area's bar to the left. |
| `Spinner` | The buttons are at the left. |
| `Slider` | A horizontal slider rises toward the left, by pointer and by key. |
| `ProgressBar` | The fill and the indeterminate segment run from the right. |
| `Menu` | Text, check marks, hints and arrows are at the other side. Submenus open to the left, and Left opens a submenu while Right goes back. |
| `MenuBar` | The first title is at the right, and Left and Right step the other way. |
| `TabBar` | Tabs run from the right, and Left selects the next tab. |
| `TreeView` | Rows indent from the right, closed arrows point left, Left expands and Right collapses. |
| `ListView`, `GridView` | The scroll bar is at the left. Row backgrounds and the drop line mirror. The grid swaps Left and Right. |
| `ScrollBar` | A horizontal bar starts at its right end, and pages and steps go the other way. |
| `SplitPane` | A side-by-side split has its first side at the right, and its divider drags and steps the matching way. |
| `Expander` | The arrow is at the right, pointing left while closed, and the title is before it. |
| `NavigationView` | The pane is at the right. Rows mirror their indicator, icon, text, badge and arrow. The pane over the content opens from the right, and Left expands. |
| `Window` | The close button is at the left of the title bar. |

A popup is placed with its right edge on its anchor's right edge while it runs right to left, so a combo box's list lines up with the box, and a menu opened at the pointer opens to its left. Toasts stack against the left edge, and the drag image trails the pointer on its left.

### In a component of your own

A component that places its own parts reads `flowDirection()` and mirrors them. Containers you write with `CustomComposite` or `CustomContainer` arrange their children as usual and get the mirroring from the framework. A component that paints and has no children has to decide where its parts go:

```java
--8<-- "internationalization/InternationalizationExamples.java:own"
```

Keys follow the same rule: a control that moves with Left and Right swaps them while it runs right to left.

## Text that mixes directions

A line holding Hebrew with an English name inside it, such as a greeting followed by Ana, has runs of both directions. Strata orders each line of drawn text with the Unicode bidirectional algorithm and draws the runs in visual order. Captions, menu items, combo boxes, placeholders, labels, tooltips and the other controls that draw text all follow. The paragraph's direction is taken from the first letter that has a direction, or from the control's own direction where it has one.

Text stays in logical order everywhere your code sees it: `text()`, `length`, a caret's index and a selection all count in the order the user typed. Wrapping and ellipsis work on logical text, as the algorithm requires, and only the drawing reorders.

What the engine does for a right-to-left run depends on the platform. An engine that shapes right-to-left text itself (Java2D does) receives the run in logical order and joins Arabic letters. An engine that only draws code points left to right gets the run reversed by Strata, with its paired brackets mirrored: Hebrew reads correctly, and Arabic draws unjoined. See [Platform differences](../platforms/differences.md).

## Plurals

"1 offer" and "3 offers" differ, and in other languages the number of forms differs too. Russian has three for whole numbers, Arabic six, and Japanese one. `Text.plural(key, count, args...)` picks the form from the translator's language:

```java
--8<-- "internationalization/InternationalizationExamples.java:plural"
```

The text resolves on every layout, as translatable text does. The translator's `plural(count)` gives the category, and the text reads the translation of `key.<category>`, falling back to `key.other`. If neither exists it shows `key.other` as the text and logs one warning. The arguments are formatted as for `Text.translatable`, so `%s` stands for the next argument, and the count is usually the first argument too. English needs two entries:

```java
--8<-- "internationalization/InternationalizationExamples.java:english"
```

Russian needs more, and the translator decides which exist:

```java
--8<-- "internationalization/InternationalizationExamples.java:russian"
```

The categories are `PluralCategory.ZERO`, `ONE`, `TWO`, `FEW`, `MANY` and `OTHER`, written in keys as `zero`, `one`, `two`, `few`, `many` and `other` (`category.suffix()`). `PluralCategory.of(locale, count)` works out the category of a count in any locale. `Translator.plural(long)` defaults to that, so a translator only overrides it when its bundle carries rules of its own.

Strata ships the CLDR cardinal rules for whole numbers:

- no plural forms: Japanese, Chinese, Korean, Thai, Vietnamese and others,
- one for 0 and 1: French, Brazilian Portuguese, Hindi and others,
- the Spanish, Italian, Catalan, French and Portuguese rules, including "many" for whole millions,
- the Slavic three- and four-form rules, Lithuanian, Latvian, Romanian and Slovenian,
- Arabic's six forms, Hebrew, Irish, Scottish Gaelic, Welsh and Maltese.

Any other language uses English's rule. A negative count takes the category of its absolute value, and the categories that only fractions reach never come out. A translator that throws or returns null counts as `other`. Plural text is equal when its key, count and arguments are.

## Numbers and dates

A spinner shows its value in the language's digits, decimal separator and minus sign, without grouping: a German user sees 2,5, and an Arabic user may see Arabic-Indic digits. It reads them back too: its filter accepts ASCII digits and the locale's digits, the locale's separator or a period, and the locale's minus sign or a hyphen. A period is accepted as a decimal separator in every language. If the language changes while a spinner shows, it builds its filter again and shows the value in the new language.

The number converters in [Data binding](binding.md#converters) write and read through the same code, so a text box bound with `Converters.decimal` shows a value the same way a spinner does.

Month and weekday names, the first day of the week, the order of a date's parts and the 12 or 24 hour clock follow the translator's locale in the date and time controls. Any of them can be overridden with a translation key. See [Dates and times](controls.md#dates-and-times) and the [translation keys](../reference/translation-keys.md#dates-and-times).

## Characters that belong together

A letter with an accent can be one code point or two, and an emoji can be many. Strata treats a grapheme cluster, what a reader sees as one character, as the unit wherever text is cut or stepped through:

- Labels, tooltips, toasts and info bars break a word that is too wide for its row between clusters.
- Ellipsis keeps whole clusters, so it never cuts a letter from its accent.
- Text fields step the caret by cluster, Backspace removes a whole cluster, and indexes that code passes to `select`, `insert` and the other edit methods snap to the start of the cluster they land in, so a caret or a selection never lands inside one. A password box steps by code point.
- `maxLength` counts code points, and every cut to it, whether typed, pasted or set from code, leaves out a cluster that would cross the limit whole.

## What an adapter reports

An adapter needs nothing to keep working. Two optional things improve right-to-left text:

- `Capabilities.shapesRightToLeft` is true when the engine orders and joins right-to-left text itself. Core then calls `Canvas.text(span, x, y, true)` for each run it put right to left, with the run in logical order, and the engine draws it as wide as `TextMetrics.width` measures it. When false, core reverses the run and mirrors its brackets, and calls the plain `text`.
- `Translator.plural(long)` may follow the engine's own plural rules if it has some.

See [Writing an adapter](../platforms/writing-an-adapter.md).

## Testing

A test picks a language through `TesterOptions.withTranslator`, and the screen then mirrors and pluralizes as it would for a user. The plural example runs that way:

```java
--8<-- "internationalization/InternationalizationExamplesTest.java:test"
```

See [Testing](testing.md).
