# Drag and drop

A user drags a listing from one list into another, rows of a list into a new order, or selected text into another field, and drops a file from the desktop on a window. A drag carries a payload, anything under the pointer can accept it, and the user sees the effect it would have.

Drag and drop builds on the plain pointer drag described under [Drags](events.md#drags). A press becomes a drag once the pointer moves more than 4 units. A handler for `DRAG_START` can then start a transfer: from there the drag carries data, and the components it passes over hear about it.

## What moves

A `Transfer` (`org.inspirenxe.strata.api.transfer`) is one payload in one or more formats. It is immutable. The clipboard carries the same type, see [Clipboard](clipboard.md).

```java
Transfer.text("Ana");
Transfer.files(List.of(Path.of("notes.txt")));
Transfer.builder().add(DataFormat.TEXT, "Ana\t12").add(DataFormat.mime("text/csv"), "Ana,12").build();
```

`transfer.get(format)` returns the value as an `Optional`, `has(format)` says whether the transfer holds the format, `formats()` lists them in the order they were added, and `only(test)` returns a transfer with just the formats that pass the test.

A `DataFormat<T>` names a kind of value and its Java type:

| Format | Value | Notes |
|---|---|---|
| `DataFormat.TEXT` | `String` | `text/plain` |
| `DataFormat.STYLED_TEXT` | `Text` | Text with its styles |
| `DataFormat.FILES` | `List<Path>` | Never empty. The transfer holds paths only. |
| `DataFormat.IMAGE` | `PixelBuffer` | See the drawing section of [Extending Strata](extending.md) |
| `DataFormat.mime(type)` | `String` | Any MIME type such as `text/csv`: a type and a subtype joined by one slash, with no parameters. The same type gives the same format, ignoring case, so two libraries that name `text/csv` share one, and `mime("text/plain")` is `TEXT`. |
| `DataFormat.local(name, type)` | any | Never leaves the process. Use it for your own objects. A local format equals only itself. |

A transfer can carry several formats at once, so the target picks the richest one it understands. A text field takes `TEXT`, a spreadsheet takes `text/csv`, and your own list takes the objects. `Transfer.Builder.add` checks the value against the format's type. `supply(format, supplier)` adds a value that is read the first time `get` asks for it. An adapter uses it for a drag from another application, whose data can be read only once it is dropped: `has` answers at once, and `get` reads empty until the value can be read.

## Starting a drag

Call `startTransfer` on the `DragEvent` of `DRAG_START`, from a handler or from the `onDragStart` hook of a component you write:

```java
--8<-- "dragdrop/DragDropExamples.java:tile"
```

`startTransfer(transfer, allowed)` takes the effects the drop may have: `MOVE`, `COPY` or `LINK`. A third argument builds a drag image with the host's factory. The image is built once, laid out 8 units right of and below the pointer, kept inside the host, drawn above everything including the tooltip, and never hit by the pointer. Without an image the drag shows the cursor only.

Rules for `startTransfer`:

- It works only inside `DRAG_START`, and once per drag. Anywhere else, a second time, or while another drag of the host carries a transfer, it throws `IllegalStateException`.
- An empty transfer or an empty set of effects throws `IllegalArgumentException`.
- The current component becomes the source, and the event is consumed, so an ancestor does not start a transfer of its own.
- The press then belongs to the source, so the drag survives the pressed child leaving the tree. A source that leaves the tree ends its drag with no events.

While a drag carries a transfer, the canvas is asked for the `COPY` cursor for a copy, `GRABBING` for a move or a link, and `NO_DROP` while nothing accepts. A canvas that lacks one of these cursors falls back to a simpler kind; the chains are listed in [Defaults and timings](../reference/defaults.md).

## Receiving a drop

Five events carry a transfer. All are `TransferEvent`s, a kind of pointer event, with the position of the pointer, `transfer()`, `allowed()`, `source()` (empty for a drag from another application), `preferred()`, `effect()`, `accept` and `reject`. Each has a hook on `CustomComponent` and an event type in `InputEvents`.

| Event | Hook | Reaches | When |
|---|---|---|---|
| `DRAG_ENTER` | `onDragEnter` | The component that is newly under the pointer. Direct. | The drag moves onto a component |
| `DRAG_OVER` | `onDragOver` | The same component, then its ancestors. Bubbles. | Every move, until a handler accepts or rejects |
| `DRAG_EXIT` | `onDragExit` | The component the drag was over. Direct. | The drag leaves it, ends, or is canceled |
| `DROP` | `onDrop` | The component that accepted last, then its ancestors. Bubbles. | The button is released over an accepting target |
| `TRANSFER_DONE` | `onTransferDone` | The source. Direct. | The drag has ended, after the drop if there was one |

On each move the host fires `DRAG_EXIT` at the component the drag was over and `DRAG_ENTER` at the new one, which is the deepest component under the pointer, or its nearest enabled ancestor when that component is disabled. Then `DRAG_OVER` goes from there up, until a handler decides. The source hears no `DRAG` events while a transfer is under way.

A target says yes in `DRAG_OVER`:

```java
--8<-- "dragdrop/DragDropExamples.java:bin"
```

- `accept(effect)` records the current component as the target with the effect, and consumes the event, so it stops climbing. The effect must be one of `allowed()`, otherwise `IllegalArgumentException`. `accept()` takes `preferred()`.
- `reject()` consumes the event with nothing accepted, so no ancestor takes the drop either. A child can use it to keep a container from accepting over it.
- `preferred()` is `LINK` with Control and Shift held, `COPY` with Control, and `MOVE` with Shift or nothing, each when the source allows it, and otherwise the first allowed of move, copy and link.
- `accept` and `reject` work only in `DRAG_OVER` and `DROP`. In the other three they throw `IllegalStateException`.
- `effect()` is empty during `DRAG_OVER` until something accepts. In `DROP` it is the effect the target accepted on the last move, and it is what the target takes unless a handler changes it with `accept(effect)`. In `TRANSFER_DONE` it is the effect the target took.

A drop is a `DROP` event at the accepted component. A handler there reads the data and applies it. Calling `reject()` in `DROP` ends the drop with no effect. Canceling a `DROP` only skips the default behavior of the components it reaches, as for any input event, so a handler that took the drop itself may consume it without making the source think nothing was taken.

All of these events run in the input frame of the pointer event that carries them, with `Origin.User`, and with the source component on the cause chain (`event.cause()`). A target can therefore tell a reorder inside its own list from a drag out of another component. Because the origin is the user, the requests of built-in controls fire as for any user action.

## After the drop

The source hears `TRANSFER_DONE` with the effect the target took, or no effect. For `MOVE` the source is the one that removes what moved, since the items are its data:

```java
--8<-- "dragdrop/DragDropExamples.java:trade"
```

The release is followed by `DRAG_EXIT` and then `TRANSFER_DONE`, and then the source's own `DRAG_END`. A drag that ends with no drop does the same without `DROP`: Escape, a release over nothing that accepts, and a release the engine lost end the drag with no effect. After Escape the press goes on as a plain drag, so the source hears `DRAG` again and its `DRAG_END`.

## Rows between two lists

Two lists that move rows between them need no new component. Each list sends its selected rows as text, accepts the other's rows, and the source removes them when they moved:

```java
--8<-- "dragdrop/DragDropExamples.java:rowlist"
```

```java
--8<-- "dragdrop/DragDropExamples.java:lists"
```

The list's own rows need no handler: dragged within one list they reorder, as the next section says. The handlers in `trade` above are written for rows that come from the other list. `DRAG_OVER` accepts only a drag whose `source()` is that list, `DROP` adds the lines of the text to this list, and the source's `TRANSFER_DONE` removes them again when the effect was `MOVE` and the other list holds the rows now. Holding Control asks for `COPY`, and the source then keeps its rows.

## What the built-in controls do

### ListView

A list implements `ItemDragging` (`org.inspirenxe.strata.api.control.attribute.item`). Rows are dragged only once the list has a drag transfer:

```java
list.setDragTransfer(rows -> Transfer.text(String.join("\n", rows)));
```

The function gets the dragged rows' items and returns the transfer. Returning null or an empty transfer starts no drag. `setDragTransfer(null)` turns dragging off, which is the default. A left drag that starts on a row drags the selected rows if that row is selected, and that row alone otherwise, with `MOVE` and `COPY` allowed. The drag image is the first item's cell, built again from the cell factory at the row's size and at 0.75 opacity.

In multiple selection a plain press on one of several selected rows selects it alone only when the button comes up and no drag started, so a drag from it carries all of them.

Over itself the list accepts its own rows with `MOVE`. It draws a two-unit line (`ListView.DROP_MARK_HEIGHT`) in the `list.drop` paint at the gap nearest the pointer, and on the drop it asks `ItemDragging.MOVE_ITEMS`:

```java
--8<-- "dragdrop/DragDropExamples.java:reorder"
```

The event is a request with a value, an `ItemMove(indexes, before)`: the indexes of the moved rows in the items as they are now, ascending, and the index of the row they go in front of, or the number of items for the end. `move.apply(items)` returns the reordered list. A handler can cancel the request, or rewrite the move with `setValue`. Unless a handler cancels, the list reorders with `setItems`, so the selection follows the rows by key. A move that names no rows, or changes nothing, is ignored, and a drop where the rows already are asks nothing. There is no report event, as `setItems` has none. If the items are bound, set the moved list into the state in the handler and let the binding find it equal.

Drops from elsewhere, and removing rows after a move to another component, are your `DROP` and `TRANSFER_DONE` handlers.

### TextBox and TextArea

A press inside the selection places the caret only when the button comes up without a drag. A drag from it carries the selected text as `TEXT`, allowing a move and a copy, or a copy only from a read-only box. An enabled, editable box accepts `TEXT`. While the drag is over it the box draws a steady caret at the boundary under the pointer, and the drop inserts the text there as one edit through `TextInput.EDIT`, with its line breaks removed. A move inside one box is one edit over the span it crosses, so it undoes in one step. A drop inside the text being dragged is rejected. After a move to another component the box removes the text it gave away, as an edit.

### PasswordTextBox

A password box neither lends nor takes text.

### Scrolling during a drag

A drag that carries a transfer scrolls the panel or list it is held near. Within 16 units of an edge of a viewport (`ScrollPanel.DRAG_SCROLL_ZONE`), and inside it, a scroll panel scrolls toward that edge by its scroll rate times how deep into the zone the pointer is, at least an eighth of that and at most all of it. A list scrolls by its row step the same way. The innermost control that can still scroll that way does it, so an inner panel at its end lets the outer one scroll.

Scrolling goes on each frame the host's frame rate allows while the pointer stays still, and the drag is moved again over the content that is now under it. It happens at once, without a glide, since a glide each frame would trail the pointer.

Plain drags, such as a slider's, a scroll bar's or a text selection's, do not scroll anything. `ScrollPanel.setDragScroll(false)` and `setDragScroll(false)` on a list, table or tree turn the behavior off.

## Drops from other applications

An adapter reports a drag from outside through the four `dragEntered`, `dragMoved`, `dragExited` and `dropped` methods of `InputSink`. The components see the same events. `source()` is empty, `allowed()` is what the other application allows, and a format may read as empty until the drop, so test with `has` while the pointer moves, and read the value in `DROP`.

A drop with no entry before it, such as files dropped on an application window, allows every effect, and core fires the whole sequence at once: `DRAG_ENTER`, a `DRAG_OVER` whose data can be read, `DROP`, then `DRAG_EXIT`. A target that wants desktop files accepts `DataFormat.FILES`:

```java
panel.onEvent(InputEvents.DRAG_OVER, event -> {
    if (event.transfer().has(DataFormat.FILES)) {
        event.accept(DropEffect.COPY);
    }
});
panel.onEvent(InputEvents.DROP, event ->
        event.transfer().get(DataFormat.FILES).ifPresent(files -> files.forEach(importer::add)));
```

Whether an engine can deliver drops at all, and which formats, is in [Platform differences](../platforms/differences.md). Drags from a host out to other applications are not supported.

## Finding what is under a point

`ComponentFactory.componentAt(x, y)` returns the deepest component the pointer would hit at a point of the host, as an `Optional`. It lays out first if it needs to. Overlays are searched from the top, where a dialog hides everything below it and its backdrop finds nothing. Then the screen's content is searched, skipping hidden components and finding disabled ones. The tooltip and the drag image are never found. It checks the UI thread, see [Threading](threading.md).

A drop handler on a container can use it to find the child that is under the pointer when it only asks for the container's events.

## Testing

`ScreenTester` has two helpers, described in [Testing](testing.md):

```java
tester.dragTo(source, target);                        // press the source, move past the threshold, release over the target
Optional<DropEffect> taken = tester.drop(target, Transfer.text("hello"));   // as if from another application
```

`dragTo` presses the center of the source and moves to the center of the target in steps, so the whole event sequence runs. A source that starts a transfer, such as a list whose rows drag, drops it on the target as for a person. `drop` enters at the target's center allowing every effect, drops there, and returns the effect the target took, or empty when nothing took it. The examples on this page run as tests: the two lists, the tile and the bin move and take what they should.

## Defaults

| Setting | Value |
|---|---|
| Drag threshold | 4 units on either axis |
| Drag image offset | 8 units right of and below the pointer |
| List drop line | 2 units high, `list.drop` paint, `#FF8FB4F0` in the flat theme |
| Edge scrolling zone | 16 units, one step a frame |
| Lists and text boxes | allow `MOVE` and `COPY`; a read-only text box allows `COPY` only |
| List dragging | off until `setDragTransfer` |
| Edge scrolling | on |

See [Defaults and timings](../reference/defaults.md) for the full list.
