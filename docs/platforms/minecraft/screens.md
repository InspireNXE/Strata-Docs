# Screens on Minecraft

Minecraft has a screen system of its own, so Strata-Minecraft shows each Strata screen in a vanilla screen. `StrataScreen`, in `org.inspirenxe.strata.minecraft`, is a subclass of vanilla's `Screen` that holds one Strata screen and one host. Everything on the [Screens](../../guides/screens.md) guide applies; this page covers what Minecraft adds.

## Opening a screen

`Strata.screens()` opens a screen, and the code that calls it names nothing from Minecraft:

```java
Strata.screens().open(new SettingsScreen());
```

The controller makes a new `StrataScreen` for the screen, titled with the screen's `title()`. Vanilla reads that title for narration. Open screens from whatever starts your UI: a key mapping, a command, a packet handler or a button on another screen. Each loader registers those triggers in its own way, so see your loader's documentation for the trigger itself; the call to `open` is the same on every loader.

`open(screen)` uses the platform's default theme, which on Minecraft is `MinecraftThemeSource.VANILLA` (`minecraft:vanilla`). It draws controls with vanilla's own sprites and follows the player's resource packs; see [Drawing on Minecraft](drawing.md). `ThemeRef.platform()` names the same theme, so a screen can change a few keys for itself:

```java
Strata.screens().open(new SettingsScreen(), ThemeRef.platform()
        .override(Button.STYLE_NORMAL, Paint.solid(0xFF2E7D32)));
```

Where the new screen goes depends on what vanilla shows:

- When a `StrataScreen` is showing, the new screen opens above it, as if the showing screen's handle had opened it. See [Screens above screens](#screens-above-screens).
- Otherwise the new screen replaces whatever vanilla shows, and closing it returns to the game.

`Strata.screens()` throws `IllegalStateException` until Strata-Minecraft has started, which happens when the Strata mod loads; see [Minecraft](index.md). `open` also throws `IllegalStateException` if it runs before the game itself exists.

## The lifecycle

| Vanilla calls | `StrataScreen` does |
|---|---|
| `init()`, the first time | Creates a host, sizes it to the screen and opens the Strata screen, which runs its `build` and then `opened` |
| `init()` again, after a window resize or a GUI scale change | Resizes the host. The tree is kept. |
| `extractRenderState(...)` | Lets vanilla draw its widgets, then has the host lay out if needed and draw the Strata tree on top |
| `removed()` | Closes the Strata screen, so its `closed()` runs, and drops the host. A screen removed because another opened above it keeps its host instead. |

Vanilla draws its usual screen background before `extractRenderState`, so the background shows wherever the Strata tree draws nothing.

A `StrataScreen` that is shown again after it was removed makes a new host and builds a new tree.

## Closing

A Strata screen can refuse to close, as the [Screens](../../guides/screens.md) guide describes. On Minecraft the asking happens in `StrataScreen`:

| What closes the screen | Asks `closeRequested` |
|---|---|
| Escape that Strata does not use, while `shouldCloseOnEsc()` is true | Yes |
| `handle.requestClose()`, such as a Cancel button | Yes |
| Any other call to vanilla's `onClose()` | Yes |
| The inventory key in a `StrataContainerScreen` | Yes |
| `handle.close()` | No. It calls `onClose()` at once. |
| A close the game makes for its own reasons, such as a disconnect, another screen replacing this one, or `removed()` | No |

When the screen refuses, nothing closes and the screen stays as it was, so a screen with unsaved changes can show a message box and call `handle.close()` from its Discard button. A subclass that overrides `onClose()` still asks on Escape. When other code calls its `onClose()`, the asking happens where the override calls `super.onClose()`.

## Titles

The title vanilla shows and narrates, `getTitle()`, is the Strata screen's: its `title()` when it opened, then whatever it sets with `handle.setTitle`. It is read again when the player changes language. A `StrataScreen` constructed with a vanilla title keeps that title until the Strata screen sets another. A `StrataContainerScreen` narrates the menu's title from the server the same way. Minecraft's screens fill the window, so the host's `preferredSize` has no use here.

Minecraft keeps its own screens, so each `StrataScreen` holds exactly one Strata screen. `hidesBackground()` has no effect, and vanilla decides what is drawn behind a screen. Dialogs and other [overlays](../../guides/overlays.md) live inside the screen's host and need no vanilla screen of their own, so the content behind a dialog keeps drawing.

`StrataScreen` is public so that mods can subclass it, for example to keep the world running ([Pausing](#pausing)) or to return to a vanilla screen ([Returning to a vanilla screen](#returning-to-a-vanilla-screen)). It has two public constructors, both taking the vanilla title and the Strata screen:

| Constructor | Theme |
|---|---|
| `StrataScreen(Component title, Screen content, ThemeRef theme)` | Resolved when the screen opens and again after every resource reload, keeping the ref's overrides |
| `StrataScreen(Component title, Screen content, Theme theme)` | Kept as given; resource packs never change it |

Here `Component` is vanilla's `net.minecraft.network.chat.Component` and `Screen` is Strata's `org.inspirenxe.strata.api.screen.Screen`. `content()` returns the Strata screen. A screen made this way is a vanilla screen like any other, opened on the client thread:

```java
Minecraft.getInstance().gui.setScreen(
        new StrataScreen(Component.literal("Settings"), new SettingsScreen(), ThemeRef.platform()));
```

Because a `StrataScreen` is an ordinary vanilla screen, loader events about screens, such as NeoForge's `ScreenEvent`s, fire for it as for any other.

Container screens use `StrataContainerScreen` instead, which is covered in [Container screens](containers.md).

## Screens above screens

A screen opens another above itself with its handle, as the [Screens](../../guides/screens.md#screens-above-screens) guide shows:

```java
factory.button("Details")
        .onEvent(Button.PRESSED, event -> this.handle.open(new DetailsScreen()));
```

On Minecraft the new screen gets a `StrataScreen` of its own. The one beneath keeps its host while it waits, so its tree, focus, scroll positions and text stay as the player left them, and its `closed()` does not run. When the screen above closes, through Escape or `handle.close()`, its `onClose()` sets vanilla's screen back to the one beneath, which only resizes its host.

Some cases close more than one screen:

- If vanilla replaces the screen above with anything other than the one beneath, such as on a disconnect or when another mod opens a vanilla screen, the screens waiting beneath close too, since nothing will return to them.
- `handle.close()` on a screen that waits beneath others closes it and every screen above it, and shows the screen it was opened above, or returns to the game.
- A handle whose screen waits beneath another opens the new screen on top of the screen showing, as `Strata.screens()` does.

Container screens cannot wait beneath another screen, because vanilla tells the server the menu closed when its screen is removed. A `StrataContainerScreen`'s handle therefore opens screens as `Strata.screens()` does, and the new screen replaces the container screen.

## Returning to a vanilla screen

Vanilla's `onClose()` leaves the screen and returns to the game, or to the title screen when no world is loaded. A `StrataScreen` opened above another `StrataScreen` returns to it, but one opened over a vanilla screen does not. To go back to a vanilla screen, such as the options screen that opened yours, subclass `StrataScreen` and override `onClose()`:

```java
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.inspirenxe.strata.api.theme.ThemeRef;
import org.inspirenxe.strata.minecraft.StrataScreen;

public class ChildScreen extends StrataScreen {
    private final Screen parent;

    public ChildScreen(final Component title, final org.inspirenxe.strata.api.screen.Screen content,
                       final Screen parent) {
        super(title, content, ThemeRef.platform());
        this.parent = parent;
    }

    @Override
    public void onClose() {
        // Escape and handle.close() both end up here
        this.minecraft.gui.setScreen(this.parent);
    }
}
```

The vanilla screen opens it with itself as the parent:

```java
this.minecraft.gui.setScreen(new ChildScreen(Component.literal("Settings"), new SettingsScreen(), this));
```

Screens opened above a `ChildScreen` still return to it, since they use `StrataScreen`'s own `onClose()`.

## Pausing

Whether the game pauses behind a screen follows vanilla's `isPauseScreen()`, which is true by default. Vanilla pauses only a single-player world that is not open to LAN. Override it to keep the world running:

```java
public class LiveScreen extends StrataScreen {

    public LiveScreen(final Component title, final org.inspirenxe.strata.api.screen.Screen content) {
        super(title, content, ThemeRef.platform());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
```

A screen opened through `Strata.screens()` is a plain `StrataScreen` and always pauses. Open a subclass such as `LiveScreen` with `setScreen`, as in [The lifecycle](#the-lifecycle).

## Threading

The adapter runs on Minecraft's client thread, the render thread that runs vanilla's screens, and that is Strata's [UI thread](../../guides/threading.md). Build and change Strata components only there. From the server thread, a network thread or your own threads, hand the work over with `Strata.uiThread().execute(...)`, with `factory.uiThread()` where you hold a factory, or by setting a `State` the screen is bound to, which any thread may do:

```java
// In a network handler: the rows reach the screen on the client thread
Strata.uiThread().execute(() -> listings.setItems(rows));
```

Tasks go on the game's own task queue, which the client runs between frames, in the order they were handed over. A task handed over on the client thread also queues, and runs after the current call returns instead of inside it. A change to a shown component from another thread throws an `IllegalStateException` that names the thread.

`Strata.screens().open` may be called from any thread and moves to the client thread itself, as the [Screens](../../guides/screens.md) guide describes. The handle it returns works from the moment the screen shows. Vanilla's `setScreen`, which opens a `StrataScreen` subclass, must run on the client thread.

Screens' [repeating tasks](../../guides/screens.md#repeating-tasks) run on the client thread. Where Sponge is present they run on Sponge's client scheduler, as tasks of the `strata` plugin, so they show in Sponge's task list. Without Sponge, on every loader, the Strata screen the game shows runs them from its client tick, twenty times a second, so an interval rounds up to whole ticks of 50 ms and tasks wait while a vanilla screen covers the Strata one. `StrataMinecraft.scheduler()` returns the one in use.

The game draws every frame, so the adapter renders Strata screens every frame and ignores Strata's frame scheduling (`needsFrame`, `nextFrameNanos`, the frame requester and the frame rate), which adapters that draw on demand use. Motion still reads the clock, so it moves every frame the game draws.
