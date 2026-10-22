# Commands and shortcuts

A document editor has a Save button on its tool bar, a Save item in its File menu and Ctrl+S on the keyboard. All three should do the same thing, be enabled at the same time and show the same caption. A command is a value that holds those parts once, and each control that shows it follows it. Shortcuts, mnemonics, default and cancel buttons and context menus complete the keyboard side of a screen, and this page covers them in the order a key meets them.

## Commands

`Command` (`org.inspirenxe.strata.api.command`) is a sealed interface with two records:

| Command | Holds | Shown by |
|---|---|---|
| `Command.Action` | A caption, a shortcut, an enabled `Source<Boolean>` and a `Runnable` | `Button`, `MenuItem` |
| `Command.Toggle` | A caption, a shortcut, an enabled `Source<Boolean>` and a `State<Boolean>` | `CheckBox`, `ToggleSwitch`, `ToggleButton`, `CheckMenuItem` |

`Command.of(text, run)` makes an action and `Command.toggle(text, state)` a toggle, each with a `Text` or a `String` caption, always enabled and with no shortcut. `withShortcut(stroke)` and `withEnabled(source)` return a changed copy, so a command is built in one chain:

```java
--8<-- "commands/CommandExamples.java:save-command"
```

Any `Source<Boolean>` can say when the command is enabled: a `State`, a lambda that reads states, or `State.combine(...)`. See [Data binding](binding.md) for sources.

A control takes a command with `setCommand`, which `HasCommand` declares. Pass null to clear it.

```java
--8<-- "commands/CommandExamples.java:save-screen"
```

What a control does with its command:

- It takes the command's text as its caption when the command is set. Clearing the command keeps the text and takes the bindings off.
- A button, check box, toggle switch or toggle button binds `Component.ENABLED` to the command's enabled source, so the command decides. A menu item or check menu item has no bindings; it keeps its own flag and is usable while that flag is set and the command is enabled, and `isEnabled()` reports both.
- A toggle command also ties the control's selected state to the command's state, both ways. Every control showing one toggle shows the same state, and setting the state from code moves them all.
- A button runs its action after `PRESS` and `PRESSED` fired and nothing canceled the press, and after a call to `press()`. A menu item runs its action after `Menu.CHOSEN`. A check menu item flips the toggle's state with the item and does not run it a second time. The command runs only while its enabled source gives true.
- The action runs inside the user's input frame with the command pushed on the cause chain, so what it changes reports `Origin.User` when a person pressed the control or its key. See [Events](events.md#who-caused-it).
- A menu item that shows a command and has no hint text of its own draws the command's key stroke as its hint, such as "Ctrl+S".

Entries such as menu items take no bindings, so they read the command each time the menu asks. Their behavior is the same as a control's.

The toggle shared by a check menu item, a toggle switch and a check box:

```java
--8<-- "commands/CommandExamples.java:toggle-screen"
```

## Key strokes

A `KeyStroke` names a key with the exact modifiers that must be held. `KeyStroke.of(Key.ENTER, Modifier.CONTROL)` is a plain stroke. `KeyStroke.shortcut(Key.S)` also holds the platform's shortcut modifier, which is Control on most platforms and Command where an adapter reports `Modifier.SUPER` (macOS). A command written with `shortcut` is therefore Ctrl+S on Windows and Command+S on a Mac without a line of platform code.

A stroke matches exactly: Ctrl+S does not match Ctrl+Shift+S. `KeyEvent.is(KeyStroke)` tests a press or repeat in a handler on the host's platform, and `KeyEvent.shortcutModifier()` names the modifier.

`ComponentFactory.keyText(stroke)` returns the stroke as text for a hint:

- The modifiers come in the order Control, Alt, Shift and Super, each through the translation key `strata.modifier.<name>`, joined by `strata.modifier.separator`.
- Letters, digits, function keys and punctuation show what they show. The keypad goes through `strata.key.keypad`, and every other key through `strata.key.<constant in lower case>`.
- Core's English has all of them ("Ctrl", "Esc", "PgUp"). A translator can answer them with the platform's own symbols. The keys are in [Translation keys](../reference/translation-keys.md).

## Shortcuts

`ComponentFactory.shortcuts()` returns the host's `Shortcuts`. A command added there runs from its key stroke:

```java
ui.shortcuts().add(save);                      // anywhere on the host
ui.shortcuts().add(composer, send);            // only while focus is inside composer
```

Both return a `Subscription` that removes the command. Shortcuts belong to the host, not to the tree, so a screen that rebuilds should close the subscription it made before it adds the command again, as the example above does. A command without a stroke is held and never matches.

A shortcut scoped to a component runs while focus is on that component or inside it. The example below gives everything in one panel a Ctrl+Enter and names the stroke on screen with `keyText`:

```java
--8<-- "commands/CommandExamples.java:scoped"
```

### Lookup order

A key goes along the focus path first, so a text box keeps Ctrl+A, Ctrl+C and the other keys it uses. If nothing consumed a press or a repeat, the host looks for a command to run, in this order:

1. Commands scoped to the key's target, then to each ancestor, innermost first. Inside one scope, the command added first wins.
2. Commands added for the host as a whole, only while no modal dialog is open.
3. The commands of the visible, enabled menu bars in the top dialog, or in the screen's content when no dialog is open. A menu bar gives the commands of the usable items in its menus and their submenus.

The first command whose stroke matches and whose enabled source gives true runs and uses up the key. A matching command that is disabled is skipped, so the next match gets its turn and, with none, the key goes on. A command that runs does so inside the key's input frame on the UI thread.

A modal dialog blocks the host's own shortcuts and the menu bars behind it, so Ctrl+S does not save the document under an open message box. Commands scoped to something inside the dialog still match. Popups, flyouts and menus are not modal, so the host's shortcuts keep working while one has focus. A menu opened from a button or as a context menu adds no shortcuts of its own.

A text box, text area and password box use the shortcut modifier for select all, copy, cut, paste, undo and redo, and a key with Super held does not count as typing, so a host shortcut such as Command+S runs from inside a field.

## Mnemonics

A caption marks a mnemonic with an underscore before the letter, as in `"_Save"`. The underscore is never drawn. Translations place their own, so `"_Speichern"` and `"_Save"` can differ. A doubled underscore stands for a single one, and an underscore at the very end stays.

- Buttons, drop-down buttons, check boxes, radio buttons, toggle switches, tabs, expander titles, menu entries and menu bar titles always read mnemonics. A label does so after `setMnemonicParsing(true)`.
- While Alt is held, or after the keyboard opened a menu, the letter is underlined.
- Alt with the letter acts on the component that has it in the top layer, when nothing closer consumed the key. A button presses, a drop-down button opens its popup, a box toggles, a radio button selects, a switch flips, a tab selects, an expander toggles, and a menu bar title opens its menu. A label moves focus to the next tab stop after it, which is how a caption names its field.
- When several components in the layer share the letter, each press moves focus to the next of them without acting.
- In an open menu, a letter or digit alone chooses the one usable entry that has it, or highlights the entries that share it in turn. A letter no entry claims moves to the next entry whose text starts with it.
- Alt pressed and released alone, or F10, hands the keys to the first visible, enabled menu bar in the top dialog or the content. The bar highlights its first title, Left and Right move between titles, Down, Enter or Space open one, a title's letter opens it, and Escape or Alt again lets go. Focus never moves to the bar.

An adapter reports through `Conventions.mnemonics` whether the platform uses them. With it off, captions still drop their markers, and Alt, Alt with a letter and F10 do nothing. See [Writing an adapter](../platforms/writing-an-adapter.md).

## Default and cancel buttons

`Button.setDefault(true)` makes a button the one that Enter presses, and `setCancel(true)` the one that Escape presses:

```java
--8<-- "commands/CommandExamples.java:default"
```

- A default button draws the optional style key `button.default` in place of its idle face, falling back to `button.normal`.
- Enter or keypad Enter with no modifiers presses the default button of the key's scope. The scope is the nearest `Window` around the focused component, or else the screen's content or the overlay it is in. The first default or cancel button in tree order that is visible and enabled, and not inside a nested window, is the one pressed. A press from the key fires `PRESS` for the user, so handlers and requests see it as the user's.
- A text box consumes Enter itself only while a submit handler is on its path, so Enter in a text box reaches the default button otherwise.
- Escape with no modifiers presses the scope's cancel button before the host closes the top overlay. A closable window that acts on Escape presses its cancel button instead of closing; one that cannot be closed still ignores Escape.
- A message box's default result names its default button, and its dismiss result its cancel button. The default button takes focus when the box opens, so Enter answers it at once. See [MessageBox](controls.md#messagebox).

## Context menus

Every control that has a tooltip, and every container, can have a context menu: `setContextMenu(popup)` on the component, null to clear it. The menu opens at the pointer on a right click, on a right press where the platform's conventions act on press, and after a long press with a finger. The Menu key, or Shift+F10, opens the key target's menu below the target. A component without a menu hands the request to its nearest ancestor that has one. Before the menu opens the component fires the request `HasContextMenu.SHOW_CONTEXT_MENU` with the popup, and a handler can cancel it or rewrite the menu, for example to list the commands that fit the row that was pressed. See [Controls](controls.md) for the event and the text fields' default menus.

## What a key meets

A press that no handler on the focus path consumed goes through these steps in order, and the first that acts uses it up:

1. A drag that carries data ends on Escape, before anything else hears the key.
2. The `KEY` event runs along the focus path, to the focused component, or to the top dialog's content or the screen's content when nothing has focus. While a menu bar holds the keys after Alt alone, the bar comes first.
3. Shortcuts, in the lookup order above (on repeats too).
4. A mnemonic: Alt with a letter or number-row digit and optionally Shift, no Control or Super. F10 alone enters the menu bar.
5. The context menu: the Menu key with no modifiers, or Shift+F10.
6. The default button for Enter, or the cancel button for Escape, with no modifiers.
7. Escape closes the top overlay that is not a toast, if its `Closable.CLOSE` request is not canceled.
8. Tab and Shift+Tab move focus, without Control or Alt. See [Input and focus](input-focus.md).

Alt pressed and released alone, with no other key or press between, hands the keys to the menu bar when nothing consumed the release. The host returns whether it used a key, and the adapter passes unused keys on to the engine.

## Defaults

| What | Default |
|---|---|
| A command's shortcut | None |
| A command's enabled source | Always true |
| Shortcut modifier | `Modifier.CONTROL` (`Conventions.DEFAULT`); an adapter reports its own |
| Mnemonics | On (`Conventions.DEFAULT`) |
| `Button.isDefault()`, `isCancel()` | false |
| `Label.setMnemonicParsing` | false |
| A message box's default result | Its first result |
