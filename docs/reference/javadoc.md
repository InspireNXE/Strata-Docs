# Javadoc

The Javadoc is the contract. Every public and protected type and member in the library modules is documented, and the build fails on a missing comment. The API and SPI docs state what implementors must guarantee: threading, null handling, units, coordinate spaces and what happens on bad input.

No parameter accepts null unless its documentation says so, and passing null where it isn't allowed throws `NullPointerException`. The Javadoc doesn't repeat this on every method. The next section says how the API shows a value that can be absent.

## Absent values

The same rule holds across the API, so you can tell from a signature what absent means:

1. A getter whose value can be absent in normal use returns an `Optional`. Examples are `Component.parent()`, `HasTooltip.tooltip()`, `HasCursor.cursor()`, `HasIcon.iconSprite()` and `iconVisual()`, `Image.sprite()`, `visual()` and `value()`, `Expander.content()` and `SplitPane.first()` and `second()`.
2. A setter that has one form takes null to clear: `Component.setId(null)` removes the id. A setter that has overloads never takes null and throws `NullPointerException` for it. It has a `clearX()` beside it instead: `clearTooltip()`, `clearPadding()`, `clearTextPadding()` and `clearIcon()`.
3. Text is never absent. An empty `Text` is none, so `setPlaceholder` and `Avatar.setName` take empty text where they would take nothing.
4. Index getters keep -1 for none, as `List.indexOf` does: `selectedIndex()` on lists, combo boxes, menu bars and radio groups.
5. Record components and event values may be null where their Javadoc says so, such as `TextSpan.element`, `Image.State.sprite` and the date of an empty picker.
6. A `PropertyKey` reads an absent value as null and writes null as a clear, so bindings use null for none.

Core's own peers and nodes keep null internally; only the API follows the rule.

## Ids

`ComponentId`, `ThemeId` and `SpriteId` implement `NamespacedId`, which holds the grammar once: a namespace that is not empty and has no colon, and a path that is not empty. An id prints as `namespace:path` and parses back to an equal id, splitting at the first colon, so a path may hold more colons. Text without a colon, or with an empty part, is refused with `IllegalArgumentException`. Do not implement `NamespacedId`.

The `strata` namespace belongs to Strata. Every built-in control and panel is registered as a component with the id `strata:` and its snake case name:

| Kind | Ids |
|---|---|
| Panels | `strata:anchor_panel`, `strata:stack_panel`, `strata:grid_panel`, `strata:wrap_panel`, `strata:absolute_panel`, `strata:scroll_panel`, `strata:spacer` |
| Display | `strata:label`, `strata:image`, `strata:avatar`, `strata:info_bar`, `strata:progress_bar`, `strata:busy_indicator`, `strata:separator` |
| Buttons | `strata:button`, `strata:drop_down_button`, `strata:cycle_button`, `strata:check_box`, `strata:toggle_button`, `strata:toggle_switch` |
| Fields | `strata:text_box`, `strata:password_text_box`, `strata:text_area`, `strata:spinner`, `strata:slider`, `strata:color_picker`, `strata:calendar_view`, `strata:date_picker`, `strata:time_picker`, `strata:date_time_picker` |
| Collections | `strata:list_view`, `strata:combo_box`, `strata:table_view`, `strata:tree_view`, `strata:grid_view` |
| Menus and panes | `strata:menu`, `strata:menu_bar`, `strata:flyout`, `strata:tab_bar`, `strata:expander`, `strata:split_pane`, `strata:scroll_bar`, `strata:navigation_view` |
| Windows | `strata:window`, `strata:message_box` |

Entries that a control holds, such as menu items, radio menu items, tabs and table columns, are made by the factory and have no component id.

| Module | For | Javadoc |
|---|---|---|
| `api` | Everyone writing screens and components | [Open](../javadoc/api/index.html) |
| `spi` | Adapter authors | [Open](../javadoc/spi/index.html) |
| `swing` | Swing applications | [Open](../javadoc/swing/index.html) |
| `core` | Contributors. Not part of the API. | [Open](../javadoc/core/index.html) |

Good starting points in the API Javadoc:

- `org.inspirenxe.strata.api.component.Component`: everything every component has.
- `org.inspirenxe.strata.api.event` (the package page): the order handlers and default behavior run in.
- `org.inspirenxe.strata.api.event.input.InputEvents`: every input event type.
- `org.inspirenxe.strata.api.extension.CustomComponent`: the rules for writing components.
- `org.inspirenxe.strata.api.text.Text`: making, styling and translating text.
- `org.inspirenxe.strata.api.theme.ThemeRef` and `ThemeRegistry`: themes by id, their layers and overrides from code.
- `org.inspirenxe.strata.spi` (the package page): the steps for writing an adapter, with each interface's requirements for implementors on its own page.

To build the Javadoc locally, run `./gradlew javadoc` in Strata or Strata-Swing. Each module's output is in its `build/docs/javadoc` directory.
