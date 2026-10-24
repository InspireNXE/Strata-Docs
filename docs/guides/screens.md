# Screens

A `Screen` is one full screen of UI: a settings page, a dialog, an inventory. You implement it; a host calls it.

## Lifecycle

1. The host calls `build(factory)` for the component tree and shows it, sized to the host.
2. The host calls `opened(handle)` with a `ScreenHandle` the screen may keep.
3. After `handle.invalidate()`, the host calls `build` again before the next frame and replaces the whole tree. Focus and hover start over.
4. When the screen closes, the host calls `closed()` and then detaches the tree. A close that the user asks for goes through `closeRequested` first; see [Closing](#closing).

A screen object may be opened again after it closes. Each opening builds a new tree and gets a new handle.

`build` runs before `opened`, so the first build has no handle yet. Keep the handle in a field and use it from handlers, which run later, as the examples below do. A rebuild calls `build` again but not `opened`: the handle stays the same for the whole opening. If `build` throws, or the theme lacks keys the tree needs, the screen does not open and `opened` is never called.

`Screen` is a functional interface, so a lambda works for simple screens:

```java
Strata.screens().open(factory -> factory.anchorPanel()
        .add(factory.label("Loading").setAnchor(Anchor.CENTER))
        .setAnchor(Anchor.FILL));
```

A screen can give itself a title by overriding `title()`; see [Title](#title).

## Opening a screen

`Strata.screens()` returns the platform's `ScreenController`, which opens screens wherever the engine adapter shows them. Code that opens a screen names nothing from the adapter:

```java
--8<-- "screens/ScreenExamples.java:open"
```

| Method | Does |
|---|---|
| `open(screen)` | Opens the screen in the platform's default theme, the one `ThemeRef.platform()` names |
| `open(screen, theme)` | Opens it in a theme of your choice |

Both return the screen's `ScreenHandle`, and both may be called from any thread; see [Opening from another thread](#opening-from-another-thread). The adapter hands its controller to Strata when it starts. Until then, `Strata.screens()` throws `IllegalStateException` with a message that says how to set one. On Swing, which has no screens of its own, the controller is the stack of the panel marked with `StrataPanel.setDefault()`; see [Swing](../platforms/swing.md#showing-screens). Each adapter's documentation says where screens open.

### Opening from another thread

Called on the UI thread, `open` builds and shows the screen at once. Called from any other thread, such as a network handler, it hands the opening to the UI thread and returns the handle at once. That handle works before the screen exists:

- `invalidate`, `close`, `setTitle`, `every` and `open` on it are handed over too, in the order they were called, and reach the screen once it has opened. A task from `every` starts when the screen opens, and closing its subscription before then keeps it from starting.
- `close()` or `requestClose()` before the screen has opened cancels the opening, so the screen never builds, and `requestClose()` returns true.
- After the screen has opened, `requestClose()` from another thread hands the request over and returns false, since the answer is not known yet.
- `isOpen()` is true while the opening waits.
- `factory()` throws `IllegalStateException` until the screen has opened, since the host it belongs to does not exist before then.

If the opening fails on the UI thread, such as for a theme that lacks a key, the error is logged there and the handle counts as closed. The screen stack and the engine adapters' controllers all behave this way. See [The UI thread](threading.md).

## Rules for `build`

- Return a new component with no parent, made with the factory passed in. A component from another factory, or one already shown elsewhere, fails with `IllegalArgumentException`.
- The host's theme must define every required style key of the tree's components, or opening fails with `ThemeException`. See [Themes](theming.md#required-and-optional-keys).
- Make the root fill the host with `Anchor.FILL` if it should cover the whole area. Otherwise it takes its desired size at the top left.
- Everything runs on the UI thread. Code on another thread reaches the screen through `Strata.uiThread()` or a `State`; see [The UI thread](threading.md).

## Keep data on the screen, not in the tree

Because `build` can run more than once, treat the tree as disposable and the screen object as the owner of your data. Small changes, such as new label text or a disabled button, only need setters. Changes to the structure, such as adding or removing rows, are easiest with a rebuild:

![A list whose rows are rebuilt](../assets/images/screens-list.png)

```java
--8<-- "screens/ScreenExamples.java:rebuild"
```

Rebuilding is cheap for screens of ordinary size, but it resets focus, hover, text box carets and anything else that lives only in components. For a list the user is typing into, change the tree in place instead: `remove` and `add` on the container work at any time.

## The handle

| Method | Does |
|---|---|
| `invalidate()` | Asks for a rebuild before the next frame |
| `close()` | Closes the screen. It never asks the screen first. |
| `requestClose()` | Asks the screen's `closeRequested`, and closes the screen if it agrees. Returns whether it closed. |
| `isOpen()` | True while this handle is the host's current opening |
| `setTitle(title)` | Replaces the screen's title and returns the handle; see [Title](#title) |
| `every(millis, task)` | Runs a task every interval while the screen is open; see [Repeating tasks](#repeating-tasks) |
| `factory()` | The host's factory, the same one `build` receives |
| `open(screen)`, `open(screen, theme)` | Opens another screen above this one. Closing it shows this one again. |

A handle belongs to one opening. Once that opening ends, because the screen closed or the host opened another, its methods do nothing, so a stale handle is harmless. `open` is the exception: on a stale handle it throws `IllegalStateException`, since the screen it would return to has gone.

```java
--8<-- "events/EventExamples.java:handle"
```

What closing means depends on the host. A Swing screen stack pops the screen. An adapter that holds each Strata screen in an engine screen usually closes that engine screen, and the engine decides what shows next.

## Repeating tasks

A screen that polls, to refresh figures, check on a download or enable a button after a delay, asks its handle for a repeating task:

```java
--8<-- "screens/ScreenExamples.java:every"
```

The platform runs the task on the UI thread, so the task may change components. The first run comes about one interval after the call. The platform decides the timing: Swing uses a timer on the event thread, Vulkan runs due tasks as the view renders unless the application gives its own scheduler, and Minecraft uses Sponge's scheduler or the client tick, so an interval there rounds up to whole ticks of 50 ms. A busy or paused engine may drop runs it missed rather than run them one after another.

The task stops when its `Subscription` closes or the screen closes, whichever comes first, and never runs after that. Closing the subscription twice, or after the screen closed, does nothing. A screen covered by another one it opened is still open, so its tasks keep running. A handle whose opening has ended starts nothing and returns a subscription that is already closed.

For motion, use the [animator](animation.md) instead. An animation steps once for every frame drawn, while a task runs on the engine's own beat, which on Minecraft is slower than the frames.

## Closing

A screen can refuse to close, such as an editor with unsaved changes. Closing comes in two kinds:

- A close the user asks for: the window's close button, the engine's key for leaving a screen, or a Cancel button in the screen. These call `ScreenHandle.requestClose()` (or the host's `requestClose()` for the engine's own controls), which asks `Screen.closeRequested(handle)`. The default returns true.
- A close the program decides: `handle.close()`, a stack's `pop()` or `clear()`, and the engine closing the screen for its own reasons. These never ask.

`requestClose()` returns true when the screen agreed and the opening is still current, and false when the screen refused or the opening has ended. A Cancel button calls it, so it behaves like the window's close button.

The usual way to refuse is to return false and ask the user, then call `close()` if the answer is yes. `close()` skips `closeRequested`, so the second close does not ask again:

```java
--8<-- "screens/ClosingExamples.java:editor"
```

When the engine closes a screen without being asked, such as a disconnect or another screen replacing this one, `closed()` runs and `closeRequested` does not. A stack asks its screens from the top down, removes each that agrees, and stops at the first that refuses; it reports whether it ended up empty. A screen that closes itself through its handle while it is being asked counts as agreeing.

Escape does not close a screen by itself. The host closes the top overlay that nothing consumed Escape for, and presses a cancel button where there is one. An adapter whose engine closes a screen on Escape calls `ScreenHost.requestClose()`; see [Writing an adapter](../platforms/writing-an-adapter.md).

## Title

`Screen.title()` returns empty text by default. The host reads it when the screen opens, and engines that show or read out a screen's title, such as a window's title bar or a screen reader, use it. `ScreenHandle.setTitle(Text)` replaces it while the screen is open, as the editor above does when the text changes, and returns the handle. The host's `title()` returns the current title, and empty text when no screen is open. A title can be translatable text, and the adapter resolves it again when the language changes.

### Screens above screens

A screen opens another above itself with its handle, such as a details page from a list:

```java
--8<-- "screens/ScreenExamples.java:above"
```

The screen beneath keeps its tree while it waits, so its focus, scroll positions and text are as the user left them when the screen above closes. In a stack, the new screen goes straight above the screen whose handle opened it, even when that screen is not on top. Without a theme, the new screen opens in the platform's default theme, wherever it was opened from.

## Stacks of screens

Engines without a screen system of their own, such as Swing, use a `ScreenStack`. `StrataPanel.screens()` returns one.

| Method | Does |
|---|---|
| `push(screen)` | Opens a screen on top, at the stack's size and theme |
| `open(screen)`, `open(screen, theme)` | Opens a screen on top in a theme of its own: the platform's default, or the one given. The stack is a `ScreenController`, so this is the same `open` that `Strata.screens()` offers. |
| `pop()` | Closes and removes the top screen. It does not ask. |
| `requestClose()` | Asks the screens from the top down, as [Closing](#closing) says. For a window's close button. |
| `title()` | The top screen's title |
| `preferredSize(w, h)` | The size the top screen wants in the space offered, for a toolkit that sizes a widget from its content |
| `top()`, `isEmpty()` | Inspect the stack |
| `clear()` | Closes every screen, top first |
| `setTheme(theme)` | Applies to every screen, and to screens pushed later, except screens opened with a theme of their own |
| `resize(w, h)` | Applies to every screen, and to screens pushed later |

Only the top screen receives input. A screen that closes through its handle is removed from wherever it is in the stack. Each screen in a stack has its own host inside, with its own factory, focus and hover, so components cannot move between screens.

```java
--8<-- "screens/ScreenExamples.java:stack"
```

### Drawing the screens below

A stack draws from the topmost screen that hides the background, or from the bottom screen if none does, up to the top. By default `hidesBackground()` is false and screens below show through, which suits dialogs. A screen that covers everything should return true, so the stack skips drawing what is beneath it:

```java
--8<-- "screens/ScreenExamples.java:opaque"
```

Engines with a screen stack of their own may hold one Strata screen per engine screen. There `hidesBackground` has no effect, and the engine decides what is drawn behind a screen.

## Changing the theme

Screens cannot change their host's theme. Adapters can: `ScreenStack.setTheme` and `ScreenHost.setTheme` swap it, components re-read their keys, and a new layout runs. A new theme that lacks keys the open tree needs throws `ThemeException`, and the old theme stays.

A screen opened with a `ThemeRef`, which `Strata.screens().open` always uses, follows that reference while it is open. When the engine reloads its theme files, or code adds a theme or an override through `Strata.themes()`, the host resolves the reference again before the next frame, and the components re-read their keys. If the reference no longer resolves, or the new theme lacks keys the tree needs, the host logs a warning and keeps the theme it had. See [Themes](theming.md).

When the language changes, the host lays every component out again before the next frame, so translated text takes its new width.
