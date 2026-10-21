# Platforms

Strata runs wherever an adapter exists. The Swing adapter lives in the Strata-Swing repository:

| Adapter | Artifact | Show a screen with | One UI unit is |
|---|---|---|---|
| [Swing](swing.md) | `strata-canvas-swing` | `Strata.screens()` on a `StrataPanel`, a `JPanel`, marked with `setDefault()` | `scale` pixels |

The Minecraft adapter lives in the [Strata-Minecraft](https://github.com/InspireNXE/Strata-Minecraft) repository.

Screens written against the API run on every adapter unchanged, and `Strata.screens().open(screen)` opens them on every adapter. A few behaviors differ between engines, mostly in fonts, modifier keys and nine-slices; [Platform differences](differences.md) lists what an adapter may decide for itself.

To run Strata on another engine or toolkit, such as JavaFX, see [Writing an adapter](writing-an-adapter.md).
