# Container screens on Minecraft

`StrataContainerScreen`, in `org.inspirenxe.strata.minecraft`, shows a menu's slots inside a Strata tree, for machines, storage and anything else a server opens with `player.openMenu`. The mod writes its `AbstractContainerMenu` exactly as it would for a vanilla screen, and registers the Strata screen for the menu's type the usual way. For screens without slots, use `Strata.screens()` as on any platform; see [Screens](screens.md).

## Writing the screen

The screen's content is a `ContainerContent`, a function that gets the screen's `ComponentFactory`, the menu and a `MenuPanel`, the area that holds the menu's slots, and returns the root of the tree:

```java
/**
 * A crate's screen: a three-row chest menu with its title above the slots.
 */
static StrataContainerScreen<ChestMenu> crateScreen(final ChestMenu menu, final Inventory inventory,
                                                    final Component title) {
    // 176 by 168 is vanilla's size for three rows, which the menu's slot positions assume
    return new StrataContainerScreen<>(menu, inventory, title, 176, 168,
            (ui, crate, slots) -> ui.anchorPanel()
                    .add(ui.stackPanel().setSpacing(4)
                            .add(ui.label(MinecraftText.of(title)))
                            .add(slots)
                            .setAnchor(Anchor.CENTER))
                    .setAnchor(Anchor.FILL),
            ThemeRef.platform(), SlotRendering.STRATA);
}
```

| Constructor | Image size | Theme | Slots drawn by |
|---|---|---|---|
| `(menu, inventory, title, content, themeRef)` | 176 by 166, vanilla's default | Resolved from the ref, following resource packs | Vanilla |
| `(menu, inventory, title, imageWidth, imageHeight, content, themeRef, slotRendering)` | As given | Resolved from the ref, following resource packs | As given |
| `(menu, inventory, title, imageWidth, imageHeight, content, theme, slotRendering)` | As given | The fixed `Theme`, which resource packs never change | As given |

The menu, inventory and title are the ones vanilla passes to the screen constructor. The title is used for narration; vanilla's title and inventory labels are not drawn, so put your own labels in the tree. The image size is the size of the slot area that the menu's slot positions are relative to, the same `imageWidth` and `imageHeight` a vanilla screen for the menu would have. `ThemeRef.platform()` is the vanilla theme; see [Drawing](drawing.md).

The content is built once, on the render thread, when the screen first opens. Add the `MenuPanel` to the tree exactly once, anywhere except inside a `ScrollPanel`, since vanilla places slots in screen coordinates. A tree that leaves the panel out, or holds it inside a `ScrollPanel`, throws `IllegalStateException` at the first layout. The panel cannot be made any other way.

## Registering the screen

Register a screen constructor for the menu type as for any container screen. A method reference to a method like `crateScreen` above fits vanilla's `MenuScreens.ScreenConstructor`:

```java
// NeoForge, in RegisterMenuScreensEvent; CRATE is the MenuType<ChestMenu> the mod registered
event.register(ExampleMenus.CRATE, CrateScreens::crateScreen);

// Fabric, in the client entry point
MenuScreens.register(ExampleMenus.CRATE, CrateScreens::crateScreen);
```

`MenuScreens.register` is private in vanilla. Forge's access transformer makes it public, so a Forge mod calls it the way the Fabric example does, during client setup. On Fabric, Fabric API's transitive access wideners make it accessible; a mod that does without Fabric API widens it in its own access widener.

## How other code sees the screen

`StrataContainerScreen` extends vanilla's `AbstractContainerScreen` over the mod's own menu. The server sees an ordinary menu, so the screen works on any server, including plugin servers and servers without Strata. Mods that work with any container screen, such as recipe viewers and inventory sorters, find the usual fields where they expect them:

- The panel's desired size is the image size. After every layout the screen moves vanilla's `leftPos` and `topPos` to the panel's top-left corner, so each slot sits where the menu's constructor put it, relative to the panel.
- Clicks, drag-splitting, shift-clicks, double-clicks, scrolling and the hotbar, drop, pick-block and inventory keys run vanilla's own container code, with the player's key bindings.
- The carried stack is drawn after everything Strata draws, so it floats over dialogs and popups too.
- The world behind is dimmed as for any container screen, and the game does not pause.

While the panel is hidden or removed from the tree, no input goes to vanilla and no slot is hovered.

## Input

Strata sees every press first, so popups close and focus moves as on any Strata screen. The press then also goes to vanilla's container handling when all of these hold:

- the press did not close a popup;
- no modal dialog is open;
- the pointer is over the panel or something inside it, over the root of the tree, or over nothing.

A release or drag goes to vanilla when the press that started it did. A scroll over the panel goes to vanilla first, and to Strata when vanilla does not use it.

Keys go to Strata first. A key Strata does not use goes to vanilla unless a modal dialog is open. When the pointer is over Strata content that covers a slot, vanilla gets the key with no hovered slot, so slot keys such as the hotbar keys do not act on the covered slot, while keys such as the inventory key still close the screen.

A slot's tooltip is vanilla's, with tooltip images such as a bundle's contents. It shows only while pointer input would go to vanilla, so it is hidden while a modal dialog is open or while the pointer is over Strata content.

## Slot rendering

The `SlotRendering` passed to the constructor says who draws the slots. Slot positions, input, networking, the carried stack and slot tooltips are vanilla's in both modes.

| | `SlotRendering.VANILLA`, the default | `SlotRendering.STRATA` |
|---|---|---|
| Drawn by | Vanilla's container drawing, run from inside the panel's paint | The panel, from the theme's `slot.*` keys and item visuals |
| What other mods draw on slots | Shown | Not shown on this screen |
| Restyled by | Resource packs, as for vanilla screens | Strata themes and pack theme files |

The screen draws no container texture behind the slots, so in both modes the panel paints `slot.background`, an 18 by 18 square around each active slot's 16 by 16 item, before the slots themselves. Strata content drawn later, such as a dialog, covers the slots in either mode.

In Strata mode the panel draws each active slot:

- the hovered slot's `slot.highlight_back` behind the items and `slot.highlight_front` over them, 24 by 24 around the slot, for slots that are highlightable;
- the item through an item visual with its count and damage bar, using vanilla's model seed for the slot, and a fake slot's item with no entity holding it, as vanilla does;
- an empty slot's icon, such as an armor outline;
- while the player drags a stack over slots, each slot's share of it on a translucent white square, with the count in yellow where it reaches the slot's limit.

The screen also does what vanilla's slot drawing does besides drawing. It tracks the hovered slot, tells slot mouse actions such as bundles' when the pointer leaves a slot, and takes a slot that stops accepting the dragged stack out of the drag, counting the carried stack again. Inactive slots draw nothing.

| Theme key | Kind | Used for | Default |
|---|---|---|---|
| `slot.background` | Paint, optional | Behind each active slot, in both modes | Vanilla's `container/slot` sprite |
| `slot.highlight_back` | Paint, optional | Behind the hovered slot's item, in Strata mode | Vanilla's `container/slot_highlight_back` sprite |
| `slot.highlight_front` | Paint, optional | Over the hovered slot's item, in Strata mode | Vanilla's `container/slot_highlight_front` sprite |

The keys are also `MenuPanel.STYLE_SLOT`, `STYLE_HIGHLIGHT_BACK` and `STYLE_HIGHLIGHT_FRONT`.

## Forcing vanilla slots

Players can make screens draw their slots through vanilla. The first time a container screen opens and `config/strata-client.json` does not exist in the game directory, Strata writes it with the defaults:

```json
{
    "forceVanillaSlots": false,
    "forceVanillaSlotsFor": []
}
```

`"forceVanillaSlots": true` makes every Strata container screen draw its slots through vanilla. `"forceVanillaSlotsFor"` lists menu type ids, such as `"examplemod:grinder"`, for single screens. Menus without a type, such as the player's inventory, are only affected by `forceVanillaSlots`. The file is read each time a container screen opens, so changes need no restart. A file that cannot be read or parsed, and entries of the wrong type, are logged and count as the defaults; ids that are not valid identifiers are logged and skipped.

`slotRendering()` on the screen and on its `MenuPanel` returns the mode in use after the player's settings. Before the screen has opened, the screen's returns the mode the constructor asked for.

## Threading

Use the screen and its panel on the render thread, as vanilla's screens are.
