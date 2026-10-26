# Components

Everything on screen is a `Component`. This page covers what all components share. The built-in controls add their own behavior on top, described in [Controls](controls.md).

## Getting components

There are three ways to get a component:

- From the `ComponentFactory` a screen receives, such as `anchorPanel()`, `stackPanel()`, `label()`, `button()` and `textBox()`, and the rest of the [controls](controls.md). To change what a built-in does, handle its [events](events.md).
- From the same factory by type or id, with `create(Gauge.class)` or `create(ComponentId.of("fancy", "gauge"))`, for components a library registered. See [Share your components](extending.md#share-your-components).
- By subclassing `CustomComponent` or `CustomContainer`. See [Extending Strata](extending.md).

Each factory call returns a new component with default settings, not attached to anything. Every factory method has a form that takes no arguments, and some have shorthands that take the setting most callers make at once:

```java
Button save = factory.button().setText("Save");   // no arguments, then setters
Button same = factory.button("Save");             // the shorthand
ListView<Offer> offers = factory.<Offer>listView(); // cells show String.valueOf(item) until setCellFactory
```

Besides components, the factory gives you its host's `overlays()`, for [popups, dialogs and toasts](overlays.md), and its current `theme()`.

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
| `parent()` | The component this one was added to as an `Optional`, empty for a screen's root, an overlay's content or a detached component |
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

`Component.ATTACHED` and `Component.DETACHED` fire as a component joins and leaves a shown tree, and `whileAttached` runs something for exactly as long as that lasts, such as a subscription to a feed. [Events](events.md#attaching-and-detaching) covers the order and the cases.

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
| `setEnabled(false)` | enabled | Still drawn (built-in controls draw a disabled look) and still takes space, but acts on no pointer or keyboard input and cannot be focused |
| `setOpacity(f)` | 1 | Drawn faded, from 0 (not drawn) to 1 (opaque). It changes drawing only. |
| `setHitTestVisible(false)` | hit | Drawn and laid out as before, but the pointer passes through it and everything inside it to what lies below, usually its parent. `componentAt` and drop targets skip it too. |

Hiding or disabling a component also hides or disables everything inside it, and clears focus if the focused component is inside.

!!! note "Disabled components take the pointer and do nothing with it"
    The pointer cannot hit a hidden component or anything inside it, but it does hit a disabled one, or one inside a disabled container, so nothing behind it acts. A press on a disabled button does not select the list row under it or start a window drag. It closes any light-dismiss popup above, as a press elsewhere would, and then fires no events, runs no default behavior, leaves focus where it is and uses up its release, so no click follows. The component is not hovered and hears no pointer enter, exit or move, but its tooltip shows, so a control can say why it is disabled. Its cursor is the nearest enabled ancestor's, and a scroll over it goes to that ancestor.

    `isEnabled()` and `isVisible()` say whether the component itself is on. `isEffectivelyEnabled()` and `isEffectivelyVisible()` also check every container and composite above it, which is what decides whether it can take input.

`isEnabled()` and `setEnabled` come from `Enableable`, which the entries that controls hold share with components: menu items, check and radio menu items, submenu items, tabs and navigation items. Code that turns things on and off can take an `Enableable<?>` and not care which it has. `Enableable` has no events, and entries take no binding, so a component's `Component.ENABLED` key is the one to bind.

### Opacity

`setOpacity(float)` fades a component and everything inside it. Opacities multiply down the tree, so a child at 0.5 inside a parent at 0.5 draws at 0.25. A component at 0 is not drawn at all, though it keeps its room in the layout, takes the pointer and keeps focus: opacity changes drawing only, and a change redraws without laying anything out again. A value outside 0 to 1 or NaN throws `IllegalArgumentException`.

`Component.OPACITY` is the property key for [binding](binding.md) and for [animation](animation.md), where a fade is one call:

```java
ui.animator().animate(card, Component.OPACITY, 0f, 200, Easing.EASE_IN, Interpolator.FLOAT);
```

How a partly faded component looks depends on the engine. An adapter fades what it draws through its canvas, and one that cannot fade some draws, such as engine-rendered visuals, draws them opaque. [Writing an adapter](../platforms/writing-an-adapter.md) lists what an adapter must do.

## Ids

`setId(String)` gives a component an id and `id()` returns it as an `Optional<String>`. An id is a free string that names a component for code that does not hold a reference to it, such as a [test](testing.md) that finds a button to click. Strata neither requires an id nor checks that ids are unique, and there is no lookup by id in the API: a screen keeps references to the components it needs. `setId(null)` removes an id.

```java
Button save = factory.button("Save").setId("save");
```

## What only some components have

`Component` holds only what every component needs: the tree, visibility, enabled state and layout. The rest comes from smaller interfaces that a component has when they fit it.

| Interface | Gives | Who has it |
|---|---|---|
| `Focusable` | Keyboard focus and tab order | Buttons, drop-down buttons, cycle buttons, checkboxes, radio buttons, toggle buttons and switches, sliders, spinners, text boxes, password boxes, text areas, color pickers, combo boxes, lists, tables, trees, grids, calendars, the date and time pickers, menus, flyouts, expanders, split panes, navigation views and tab bars. Scroll bars and scroll panels have it too, turned off until you call `setFocusable(true)`. |
| `HasCursor` | `setCursor` | Every container, and every control except separators, busy indicators and flyouts |
| `HasTooltip` | `setTooltip` | Every container, and every control except separators and busy indicators |
| `HasContextMenu` | `setContextMenu` | Every container, and every control except spacers, separators and busy indicators. See [Context menus](controls.md#context-menus). |
| `Container` | Children, padding, background and border | Panels, windows |

A custom component gets any of the first three by declaring the interface; see [Extending](extending.md).

### Focus

| Method | Default | Effect |
|---|---|---|
| `setFocusable(false)` | focusable | Refuses keyboard focus by a click or `requestFocus()` |
| `setTabbable(false)` | tabbable | Tab and Shift+Tab skip it. It can still be focused by a click. |
| `setTabIndex(n)` | 0 | Tab visits lower indexes first, then tree order among equal indexes. Negative values are allowed. |
| `isFocused()` | | True while it has keyboard focus. At most one component per host does. |
| `isFocusWithin()` | | True while it or a descendant has focus. Containers answer too, though they never take focus. |

A table, a tree and a spinner keep focus on a part inside them, a list, a text box, so `isFocused()` on the control is true while that part has focus, and `FOCUS_GAINED` and `FOCUS_LOST` fire on the control as well. [Input and focus](input-focus.md#focus-within) covers `isFocusWithin()` and the events that tell when focus enters and leaves a section of a screen.

Hover and press state belong to a control's own drawing code, so they are not public. Listen for `POINTER_ENTER`, `POINTER_EXIT`, `POINTER_PRESS` and `POINTER_RELEASE` to follow them from outside.

### Cursors

`setCursor(CursorKind)` picks the mouse cursor shown while the pointer is over the component, and `setCursor(null)` goes back to the ancestor's. `cursor()` returns an `Optional`, empty while the component has none of its own. A component with no cursor of its own uses its nearest ancestor's, and the default is `ARROW`. Buttons, checkboxes and sliders show `HAND`, and text boxes show `TEXT`.

| `CursorKind` | Meaning |
|---|---|
| `ARROW` | Default pointer |
| `TEXT` | I-beam, for editable text |
| `HAND` | Pointing hand, for clickable things |
| `CROSSHAIR` | Precise picking |
| `RESIZE_HORIZONTAL`, `RESIZE_VERTICAL`, `RESIZE_ALL` | Resizing and moving |
| `RESIZE_DIAGONAL_DOWN`, `RESIZE_DIAGONAL_UP` | Resizing from a corner: the first runs from the top left to the bottom right, the second from the bottom left to the top right |
| `NOT_ALLOWED` | Something that cannot be used now |
| `WAIT`, `PROGRESS` | Busy, and busy while still taking input |
| `GRAB`, `GRABBING` | Something that can be picked up, and being carried |
| `COPY`, `NO_DROP` | A drag that would copy where it is over, and one that cannot drop there |
| `HELP` | Help is available |

Engines without a shape show the closest one they have. Each kind has a `fallback()`: the diagonals fall back to `RESIZE_ALL`, `GRAB` and `GRABBING` to `HAND`, `NO_DROP` to `NOT_ALLOWED`, and every other kind to `ARROW`. The host asks the engine for the first kind in the chain that it can show, so a cursor never fails to appear. [Platform differences](../platforms/differences.md) lists what each adapter shows.

### Tooltips

`setTooltip` gives a component a `Tooltip`, and `tooltip()` returns it as an `Optional`. The host shows it near the pointer once it has rested on the component for half a second, unless the theme's `tooltip.delay` or the adapter sets another wait. Like a cursor, a tooltip covers what is inside the component: the host shows the tooltip of the deepest component under the pointer that has one, looking up through its ancestors. There are two kinds:

| Kind | Made with | Shows |
|---|---|---|
| `Tooltip.Lines` | `setTooltip(String)`, `setTooltip(Text)`, `Tooltip.text(...)`, `Tooltip.lines(list)` | Lines of text in a box drawn from the theme's tooltip keys |
| `Tooltip.Custom` | `Tooltip.of(factory -> component)` | A component built each time the tooltip shows, drawing its own background |

`clearTooltip()` removes it, and `setTooltip` refuses null. [Tooltips](overlays.md#tooltips) covers timing, placement and the theme keys.

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

## State at the time of an event

An event handler can compare a component with how it was when the event fired. `event.snapshot()` returns a `ComponentSnapshot` of the target, taken before any handler or default behavior ran. It holds what every component has, such as `bounds()`, `visible()`, `enabled()`, `focused()`, the margin and the alignments, and a `state()` with what the component's kind adds, such as a text box's text and caret. Snapshots are shallow: a container's snapshot says nothing about its children. Input events and requests, the events that are `SnapshotEvent`s, have them; reports, paint and measure events do not. See [Event snapshots](snapshots.md).

## Threading and timing

- Call components only from the UI thread. A component that is attached to a host checks the thread on most changes and throws `IllegalStateException` from any other. Hand work over with `Strata.uiThread()` or the factory's `uiThread()`; setting a `State` is the one change that works from any thread. See [The UI thread](threading.md).
- Setters take effect on the next frame. Changes that affect size schedule a layout; you never call layout or repaint.
- Custom components call their protected `invalidateLayout()` when their content changes size. Setters call it for you.
