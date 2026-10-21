# Input on Minecraft

`StrataScreen` takes its input from vanilla's screen callbacks and hands it to its host. Every callback except `mouseMoved` passes input Strata did not use on to vanilla's own `Screen` handling, so vanilla still reacts to it; [What reaches the engine](../../guides/input-focus.md#what-reaches-the-engine) lists when Strata counts input as used. Loader events that wrap these callbacks, such as NeoForge's screen key and mouse events, fire around a `StrataScreen` as around any vanilla screen.

Every event an input causes reports `Origin.User` as its [origin](../../guides/events.md#who-caused-it), and its identity is the local player's `GameProfile`, from vanilla's `Minecraft.getGameProfile()`. When Strata runs with Sponge, the host's input frames are pushed on Sponge's cause stack.

## Input mapping

| Vanilla callback | Becomes | Notes |
|---|---|---|
| `mouseMoved` | Pointer move | No modifiers. Never passed on to vanilla. |
| `mouseClicked`, `mouseReleased` | Button press, release | Modifiers from the event. Strata counts clicks itself and ignores vanilla's double-click flag. |
| `mouseScrolled` | Scroll | No modifiers. Horizontal amounts are flipped to Strata's direction. |
| `keyPressed`, `keyReleased` | Key | Modifiers from the event. Vanilla sends held keys as more presses, which become `REPEAT`. |
| `charTyped` | Character | No modifiers |

Mouse buttons 4 and 5 become `MouseButton.BACK` and `FORWARD`, and any button past those becomes `UNKNOWN`.

Minecraft sends no modifiers with pointer moves, scrolls or typed characters, so those events report `Modifiers.NONE`. A Shift+scroll or Ctrl+drag check that works on Swing never fires in game. If you need modifier state during a drag, remember it from the `POINTER_PRESS` that started it. Button presses and keys carry Shift, Control, Alt and Super.

Scroll amounts arrive from vanilla already multiplied by the player's scroll sensitivity, and as whole steps when the discrete scrolling option is on. A scroll whose amounts are whole numbers counts as `ScrollKind.WHEEL` and any other as `PRECISE`, so a sensitivity other than 1 turns a notched wheel into `PRECISE` scrolls.

Vanilla's buttons, checkboxes, cycle buttons and tabs act as the mouse button goes down, so the Minecraft platform turns on `Conventions.activateOnPress`. Strata's controls that follow that convention act on the press in game and on the click in Swing; [Platform differences](../differences.md#at-a-glance) lists them.

Vanilla handles its fullscreen and screenshot keys (F11 and F2 unless the player rebinds them) before any screen sees the press, so Strata never receives those presses.

## Keys are SDL scancodes

Since 26.3, Minecraft's key values are SDL scancodes, which name physical key positions. That matches Strata's `Key`, which also names positions, so `Key.A` is the key in the A position whatever the player's layout prints on it.

The adapter maps scancodes through the names in vanilla's `InputConstants`, never through raw numbers. Keys that `InputConstants` names and Strata has no constant for, F13 to F24 and the keypad comma, arrive as `Key.UNKNOWN`. So do keys that 26.3's `InputConstants` does not name at all, among them keypad divide, keypad subtract, keypad decimal and the Menu key, even though Strata has `Key.KP_DIVIDE`, `KP_SUBTRACT`, `KP_DECIMAL` and `MENU`.

`MinecraftInput` holds the mapping as public static methods, for code that overrides vanilla's callbacks in a `StrataScreen` subclass and wants Strata's types:

| Method | Returns |
|---|---|
| `key(int scancode)` | The `Key` for `KeyEvent.key()`, or `Key.UNKNOWN` |
| `button(int button)` | The `MouseButton` for a vanilla button number |
| `modifiers(InputWithModifiers input)` | The modifiers of a vanilla key or mouse button event |
| `scrollX(double scrollX)` | Vanilla's horizontal scroll in Strata's direction |
| `scrollKind(double scrollX, double scrollY)` | `WHEEL` for whole amounts, `PRECISE` otherwise |

## Typed characters need text input mode

Minecraft 26.3 sends no characters until a text field reports focus. Strata does this through [text input mode](../../guides/input-focus.md#text-input-mode): when a Strata text box, or any component whose `acceptsTextInput()` is true, gains focus, the adapter calls `Minecraft.onTextInputFocusChange` with the hosting screen, and it stops text input when focus leaves the last such component. If a custom component takes typing but never receives `CHAR` events, it is missing that override. The adapter does not pass the field's area on, so the game is never told where the field is for placing input method windows.

A focused text box consumes the keys that type characters, so vanilla never sees them. In a container screen, that keeps the inventory key from closing the screen while the player types.

A password box asks for text input of the `SECRET` kind, and the adapter starts text input for it as for any other field. The game's text input takes no input type, so the adapter cannot turn off the input method or the system's suggestions for a secret. The adapter never logs typed characters or drawn text, and vanilla's narration reads only the screen's title, so nothing of a password reaches either.

## Escape

Vanilla closes a screen on an Escape press that reaches it, so Escape closes a Strata screen unless something in Strata consumes it first. Strata consumes Escape when it closes something smaller than the screen: the top overlay other than a toast, such as a popup or dialog, an open combo box or menu, a date or time picker that is open or holds an edit not yet committed, a text box's suggestion list, or a closable window that something listens to for `CLOSE` or `CLOSED` or that is shown in an overlay. A plain focused text box does not consume it, so Escape closes the screen while the player types.

To keep a screen open on Escape, subclass `StrataScreen` and override vanilla's `shouldCloseOnEsc()` to return false. Strata still sees Escape first, so it still closes popups and dialogs. A handler on the screen's content that consumes Escape also keeps the screen open, but it runs before Strata closes the top popup or dialog, so that press closes nothing:

```java
root.onEvent(InputEvents.KEY, event -> {
    if (event.key() == Key.ESCAPE) {
        event.consume();
    }
});
```

## Strata claims the whole screen

A button press counts as used whenever it hits a Strata component, and a root anchored with `Anchor.FILL` covers the whole screen. Vanilla widgets added to a `StrataScreen` subclass would therefore never be clicked. Vanilla also draws them before the Strata tree, so anything Strata draws covers them. Mixing vanilla widgets and Strata components in one screen is not supported yet.

## macOS

On macOS, vanilla uses Command for its edit shortcuts. Strata reports Command as `Modifier.SUPER`, and its text box checks Control on every platform, so in a Strata text box on a Mac, copy, cut, paste and select all are Control+C, X, V and A. A platform shortcut modifier is planned.

## Differences from Swing

The [Swing adapter](../swing.md) is the quickest place to work on a screen. When the same screen runs in game, these behaviors change. [Platform differences](../differences.md) explains each topic.

| Topic | Minecraft 26.3 | Swing |
|---|---|---|
| One UI unit | One GUI pixel, so it follows the GUI scale | `scale` pixels, fixed when the panel is made |
| Rounding | Each edge of a fill, sprite or clip moves down to a whole GUI pixel; text positions round to the nearest pixel | None. Java2D draws fractional coordinates. |
| Font | The player's font, from resource packs | An AWT font, Dialog 9 by default |
| Text widths | Whole GUI pixels, as vanilla's font reports them; bold adds vanilla's extra pixel per character | Fractional |
| Line height | The font's line height, 9 for the default font | The AWT font's height |
| Monospaced text | The normal font | Monospaced |
| Modifiers with pointer moves, scrolls and characters | Never reported (`NONE`) | Reported |
| Modifiers with button presses and keys | Reported | Reported |
| Edit shortcuts on macOS | Control in a Strata text box, Command in vanilla's | Control |
| Key repeat | Held keys repeat at the system rate | Same |
| Typed characters | Only while a component that accepts text input has focus | Always |
| Secret text input | Started like any other field; the game has no mode for secrets | Input methods off on the panel while a secret field has focus |
| Horizontal scrolling | From the device, flipped to Strata's direction | Shift plus the vertical wheel |
| When buttons act | On the press | On the click |
| Escape | Closes the screen unless Strata consumes it | Does nothing unless you handle it |
| Clipboard | Vanilla's | The system clipboard; none when headless |
| Missing sprite | Vanilla's missing-texture pattern | Magenta |
| Sprite location | `assets/<namespace>/textures/gui/sprites/<path>.png`, through vanilla's gui atlas | `assets/<namespace>/sprites/<path>.png` on the class path, or wherever a `SpriteResolver` points |
| Nine-slice slicing | From the sprite's `.png.mcmeta`; the `border` argument is ignored | From the `border` argument, in image pixels |
| Nine-slice edges and center | Tiled, unless the `.mcmeta` sets `stretch_inner` | Stretched |
| `NOT_ALLOWED` cursor | Shown | Shows the default arrow |
| Screen stack | Vanilla's, with one Strata screen per `StrataScreen`; a screen opened above another returns to it | `StrataPanel.screens()` |
| `Strata.screens()` | Opens a new `StrataScreen`; works once the Strata mod has loaded | The stack of the panel marked with `StrataPanel.setDefault()`; throws until one is marked |
| Default theme (`ThemeRef.platform()`) | `MinecraftThemeSource.VANILLA` | `strata:flat`, or the theme `SwingThemeSource.setDefaultTheme` names |
| `hidesBackground()` | No effect | Skips drawing the screens below |
| Pausing | A single-player world pauses unless `isPauseScreen()` is false | Not applicable |
| UI thread | The client (render) thread | The event dispatch thread |

[Screens on Minecraft](screens.md) covers the screen rows, and [Drawing on Minecraft](drawing.md) the drawing rows.
