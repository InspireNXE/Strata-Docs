# Themes

A theme decides how components look. It maps style keys, such as `button.hovered`, to paints and text styles. Every component in one host shares the host's theme.

## Built-in themes

| Theme | Where | Look |
|---|---|---|
| `Themes.flat()` | `api` | Dark, solid colors only. Works on any engine without image assets. |

It defines every key of every built-in control. Adapters may ship themes of their own that use the engine's widget images.

![The flat theme](../assets/images/theme-flat.png)

## Deriving a theme

Themes are immutable. `toBuilder()` starts a builder holding all of a theme's keys, so a variant only lists what changes:

```java
--8<-- "theming/ThemeExamples.java:derive"
```

![The flat theme with green buttons](../assets/images/theme-derived.png)

## A theme from scratch

`Theme.builder(name)` starts empty. The name appears in error messages.

```java
--8<-- "theming/ThemeExamples.java:scratch"
```

![A light theme built from nothing](../assets/images/theme-light.png)

Labels and panels have no backgrounds in any theme. Give a panel one with `setBackground`, and put a label in a panel to give it one.

## Required and optional keys

Each control lists the keys it reads as `STYLE_*` constants on its interface. Some are required, and the rest are optional with a documented fallback. The [theme key reference](../reference/theme-keys.md) lists them all.

A host checks the theme before it shows anything. When it opens a screen, it collects the required keys of every component in the tree, and if the theme lacks any of them it throws a `ThemeException` that lists every missing key at once, so one run shows everything to add. The same check runs when a component is added to a tree that is already on screen, and when the host's theme changes.

```java
--8<-- "theming/ThemeExamples.java:missing"
```

Your own components declare required keys through `requiredStyleKeys()`. Remember to add them to the theme you use, as the [switch example](extending.md#write-a-new-component) does.

A key may have a paint, a text style, [insets](#insets), or any mix; `has(key)` is true for any of them. Look keys up with `paint(key)`, `textStyle(key)` and `insets(key)`, which return `Optional`, or with `paintOr`, `textStyleOr` and `insetsOr`, which take a fallback.

## Paints

A `Paint` fills a rectangle. There are five kinds:

| Factory | Record | Draws |
|---|---|---|
| `Paint.solid(argb)` | `Paint.Solid` | One color |
| `Paint.gradient(top, bottom)` | `Paint.Gradient` | A vertical gradient |
| `Paint.sprite(id)` | `Paint.Sprite` | An image stretched over the rectangle |
| `Paint.tiled(id, size)` | `Paint.Tiled` | An image repeated from the top left corner, one copy per `size` |
| `Paint.nineSlice(id, border)`, `Paint.nineSlice(id, size, border)`, `Paint.tiledNineSlice(id, size, border)` | `Paint.NineSlice` | An image cut into a 3 by 3 grid |

`Paint.NONE` draws nothing; it is the default background. The sprite factories make untinted paints. To tint, use the record constructor with an ARGB tint, which is multiplied with the image's pixels: `new Paint.NineSlice(id, border, 0xFFC0C0C0)` darkens it slightly. Opaque white leaves an image unchanged.

`paint.paint(painter, rect)` draws any paint, so custom components need not care which kind they hold.

### Colors

Colors are ARGB ints, `0xAARRGGBB`. The alpha byte comes first: `0xFF000000` is opaque black, and `0x80FFFFFF` is half-transparent white.

A color with alpha 0 draws nothing, so be careful with `0xRRGGBB` literals: `0xFF0000` has alpha 0 and is invisible. Write `0xFFFF0000` for opaque red.

### Sprites

A `SpriteId` names an image as `namespace:path`:

```java
--8<-- "theming/ThemeExamples.java:sprites"
```

Each adapter decides where the image comes from. The Swing adapter reads it from the classpath, by default at:

```text
assets/<namespace>/sprites/<path>.png
```

A `SpriteResolver` given to `SwingSprites` maps ids to any other layout. A missing image draws as magenta.

`SpriteId.parse("examplemod:button")` parses the text form. A missing namespace is an error, so a sprite never lands in another namespace by accident.

### Nine-slices

A nine-slice keeps its corners at their size, stretches its edges along one axis and its center both ways, so one small image can frame a rectangle of any size. With `Paint.nineSlice(id, border)`, `border` gives the width of each fixed edge in the image's pixels, and the image draws at one pixel per unit.

Give a size to say how many units the whole image stands for. `border` is then in those units, so an image with twice the pixels draws the same frame, only sharper. `Paint.tiledNineSlice` also repeats the edges and the center at their size instead of stretching them, and crops the last copy. `Paint.tiled` repeats a whole image the same way:

```java
--8<-- "theming/ThemeExamples.java:sized"
```

When a rectangle is smaller than the borders, they shrink in proportion. The borders must fit in the size, and a tiled nine-slice needs one. Strata draws sized and tiled paints itself, from pieces of the image, on every engine except one that stores slicing with its images, which uses its own. Keep the two in agreement so every engine draws the same.

## Text styles

A `TextStyle` sets color, bold, italic, underline, strikethrough, a shadow and the monospaced font. Themes map text keys to styles; labels can also take one directly. See [Text](text.md).

## Insets

Paints and text styles say what a control looks like. Insets, the third kind of theme value, say where it puts its content, in UI units. A tab bar reads `tab.padding` for the space around an unselected tab's text and `tab.padding.selected` for the selected tab's, so a theme whose unselected tab image is drawn shorter than the tab can move that tab's text down to match:

```java
Theme theme = Themes.flat().toBuilder()
        .insets(TabBar.STYLE_PADDING, new Insets(8, 5, 8, 2))
        .build();
```

`theme.insets(key)` and `insetsOr(key, fallback)` look them up, and `ThemeRef.override(key, insets)` overrides them like paints. A control that reads insets for several states sizes itself for the largest, so selecting a tab never moves its neighbors. [Theme keys](../reference/theme-keys.md) lists the insets keys with their defaults.

## Per-component overrides

Backgrounds and label text styles can be set per component, outside the theme:

```java
--8<-- "theming/ThemeExamples.java:local"
```

There is no per-component theme or per-subtree theme. To give one button a different face, cancel its `Component.PAINT` and draw something else, or write a component that reads its own keys.

## Themes by id

A `Theme` built in code is fixed. To let players restyle your screens with packs, name the theme by id instead and hand the host a `ThemeRef`:

```java
ThemeRef theme = ThemeRef.of(ThemeId.of("mymod", "shop"))
        .override(Button.STYLE_NORMAL, Paint.solid(0xFF2E7D32));
```

The host resolves the ref when it opens. It starts from the theme registered under the id, applies every theme file the engine finds for that id, and applies the ref's overrides last. `Themes.FLAT` (`strata:flat`) is registered on every engine, and adapters register their engine themes under ids of their own. An id with nothing registered is defined by its files alone and names its starting theme as `parent`.

When the engine reloads its assets, every host with a ref resolves it again and keeps the overrides. A reload whose result lacks a key the open screen needs is logged and ignored, so the screen keeps its old theme.

### Theme files

A theme file sets only the keys it changes. Where the files live depends on the engine: Swing reads `assets/<namespace>/themes/<path>.json` from the class path. A file whose id has a registered theme changes that theme key by key, and when several packs ship the same file, the higher-priority pack wins per key.

```json
{
  "parent": "strata:flat",
  "paints": {
    "button.normal": { "nine_slice": "mymod:button", "border": [3, 3, 3, 3] },
    "button.hovered": { "sprite": "mymod:button_hover", "tint": "#FFE0E0FF" },
    "button.focus": "none",
    "list.row": "#1F2024",
    "window.background": { "gradient": ["#F0101010", "#F0202020"] }
  },
  "text": {
    "button.text": { "color": "#FFFFFF", "shadow": true }
  },
  "insets": {
    "tab.padding": [8, 5, 8, 2]
  }
}
```

| Value | Written as |
|---|---|
| Color | `"#RRGGBB"` (opaque) or `"#AARRGGBB"` |
| No paint | `"none"` |
| Sprite | `{ "sprite": "ns:path" }`, with an optional `"tint"` color |
| Nine-slice | `{ "nine_slice": "ns:path", "border": ... }`, with an optional `"tint"`, `"size": [width, height]` and `"tiled": true`, which needs a size. The border is one number, `[horizontal, vertical]` or `[left, top, right, bottom]` |
| Tiled sprite | `{ "tiled": "ns:path", "size": [width, height] }`, with an optional `"tint"` |
| Gradient | `{ "gradient": [top, bottom] }` |
| Text style | Any of `color`, `bold`, `italic`, `underline`, `strikethrough`, `shadow` and `monospace`. Fields you leave out keep the inherited style for that key |
| Insets | One number, `[horizontal, vertical]` or `[left, top, right, bottom]`, in UI units |

A misspelled member, a bad color or broken JSON makes the whole file invalid. The engine logs it and skips that file, and the other packs still apply.

## Changing the theme at run time

The host owns the theme. Adapters can change it with `ScreenHost.setTheme` or `ScreenStack.setTheme`, which take either a `Theme` or a `ThemeRef`; components then re-read their keys in `onApplyTheme` and a new layout runs. See [Screens](screens.md#changing-the-theme).
