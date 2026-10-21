# Components

Everything on screen is a `Component`. This page covers what all components share. The built-in controls add their own behavior on top, described in [Controls](controls.md).

## Getting components

There are exactly two ways to get a component:

- From the `ComponentFactory` a screen receives, such as `anchorPanel()`, `stackPanel()`, `label()`, `button()` and `textBox()`, and the rest of the [controls](controls.md). To change what a built-in does, handle its [events](events.md).
- By subclassing `CustomComponent` or `CustomContainer`. See [Extending Strata](extending.md).

Each factory call returns a new component with default settings, not attached to anything. Every factory method has a form that takes no arguments, and some have shorthands that take the setting most callers make at once:

```java
Button save = factory.button().setText("Save");   // no arguments, then setters
Button same = factory.button("Save");             // the shorthand
ListView<Offer> offers = factory.<Offer>listView(); // cells show String.valueOf(item) until setCellFactory
```

The defaults with no arguments are empty text and titles, cells and options shown as `String.valueOf(item)`, horizontal separators, sliders and split panes, vertical stack panels and scroll bars, an image that draws nothing, and a message box with only an OK button. [Defaults](../reference/defaults.md) lists the rest.

!!! danger "Do not implement the component interfaces"
    A class that implements `Component`, `Button` or any other component interface directly has no Strata implementation behind it. Adding it to a container, or returning it from `Screen.build`, throws `IllegalArgumentException`. Subclass `CustomComponent` instead.

## The type parameter

`Component<T extends Component<T>>` is self-typed: every setter returns `T`, the type you see the component as. That is what makes chaining work without casts:

```java
Button save = factory.button("Save").setEnabled(false).setTabIndex(2).onEvent(Button.PRESSED, e -> this.save());
```

Setters inherited from `Component` return the specific type: `setEnabled` on a `Button` returns `Button`, and `setPadding` on a `StackPanel` returns `StackPanel`. When a parameter or field only needs "some component", use `Component<?>`.

## The tree

| Method | What it does |
|---|---|
| `parent()` | The component this one was added to, or `null` for a screen's root, an overlay's content or a detached component |
| `parent(type)` | The parent as `type` if it is one, otherwise empty |
| `ancestor(type)` | The nearest parent, grandparent and so on that is a `type`, or empty |
| `Container.add(child)` | Adds at the end, so the child is drawn on top of earlier ones with the same z index |
| `Container.insert(index, child)` | Adds at a place in the layout order. It is drawn on top, as with `add`. |
| `Container.move(child, index)` | Moves a child in the layout order without taking it out of the tree |
| `Container.indexOf(child)` | Where a child is in `children()`, or -1 |
| `Container.remove(child)` | Removes it. It loses focus, hover and press state and is detached. Does nothing if it is not a child. |
| `Container.clear()` | Removes every child |
| `Container.children()` | A live, unmodifiable view in layout order |
| `Container.paintOrder()` | A copy of the children in drawing order, bottom first |

A parent is not always a `Container`. An expander's content, an info bar's action and a navigation view's page have that control as their parent, and the parts and content of a composite you write have your composite. So ask the parent for what you need instead of casting `parent()`. The type can be a raw interface, such as `Container.class` or an attribute like `Closable.class`:

```java
// Close the info bar or flyout this button is in, whichever it is
button.ancestor(Closable.class).ifPresent(Closable::close);
```

`add` throws `IllegalArgumentException` if the child already has a parent, is a screen's root, is the container itself or one of its ancestors, has no Strata implementation, or was made by another factory. Remove a component from its parent before adding it elsewhere. To move content a control holds, take it out through the control first, as `expander.setContent(null)` does.

`add` can also throw `ThemeException`: when the container is already on screen, the child is checked against the host's theme right away. See [Themes](theming.md#required-and-optional-keys).

### Attachment

A component is attached while it is in a tree that a host is showing. `isAttached()` tells you. Some things only happen while attached:

- Hover, focus and press state change only while attached.
- `requestFocus()` fails on a detached component.
- Custom components receive event hooks and their theme only while attached.

Adding a child to an attached container attaches the child, and the child's theme hook runs before the next layout.

Removing a component does not remove its event handlers. If the component will be reused, register the handlers that must go with `subscribe` and close their [subscriptions](events.md#removing-handlers).

## Drawing order

A container's children have two orders. The layout order is `children()`: layout places children in it, and focus and Tab follow it. The drawing order is `paintOrder()`: children later in it are drawn over earlier ones, and the pointer hits them first. Until you change it, the drawing order is the order children were added.

| Method | What it does |
|---|---|
| `setZIndex(z)` | Sets where a component is drawn among its siblings. Higher draws later. 0 by default, and it may be negative. |
| `Container.raise(child)` | Draws the child over its siblings with the same z index |
| `Container.lower(child)` | Draws the child under its siblings with the same z index |
| `Container.move(child, index)` | Changes the layout order only. The drawing order stays. |

```java
// Bring a panel to the front without moving it in the layout
panel.raise(details);
// Keep a toolbar over every panel, whatever is raised
toolbar.setZIndex(1);
```

Children are drawn by z index first, then by when they were added, raised or lowered: adding or inserting a child and raising it put it above its siblings with the same z index, and lowering puts it below them. A z index orders siblings only. A child with a high z index is still drawn inside its parent, under anything its parent's later siblings draw.

Raising, lowering and a new z index redraw but lay nothing out again, and they leave focus and Tab where they were. `move` lays the container out again, since the layout order changed, and moves the child in the Tab order too, but it never takes the child out of the tree: the child keeps focus, hover and press state, and no handler sees it leave or return. `insert` and `move` throw `IndexOutOfBoundsException` for an index out of range; `insert` of a child the container already has throws `IllegalArgumentException`, as `add` does, and `move`, `raise` and `lower` throw it for a component that is not a child.

A [window](controls.md#window) raises itself in its parent when it is pressed, so of several windows in an [`AbsolutePanel`](layout.md#absolutepanel) the one the user pressed comes to the front. Overlays are ordered on their own: see [Working with open overlays](overlays.md#working-with-open-overlays).

## State flags

| Flag | Default | Effect |
|---|---|---|
| `setVisible(false)` | visible | Not drawn, cannot be hovered or focused, takes no space in layout, and its bounds become empty |
| `setEnabled(false)` | enabled | Still drawn (built-in controls draw a disabled look) and still takes space, but gets no pointer or keyboard input and cannot be focused |

Hiding or disabling a component also hides or disables everything inside it, and clears focus if the focused component is inside.

!!! note "Disabled components pass the pointer to their ancestors"
    The pointer cannot hit a disabled or hidden component or anything inside it. A click on a disabled button therefore lands on the nearest enabled ancestor under the pointer, usually the panel behind it. If that ancestor has a click handler, it runs.

## What only some components have

`Component` holds only what every component needs: the tree, visibility, enabled state and layout. The rest comes from smaller interfaces that a component has when they fit it.

| Interface | Gives | Who has it |
|---|---|---|
| `Focusable` | Keyboard focus and tab order | Buttons, drop-down buttons, cycle buttons, checkboxes, radio buttons, toggle switches, sliders, text boxes, password boxes, text areas, combo boxes, lists, calendars, the date and time pickers, menus, flyouts, expanders, navigation views and tab bars. Scroll bars and scroll panels have it too, turned off until you call `setFocusable(true)`. |
| `HasCursor` | `setCursor` | Every container, and every control except separators, busy indicators and flyouts |
| `HasTooltip` | `setTooltip` | Every container, and every control except separators and busy indicators |
| `Container` | Children, padding, background and border | Panels, windows |

A custom component gets any of the first three by declaring the interface; see [Extending](extending.md).

### Focus

| Method | Default | Effect |
|---|---|---|
| `setFocusable(false)` | focusable | Refuses keyboard focus by a click or `requestFocus()` |
| `setTabbable(false)` | tabbable | Tab and Shift+Tab skip it. It can still be focused by a click. |
| `setTabIndex(n)` | 0 | Tab visits lower indexes first, then tree order among equal indexes. Negative values are allowed. |
| `isFocused()` | | True while it has keyboard focus. At most one component per host does. |

Hover and press state belong to a control's own drawing code, so they are not public. Listen for `POINTER_ENTER`, `POINTER_EXIT`, `POINTER_PRESS` and `POINTER_RELEASE` to follow them from outside.

### Cursors

`setCursor(CursorKind)` picks the mouse cursor shown while the pointer is over the component. A component with no cursor of its own uses its nearest ancestor's, and the default is `ARROW`. Buttons, checkboxes and sliders show `HAND`, and text boxes show `TEXT`.

| `CursorKind` | Meaning |
|---|---|
| `ARROW` | Default pointer |
| `TEXT` | I-beam, for editable text |
| `HAND` | Pointing hand, for clickable things |
| `CROSSHAIR` | Precise picking |
| `RESIZE_HORIZONTAL`, `RESIZE_VERTICAL`, `RESIZE_ALL` | Resizing and moving |
| `NOT_ALLOWED` | Something that cannot be used now |

Engines without a shape show the closest one they have. Swing has no "not allowed" cursor and shows the default arrow.

### Backgrounds and borders

`setBackground(Paint)` and `setBorder(Border)` are on containers. A background is drawn over the container's bounds before anything else of it, and a border along the inside of its edges, and neither changes layout. Both are `NONE` by default, and themes do not set them. See [Paints](theming.md#paints).

Controls take their looks from the theme. To put a control on a colored or bordered area, put it in a panel and give the panel the background:

```java
AnchorPanel framed = factory.anchorPanel()
        .setBackground(Paint.solid(0xD7000000))
        .setBorder(Border.of(0xB9FFFFFF, 1))
        .setPadding(1)
        .add(list.setAnchor(Anchor.FILL));
```

## Reading geometry

| Method | Returns |
|---|---|
| `bounds()` | Where the component sits as of the last layout, in the host's coordinates. `Rect.ZERO` before the first layout, and empty while hidden. |
| `desiredWidth()`, `desiredHeight()` | What it asked for in the last measure, margin excluded |

Bounds are in host coordinates, not relative to the parent. Pointer events give you `localX()` and `localY()` for positions relative to the current component.

Bounds are updated by layout, which runs before the next frame. Right after you change something, `bounds()` still holds the old value. Adapters can force a layout with `ScreenHost.layout()`, but screens and components should not need to.

## Threading and timing

- Call components only from the engine's UI thread.
- Setters take effect on the next frame. Changes that affect size schedule a layout; you never call layout or repaint.
- Custom components call their protected `invalidateLayout()` when their content changes size. Setters call it for you.
