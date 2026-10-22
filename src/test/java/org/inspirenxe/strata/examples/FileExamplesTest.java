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
package org.inspirenxe.strata.examples;

import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.file.FileDialog;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.Themes;
import org.inspirenxe.strata.core.StrataCore;
import org.inspirenxe.strata.examples.adapter.ToyPlatform;
import org.inspirenxe.strata.examples.files.FileExamples;
import org.inspirenxe.strata.spi.Capabilities;
import org.inspirenxe.strata.spi.Clipboard;
import org.inspirenxe.strata.spi.Clock;
import org.inspirenxe.strata.spi.FileDialogSource;
import org.inspirenxe.strata.spi.Platform;
import org.inspirenxe.strata.spi.ScreenHost;
import org.inspirenxe.strata.spi.TextInputSink;
import org.inspirenxe.strata.spi.ThemeSource;
import org.inspirenxe.strata.api.text.TextMetrics;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that the file dialogs guide's examples do what the page says.
 */
class FileExamplesTest {

    /**
     * A platform whose dialogs wait for the test to answer them, from any thread.
     */
    private static final class AskingPlatform implements Platform {
        private final ToyPlatform toy = new ToyPlatform();
        private final FileDialogSource source;

        AskingPlatform(final FileDialogSource source) {
            this.source = source;
        }

        @Override
        public TextMetrics textMetrics() {
            return this.toy.textMetrics();
        }

        @Override
        public Clipboard clipboard() {
            return this.toy.clipboard();
        }

        @Override
        public Clock clock() {
            return this.toy.clock();
        }

        @Override
        public TextInputSink textInput() {
            return this.toy.textInput();
        }

        @Override
        public Capabilities capabilities() {
            return this.toy.capabilities();
        }

        @Override
        public ThemeSource themes() {
            return this.toy.themes();
        }

        @Override
        public Optional<FileDialogSource> fileDialogs() {
            return Optional.ofNullable(this.source);
        }
    }

    /**
     * A source that records the dialogs it was asked for and returns futures the test completes.
     */
    private static final class WaitingSource implements FileDialogSource {
        final List<FileDialog> asked = new ArrayList<>();
        final List<CompletableFuture<Optional<Path>>> saves = new ArrayList<>();
        final List<CompletableFuture<List<Path>>> many = new ArrayList<>();

        @Override
        public CompletableFuture<Optional<Path>> open(final FileDialog dialog) {
            this.asked.add(dialog);
            return new CompletableFuture<>();
        }

        @Override
        public CompletableFuture<List<Path>> openMany(final FileDialog dialog) {
            this.asked.add(dialog);
            final CompletableFuture<List<Path>> future = new CompletableFuture<>();
            this.many.add(future);
            return future;
        }

        @Override
        public CompletableFuture<Optional<Path>> save(final FileDialog dialog) {
            this.asked.add(dialog);
            final CompletableFuture<Optional<Path>> future = new CompletableFuture<>();
            this.saves.add(future);
            return future;
        }

        @Override
        public CompletableFuture<Optional<Path>> folder(final FileDialog dialog) {
            this.asked.add(dialog);
            return new CompletableFuture<>();
        }
    }

    private static ScreenHost host(final Platform platform, final Label[] status) {
        final ScreenHost host = StrataCore.createScreenHost(platform, Themes.flat());
        host.resize(300, 100);
        host.open(factory -> status[0] = factory.label("idle"));
        host.layout();
        return host;
    }

    @Test
    void anExportAsksOnceAndTheAnswerArrivesOnTheUiThreadFromAnyThread() throws Exception {
        final WaitingSource source = new WaitingSource();
        final Label[] status = new Label[1];
        final ScreenHost host = host(new AskingPlatform(source), status);
        final List<Path> exported = new ArrayList<>();
        assertTrue(host.factory().fileDialogs().isSupported());

        FileExamples.export(host.factory(), exported::add, status[0]);
        assertEquals(1, source.asked.size());
        final FileDialog asked = source.asked.getFirst();
        assertEquals("orders.csv", asked.suggestedName());
        assertEquals(List.of("csv", "xlsx"), asked.filters().getFirst().extensions());

        // The engine answers on its own thread; the label changes only once the host runs its next frame
        final Path file = Path.of("out", "orders.csv");
        final Thread engine = new Thread(() -> source.saves.getFirst().complete(Optional.of(file)));
        engine.start();
        engine.join();
        assertEquals("idle", status[0].text().plain());
        host.layout();
        assertEquals(List.of(file), exported);
        assertEquals("Exported to orders.csv", status[0].text().plain());
    }

    @Test
    void aCancelCompletesEmpty() {
        final WaitingSource source = new WaitingSource();
        final Label[] status = new Label[1];
        final ScreenHost host = host(new AskingPlatform(source), status);
        final List<Path> imported = new ArrayList<>();

        FileExamples.importFiles(host.factory(), Path.of("."), imported::add);
        source.many.getFirst().complete(List.of());
        host.layout();
        assertEquals(List.of(), imported);
        assertEquals(Path.of("."), source.asked.getFirst().start());

        FileExamples.export(host.factory(), imported::add, status[0]);
        source.saves.getFirst().complete(Optional.empty());
        host.layout();
        assertEquals("Export canceled", status[0].text().plain());
    }

    @Test
    void aFailureCompletesExceptionally() {
        final WaitingSource source = new WaitingSource();
        final Label[] status = new Label[1];
        final ScreenHost host = host(new AskingPlatform(source), status);

        FileExamples.export(host.factory(), path -> { }, status[0]);
        source.saves.getFirst().completeExceptionally(new IllegalStateException("no display"));
        host.layout();
        assertTrue(status[0].text().plain().startsWith("The dialog failed"));
    }

    @Test
    void withoutDialogsEveryFutureCompletesEmptyAtOnce() {
        final Label[] status = new Label[1];
        final ScreenHost host = host(new AskingPlatform(null), status);
        assertFalse(host.factory().fileDialogs().isSupported());

        final var open = host.factory().fileDialogs().open(FileDialog.of(Text.of("Open")));
        assertTrue(open.isDone());
        assertEquals(Optional.empty(), open.join());
        final var many = host.factory().fileDialogs().openMany(FileDialog.of(Text.of("Open")));
        assertEquals(List.of(), many.join());

        FileExamples.export(host.factory(), path -> { }, status[0]);
        assertEquals("File dialogs are not supported here", status[0].text().plain());
    }
}
