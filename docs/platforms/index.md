# Platforms

Strata runs wherever an adapter exists. Each adapter lives in a repository of its own:

| | Adapter | Artifact | Show a screen with | One UI unit is |
|---|---|---|---|---|
| ![Swing](../assets/brand/platform-swing.svg){ .strata-icon } | [Swing](swing.md), in [Strata-Swing](https://github.com/InspireNXE/Strata-Swing) | `strata-swing` | `Strata.screens()` on a `StrataPanel`, a `JPanel`, marked with `setDefault()` | `scale` pixels |
| ![Minecraft](../assets/brand/platform-minecraft.svg){ .strata-icon } | [Minecraft](minecraft/index.md), in [Strata-Minecraft](https://github.com/InspireNXE/Strata-Minecraft) | `strata-minecraft-neoforge`, `-forge`, `-fabric` or `-spongevanilla` | `Strata.screens()`, shown in a vanilla `Screen` | One GUI pixel, screen pixels divided by the GUI scale |
| ![Your engine](../assets/brand/platform-engine.svg){ .strata-icon } | [Your engine](writing-an-adapter.md) | | | |

Screens written against the API run on every adapter unchanged, and `Strata.screens().open(screen)` opens them on every adapter. A few behaviors differ between engines, mostly in fonts, modifier keys, shapes, input devices and nine-slices; [Platform differences](differences.md) lists what an adapter may decide for itself.

To run Strata on another engine or toolkit, such as JavaFX, see [Writing an adapter](writing-an-adapter.md).
