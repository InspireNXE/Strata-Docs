# Overlays and tooltips

Some things draw above a screen instead of inside it: a drop-down's list, a confirmation box, an item's tooltip. Strata keeps them out of the screen's tree, so no container clips them and they never push the layout around.

Each host has one `Overlays`, from `factory.overlays()`. It opens three kinds of overlay: popups, dialogs and toasts. Each call returns an `Overlay`, the open overlay, which you can close, raise and listen to. Overlays stack in the order they open, each above the screen and the overlays before it.

## Popups

A popup shows a component next to an area, usually the bounds of the component that opened it.

```java
--8<-- "overlays/OverlayExamples.java:popup"
```

The popup is measured with the host's size and takes its desired size. It sits below the area with left edges lined up. If it does not fit below and there is more room above, it sits above instead. It moves left as needed to stay inside the host. A press anywhere outside the popup closes it, and that press goes nowhere else, so clicking another button while a menu is open only closes the menu. A press on a popup lower in the stack closes the popups above it, as when a submenu is open and the user presses its parent menu. Escape closes the topmost popup. The [`ComboBox`](controls.md#combobox) is built this way.

A popup can also be laid out inside an area with `popupIn(content, area)`, as a dialog is laid out in the whole host: content that stretches fills the area, and other content takes its size where its alignment places it. The area is cut to the host. It closes like any popup. A [navigation view](controls.md#navigationview) opens its pane over the content this way.

```java
--8<-- "overlays/OverlayExamples.java:popup-in"
```

Two controls open themselves as popups and share the `Popup` attribute: a [`Menu`](controls.md#menu-and-menubar) of commands, and a [`Flyout`](controls.md#flyout) that shows any component. Both take focus when they open and give it back when they close, and a [`DropDownButton`](controls.md#dropdownbutton) opens either one below itself. A flyout can also turn off closing on a press outside, so the press reaches the screen and the flyout stays open, and open inside an area with `openIn`, such as beside a component.

## Dialogs

A dialog is modal. Its content is laid out in the whole host, where its own alignment places it, so a centered [window](controls.md#window) stays centered when the host resizes.

```java
--8<-- "overlays/OverlayExamples.java:dialog"
```

While a dialog is open:

- Nothing below it gets the pointer, focus or Tab. Keys go to it when nothing in it has focus.
- The screen below stays visible, under the theme's `overlay.backdrop` paint if it has one.
- Focus moves to the first tabbable component in it when it opens, and back to where it was when it closes.
- An Escape press that nothing consumed closes it. A window in a dialog handles Escape itself, as a close by the user, so a `Closable.CLOSE` handler can keep it open.

A [`MessageBox`](controls.md#messagebox) is a ready-made dialog for confirmations:

```java
--8<-- "overlays/OverlayExamples.java:message-box"
```

## Toasts

A toast is a short notice that closes itself, such as "Listing created".

```java
--8<-- "overlays/OverlayExamples.java:toast"
```

Toasts stack down the top-right corner, 4 units in from the edges and 4 apart. A text toast shows for 3 seconds and wraps at a third of the host's width; a component toast shows for the time given and draws its own background. The time runs on the platform's clock, and a toast closes on the first frame after it runs out. A click on a toast that nothing in it consumed closes it early. Presses elsewhere and Escape leave toasts alone, and they take no focus.

A text toast draws on `toast.background` with `toast.text`, inside `toast.padding` (6 units on every side by default), falling back to the tooltip keys for the background and text.

## Working with open overlays

| Call | Does |
|---|---|
| `overlay.content()` | The component it shows |
| `overlay.isOpen()`, `overlay.isModal()` | Whether it is still open, and whether it is a dialog |
| `overlay.close()` | Closes it and detaches its content. Closing twice does nothing. |
| `overlay.raise()` | Brings a popup or toast above the other popups and toasts, but no higher than the first dialog opened after it, so a dialog stays above everything opened before it. Does nothing for a dialog or a closed overlay. |
| `overlay.onEvent(Closable.CLOSED, handler)` | Runs once it has closed, however that happened |
| `overlays.of(component)` | Finds the overlay a component is shown in, for a button inside a dialog that closes it |
| `overlays.open()` | Every open overlay, bottom first |
| `overlays.closeAll()` | Closes them all, top first, toasts included |

Content must be new: a component already in a tree is refused. Closing or rebuilding the screen closes every overlay.

Code inside an overlay often needs to close it, such as a Done button in a dialog. `of` finds the overlay from any component in it:

```java
--8<-- "overlays/OverlayExamples.java:close-from-inside"
```

A control that is itself a window or a popup has a simpler way: a [window](controls.md#window) closes its dialog with `close()`, and a component inside a flyout or menu can find it with `ancestor(Closable.class)`.

Dialogs are overlays inside the screen's host, so they need no engine screen of their own, and the screen behind keeps updating.

## How overlays are layered

The host draws in layers: the screen's tree first, then each open overlay in the order it opened (or was raised), then the tooltip on top of everything. A dialog's layer first draws the theme's `overlay.backdrop` paint over everything below it. Each layer is drawn outside every clip of the layers below, so a popup is never cut off by the panel that opened it.

The pointer goes to the topmost layer that has something under it, and to the screen only where no overlay is. A dialog blocks every layer below it, even where it has nothing under the pointer. The tooltip is never hit. Keys go to the focused component, or to the topmost dialog when nothing has focus, or else to the screen. An Escape press that nothing consumed closes the topmost overlay that is not a toast.

An overlay's content has no parent, so events inside it stop at its content. A control event fired inside a popup or dialog, such as `Menu.CHOSEN`, does not reach handlers on the screen. Register the handler on the content or on the control, and listen for the overlay closing on the `Overlay` itself.

## Tooltips

Any [component that has `HasTooltip`](components.md#tooltips) can have a tooltip. It shows once the pointer has rested for half a second (`Tooltip.DEFAULT_DELAY_MILLIS`) on the component or anything inside it that has none of its own, and at once when the pointer moves straight from another tooltip. Adapters can change the wait with `setTooltipDelay` on their screen host or stack. The tooltip follows the pointer 12 units right of and above it (left of it near the right edge), and stays inside the host. It hides while a drag is in progress. Tooltips take no input.

```java
--8<-- "overlays/OverlayExamples.java:tooltips"
```

| Kind | Made with | Drawn |
|---|---|---|
| `Tooltip.Lines` with one entry | `setTooltip(String)`, `setTooltip(Text)`, `Tooltip.text` | In the `tooltip.text` style, `\n` for new lines |
| `Tooltip.Lines` | `Tooltip.lines(List<Text>)` | One `Text` per line, each over the `tooltip.text` style |
| `Tooltip.Custom` | `Tooltip.of(factory -> component)` | Built again with the host's factory each time the tooltip shows, with no box of its own |

`Tooltip.Lines` tooltips draw on the `tooltip.background` paint inside the `tooltip.padding` insets, 4 units on every side by default, or on a dark box with a purple edge when the theme lacks the background key. Lines sit one unit apart.

Adapters can build a lines tooltip from an engine object's own tooltip text.

## Held modifiers

`InputEvents.MODIFIERS_CHANGED` fires at the screen's content and each open overlay whenever the held modifiers change, by a modifier key or by input that arrives with different ones. Use it for controls that change while a key is held:

```java
--8<-- "overlays/OverlayExamples.java:modifiers"
```

A custom component reads the current set at any time from `environment().modifiers()`.
