/*
 * Copyright (c) InspireNXE <https://github.com/InspireNXE>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.inspirenxe.strata.examples.files;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.file.FileDialog;
import org.inspirenxe.strata.api.file.FileDialogs;
import org.inspirenxe.strata.api.text.Text;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * The file dialogs guide's examples.
 */
public final class FileExamples {

    private FileExamples() {
    }

    // --8<-- [start:export]
    /**
     * Asks where to export the orders, and says what happened.
     */
    public static void export(final ComponentFactory ui, final Consumer<Path> exporter, final Label status) {
        final FileDialogs dialogs = ui.fileDialogs();
        if (!dialogs.isSupported()) {
            status.setText(Text.of("File dialogs are not supported here"));
            return;
        }
        dialogs.save(FileDialog.of(Text.of("Export orders"))
                        .withSuggestedName("orders.csv")
                        .withFilter(Text.of("Spreadsheets"), "csv", "xlsx"))
                // The future completes on the UI thread, so the actions may change components
                .thenAccept(chosen -> chosen.ifPresentOrElse(file -> {
                    exporter.accept(file);
                    status.setText(Text.of("Exported to " + file.getFileName()));
                }, () -> status.setText(Text.of("Export canceled"))))
                .exceptionally(failure -> {
                    status.setText(Text.of("The dialog failed: " + failure.getMessage()));
                    return null;
                });
    }
    // --8<-- [end:export]

    // --8<-- [start:import]
    /**
     * Asks for one or more files to import, starting in the folder the user imported from last time.
     */
    public static void importFiles(final ComponentFactory ui, final Path lastFolder, final Consumer<Path> importer) {
        ui.fileDialogs()
                .openMany(FileDialog.of(Text.translatable("orders.import.title"))
                        .withStart(lastFolder)
                        .withFilter(Text.of("Orders"), "csv"))
                .thenAccept(files -> files.forEach(importer));        // an empty list when canceled
    }
    // --8<-- [end:import]

    // --8<-- [start:folder]
    /**
     * Asks for a folder. The dialog's filters would not apply.
     */
    public static void chooseFolder(final ComponentFactory ui, final Consumer<Path> chosen) {
        ui.fileDialogs().folder(FileDialog.of(Text.of("Save reports in")))
                .thenAccept(folder -> folder.ifPresent(chosen));
    }
    // --8<-- [end:folder]
}
