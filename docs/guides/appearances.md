# Appearances

An appearance says how values of one type look to a user: an icon, a name and a tooltip. Register one per type, and every list, tree, grid, combo box and table text column shows those values with their icon, name and tooltip, with no cell factory.

```java
--8<-- "appearances/AppearanceExamples.java:register"
```

```java
--8<-- "appearances/AppearanceExamples.java:use"
```

## Registering

`Strata.appearances()` returns the `Appearances` registry for the whole process, so you can register while your project starts, before any screen exists, from any thread. Inside a screen, `ui.appearances()` returns the same registry.

| Part | Default |
|---|---|
| `icon(value)` | None |
| `name(value)` | `String.valueOf(value)` as text |
| `tooltip(value)` | None. Asked anew each time the pointer arrives, so it can follow settings that change. |

`Appearance.builder()` makes one from functions. The icon and tooltip functions may return null for none, and the name function must return a name. Parts you leave unset keep the defaults above. `Appearance.as(mapping)` makes a type look like another value it leads to, such as a listing like its item, and looks that value's appearance up anew each time.

You can also implement `Appearance` yourself. Each method must be a pure function of the value as it is at the call. Controls call them often, on the UI thread, so keep them quick. An icon made from a mutable object should copy it; see [Visuals and icons](#visuals-and-icons).

`register` returns a `Subscription`. A later registration for the same type hides an earlier one until you remove it, so you can replace an engine adapter's default look for a type.

A value takes the appearance registered for its class, else for its nearest superclass with one, else for the nearest of its interfaces with one. Interfaces are searched breadth first: those the class and its superclasses implement directly, then the interfaces those extend. Of two interfaces at the same distance, the one whose type was registered first wins. A value with nothing registered has no icon and no tooltip, and is named by `String.valueOf`. Lookups are cached per class until a registration changes.

Strata registers `Text`, named by itself, and `Visual`, whose icon is itself. The [Swing adapter](../platforms/swing.md) registers `BufferedImage`. Other adapters register their engine's types, such as items.

## Where appearances show

| Control | What it shows |
|---|---|
| `ListView`, `TreeView`, `GridView` made without a cell factory | `factory.cell(item)`: one cell label with the icon as a [picture in the text](text.md#pictures-in-text), then the name, and the tooltip |
| `ComboBox` | The name of what the label function returns, the option itself by default |
| `CycleButton` | The name of what the value label function returns, the value itself by default |
| `TableView` text columns, `TableColumn.setCellText` | The name of what the function returns; the column sorts by it, ignoring case |
| `Image.setValue(value)`, `factory.image(value)` | The icon at its natural size, and the tooltip until you set another |

Label functions take any result: a `String`, a `Text`, or a value with an appearance of its own. Use `factory.cell(value)` in your own cell factories to keep the default row and add to it.

## Asking for a look

`iconOf`, `nameOf` and `tooltipOf` on the registry take any value and answer as the controls do, so your own rows and components can show values the same way. `find(type)` returns the appearance a class takes, if any. Ask on the UI thread.

```java
--8<-- "appearances/AppearanceExamples.java:lookup"
```

A control that only shows a value differently, such as a slot with an item and its price, is a few lines with `CustomComponent`, `Painter.visual` and `appearances()`; see [Extending Strata](extending.md).

## Visuals and icons

An icon is a `Visual`, something only the engine can draw, such as a loaded image. Adapters make visuals in classes of their own, such as `SwingVisuals.image` on [Swing](../platforms/swing.md#appearances), and draw a placeholder for a visual another adapter made. Treat a visual as a snapshot: to show a change to the object behind it, make a new visual. Adapters copy mutable engine objects when they make one, except where their documentation says otherwise; `SwingVisuals.image` keeps the image itself, so do not draw into an image while it is shown. Two visuals of the same thing are equal.
