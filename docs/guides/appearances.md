# Appearances

An appearance says how values of one type look to a user: an icon, a name and a tooltip. Register one per type, and every list, tree, grid, combo box and table text column shows those values with their icon, name and tooltip, with no cell factory.

```java
--8<-- "appearances/AppearanceExamples.java:register"
```

```java
--8<-- "appearances/AppearanceExamples.java:use"
```

## Registering

`Strata.appearances()` is the registry for the whole process, so you can register while your project starts, before any screen exists. Inside a screen, `ui.appearances()` returns the same registry.

| Part | Default |
|---|---|
| `icon(value)` | None |
| `name(value)` | `String.valueOf(value)` as text |
| `tooltip(value)` | None. Asked anew each time the pointer arrives, so it can follow settings that change. |

`Appearance.builder()` makes one from functions. `Appearance.as(mapping)` makes a type look like another value it leads to, such as a listing like its item. You can also implement `Appearance` yourself.

`register` returns a `Subscription`. A later registration for the same type hides an earlier one until you remove it, so you can replace an engine adapter's default look for a type.

A value takes the appearance registered for its class, else for its nearest superclass with one, else for the nearest of its interfaces with one. Of two interfaces at the same distance, the one registered first wins. A value with nothing registered has no icon and no tooltip, and is named by `String.valueOf`.

Strata registers `Text`, named by itself, and `Visual`, whose icon is itself. The [Swing adapter](../platforms/swing.md) registers `BufferedImage`. Other adapters register their engine's types, such as items.

## Where appearances show

| Control | What it shows |
|---|---|
| `ListView`, `TreeView`, `GridView` made without a cell factory | `factory.cell(item)`: the icon, then the name in a cell label, and the tooltip |
| `ComboBox` | The name of what the label function returns, the option itself by default |
| `CycleButton` | The name of what the value label function returns, the value itself by default |
| `TableView` text columns, `TableColumn.setCellText` | The name of what the function returns; the column sorts by it, ignoring case |
| `Image.setValue(value)`, `factory.image(value)` | The icon at its natural size, and the tooltip until you set another |

Label functions take any result: a `String`, a `Text`, or a value with an appearance of its own. Use `factory.cell(value)` in your own cell factories to keep the default row and add to it.

A control that only shows a value differently, such as a slot with an item and its price, is a few lines with `CustomComponent`, `Painter.visual` and `appearances()`; see [Extending Strata](extending.md).
