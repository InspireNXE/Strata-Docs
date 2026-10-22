# Layout

Strata lays out a tree in two passes, the way WPF and Wintergreen do. This page explains the passes, every input a component has, each of the containers, the spacer, lining up text, text styles that panels pass down, and mirroring for right to left languages.

## The two passes

Layout runs before a frame is drawn, and only after something changed.

Measure
:   Each container offers each child some available space, possibly infinite, and asks how big it wants to be. The child answers with its desired size. Containers measure their children first, then decide their own desired size from them.

Arrange
:   Each container gives each child a slot: a rectangle, margin included. The child shrinks the slot by its margin and places itself inside by its alignment and size limits. The resulting rectangle is its bounds.

The screen's root gets the whole host as its slot.

A change that affects size, such as new label text, a new child or a different margin, marks the component and its ancestors for layout, and the tree is laid out again before the next frame. Only the changed branch does work: a component that did not change and is offered the same space as last time keeps its desired size without being measured, and one given the same slot keeps its bounds without arranging its children. Nothing is measured every frame, and a change that only affects drawing, such as a background, causes no layout at all. Moves that change no size, such as scrolling and a window's offset, arrange the component again in its last slot without measuring anything.

## Inputs every component has

| Input | Default | Meaning |
|---|---|---|
| Margin | `Insets.NONE` | Space kept clear around the component, outside its bounds |
| Minimum size | 0 by 0 | The smallest size it may take |
| Maximum size | infinite | The largest size it may take. It also limits the space offered when measuring, so text wraps within it. |
| Fixed width and height | unset (`NaN`) | Replaces the measured size and overrides `STRETCH` |
| Horizontal alignment | `LEFT` | Where it sits across its slot's width |
| Vertical alignment | `TOP` | Where it sits across its slot's height |
| Offset | 0, 0 | Moves it after layout places it. Windows and custom components can set it; see [Offset](#offset) |

Containers add padding (`Insets.NONE` by default): space inside their edges that children stay out of.

Every container also has a border (`setBorder(Border.of(argb, width))`, a width per side with `new Border(argb, insets)`). It draws along the inside of the bounds, over the background, and takes no space in layout, so give a container padding at least as wide as its border.

### Offset

A window's `setOffset(dx, dy)` moves it, and everything inside it, from where its parent's layout puts it. Siblings and the parent's size do not change, and the move needs no new measure. A [window](controls.md#window) moves by its offset when its title bar is dragged.

### How a component's size is decided

For each axis:

1. If a fixed size is set, use it.
2. Otherwise, if the alignment is `STRETCH`, use the slot's size minus the margin.
3. Otherwise, use the desired size.
4. Then clamp to the minimum and maximum.

The minimum and maximum always win, even over a fixed size.

Measuring follows the same rules. The space a parent offers, less the margin, is what the component measures its content in, except that a fixed size takes the place of the offered size on its axis and the maximum caps both. That is how a maximum width makes text wrap. A hidden component measures as zero, and its bounds are an empty rectangle at the top-left corner of its slot.

With any alignment other than `STRETCH`, a component keeps its size even if the slot is smaller and overflows it. If an ancestor clips its children, which containers do by default, the overflow is cut off.

### Alignment

| Horizontal | Vertical | Places the component |
|---|---|---|
| `LEFT` | `TOP` | Against the slot's left or top edge, after the margin |
| `CENTER` | `MIDDLE` | Centered in the slot |
| `RIGHT` | `BOTTOM` | Against the right or bottom edge, before the margin |
| `STRETCH` | `STRETCH` | Filling the slot minus the margin, unless a fixed size is set |

`setAlignment(horizontal, vertical)` sets both. `setAnchor(Anchor)` sets both at once by name:

| Anchor | Horizontal, vertical |
|---|---|
| `TOP_LEFT`, `TOP_CENTER`, `TOP_RIGHT` | `LEFT`/`CENTER`/`RIGHT`, `TOP` |
| `MIDDLE_LEFT`, `CENTER`, `MIDDLE_RIGHT` | `LEFT`/`CENTER`/`RIGHT`, `MIDDLE` |
| `BOTTOM_LEFT`, `BOTTOM_CENTER`, `BOTTOM_RIGHT` | `LEFT`/`CENTER`/`RIGHT`, `BOTTOM` |
| `FILL` | `STRETCH`, `STRETCH` |

`VerticalAlignment` has a fifth value, `BASELINE`, which puts the component's first line of text on a line its neighbors share. See [Lining up text](#lining-up-text).

Alignment is a request to the parent. Each container decides how big a slot to give, and some ignore alignment along one axis. The container sections below say which.

### Margin and padding

![A panel with a margin inside a padded panel](../assets/images/layout-spacing.png)

```java
--8<-- "layout/LayoutExamples.java:spacing"
```

A container's background covers its padding, but a child's margin belongs to the child and is never painted by it. Insets can be negative: a negative margin lets a component overlap its neighbors, and layout clamps any size that would go below zero.

`Insets.of(all)` sets every side, `Insets.of(horizontal, vertical)` sets left and right to one value and top and bottom to another, and the record constructor `new Insets(left, top, right, bottom)` sets each side. `setMargin`, `setPadding` and `setTextPadding` take the same numbers directly, so `setPadding(8)`, `setMargin(12, 0)` and `setPadding(3, 0, 0, 0)` need no `Insets` at all.

### Size limits in practice

![Natural, fixed, stretched and minimum sizes](../assets/images/layout-sizes.png)

```java
--8<-- "layout/LayoutExamples.java:sizes"
```

To go back to the measured size after fixing one, pass `Float.NaN` to `setWidth` or `setHeight`.

## AnchorPanel

Every child gets the whole content area (the bounds minus padding) as its slot, so children overlap and place themselves by their own anchor. Use it to pin things to edges, corners and the center of the same area. Its desired size is the largest child plus margin, plus the padding.

![An anchor panel with a title, a close button, a footer and a centered dialog](../assets/images/layout-anchor.png)

```java
--8<-- "layout/LayoutExamples.java:anchor"
```

The root of most screens is an anchor panel with `Anchor.FILL`.

## StackPanel

Children follow one another in a column (`Orientation.VERTICAL`, the default) or a row (`Orientation.HORIZONTAL`), with `setSpacing` units between neighbors and none before the first or after the last.

Along the orientation, each child gets exactly the space it asks for, so alignment along that axis has no effect, unless the child has a weight (below). Across it, each child gets the full content size and aligns itself: in a column, `STRETCH` makes a child as wide as the column, and `CENTER` centers it. Hidden children take no space and no gap.

![A column with stretched, centered and right-aligned children](../assets/images/layout-stack.png)

```java
--8<-- "layout/LayoutExamples.java:stack"
```

### Weights

`add(child, weight)`, or `setWeight(child, weight)` for a child already added, gives a child a share of the room the other children and the gaps leave. Weight 2 gets twice the space of weight 1, and 0 takes the weight away again. A weighted child is placed in its share by its own alignment, so give it `STRETCH` along the orientation to fill it. Its share stays within the child's minimum and maximum size plus its margin. The minimum is the larger of the minimum size and what the content needs, such as a wrapping label's longest word, a button's caption or a panel's children, or the fixed size where one is set. A child that would go past a limit is held at it, and the other weighted children share what is left. Its own desired size does not count once the panel has a size, so a weighted child shrinks first when space runs out, down to its minimum. When the minimums do not fit, the children run past the end of the panel. Measured without a limit along its orientation, inside a scroll panel for example, the panel asks for enough room to give each weighted child its desired size while keeping the proportions. `weight(child)` reads a weight back, and removing a child forgets its weight.

This covers a frame with a header and a footer at their own height and a body that takes the rest:

![A header, a scrolling body and a footer](../assets/images/layout-frame.png)

```java
--8<-- "layout/LayoutExamples.java:frame"
```

### Spreading

`setSpread(true)` spreads the children across the content box with equal gaps, the first at the start and the last at the end. The panel still asks only for its children and its spacing, so give it room: stretch it or give it a fixed size. The spacing becomes the smallest gap, a single child stays at the start, and spreading does nothing while a visible child has a weight, since that child takes the extra room.

![Three buttons spread across a row](../assets/images/layout-spread.png)

```java
--8<-- "layout/LayoutExamples.java:spread"
```

## GridPanel

Children sit in the cells of rows and columns. Each row and column has a `GridLength`:

| Length | Size |
|---|---|
| `GridLength.auto()` | As big as the largest child that sits only in it |
| `GridLength.fixed(size)` | Exactly `size` units, whatever its children need |
| `GridLength.weighted(weight)` | A share of the space left after the fixed and auto tracks. Weight 2 gets twice the space of weight 1. |

Fixed and auto tracks are sized first. Whatever width or height remains is shared among weighted tracks in proportion to their weights. A weighted track stays within the largest minimum and the largest maximum of the children that sit only in it, a minimum counting what the content needs as a stack's does, and children in weighted rows are measured with their row's share, so wrapping text in them sees its height. When the grid itself is measured without a size limit, weighted tracks take the smallest size that fits their children while keeping their proportions.

```java
--8<-- "layout/LayoutExamples.java:grid"
```

![A two-column login form](../assets/images/layout-grid.png)

- `add(child, row, column)` puts a child in one cell. `add(child, row, column, rowSpan, columnSpan)` lets it cover several. A spanning child's slot includes the spacing between the cells it covers.
- A child added with plain `add(child)` sits in row 0, column 0.
- Several children may share a cell. They overlap, like in an anchor panel.
- A child aligns itself within its cell. `STRETCH` fills the cell.
- Children spanning several auto tracks grow those tracks if they would not fit otherwise.
- A grid with no rows or columns defined has one cell that fills its content box.
- A cell outside the defined rows or columns is moved to the last row or column, and spans are cut to fit. A negative row or column, or a span below 1, throws `IllegalArgumentException`.
- `setSpacing(columnGap, rowGap)` puts gaps between tracks, never around the edge. Use padding for that.
- `columns(...)` and `rows(...)` replace the definitions; `addColumn` and `addRow` append one, and `columnDefinitions()` and `rowDefinitions()` read them back.
- `cellOf(child)` returns a child's `GridPanel.Cell`: its row, column and spans as given when it was added, before any cutting to fit. A child added with plain `add`, or a component that is not a child, gives `Cell.ORIGIN`.

Weighted tracks make proportional layouts:

![A sidebar and a main area twice as wide, above a fixed status bar](../assets/images/layout-weighted.png)

```java
--8<-- "layout/LayoutExamples.java:weighted"
```

## Spacer

`factory.spacer()` makes an empty component that only takes room: it draws nothing of its own, takes no input and asks for no size. It has `Anchor.FILL`, so it fills whatever slot it gets. Give it a weight in a stack panel to take the room that is left, or a fixed size with `factory.spacer(width, height)` to keep a set gap.

![A toolbar with a gap that pushes Help to the right](../assets/images/layout-spacer.png)

```java
--8<-- "layout/LayoutExamples.java:spacer"
```

## WrapPanel

Children follow one another in rows, and a row that is full continues on a new one, like words in a paragraph. Use it for tags, chips and a toolbar that wraps as its window narrows. `Orientation.HORIZONTAL`, the default, fills rows; `Orientation.VERTICAL` fills columns, which is the same with the axes swapped. `ui.wrapPanel()` and `ui.wrapPanel(orientation)` make one.

```java
--8<-- "layout/LayoutAdditionsExamples.java:wrap-panel"
```

`setSpacing(spacing, lineSpacing)` sets the gap between neighbors in a line and the gap between lines, both 0 by default. Gaps lie between children and between lines, never before the first or after the last, and a hidden child takes no space and no gap.

- Each child is measured with the content length along the lines and unlimited space across them. A new line starts when the next child would pass the end of the line. A child longer than the panel gets a line of its own.
- Offered unlimited length, as inside a horizontal scroll panel, every child is in one line, as in a stack panel. A wrap panel in a column needs a width from outside, such as a fixed width, a maximum width or `STRETCH` in a parent that has one, just as a wrapping label does.
- A line is as thick as its thickest child. Children sit in it by their own alignment across the line: `TOP`, `MIDDLE`, `BOTTOM` or [`BASELINE`](#lining-up-text) in rows.
- `setLineAlignment(alignment)` places a line's spare length: `LEFT` leaves it after the children (the default), `CENTER` splits it before and after, `RIGHT` puts it before, and `STRETCH` shares it among the gaps so the first child is at the start and the last at the end. A line of one child stays at the start. In columns, `LEFT` is the top.
- The panel asks for its longest line and for the sum of its lines and gaps, plus its padding. Its minimum size, which a parent that shares space respects, is its largest visible child's minimum on each axis plus the padding.

## Lining up text

A form row of "Price", a text box and "per unit" looks wrong when each text sits at its own height, because the box pads its text and the labels do not. Give the children `VerticalAlignment.BASELINE` and the first line of text in each of them sits on one line:

```java
--8<-- "layout/LayoutAdditionsExamples.java:baseline"
```

The baseline of a component is the distance from the top of its bounds to the bottom of its first line of letters. Strata's text controls report it: the label, buttons, the check box, radio button and toggle switch, the text box, password box, combo box and spinner, the date and time pickers, and the tab bar. A component that draws text of its own reports it by overriding `baseline()`; see [Lining up text](extending.md#lining-up-text) in the extending guide.

Three kinds of container line up their `BASELINE` children:

| Container | Lines up |
|---|---|
| A horizontal `StackPanel` | The children of the row |
| `WrapPanel` | The children of each line, in rows |
| `GridPanel` | The children of each row, but only those that sit in that row alone |

Each child moves down until its baseline meets the lowest baseline among them, and the row grows to hold the line and the deepest part below it. A child with no baseline, such as an image or a slider, counts its bottom edge as its baseline. Children in the same row that are not aligned `BASELINE` are placed by their own alignment as before. Everywhere else `BASELINE` acts as `TOP`: in a column, in an anchor or absolute panel, and for a child that spans several grid rows.

## AbsolutePanel

Each child sits at a fixed position measured from the top-left corner of the content area. Children are measured with unlimited space, so a wrapping label in one needs a fixed or maximum width. Its slot is exactly its desired size plus margin at that position, so alignment has no effect. Children may overlap. Later ones are drawn on top, until `raise`, `lower` or a z index changes the [drawing order](components.md#drawing-order), and a window in the panel comes to the front when it is pressed. The panel's desired size is just big enough to cover every child.

![Three buttons at fixed positions](../assets/images/layout-absolute.png)

```java
--8<-- "layout/LayoutExamples.java:absolute"
```

`add(child)` places a child at (0, 0). `move(child, x, y)` moves one later and does nothing if it is not a child. It is not `move(child, index)`, which every container has and which changes the order of `children()`. There is no getter for a child's position, so keep your own if you need it, as the [drag example](events.md#drags) does.

Use it for free placement, such as a board of draggable pieces. For forms and menus, the other containers adapt to text and screen size, so prefer them.

## ScrollPanel

Shows part of content larger than itself. Children are laid out as in an `AnchorPanel`, each given the whole scrolled area: the size of the largest child plus its margin, and at least the viewport. Most panels hold one `StackPanel` or `GridPanel`.

```java
--8<-- "layout/LayoutExamples.java:scroll"
```

Each axis has a `ScrollPanel.ScrollPolicy`, set with `setPolicy(horizontal, vertical)`:

| Policy | The axis |
|---|---|
| `NEVER` | Does not scroll. Children are measured with the viewport's size on it, so text wraps. |
| `AUTO` | Scrolls, and its bar shows only while the content is larger than the viewport |
| `ALWAYS` | Scrolls, and its bar always shows |

The default is vertical `AUTO`, horizontal `NEVER`. The panel asks for its content's size plus its padding and shown bars, but no more than it is offered on a scrolling axis, so give it a height or stretch it in a parent with room. The viewport, which `viewport()` returns, is the bounds less the padding and the gutter of each shown bar (`BAR_WIDTH` plus `BAR_GAP`, 8 units).

| Input | Scrolls |
|---|---|
| Wheel | 10 units a notch, set with `setScrollRate`. A scroll past the end is not consumed, so it reaches an outer panel. A panel that scrolls only sideways takes the vertical wheel too. |
| The bar | Drag the thumb with the left button, or press the track to page |
| Up, Down, Page Up, Page Down, Home, End | Vertically, by the rate, by the viewport less one rate, or to the ends, when no focused descendant used them. Held keys repeat. Not with Control or Alt, nor while the vertical policy is `NEVER`, and consumed only when the offset changed. |
| Left, Right | Sideways by the rate, while the horizontal axis scrolls and no focused descendant used them. Not with Control or Alt, and consumed only when the offset changed. A panel that scrolls only sideways takes Left and Right and leaves the vertical keys alone. |
| Shift with the wheel | A vertical wheel with Shift held scrolls the horizontal axis while it scrolls. A horizontal scroll, such as one an engine makes from Shift and the wheel itself, is taken as it is. |
| A drag that carries data | Near the panel's edge, scrolls toward it; see below |
| Focus | A descendant that gains focus is scrolled into view |

`setScroll(x, y)`, `scrollBy` and `scrollToVisible(component)` work from code, `scrollX()`, `scrollY()`, `maxScrollX()` and `maxScrollY()` read the offsets, and `ScrollPanel.SCROLLED` reports every change of offset with a `ScrollPanel.State`. The offset is clamped to its range on every change and after every layout (before the first layout it is only kept from going below 0), and children move by it rounded to whole units, so engines that draw whole pixels do not shimmer. A scroll panel is `Focusable` but starts unfocusable; call `setFocusable(true)` to let it take focus and its keys by itself. Scrolling moves the children's real bounds without measuring them again, so pointer positions and `bounds()` stay in host coordinates inside a scroll panel. Children are clipped to the viewport, the bounds less the padding and the bar's gutter. For long lists of similar rows, use a [`ListView`](controls.md#listview), which builds only the rows in view.

A theme can make the panel glide to a new offset instead of jumping. With the metric `scroll.smooth` above 0, wheel steps, the scrolling keys and `scrollToVisible` take that many milliseconds to arrive, and a further step adds to where the glide is headed. The default is 0. `setScroll`, `scrollBy`, the thumb and a press on the track move at once and stop a glide. See [Animation](animation.md).

While a drag that carries data is held within 16 units of a viewport edge, the panel scrolls toward that edge, faster the deeper into the zone the pointer is, so a row can be dropped at the end of a long page. An inner panel that cannot scroll that way any more lets the outer one scroll. `setDragScroll(false)` turns it off. See [Drag and drop](drag-and-drop.md).

## Inherited text styles

A container can change the text of every control inside it. `setTextStyle(patch)` takes a `TextStylePatch`, which names only the properties it changes, such as the color, so the controls keep the rest of the style the theme gives them:

```java
--8<-- "layout/LayoutAdditionsExamples.java:text-style"
```

Each control's style from the theme comes first, then the patches of the containers around it, the outermost first, so an inner container wins where both set a property. `textStyle()` returns the patch, an `Optional` that is empty by default, and `setTextStyle(null)` removes it. The patch changes the text style keys the theme defines, for every control inside, and nothing else:

- A style set on a control in code, such as `Label.setStyle`, wins over every patch.
- A control that reads a key the theme lacks keeps its own fallback, unpatched.
- Popups and other overlays are not inside the container, so a menu opened from a control in it keeps its normal text.
- The container's own text, such as a window's title, is not changed by its own patch.

Strata works the styles out when the theme is applied or the tree changes, never while painting. A theme override can patch a text style key for the whole screen; see [Changing part of a text style](theming.md#changing-part-of-a-text-style).

## Right to left

Hebrew and Arabic are written from the right, and a screen in them should be the mirror image of one in English: the first column at the right, the back button at the right of the title, a progress bar filling from the right. Layout does this once for the whole tree, so a screen needs no code of its own for it.

Every component has a `FlowDirection`, `LEFT_TO_RIGHT` or `RIGHT_TO_LEFT`. `flowDirection()` returns the component's own direction if it has one, and otherwise the nearest ancestor's. At the top of a tree it is the direction of the screen's root content if that has one, and otherwise the direction of the translator's language, which `FlowDirection.of(locale)` works out from the language and its script. Overlays follow the screen. `setFlowDirection(direction)` sets a component's own direction, and `setFlowDirection(null)` makes it follow its parent again. A language change lays everything out again.

When the parent's direction is right to left, each child is placed as it is for left to right, and the result is then mirrored inside the parent's bounds. So every container mirrors with no code of its own, and:

- `LEFT` and `RIGHT` alignment, margins and padding mean the start and the end of the line. A left margin of 8 units, `new Insets(8, 0, 0, 0)`, is a gap at the left in English and at the right in Arabic.
- A stack panel runs from the right, a grid's first column is at the right, a wrap panel fills its lines from the right, and the positions in an `AbsolutePanel` are measured from the right edge.
- The offset (`setOffset`, a dragged window) is added after the mirror and is not mirrored.
- Bounds are real coordinates, so hit testing and painting need nothing new. Text, sprites and visuals draw unmirrored.

```java
--8<-- "layout/LayoutAdditionsExamples.java:direction"
```

The built-in controls mirror their insides as well: check boxes, sliders, menus, tabs, trees, scroll bars and the rest. [Internationalization](internationalization.md) lists what each one does, and covers text direction, plurals and numbers. A component of your own that places parts itself reads `flowDirection()` and mirrors them, as the meter above does.

## Clipping

Containers clip their children to their own bounds by default. A clipped part of a child is neither drawn nor hit by the pointer, so a child scrolled out of view cannot be clicked, and a child entirely outside the clip is not drawn at all. `setClipChildren(false)` lets children draw outside, for example a focus glow. For something that must hang over everything, such as a menu, use a [popup](overlays.md#popups).

## Recipes

### Center something on the screen

Put it in a root anchor panel with `Anchor.FILL` and give it `Anchor.CENTER`. Give it a fixed width if it holds stretched children, so they have a width to stretch to.

### Filling the remaining space

In a stack panel, give the child that should fill the rest a weight of 1 and stretch it, as the [frame](#weights) does. To push the children after a point to the far end, put a [weighted spacer](#spacer) there. In a grid, use a weighted row or column, which can stay empty for the same effect.

### Wrapping text

A wrapping label needs a width to wrap to: a fixed width, a maximum width, or `STRETCH` inside a parent that has a width. Offered unlimited width, it breaks only at `\n`.

![A wrapping label in a fixed-width column](../assets/images/layout-wrap.png)

```java
--8<-- "layout/LayoutExamples.java:wrap"
```

### Equal-width buttons

Add them to a stretched row with the same weight each and give each `Anchor.FILL`, or put them in a grid with equal weighted columns, or give each the same fixed width.

## Common problems

| Symptom | Likely cause |
|---|---|
| A component is not visible at all | Its size is zero: a container with no children measures as just its padding. Or it is hidden, or outside a clipping ancestor, or the root is not `FILL` and is smaller than you think. |
| A stretched child is not stretching | Its parent has no width to give. The root must be `FILL`, or a parent between needs a fixed width or `STRETCH`. |
| A child is cut off | It is bigger than its slot and an ancestor clips. Give the ancestor more room, or turn clipping off. |
| Text does not wrap | See [Wrapping text](#wrapping-text). |
| Alignment along a stack's direction does nothing | A stack panel gives each child exactly its size along that axis. Give the child a [weight](#weights), or use a grid. |
| `bounds()` is out of date | Layout runs before the next frame. Read bounds in a later frame or in an event handler. |
| `BASELINE` seems to do nothing | The parent is a column, an anchor panel or an absolute panel, or the child spans several grid rows, where it acts as `TOP`. Use a horizontal stack panel, a wrap panel or a grid row. |
| A wrap panel shows one long line | It was offered unlimited width. Give it a fixed or maximum width, or `STRETCH` in a parent that has one. |

See [Troubleshooting](../help/troubleshooting.md) for more.

## Writing your own layout

Extend `CustomContainer`. The [extending guide](extending.md#write-a-new-layout) walks through a flow layout that wraps children into rows, and [Lining up text](extending.md#lining-up-text) shows how a layout of your own lines up baselines.
