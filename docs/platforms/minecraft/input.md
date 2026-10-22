# Input on Minecraft

`StrataScreen` takes its input from vanilla's screen callbacks and hands it to its host. Every callback except `mouseMoved` passes input Strata did not use on to vanilla's own `Screen` handling, so vanilla still reacts to it; [What reaches the engine](../../guides/input-focus.md#what-reaches-the-engine) lists when Strata counts input as used. Loader events that wrap these callbacks, such as NeoForge's screen key and mouse events, fire around a `StrataScreen` as around any vanilla screen.

Every event an input causes reports `Origin.User` as its [origin](../../guides/events.md#who-caused-it), and its identity is the local player's `GameProfile`, from vanilla's `Minecraft.getGameProfile()`. When Strata runs with Sponge, the host's input frames are pushed on Sponge's cause stack.

## Input mapping

| Vanilla callback | Becomes | Notes |
|---|---|---|
| `mouseMoved` | Pointer move | Shift, Control and Alt as held at the time, read from the game. Never passed on to vanilla. |
| `mouseClicked`, `mouseReleased` | Button press, release | Modifiers from the event. Strata counts clicks itself and ignores vanilla's double-click flag. |
| `mouseScrolled` | Scroll | Shift, Control and Alt as held at the time, so Shift with the wheel scrolls sideways. Horizontal amounts are flipped to Strata's direction. |
| `keyPressed`, `keyReleased` | Key | Modifiers from the event. Vanilla sends held keys as more presses, which become `REPEAT`. |
| `charTyped` | Character | No modifiers |

Mouse buttons 4 and 5 become `MouseButton.BACK` and `FORWARD`, and any button past those becomes `UNKNOWN`.

Minecraft sends no modifiers with pointer moves, scrolls or typed characters. For moves and scrolls the adapter reads the Shift, Control and Alt keys held at that moment from the game, so Shift+wheel and Shift+drag checks work as on Swing; Super is not read for them. Typed characters report `Modifiers.NONE`, because some keyboard layouts type characters with Control and Alt held (AltGr). Button presses and keys carry Shift, Control, Alt and Super.

Scroll amounts arrive from vanilla already multiplied by the player's scroll sensitivity, and as whole steps when the discrete scrolling option is on. A scroll whose amounts are whole numbers counts as `ScrollKind.WHEEL` and any other as `PRECISE`, so a sensitivity other than 1 turns a notched wheel into `PRECISE` scrolls.

Vanilla's buttons, checkboxes, cycle buttons and tabs act as the mouse button goes down, so the Minecraft platform turns on `Conventions.activateOnPress`. Strata's controls that follow that convention act on the press in game and on the click in Swing; [Platform differences](../differences.md#at-a-glance) lists them.

Vanilla handles its fullscreen and screenshot keys (F11 and F2 unless the player rebinds them) before any screen sees the press, so Strata never receives those presses.

## Files dropped on the window

Files dragged from the desktop onto the game window reach the Strata component under the pointer as a drop of `Transfer.files(paths)`, through vanilla's `Screen.onFilesDrop`, in `StrataScreen` and `StrataContainerScreen` alike. The game reports nothing while the files are dragged over the window, only the drop, so a component hears `DRAG_OVER` and `DROP` together when the files land, at the pointer's last position. No cursor or drop line shows before then. A component accepts them as it would any [drop](../../guides/drag-and-drop.md):

```java
// A drop zone for images from the desktop
zone.onEvent(InputEvents.DRAG_OVER, event -> {
    if (event.transfer().has(DataFormat.FILES)) {
        event.accept();
    }
}).onEvent(InputEvents.DROP, event -> event.transfer().get(DataFormat.FILES).ifPresent(this::importImages));
```

Drags inside a Strata screen, such as reordering a list's rows or moving text between text boxes, need nothing from the game and work as on any engine. The drag image draws through the canvas. Dragging out of the game window to other applications is not supported.

The game's clipboard holds text only. Strata keeps the other formats a copy carries, such as a list's rows, and hands them back to a paste in the same screen while the clipboard still holds the copied text. Another program sees the text.

## Devices

The game reports a mouse and a keyboard only, so input reaches Strata without an `InputContext`: pointer input is `InputContext.MOUSE` and keys are `InputContext.KEYBOARD`. A controller mod could send its buttons as keys with `new InputContext(Device.CONTROLLER, 0, 1f)`. The adapter does not do this itself.

## Keys are SDL scancodes

Since 26.3, Minecraft's key values are SDL scancodes, which name physical key positions. That matches Strata's `Key`, which also names positions, so `Key.A` is the key in the A position whatever the player's layout prints on it.

The adapter maps scancodes through the names in vanilla's `InputConstants`, never through raw numbers. Keys that `InputConstants` names and Strata has no constant for, F13 to F24 and the keypad comma, arrive as `Key.UNKNOWN`. So do keys that 26.3's `InputConstants` does not name at all, among them keypad divide, keypad subtract and keypad decimal, even though Strata has `Key.KP_DIVIDE`, `KP_SUBTRACT` and `KP_DECIMAL`. The context menu key has no name there either, so the adapter matches it by SDL's own scancode names and maps it to `Key.MENU`.

`MinecraftInput` holds the mapping as public static methods, for code that overrides vanilla's callbacks in a `StrataScreen` subclass and wants Strata's types:

| Method | Returns |
|---|---|
| `key(int scancode)` | The `Key` for `KeyEvent.key()`, or `Key.UNKNOWN` |
| `button(int button)` | The `MouseButton` for a vanilla button number |
| `modifiers(InputWithModifiers input)` | The modifiers of a vanilla key or mouse button event |
| `held(Minecraft minecraft)` | Shift, Control and Alt as held now, for input vanilla reports without modifiers, such as moves and scrolls |
| `scrollX(double scrollX)` | Vanilla's horizontal scroll in Strata's direction |
| `scrollKind(double scrollX, double scrollY)` | `WHEEL` for whole amounts, `PRECISE` otherwise |

## Typed characters need text input mode

Minecraft 26.3 sends no characters until a text field reports focus. Strata does this through [text input mode](../../guides/input-focus.md#text-input-mode): when a Strata text box, or any component whose `acceptsTextInput()` is true, gains focus, the adapter calls `Minecraft.onTextInputFocusChange` with the hosting screen, and it stops text input when focus leaves the last such component. If a custom component takes typing but never receives `CHAR` events, it is missing that override. The adapter does not pass the field's area on, so the game is never told where the field is for placing input method windows.

A focused text box consumes the keys that type characters, so vanilla never sees them. In a container screen, that keeps the inventory key from closing the screen while the player types.

A password box asks for text input of the `SECRET` kind, and the adapter starts text input for it as for any other field. The game's text input takes no input type, so the adapter cannot turn off the input method or the system's suggestions for a secret. The adapter never logs typed characters or drawn text, and vanilla's narration reads only the screen's title, so nothing of a password reaches either.

## Escape

Vanilla closes a screen on an Escape press that reaches it, so Escape closes a Strata screen unless something in Strata consumes it first. Strata consumes Escape when it closes something smaller than the screen: the top overlay other than a toast, such as a popup or dialog, an open combo box or menu, a date or time picker that is open or holds an edit not yet committed, a text box's suggestion list, or a closable window that something listens to for `CLOSE` or `CLOSED` or that is shown in an overlay. A message box answers Escape through its cancel button. A plain focused text box does not consume it, so Escape closes the screen while the player types.

Escape that nothing used closes the screen only once the screen's `closeRequested` agrees. A screen with unsaved changes can refuse, show a message box and close itself from the box's Discard button; [Screens](screens.md#closing) lists what else asks. To keep a screen open on Escape whatever it answers, subclass `StrataScreen` and override vanilla's `shouldCloseOnEsc()` to return false. Strata still sees Escape first, so it still closes popups and dialogs. A handler on the screen's content that consumes Escape also keeps the screen open, but it runs before Strata closes the top popup or dialog, so that press closes nothing:

```java
root.onEvent(InputEvents.KEY, event -> {
    if (event.key() == Key.ESCAPE) {
        event.consume();
    }
});
```

## Strata claims the whole screen

A button press counts as used whenever it hits a Strata component, and a root anchored with `Anchor.FILL` covers the whole screen. Vanilla widgets added to a `StrataScreen` subclass would therefore never be clicked. Vanilla also draws them before the Strata tree, so anything Strata draws covers them. Mixing vanilla widgets and Strata components in one screen is not supported yet.

## Keyboard conventions

Strata screens follow the desktop's keyboard conventions, which Minecraft's own screens lack: they have no mnemonics, menu bars or context menus. In a Strata screen:

- Alt with an underlined letter presses a button or opens a menu, and Alt alone or F10 enters a menu bar. Vanilla binds nothing to Alt, so the platform reports `Conventions.mnemonics` as true. A mod whose key bindings use Alt keeps them, since a key Strata does not use goes on to vanilla.
- The context menu key (`Key.MENU`) or Shift+F10 opens a component's context menu.
- Enter presses a default button and Escape a cancel button.
- Tab moves through the color picker and a split pane's divider as well as the fields, buttons, lists, tables, trees and grids.

The [keyboard shortcuts](../../reference/shortcuts.md) reference lists every key.

## The shortcut modifier

The shortcut modifier is the one vanilla uses for its own edit shortcuts: Command on macOS, which Strata reports as `Modifier.SUPER`, and Control elsewhere. `MinecraftPlatform.conventions()` reports it as `Conventions.shortcutModifier`. `KeyStroke.shortcut` strokes take it, and so do the edit keys of Strata's text fields: copy, cut, paste, select all, undo and redo. On a Mac, copy in a Strata text box is Command+C, as in vanilla's.

Strata's text fields undo with the shortcut modifier and Z, and redo with Y or Shift and Z, which vanilla's edit boxes cannot. They consume those keys only when there is something to undo or redo, so a mod's own binding on Ctrl+Z still works on an empty history.

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
| Modifiers with pointer moves and scrolls | Shift, Control and Alt, read from the game when the event arrives | Reported |
| Modifiers with typed characters | Never reported (`NONE`) | Reported |
| Modifiers with button presses and keys | Reported | Reported |
| Shortcut modifier | Command on macOS, Control elsewhere | Command on macOS, Control elsewhere |
| Key repeat | Held keys repeat at the system rate | Same |
| Typed characters | Only while a component that accepts text input has focus | Always |
| Secret text input | Started like any other field; the game has no mode for secrets | Input methods off on the panel while a secret field has focus |
| Horizontal scrolling | From the device, flipped to Strata's direction, and Shift plus the wheel | Shift plus the vertical wheel |
| When buttons act | On the press | On the click |
| Escape | Closes the screen unless Strata consumes it, once `closeRequested` agrees | Does nothing unless you handle it |
| Clipboard | Vanilla's, which holds text only; other formats of a copy stay in the screen | The system clipboard with text, files, images and MIME text; none when headless |
| Files dropped from the desktop | `DROP` when they land, with no drag before it | `DRAG_ENTER`, `DRAG_OVER` and `DROP` as they move |
| Missing sprite | Vanilla's missing-texture pattern | Magenta |
| Sprite location | `assets/<namespace>/textures/gui/sprites/<path>.png`, through vanilla's gui atlas | `assets/<namespace>/sprites/<path>.png` on the class path, or wherever a `SpriteResolver` points |
| Nine-slice slicing | From the sprite's `.png.mcmeta`; the `border` argument is ignored | From the `border` argument, in image pixels |
| Nine-slice edges and center | Tiled, unless the `.mcmeta` sets `stretch_inner` | Stretched |
| Cursors | All but the grab, grabbing, copy, no-drop and help cursors, which show their fallbacks | Every kind; headless, the five custom ones fall back |
| Screen stack | Vanilla's, with one Strata screen per `StrataScreen`; a screen opened above another returns to it | `StrataPanel.screens()` |
| `Strata.screens()` | Opens a new `StrataScreen`; works once the Strata mod has loaded | The stack of the panel marked with `StrataPanel.setDefault()`; throws until one is marked |
| Default theme (`ThemeRef.platform()`) | `MinecraftThemeSource.VANILLA` | `strata:flat`, or the theme `SwingThemeSource.setDefaultTheme` names |
| `hidesBackground()` | No effect | Skips drawing the screens below |
| Pausing | A single-player world pauses unless `isPauseScreen()` is false | Not applicable |
| UI thread | The client (render) thread; `Strata.uiThread()` queues on the game's task queue, even from that thread | The event dispatch thread |
| Frames | Every frame; frame scheduling is ignored | On demand |
| Density | The GUI scale | The panel's scale times the display's |
| Shapes | Core's fallback, without anti-aliasing; round clips are square | Java2D |
| Opacity | Everything except item visuals | Everything |
| Pixel buffers | A texture per buffer | An image sharing the array |
| File dialogs | Not offered | The system's |
| Plural rules | Core's, since vanilla has none | Core's |
| Right-to-left text | Runs reversed by core; Arabic draws unjoined | Shaped by Java2D |

[Screens on Minecraft](screens.md) covers the screen rows, and [Drawing on Minecraft](drawing.md) the drawing rows.
