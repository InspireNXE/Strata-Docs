# Minecraft help

This page covers what is specific to Minecraft. Questions about Strata itself are in the [FAQ](../../help/faq.md), [Troubleshooting](../../help/troubleshooting.md) and [Status and limitations](../../help/status.md).

## Common questions

### Which Minecraft versions are supported?

26.3 on the `main` branch, whose mod metadata accepts Minecraft `[26.3, 26.4)`. The `mc/1.21.1` and `mc/1.20.1` branches hold the adapter for those versions. Each branch is one Minecraft version, and only `main` is published so far. [Versions and loaders](index.md#versions-and-loaders) lists the loaders for each.

### Do players have to install Strata?

Yes. Strata is a mod of its own with the id `strata`, and players install the jar for their loader next to your mod. Declare it as a required dependency in your mod's metadata, as [Setup](index.md#setup) shows, so the game refuses to start without it.

### Does Strata need Fabric API?

No. The Fabric jar depends only on Fabric Loader, Minecraft and Java. A Fabric mod that registers a [container screen](containers.md) for its menu type needs Fabric API itself, because vanilla's `MenuScreens.register` is private and Fabric API opens it to mods.

### Does Strata use mixins?

No. It shows screens through vanilla's own `Screen` class and takes input from its callbacks.

### Does it work on a dedicated server?

Strata runs on the client only. Keep classes that touch it out of server code, as you would for vanilla screen classes. The SpongeVanilla plugin may sit on a server, where it does nothing.

### Can I try my screens without starting the game?

Yes. A screen written against Strata's API runs unchanged on the [Swing adapter](../swing.md), which opens in a window or renders to images without a display. Fonts, rounding and a few input details differ in game; [Input](input.md) and [Drawing](drawing.md) list them.

### Can I draw on the HUD?

Not yet. HUD layers, through NeoForge's `RegisterGuiLayersEvent` and Fabric's `HudElementRegistry`, are planned.

### Can I put vanilla buttons in a Strata screen, or Strata components in a vanilla screen?

Not yet. Both directions are planned as hosting adapters, without mixins. See [Not available yet](#not-available-yet).

## Troubleshooting

| Symptom | Cause and fix |
|---|---|
| The log says "Strata's core could not be found" at startup | Strata's `api` classes cannot see `core` through `ServiceLoader`. Usually a mod or plugin copied Strata's classes into its own jar. Take `strata-api` as `compileOnly` and let the Strata jar bring Strata. |
| The log says "Strata's core answered with another screen controller" | A second copy of `strata-core` is loaded, usually bundled in another mod. Keep exactly one, the one in the Strata jar. |
| `IllegalStateException: No screen controller is set` | `Strata.screens()` ran before Strata's entry point did, such as in a mod constructed before Strata. Declare the dependency with `ordering = "AFTER"` on NeoForge and Forge or `"load-order": "after"` on SpongeVanilla, or open screens later, from a key mapping, a command or an event. |
| `IllegalStateException: Screens cannot open before the game has started` | A screen was opened before the game client existed. Open screens from something that happens once the game runs. |
| Escape closes the screen while typing | Built-in controls pass Escape on to vanilla, which closes the screen. Strata keeps Escape only when it closes an open popup, menu, dialog or closable window, or a text box's suggestion list. To keep the screen open, consume Escape in a handler, or subclass `StrataScreen` and override vanilla's `shouldCloseOnEsc()`. |
| Typing does nothing in a custom component | Minecraft sends typed characters only while a text field has focus. Override `acceptsTextInput()` to return true, and the adapter turns text input on while the component has focus. See [Input](input.md). |
| Modifier checks never pass in game | Minecraft sends no modifiers with pointer moves, scrolls or typed characters, so those events report `Modifiers.NONE`. Remember the modifiers from the press that started a drag. |
| Closing goes back to the game instead of the previous screen | The previous screen was a vanilla screen. A `StrataScreen` returns to the Strata screen it was opened above, or else to the game. Subclass `StrataScreen` and override `onClose()` to return to a vanilla screen. See [Screens](screens.md). |
| Vanilla widgets in a `StrataScreen` subclass never get clicks | Strata takes every press that hits one of its components, and a root anchored with `FILL` covers the screen. Mixing them is not supported yet. |
| Text is cut off in game but fine in Swing | Minecraft's font has other widths and measures in whole GUI pixels. Leave slack around text in fixed widths, or let components measure. |
| Things are a pixel off in game | The canvas moves each edge of a fill down to a whole GUI pixel. Use whole numbers for positions and sizes. |
| A purple and black checkerboard | The sprite is missing. Put it at `assets/<modid>/textures/gui/sprites/<path>.png` and name it `SpriteId.of("<modid>", "<path>")`, then reload resources (F3+T) in a running game. |
| A nine-slice stretches in game | Minecraft takes the slicing from the sprite's `.png.mcmeta`, and the sprite has none with `nine_slice` scaling. See [Drawing](drawing.md). |
| `IllegalStateException` from a `StrataContainerScreen` at its first layout | The content does not add its `MenuPanel` to the tree, or adds it inside a `ScrollPanel`, where slots cannot follow it. See [Container screens](containers.md). |
| Gradle cannot use `strata-minecraft-common` or `strata-minecraft-sponge` | They are compiled into each loader jar and are not mods. Depend on `strata-minecraft-neoforge`, `-forge`, `-fabric` or `-spongevanilla`. |

## Status

The adapter compiles against the real 26.3 classes, and its unit tests run against them:

| Area | How it is tested |
|---|---|
| Input mapping | Every key constant in the real `InputConstants` maps to a distinct Strata key; mouse buttons, modifiers and scroll direction and kind |
| The canvas | Against a mocked `GuiGraphicsExtractor`: fills, gradients, clips, translations, layers, sprites, nine-slices, text and cursors, and item visuals made from real item stacks |
| Player faces | Against a mocked `GuiGraphicsExtractor`, from skin textures and profile lookups |
| Formatted text and item tooltips | Styles and `§` codes against real chat components |
| Translations | Language codes turned into Java locales, with fallbacks for languages the JDK lacks |
| `ItemDisplay` | In a real Strata host over a mocked platform, with real item stacks |
| `MenuPanel` | Drawing in both slot modes, drag previews, the theme's slot keys, and which pointer input goes to vanilla, in a real Strata host with a real menu |
| Client settings | Parsing, broken files and writing the defaults |
| Theme files | Pack order, mods' files above the player's packs, reloads and registration, over a mocked resource manager |
| The vanilla theme | Every key the flat theme sets comes from the vanilla theme's own file, and every required key is set |
| Opening screens | Screens above screens, closing several at once, the controller and the default theme, with the game mocked and vanilla's screen switching replaced by a stand-in |
| Sponge causes | Strata's frames opening and closing Sponge's, over a stand-in for Sponge's cause stack manager |
| Loader packaging | The processed mod and plugin metadata and the entry points on each loader, and that the NeoForge and Forge mods start without SpongeAPI on the class path |

Strata screens have run in a Fabric 26.3 client only through a separate test mod, outside the repository, which opened screens of controls next to vanilla's widgets and compared them pixel by pixel. The vanilla theme's values come from that comparison. Everything else that needs a running game is covered by compiling against the real classes and by the unit tests above: drawing through vanilla's renderer, item rendering, text input focus, `StrataContainerScreen` with its input routing and its compatibility with recipe viewers and slot-drawing mods, and loading on NeoForge and Forge. No Strata screen has been shown on NeoForge or Forge yet, and none of SpongeVanilla, SpongeNeo and SpongeForge has been run with Strata. Depending on the published jars from a separate mod or plugin project has not been tried.

## Not available yet

- HUD layers, through NeoForge's `RegisterGuiLayersEvent` and Fabric's `HudElementRegistry`, are planned.
- Vanilla widgets inside Strata screens, and Strata components inside vanilla screens. Both are planned as hosting adapters, `MinecraftUi.wrap` and a `StrataElement`, without mixins.
- Narration of Strata components. Vanilla narrates the screen's title only.
- Controller and gamepad navigation.
- Tooltip images, such as a bundle's contents, in item tooltips built with `MinecraftTooltips`.
- Slots that scroll or move with Strata's layout after the menu's constructor placed them. A `MenuPanel` cannot sit inside a `ScrollPanel`.
- Client-only slots, such as filter or ghost slots, outside a menu.
- Changing a `StrataScreen`'s theme after it is made. It takes its theme in the constructor and has no setter, though a `ThemeRef` given there follows resource reloads.
- A platform shortcut modifier. On macOS, Strata's text box uses Control for its edit shortcuts where vanilla uses Command.
