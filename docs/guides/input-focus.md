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

Put screen-wide shortcuts on the screen's content, as above. Keys bubble from the focused component, so a shortcut there fires unless something focused consumed the key first. A focused text box consumes plain typing keys, but not Control combinations it does not use, so Ctrl+S still reaches the content while the user is typing. Keys pressed inside an overlay bubble to the overlay's content and stop there, so a shortcut that should also work while a popup or dialog has focus needs a handler on that content too.

`ModifierPolicy.matches(required, held)` is the test `is` uses, and it works on the modifiers of any event, such as a click that does something else with Control held:

```java
--8<-- "events/EventExamples.java:modifier-click"
```

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

- a mouse button goes down: focus moves to the pressed component or its nearest focusable ancestor, or is cleared if there is none. This happens just before `POINTER_PRESS` fires, so a click on empty space clears focus;
- Tab or Shift+Tab is pressed or repeats, without Control or Alt, and nothing consumed it;
- code calls `requestFocus()`, which returns false and changes nothing if the component cannot take focus.

Focus is cleared when the focused component, or an ancestor, is hidden, disabled or removed. A screen that is rebuilt starts with nothing focused.

There is no public method to clear focus from a component. Requesting focus on another component moves it.

## Tab order

Tab moves to the next component in tab order and Shift+Tab to the previous one, wrapping at both ends. With nothing focused, Tab goes to the first and Shift+Tab to the last.

The order holds every component that is focusable and tabbable, with all its ancestors visible and enabled, in the top dialog or, with no dialog open, in the screen's content. Tab never moves into a popup; a [flyout](controls.md#flyout) handles Tab itself and keeps focus inside it. It is sorted by tab index, lowest first, and components with equal indexes keep tree order: depth first, children in the order of `children()`, which `insert` and `move` change. The [drawing order](components.md#drawing-order) does not count, so raising a window does not change where Tab goes. A focused component that is not in the order, for example one focused by a click with tabbing turned off, counts as "nothing focused".

Ctrl+Tab and Alt+Tab do not move focus, so they stay free for the engine or the operating system. A component that consumes Tab keeps focus where it is. Among the built-ins, only a flyout and a text box showing suggestions do.

```java
first.setTabIndex(1);
second.setTabIndex(2);
skipMe.setTabbable(false);   // still focusable by clicking
```

## Pointer input

Pointer positions are in UI units in host coordinates. `MouseButton` is `LEFT`, `RIGHT`, `MIDDLE`, `BACK` (button 4), `FORWARD` (button 5) or `UNKNOWN` for a button the adapter could not map. Scroll amounts are in wheel notches, and `ScrollEvent.kind()` says whether they came from a notched wheel (`ScrollKind.WHEEL`, 1 per notch) or a trackpad or smooth wheel (`ScrollKind.PRECISE`, fractions). Clicks, drags, hover and scrolling are covered in [Events](events.md#clicks).

## What reaches the engine

Every input call into the host returns whether Strata used it, and adapters pass unused input on to the engine:

| Input | Counts as used when |
|---|---|
| Pointer move | The pointer is over a component, or a drag is in progress |
| Button press | It hit a component, closed a popup, or a dialog was open |
| Button release | It ended a press that started on a component |
| Scroll | A component consumed it |
| Key | A component consumed it, Escape closed an overlay, or Tab moved focus |
| Character | The focused component consumed it |

A screen whose root fills the host is "a component" everywhere, so it claims every press. On an engine that shows its own widgets in the same screen as a Strata host, those widgets would never be clicked.
