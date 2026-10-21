# Overlays and tooltips

Some things draw above a screen instead of inside it: a drop-down's list, a confirmation box, an item's tooltip. Strata keeps them out of the screen's tree, so no container clips them and they never push the layout around.

Each host has one `Overlays`, from `factory.overlays()`. Overlays stack in the order they open, each above the screen and the overlays before it.

## Popups

A popup shows a component next to an area, usually the bounds of the component that opened it.

```java
--8<-- "overlays/OverlayExamples.java:popup"
```

The popup takes its desired size. It sits below the area with left edges lined up, above it when there is more room above, and moves left as needed to stay inside the host. A press anywhere outside the popup closes it, and that press goes nowhere else, so clicking another button while a menu is open only closes the menu. Escape closes it too. The [`ComboBox`](controls.md#combobox) is built this way.

A popup can also be laid out inside an area with `popupIn(content, area)`, as a dialog is laid out in the whole host: content that stretches fills the area, and other content takes its size where its alignment places it. The area is cut to the host. It closes like any popup. A [navigation view](controls.md#navigationview) opens its pane over the content this way.

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
factory.overlays().toast("Listing created");
factory.overlays().toast(myNoticePanel, 5000);
```

Toasts stack down the top-right corner, 4 units in from the edges and 4 apart. A text toast shows for 3 seconds and wraps at a third of the host's width; a component toast shows for the time given and draws its own background. The time runs on the platform's clock, and a toast closes on the first frame after it runs out. A click on a toast that nothing in it consumed closes it early. Presses elsewhere and Escape leave toasts alone, and they take no focus.

A text toast draws on `toast.background` with `toast.text`, falling back to the tooltip keys.

## Working with open overlays

| Call | Does |
|---|---|
| `overlay.close()` | Closes it and detaches its content. Closing twice does nothing. |
| `overlay.raise()` | Brings a popup or toast above the other popups and toasts, but no higher than the first dialog opened after it, so a dialog stays above everything opened before it. Does nothing for a dialog or a closed overlay. |
| `overlay.onEvent(Closable.CLOSED, handler)` | Runs once it has closed, however that happened |
| `overlays.of(component)` | Finds the overlay a component is shown in, for a button inside a dialog that closes it |
| `overlays.open()` | Every open overlay, bottom first |
| `overlays.closeAll()` | Closes them all, top first, toasts included |

Content must be new: a component already in a tree is refused. Closing or rebuilding the screen closes every overlay.

Dialogs are overlays inside the screen's host, so they need no engine screen of their own, and the screen behind keeps updating.

## Tooltips

Any component can have a tooltip. It shows once the pointer has rested for half a second on the component or anything inside it that has none of its own, and at once when the pointer moves straight from another tooltip. Adapters can change the wait with `setTooltipDelay` on their screen host or stack. The tooltip follows the pointer 12 units right of and above it (left of it near the right edge), and stays inside the host. It hides while a drag is in progress. Tooltips take no input.

```java
--8<-- "overlays/OverlayExamples.java:tooltips"
```

| Kind | Made with | Drawn |
|---|---|---|
| Text | `setTooltip(String)`, `setTooltip(Text)`, `Tooltip.text` | In the `tooltip.text` style, `\n` for new lines |
| Lines | `Tooltip.lines(List<Text>)` | One `Text` per line, each over the `tooltip.text` style |
| A component | `Tooltip.of(factory -> component)` | Built again each time the tooltip shows, with no box of its own |

Text tooltips draw on the `tooltip.background` paint inside the `tooltip.padding` insets, 4 units on every side by default, or on a dark box with a purple edge when the theme lacks the key.

Adapters can build a lines tooltip from an engine object's own tooltip text.

## Held modifiers

`InputEvents.MODIFIERS_CHANGED` fires at the screen's content and each open overlay whenever the held modifiers change, by a modifier key or by input that arrives with different ones. Use it for controls that change while a key is held:

```java
--8<-- "overlays/OverlayExamples.java:modifiers"
```

A component reads the current set at any time from `environment().modifiers()`.
