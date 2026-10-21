# Controls

Strata's built-in controls are interfaces in the subpackages of `org.inspirenxe.strata.api.control`, made by the factory: `display` (`Label`, `Image`, `Avatar`, `InfoBar`, `ProgressBar`, `BusyIndicator`, `Separator`), `button` (`Button`, `DropDownButton`, `CycleButton`, `CheckBox`, `RadioButton`, `RadioGroup`, `ToggleSwitch`), `field` (`TextBox`, `PasswordTextBox`, `TextArea`, `Spinner`, `Slider`, `ColorPicker`, `CalendarView`, `DatePicker`, `TimePicker`, `DateTimePicker`), `collection` (`ListView`, `ComboBox`, `TableView`, `TreeView`, `GridView`), `menu` (`Menu`, `MenuBar`, `Flyout`), `pane` (`TabBar`, `Expander`, `SplitPane`, `ScrollBar`, `NavigationView`) and `window` (`Window`, `MessageBox`). Each one has [events](events.md): a request (Pre) for each change the user can make, which a handler can cancel or rewrite, and a report (Post) for each change from any source. Each section below lists them in an Events table. Their theme keys are listed on each control below and in the [theme key reference](../reference/theme-keys.md). Scrolling is a container, [`ScrollPanel`](layout.md#scrollpanel), and popups, dialogs and tooltips are in [Overlays and tooltips](overlays.md); `Menu` and `Flyout` are controls that open themselves as popups.

## Making controls

Every factory method has a form with no arguments, and the forms with arguments are shorthands for it followed by setters. `factory.button("Save")` makes the same button as `factory.button().setText("Save")`, and `factory.infoBar(InfoBar.Severity.ERROR, "Not saved", message)` the same bar as `factory.infoBar()` with its severity, title and message set. Where a control shows your items, the form with no arguments shows each item through its [appearance](appearances.md): `factory.listView()`, `factory.treeView()` and `factory.gridView()` build each row or cell with `factory.cell(item)`, which shows the item's icon and name, and `factory.comboBox()` shows each option by its name. Pass a cell factory or a label function to show items your own way.

Setters return the control, so a control is made, set up and given its handlers in one chain. Text setters take a `String` or a [`Text`](text.md#text), which may be styled or translated.

## Attributes

Controls that do the same kind of thing share small interfaces called attributes, in `org.inspirenxe.strata.api.control.attribute`. A control has every attribute that fits it, so the same thing has the same name on every control, and code written against an attribute works for all of them.

| Attribute | Package | What it adds | Events | Binding keys | Controls |
|---|---|---|---|---|---|
| `HasText` | `text` | `text()`, `setText` | | `TEXT` | `Button`, `DropDownButton`, `CycleButton`, `CheckBox`, `RadioButton`, `ToggleSwitch`, `Label`, `InfoBar` and `MessageBox` (the message), `ProgressBar`, `BusyIndicator`, and through `TextInput` the text fields; also the entries `MenuItem`, `CheckMenuItem`, `SubmenuItem`, `Tab`, `NavigationItem` and `NavigationHeader` |
| `Titled` | `text` | `title()`, `setTitle` | | `TITLE` | `Window`, `MessageBox`, `Expander`, `InfoBar`, `NavigationView`; also the entry `TableColumn` |
| `TextEntry` | `text` | `length()`, `isEmpty()`, `clear()`, the caret, the selection, `setMaxLength`, `setReadOnly`, `setTextPadding`; nothing that carries the text | `MODIFY`, `MODIFIED` | | `TextBox`, `TextArea`, `PasswordTextBox` |
| `TextInput` | `text` | `TextEntry` plus `text()`, `setText`, `selectedText()` and the highlighter | `EDIT`, `EDITED` | `TEXT` | `TextBox`, `TextArea` |
| `Toggle` | `value` | `isSelected()`, `setSelected`, `toggle()` | `TOGGLE`, `TOGGLED` | `SELECTED` | `CheckBox`, `ToggleSwitch`, `RadioButton` |
| `Ranged` | `value` | `value()`, `setValue`, `min()`, `max()`, `setRange` | `CHANGE`, `CHANGED` | `VALUE` | `Slider`, `Spinner`, `ProgressBar`, `ScrollBar` |
| `Stepped` | `value` | `Ranged` plus `step()`, `setStep` | | | `Slider`, `Spinner` |
| `HasDate` | `value` | `date()`, `setDate`, `clearDate()`, `minDate()`, `maxDate()`, `setDateRange`, `setDateFilter`, `firstDayOfWeek()`, `setFirstDayOfWeek` | `PICK_DATE`, `DATE_PICKED` | | `CalendarView`, `DatePicker`, `DateTimePicker` |
| `HasTime` | `value` | `time()`, `setTime`, `clearTime()`, `clockFormat()`, `setClockFormat`, `isSecondsShown()`, `setSecondsShown`, `minuteStep()`, `setMinuteStep` | `PICK_TIME`, `TIME_PICKED` | | `TimePicker`, `DateTimePicker` |
| `ItemSource` | `item` | `items()`, `setItems`, `refresh()`, `setKeyFunction` | | `itemsKey()` | `ListView`, `TableView`, `TreeView`, `ComboBox`, `GridView`, `CycleButton` |
| `CellSource` | `item` | `setCellFactory` | | | `ListView`, `TreeView`, `GridView`; also the entry `TableColumn` |
| `Selection` | `item` | `selectedIndex()`, `selected()`, `select(index)`, `selectItem(item)`, `clearSelection()`: the current entry | `SELECT`, `SELECTED` | `SELECTED_INDEX`, `selectedItem()` | every control below that selects |
| `SingleSelection` | `item` | `Selection` and nothing more: the control only ever selects one entry | | | `ComboBox`, `GridView`, `CycleButton`, `TabBar`, `MenuBar`, `RadioGroup`, `NavigationView` |
| `MultiSelection` | `item` | `Selection` plus `setSelectionMode`, `selectedIndexes()`, `selectedItems()`, `selectIndexes`, `selectAll()` | `SELECT_MANY`, `SELECTED_MANY` | | `ListView`, `TableView`, `TreeView` |
| `Activatable` | `item` | `setActivateOnClick` | `ACTIVATE`, `ACTIVATED` | | `ListView`, `TableView`, `TreeView`, `GridView`, `NavigationView` |
| `RowView` | `item` | row height and spacing, the placeholder, scrolling, `setDeselectable` | | | `ListView`, `TableView`, `TreeView` |
| `Closable` | `popup` | `close()` | `CLOSE`, `CLOSED` | | `Window`, `Overlay`, and through `Openable` the others |
| `Openable` | `popup` | `Closable` plus `isOpen()` | `OPEN`, `OPENED` | | `ComboBox`, `DropDownButton`, `DatePicker`, `TimePicker`, `DateTimePicker`, `InfoBar`, and through `Popup` the popups |
| `Popup` | `popup` | `Openable` plus `open(rect)` and `openAt(x, y)`: a component that opens itself in an overlay | | | `Menu`, `Flyout` |

An attribute owns its events, so `Toggle.TOGGLED` fires for a checkbox, a switch and a radio button alike, and one handler on a container hears every control inside it that has the attribute:

```java
// Save the settings page whenever any checkbox, switch or radio button on it changes
settings.onEvent(Toggle.TOGGLED, event -> save());

// Refresh the buttons whenever any list, table, tree, combo box or grid in the form changes selection
form.onEvent(Selection.SELECTED, event -> updateButtons());
```

Every component also has the keys `Component.ENABLED` and `Component.VISIBLE`. A binding key names a property that [`bind`](binding.md) can keep in step with a state, and sits beside the events that report the property's changes:

```java
music.bind(Toggle.SELECTED, model.music);       // works on a checkbox, a switch or a radio button
```

The event's `control()` is typed as the attribute, such as `Toggle<?>`, and `target()` is the component that fired it. An event fires only where it means something: a progress bar has no user input, so it reports `Ranged.CHANGED` but never asks with `Ranged.CHANGE`. Each control's section below says which events it fires and when. Events that only one control has, such as `Button.PRESSED` or `TableView.SORTED`, stay constants on that control.

Entries such as menu items, tabs and table columns have the attributes without events (`HasText`, `Titled`, `CellSource`), so a menu item's `setText` is the same method as a button's. Only components bind, so an entry takes no binding key.

### Controls that show items

The `item` attributes fit together. A control that shows your data is an `ItemSource`: it keeps a copy of the list you give `setItems`, and `refresh()` shows the same items again after they changed inside. Each item gets a key, the item itself unless `setKeyFunction` says otherwise, and when `setItems` replaces the list the selection follows the key, so a list of offers fetched again from a server keeps the same offer selected. A `CellSource` builds a component for each item with its cell factory; lists, tables and trees build only the cells in view and drop them when they scroll out, so a cell is built from the item and keeps nothing that must outlive it.

Every control that selects is a `Selection` and also exactly one of these:

- `SingleSelection`: only ever one entry, such as a combo box, a tab bar or a radio group. It adds nothing to `Selection`; it says the current entry is the whole selection.
- `MultiSelection`: a list, a table or a tree, which select one entry in `SelectionMode.SINGLE`, the default, and any number in `SelectionMode.MULTIPLE`. See [ListView](#listview) for the clicks and keys.

Selection events carry indexes into what the control shows: a list's items, a table's sorted rows (`shownItems()`), a tree's visible rows (`shownItems()`), a tab bar's tabs, a navigation view's `allItems()`. Read the entry from the control, or use `selected()`. -1 means nothing is selected.

```java
offers.onEvent(Selection.SELECTED, event -> offers.selected().ifPresent(this::showOffer));
```

`Activatable` controls, the lists, tables, trees, grids and navigation views, report a double click or Enter on an entry as `ACTIVATED`, or a single click with `setActivateOnClick(true)`. There is no method that activates an entry from code. `RowView` controls, the lists, tables and trees, show their entries as rows of one height (`setRowHeight`, 20 units by default, and `setRowSpacing`), show a placeholder while there are none (`setPlaceholder`), scroll with `setScrollY` and `scrollToIndex`, and let a press on the selected row clear it once `setDeselectable(true)`.

`Stepped` is a `Ranged` control whose value moves in steps, a slider or a spinner. `setStep` rounds the value to the new step at once. A slider takes a step of 0 for any value in the range; a spinner needs a step above 0.

### Entries

Some controls hold a list of things that are not your data and not components: a menu's `MenuEntry`s, a navigation view's `NavigationEntry`s, a tab bar's `Tab`s and a table's `TableColumn`s. They follow the same rules:

- The factory makes them, with a form with no arguments and one that takes the text or title: `factory.menuItem("Copy")`, `factory.navigationHeader("Account")`, `factory.tab("Sell")`, `factory.tableColumn("Price")`. Each control also has shorthands that make and add one, such as `menu.addItem("Copy", this::copy)`.
- They are mutable. A change to one shows in its control at once, so you can keep an entry and enable, rename or check it later.
- An entry belongs to one control at a time. Adding one that another control holds throws `IllegalArgumentException`; remove it first.
- They are not components and not event targets. The control's events carry the entry, or for selection the entry's index, so a handler on the control or a container hears them.
- Call them on the UI thread, as you call controls.

## Label

Text the user reads but does not edit.

```java
Label title = factory.label("Settings").setStyle(TextStyle.DEFAULT.withBold(true));
Label help = factory.label(longText).setWrap(true);
help.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
```

| Method | Default | Notes |
|---|---|---|
| `setText(text)` | | A `String` or a [`Text`](text.md#text), which may be styled or translated. `\n` starts a new line. A new layout runs if the text changed. |
| `setWrap(boolean)` | off | Also break between words to fit the width offered. See [Wrapping text](layout.md#wrapping-text). |
| `setStyle(TextStyle)` | `null` | Overrides the theme's `label.text` for this label. `null` goes back to the theme. |
| `setTextAlignment(HorizontalAlignment)` | `LEFT` | How each line sits within the label's width. It only shows when the label is wider than its text, for example when stretched. `STRETCH` acts as `LEFT`. |
| `setTextScale(float)` | 1 | Draws and measures the text larger or smaller, such as 0.8 for small print. Not greater than 0 throws. |
| `setOverflow(TextOverflow)` | `VISIBLE` | `ELLIPSIS` cuts a line that does not fit the bounds and ends it with `...`, and the label asks for no more width than it is offered. |

With wrapping on, a single word wider than a whole line is broken between characters, and styled text wraps the same way, each piece keeping its part's style. The desired size is the widest line by the number of lines times the line height, all times the text scale.

```java
Label price = factory.label(Text.of("125.50").color(GOLD).append(Text.of("/ea").color(GRAY)))
        .setTextScale(0.8f);
Label name = factory.label(item.name()).setOverflow(TextOverflow.ELLIPSIS);
name.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.MIDDLE);
```

A label is not focusable and has no default behavior for input, so clicks and keys pass through to its ancestors. That makes labels handy click targets for a container-level handler; see [Handling events on a container](events.md#handling-events-on-a-container).

`style()` returns the style the label draws with: the one you set, or the theme's.

A label reads one key, `label.text` (text style, optional), unless you set a style; without it the label draws in `TextStyle.DEFAULT`.

A label fires no events, since the user cannot change it. `setText` reports nothing; [bind](binding.md) `HasText.TEXT` to keep it in step with a state.

## Image

A sprite drawn at a size you choose, such as an icon or a picture.

```java
--8<-- "controls/ControlExamples.java:image"
```

| Method | Default | Notes |
|---|---|---|
| `setSprite(SpriteId)` | | The sprite, looked up as described in [Sprites](theming.md#sprites) |
| `setImageSize(w, h)` | 16 by 16 | The size in UI units. It is the desired size and the aspect ratio `FIT` keeps. Negative or infinite sizes throw. |
| `setScaling(Scaling)` | `STRETCH` | `STRETCH` covers the bounds; `FIT` draws as large as fits with the image size's aspect ratio, centered |
| `setTint(argb)` | `0xFFFFFFFF` | Multiplied with the sprite. Lower alpha fades it. |
| `setVisual(Visual)` | | Shows an engine visual instead of a sprite, and takes its natural size |
| `setValue(value)` | none | Shows a value through its [appearance](appearances.md): its icon at the icon's natural size, or nothing, and its tooltip, asked anew each time the pointer arrives. A tooltip set afterwards replaces the value's until the next `setValue`, and a sprite or a visual set afterwards clears the value. |

`factory.image()` makes an empty image, `factory.image(sprite)` and `factory.image(visual)` show one, and `factory.image(value)` shows a value as `setValue` does. An image shows a sprite or a visual, never both: each setter replaces the other.

Strata cannot ask the engine how big an image file is, so set the image size yourself, usually to the size you want it drawn at. A missing sprite draws the engine's placeholder. An image reads no theme keys, is not focusable and has no input behavior, so clicks pass through to its ancestors. A disabled image looks the same as an enabled one; change its tint to show it. It fires no events.

### Engine visuals

A `Visual` is something only the engine can draw, such as a game's item icon. The API never names the engine's type: the adapter makes the visual from the engine object, and Strata hands it back to the adapter's canvas to draw. `factory.image(visual)` shows one at its natural size.

```java
// On Swing, any image
Image icon = factory.image(SwingVisuals.image(bufferedImage));
```

A canvas draws a placeholder for a visual it did not make. Each adapter documents the visuals it makes.

## Avatar

Who someone or something is, in a small square: their picture, or else their initials, or else a placeholder figure, with an optional badge for a count or a status.

```java
--8<-- "controls/ControlExamples.java:avatar"
```

The square shows the first of these it has:

1. A sprite or visual set with `setSprite` or `setVisual`, stretched over the square.
2. The icon of the value's [appearance](appearances.md). An adapter that registers an appearance for its player or account type makes `factory.avatar(account)` show that face with no more code.
3. Initials: those set with `setInitials`, else worked out from the name set with `setName`, else from the value's name. They are the first letter of the first word and the first letter of the last word, uppercased for the language, so Ana Lima shows `AL`, Cleo shows `C`, and a name with no letters shows none.
4. The theme's `avatar.placeholder`, or `avatar.placeholder.group` for a group, when it has one. Otherwise only the background shows.

| Method | Default | Notes |
|---|---|---|
| `setValue(value)` | none | Its icon, its name for the initials, and its tooltip, all through its appearance. A value with no appearance is named by `String.valueOf`, so `factory.avatar("Ana Lima")` shows `AL`. |
| `setSprite(sprite)`, `setVisual(visual)` | none | The picture. Each replaces the other, and `null` clears it. |
| `setName(name)` | none | A `String` or a [`Text`](text.md#text) the initials come from, in place of the value's name |
| `setInitials(initials)` | worked out | `null` works them out from the name again, and an empty string shows none |
| `setGroup(group)` | `false` | Shows the group placeholder |
| `setAvatarSize(size)` | 24 | The side of the square, which is also the desired size. Negative or infinite sizes throw. |
| `setBadge(count)` | 0 | 0 hides the badge, and a count above 99 shows as `99+`. Negative counts throw. |
| `setBadgeVisual(visual)` | none | A visual shown in place of the number, such as a status dot, in a square a third of the avatar's size |

The initials draw centered in the `avatar.initials` style. They are scaled by a whole number so that a line takes at most two fifths of the square, and never drawn smaller than the font's own size, which keeps bitmap fonts crisp: with a 9-unit line, a 24-unit avatar draws them at their own size and a 48-unit one at twice it. Translated names follow the language.

`avatar.background` draws behind the square and `avatar.frame` over the picture, so a theme can round the corners with a frame sprite whose corners are the color behind the avatar. An avatar is always square, since canvases clip only to rectangles; in bounds of another shape it draws the largest square that fits, centered.

The badge sits at the square's bottom-right corner: its number is a line tall, with `badge.padding` around it on `badge.background`. The three `badge.*` keys carry no avatar prefix, since the items of a [navigation view](#navigationview) read them too.

An avatar has no input. It is not focusable, and clicks pass through it to its ancestors. Its tooltip is the value's, asked anew each time the pointer arrives, until you set one with `setTooltip`. To make an avatar clickable, put it in a component that handles clicks, or use a [button](#button) whose icon is the avatar's picture. A disabled avatar looks the same as an enabled one.

| Key | Required | Used for |
|---|---|---|
| `avatar.background` | yes | Behind the square |
| `avatar.frame` | no | Over the picture, the initials or the placeholder. Nothing if missing. |
| `avatar.initials` | no | The initials. Falls back to `TextStyle.DEFAULT`. |
| `avatar.placeholder`, `avatar.placeholder.group` | no | The figure shown with no picture and no initials. Nothing if missing, and the group's falls back to the plain one. |
| `badge.background` | no | Behind the badge's number. A red fill if missing. |
| `badge.text` | no | The badge's number. Falls back to `TextStyle.DEFAULT`. |
| `badge.padding` | no | Around the number. 2 at the sides and 1 at the top and bottom. |

An avatar fires no events.

## InfoBar

A strip in a screen's layout that tells the user something about the screen, such as a failed save or a message from a server, and stays until it is closed. A [toast](overlays.md) closes itself after its time; an info bar does not.

```java
--8<-- "controls/ControlExamples.java:infobar"
```

`factory.infoBar()` makes an open `INFO` bar with a close button and no title, message or action, and `factory.infoBar(severity, title, message)` sets those three.

From the left, the bar shows an accent strip in the severity's color, the severity's icon when the theme has one, the title in bold, the message, the action, and the close button at the right end. When the title, the message and the action fit on one line beside the close button, they share it. Otherwise the title and the message wrap, the message below the title, and the action goes below the message. The bar stretches across its slot unless you align it otherwise.

| Method | Default | Notes |
|---|---|---|
| `setSeverity(severity)` | `INFO` | An `InfoBar.Severity`: `INFO` for something the user may want to know, `SUCCESS` for something that went well, `WARNING` for something that may need their attention, `ERROR` for something that failed. Picks the background, the accent strip and the icon. |
| `setTitle(title)` | none | A `String` or a [`Text`](text.md#text), drawn before the message in the title style |
| `setText(message)` | none | The message. It is the bar's `HasText`, so code written against labels reaches it. |
| `setAction(component)` | none | Any component after the message, such as a button. `null` removes it. A component already in a tree, or made by another factory, throws. |
| `setAction(caption, runnable)` | | A link button that runs the code when pressed |
| `setClosable(closable)` | `true` | Whether the close button shows. A bar without one closes only from code. |
| `setIconShown(shown)` | `true` | Whether the severity's icon shows when the theme has one |
| `open()`, `close()` | open | Show the bar again, or close it from code |

A click on the close button, or Enter or Space while it has focus, asks `Closable.CLOSE`. Unless a handler cancels it, the bar closes. The close button is in the tab order, and its tooltip is the translated `strata.button.close`. The bar itself takes no focus.

A closed bar takes no space: it measures zero and takes its action and close button out of the tree, as a collapsed [expander](#expander) takes out its content, so nothing in it keeps focus. Wrap the bar in a panel of fixed height if the screen must not move when it closes. `open()` shows it again. `setVisible(false)` is separate from closing, so layout code that hides a bar is not the user closing it, and a hidden bar reports nothing.

The action is your content, so its events, such as a button's `PRESSED`, pass the bar on to its ancestors. Pressing it leaves the bar open; close it from a handler if you want. A disabled bar and everything in it take no input.

| Key | Required | Used for |
|---|---|---|
| `infobar.background` | yes | Behind the bar |
| `infobar.background.info`, `.success`, `.warning`, `.error` | no | Behind a bar of that severity. Each falls back to `infobar.background`. |
| `infobar.accent.info`, `.success`, `.warning`, `.error` | no | The strip, 3 units wide, down the left edge. Nothing if missing; the strip's space stays. |
| `infobar.icon.info`, `.success`, `.warning`, `.error` | no | The icon, a square one line tall. No icon, and no space for one, if missing. |
| `infobar.title` | no | The title. Falls back to `infobar.text` made bold. |
| `infobar.text` | no | The message. Falls back to `TextStyle.DEFAULT`. |
| `infobar.close`, `infobar.close.hovered` | no | The close button's `x`, and while it is hovered or focused. Fall back to `window.close` and `window.close.hovered`. |
| `infobar.padding` | no | Between the strip and the bar's edges and its contents. 6 at the sides and 4 at the top and bottom. |

The focused close button also draws `button.focus` as a ring.

| Event | Kind | Fires |
|---|---|---|
| `Closable.CLOSE` | Pre | When the user presses the close button. Cancel it to keep the bar open. |
| `Closable.CLOSED` | Post | After the bar closed, by the user or with `close()` |
| `Openable.OPENED` | Post | After `open()` showed a closed bar. A bar never fires `Openable.OPEN`, since only code opens it. |

## Button

A push button with a text caption, an icon, or both.

```java
Button save = factory.button("Save").onEvent(Button.PRESSED, event -> this.save());
```

| Method | Notes |
|---|---|
| `setText(text)` | One line. A new layout runs. |
| `press()` | Presses it from code: fires `PRESSED` but no `PRESS` request. Does nothing while disabled. |
| `setTextPadding(Insets)`, `setTextPadding(all)`, `setTextPadding(h, v)`, `setTextPadding(l, t, r, b)` | Space between the caption and the edges. By default the theme's `button.padding` (8 left and right, 5 top and bottom without it), `button.padding.icon` (2 on every side) for an icon with no caption, and none for a link. `null` follows the theme again. |
| `setIcon(SpriteId)`, `setIcon(Visual)` | Shows an icon before the caption. A visual is drawn at its natural size. |
| `setIconSize(width, height)` | The size the icon is drawn at, 16 by 16 by default. Strata cannot ask an engine for a sprite's size, so the caller gives it. |
| `clearIcon()` | Removes the icon. |
| `setLink(boolean)` | Makes the button a link. See below. |

A button presses on a left click, or on a press of Enter, keypad Enter or Space while it has focus. Held keys that repeat do not press it again. It consumes those inputs. A disabled button does neither and draws its disabled face.

It is focusable and tabbable, shows the hand cursor, and has a minimum size of 20 by 20. Its desired size is the caption plus the text padding. The caption is centered in whatever size the button ends up.

| Face | When |
|---|---|
| `button.normal` | Idle |
| `button.hovered` | Hovered or focused |
| `button.pressed` | Pressed with the pointer still over it |
| `button.disabled` | Disabled |
| `button.focus` (optional) | A one-unit ring drawn inside the edge while focused |

| Event | Kind | Fires |
|---|---|---|
| `Button.PRESS` | Pre | When the user presses it. Cancel to refuse the press. |
| `Button.PRESSED` | Post | After every press, including `press()` |

To react to other mouse buttons, such as a right click, handle `CLICK` yourself; see [Clicks](events.md#clicks).

### Icons

```java
Button refresh = factory.button("Refresh").setIcon(SpriteId.of("mymod", "refresh"));
Button settings = factory.button().setIcon(icon);
```

The icon is drawn 4 units before the caption, and the two are centered together as one block. A button with an icon and no caption is meant to be square: it pads by 2 units on every side, so a 16 by 16 icon gives 20 by 20. A disabled button draws its icon in half-bright gray. `setIcon` with a sprite keeps the current icon size, and with a visual takes the visual's size.

### Links

```java
Button terms = factory.button("Terms of sale").setLink(true).onEvent(Button.PRESSED, event -> this.openTerms());
```

A link has no frame. It draws only its caption, and the caption underlines while the link is hovered or focused. It has no padding and no minimum size, so it sits in a line of other text. It presses, focuses and fires events exactly like a button. `setLink(false)` brings the frame and the 20 by 20 minimum size back.

| Key | Used for |
|---|---|
| `button.link.text` (optional) | The caption. Falls back to `button.text`. |
| `button.link.text.hovered` (optional) | The caption while hovered or focused. Falls back to `button.link.text` underlined. |
| `button.link.text.disabled` (optional) | The caption when disabled. Falls back to `button.text.disabled`. |

A link reads none of the face keys and no focus ring.

## DropDownButton

A button with a down arrow after its caption that opens a popup below itself: a [menu](#menu-and-menubar) of commands, or a [flyout](#flyout) that holds any component.

```java
--8<-- "controls/ControlExamples.java:dropdown"
```

`factory.dropDownButton()` makes one with no caption and no popup, `factory.dropDownButton(text)` gives it a caption, and `factory.dropDownButton(text, popup)` a popup too.

| Method | Notes |
|---|---|
| `setPopup(popup)` | The `Menu` or `Flyout` it opens; `null` for none. Replacing an open popup closes it first. A popup belongs to one button at a time, so a popup another button holds, a menu that is a submenu or in a menu bar, and a popup made by another factory throw `IllegalArgumentException`. |
| `popup()` | The popup, or empty |
| `open()`, `close()` | Open the popup below the button, or close it, from code. Neither asks anything; both report. |
| `isOpen()` | True from `OPENED` to `CLOSED`. A popup the button holds but someone else opened does not count. |
| `setText(text)`, `setTextPadding(...)`, `setIcon(SpriteId)`, `setIcon(Visual)`, `setIconSize(width, height)`, `clearIcon()` | As on a [button](#button) |

A left click opens the popup, and so does Enter, keypad Enter, Space or Alt+Down while the button has focus (held keys that repeat do not). The popup takes focus. A menu opened by a key highlights its first item, so a second Enter chooses it. When the popup closes, for any reason, focus goes back to the button. A click on the open button closes the popup; with a popup that closes on a press outside, that press closes it and does nothing else.

A menu opens at least as wide as the button, for that opening only, and keeps any minimum width you gave it. A flyout opens at its content's size. With no popup, or one with nothing to show (a menu with no items, a flyout with no content), the button asks nothing and stays closed. A disabled button takes no focus, does not open, and closes its popup if it is open.

It looks and measures like a button, with the combo box's arrow, 7 by 4, at the right end of the space inside its padding, 4 units after the caption. The caption and icon center in the space left of the arrow. While open it draws `button.pressed`. It reads every `button.*` key a framed button reads, requires the same four faces, and adds one key:

| Key | Required | Used for |
|---|---|---|
| `dropdown.arrow` | no | The arrow's lines. Falls back to the caption's text color. A disabled button draws the arrow in the disabled caption's color whatever the key says. |

| Event | Kind | Fires |
|---|---|---|
| `Openable.OPEN` | Pre | When the user opens the popup. Cancel it to keep the popup closed. A handler may also change the menu's items first; a menu left empty does not open. |
| `Openable.OPENED` | Post | After the button opened its popup, including `open()`, once the popup has reported its own `OPENED` |
| `Closable.CLOSED` | Post | After the popup the button opened closed, for any reason, once the popup has reported its own `CLOSED`. The button never fires `Closable.CLOSE`. |

The popup's own events, such as `Menu.CHOSEN`, reach only handlers on the popup, since it opens in an overlay outside the screen's tree. Listen on the menu or the flyout, or give menu items their actions.

## CycleButton

A button that steps through a list of values, one on each click, and shows the current one after its label, as in `Difficulty: Hard`.

```java
CycleButton<Difficulty> difficulty = factory.<Difficulty>cycleButton("Difficulty")
        .setItems(List.of(Difficulty.values()))
        .onEvent(Selection.SELECTED, event -> this.apply());
```

| Method | Default | Notes |
|---|---|---|
| `setText(label)` | empty | The text before the value (`HasText`). With no label only the value shows, and with no value only the label. |
| `setValueLabelFunction(function)` | the value's appearance name | Turns a value into what the button shows: a [`Text`](text.md#text), or any other value shown by its [appearance](appearances.md)'s name, which is `String.valueOf` for a value with no appearance. `factory.cycleButton(label, function)` sets it at once. |
| `setItems(list)` | none | The values (`ItemSource`). The selection stays on the item with the same key, or moves to the first item. |
| `select(index)`, `selectItem(item)` | the first item | Chooses a value from code (`Selection`). |
| `cycle(steps)` | | Moves the selection from code, wrapping at the ends. One step forward from nothing selects the first item. |

A left click, or Enter or Space while focused, selects the next value, and the first one after the last. A right click, or Shift with any of those, selects the previous one. A disabled button, or one with fewer than two values, ignores them. The button asks for the label plus its widest value, so cycling never changes its size, and it is focusable, tabbable and at least 20 by 20 units. It draws with the `button.*` keys of [`Button`](#button).

| Event | Kind | Fires |
|---|---|---|
| `Selection.SELECT` | Pre | When the user steps to another value, with the index about to be selected. Cancel to keep the value, or set another index (-1 clears). |
| `Selection.SELECTED` | Post | After the selection changed from any source, with the index and the previous index |

## CheckBox

A box that is checked or not, with a caption to its right.

```java
--8<-- "controls/ControlExamples.java:checkbox"
```

| Method | Default | Notes |
|---|---|---|
| `setText(text)` | | One line. May be empty for a box alone. |
| `setSelected(boolean)` | unchecked | Checks or empties it. Fires `CHANGED`, and `TOGGLED` if it changed whether the box is checked. Works while disabled, so code can keep a disabled box in step with its setting. |
| `setState(CheckState)` | `UNCHECKED` | `UNCHECKED`, `CHECKED` or `MIXED`, the filled square a box shows while the things it stands for disagree. `isSelected()` is true only for `CHECKED`. |
| `setAllowsMixed(boolean)` | off | Makes the user's clicks cycle empty, checked, mixed. Off, a click checks an empty or mixed box and empties a checked one, and only code sets mixed. |
| `toggle()` | | Flips it from code: fires `TOGGLED` but no `TOGGLE` request. Does nothing while disabled. |
| `setBoxSize(size)` | line height plus 8 | The square box's size. `Float.NaN` goes back to the default, which is 17 with a 9-unit line height. |

A left click anywhere on the checkbox, caption included, toggles it, and so does a press of Enter, keypad Enter or Space while it has focus. It consumes those inputs. It is focusable and tabbable and shows the hand cursor. The caption sits 4 units right of the box, both centered vertically.

| Face | When |
|---|---|
| `checkbox.normal`, `checkbox.checked` | Idle |
| `checkbox.hovered`, `checkbox.checked.hovered` | Hovered or focused |
| `checkbox.disabled`, `checkbox.checked.disabled` (optional) | Disabled. Fall back to the idle faces. |
| `checkbox.mark`, `checkbox.mark.disabled` (optional) | A check drawn over a checked box, inside a fifth of its size from each edge |
| `checkbox.mark.mixed`, `checkbox.mark.mixed.disabled` (optional) | A square over a mixed box, inset by a fifth of its size. Fall back to the mark keys. A mixed box uses the unchecked faces. |
| `checkbox.focus` (optional) | A one-unit ring inside the box's edge while focused |
| `checkbox.text`, `checkbox.text.disabled` (optional) | The caption, and the caption when disabled. The first falls back to `TextStyle.DEFAULT` and the second to the first. |

Themes built from images can draw the check mark into the checked faces and leave `checkbox.mark` out. Themes built from colors use `checkbox.mark`.

| Event | Kind | Fires |
|---|---|---|
| `CheckBox.CHANGE` | Pre | When the user changes it, with the `CheckState` about to be taken. Cancel to refuse, or set another state. |
| `Toggle.TOGGLE` | Pre | After `CHANGE`, when the change would check or uncheck the box, with whether it would be checked. Cancel to refuse. |
| `CheckBox.CHANGED` | Post | After every change of state, with the new and previous `CheckState` |
| `Toggle.TOGGLED` | Post | After a change that checked or unchecked it. Going between empty and mixed fires only `CHANGED`. |

## Slider

A slider that picks a number from a range. It runs across by default; `setOrientation(Orientation.VERTICAL)`, or `factory.slider(Orientation.VERTICAL)`, stands it up with the minimum at the bottom.

```java
--8<-- "controls/ControlExamples.java:slider"
```

| Method | Default | Notes |
|---|---|---|
| `setRange(min, max)` | 0 to 1 | Bounds must be finite with `max` above `min`. The value is clamped into the new range. |
| `setStep(step)` | 0, continuous | Values snap to `min + k * step`. The maximum is always reachable, even off that grid. |
| `setValue(value)` | 0 | Clamped and snapped. NaN throws. Works while disabled. |
| `setLabel(DoubleFunction<String>)` | none | Text drawn centered over the track, recomputed when the value changes. Text wider than the slider is cut off. |
| `setOrientation(orientation)` | `HORIZONTAL` | `VERTICAL` runs up from the bottom. The value is kept. |
| `keyStep()` | | The step, or a hundredth of the range without one |

| Event | Kind | Fires |
|---|---|---|
| `Ranged.CHANGE` | Pre | When a press, a drag move or a key would change the value, with the new value already clamped and snapped. Cancel to keep the old value, or set another, which is clamped and snapped again. |
| `Ranged.CHANGED` | Post | After every change, including each drag move and changes from `setValue`, `setRange` and `setStep` |

Both fire only when the value actually changes, so setting the same value twice, or a value that clamps or snaps to the current one, fires nothing.

Whole numbers come from a step of 1:

```java
--8<-- "controls/ControlExamples.java:integers"
```

Input:

- A left press anywhere on the slider moves the thumb's center to the pointer. Dragging then follows the pointer, even outside the slider, once it has moved past the 4-unit drag threshold. A vertical slider follows only the pointer's height.
- While focused, Left and Down lower the value by the key step and Right and Up raise it, in either orientation. Page Down and Page Up move ten key steps, and Home and End go to the ends. Held keys repeat.
- Scrolling is ignored, so a scroll over a slider reaches its ancestors and a scrolling panel still scrolls.

It is focusable and tabbable and shows the hand cursor. A horizontal slider wants 150 by 20 units and a vertical one 20 by 150; stretch it or set its length to change that. The thumb is 8 units along the track and spans the slider across it.

| Key | Required | Used for |
|---|---|---|
| `slider.track` | yes | The track: the whole bounds |
| `slider.track.focused` | no | The track while focused. Falls back to `slider.track`. |
| `slider.track.disabled` | no | The track when disabled. Falls back to `slider.track`. |
| `slider.thumb` | yes | The thumb |
| `slider.thumb.hovered` | yes | The thumb while the slider is hovered, focused or pressed |
| `slider.thumb.disabled` | no | Falls back to `slider.thumb` |
| `slider.text`, `slider.text.disabled` | no | The label, drawn over the thumb as well as the track. The flat theme gives it a shadow so it stays readable on both. |
| `slider.padding` | no | Between the slider's edges and its label, which centers inside it and is cut to it. None by default. |

## The controls together

![Checkboxes, a disabled checkbox and two sliders](../assets/images/controls-basic.png)

## TextBox

A single-line text field.

```java
TextBox name = factory.textBox()
        .setMaxLength(32)
        .onEvent(TextInput.EDITED, event -> preview.setText("Hello, " + event.value()));
name.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
```

### Settings

| Method | Default | Notes |
|---|---|---|
| `setText(text)` | empty | Removes line breaks, cuts to the length limit and puts the caret at the end. Fires `EDITED` if the text changed, but no `EDIT` request. |
| `setMaxLength(n)` | `Integer.MAX_VALUE` | Counted in code points. Longer text is cut at once, which fires `EDITED`. Negative throws. |
| `setPlaceholder(text)` | none | A hint drawn in `textbox.placeholder` while the box is empty, focused or not. It is never part of `text()`. |
| `setTextPadding(Insets)` and its number forms | the theme's `textbox.padding`, else 4 left and right, 5 top and bottom | Space between the text and the edges; `null` follows the theme again |
| `setReadOnly(boolean)` | off | The box still focuses, selects and copies, but does not edit, and shows no caret. Its text uses `textbox.text.readonly`. |
| `setHighlighter(h)` | none | Draws parts of the text in their own styles, for syntax coloring, highlighted matches or colored input. See [Styled text in fields](text.md#styled-text-in-fields). |
| `setSelectAllOnFocus(boolean)` | off | Gaining focus selects everything, so typing replaces it. |

### Reading and selecting

| Method | Returns |
|---|---|
| `text()` | The text, never null |
| `caret()` | The caret, a char index from 0 to `text().length()` |
| `selectionStart()`, `selectionEnd()` | The selected range. Equal when nothing is selected. |
| `selectedText()` | The selected text, or empty |
| `select(anchor, caret)` | Selects a range. The anchor stays put and the caret is the moving end, as after Shift+arrow. Out-of-range indexes are clamped. |
| `selectAll()` | Selects everything, caret at the end |

Indexes count UTF-16 chars, like `String` indexes, but never fall inside a surrogate pair: editing always works on whole code points, so an emoji is deleted in one go. An index inside a pair given to `select` moves to the pair's start.

### Mouse

- A click puts the caret at the nearest character boundary. Shift+click extends the selection to it.
- A drag selects by character. A drag that starts with a double click selects whole words.
- A double click selects the word, or the run of spaces, under the pointer. Double-clicking the first letter of a word selects that word.
- A triple click selects everything.

### Keyboard

| Keys | Action |
|---|---|
| Left, Right | Move one code point. With a selection and no Shift, collapse it to its start or end. |
| Home, End | Move to the start or end |
| Ctrl+Left, Ctrl+Right | Move by word |
| Backspace, Delete | Delete the selection, or one code point |
| Ctrl+Backspace, Ctrl+Delete | Delete the selection, or one word |
| Shift with any movement | Extend the selection |
| Ctrl+A | Select all |
| Ctrl+C, Ctrl+X | Copy, cut |
| Ctrl+V | Paste. Line breaks in the pasted text are removed. |
| Typing | Replaces the selection. Text past the length limit is dropped. |

Shortcuts need Control held without Alt, so AltGr combinations, which many layouts report as Control plus Alt, type their characters instead of triggering shortcuts. On macOS, see the [platform note on Command](../platforms/differences.md#modifier-keys).

### Focus and the engine

The box is focusable and tabbable and shows the text cursor. Focus by Tab or `requestFocus()` puts the caret at the end; focus by a click puts it at the click. While focused, it:

- asks the engine for text input, which some engines need before they send any characters;
- consumes every key press that types a character (any printable key without Control or Alt), so engine shortcuts on those keys do not fire while you type;
- consumes the editing keys and shortcuts in the table above;
- does not consume Tab, Escape, Up, Down, Page Up or Page Down. Tab moves focus and Escape reaches the engine, which may close the screen. Enter is consumed only when a `SUBMIT` or `SUBMITTED` handler is on the box's path; otherwise it bubbles to ancestors. While the [suggestion list](#suggestions) is open, the box consumes Up, Down, Tab, Enter and Escape itself.

A disabled box ignores input.

### Drawing

The caret is one unit wide and blinks every half second on the host's clock. It shows again after each edit or move. When the text is wider than the box, it scrolls sideways to keep the caret in view.

| Key | Required | Used for |
|---|---|---|
| `textbox.normal` | yes | Background when not focused |
| `textbox.focused` | yes | Background when focused |
| `textbox.disabled` | yes | Background when disabled |
| `textbox.text` | no | Text style. Falls back to the default text style. |
| `textbox.text.disabled` | no | Text when disabled. Falls back to `textbox.text`. |
| `textbox.text.readonly` | no | Text when read-only. Falls back to `textbox.text.disabled`. |
| `textbox.placeholder` | no | The placeholder. Falls back to `textbox.text.disabled`. |
| `textbox.selection` | no | Behind selected text. Falls back to translucent blue. |
| `textbox.caret` | no | The caret. Falls back to light gray. |
| `textbox.suggestions` | no | Behind the suggestion list. Falls back to `combobox.popup`. |
| `textbox.suggestion.text` | no | The text of each suggestion. Falls back to `textbox.text`. |

The suggestion rows use the [`ListView`](#listview) row keys, the highlighted one as the selected row.

### Suggestions

Give the box a suggestion provider and it offers completions in a popup list below it as the user types:

```java
TextBox name = factory.textBox()
        .setSuggestionProvider(TextBox.SuggestionProvider.words(List.of("Ana", "Ben", "Cleo", "Dev")))
        .onEvent(TextBox.COMPLETED, event -> greet(event.value().text()));
```

A provider is a function from the text and the caret to a list of `TextBox.Suggestion`s. A suggestion holds the text the list shows and the range of the box's text it replaces, so one provider can complete the word at the caret and another the whole text:

```java
box.setSuggestionProvider((text, caret) -> text.startsWith("/")
        ? commands.stream()
                .filter(command -> command.startsWith(text))
                .map(command -> new TextBox.Suggestion(command, 0, text.length()))
                .toList()
        : List.of());
```

`TextBox.SuggestionProvider.words(list)` completes the word around the caret from a list. The word is the run of characters other than whitespace around the caret. Every entry that starts with the part of the word before the caret, ignoring case, is suggested in the list's order, except an entry equal to the whole word, and each suggestion replaces the whole word. It suggests nothing for a caret with no word before it, and it copies the list, so later changes to it are not seen.

A `Suggestion`'s range counts chars, as the caret does: `start` is where the replaced range begins and `end` where it ends, exclusive. A suggestion with `start` equal to `end` inserts its text at that point, and a range that starts below 0 or ends before it starts throws. The provider runs on the UI thread each time the box asks, so keep it quick. `setSuggestionProvider(null)` turns suggestions off again.

The box asks the provider after each edit by the user that changes the text, and on Ctrl+Space. If there are suggestions, the list shows them with the first highlighted, at least as wide as the box and up to `setMaxVisibleSuggestions(n)` rows tall (8 by default) before it scrolls. If there are none, the list closes. A read-only or disabled box never asks.

| Keys or input | While the list is open |
|---|---|
| Up, Down | Move the highlight, wrapping at either end |
| Tab, Enter, keypad Enter | Accept the highlighted suggestion. Enter then does not submit. |
| Click on a row | Accept that suggestion. The box keeps focus and its caret. |
| Escape | Close the list |
| Moving the caret with the keys, losing focus, `setText`, a press outside the list | Close the list |

Accepting closes the list, then replaces the suggestion's range with its text and puts the caret after it. That is an edit by the user, so it goes through `TextInput.EDIT` like typing, and the list stays closed until the next edit. `showSuggestions()` and `hideSuggestions()` open and close the list from code, and `shownSuggestions()` returns what it shows.

### Events

| Event | Kind | Fires |
|---|---|---|
| `TextEntry.MODIFY` | Pre | When the user's typing, deleting, cutting or pasting would change the text, before `TextInput.EDIT`. It carries no value. Cancel to reject the edit; `EDIT` then does not fire. |
| `TextInput.EDIT` | Pre | For the same edits, unless `MODIFY` was canceled, with the whole text the edit would produce. Cancel to reject the edit, or set another text, which is cleaned of line breaks and cut to the length limit. |
| `TextInput.EDITED` | Post | After every change of the text, with the new and previous text, including `setText` and `setMaxLength` cutting |
| `TextEntry.MODIFIED` | Post | After `EDITED`, for every change, with the new and previous length in code points |
| `TextBox.SUBMIT` | Pre | When the user presses Enter or keypad Enter. Cancel to ignore it. |
| `TextBox.SUBMITTED` | Post | After a submit nobody canceled, with the text |
| `TextBox.COMPLETE` | Pre | When the user accepts a suggestion, with the suggestion. Cancel to keep the text, or set another suggestion to apply instead. |
| `TextBox.COMPLETED` | Post | After an accepted suggestion was applied, unless `TextInput.EDIT` canceled the edit |

### Size

The desired size is 120 units wide and one line plus the text padding tall, with a minimum of 20 by 20. Stretch it or set a width to make it wider.

### Common recipes

Submit on Enter:

```java
--8<-- "events/EventExamples.java:submit"
```

Accept only a whole number up to a limit:

```java
amount.onEvent(TextInput.EDIT, event -> {
    String digits = event.value().replaceAll("[^0-9]", "");
    event.setValue(digits.isEmpty() ? "" : Long.toString(Math.min(Long.parseLong(digits), max)));
});
```

React to each change: `TextInput.EDITED`. React only when the user leaves the field: handle `Focusable.FOCUS_LOST`. React to any field of a form changing, password boxes included, without reading text: `TextEntry.MODIFIED`.

## PasswordTextBox

A single-line field for a password, a PIN or another secret. It edits like a [`TextBox`](#textbox) and keeps its text from showing up where nobody meant it to: on screen, on the clipboard, in a form's event handlers, in snapshots, in `toString` and log lines, and in `String`s left on the heap after the screen closes.

```java
--8<-- "controls/ControlExamples.java:password"
```

It cannot hide the text from code in the same process that sets out to read it. A `CHAR` handler on the box sees each typed code point, the engine delivers them, and any mod can read the heap.

### Reading the password

The box holds its text in a `char[]` that Strata zeroes, and makes a `String` of it only to show it while revealed.

| Method | What it does |
|---|---|
| `withPassword(reader)` | Hands the reader a copy of the text and zeroes the copy when the reader returns or throws. A reader that keeps the array sees zeros later. |
| `copyPassword()` | Returns a new copy on each call. You own it; zero it with `Arrays.fill(chars, '\0')` when done. |
| `matches(other)` | Whether two boxes hold the same text, compared in constant time without copying either, for a confirm field |
| `setPassword(char[])` | Copies the array with its line breaks removed, cut to the length limit. Your array is not kept. The caret goes to the end, and the text hides if it showed. |
| `clear()` | Zeroes and empties the box |
| `length()`, `isEmpty()` | The length in code points |

There is no `setPassword(String)`. Code that has a `String` passes `toCharArray()`, which keeps the conversion in sight, and code that needs a `String`, such as a network library, makes one itself with `new String(chars)`.

The box also zeroes and empties itself, firing nothing, after the screen that holds it closes or rebuilds and after the overlay that holds it closes. `Screen.closed()` runs first, so a screen can still read the box there. A collapsed expander or a swapped tab page keeps its boxes' text, since the content comes back.

### Settings

| Method | Default | Notes |
|---|---|---|
| `setMaskCodePoint(cp)` | `'*'` | What every code point draws as. Every font has `*`; `•` looks like other platforms but may be missing from bitmap fonts. Throws for an invalid code point. |
| `setRevealable(boolean)` | on | Off removes the eye, ignores Alt+F8 and hides the text if it showed |
| `setRevealed(boolean)` | off | Shows or hides the text from code, revealable or not |
| `setCodePointFilter(filter)` | none | Which code points the user may type or paste. It sees one code point at a time and belongs to the box; it does not bubble. |
| `setMaxLength(n)` | `Integer.MAX_VALUE` | Counted in code points, as on a text box |
| `setPlaceholder(text)` | none | Drawn in `textbox.placeholder` while the box is empty. The mask does not cover it. |
| `setReadOnly(boolean)`, `setSelectAllOnFocus(boolean)`, `setTextPadding(insets)` | off, off, the theme's `textbox.padding` | As on a text box |

```java
--8<-- "controls/ControlExamples.java:pin"
```

### Peeking

An eye at the right end of the box shows the text on a click and hides it on the next, and Alt+F8 does the same while the box has focus, as in Windows' own password fields. The eye cannot take focus, so Tab order is the same with or without it, and a click on it leaves focus and the caret in the box. It shows while the box is revealable, enabled and not empty, and the text always keeps room for it, so nothing moves when it appears. Its tooltip reads "Show password" or "Hide password", from the [translation keys](../reference/translation-keys.md) `strata.password.show` and `strata.password.hide`.

- Text set from code cannot be revealed by the user until they clear the box or replace all of it, so someone at an unlocked screen cannot read a remembered password. The eye draws disabled until then. `setRevealed(true)` still works from code.
- The box hides the text again when it loses focus, is disabled or is made unrevealable, and when code sets its text.
- While revealed, the box makes a `String` of its text once per edit to measure and draw it, and drops it when the text hides. A project that cannot accept that calls `setRevealable(false)`.

### What differs from a text box

| Path | In a password box |
|---|---|
| `text()`, `selectedText()`, `setText` | Not there. Use `withPassword`, `copyPassword`, `setPassword` and `clear`. |
| `TextInput.EDIT`, `EDITED` | Not fired. `TextEntry.MODIFY` carries no value and `MODIFIED` carries lengths. A `TextInput<?>` parameter does not take a password box. |
| Snapshots | `PasswordTextBox.State` holds the length, caret, anchor, length limit and reveal state, never the text |
| Ctrl+C, Ctrl+X | Do nothing, read-only or not, revealed or not |
| Ctrl+V | Pastes with line breaks removed. A paste that the length limit or the filter would change is refused whole, since a password silently cut short is worse than none. |
| Word movement | Ctrl+Left, Ctrl+Right, Ctrl+Backspace and Ctrl+Delete treat the whole text as one word, and a double click selects everything, so the caret does not show where spaces are |
| Suggestions, highlighter | Not there |
| Typed characters | Consumed while the box has focus, read-only included, so they never reach an ancestor's `CHAR` handler. Control characters are not typed. |
| Text input | The box asks the engine for `TextInputKind.SECRET` text, so it can turn off input methods and suggestions. Swing turns input methods off while the box has focus. |
| Tooltips | Only what code sets, and the eye's |
| `toString()` | The class name |

The caret and the selection sit on the mask glyphs. Every glyph of the mask is as wide as the next, so the boundary after the k-th of n code points is at k over n of the mask's width.

### Drawing

A password box reads the [`TextBox` keys](#drawing) for its face, text, selection, caret, placeholder and padding, so any theme that styles text boxes styles it too. Its own keys are optional:

| Key | Required | Used for |
|---|---|---|
| `passwordbox.mask` | no | The mask while enabled and editable. Falls back to `textbox.text`. A disabled or read-only box draws the mask in `textbox.text.disabled` or `textbox.text.readonly`. |
| `passwordbox.reveal` | no | The eye while the text is hidden. Falls back to an eye drawn in the placeholder's color. |
| `passwordbox.reveal.hovered` | no | The eye under the pointer. Falls back to `passwordbox.reveal`, else an eye in the text color. |
| `passwordbox.reveal.active` | no | The eye while the text shows, offering to hide it. Falls back to an eye with a slash, in the text color. |
| `passwordbox.reveal.disabled` | no | The eye while the user cannot reveal the text. Falls back to `passwordbox.reveal`, else an eye in the disabled text color. |

The eye is a square one line tall at the right of the text area, 2 units from the text. `Themes.flat()` sets none of these keys.

### Events

| Event | Kind | Fires |
|---|---|---|
| `TextEntry.MODIFY` | Pre | When the user's typing, deleting, cutting or pasting would change the text. It carries no value; cancel it to refuse the edit. |
| `TextEntry.MODIFIED` | Post | After every change, with the new and previous length in code points, including `setPassword`, `clear` and `setMaxLength` cutting. A wipe on close fires nothing. |
| `PasswordTextBox.REVEAL` | Pre | When the user clicks the eye or presses Alt+F8, with the state it would set. Cancel it, for example to ask for the account password first, or set another value. |
| `PasswordTextBox.REVEALED` | Post | After the text was shown or hidden, by the user, by `setRevealed`, or by the box hiding itself |
| `PasswordTextBox.SUBMIT` | Pre | When the user presses Enter or keypad Enter. Cancel it to ignore the key. |
| `PasswordTextBox.SUBMITTED` | Post | After a submit nobody canceled. It carries no value; read the box with `withPassword`. |

### Moving from `setPassword(true)`

Text boxes had a password mode until Strata gained this control. `TextBox.isPassword`, `setPassword(boolean)` and `setMaskCodePoint` are gone, and `TextBox.State` lost its `password` component. Make the field with `factory.passwordTextBox()`, read it with `withPassword`, and listen for `TextEntry.MODIFIED` where you listened for `TextInput.EDITED`.

### Size

As a text box: 120 units wide and one line plus the text padding tall, at least 20 by 20.

## ListView

A scrolling list of items that selects one row, or several in `MULTIPLE` mode. Each row is built by a cell factory, and only the rows in view exist, so a list of thousands of items costs no more than a screenful.

```java
ListView<Listing> listings = factory.<Listing>listView(listing -> factory.label(listing.name()))
        .setRowHeight(24)
        .setKeyFunction(Listing::record)
        .setPlaceholder("No results found.")
        .onEvent(Selection.SELECTED, event -> updateButtons());
listings.onEvent(Activatable.ACTIVATED, event -> buy(listings.items().get(event.value()), 1));
listings.setItems(fromServer);
```

| Method | Default | Notes |
|---|---|---|
| `setItems(list)` | empty | Rebuilds the rows in view. The selection stays on the item with the same key, or clears. |
| `setKeyFunction(f)` | the item itself | How items are told apart when `setItems` keeps the selection, such as a record id from the server |
| `setRowHeight(h)`, `setRowSpacing(s)` | 20, 0 | Every row has the same height |
| `select(index)`, `selectItem(item)` | nothing selected | Selecting scrolls the row into view. -1 clears. |
| `setDeselectable(boolean)` | off | A press on the selected row deselects it |
| `setSelectionMode(SelectionMode)` | `SINGLE` | `MULTIPLE` lets the user select several rows; see below |
| `selectIndexes(indexes)`, `selectAll()` | | Replace the selected set from code. More than one index in `SINGLE` mode throws. `selectAll()` does nothing in `SINGLE` mode. |
| `setActivateOnClick(boolean)` | off | A single click activates, as in a drop-down, instead of a double click |
| `setPlaceholder(text)` | none | Shown in the middle while there are no items |
| `refresh()` | | Rebuilds the rows in view from the same items |
| `setScrollY(y)`, `scrollToIndex(i)` | | Scroll by code |

A press on a row selects it, unless something in the cell consumed the press. A double click or Enter activates the selected item. While focused, Up, Down, Page Up, Page Down, Home and End move the selection. The wheel scrolls one row per notch, and the scroll bar works as in a [`ScrollPanel`](layout.md#scrollpanel). Rows sit inside the list's border widths, so a bordered list keeps its rows off the border.

In `MULTIPLE` mode, a press selects one row, Control and a press adds or removes one, Shift and a press selects the range from the anchor, Shift and the arrow keys extend it, and Control+A selects every row. `selectedIndexes()` and `selectedItems()` are the set in order; `selectedIndex()` is the current row, the one the keys move from. The user's changes ask `MultiSelection.SELECT_MANY` with the set they would leave, and every change of the set reports `MultiSelection.SELECTED_MANY`, followed by `Selection.SELECTED` when the current row moved too. `setItems` keeps the selected items it finds by key. Tables and trees work the same way; collapsing a tree branch drops its hidden rows from the set.

```java
list.setSelectionMode(SelectionMode.MULTIPLE)
        .onEvent(MultiSelection.SELECTED_MANY, event -> remove.setEnabled(!event.value().isEmpty()));
```

Build each cell fresh from its item, and keep nothing in it that must survive scrolling: a row that scrolls out is dropped and rebuilt when it comes back.

`factory.listView()` with no cell factory shows each item through its [appearance](appearances.md), with `factory.cell(item)`: the item's icon, if it has one, and its name, in a cell label, with the item's tooltip. `setCellFactory` replaces it and builds the rows in view again.

`ui.cellLabel(text)` makes the label the default cells use: the theme's `list.cell.padding` as its margin, or `ComponentFactory.CELL_PADDING` (4 units) at the sides without it, vertically centered, and cut short with an ellipsis. Its margin follows the theme until you set one. Use it in your own cell factories so text rows line up with tables and the built-in lists. The `list.background` key paints the list's whole area behind its rows, so an empty list shows its placeholder on it.

### Rows of controls

A cell can be any component, controls included, so a list of option rows needs nothing more than a cell factory. Each cell is content of the list, so the controls in it behave as they do anywhere else, and their events pass the list: one handler around the list hears every row.

```java
--8<-- "controls/ControlExamples.java:optionRows"
```

- Keep the value in your model, not in the control. The row is rebuilt from the item when it scrolls back into view, so set each control from the model in the cell factory and write changes back from a handler.
- A press that a control does not consume reaches the list and selects the row. Buttons only act on the click, so a press on a button selects its row; sliders, text boxes and the like consume their press and leave the selection alone. To keep rows from ever being selected, cancel `Selection.SELECT` on the list.
- Give the list a row height that fits the tallest control, and the row panel `Anchor.FILL` so it spans the row. A weighted [spacer](layout.md#spacer) between the label and the control pushes the control to the right edge.

| Key | Required | Used for |
|---|---|---|
| `list.background` | no | The list's whole area, behind its rows. Nothing if missing. |
| `list.row.selected` | yes | The selected row |
| `list.row` | no | Every other row. Nothing if missing. |
| `list.row.hovered` | no | The row under the pointer. Falls back to `list.row`. |
| `list.placeholder` | no | Placeholder text style |
| `scroll.*` | | The scroll bar, as for `ScrollPanel` |

| Event | Kind | Fires |
|---|---|---|
| `Selection.SELECT` | Pre | When a press or a key would change the selection, with the row index, or -1 to clear. Cancel to keep the selection, or set another row. |
| `Selection.SELECTED` | Post | After every change of the selected row, including `select`, `selectItem` and `setItems` |
| `MultiSelection.SELECT_MANY` | Pre | In `MULTIPLE` mode, when a press or a key would change the set, with the indexes it would leave. Cancel to keep the set, or set others. |
| `MultiSelection.SELECTED_MANY` | Post | After every change of the set, in either mode, before `Selection.SELECTED` |
| `Activatable.ACTIVATE` | Pre | When the user activates a row: a double press, Enter, or a click with `setActivateOnClick` |
| `Activatable.ACTIVATED` | Post | After an activation nobody canceled, with the row index |

## ComboBox

A drop-down that shows the selected option and opens a list of every option.

```java
ComboBox<SortType> sort = factory.<SortType>comboBox(SortType::label)
        .setItems(List.of(SortType.values()))
        .onEvent(Selection.SELECTED, event -> search());
```

`factory.comboBox()` shows each option by its [appearance](appearances.md)'s name, or by `String.valueOf` when it has none. `factory.comboBox(labelFunction)` and `setLabelFunction` show what the function returns: a [`Text`](text.md#text), or any other value shown by its appearance's name. The selection events carry option indexes, so read the option with `box.items().get(event.value())` or `box.selected()`.

A click opens the list as a [popup](overlays.md#popups) below the box, or above it when there is more room there, at least as wide as the box; a click on an option selects it and closes the list, and a click anywhere else closes it with no change. While closed and focused, Up, Down, Home and End change the selection, and Enter, Space or Alt+Down open it. While open, Up and Down move the highlight, Enter or Space picks it, and Escape closes without changing anything. `setMaxVisibleOptions(n)` (8 by default) caps how many rows show before the list scrolls. `setItems` keeps the selection on the option with the same key (`setKeyFunction`, the option itself by default), or selects the first option. `clearSelection()` leaves nothing selected, and the box then shows no text. `open()` and `close()` open and close the list from code; `open()` does nothing while the box is detached, disabled or has no options.

The box is focusable and tabbable and shows the hand cursor. It asks for its widest option plus its padding and the arrow, and one line plus 3 units above and below, so picking another option never changes its size. A disabled box ignores input.

| Key | Required | Used for |
|---|---|---|
| `combobox.normal` | yes | The box |
| `combobox.popup` | yes | Behind the open list |
| `combobox.hovered` | no | The box while hovered, focused or open. Falls back to `combobox.normal`. |
| `combobox.disabled` | no | The box when disabled. Falls back to `combobox.normal`. |
| `combobox.text`, `combobox.text.disabled` | no | The option text, and the selected option when disabled. The first falls back to `TextStyle.DEFAULT` and the second to the first. |
| `combobox.padding` | no | Between the box's edges and its value on the left and its arrow on the right. 4 at the sides and 3 at the top and bottom. |
| `list.*`, `scroll.*` | | The rows and the scroll bar of the open list, as for `ListView` |

| Event | Kind | Fires |
|---|---|---|
| `Openable.OPEN` | Pre | When the user opens the list. A handler can change the options first, or cancel. |
| `Openable.OPENED` | Post | After the list opened, including `open()` |
| `Selection.SELECT` | Pre | When the user picks an option, or Up, Down, Home or End on the closed box would change it, with the option index |
| `Selection.SELECTED` | Post | After every change of the selected index, including `select`, `selectItem`, `clearSelection` and `setItems` |
| `Closable.CLOSED` | Post | Each time the list closes, however it closed. The box never fires `Closable.CLOSE`. |

## Window

A framed panel with a title bar and a close button. Children are laid out below the bar as in an `AnchorPanel`, inside the theme's `window.padding`, 3 units on every side without it.

```java
Window window = factory.window("Exchange").add(content);
window.setSize(600, 370);
window.onEvent(Closable.CLOSED, event -> handle.close());
```

Dragging the title bar moves the window, through its [offset](layout.md#offset), kept inside the slot its parent gives it (`setMovable(false)` stops that). The close button and an Escape the window acts on fire `Closable.CLOSE`, which a handler can cancel, then call `close()`, which fires `Closable.CLOSED` and closes the overlay showing the window. A window that is a screen's content does not close itself: close the screen from a `CLOSED` handler. With no `CLOSE` or `CLOSED` handler on its path, and outside an overlay, it leaves Escape to the engine. `setClosable(false)` hides the button and ignores Escape. A new window is centered in its slot. `factory.window()` makes one with no title, and `factory.window(title)` one with a title. A window is a container: `add` puts content in it, and `setTitle` changes the bar.

A press with any button anywhere in a window, on its content included, [raises](components.md#drawing-order) it among its siblings before the press is delivered, so of several windows in an `AbsolutePanel` the one the user pressed comes to the front. It works even when a handler inside cancels the press or stops it. A sibling with a higher z index stays above it. `setRaiseOnPress(false)` turns it off. A window that is a screen's content or an overlay's has no siblings, so nothing changes.

```java
AbsolutePanel desk = factory.absolutePanel();
desk.add(factory.window("Ana").setSize(150, 90), 10, 10)
        .add(factory.window("Ben").setSize(150, 90), 90, 40);
// Notes stays over both, whichever was pressed last
desk.add(factory.window("Notes").setSize(110, 70).setZIndex(1), 240, 20);
```

### Resizing

`setResizable(true)` draws a grip in the bottom-right corner, 10 units square, and dragging it resizes the window with its top-left corner in place. `setEdgeResizable(true)` lets the user drag every edge too: a press within 3 units of an edge moves that edge, and one within 10 units of a corner moves both edges there. The opposite edge stays put.

```java
Window settings = factory.window("Settings")
        .setResizable(true)
        .setEdgeResizable(true)
        .add(content)
        .setSize(240, 160)
        .setMinSize(160, 100)
        .onEvent(Window.RESIZED, event -> this.saveSize(event.value()));
```

A resize keeps the window within its minimum and maximum sizes and inside the slot its parent gives it, and never makes it narrower than its title bar asks for or shorter than the bar plus the grip. Each resize sets the window's fixed size and moves it by its offset, the same way a move does, so the window stays where the user left it whatever its alignment. The pointer shows `RESIZE_ALL` over the grip and the corners and `RESIZE_HORIZONTAL` or `RESIZE_VERTICAL` over an edge. The grip sits over the window's content, so a control under it that takes the press, such as a button, wins; keep the corner clear, or give the window enough padding.

| Key | Required | Used for |
|---|---|---|
| `window.background` | yes | The whole window |
| `window.title` | no | The title bar. Nothing if missing. |
| `window.title.text` | no | The title, centered on the bar. Falls back to `TextStyle.DEFAULT`. |
| `window.close`, `window.close.hovered` | no | The close button's `x`, and while it is hovered. The first falls back to `window.title.text` and the second to the first. |
| `window.title.padding` | no | Around the title in the title bar. 3 on every side. |
| `window.padding` | no | Between the window's edges, below the title bar, and its content. 3 on every side. Padding set with `setPadding` wins over it. |
| `window.grip`, `window.grip.hovered` | no | The resize grip, drawn as six small squares in a triangle. The hovered paint shows while the pointer is over a part that resizes, and during a resize. |

| Event | Kind | Fires |
|---|---|---|
| `Closable.CLOSE` | Pre | When the close button or an Escape the window acts on asks to close it. Cancel to keep it open. |
| `Closable.CLOSED` | Post | Each time it closes, including `close()` |
| `Window.RESIZE` | Pre | On each move of a resize drag that would change the size, with the new size. Cancel to keep the size for that move, or set another size, which is kept within the same limits. |
| `Window.RESIZED` | Post | After each resize by the user, with the new and the previous size. A size set with `setSize` is layout and fires nothing. |

## MessageBox

A window with a message and a row of buttons, opened as a [dialog](overlays.md#dialogs).

```java
--8<-- "overlays/OverlayExamples.java:message-box"
```

`factory.messageBox(title, message, buttons)` makes one, and `factory.messageBox()` one with no title, no message and an OK button. Each button stands for a `MessageBox.Result`: `OK`, `CANCEL`, `YES`, `NO` or `CLOSE`. The buttons come in sets, a `MessageBox.Buttons`:

| Set | Buttons, left to right | The close button and Escape answer |
|---|---|---|
| `OK` | OK | `OK` |
| `OK_CANCEL` | OK, Cancel | `CANCEL` |
| `YES_NO` | Yes, No | `NO` |
| `YES_NO_CANCEL` | Yes, No, Cancel | `CANCEL` |
| `CLOSE` | Close | `CLOSE` |

`results()` lists a set's buttons and `dismissResult()` its last column. The buttons sit at the bottom right, 64 units wide and 4 apart, and the first one is focused when the dialog opens. Their captions are the translated `strata.button.ok`, `strata.button.cancel`, `strata.button.yes`, `strata.button.no` and `strata.button.close` from the [translation keys](../reference/translation-keys.md); `setButtonText(result, caption)` replaces one.

A button, pressed with a click, Enter or Space, the window's close button or Escape asks with `MessageBox.ANSWER`. A handler can cancel it to keep the box open, or rewrite it to any result, even one the box has no button for. Unless canceled, the box reports the result once with `MessageBox.ANSWERED` and closes the dialog. Escape counts when focus is inside the box, or when nothing has focus and the box is the content of the top dialog. Closing `window()` from code answers the set's dismiss result too, and reports it without asking.

| Method | Notes |
|---|---|
| `setText(message)` | The message. It wraps at 280 units (`MESSAGE_WIDTH`), and `\n` starts a new line. |
| `setTitle(title)` | The window's title |
| `setButtons(buttons)` | Another set. Captions set with `setButtonText` stay for the results that still have a button. |
| `setButtonText(result, caption)` | One button's caption, such as a more specific word than Yes |
| `window()` | The window the box is built on, to change its size or other settings |

The box is a composite: the events of its window and buttons reach handlers on those parts and on the box, and nothing outside it, so a handler around the dialog does not hear its buttons' `PRESSED`. Handle `ANSWER` and `ANSWERED` on the box. It draws with the `window.*`, `label.*` and `button.*` keys, plus one of its own:

| Key | Required | Used for |
|---|---|---|
| `messagebox.padding` | no | Between the window's padding and the message and buttons. 4 on every side. |

| Event | Kind | Fires |
|---|---|---|
| `MessageBox.ANSWER` | Pre | When a button, the close button or Escape answers, with the `Result`. Cancel to keep the box open, or set another result. |
| `MessageBox.ANSWERED` | Post | Once, when the box answers, before its overlay closes |

## TabBar

A row of tabs, one of them selected. It only picks a tab; show the matching content from `Selection.SELECTED`. Each tab is a `Tab`, made with `factory.tab(text)` or by the `addTab(text)` shorthand, which holds its text, whether it is enabled, and its tooltip. `tabs()` lists them, `addTab(tab)`, `addTab(index, tab)` and `removeTab(tab)` change them, `selected()` is the selected `Tab` and `selectItem(tab)` selects one.

```java
Tab sell = factory.tab("Sell")
        .setEnabled(false)
        .setTooltip(Tooltip.text("There are no items for sale."));
TabBar sides = factory.tabBar()
        .addTab("Buy")
        .addTab(sell)
        .onEvent(Selection.SELECTED, event -> showSide(event.value()));
```

A change to a tab shows in its bar at once, and a tab belongs to one bar at a time.

| Tab method | Default | Notes |
|---|---|---|
| `setText(text)` | | The tab's text |
| `setEnabled(enabled)` | `true` | A disabled tab cannot be selected by clicks or keys, draws its disabled look and still shows its tooltip. Disabling the selected tab keeps it selected. |
| `setTooltip(tooltip)` | none | Shown while the pointer rests on the tab, such as one that says why it is disabled |

The first tab added to an empty bar is selected, without an event. `addTab(index, tab)` keeps the selection on the same tab, and reports `SELECTED` if its index moved. Removing the selected tab selects the one that takes its place, or the last one, or none when no tab is left. `select(index)` selects a tab from code, enabled or not.

Tabs share the bar's width equally, with `setSpacing` between them (0 by default), and are one line plus 7 units tall. A click selects an enabled tab, and while the bar has focus Left and Right select the nearest enabled tab that way. The bar is focusable and tabbable, and an enabled tab shows the hand cursor. Disabling the whole bar draws every tab disabled, and the selected tab keeps a distinct `tab.selected.disabled` look.

| Key | Required | Used for |
|---|---|---|
| `tab.normal`, `tab.selected` | yes | Unselected and selected tabs |
| `tab.hovered`, `tab.disabled` | no | Fall back to `tab.normal` |
| `tab.selected.disabled` | no | The selected tab when it or the bar is disabled. Falls back to `tab.disabled`. |
| `tab.text`, `tab.text.selected`, `tab.text.disabled` | no | Tab text. The first falls back to `TextStyle.DEFAULT` and the others to it. |
| `tab.padding` | no | Between an unselected tab's edges and its text. 8 at the sides, 4 at the top and 3 at the bottom. |
| `tab.padding.selected` | no | The same for the selected tab. Falls back to `tab.padding`. |

| Event | Kind | Fires |
|---|---|---|
| `Selection.SELECT` | Pre | When a click or Left and Right would select another tab, with its index. A rewritten index that is disabled or out of range is ignored. |
| `Selection.SELECTED` | Post | After another tab was selected, including `select`, and after adding or removing tabs moved the selected tab's index. The first tab selected by `addTab` fires nothing. |

## ProgressBar

A bar filled from the left to show how far a value is through its range, with an optional caption, such as how much of a quota is used.

```java
ProgressBar quota = factory.progressBar()
        .setRange(0, limit)
        .setValue(used)
        .setText(used + "/" + limit);
```

The range is 0 to 1 by default, so the value is the fraction filled. `setValue` clamps to the range and NaN throws. `setRange` needs `max` above `min` and clamps the value into it. The bar takes no input and is not focusable.

| Key | Required | Used for |
|---|---|---|
| `progress.track` | yes | The whole bar |
| `progress.fill` | yes | The filled part |
| `progress.segment` | no | The moving segment of an indeterminate bar. Falls back to `progress.fill`. |
| `progress.text` | no | The caption, centered. Falls back to `TextStyle.DEFAULT`. |
| `progress.padding` | no | Around the caption. The bar is at least as wide as the caption plus this left and right, and a line plus this top and bottom tall. 2 on every side. |

For work whose length is not known, `setIndeterminate(true)` draws a moving segment instead of the fill. The segment is a quarter of the bar's width, at least 8 units, and slides in at the left and out at the right once every 1.5 seconds, by the host's clock, in whole units. The value and the caption are kept, and `setIndeterminate(false)` shows the value again.

```java
ProgressBar download = factory.progressBar()
        .setIndeterminate(true)
        .setText("Connecting");
```

| Event | Kind | Fires |
|---|---|---|
| `Ranged.CHANGED` | Post | After every change of the value, from `setValue` or from `setRange` clamping it. The bar has no input, so it never fires `Ranged.CHANGE`. Turning indeterminate on or off fires nothing. |

## BusyIndicator

A small sign that something is under way, like a "joining" or "loading" screen: three dots, one of them lit, with an optional caption above.

```java
BusyIndicator joining = factory.busyIndicator("Joining world");
```

The lit dot moves from the first dot to the last and back, one step every 300 milliseconds by the host's clock, for as long as the indicator is shown. Hide or remove it when the work is done. The dots are 4 units square and 4 apart, centered at the bottom of the bounds, and the caption sits centered 4 units above them. It asks for the wider of the caption and the dots (20 units), and for 4 units of height, plus one line and 4 with a caption. It is not focusable and takes no input.

| Key | Required | Used for |
|---|---|---|
| `busy.dot` | yes | Each unlit dot |
| `busy.dot.active` | yes | The lit dot |
| `busy.text` | no | The caption. Falls back to the default text style. |

It fires no events, since the user cannot change anything on it, and `setText` reports nothing.

## Separator

A thin line that divides a panel, across or down.

```java
--8<-- "controls/ControlExamples.java:separator"
```

`factory.separator()` makes a horizontal one, and `factory.separator(orientation)` either. A horizontal separator is one unit tall and stretches across its slot, at the top; a vertical one is one unit wide and stretches down its slot, at the left. Along the line it asks for no length, so it takes the length of the slot. `setThickness` changes the thickness, and `setOrientation` turns it and sets the matching alignment again, so call `setAlignment` after it if you want another.

A separator is not focusable, takes no input and fires no events. Key: `separator` (paint, required), which menus, navigation views and split panes fall back to for their own lines.

## RadioButton and RadioGroup

A set of choices of which at most one is picked. The group is not a component: it makes the buttons, and you place them in any layout.

```java
RadioGroup<Difficulty> difficulty = factory.radioGroup();
column.add(difficulty.add("Easy", Difficulty.EASY))
        .add(difficulty.add("Hard", Difficulty.HARD));
difficulty.selectItem(settings.difficulty())
        .onEvent(Selection.SELECTED, event -> settings.setDifficulty(difficulty.selected().orElseThrow()));
```

`factory.radioGroup()` makes an empty group, and `group.add(caption, value)` makes each button, unselected and not yet in any container. Each button stands for a value, which may be null, and `group.selected()` returns the value of the selected one. `buttons()` lists the buttons in the order they were made, `value(index)` is a button's value, `selectedButton()` the selected button, and a button's `group()` its group. A new group has nothing selected, and selecting a button deselects the one before. A click, Enter or Space selects a button. Up and Left move the selection and focus to the previous usable button in the group, Down and Right to the next, wrapping at the ends and skipping disabled, hidden and detached buttons. `button.setSelected(true)`, `group.select(index)` and `group.selectItem(value)` work while disabled too; `selectItem` with a value no button stands for clears the selection. A button measures and draws like a [`CheckBox`](#checkbox), with the mark inset by three tenths of the box.

| Key | Required | Used for |
|---|---|---|
| `radio.normal`, `radio.hovered` | yes | The box when not selected, plain and hovered or focused |
| `radio.selected`, `radio.selected.hovered` | yes | The box when selected |
| `radio.disabled`, `radio.selected.disabled` | no | Disabled boxes. Fall back to `radio.normal` and `radio.selected`. |
| `radio.mark`, `radio.mark.disabled` | no | Drawn over a selected box |
| `radio.focus` | no | A one-unit ring while focused |
| `radio.text`, `radio.text.disabled` | no | The caption |

| Event | Kind | Fires |
|---|---|---|
| `Toggle.TOGGLE` | Pre | On the button about to be selected, by a click, Enter, Space or an arrow key, with the value true. Cancel to keep the selection and focus as they are. |
| `Toggle.TOGGLED` | Post | On each button whose state changed, the one deselected first, from any source |
| `Selection.SELECTED` | Post | On the group, after the selected button changed from any source, with its index in `buttons()` or -1. Register it on the group: `group.onEvent(Selection.SELECTED, ...)`. |

The group is an event target of its own, outside the tree its buttons sit in. Its `SELECTED` reaches only handlers on the group and does not bubble, and the event's `target()` is the newly selected button, or the deselected one when the selection was cleared. The group never fires `Selection.SELECT`: to refuse a choice, cancel `Toggle.TOGGLE` on the buttons or on a container around them. The buttons' own `TOGGLE` and `TOGGLED` bubble as any control's do.

## ToggleSwitch

An on and off switch with a sliding knob, for settings that take effect at once.

```java
ToggleSwitch pvp = factory.toggleSwitch("PvP").setSelected(settings.pvp())
        .onEvent(Toggle.TOGGLED, event -> settings.setPvp(event.value()));
```

A left click anywhere on it, or Enter, keypad Enter or Space while it has focus, flips it; held keys that repeat do not. `setSelected` works while disabled; `toggle()` does not. It is focusable and tabbable and shows the hand cursor. With the default paddings, the track is the caption's line height plus 4 tall and twice that wide, the knob a square 2 units in from its edges, and the caption 4 units to its right. The knob jumps between the ends; it does not slide.

| Key | Required | Used for |
|---|---|---|
| `toggle.track`, `toggle.track.on` | yes | The track when off and on |
| `toggle.knob` | yes | The knob |
| `toggle.track.disabled` | no | Falls back to `toggle.track` |
| `toggle.knob.hovered`, `toggle.knob.disabled` | no | Fall back to `toggle.knob` |
| `toggle.focus` | no | A one-unit ring inside the track while focused |
| `toggle.text`, `toggle.text.disabled` | no | The caption |
| `toggle.track.padding` | no | How far the track reaches past a box a line tall and two lines wide. 4 at the sides and 2 at the top and bottom. |
| `toggle.knob.padding` | no | Between the knob and the track's edges. The knob is as tall as the track less this top and bottom. 2 on every side. |

| Event | Kind | Fires |
|---|---|---|
| `Toggle.TOGGLE` | Pre | When the user flips it, with the state about to be taken |
| `Toggle.TOGGLED` | Post | After every change, including `setSelected` and `toggle()` |

## Spinner

A number in a field with step buttons, such as a quantity.

```java
Spinner amount = factory.spinner().setRange(1, 64).setValue(1)
        .onEvent(Ranged.CHANGED, event -> this.amount = event.value().intValue());
Spinner price = factory.spinner().setRange(0, 10_000).setDecimals(2).setStep(0.25);
```

| Method | Default | Notes |
|---|---|---|
| `setRange(min, max)` | 0 to 100 | The value is clamped into it |
| `setStep(step)` | 1 | How far a button, arrow key or wheel notch moves it. Page Up and Page Down move ten steps. |
| `setDecimals(n)` | 0 | Digits kept after the point, 0 to 9. Values are rounded half up. |
| `increment()`, `decrement()` | | One step, as the buttons do. Nothing while disabled. |

The field is a [`TextBox`](#textbox) that accepts only digits, one point when decimals are allowed, and a leading minus when the minimum is negative. Typed text becomes the value on Enter and when the field loses focus; text that is not a number goes back to the value. The spinner asks for 60 units of width, and the buttons take the right 12, the upper half stepping up. A button whose step would leave the range draws disabled.

The field reads the `textbox.*` keys. The buttons read `spinner.button`, `spinner.button.hovered` and `spinner.button.disabled`, which fall back to `button.normal`, `button.hovered` and `button.disabled`, and draw their arrows in the `button.text` color.

| Event | Kind | Fires |
|---|---|---|
| `Ranged.CHANGE` | Pre | When the step buttons, the wheel, the keys or typed text (taken on Enter or focus loss) would change the value, already clamped and rounded |
| `Ranged.CHANGED` | Post | After every change, including `setValue`, `increment` and `decrement` |

## TextArea

Text over several lines, such as a note or a description.

```java
TextArea notes = factory.textArea().setText(item.notes()).setMaxLength(500)
        .onEvent(TextInput.EDITED, event -> item.setNotes(event.value()));
```

It edits like a [`TextBox`](#textbox), with a filter, a length limit, read-only mode and the same clipboard and word shortcuts. The differences:

- Enter inserts a line break. Tab still moves focus.
- Up and Down move a row and keep the caret's column across short rows. Page Up and Page Down move a page.
- Home and End go to the ends of the row; with Ctrl, to the ends of the text.
- A triple click selects the line. Pasted text keeps its line breaks.
- `setText` puts the caret at the start and scrolls to the top.

| Method | Default | Notes |
|---|---|---|
| `setWrap(boolean)` | on | Long lines break after the last space that fits. Off, rows are lines and the view scrolls sideways, with a scroll bar along the bottom when a line is too wide. |
| `setVisibleLines(n)` | 4 | How many lines the desired height holds |
| `setHighlighter(h)` | none | Draws parts of the text in their own styles, measured in those styles, on every row. See [Styled text in fields](text.md#styled-text-in-fields). |
| `setTextPadding(insets)` | the theme's `textarea.padding`, else `textbox.padding`, else 4 on each side | Space between the edge and the text; `null` follows the theme again |

The area asks for 160 units of width. Text taller than the view scrolls with the wheel (three lines a notch) and a scroll bar, and follows the caret. It reads the `textbox.*` keys, so it matches the text boxes around it, and the `scroll.*` keys for its bar.

| Event | Kind | Fires |
|---|---|---|
| `TextEntry.MODIFY` | Pre | When an edit of the user's, Enter included, would change the text, before `TextInput.EDIT`. No value; cancel to reject the edit. |
| `TextInput.EDIT` | Pre | For the same edits, unless `MODIFY` was canceled, with the whole text the edit would produce. Cancel or set another text. |
| `TextInput.EDITED` | Post | After every change of the text, including `setText` |
| `TextEntry.MODIFIED` | Post | After `EDITED`, with the new and previous length in code points, line breaks included |

## TableView

Rows of items under a header of columns, such as offers with a name, a seller and a price.

```java
TableView<Offer> offers = factory.<Offer>tableView()
        .addTextColumn("Item", GridLength.weighted(1), Offer::item)
        .addTextColumn("Seller", GridLength.fixed(70), Offer::seller)
        .addColumn("", GridLength.fixed(40), offer -> factory.button("Buy").onEvent(Button.PRESSED, event -> buy(offer)))
        .setItems(market.offers());
```

`factory.tableView()` makes a table with no columns and no items. Each column is a `TableColumn` [entry](#entries), made with `factory.tableColumn(title)` or by the `addColumn(title, width, cellFactory)` and `addTextColumn(title, width, text)` shorthands. `columns()` lists them, and `addColumn(column)`, `addColumn(index, column)` and `removeColumn(column)` change them.

| Column method | Default | Notes |
|---|---|---|
| `setTitle(title)` | | The header title (`Titled`) |
| `setWidth(width)` | `GridLength.weighted(1)` | How wide the column is, as a [`GridPanel`](layout.md#gridpanel) sizes its columns |
| `setCellText(text)` | each item's appearance name | Text cells: one-line [cell labels](#listview) cut short with `...`. The function returns a [`Text`](text.md#text), or any other value shown by its appearance's name. It also makes the column sortable by that text, ignoring case; set a comparator afterwards to sort another way. |
| `setCellFactory(factory)` | | Any component per cell (`CellSource`), built when the item's row comes into view |
| `setComparator(comparator)` | none | Makes the column sortable, or not sortable with `null`. If the rows are sorted by this column they sort again. |

A new column shows each item's appearance name as text and does not sort.

```java
TableColumn<Offer> price = factory.<Offer>tableColumn("Price")
        .setWidth(GridLength.fixed(60))
        .setCellText(offer -> format(offer.price()))
        .setComparator(Comparator.comparingDouble(Offer::price));
offers.addColumn(price).sortBy(price, true);
```

The body is a [`ListView`](#listview): it builds only the rows in view, scrolls, selects and activates the same way, and keeps the selection on an item's key. Fixed columns take their width, auto columns their title's width plus padding, and weighted columns share the rest. The header lines up with the cells even while the scroll bar shows.

A column with a comparator is sortable, and text columns sort by their text, ignoring case, unless given another. Clicking a sortable title sorts by it, ascending first and then flipping, and the title shows an arrow. `sortBy(column, ascending)` does the same from code, and `sortBy(null, true)` goes back to the set order; `sortColumn()` returns the column, or empty. Sorting only changes the order on screen: `items()` keeps the set order, while `shownItems()` and every row index follow the screen.

| Key | Required | Used for |
|---|---|---|
| `table.header` | yes | Behind each title |
| `table.header.hovered` | no | A sortable title under the pointer. Falls back to `table.header`. |
| `table.header.text` | no | The titles and sort arrows |
| `table.grid` | no | One-unit lines between columns. Nothing if missing. |
| `table.header.padding` | no | Between a column's edges and its title and sort arrow. 4 at the sides and none at the top and bottom. |
| `list.*`, `scroll.*` | | The body, as for `ListView` |

| Event | Kind | Fires |
|---|---|---|
| `Selection.SELECT` | Pre | As for a list, with a row of `shownItems()` |
| `Selection.SELECTED` | Post | After every change of the selected row, including a sort or `setItems` moving it |
| `Activatable.ACTIVATE` | Pre | When the user activates a row |
| `Activatable.ACTIVATED` | Post | After an activation nobody canceled |
| `TableView.SORT` | Pre | When a click on a sortable title asks to sort, with a `TableView.Sort` naming the column. Cancel or set another sort. |
| `TableView.SORTED` | Post | After the sort column or direction changed, including `sortBy`, a column losing its comparator and the sort column being removed |

## TreeView

Items in a hierarchy, such as store categories, each indented under its parent.

```java
TreeView<Category> categories = factory.<Category>treeView(category -> factory.label(category.name()))
        .setChildren(Category::subcategories)
        .setItems(store.categories());
categories.onEvent(Selection.SELECTED, event -> categories.selected().ifPresent(this::showCategory));
```

`factory.treeView(cellFactory)` makes a tree, and `factory.treeView()` one whose rows show each item's [appearance](appearances.md). `setChildren(function)` says how to find an item's children; until it is set every item is a leaf. The tree asks the children function for an item's children only when it shows them, so a large hierarchy costs what is expanded. Rows are indented 10 units per level (`setIndent`), then a 12-unit arrow column, then the cell. Clicking an arrow expands or collapses its item. Right expands the selected item or moves into it; Left collapses it or moves to its parent. The other keys and the scrolling work as in a list.

`setExpanded(item, boolean)`, `expandAll()` and `collapseAll()` change what shows, and a selection inside a collapsed item moves up to it. `selectItem(item)` searches the hierarchy, expands the item's ancestors and scrolls to it, and `select(row)` selects a row of `shownItems()`. `items()` and `setItems` are the roots, `shownItems()` the rows in order, and `depth(row)` how deep a row is, 0 for a root. `isExpanded(item)` says whether an item's key is expanded, shown or not. Items are told apart by their key, the item itself unless `setKeyFunction` says otherwise, and expanded keys carry over when the roots change. After the children of shown items change, call `refresh()`.

The expansion events carry a `TreeView.Expansion`, a record of the item and whether its children show. A handler on `EXPAND` may rewrite it, even to another item of the tree, and the tree applies it as `setExpanded` does. The press that starts a click on an arrow selects the row first, through `Selection.SELECT`. `expandAll()` and `collapseAll()` report `EXPANDED` once for each item that changed, and `selectItem` once for each ancestor it expanded.

The rows read the `list.*` keys. The arrows read `tree.arrow` (paint, optional), which falls back to a light gray.

| Event | Kind | Fires |
|---|---|---|
| `Selection.SELECT` | Pre | As for a list, with a row of `shownItems()`. Right and Left moving to a child or parent count too. |
| `Selection.SELECTED` | Post | After every change of the selected row, including expanding or collapsing moving it |
| `Activatable.ACTIVATE` | Pre | When the user activates a row |
| `Activatable.ACTIVATED` | Post | After an activation nobody canceled |
| `TreeView.EXPAND` | Pre | When a click on an arrow, or Right or Left, would expand or collapse an item, with a `TreeView.Expansion` |
| `TreeView.EXPANDED` | Post | After each item expanded or collapsed, including `setExpanded`, `expandAll` and `collapseAll` |

## Menu and MenuBar

A list of commands in a popup, opened from a right click, a button or a menu bar.

```java
Menu menu = factory.menu()
        .addItem("Copy", "Ctrl+C", this::copy)
        .addSubmenu("Send to", sendMenu)
        .addSeparator()
        .addCheckItem("Show prices", settings.prices(), settings::setPrices);
cell.onEvent(InputEvents.CLICK, event -> {
    if (event.button() == MouseButton.RIGHT) {
        menu.openAt(event.x(), event.y());
    }
});
```

Its entries are `MenuEntry`s, a sealed set of four kinds of [entry](#entries). The shorthands above make them; the factory makes them for you to keep, change and add with `addItem(entry)`. `items()` lists the entries, and `addItem(index, entry)`, `removeItem(entry)` and `clearItems()` change them.

| Entry | Made by | Methods |
|---|---|---|
| `MenuItem`: runs a command | `factory.menuItem(text)`, `menu.addItem(text, action)`, `menu.addItem(text, shortcut, action)` | `setText`, `setShortcut` (a hint drawn at the right, such as `"Ctrl+C"`, which binds no key), `setEnabled`, `setAction(Runnable)` |
| `CheckMenuItem`: a setting that is on or off | `factory.checkMenuItem(text)`, `menu.addCheckItem(text, checked, action)` | `setText`, `setSelected` (checks it without running the action), `setEnabled`, `setAction(Consumer<Boolean>)`. Choosing it flips the check and runs the action with the new state. |
| `SubmenuItem`: opens another menu beside this one | `factory.submenuItem(text)`, `menu.addSubmenu(text, submenu)`, `bar.addMenu(title, menu)` | `setText`, `setMenu`, `setEnabled`. Without a menu it opens nothing. |
| `MenuSeparator`: a line between groups | `factory.menuSeparator()`, `menu.addSeparator()` | none. It cannot be highlighted or chosen. |

A disabled item draws dimmed and cannot be highlighted or chosen. An action runs after the menus close and before `Menu.CHOSEN`, and does nothing until you set one. A menu opens from one place at a time: a submenu item in a menu or a bar, or a drop-down button. Giving a menu that already opens from one place to another throws `IllegalArgumentException`, and so does a submenu item that would open the menu it is in or one that menu opens from. A check item names its state as `Toggle` does but fires no `Toggle` events; listen to the menu's `CHOSEN` or give the item its action.

Kept entries can change while the menu is closed or open. This bar enables Paste each time the Edit menu opens:

```java
--8<-- "controls/ControlExamples.java:menuEntries"
```

```java
MenuItem paste = factory.menuItem("Paste")
        .setShortcut("Ctrl+V")
        .setAction(this::paste);
menu.addItem(paste);
paste.setEnabled(clipboard.hasText());
```

`factory.menu()` makes an empty menu. A menu is a component only so it can sit in a popup: do not add it to a container. `open(rect)` opens it below a rectangle as a [popup](overlays.md#popups), and `openAt(x, y)` with its corner at a point; `close()` closes it. A [drop-down button](#dropdownbutton) opens a menu below itself for you. `setMinWidth(width)` makes a menu at least that wide; it is never narrower than 60 units or its widest item. With the default paddings an item is one line plus 6 units tall, with 14 units at the left for the check mark and 14 at the right for the submenu arrow, and a separator is 5 units tall.

A menu is the whole content of its popup and has no parent, so its events reach only handlers on the menu itself, never the screen.

An open menu takes focus. Hovering an item highlights it and opens its submenu. Up and Down move the highlight past separators and disabled items; Right opens a submenu and Left closes it; Enter or Space chooses; Escape closes the menu it is pressed in. Choosing closes the whole chain of menus and then runs the action, and focus goes back to where it was before the first menu opened. A click outside closes them all.

```java
MenuBar bar = factory.menuBar()
        .addMenu("File", factory.menu().addItem("Close", handle::close))
        .addMenu("View", viewMenu);
```

`factory.menuBar()` makes an empty bar. Its titles are `SubmenuItem`s: `menus()` lists them, `addMenu(entry)`, `addMenu(index, entry)` and `removeMenu(entry)` change them, and a disabled title, or one with no menu, opens nothing. A title is its text plus the title padding, the bar is one line plus 6 units tall, and the bar is not focusable. A menu bar opens a title's menu below it on a click and closes it on a second. While one of its menus is open, moving the pointer onto another title switches to that menu, and Left and Right in a menu with no submenu open move along the bar. A menu belongs to at most one bar, parent menu or drop-down button. The bar's selection is its open menu: `selectedIndex()` is the open title or -1, `select(index)` opens a menu from code and `clearSelection()` closes it.

| Key | Required | Used for |
|---|---|---|
| `menu.background` | yes | Behind the menu |
| `menu.item.hovered` | yes | The highlighted item |
| `menu.text`, `menu.text.disabled` | no | Item text, check marks and arrows |
| `menu.shortcut` | no | Shortcut hints. Falls back to `menu.text.disabled`. |
| `menu.separator` | no | Separator lines. Falls back to `separator`. |
| `menu.item.padding` | no | Around an item's text; the left side holds the check mark and the right side the submenu arrow. 14 at the sides and 3 at the top and bottom. |
| `menu.padding` | no | Between the menu's edges and its items. 2 on every side. |
| `menubar.background` | no | Behind the bar. Nothing if missing. |
| `menubar.item.hovered` | no | A title under the pointer or with its menu open. Falls back to `menu.item.hovered`. |
| `menubar.text` | no | Titles. Falls back to `menu.text`. |
| `menubar.title.padding` | no | Around each title. 8 at the sides and 3 at the top and bottom. |

| Event | Kind | Fires |
|---|---|---|
| `Menu.CHOOSE` | Pre | When the user chooses an item, with the `MenuEntry`. Cancel to keep the menu open and the item as it is, or set another entry. |
| `Menu.CHOSEN` | Post | After the menus closed and the item's action ran, with the entry |
| `Openable.OPENED` | Post | Each time the menu opens, from code, its bar or a drop-down button. A menu never fires `Openable.OPEN`; the bar and the button ask on their own behalf. |
| `Closable.CLOSED` | Post | Each time the menu closes, for any reason |
| `Selection.SELECT` | Pre | On the bar, when a click, a hover while a menu is open, or Left and Right would open a menu, with its title index |
| `Selection.SELECTED` | Post | On the bar, after its open menu changed: opened from a title or `select`, switched, or closed. Switching reports once. |

## Flyout

A popup that shows any component, such as a color picker opened from a button, or a small form opened on a right click.

```java
--8<-- "controls/ControlExamples.java:flyout"
```

`open(rect)` opens it below a rectangle, or above when there is more room there, and `openAt(x, y)` with its corner at a point, as a [menu](#menu-and-menubar) opens. `openIn(rect)` lays it out inside an area, at its size and where its alignment places it, the area's top-left corner by default, so a flyout can open beside a component: pass the space from the component's right edge to the host's. A [drop-down button](#dropdownbutton) opens one for you. Opening an open flyout, or one with no content, does nothing. `factory.flyout()` makes an empty flyout and `factory.flyout(content)` one that shows a component. Like a menu, a flyout is a component only so it can sit in its popup: open it, and do not add it to a container.

| Method | Default | Notes |
|---|---|---|
| `setContent(component)` | none | What it shows; `null` for none. Content already in a tree, or made by another factory, throws `IllegalArgumentException`. While open, new content takes the old content's place, and clearing the content closes the flyout. |
| `setLightDismiss(dismiss)` | `true` | Whether a click outside closes it. Applies at once to an open flyout. |
| `close()` | | Closes it from code |

The flyout draws `flyout.background` and puts its content inside `flyout.padding`. It asks for the content's desired size plus the padding, no larger than the host, and stays inside the host.

Opening it moves focus to the first component of its content that Tab would stop at, or to the flyout itself when there is none; the flyout is focusable for that reason but not tabbable. While focus is inside it, Tab and Shift+Tab move only among the content's components and wrap at the ends. Escape closes it, as it closes the top overlay. When it closes, focus goes back to where it was before it opened.

With light dismiss, the default, a click outside the flyout closes it and does nothing else, as for any popup. Without it, the flyout stays open and the click goes on to what is under the pointer, so the user can keep working on the screen behind it. Escape and `close()` still close it.

The content is yours, so the control events of components inside it, such as a button's `PRESSED` or a color picker's `PICKED`, reach handlers on the flyout. They go no further, since the flyout opens in an overlay outside the screen's tree.

| Key | Required | Used for |
|---|---|---|
| `flyout.background` | no | Behind the flyout. Falls back to `combobox.popup`. |
| `flyout.padding` | no | Between the flyout's edges and its content. 4 on every side by default. |

| Event | Kind | Fires |
|---|---|---|
| `Openable.OPENED` | Post | Each time the flyout opens. A flyout never fires `Openable.OPEN`; a drop-down button asks on its own behalf. |
| `Closable.CLOSED` | Post | Each time it closes, for any reason: Escape, a click outside, `close()`, or its content cleared |

## Expander

A titled header that shows or hides a component below it.

```java
Expander filters = factory.expander("Filters").setContent(filterPanel).setExpanded(true);
```

`factory.expander()` makes one with no title, collapsed and empty, and `factory.expander(title)` gives it a title.

| Method | Default | Notes |
|---|---|---|
| `setTitle(title)` | none | One line, in the header (`Titled`) |
| `setContent(component)` | none | What shows below the header; `null` for none. The old content leaves the tree. A component that already has a parent throws. |
| `setExpanded(expanded)` | collapsed | Works while disabled too |
| `toggle()` | | Flips it as a click would, firing `EXPANDED` but not `EXPAND`. Does nothing while disabled. |

A left click on the header, or Enter, keypad Enter or Space while it has focus, flips it; held keys that repeat do not, and keys pressed inside the content do not either. While collapsed, the content is out of the tree: it takes no space and no input, and loses focus, though `content()` still returns it. The expander is focusable and tabbable, and the header shows the hand cursor. The header is one line plus 6 units tall: a 7-unit arrow, pointing right when collapsed and down when expanded, then the title. The content goes below it, across the expander's width, placed by its own alignment. The expander asks for the wider of the header and the content, and the header's height plus the content's while expanded. A disabled expander does not flip.

| Key | Required | Used for |
|---|---|---|
| `expander.header`, `expander.header.hovered` | no | Behind the header. Nothing if missing. |
| `expander.text`, `expander.text.disabled` | no | The title and arrow |
| `expander.header.padding` | no | Between the header's edges and its arrow and title. 4 at the sides and 3 at the top and bottom. |

| Event | Kind | Fires |
|---|---|---|
| `Expander.EXPAND` | Pre | When the user expands or collapses it, with the state about to be taken |
| `Expander.EXPANDED` | Post | After every change, including `setExpanded` and `toggle()` |

## SplitPane

Two components with a divider between them that the user drags.

```java
SplitPane split = factory.splitPane(Orientation.HORIZONTAL)
        .setFirst(categories.setAnchor(Anchor.FILL))
        .setSecond(offers.setAnchor(Anchor.FILL))
        .setPosition(0.3f);
```

`factory.splitPane()` makes an empty pane side by side, and `factory.splitPane(orientation)` either way. `HORIZONTAL` puts the first component on the left, `VERTICAL` on top; `setOrientation` changes it later. `setFirst` and `setSecond` place the two sides, and a missing side counts as empty. The position is the fraction of the space, after the 4-unit divider, that the first side gets, 0.5 by default. A drag with the left button keeps each side at least its minimum (20 units each by default, `setMinSizes`) when both fit; `setPosition` is not limited, and throws outside 0 to 1. Each side is placed in its part by its own alignment, so give it `Anchor.FILL` to fill it. The divider shows a resize cursor.

The pane asks for both sides' desired sizes and the divider along the split, and the larger of the two across it.

Keys: `split.divider` and `split.divider.hovered` (paints, optional), the divider and the divider under the pointer or being dragged. The first falls back to `separator` and the second to the first.

| Event | Kind | Fires |
|---|---|---|
| `SplitPane.MOVE` | Pre | For each drag step that would move the divider, with the position already kept within the minimum sizes. Cancel to keep the divider in place, or set another position, which is clamped to 0 to 1 and kept within the minimum sizes. |
| `SplitPane.MOVED` | Post | After every move, including `setPosition` |

## NavigationView

A pane of navigation items at the side of a screen, with the content of the selected item beside it, for screens with several sections such as a settings screen or a market with Buy, Sell and History.

```java
--8<-- "controls/ControlExamples.java:navigation"
```

`factory.navigationView()` makes an empty view in `AUTO` mode with nothing selected. It stretches over its slot by default. The pane holds, from the top: the toggle and the title (`setTitle`), the pane header (`setPaneHeader`, such as a search box), the items, which scroll, the footer items (`addFooterItem`), and the pane footer (`setPaneFooter`, such as the user's [avatar](#avatar)). The toggle, three lines, shows only while the pane can open over the content, so not in `EXPANDED`.

### Entries

The pane's entries are `NavigationEntry`s, a sealed set of three kinds of [entry](#entries):

| Entry | Made by | What it is |
|---|---|---|
| `NavigationItem` | `factory.navigationItem(text)`, `view.addItem(text, page)` | Something the user picks: a section of the screen, with an optional icon, badge, page and children |
| `NavigationHeader` | `factory.navigationHeader(text)`, `view.addHeader(text)` | A caption over a group of items, drawn in `nav.header.text`. It has only `setText`. The user cannot pick it, the keyboard passes over it, and it is hidden in the column of icons. |
| `NavigationSeparator` | `factory.navigationSeparator()`, `view.addSeparator()` | A line between groups. The user cannot pick it and the keyboard passes over it. |

`items()` lists the entries above the footer, and `addItem(entry)`, `addItem(index, entry)`, `removeItem(entry)` and `clearItems()` change them. `footerItems()`, `addFooterItem(entry)` and `removeFooterItem(entry)` do the same for the footer, whose entries do not scroll. `allItems()` lists every `NavigationItem` in the order selection indexes count them, headers and separators left out. An entry belongs to one view, or one parent item, at a time, and an entry made by anything but Strata's factory throws. Removing an item discards the pages kept for it and its children, and removing the selected item clears the selection.

| Item method | Default | Notes |
|---|---|---|
| `setText(text)` | | The item's text |
| `setIcon(sprite)`, `setIcon(visual)`, `clearIcon()` | none | A square one line tall before the text. In the column of icons, the icon is all the item shows. Without an icon the space stays, so the texts of items line up. |
| `setPage(builder)` | none | Builds the item's page with the view's factory the first time it is selected; see below |
| `setSelectable(selectable)` | `true` | Not selectable: a pick only activates it, as for Sign out, or expands it if it has children. Code can still select it. |
| `setEnabled(enabled)` | `true` | A disabled item draws in the disabled text, cannot be picked or reached with the keyboard, and still shows its tooltip. Disabling the selected item keeps it selected. |
| `setTooltip(tooltip)` | none | In the column of icons, an item with no tooltip shows its text |
| `setBadge(count)` | 0 | A count at the item's right end, or at the corner of its icon in the column of icons, with the shared `badge.*` keys; 0 hides it, and above 99 it shows `99+`. Negative throws. |
| `addItem(child)`, `removeItem(child)` | none | Children, shown below the item, indented, while it is expanded. An item with children shows an arrow at its right end. |
| `setExpanded(expanded)` | `false` | Shows the children. In a view, a change reports `NavigationView.ITEM_EXPANDED`. In the column of icons children open in a menu instead, whatever this says. |

### The view

| View method | Default | Notes |
|---|---|---|
| `setTitle(title)` | none | Beside the toggle at the top of the pane, and in the bar of `MINIMAL` (`Titled`) |
| `setDisplayMode(mode)` | `AUTO` | A `NavigationView.DisplayMode`; see below |
| `setPaneWidth(width)` | 120 | The pane's width, beside the content in `EXPANDED` and over it in the other modes |
| `setAutoWidths(compactBelow, minimalBelow)` | 400, 240 | Where `AUTO` changes mode. `minimalBelow` above `compactBelow` throws. |
| `setToggleShown(shown)` | `true` | Without the toggle, only code, and Enter or Space in `MINIMAL`, open the pane over the content |
| `setPaneHeader(component)`, `setPaneFooter(component)` | none | Your components at the top and foot of the pane, out of the tree in the column of icons. `null` removes one. |
| `setContent(component)` | none | Shown beside the pane while the selected item has no page, or nothing is selected |
| `setActivateOnClick(onClick)` | `true` | Off, a single click selects or expands without activating, and a double click activates. Enter and Space always activate. |
| `setExpanded(expanded)` | `false` | Opens or closes the pane over the content from code, reporting `EXPANDED` but never asking `EXPAND`. Does nothing in `EXPANDED` mode or while the view is disabled, hidden or not on a screen. |

A component given to `setPaneHeader`, `setPaneFooter` or `setContent` that is already in a tree, or was made by another factory, throws `IllegalArgumentException`. Disabled, the view closes the pane over the content and its menu, and it and everything in it take no focus or input.

### Display modes

`setDisplayMode` picks how the pane shows, with a `NavigationView.DisplayMode`:

- `EXPANDED`: the pane is open at its width, 120 by default (`setPaneWidth`), beside the content.
- `COMPACT`: the pane is a column of icons beside the content. The toggle at its top opens the full pane over the content.
- `MINIMAL`: the pane is hidden, and a bar across the top holds the toggle and the title. The toggle opens the pane over the content.
- `AUTO`, the default: `EXPANDED` at 400 units wide and more, `COMPACT` from 240, and `MINIMAL` below, worked out from the view's width each time it is laid out. `setAutoWidths(compactBelow, minimalBelow)` changes the two widths, and `shownMode()` says what the view chose.

The pane over the content is a popup laid out at the view's left edge, as wide as the pane and as tall as the view. A click outside it, Escape and a pick close it, and so does a change of the shown mode. `setExpanded(true)` opens it from code, and `isExpanded()` says whether it is open. While it is open the pane is in that popup, outside the view's tree, so the events of the pane header and footer reach only handlers on them until it closes. `setToggleShown(false)` leaves the toggle out.

### Picking

A click on an item asks `Activatable.ACTIVATE`, then, if the item is selectable and not selected, `Selection.SELECT`, and reports `SELECTED` and `ACTIVATED`. Activation fires on every click, the selected item's included, so an item that only acts, such as Sign out, is made not selectable and handled with `ACTIVATED`. A click on an item's arrow expands or collapses it, and a click elsewhere on an item with children that is not selectable does the same. In the column of icons, a click on an item with children opens them in a menu beside the column; choosing one picks that child. With `setActivateOnClick(false)`, a single click selects or expands without activating, and a double click activates.

The indexes of these events count `allItems()`: every item depth first, the children of collapsed items included, then the footer items. Read the item with `view.allItems().get(event.value())`, or `view.selected()`. Adding or removing items before the selected one moves its index, and the view reports `SELECTED` with the new one.

The item events carry a `NavigationView.Expansion`, a record of the item and whether its children show. A handler on `EXPAND_ITEM` may rewrite it to another item of the view; the view ignores null and items it does not hold. A click on an item's arrow asks `EXPAND_ITEM` and nothing else.

### Pages and content

An item's page is built the first time the item is selected and kept while the item stays in the view. Selecting another item takes the old page out of the tree, as a collapsed [expander](#expander) takes out its content, so a hidden page keeps no focus and costs no layout, and coming back finds it as it was. Removing the item, or calling `setPage` again, discards the kept page. While the selected item has no page, or nothing is selected, the view shows its own `setContent`, so code that manages its own content can set it from a `SELECTED` handler. Pages, the content, the pane header and the pane footer are your content, so their events pass the view.

### Keyboard

The view is one tab stop. While it has focus, a cursor marks one of the items the pane shows, starting on the selected one. Up and Down move it, skipping headers, separators and disabled items, Home and End go to the ends, Enter and Space pick its item, Right expands it, or opens its menu in the column of icons, and Left collapses it or moves to its parent. With the pane hidden in `MINIMAL`, Enter and Space open it. Tab moves on to the pane header, the pane footer and the content.

### Drawing

An item is a line plus `nav.item.padding`'s top and bottom tall, 19 units with a 9-unit line. The selected item draws `nav.item.selected` and a strip, `nav.indicator`, 3 units wide down its left edge; while its row is hidden, its nearest shown parent draws them. The toggle draws three lines in the item text's color on `nav.toggle`.

| Key | Required | Used for |
|---|---|---|
| `nav.pane` | no | Behind the pane beside the content and the bar of `MINIMAL`. Nothing if missing. |
| `nav.pane.overlay` | no | Behind the pane over the content. Falls back to `nav.pane`, then `combobox.popup`. |
| `nav.item.hovered` | no | Behind the hovered item. Falls back to `list.row.hovered`. |
| `nav.item.selected` | yes | Behind the selected item |
| `nav.indicator` | no | The strip down the selected item's left edge. Nothing if missing. |
| `nav.item.text`, `nav.item.text.selected`, `nav.item.text.disabled` | no | Item text, and the color of arrows and the toggle's lines. The selected one falls back to `nav.item.text`, the disabled one to `textbox.text.disabled`. |
| `nav.header.text` | no | Headers. Falls back to `nav.item.text.disabled`. |
| `nav.title.text` | no | The title |
| `nav.separator` | no | Separators. Falls back to `separator`. |
| `nav.toggle`, `nav.toggle.hovered` | no | Behind the toggle, and while the pointer is over it. If missing, `nav.toggle` draws nothing and `nav.toggle.hovered` falls back to `nav.item.hovered`. |
| `nav.focus` | no | The ring around the keyboard cursor's item. Falls back to `button.focus`. |
| `nav.item.padding` | no | Around an item's icon and text. 6 at the sides and 5 at the top and bottom. |
| `nav.pane.padding` | no | Between the pane's edges and what it holds. 2 on every side. |

| Event | Kind | Fires |
|---|---|---|
| `Selection.SELECT` | Pre | When the user picks a selectable item that is not selected, with its index |
| `Selection.SELECTED` | Post | After every change of the selection, including `select`, `selectItem` and items added or removed before it |
| `Activatable.ACTIVATE` | Pre | For every pick of an enabled item, with its index. Cancel it to stop the pick, or set another index. |
| `Activatable.ACTIVATED` | Post | After the pick, the selection or expansion it made included |
| `NavigationView.EXPAND` | Pre | When the user opens or closes the pane over the content with the toggle, with whether it would be open |
| `NavigationView.EXPANDED` | Post | After the pane over the content opened or closed, for any reason |
| `NavigationView.EXPAND_ITEM` | Pre | When the user expands or collapses an item, with an `Expansion` of the item and its new state |
| `NavigationView.ITEM_EXPANDED` | Post | After an item was expanded or collapsed, including `setExpanded` on the item |

## ScrollBar

A scroll bar on its own, for a component that scrolls its content itself, such as a custom list, a map or a chat log. [`ScrollPanel`](layout.md#scrollpanel), `ListView` and `TextArea` draw their own bars and do not need one.

```java
ScrollBar bar = factory.scrollBar()
        .setExtent(log.viewHeight(), log.contentHeight())
        .onEvent(Ranged.CHANGED, event -> log.setScrollY(event.value()));
```

The value is the scroll offset, as in a scroll panel: how far the content is scrolled down or right. `setExtent(viewport, extent)` sets the range to 0 through `extent - viewport` and the visible amount to `viewport`, which sizes the thumb and the page. Call it again whenever either length changes; the offset is clamped into the new range. A range with `min` equal to `max` means there is nothing to scroll, and a new bar starts that way.

| Method | Default | Notes |
|---|---|---|
| `setExtent(viewport, extent)` | | Sets the range and the visible amount from the two lengths. Negative or infinite lengths throw. |
| `setRange(min, max)` | 0 to 0 | `min` may equal `max`, but not exceed it. Clamps the offset. |
| `setVisibleAmount(amount)` | 0 | The viewport's length in the value's units. Changes neither the offset nor any event. |
| `setValue(offset)` | 0 | Clamped. NaN throws. |
| `setScrollRate(rate)` | 10 | How far a scroll notch or an arrow key moves |
| `setOrientation(orientation)` | `VERTICAL` | `HORIZONTAL` for content that scrolls sideways |
| `setSlim(boolean)` | off | A 2-unit bar with a thumb at least 16 long, drawn with the `scroll.slim.*` keys, for chat logs, tooltips and other tight places |

Input:

- A left press on the thumb grabs it, and dragging moves it, even outside the bar. A left press on the track beside the thumb moves one page toward the pointer. A page is the visible amount less one scroll rate, and at least one rate.
- Scrolling over the bar moves it by the scroll rate a notch. A horizontal bar takes vertical scrolling when there is no horizontal scrolling. The bar consumes a scroll only when the offset changed, so a scroll at the end reaches a scroll panel around it.
- The bar is not focusable, because the content it scrolls usually takes the keys. Made focusable with `setFocusable(true)`, it moves on Up and Down (vertical) or Left and Right (horizontal) by the scroll rate, Page Up and Page Down by a page, and Home and End to the ends.

The track covers the bounds and the thumb spans the bar across. Along the bar, the thumb's share of the track is the visible amount over the content's length, at least 32 units (16 when slim) and at most the track less 8, as in a scroll panel. With nothing to scroll the thumb fills the track. A vertical bar wants 6 by 100 units and a horizontal one 100 by 6 (2 thick when slim); stretch it along the content it scrolls. It shows the hand cursor.

A standard bar reads the same keys as a scroll panel's bars, so the two look alike. A slim bar's keys each fall back to the standard one, so a theme without them still draws it.

| Key | Required | Used for |
|---|---|---|
| `scroll.track` | no | The track. Nothing if missing. |
| `scroll.thumb` | yes | The thumb |
| `scroll.thumb.hovered` | no | The thumb under the pointer or being dragged. Falls back to `scroll.thumb`. |
| `scroll.thumb.disabled` | no | The thumb of a disabled bar, or of one with nothing to scroll. Falls back to `scroll.thumb`. |
| `scroll.slim.track` | no | A slim bar's track. Falls back to `scroll.track`. |
| `scroll.slim.thumb` | no | A slim bar's thumb. Falls back to `scroll.thumb`. |
| `scroll.slim.thumb.hovered`, `scroll.slim.thumb.disabled` | no | Fall back to `scroll.slim.thumb` |

| Event | Kind | Fires |
|---|---|---|
| `Ranged.CHANGE` | Pre | When a press on the track, a thumb drag move, a scroll or a key would change the offset, with the new offset already clamped. Cancel to stay put, or set another offset, which is clamped again. |
| `Ranged.CHANGED` | Post | After every change of the offset, including `setValue`, and `setRange` or `setExtent` clamping it. A change of the visible amount alone fires nothing. |

## GridView

Equal square cells over a list of items, such as an inventory, an icon picker or an image gallery.

```java
GridView<Item> inventory = factory.<Item>gridView(item -> factory.image(item.icon()))
        .setColumns(9)
        .setTooltips(item -> Tooltip.text(item.name()))
        .setItems(storage.contents())
        .onEvent(GridView.CLICKED, event -> this.take(event.value().index(), event.value().button()));
```

`factory.gridView(cellFactory)` makes a grid, and `factory.gridView()` one whose cells show each item's [appearance](appearances.md). There is one cell per item, null items being empty cells, in rows of `setColumns` (9 by default). Cells are 18 units square (`setCellSize`) with no space between them (`setSpacing`), and a filled cell shows the component its cell factory made, inset by `grid.cell.padding`, one unit by default. `setTooltips(function)` gives each filled cell a tooltip, and `hoveredIndex()` is the cell under the pointer or -1. The grid asks for its columns and rows of cells with the spacing between them. It is not focusable, so it has no keyboard selection, and it consumes the clicks on its cells, empty ones included. A click with any button fires `GridView.CLICK` and then `GridView.CLICKED` with the cell, its item, the button and the modifiers. `setSelectable(true)` also selects the cell a left click lands on. A left double click activates a cell, or a single click with `setActivateOnClick(true)`. `setItem(index, item)` rebuilds one cell. The grid is positional: `setItems` keeps the selected index, and the key function only matters to `selectItem`.

| Key | Required | Used for |
|---|---|---|
| `grid.cell` | yes | Each cell, behind its content |
| `grid.cell.hovered` | no | Over the cell under the pointer. Nothing if missing. |
| `grid.cell.selected` | no | Over the selected cell. Falls back to `grid.cell.hovered`. |
| `grid.cell.padding` | no | Between a cell's edges and its content. 1 on every side. |

| Event | Kind | Fires |
|---|---|---|
| `Selection.SELECT` | Pre | When a left click on a selectable grid would select another cell |
| `Selection.SELECTED` | Post | After every change of the selected cell, including `select` and `setItems` |
| `GridView.CLICK` | Pre | When the user clicks a cell with any button, with a `GridView.Click` |
| `GridView.CLICKED` | Post | After a click nobody canceled |
| `Activatable.ACTIVATE` | Pre | After the click's `GridView.CLICK`, when a left double click, or a single one with `setActivateOnClick`, would activate a cell |
| `Activatable.ACTIVATED` | Post | After an activation nobody canceled |

## ColorPicker

Picks an ARGB color with a square for saturation and brightness, a strip for hue, an optional strip for alpha and a hex field.

```java
ColorPicker tint = factory.colorPicker()
        .setAlphaShown(true)
        .setColor(settings.tint())
        .onEvent(ColorPicker.PICKED, event -> settings.setTint(event.value()));
```

The color is an `int` in ARGB order, opaque white by default. A press or drag in the square sets the saturation (left to right) and brightness (bottom to top), and on the strips the hue (red at the top, through yellow, green, cyan, blue and magenta, to red at the bottom) or the alpha (opaque at the top). Drags follow the pointer outside the area they started in. The hex field takes `RRGGBB` or `AARRGGBB`, with or without `#`, on Enter or when it loses focus; six digits keep the current alpha, and anything else puts the current color back. The picker itself is not focusable, so keyboard users type into the hex field.

The picker keeps the hue, saturation and brightness as well as the color, so a gray or black color keeps the hue the user chose: drag the brightness down to black and back up and the hue is still there. `setColor` works the hue out from the color, keeping the old hue for a gray and the old saturation too for black. `hue()`, `saturation()` and `brightness()` read them. `setAlphaShown(false)`, the default, hides the alpha strip and leaves the alpha as it is.

The square asks to be 96 units on each side, each strip is 10 wide and 4 to the right, and below them sit a swatch of the color and the hex field. Given more room, the square takes it.

| Key | Required | Used for |
|---|---|---|
| `colorpicker.frame` | no | One unit larger than the square, each strip and the swatch, behind them. Nothing if missing. |
| `colorpicker.marker` | no | The ring in the square and the bars on the strips. Falls back to white. |
| `colorpicker.marker.outline` | no | A one-unit line around the markers. Falls back to black. |
| `colorpicker.disabled` | no | Over the square and strips while disabled. Nothing if missing. |

The hex field uses the `TextBox` keys. The alpha strip and the swatch draw a gray and white checkerboard behind translucent colors.

| Event | Kind | Fires |
|---|---|---|
| `ColorPicker.PICK` | Pre | When a press, a drag or a hex code would change the color, with the new color. During a drag it fires for each move that changes it. Cancel to keep the color, or set another one, which applies as `setColor` does. |
| `ColorPicker.PICKED` | Post | After every change of the color, including `setColor` |

The color is an `int`, which `Ranged`'s `double` cannot hold, so the picker has its own events.

## Dates and times

Four controls pick dates and times: `CalendarView`, a month grid; `DatePicker`, a field with a calendar in a popup; `TimePicker`, a field with columns of hours and minutes in a popup; and `DateTimePicker`, one field for both. Values are `java.time` types with no zone: `LocalDate`, `LocalTime` and `LocalDateTime`. A screen shows what the user picked, and your project decides which zone it means. Every value may be empty, and empty is where the pickers start.

```java
--8<-- "controls/ControlExamples.java:booking"
```

They share two attributes, `HasDate` and `HasTime`, so one handler on a form hears every date or time in it:

| Method | Default | Notes |
|---|---|---|
| `setDate(date)`, `clearDate()` | empty | Kept inside the range. `null` clears. The filter does not apply to code. |
| `setDateRange(min, max)` | no limits | Both ends included; `null` for no limit at that end. A date outside moves to the nearest end. |
| `setDateFilter(allowed)` | every date | Which dates the user may pick. Refused days draw disabled. The control asks the filter whenever it draws or checks a date, so it may read state that changes. |
| `setFirstDayOfWeek(day)` | the language's | `null` follows the language again |
| `setTime(time)`, `clearTime()` | empty | Seconds are dropped unless shown, and the fraction of a second always |
| `setClockFormat(format)` | the language's | A `HasTime.ClockFormat`: `H12` for hours 1 to 12 with the half of the day, `H24` for hours 0 to 23. `null` follows the language again, and `clockFormat()` is empty while it does. |
| `setSecondsShown(shown)` | false | Hiding seconds drops them from the time |
| `setMinuteStep(minutes)` | 1 | A divisor of 60: the rows of the minute column and the step of Up and Down on the minutes. Typed minutes and minutes from code stay as they are. |

The three pickers, `DatePicker`, `TimePicker` and `DateTimePicker`, also share these:

| Method | Default | Notes |
|---|---|---|
| `factory.datePicker(date)`, `factory.timePicker(time)`, `factory.dateTimePicker(value)` | | Make a picker with a value. The forms with no arguments make an empty one. |
| `setPlaceholder(text)` | "Pick a date", "Pick a time", "Pick a date and time" | Shown while the field is empty and unfocused. The defaults are the translated `strata.date.placeholder`, `strata.time.placeholder` and `strata.datetime.placeholder`. |
| `setClearable(clearable)` | `true` | Whether Delete clears the value. Code can always clear it. |
| `open()`, `close()` | | Open or close the popup from code. `open()` reports `OPENED` but never asks `OPEN`, and does nothing while the picker is open, disabled or not in a tree. |

The language decides month and weekday names, the first day of the week, the order of a date's parts and their separator, and whether the clock has 12 or 24 hours. Strata reads them from the JDK's data for the translator's locale, after asking the translator for the `strata.date.*` and `strata.time.*` keys in the [translation keys reference](../reference/translation-keys.md#dates-and-times). Today comes from the platform's wall clock.

### CalendarView

A month to pick a day from, with a year view and a decade view to move far quickly.

```java
CalendarView calendar = factory.calendarView()
        .setDateRange(LocalDate.now(), null)
        .setMarkFilter(bookings::hasBookingOn)
        .onEvent(HasDate.DATE_PICKED, event -> showBookings(event.value()));
```

The header holds the month and year between two arrows, then come the short weekday names and six rows of days, always six so the calendar keeps its height. Days of the months before and after fill the rows in the outside style; `setOutsideDaysShown(false)` leaves their cells empty. Today has an outline, the picked day the `calendar.day.selected` paint, and a day the mark filter accepts (`setMarkFilter`) a small square under its number.

A click on a day picks it, and a click on a day of the month before or after browses there first. The arrows browse by a month, and stop at the months of the range. A click on the title zooms out to the twelve months of the year, and again to the twelve years around a decade; a click on a month or a year zooms back in. The wheel is left to the panel around the calendar.

While focused, a cursor marks one day, starting on the picked date, else today, else the first of the month. The arrow keys move it by a day or a week, Page Up and Page Down by a month (with Shift by a year), and Home and End to the ends of the month. It passes over disabled days, stays inside the range, and browses when it leaves the month. Enter or Space picks its day. Control+Up zooms out, and Control+Down or Enter zooms in.

`shownMonth()` and `setShownMonth(month)` read and set the month in view, and `setZoom` the view (`Zoom.MONTH`, `YEAR` or `DECADE`). `setDate` also shows the date's month.

A cell is a line plus the cell padding tall, and as wide as the widest day number or weekday name plus the padding, but never narrower than it is tall. The calendar asks for seven cells across and eight down (the header, the weekday names and six rows), plus its padding; given more room, the cells share it.

| Event | Kind | Fires |
|---|---|---|
| `HasDate.PICK_DATE` | Pre | When the user picks a day, with the day. Cancel it, or set another date, which applies as `setDate` does. A day already picked fires nothing. |
| `HasDate.DATE_PICKED` | Post | After every change of the date, including `setDate` and `setDateRange` |
| `CalendarView.BROWSE` | Pre | When an arrow, the cursor, a click outside the month or zooming in would show another month, with that month. Cancel it, or set another month, which is kept inside the range. |
| `CalendarView.BROWSED` | Post | After every change of the month in view, including `setShownMonth` and `setDate` |

### DatePicker

A date in parts the user can type into or step, with a button that opens a calendar in a popup.

```java
DatePicker due = factory.datePicker()
        .setDateRange(LocalDate.now(), null)
        .onEvent(HasDate.DATE_PICKED, event -> task.setDue(event.value()));
```

The date shows as day, month and year in the language's order and with its separator, such as `20.10.2026`, `10/20/2026` or `2026/10/20`. Days and months have two digits and years four. Empty and unfocused, the field shows its placeholder, "Pick a date" by default (`setPlaceholder`); focused and empty, each part shows dashes.

- A click on a part focuses it. Left and Right move between the parts, Home and End go to the first and last, and Tab leaves the field.
- Up and Down step the focused part, wrapping within it, so the 31st steps to the 1st and the month stays. An empty part starts from today's.
- Digits type into the focused part and move on when no further digit could fit: on a month-first field, `1`, `0`, `2`, `0` fill the month and the day. Typing the separator moves on too, and Backspace empties the part.
- What the user types or steps is a draft. It becomes the date on Enter and when the field loses focus, if every part is filled and they make a date the user may pick. A day past the end of the month is cut to its last day, and a year typed with two digits lands in the hundred years around today. On Enter a draft that cannot be taken stays to be fixed; on focus loss it is dropped and the field shows the date again. Escape drops the draft, and is consumed only when there was one, so an Escape with nothing to drop still closes a dialog.
- Delete clears the date while the picker is clearable (`setClearable`, true by default).
- The button, Alt+Down and F4 open the calendar, with the picker's range, filter and first day of the week. It shows the date's month or today's, and takes focus. Picking a day sets the date, closes the calendar and gives focus back to the field. Escape and a press outside close it with no change.

The picker asks for the width of its widest possible text in the current font and language, or of its placeholder, plus its padding and the 12-unit button, so it never resizes as the date changes. A disabled picker takes no focus, draws the text box's disabled face and cannot open.

| Event | Kind | Fires |
|---|---|---|
| `HasDate.PICK_DATE` | Pre | When a draft is taken on Enter or focus loss, a day is picked in the calendar, or Delete clears the date (with `null`). Cancel it, or set another date, which applies as `setDate` does. |
| `HasDate.DATE_PICKED` | Post | After every change of the date, including `setDate` and `setDateRange` |
| `Openable.OPEN` | Pre | When the user opens the calendar. Cancel to keep it closed. |
| `Openable.OPENED` | Post | After the calendar opened, including `open()` |
| `Closable.CLOSED` | Post | Each time the calendar closes. The picker never fires `Closable.CLOSE`. |

The calendar is private to the picker and fires no events of its own.

### TimePicker

A time of day in parts, with columns of hours, minutes, seconds and the halves of the day in a popup.

```java
TimePicker alarm = factory.timePicker(LocalTime.of(7, 30))
        .setMinuteStep(5)
        .onEvent(HasTime.TIME_PICKED, event -> settings.setAlarm(event.value()));
```

The time shows as hour and minute, the second while shown, and on a 12-hour clock the half of the day, such as `14:05` or `02:05 PM`. The field edits as a date picker's does. On the half of the day, the first letter of either name picks it, and Up and Down flip it. Up and Down on the minutes move by the minute step, and an empty part starts from the current time's.

The button, Alt+Down and F4 open a row of lists: hours (12 and 1 to 11, or 0 to 23), minutes in the minute step, seconds while shown, and the two halves on a 12-hour clock. Rows are a line plus 4 tall and at most seven show. A row picked sets that part of the time at once; when the time was empty, the other parts start at zero. Focus stays in the field, whose focused part is the column the keys work on: Left and Right move between columns, Up and Down pick the row above or below at once, and Enter and Escape close the popup.

| Event | Kind | Fires |
|---|---|---|
| `HasTime.PICK_TIME` | Pre | When a draft is taken on Enter or focus loss, a row is picked or stepped in the popup, or Delete clears the time (with `null`). Cancel it, or set another time, which applies as `setTime` does. |
| `HasTime.TIME_PICKED` | Post | After every change of the time, including `setTime` and hiding seconds |
| `Openable.OPEN`, `Openable.OPENED`, `Closable.CLOSED` | | As on `DatePicker` |

### DateTimePicker

One field for a date and a time: the date's parts, a space, then the time's, with the calendar beside the time columns in its popup.

```java
DateTimePicker start = factory.dateTimePicker()
        .setDefaultTime(LocalTime.of(9, 0))
        .onEvent(DateTimePicker.PICKED, event -> meeting.setStart(event.value()));
```

The value is one `LocalDateTime` with one range, `setRange(min, max)`; `dateTime()` reads it, and `date()` and `time()` its parts. `setDateRange` sets the range from the start of the first day to the end of the last. Setting one part of an empty value fills the other, the date with today and the time with `defaultTime()` (midnight unless `setDefaultTime` says otherwise), and clearing either part clears the value. Picking a day in the popup sets the date and leaves the popup open so the time can follow. Focus stays in the field while the popup is open, and Up and Down set the value at once.

A change by the user first asks `PICK` with the whole new value, then `PICK_DATE` if the date changes and `PICK_TIME` if the time does. Canceling any of them stops the change. Rewriting `PICK` changes both parts, and rewriting `PICK_DATE` only the date. Every change then reports `PICKED`, and `DATE_PICKED` and `TIME_PICKED` for the parts that changed.

| Event | Kind | Fires |
|---|---|---|
| `DateTimePicker.PICK` | Pre | When the user would change the value, with the whole new value, or `null` for Delete |
| `HasDate.PICK_DATE`, `HasTime.PICK_TIME` | Pre | After `PICK`, for each part that changes |
| `DateTimePicker.PICKED` | Post | After every change of the value, including `setDateTime`, `setDate`, `setTime` and the ranges |
| `HasDate.DATE_PICKED`, `HasTime.TIME_PICKED` | Post | After `PICKED`, for each part that changed |
| `Openable.OPEN`, `Openable.OPENED`, `Closable.CLOSED` | | As on `DatePicker` |

### Drawing the pickers

The fields draw with the text box's faces, text, placeholder and padding, and the time columns with the list and scroll bar keys, so a theme that styles text boxes and lists styles the pickers too. The calendar needs one key of its own, `calendar.day.selected`, which a date picker needs as well. The other calendar keys are optional:

| Key | Required | Used for |
|---|---|---|
| `calendar.day.selected` | yes | Behind the picked day |
| `calendar.background` | no | Behind the whole calendar. Nothing if missing. |
| `calendar.header.text` | no | The title. Falls back to `TextStyle.DEFAULT`. |
| `calendar.header.hovered` | no | Behind the title or an arrow under the pointer. Falls back to `list.row.hovered`. |
| `calendar.weekday.text` | no | The weekday names. Falls back to `calendar.day.text.outside`. |
| `calendar.day.text` | no | Days of the month in view, months and years. Falls back to `TextStyle.DEFAULT`. |
| `calendar.day.text.outside` | no | Days of the months before and after, and the years outside the decade. Falls back to `calendar.day.text.disabled`. |
| `calendar.day.text.disabled` | no | Days that cannot be picked, and every day while disabled. Falls back to `textbox.text.disabled`. |
| `calendar.day.text.selected` | no | The picked day. Falls back to `calendar.day.text`. |
| `calendar.day.hovered` | no | Behind the day under the pointer. Falls back to `list.row.hovered`. |
| `calendar.day.today` | no | Behind today. Without it, today has a one-unit outline in the day text's color. |
| `calendar.day.marker` | no | The square under a marked day. Falls back to the day text's color. |
| `calendar.arrow` | no | The arrows. Without it they draw in the title's color, and in the disabled text's color at the ends of the range. |
| `calendar.focus` | no | The ring around the cursor's cell while focused. Falls back to `button.focus`. |
| `calendar.padding` | no | Between the calendar's edges and its grid. 4 on every side. |
| `calendar.cell.padding` | no | Around each day number. 3 at the sides and 2 at the top and bottom. |

The pickers' own keys are optional too, and `TimePicker` and `DateTimePicker` read the same ones as `DatePicker`:

| Key | Required | Used for |
|---|---|---|
| `picker.segment.focused` | no | Behind the focused part while the field has focus. Falls back to `textbox.selection`. |
| `picker.button`, `picker.button.hovered`, `picker.button.disabled` | no | The button, under the pointer, and on a disabled picker. They fall back to `spinner.button`, `spinner.button.hovered` and `spinner.button.disabled`, and those to the button faces. |
| `picker.icon.date`, `picker.icon.time` | no | The icon on a date picker's or date and time picker's button, and on a time picker's. Without it the button draws the combo box's down arrow in the text color. |
| `picker.popup` | no | Behind the popup. Falls back to `combobox.popup`. |
| `picker.padding` | no | Between the field's edges and its text. Falls back to `textbox.padding`. |

## Controls that do not exist yet

There is no editable table cell yet. You can build one on `CustomComponent`, or on `CustomComposite` for one made of other components; the [extending guide](extending.md) builds a switch. [Status and limitations](../help/status.md) lists what is planned.
