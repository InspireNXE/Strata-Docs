# Clipboard

The clipboard holds a `Transfer`, the same payload type that [drag and drop](drag-and-drop.md) carries. A table can therefore copy rows as plain text for a spreadsheet and as objects for a paste inside your application, in one call.

## Reading and writing

Two calls on `ComponentFactory` reach the clipboard of the host's platform, and the same two exist on `Environment` for a component you write (`this.environment().clipboard()`):

| Call | Does |
|---|---|
| `clipboard()` | Returns the clipboard's content in every format it holds, as an `Optional<Transfer>`. Empty when the clipboard holds nothing or the engine has no clipboard. |
| `setClipboard(transfer)` | Puts a transfer on the clipboard. |

Plain text has shorter forms, `Environment.clipboardText()` and `setClipboardText(text)`. They go through the same path as the transfer calls. Text boxes and text areas copy, cut and paste through them, and a password box only pastes.

```java
--8<-- "clipboard/ClipboardExamples.java:formats"
```

```java
--8<-- "clipboard/ClipboardExamples.java:copy"
```

```java
--8<-- "clipboard/ClipboardExamples.java:paste"
```

A transfer is built from formats, the same ones drag and drop uses: `TEXT`, `STYLED_TEXT`, `FILES`, `IMAGE`, MIME formats made with `DataFormat.mime(type)`, and local formats made with `DataFormat.local(name, type)`. See [What moves](drag-and-drop.md#what-moves).

## What leaves the process

The engine's clipboard is shared with other programs, and only some formats can go there. Core hands the engine every format except the local ones, in whatever the engine can hold, which is at least the text. What an engine does with the other formats is in [Platform differences](../platforms/differences.md). Styled text, for instance, may go out as its plain text only.

Local formats never leave the process. Core keeps, per platform, the last transfer you set together with the text the engine's clipboard returned right after the write. While the engine's clipboard still returns that text, `clipboard()` gives back the whole transfer, local formats included. When another program writes to the clipboard in the meantime, the text no longer matches, core reads the engine and forgets its own transfer. So a paste inside your application gets the objects, a paste in a text editor gets the text, and nothing stale comes back after another program copied.

Core compares with what the engine returns after the write, not with what it sent. A transfer without text, and an engine that changes the text it stores, work the same way.

## Reading what came from elsewhere

Another program's clipboard shows up as a transfer with the formats the engine can read: usually `TEXT`, perhaps `FILES`, `IMAGE` and some MIME formats. Check with `has` before you read, and fall back to text, as the paste method above does.

## Testing

The test kit's platform keeps the clipboard in memory, and `tester.factory().clipboard()` reads it. See [Testing](testing.md). The examples above run as tests: they copy listings, paste them back as objects, and read lines of text that another program put there.
