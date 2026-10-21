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
package org.inspirenxe.strata.examples.binding;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.item.ItemSource;
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.attribute.text.HasText;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.attribute.value.Ranged;
import org.inspirenxe.strata.api.control.attribute.value.Toggle;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.state.Source;
import org.inspirenxe.strata.api.state.State;
import org.inspirenxe.strata.api.text.Text;

import java.util.List;
import java.util.function.Consumer;

/**
 * The binding guide's examples.
 */
public final class BindingExamples {

    private BindingExamples() {
    }

    // --8<-- [start:model]
    public static final class SettingsModel {
        public final State<Boolean> music = State.of(true);
        public final State<Double> volume = State.of(0.8);
        public final State<String> name = State.of("Ana");

        public boolean canSave() {
            return !this.name.get().isBlank();
        }
    }
    // --8<-- [end:model]

    /**
     * A settings screen bound to a model.
     */
    public static final class SettingsScreen implements Screen {
        private final SettingsModel model;
        private final Consumer<SettingsModel> save;

        public SettingsScreen(final SettingsModel model, final Consumer<SettingsModel> save) {
            this.model = model;
            this.save = save;
        }

        @Override
        public Component<?> build(final ComponentFactory ui) {
            final SettingsModel model = this.model;
            // --8<-- [start:bind]
            final StackPanel column = ui.stackPanel().setSpacing(4).setPadding(8)
                    .add(ui.checkBox("Music").bind(Toggle.SELECTED, model.music))
                    .add(ui.slider().bind(Ranged.VALUE, model.volume).bind(Component.ENABLED, model.music))
                    .add(ui.textBox().setWidth(160).bind(TextInput.TEXT, model.name))
                    .add(ui.label().bind(HasText.TEXT, () -> Text.of("Hello, " + model.name.get())))
                    .add(ui.button("Save")
                            .bind(Component.ENABLED, model::canSave)
                            .onEvent(Button.PRESSED, event -> this.save.accept(model)));
            // --8<-- [end:bind]
            return column;
        }
    }

    static void derived(final ComponentFactory ui, final State<Integer> price, final State<Integer> balance) {
        // --8<-- [start:derived]
        // Every state the lambda reads is followed, here price and balance
        final Label left = ui.label().bind(HasText.TEXT, () -> Text.of("Left after buying: "
                + (balance.get() - price.get())));

        // map and combine name their sources
        final Label cost = ui.label().bind(HasText.TEXT, price.map(value -> Text.of("Costs " + value)));
        final Button buy = ui.button("Buy").bind(Component.ENABLED,
                State.combine(price, balance, (p, b) -> p <= b));
        // --8<-- [end:derived]
    }

    /**
     * Binds a list's items and its selection, in that order, so the selection is set once the items are there.
     *
     * @param ui the factory
     * @param names the items
     * @param chosen the selected item, or null for none
     * @return the list
     */
    public static ListView<String> list(final ComponentFactory ui, final State<List<String>> names,
                                        final State<String> chosen) {
        // --8<-- [start:list]
        final ListView<String> list = ui.<String>listView()
                .bind(ItemSource.itemsKey(), names)              // replaced whole when the state changes
                .bind(Selection.selectedItem(), chosen);         // two way; null means nothing selected
        // --8<-- [end:list]
        return list;
    }

    static void network(final SettingsModel model, final Runnable receive) {
        // --8<-- [start:threads]
        // Any thread may set a state; the screen shows it on its next frame
        new Thread(() -> {
            receive.run();
            model.volume.set(0.5);
            model.music.set(false);
        }, "network").start();
        // --8<-- [end:threads]
    }

    /**
     * A computed source used as a value elsewhere.
     *
     * @param model the model
     * @return a source of the greeting
     */
    public static Source<Text> greeting(final SettingsModel model) {
        return () -> Text.of("Hello, " + model.name.get());
    }
}
