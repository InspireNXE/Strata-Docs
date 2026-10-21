# Status and limitations

Strata is at version 0.1.0 and has not been released. This page lists what is tested, what has only been tested indirectly, known issues, and what is planned.

## What is tested

| Area | How |
|---|---|
| Layout, sibling order, input, focus, events, snapshots, binding, extension, the controls, scrolling, lists, overlays, tooltips, screens and `Strata.screens()`, and theme resolution | Unit tests in `core` against a test platform with a manual clock |
| The SPI contracts | Contract tests run on the test platform, on the Swing adapter, and on the example adapter in these docs |
| The Swing adapter | Every contract, sprite, tint and nine-slice drawing on generated images, key and button mapping, a panel driven with synthetic AWT events, end-to-end snapshots |
| These docs | Every Java example compiles, and the example adapter and snapshot timing are tested |

## What is not tested

Snapshots are published to Maven Central's snapshot repository, and Strata-Swing and Strata-Minecraft build against them in their own workflows. No release has gone to Maven Central yet. Adapters in other repositories list their own status.

## Known issues

- On macOS, the text box's shortcuts use Control, not Command, so they do not match the rest of the system.
- A key held while focus moves keeps repeating into the newly focused component. Holding Backspace while clicking into another text box deletes text there.
- Control events fired inside an overlay do not reach the screen behind it. A `Menu.CHOSEN` in an open menu, or the events of a dialog's controls, never bubble to a handler on the screen's content. Register those handlers on the overlay's content or its controls. See [How overlays are layered](../guides/overlays.md#how-overlays-are-layered).

## Not built yet

- Controls: editable table cells.
- Narration and other accessibility support.
- Controller navigation.
- Drawing beyond fills, gradients, sprites, nine-slices, engine visuals and text.
- A render order contract for engines that sort draws by kind. Swing draws in the order Strata asks, so overlapping siblings draw correctly there.
- A public Maven repository.

## Open design questions

These are recorded in the [pending design](../design-pending.md#open-questions):

- A platform shortcut modifier: Command on macOS, Control elsewhere.
- Style classes for themes, and whether a host's theme can be set per subtree.
- A tooltip kind that holds lines and a visual together.
- Causes and input coalescing, proposed and awaiting review.
- Popup events that continue at the component that opened the popup, which would fix the known issue with overlay events above.

## Compatibility promise

None yet. Until 1.0, any release may change the API. Once there are releases, the [changelog](https://github.com/InspireNXE/Strata/releases) will list breaking changes.
