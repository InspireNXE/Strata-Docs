# File dialogs

An application with an Export button needs to ask where the file goes, and one with an Import button needs to ask which file to read. `ComponentFactory.fileDialogs()` returns the platform's own open, save and folder dialogs. Strata draws no file chooser of its own: the dialogs are the engine's, so they look and behave as the user expects on that system, and an engine without them reports that.

## Asking

`FileDialogs` (`org.inspirenxe.strata.api.file`) has four calls, each taking a `FileDialog` that says what to ask:

| Call | Returns | Asks for |
|---|---|---|
| `open(dialog)` | `CompletableFuture<Optional<Path>>` | One file to open |
| `openMany(dialog)` | `CompletableFuture<List<Path>>` | One or more files to open, in the engine's order |
| `save(dialog)` | `CompletableFuture<Optional<Path>>` | Where to save a file |
| `folder(dialog)` | `CompletableFuture<Optional<Path>>` | A folder |

Each call shows the dialog and returns at once, without waiting for the user. The future completes later, on the UI thread, so its actions may change components. See [Threads](threading.md).

- When the user cancels, an `open`, `save` or `folder` future completes with an empty `Optional`, and an `openMany` future with an empty list.
- When the engine's dialog fails, the future completes exceptionally. Handle that with `exceptionally`, as the example below does.
- Strata writes and reads nothing. The engine may ask the user before it returns a file that exists, but your code does the saving.

```java
--8<-- "files/FileExamples.java:export"
```

## What a dialog asks

`FileDialog` is an immutable record. `FileDialog.of(title)` makes one with the engine's choice of starting place, no suggested name and no filters, and the `with` methods return changed copies:

| Part | Set with | Means |
|---|---|---|
| `title` | `FileDialog.of(title)` | The window title, as `Text`, so a translation key works |
| `start` | `withStart(path)` | The folder or file the dialog starts at. Null in the record means the engine's choice |
| `suggestedName` | `withSuggestedName(name)` | The file name a save dialog starts with. Other dialogs may ignore it |
| `filters` | `withFilter(name, extensions...)` | One more filter, after the others |

A filter has a name and one or more extensions. The first filter is the one chosen when the dialog opens, and with no filters the dialog shows every file. Extensions are written without their dot and are kept in lower case. An extension that is empty, or holds a dot, a slash, a backslash or whitespace, throws `IllegalArgumentException`, and so does a filter with no extension. `Filter.accepts(path)` tests a file name against a filter, ignoring case, if you want to check what comes back. The dialog's filters do not apply to a folder dialog.

```java
--8<-- "files/FileExamples.java:import"
```

```java
--8<-- "files/FileExamples.java:folder"
```

## Engines without dialogs

`isSupported()` is false when the engine shows no file dialogs. Every call then returns a future that is already complete and empty, so code that only reacts to a chosen file does nothing, which is the same as a cancel. Check `isSupported()` when the screen should say so instead, as the export example does, or hide the button.

Which adapters offer dialogs is on their platform pages: [Swing](../platforms/swing.md) does, and the [differences page](../platforms/differences.md) compares them.

## Testing

A test platform without dialogs is not supported, so every future completes empty. To test the code around a dialog, give the platform a `FileDialogSource` that returns futures the test completes. See [Writing an adapter](../platforms/writing-an-adapter.md) for the source's requirements.

## Defaults

| What | Default |
|---|---|
| `FileDialog.of(title)` | No start, no suggested name, no filters |
| Platform without a dialog source | `isSupported()` false, futures complete empty at once |
