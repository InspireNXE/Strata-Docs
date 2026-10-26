# Input and focus

Components never see engine input codes. Each adapter maps its engine's keys, buttons and modifiers to Strata's types in `org.inspirenxe.strata.api.input` before handing them over, so a component written against the API behaves the same on every engine.

## Keys

`Key` names physical keys by their position on a US keyboard. `Key.A` is the key labeled A on a US layout, whatever the user's own layout prints on it, so on a French AZERTY keyboard it is the key labeled Q. Use keys for shortcuts and navigation, and [characters](#characters) for text.

The constants cover letters, digits (`NUM_0` to `NUM_9`), punctuation, `SPACE`, `ENTER`, `TAB`, `BACKSPACE`, `ESCAPE`, `INSERT`, `DELETE`, arrows, `HOME`, `END`, `PAGE_UP`, `PAGE_DOWN`, the lock keys, `PRINT_SCREEN`, `PAUSE`, `F1` to `F12`, the keypad (`KP_0` to `KP_9`, `KP_ENTER` and friends), left and right modifiers, and `MENU`. Keys an adapter cannot identify, such as F13 to F24 on most adapters, arrive as `Key.UNKNOWN`.

A few helpers: `isEnter()` is true for `ENTER` and `KP_ENTER`, `isModifier()` for the eight left and right Shift, Control, Alt and Super keys, and `isPrintable()` for keys that usually type a character.

!!! warning "Store keys by name"
    The order of the `Key` constants carries no meaning and may change between versions. Save a key binding as `key.name()` and load it with `Key.valueOf(name)`, never by ordinal.

### Key actions

Each `KEY` event has a `KeyAction`:

| Action | Meaning |
|---|---|
| `PRESS` | The key went down |
| `REPEAT` | It is still held and repeating |
| `RELEASE` | It came up |

`action().isDown()` is true for presses and repeats, the moments when a held key should act again, such as moving a caret.

### Key repeat

Strata delivers repeats on every engine. Engines differ in how they report a held key, and the adapter evens that out:

- If the engine sends real repeats, the adapter passes them on.
- If the engine reports a held key as more presses (AWT does), the adapter uses the SPI's `HeldKeys` to turn the second and later presses into `REPEAT`.
- If the engine sends no repeats at all, the host makes them: after 400 ms, then every 33 ms, for Left, Right, Up, Down, Backspace, Delete, Home, End, Page Up and Page Down only. Only the last of these keys pressed repeats, with the modifiers held when it went down, and each repeat goes to whatever has focus when it fires. These timings follow the host's clock and need `render` to be called every frame. A slow frame sends at most one repeat, so repeats are dropped rather than sent in a burst.

The Swing adapter falls in the second group, so you see the user's system repeat rate there.

## Modifiers

`Modifier` is `SHIFT`, `CONTROL`, `ALT` or `SUPER`. The left and right keys of a kind count as the same modifier; use `Key.LEFT_SHIFT` and so on to tell them apart. `ALT` includes AltGr. `SUPER` is the Windows key, or Command on macOS.

Each platform also names a shortcut modifier, the one its users press for Save and Copy: Control, or Super where the platform uses Command. `KeyEvent.shortcutModifier()` returns it, and the text fields use it for their select all, copy, cut, paste, undo and redo keys. A key with Super held passes through a text field to the host's shortcuts, so Command+S runs from inside a field.

`Modifiers` is an immutable set packed into an int, so events carry it without allocating:

```java
Modifiers held = event.modifiers();
held.shift();                    // also control(), alt(), superKey()
held.has(Modifier.CONTROL);
held.isEmpty();
Modifiers.of(Modifier.CONTROL, Modifier.SHIFT);
Modifiers.NONE.with(Modifier.ALT).without(Modifier.ALT);
```

`InputEvents.MODIFIERS_CHANGED` fires whenever the held set changes, so a screen can relabel a button while Control is held; see [Held modifiers](overlays.md#held-modifiers).

Some engines do not report modifiers with every kind of input. An adapter for such an engine reports `Modifiers.NONE` for those events, typically pointer moves, scrolls or typed characters. See [Platform differences](../platforms/differences.md#modifier-keys).

## Shortcuts

`KeyEvent.is(key, required, policy)` is true for a press or repeat of `key` whose held modifiers satisfy the policy:

| `ModifierPolicy` | Matches when | Ctrl+S matches Ctrl+Shift+S? |
|---|---|---|
| `EXACT` | Exactly the required modifiers are held | no |
| `AT_LEAST` | The required modifiers are held, and maybe others | yes |
| `IGNORE` | Always, whatever is held | yes |
| `FORBID` | No modifier is held; `required` is ignored | no |

```java
--8<-- "events/EventExamples.java:shortcut"
```

`KeyEvent.is(KeyStroke)` matches a press or repeat of a `KeyStroke`, a key with its exact modifiers. `KeyStroke.shortcut(Key.S)` holds the platform's shortcut modifier, so one stroke serves Control on most platforms and Command on macOS, and the factory's `keyText` turns it into the "Ctrl+S" a menu shows. For a shortcut that runs a command, shared by a button and a menu item, use a [command](commands.md) instead of a key handler.

Put screen-wide shortcuts on the screen's content, as above. Keys bubble from the focused component, so a shortcut there fires unless something focused consumed the key first. A focused text box consumes plain typing keys, but not Control combinations it does not use, so Ctrl+S still reaches the content while the user is typing. Keys pressed inside an overlay bubble to the overlay's content and stop there, so a shortcut that should also work while a popup or dialog has focus needs a handler on that content too.

`ModifierPolicy.matches(required, held)` is the test `is` uses, and it works on the modifiers of any event, such as a click that does something else with Control held:

```java
--8<-- "events/EventExamples.java:modifier-click"
```

## Keys nothing consumed

A key goes to the focused component first and bubbles up through its ancestors. If nothing consumes it, the host tries the following in order, and the first that acts uses the key up:

1. A [command](commands.md) with a matching shortcut, for a press or repeat. Commands scoped to the key's target and its ancestors come first, then the host's, then those of the menu bars in the top dialog or the screen's content.
2. A mnemonic: Alt with a letter or digit, where the platform uses mnemonics, acts on the control in the top layer whose caption underlines that letter, such as `_Save`.
3. The menu bar: F10, or Alt pressed and released alone, gives the keys to the first menu bar. Left and Right move along its titles, Down or Enter opens one, and Escape lets go.
4. The context menu: the Menu key, or Shift+F10, opens the context menu of the focused component or its nearest ancestor that has one.
5. Enter with no modifiers presses the default button of the nearest window, or of the top layer, and Escape with no modifiers presses its cancel button.
6. Escape closes the top overlay that is not a toast.
7. Tab and Shift+Tab, without Control or Alt, move focus along the [tab order](#tab-order).

While a drag that carries data is in progress, Escape ends the drag before anything else hears it. Adapters pass a key on to the engine only when none of these used it; see [What reaches the engine](#what-reaches-the-engine).

## Characters

`CHAR` events carry text the user typed, as Unicode code points, after the keyboard layout and any input method have done their work. Characters outside the Basic Multilingual Plane, such as emoji, arrive as one event. `CharEvent.text()` gives the character as a string.

Characters go only to the focused component and are dropped when nothing has focus. Adapters never send control characters.

A key press and its character are separate events: typing "a" produces a `KEY` press of `Key.A`, a `CHAR` of `'a'`, and a `KEY` release. Use `KEY` for what the key does and `CHAR` for what it types.

### Text input mode

Some engines send no characters at all until a text field asks for them, as SDL-based engines do. Others use the signal to show an on-screen keyboard or input method window.

Strata handles this through focus. When a component that accepts text input gains focus, the host tells the engine to start text input, at the component's bounds. When focus leaves the last such component, it tells the engine to stop. The built-in text fields accept text input; a custom component opts in by overriding `acceptsTextInput()` to return true. The host also says what kind of text the field takes: a password box asks for `TextInputKind.SECRET`, so an engine can turn off input method composition, prediction and on-screen keyboard suggestions, and a custom component asks for it by overriding `textInputKind()`.

## Focus

At most one component per host has keyboard focus. Keys go to it. When nothing has focus, keys go to the content of the top dialog, or to the screen's content when no dialog is open. Characters go only to the focused component.

A component can take focus when it is attached, focusable, visible and enabled, and every ancestor is visible and enabled. While a dialog is open, only components in that dialog, or in overlays opened after it, can take focus. Built-in controls you work with the keyboard, such as buttons, checkboxes, sliders and text boxes, are focusable; [Components](components.md#what-only-some-components-have) lists them all. Labels and panels are not. Scroll panels and scroll bars are focusable once you call `setFocusable(true)`.

Focus moves when:

- a mouse button goes down: focus moves to the pressed component or its nearest focusable ancestor, or is cleared if there is none. This happens just before `POINTER_PRESS` fires, so a click on empty space clears focus. A press on a disabled component leaves focus where it is;
- Tab or Shift+Tab is pressed or repeats, without Control or Alt, and nothing consumed it;
- code calls `requestFocus()`, which returns false and changes nothing if the component cannot take focus;
- the host becomes inactive or active again; see [When the host is inactive](#when-the-host-is-inactive).

Focus is cleared when the focused component, or an ancestor, is hidden, disabled or removed. A screen that is rebuilt starts with nothing focused.

Some controls keep focus on a part inside them, their focus delegate. A table and a tree keep it on their inner list, a spinner on its text field and a split pane on its divider. To code the control is still the one that has focus: `requestFocus()` focuses the part, `isFocused()` is true while the part has focus, `setTabIndex` and the other tab settings apply to the control, and `FOCUS_GAINED` and `FOCUS_LOST` fire on the control right after the part. A press on a table's header or a spinner's step buttons focuses the part. The host's `focused()` returns the part.

### Focus within

`isFocusWithin()` is true while a component or one of its descendants has focus, panels included. To hear it change, handle `InputEvents.FOCUS_ENTER` and `InputEvents.FOCUS_EXIT` on the container. `FOCUS_EXIT` fires when focus leaves the container's subtree and `FOCUS_ENTER` when it comes in; moving between two fields inside the container fires neither. When focus moves from A to B the events fire in this order:

1. `FOCUS_LOST` on A.
2. `FOCUS_EXIT` on each ancestor of A that is not an ancestor of B, innermost first.
3. `FOCUS_ENTER` on each ancestor of B that is not an ancestor of A, outermost first.
4. `FOCUS_GAINED` on B.

`other()` is the component on the far side of the move, empty when focus came from or went to nothing. They fire on any component that has a handler for them, so a screen without handlers pays nothing. A form section that checks its fields when the user leaves it:

```java
--8<-- "input/FocusExamples.java:focus-exit"
```

There is no public method to clear focus from a component. Requesting focus on another component moves it.

### Why focus moved

`FocusEvent.reason()` says why, on `FOCUS_GAINED`, `FOCUS_LOST`, `FOCUS_ENTER` and `FOCUS_EXIT` alike:

| `FocusReason` | When |
|---|---|
| `POINTER` | A mouse button, a touch or a pen went down, or code moved focus while one was handled, such as from a `CLICK` handler |
| `KEYBOARD` | Tab, a mnemonic, a control's arrow keys, a controller's buttons sent as keys, or code moved focus while a key was handled |
| `CODE` | Code moved focus with no input being handled, such as a screen focusing its first field as it opens |
| `ACTIVATION` | The host became inactive or active again |

Strata works the reason out from the input being handled when focus moves, through the [cause](events.md#who-caused-it) every input runs in, so a radio group that calls `requestFocus()` on an arrow key reports `KEYBOARD`.

### Showing focus

`isFocusVisible()` is true while a component has focus that should be shown, as a ring or a lit face. Focus is shown when it came from the keyboard, or from code while the last press was a key's and not a pointer's, which includes code before anything was pressed. A key pressed while a component has focus shows it, and a pointer press anywhere in the host hides it, as browsers do for `:focus-visible`. Modifier keys alone change nothing. So a menu item clicked with the mouse keeps focus but does not look focused once the pointer leaves, and Tab shows where focus is.

The built-in controls draw their focus rings, lit faces and keyboard cursors only while focus is shown: buttons, checkboxes, radio buttons, toggle switches, sliders, the color picker, combo boxes, the calendar, the grid, list rings, navigation views, tabs, expanders, split panes and info bars. Text boxes, text areas and pickers show their focused look whenever they have focus, since the caret is there to type at. A theme reaches the state through the `focus_visible` [style state](extending.md#style-states), tried before `focused`, so a component's `menu.item.focus_visible` key wins over `menu.item.focused` while focus is shown.

### When the host is inactive

When the window or area a host sits in stops being the one that gets the keyboard, focus leaves the focused component, which hears `FOCUS_LOST` with `ACTIVATION`. When it becomes active again, focus comes back to that component, if it can still take focus, with `FOCUS_GAINED` and `ACTIVATION`, shown or hidden as it was. Swing reports this when the panel loses and regains keyboard focus, the GLFW bridge when the window loses and regains focus, and Minecraft when the game window does. Another adapter calls `activeChanged(boolean)` on the host or the screen stack.

## Tab order

Tab moves to the next component in tab order and Shift+Tab to the previous one, wrapping at both ends. With nothing focused, Tab goes to the first and Shift+Tab to the last.

The order holds every component that is focusable and tabbable, with all its ancestors visible and enabled, in the top dialog or, with no dialog open, in the screen's content. Tab never moves into a popup; a [flyout](controls.md#flyout) handles Tab itself and keeps focus inside it. It is sorted by tab index, lowest first, and components with equal indexes keep tree order: depth first, children in the order of `children()`, which `insert` and `move` change. The [drawing order](components.md#drawing-order) does not count, so raising a window does not change where Tab goes. A focused component that is not in the order, for example one focused by a click with tabbing turned off, counts as "nothing focused".

A [radio group](controls.md#radiobutton-and-radiogroup) is one tab stop: the selected button, or the first enabled one while nothing is selected. A button whose tab setting code changed keeps that setting.

Ctrl+Tab and Alt+Tab do not move focus, so they stay free for the engine or the operating system. A component that consumes Tab keeps focus where it is. Among the built-ins, only a flyout and a text box showing suggestions do.

```java
first.setTabIndex(1);
second.setTabIndex(2);
skipMe.setTabbable(false);   // still focusable by clicking
```

## Pointer input

Pointer positions are in UI units in host coordinates. The pointer targets the deepest component under it. `setHitTestVisible(false)` takes a component and everything inside it out of that search, so the pointer targets what lies below, usually the parent: a label inside a custom button then leaves the hover and the presses to the button. The component is still drawn, takes space and can take focus from the keyboard or code. `MouseButton` is `LEFT`, `RIGHT`, `MIDDLE`, `BACK` (button 4), `FORWARD` (button 5) or `UNKNOWN` for a button the adapter could not map. Scroll amounts are in wheel notches, and `ScrollEvent.kind()` says whether they came from a notched wheel (`ScrollKind.WHEEL`, 1 per notch) or a trackpad or smooth wheel (`ScrollKind.PRECISE`, fractions). Clicks, drags, hover and scrolling are covered in [Events](events.md#clicks).

### Devices

Every input event's cause names its device under `CauseKeys.DEVICE`: `MOUSE`, `KEYBOARD`, `PEN`, `TOUCH` or `CONTROLLER`. A pointer event also has `pointerId()`, 0 for the mouse, a pen and the first touch, and `pressure()`, from 0 to 1 and 1 for a device that reports none, which a drawing tool can follow. Each pointer id keeps its own presses, drags and click counts, so two fingers can press two buttons at once.

- Only the mouse and the pen hover. A touch hits the component under it without marking it hovered, so it shows no hover face or tooltip, and a touch that moves fires no `POINTER_MOVE`, `POINTER_ENTER` or `POINTER_EXIT`.
- A first touch held in place for 500 ms opens the context menu of the component under it, or of its nearest ancestor that has one. The release after that clicks nothing.
- A second finger that starts to drag ends both presses with no click and turns the gesture into a scroll. Each move scrolls the component under the midpoint of the first two touches, a precise notch for every 10 units, in the direction the fingers drag the content. The scroll lasts until every finger is up.
- A controller's buttons arrive as keys: the adapter sends its confirm button as Enter. A pad moves focus only inside controls that take arrow keys.

What an engine reports depends on the adapter. [Platform differences](../platforms/differences.md) lists which devices each one sends, and [Writing an adapter](../platforms/writing-an-adapter.md) shows how an adapter attaches a device to an input.

## What reaches the engine

Every input call into the host returns whether Strata used it, and adapters pass unused input on to the engine:

| Input | Counts as used when |
|---|---|
| Pointer move | The pointer is over a component, or a drag is in progress |
| Button press | It hit a component, closed a popup, or a dialog was open |
| Button release | It ended a press that started on a component |
| Scroll | A component consumed it |
| Key | A component consumed it, a shortcut, mnemonic, menu key, default or cancel button used it, Escape closed an overlay, or Tab moved focus |
| Character | The focused component consumed it |

A screen whose root fills the host is "a component" everywhere, so it claims every press. On an engine that shows its own widgets in the same screen as a Strata host, those widgets would never be clicked.
