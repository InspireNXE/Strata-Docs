# Status and limitations

Strata is at version 0.1.0 and has not been released. This page lists what is tested, what has only been tested indirectly, known issues, and what is planned.

## What is tested

| Area | How |
|---|---|
| Layout, sibling order, input, focus, events, snapshots, binding, converters and validation, commands and shortcuts, text editing and undo, drag and drop, animation, frame scheduling, right to left layout and text, plurals, extension, the controls, scrolling, lists, overlays, tooltips, screens and `Strata.screens()`, and theme resolution | Unit tests in `core` against a test platform with a manual clock |
| The SPI contracts | Contract tests run on the test platform, on the Swing adapter, and on the example adapter in these docs |
| The Swing adapter | Every contract, sprite, tint, nine-slice, shape and pixel drawing on generated images, key and button mapping, a panel driven with synthetic AWT events, clipboard round trips, end-to-end snapshots |
| The test kit | Core's own tests drive screens through it, as [Testing your screens](../guides/testing.md) describes |
| These docs | Every Java example compiles, and the example adapter and snapshot timing are tested |

## What is not tested

Snapshots are published to Maven Central's snapshot repository, and Strata-Swing and Strata-Minecraft build against them in their own workflows. No release has gone to Maven Central yet. Adapters in other repositories list their own status.

## Known issues

- A key held while focus moves keeps repeating into the newly focused component. Holding Backspace while clicking into another text box deletes text there.
- Control events fired inside an overlay do not reach the screen behind it. A `Menu.CHOSEN` in an open menu, or the events of a dialog's controls, never bubble to a handler on the screen's content. Register those handlers on the overlay's content or its controls. See [How overlays are layered](../guides/overlays.md#how-overlays-are-layered).
- Text fields do not run the bidirectional algorithm. A text box or text area draws its text, caret and selection in logical order, and right to left text wider than the box still starts at the left and scrolls sideways. Labels, captions and menus order their lines properly. See [Internationalization](../guides/internationalization.md).
- Some controls are not mirrored for right to left languages yet: the calendar and the date and time pickers, the table, the color picker, the icon and text an info bar draws itself, a window's resize grip, and the text area's horizontal bar.
- An engine that draws only rectangles draws lines, shapes and shadows from rectangles one device pixel high, so they look stepped, and its round clips are square. See [Platform differences](../platforms/differences.md).
- A mnemonic can name only a key from A to Z or a digit. A letter outside that set, such as "Ö", cannot be typed as one.

## Not built yet

- Narration and other accessibility support, and system preferences such as reduced motion.
- Controller navigation. An adapter can send a controller's buttons as keys, but nothing moves focus with a pad.
- Drawing: radial gradients, dashed strokes, a miter limit and anti-aliasing for engines that draw only rectangles.
- Image cursors, pinch zoom, and drags from a window out to other applications.
- Closable and movable tabs, a delay before submenus open, placement rules for popups, range selection in the calendar and range sliders.
- A text area that drags its selection, and drags for trees, tables and grids.
- A render order contract for engines that sort draws by kind. Swing draws in the order Strata asks, so overlapping siblings draw correctly there.
- A public Maven repository.

## Open design questions

These are recorded in the [pending design](../design-pending.md#open-questions):

- Style classes for themes, and whether a host's theme can be set per subtree. Style states and text style patches are built.
- A tooltip kind that holds lines and a visual together.
- Coalescing of continuous input. Causes and data binding are built.
- Popup events that continue at the component that opened the popup, which would fix the known issue with overlay events above.

## Compatibility promise

None yet. Until 1.0, any release may change the API. Once there are releases, the [changelog](https://github.com/InspireNXE/Strata/releases) will list breaking changes.
