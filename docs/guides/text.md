# Text

Controls show `Text`: characters with styles, which may be translated. The engine adapter picks the font, draws the text and measures it. The same text is therefore a little wider on one engine than on another, and layouts should never assume character widths.

## Text

`Text` is what captions, titles, messages, placeholders and tooltips take. Every setter that takes one also takes a `String`, so plain text needs nothing new:

```java
button.setText("Buy");
button.setText(Text.translatable("shop.buy"));
```

Text has no style of its own until a control shows it. The control resolves it against the style it would draw plain text in, its base, which usually comes from the theme. `Text.of("Ana")` draws in the base. `color`, `bold`, `italic`, `underline`, `strikethrough` and `style(change)` change the base for the text they are called on, so parts that set the same property keep theirs. `Text.of(text, style)` ignores the base and draws in exactly that style.

```java
--8<-- "text/TextExamples.java:styled"
```

`Text.builder()` returns a `Text.Builder`. Its `append` adds text, or characters in the base or in a style of their own, and `color`, `bold` and `italic` add characters with that change to the base. `build()` returns the parts added so far as one text, and the builder can go on adding after it.

Text is immutable, and two texts built the same way are equal. Read the characters with `plain()`, for searching, sorting and narration, and check for none with `isEmpty()`. The static methods on `Text` go through a `TextFactory` that Strata finds on the class path the first time one is used, so text can be made in a static field before any screen exists.

### Translated text

`Text.translatable(key, args...)` looks the key up in the current language each time a control lays out, so a screen follows a language change without code of its own. The translation is a pattern: `%s` stands for the next argument, `%1$s` for the first, and `%%` for a percent sign. A text argument keeps its own styles, and any other argument shows as `String.valueOf`.

```java
--8<-- "text/TextExamples.java:translated"
```

Where the translation comes from depends on the platform. A game's adapter usually reads the game's own language files, so keys go wherever the game keeps its translations. On Swing, set a `ResourceBundle` with `SwingTranslations.use`; see [Swing](../platforms/swing.md#translations). A key with no translation shows the key itself, which makes missing ones easy to spot, and the log gets one warning per missing key. Strata's own controls use keys too, listed in [Translation keys](../reference/translation-keys.md), with English built in.

### Plurals

"1 offer" and "3 offers" need their own translations, and some languages have more than two forms. `Text.plural(key, count, args...)` picks the translation by the language's rules for the count:

```java
--8<-- "text/TextAdditionsExamples.java:plural"
```

The translator names the count's `PluralCategory`, one of `ZERO`, `ONE`, `TWO`, `FEW`, `MANY` and `OTHER`, and the text reads the translation of `key.<category>`, such as `shop.offers.one`. If the language has no entry for that category it reads `key.other`, and if that is missing too the text shows `key.other` itself, with one warning in the log. The translation is a pattern like any other, so `%s` stands for the first argument, here the count. The English entries are:

```text
shop.offers.one=%s offer
shop.offers.other=%s offers
```

A Russian file adds `shop.offers.few` and `shop.offers.many`, and an Arabic one all six. Like `translatable`, the text resolves on every layout, so it follows a language change, and two plural texts are equal when their key, count and arguments are.

Core carries the cardinal rules of the Unicode CLDR for whole numbers, so counts need no code of yours: the languages with a single form, the languages that use one form for 0 and 1, and the ones with two, three, four or six forms, such as the Slavic languages, Arabic, Hebrew, Irish, Welsh and Maltese. A language without rules of its own uses English's. `PluralCategory.of(locale, count)` gives the category directly, for code that needs it, and a negative count takes the category of its absolute value:

```java
--8<-- "text/TextAdditionsExamples.java:category"
```

An adapter whose engine has its own plural handling overrides `Translator.plural(long)`; a translator that throws or returns null counts as `OTHER`.

### Operations

Every operation returns new text and runs each time the text is resolved, so it works on translated text in any language.

| Operation | Does |
|---|---|
| `append(text)`, `Text.concat(parts)` | Joins texts one after another |
| `Text.join(separator, parts)` | Joins texts with a separator between each two |
| `mapSpans(mapper)` | Changes every resolved span, after each part has its style |
| `styleMatches(pattern, change)` | Restyles the characters a regular expression matches, across parts |
| `replace(target, replacement)` | Replaces every occurrence; the replacement takes the style of where it lands |
| `trim()`, `normalizeWhitespace()` | Removes whitespace at the ends, or turns each run of it into one space |
| `splitByLines()`, `splitByWords()` | Splits into lines or words. How many there are is decided when you call it. |

```java
--8<-- "text/TextExamples.java:matches"
```

```java
--8<-- "text/TextExamples.java:join"
```

Tooltips take a list of texts, one per line:

```java
--8<-- "text/TextExamples.java:tooltip"
```

### Pictures in text

`Text.visual(visual)` puts an engine visual in a line of text at its natural size. `Text.sprite(sprite)` puts a sprite there as a square as tall as the line, so it sits in the text like a character, and `Text.sprite(sprite, width, height)` gives it a size of its own. Both are drawn untinted.

```java
--8<-- "text/TextExamples.java:inline"
```

`Text.sprite(sprite, width, height)` needs a positive size; a square that fits the line comes from `Text.sprite(sprite)` alone.

Labels, buttons, tooltips and toasts make each line tall enough for the pictures in it, and center the characters and each picture on the line. Controls whose rows have a fixed height, such as menus, keep it, so keep pictures there no taller than the text. A picture is never cut in two: when a label shortens a line with `...`, a picture that does not fit gives way to the dots.

To text operations a picture is one character, U+FFFC, the object replacement character (`TextSpan.OBJECT_REPLACEMENT`), which is also what `plain()` returns for it. It is not whitespace, so `trim` keeps it and wrapping keeps it next to the characters it touches.

A resolved picture is a span of its own whose `element()` holds an `InlineElement`: an `InlineSprite` with the sprite and its size, where 0 by 0 means it fits the line, or an `InlineVisual` with the visual. `isInline()` tells such a span from characters. `mapSpans` sees these spans too; `withStyle` keeps the picture, but `withText` turns the span into plain characters unless the new text is still U+FFFC, so leave picture spans alone when you change characters:

```java
--8<-- "text/TextExamples.java:inline-spans"
```

## TextStyle

A record of how a span of text looks:

| Field | Default (`TextStyle.DEFAULT`) |
|---|---|
| `color` | `0xFFFFFFFF`, opaque white. Alpha 0 draws nothing. |
| `bold`, `italic`, `underline`, `strikethrough` | off |
| `shadow` | off. A darker copy one unit down and to the right. |
| `monospace` | off. The engine's monospaced font, where every character has the same width, for code and columns. An engine without one uses its normal font; its platform reports `Capabilities.monospaceFont` false. |

`with` methods return modified copies:

```java
TextStyle heading = TextStyle.DEFAULT.withColor(0xFFFFD54F).withBold(true).withShadow(true);
```

`merge` takes the properties you name from another style and keeps the rest. Naming them is what lets a merge turn bold or a shadow off as well as on, since `false` is a value like any other:

```java
// The theme's style with the accent's color and weight; the theme's shadow stays
TextStyle accent = themeStyle.merge(heading, TextStyle.Property.COLOR, TextStyle.Property.BOLD);
```

Merging every property gives the other style, and merging none gives a style equal to this one.

Engines draw what they can:

| Style | Swing |
|---|---|
| Bold | A bold font derived from the base font |
| Italic | An italic font derived from the base font |
| Underline, strikethrough | Drawn by the adapter |
| Shadow | Drawn by the adapter |
| Monospace | The logical Monospaced font at the base font's size |

Other adapters use the engine's own text styles where it has them.

## TextSpan

A `TextSpan` is a string with a `TextStyle`, the unit engines draw, or a [picture](#pictures-in-text) in the style of the text around it. `Text.resolve(base)` turns text into spans, joining neighbors of equal style and leaving out empty ones. `TextSpan.of(text)` uses the default style, `TextSpan.of(element, style)` holds a picture, and `withText` and `withStyle` make copies.

As far as engines are concerned, a span is one line. Break text into lines before drawing it, as `Label` does.

## Measuring

`TextMetrics` measures text the way the engine draws it:

| Method | Returns |
|---|---|
| `width(span)` | How wide the span is drawn, in UI units. 0 for empty text. |
| `lineHeight(style)` | The distance from one line's top to the next line's top |
| `ascent(style)` | The distance from a line's top to its baseline, positive and no more than the line height. The default is four fifths of the line height; adapters report their font's own. |

Components get it from `MeasureContext.text()` during measure and from `environment().text()` anywhere else. The widths match drawing, so a text box can place its caret between characters. These metrics also measure pictures in text: a span that holds one is as wide as the picture. `lineHeight` describes a style, so it leaves pictures out; a line with a picture taller than its text needs room for the picture's height too.

| Engine | Widths | Line height |
|---|---|---|
| Swing | Fractional advance widths of the AWT font | The font's height: ascent, descent and leading. The ascent is the font's own. |

Engines with pixel fonts often report whole-pixel widths, and some make bold text wider.

The ascent is what lets a layout put the text of different controls on one line. A component that draws a line of text with its top at `y` has its baseline at `y` plus the ascent of the line's style; see [Lining up text](layout.md#lining-up-text).

## Drawing text in a custom component

```java
@Override
protected Size measure(MeasureContext context, float availableWidth, float availableHeight) {
    this.spans = this.caption.resolve(this.style);
    float width = 0;
    for (TextSpan span : this.spans) {
        width += context.text().width(span);
    }
    this.lineHeight = context.text().lineHeight(this.style);
    return new Size(width, this.lineHeight);
}

@Override
protected void paint(Painter painter, Rect bounds) {
    float x = bounds.x();
    for (TextSpan span : this.spans) {
        painter.text(span, x, bounds.y());
        x += this.environment().text().width(span);
    }
}
```

`Painter.text(span, x, y)` puts the top-left corner of the line box at the point: `y` is the top of the line, not the baseline. A span that holds a picture draws the picture with its top at `y`. Resolve text during measure and keep the spans for painting, so translations are read once per layout and painting does not allocate. Hosts lay every component out again when the language changes. The [switch example](extending.md#write-a-new-component) does this.

## Wrapping

`Label` wraps greedily by word when wrapping is on: it fills each line with as many words as fit the available width, and breaks a single word wider than a whole line between characters. It always breaks at `\n`. See [Wrapping text](layout.md#wrapping-text) for giving it a width.

Labels, tooltips, toasts, info bars and text areas all break by one rule. A row takes as much of its line as fits and breaks after the last whitespace that fits, where whitespace is any character `Character.isWhitespace` accepts, tabs included. A no-break space never breaks. The whitespace where a row breaks stays at the end of that row: it is not drawn and does not count toward the row's width, and a text area can put the caret in it. Whitespace anywhere else, at the start of a line included, is kept as written, so indentation survives wrapping. A word wider than a whole row breaks between grapheme clusters, never inside one, and a row always takes at least one cluster.

A grapheme cluster is what a reader sees as one character: a letter with its accent marks, an emoji with its modifiers, a flag. Wrapping, the `...` of `TextOverflow.ELLIPSIS`, a field's length limit, the caret and selections all keep clusters whole. `maxLength` counts code points, and a cut to it leaves out a cluster that would cross the limit. A password box steps by code points.

Wrapping belongs to the controls and is not a public utility. A custom component that needs wrapped text can measure and break the text itself, or contain a wrapping label.

## Mixed styles

A label draws styled text one part after another, each in its own style, and wraps it by the same rules, so a piece of a part keeps its style on any line. Use it for text like a white item name followed by a gray count, or a gold price followed by `/ea`. `setTextScale` draws a label larger or smaller, and `TextOverflow.ELLIPSIS` ends a line that does not fit with `...`, in the style of the last character kept. See [Label](controls.md#label).

## Styled text in fields

`TextBox` and `TextArea` draw their text in one style from the theme, unless they have a highlighter. A `TextHighlighter` gets the field's whole text and the style the field would draw it in, and returns `StyleRun`s: ranges of the text, each with its own style. The field draws each run in its style and measures each in its own style, so the caret and the selection line up with styled text.

```java
--8<-- "controls/ControlExamples.java:highlighter"
```

- Derive each run's style from `base`, with the `with` methods or `merge`, so highlighted text keeps what the theme gives the field, such as a shadow.
- Return runs in order and not overlapping. A run that breaks that, or runs past the end of the text, is ignored, and text no run covers draws in `base`.
- The field calls the highlighter when its text or its base style changes, from code or the user, and never while painting. Keep it a quick, pure function of its arguments.
- A disabled field ignores the runs and draws in its disabled style. A read-only field keeps them. Runs do not change the line height. A password box has no highlighter.

Strata ships no highlighters. The [gallery](https://github.com/InspireNXE/Strata-Gallery) has a small Java one, `JavaHighlighter`, as sample code: it colors keywords, strings, comments, numbers and annotations, and makes the whole text monospaced so indentation lines up.

## Text from the engine

An engine adapter can turn the engine's own text, such as a translated engine message, into `Text`. Such text reads the engine's text anew each time a control lays out, so it follows the engine's language, and every operation on this page works on it. How you get it depends on the adapter; see its documentation. Adapter authors wrap engine text with a `TextSource`, described in [Writing an adapter](../platforms/writing-an-adapter.md#starting-up).

## Fonts

Strata has no font API, so each adapter picks its fonts. Swing draws with the fonts given to `SwingTextMetrics`, AWT fonts or a `BitmapFont` cut from images. `SwingTextMetrics.defaults()` is the logical Dialog font at 9 units. See [Swing](../platforms/swing.md#fonts). Other adapters usually draw with the engine's own font.

## Right to left text

Hebrew and Arabic read from the right, and a line that mixes them with English reads in both directions. Core orders each line you draw by the Unicode bidirectional algorithm, so captions, menu items, combo boxes, placeholders and the lines of a label all draw in reading order, with no code of your own. The text you hand over stays in logical order: `plain()`, `Text.append` and searches see the characters as typed.

- The paragraph direction of a control comes from its [flow direction](layout.md#right-to-left). A caption such as "שלום Ana" has the Hebrew first and at the right in a right to left panel, and the English name after it.
- `LEFT` text alignment means the start of the line, so a label's text starts at the right in a right to left panel.
- Wrapping and ellipsis work on the logical text, as the algorithm requires, and each wrapped line is then put in reading order.
- An engine that shapes right to left text itself, as Swing does, draws each run joined and in order. An engine with a simple font gets the run with its characters in reverse order and its brackets mirrored. That reads correctly for Hebrew, but Arabic letters draw unjoined there. [Platform differences](../platforms/differences.md) says which adapters do what.

[Internationalization](internationalization.md) collects the rest: mirrored controls, numbers in the language's digits, and plurals.

## Not supported yet

- Markup such as BBCode. Avoid an engine's own formatting codes in text too: Strata does not interpret them, and the engine's text drawing may, so the same text would look different on each engine.
- Joined Arabic and other complex scripts on an engine whose text drawing cannot shape them. Core orders such text but cannot join its letters.
