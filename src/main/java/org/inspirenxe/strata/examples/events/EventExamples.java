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
package org.inspirenxe.strata.examples.events;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.attribute.value.Ranged;
import org.inspirenxe.strata.api.control.attribute.value.Toggle;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.RadioGroup;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.Slider;
import org.inspirenxe.strata.api.control.field.TextArea;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.event.EventHandler;
import org.inspirenxe.strata.api.event.Subscription;
import org.inspirenxe.strata.api.event.cause.CauseFrame;
import org.inspirenxe.strata.api.event.cause.CauseKeys;
import org.inspirenxe.strata.api.event.cause.Device;
import org.inspirenxe.strata.api.event.cause.Origin;
import org.inspirenxe.strata.api.event.control.ValueRequestEvent;
import org.inspirenxe.strata.api.event.input.InputEvents;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.input.Key;
import org.inspirenxe.strata.api.input.Modifier;
import org.inspirenxe.strata.api.input.ModifierPolicy;
import org.inspirenxe.strata.api.input.Modifiers;
import org.inspirenxe.strata.api.input.MouseButton;
import org.inspirenxe.strata.api.layout.AbsolutePanel;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.screen.ScreenHandle;
import org.inspirenxe.strata.api.theme.Paint;

import java.util.function.Consumer;

/**
 * The events guide's examples.
 */
public final class EventExamples {

    private EventExamples() {
    }

    static Button chain(final ComponentFactory factory, final Runnable save) {
        // --8<-- [start:chain]
        final Button saveButton = factory.button("Save")
                .setEnabled(false)
                .onEvent(Button.PRESSED, event -> save.run())
                .onEvent(InputEvents.POINTER_ENTER, event -> System.out.println("Hovering Save"));
        // --8<-- [end:chain]
        return saveButton;
    }

    static void handler(final TextBox name, final TextArea notes) {
        // --8<-- [start:handler]
        // A handler is an EventHandler of the event interface the type carries, so one can serve several fields
        final EventHandler<ValueRequestEvent<TextInput<?>, String>> noTabs =
                event -> event.setValue(event.value().replace("	", "    "));
        name.onEvent(TextInput.EDIT, noTabs);
        notes.onEvent(TextInput.EDIT, noTabs);
        // --8<-- [end:handler]
    }

    static void kinds(final StackPanel settings, final Button save) {
        // --8<-- [start:kinds]
        // A report with a value: control() is typed as the attribute, value() and previous() as its value
        settings.onEvent(Toggle.TOGGLED, event -> {
            final Toggle<?> toggle = event.control();
            System.out.println(toggle + " went from " + event.previous() + " to " + event.value());
            save.setEnabled(true);
        });
        // --8<-- [end:kinds]
    }

    static void origin(final CheckBox notify, final Label status) {
        // --8<-- [start:origin]
        notify.onEvent(Toggle.TOGGLED, event -> {
            switch (event.origin()) {
                case Origin.User _ -> status.setText("You turned notifications " + (event.value() ? "on" : "off"));
                case Origin.Remote(final Object who) -> status.setText(who + " changed your notifications");
                case Origin.Binding _, Origin.Code _ -> { }     // the model or code set it; nothing to say
            }
        });
        // --8<-- [end:origin]
    }

    static void chainOfCauses(final StackPanel options) {
        // --8<-- [start:chain-of-causes]
        options.onEvent(Toggle.TOGGLED, event -> {
            final boolean byGroup = event.cause().first(RadioGroup.class).isPresent();
            final boolean byKeyboard = event.cause().context(CauseKeys.DEVICE).orElse(null) == Device.KEYBOARD;
            System.out.println(event.target() + " toggled" + (byGroup ? " by its group" : "")
                    + (byKeyboard ? " from the keyboard" : ""));
        });
        // --8<-- [end:chain-of-causes]
    }

    static void remote(final ComponentFactory factory, final TextBox price, final Object buyer, final String newPrice) {
        // --8<-- [start:remote]
        // On the UI thread, applying an update the server sent about another person's purchase
        try (CauseFrame _ = factory.causes().pushFrame().put(CauseKeys.ORIGIN, new Origin.Remote(buyer))) {
            price.setText(newPrice);                    // its EDITED report says the buyer did it
        }
        // --8<-- [end:remote]
    }

    static void veto(final ComponentFactory factory, final TextBox confirmation) {
        // --8<-- [start:veto]
        // A Pre event: canceling it means the button never presses
        final Button delete = factory.button("Delete world")
                .onEvent(Button.PRESS, event -> {
                    if (!confirmation.text().equals("DELETE")) {
                        event.cancel();
                    }
                })
                .onEvent(Button.PRESSED, event -> System.out.println("Deleted"));
        // --8<-- [end:veto]
    }

    static void rewrite(final Slider volume, final double[] cap) {
        // --8<-- [start:rewrite]
        // A Pre event with a value: set another one to change what the user did
        volume.onEvent(Ranged.CHANGE, event -> event.setValue(Math.min(event.value(), cap[0])));
        // --8<-- [end:rewrite]
    }

    static void react(final TextBox name, final Label preview) {
        // --8<-- [start:react]
        // A Post event: the text has already changed, by typing or by setText
        name.onEvent(TextInput.EDITED, event -> preview.setText(event.value().toUpperCase()));
        // --8<-- [end:react]
    }

    static void form(final StackPanel form) {
        // --8<-- [start:form]
        // Every text box in the form, with one handler that runs before the form's handlers at priority 0
        form.onEvent(TextInput.EDIT, -100, event -> {
            if (event.value().length() > 32) {
                event.cancel();
            }
        });
        // --8<-- [end:form]
    }

    static StackPanel delegated(final ComponentFactory factory) {
        // --8<-- [start:delegate]
        final Label selected = factory.label("Pick a color");
        final StackPanel palette = factory.stackPanel().setSpacing(2);
        for (final String color : new String[] {"Red", "Green", "Blue"}) {
            palette.add(factory.label(color));
        }
        // Labels have no click behavior, so their clicks bubble up to the panel.
        // target() is the label that was clicked; current() is the panel running this handler.
        palette.onEvent(InputEvents.CLICK, event -> {
            if (event.target() instanceof final Label label) {
                selected.setText("Picked " + label.text());
                event.consume();
            }
        });
        // --8<-- [end:delegate]
        return palette;
    }

    static void rightClick(final Button button) {
        // --8<-- [start:buttons]
        button.onEvent(InputEvents.CLICK, event -> {
            if (event.button() == MouseButton.RIGHT) {
                System.out.println("Context menu at " + event.x() + ", " + event.y());
                event.consume();
            } else if (event.clicks() == 2) {
                System.out.println("Double click");
            }
        });
        // --8<-- [end:buttons]
    }

    static void shortcut(final AnchorPanel root, final Runnable save) {
        // --8<-- [start:shortcut]
        // Key events go to the focused component first and bubble to the root,
        // so a handler on the root sees every key nothing else consumed.
        root.onEvent(InputEvents.KEY, event -> {
            if (event.is(Key.S, Modifiers.of(Modifier.CONTROL), ModifierPolicy.EXACT)) {
                save.run();
                event.consume();
            }
        });
        // --8<-- [end:shortcut]
    }

    static void modifierClick(final Label link, final Runnable openInNewWindow) {
        // --8<-- [start:modifier-click]
        // ModifierPolicy matches any set of modifiers, so pointer input can ask for them too
        link.onEvent(InputEvents.CLICK, event -> {
            if (event.button() == MouseButton.LEFT
                    && ModifierPolicy.EXACT.matches(Modifiers.of(Modifier.CONTROL), event.modifiers())) {
                openInNewWindow.run();
                event.consume();
            }
        });
        // --8<-- [end:modifier-click]
    }

    static void submit(final TextBox name, final Consumer<String> submit) {
        // --8<-- [start:submit]
        // Enter in the box asks to submit; SUBMITTED fires once nothing refused
        name.onEvent(TextBox.SUBMITTED, event -> submit.accept(event.value()));
        // --8<-- [end:submit]
    }

    static void subscriptions(final Button button) {
        // --8<-- [start:remove]
        // subscribe registers like onEvent and returns a way to remove the handler
        final Subscription hover = button.subscribe(InputEvents.POINTER_ENTER, 0, event -> System.out.println("Hi"));
        hover.close();
        // --8<-- [end:remove]
    }

    static void paint(final Button inbox, final boolean[] unread) {
        // --8<-- [start:paint]
        // A dot over the button's corner while there is something new
        inbox.onEvent(Component.PAINTED, event -> {
            if (unread[0]) {
                final Rect bounds = event.bounds();
                event.painter().fill(new Rect(bounds.right() - 6f, bounds.y() + 2f, 4f, 4f), 0xFFE8A33D);
            }
        });
        // --8<-- [end:paint]
    }

    static void measure(final Label score) {
        // --8<-- [start:measure]
        // Never narrower than 80 units, whatever the text
        score.onEvent(Component.MEASURED, event -> event.setWidth(Math.max(80f, event.width())));
        // --8<-- [end:measure]
    }

    /**
     * A box that can be dragged around an absolute panel.
     *
     * @return the screen
     */
    public static Screen dragging() {
        // --8<-- [start:drag]
        return factory -> {
            final AbsolutePanel board = factory.absolutePanel();
            board.setAnchor(Anchor.FILL);

            // Only containers have a background, so the handle is a panel with a label in it
            final AnchorPanel handle = factory.anchorPanel()
                    .setBackground(Paint.solid(0xFF3A3D43))
                    .add(factory.label("Drag me").setAnchor(Anchor.CENTER));
            handle.setMinSize(60, 20);
            final float[] position = {20f, 20f};
            board.add(handle, position[0], position[1]);

            // Drag events keep going to the component the press started on, wherever the pointer goes
            handle.onEvent(InputEvents.DRAG, event -> {
                position[0] += event.deltaX();
                position[1] += event.deltaY();
                board.move(handle, position[0], position[1]);
                event.consume();
            });
            return board;
        };
        // --8<-- [end:drag]
    }

    /**
     * Keeps the handle from a screen's {@code opened} call to close the screen from a button.
     */
    // --8<-- [start:handle]
    public static final class ConfirmScreen implements Screen {
        private ScreenHandle handle;

        @Override
        public Component<?> build(final ComponentFactory factory) {
            final Button close = factory.button("Close").onEvent(Button.PRESSED, event -> this.handle.close());
            return factory.anchorPanel().add(close.setAnchor(Anchor.CENTER)).setAnchor(Anchor.FILL);
        }

        @Override
        public void opened(final ScreenHandle handle) {
            this.handle = handle;
        }
    }
    // --8<-- [end:handle]
}
