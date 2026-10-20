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
package org.inspirenxe.strata.examples.controls;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.item.SingleSelection;
import org.inspirenxe.strata.api.control.attribute.popup.Closable;
import org.inspirenxe.strata.api.control.attribute.popup.Openable;
import org.inspirenxe.strata.api.control.attribute.text.TextEntry;
import org.inspirenxe.strata.api.control.attribute.value.HasDate;
import org.inspirenxe.strata.api.control.attribute.value.HasTime;
import org.inspirenxe.strata.api.control.attribute.value.Ranged;
import org.inspirenxe.strata.api.control.attribute.value.Toggle;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.DropDownButton;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.display.Avatar;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.control.display.InfoBar;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.ColorPicker;
import org.inspirenxe.strata.api.control.field.DatePicker;
import org.inspirenxe.strata.api.control.field.PasswordTextBox;
import org.inspirenxe.strata.api.control.field.Slider;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.field.TimePicker;
import org.inspirenxe.strata.api.control.menu.Flyout;
import org.inspirenxe.strata.api.control.menu.Menu;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.StyleRun;
import org.inspirenxe.strata.api.text.TextHighlighter;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.api.theme.SpriteId;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The controls guide's examples for images, avatars, info bars, checkboxes, sliders, password boxes, rows of controls
 * in a list and date and time pickers.
 */
public final class ControlExamples {

    private ControlExamples() {
    }

    /**
     * Settings a game might keep.
     */
    public static final class Settings {
        private boolean music = true;
        private double volume = 80;
        private int distance = 12;

        public boolean music() {
            return this.music;
        }

        public void setMusic(final boolean music) {
            this.music = music;
        }

        public double volume() {
            return this.volume;
        }

        public void setVolume(final double volume) {
            this.volume = volume;
        }

        public int distance() {
            return this.distance;
        }

        public void setDistance(final int distance) {
            this.distance = distance;
        }
    }

    static Image image(final ComponentFactory factory) {
        // --8<-- [start:image]
        // assets/examplemod/sprites/icon/star.png, drawn at 16 by 16 units
        final Image star = factory.image(SpriteId.of("examplemod", "icon/star"));

        // The same sprite as a large picture that keeps its shape however it is stretched
        final Image banner = factory.image(SpriteId.of("examplemod", "icon/star"))
                .setImageSize(16, 16)
                .setScaling(Image.Scaling.FIT);
        banner.setAnchor(Anchor.FILL);

        // Tints multiply the image: half transparent here
        final Image faded = factory.image(SpriteId.of("examplemod", "icon/star")).setTint(0x80FFFFFF);
        // --8<-- [end:image]
        return star;
    }

    /**
     * A seller's line: their avatar, with a badge for their unread messages, beside their name.
     *
     * @param factory the factory
     * @param seller the seller's name
     * @param unread how many of the seller's messages are unread
     * @return the line: the avatar and a label
     */
    public static StackPanel sellerLine(final ComponentFactory factory, final String seller, final int unread) {
        // --8<-- [start:avatar]
        // No picture, so the avatar shows initials from the name: Ana Lima shows AL
        final Avatar avatar = factory.avatar()
                .setName(seller)
                .setAvatarSize(32)
                .setBadge(unread)
                .setTooltip(unread + " unread");
        final StackPanel line = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(6)
                .add(avatar)
                .add(factory.label(seller).setAnchor(Anchor.MIDDLE_LEFT));
        // --8<-- [end:avatar]
        return line;
    }

    /**
     * An error bar for a save that failed, with a button that tries again, which stays open while a save is running.
     *
     * @param factory the factory
     * @param saving whether a save is running
     * @param save starts a save
     * @return the bar
     */
    public static InfoBar saveFailed(final ComponentFactory factory, final BooleanSupplier saving, final Runnable save) {
        // --8<-- [start:infobar]
        final InfoBar failed = factory.infoBar(InfoBar.Severity.ERROR, "Not saved", "The server did not answer.")
                .setAction("Try again", save);
        // The close button asks first, so a handler can keep the bar open
        failed.onEvent(Closable.CLOSE, event -> {
            if (saving.getAsBoolean()) {
                event.cancel();
            }
        });
        // --8<-- [end:infobar]
        return failed;
    }

    /**
     * A booking row: a day that is not a Sunday within the next two months, a time in quarter hours, and a Book button
     * that is enabled once both are set.
     *
     * @param factory the factory
     * @param book takes the day and time when Book is pressed
     * @return the row: the date picker, the time picker and the button
     */
    public static StackPanel booking(final ComponentFactory factory, final Consumer<LocalDateTime> book) {
        // --8<-- [start:booking]
        final DatePicker day = factory.datePicker()
                .setDateRange(LocalDate.now(), LocalDate.now().plusMonths(2))
                .setDateFilter(date -> date.getDayOfWeek() != DayOfWeek.SUNDAY);
        final TimePicker time = factory.timePicker(LocalTime.of(9, 0)).setMinuteStep(15);
        final Button bookButton = factory.button("Book").setEnabled(false);
        final StackPanel row = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(4)
                .add(day).add(time).add(bookButton);
        // One handler on the row hears both pickers, from the user and from code
        row.onEvent(HasDate.DATE_PICKED, event -> bookButton.setEnabled(day.date().isPresent() && time.time().isPresent()));
        row.onEvent(HasTime.TIME_PICKED, event -> bookButton.setEnabled(day.date().isPresent() && time.time().isPresent()));
        bookButton.onEvent(Button.PRESSED, event -> book.accept(day.date().orElseThrow().atTime(time.time().orElseThrow())));
        // --8<-- [end:booking]
        return row;
    }

    /**
     * A drop-down button that opens a sort menu, and refuses to open while the list is loading.
     *
     * @param factory the factory
     * @param loading whether the list is loading
     * @param sortBy takes the order chosen: {@code name}, {@code price}, {@code unsold} or {@code all}
     * @return the button
     */
    public static DropDownButton sortButton(final ComponentFactory factory, final BooleanSupplier loading,
                                            final Consumer<String> sortBy) {
        // --8<-- [start:dropdown]
        final Menu order = factory.menu()
                .addItem("By name", () -> sortBy.accept("name"))
                .addItem("By price", () -> sortBy.accept("price"))
                .addSeparator()
                .addCheckItem("Hide sold", true, hidden -> sortBy.accept(hidden ? "unsold" : "all"));
        final DropDownButton sort = factory.dropDownButton("Sort", order).setTooltip("Change the order");
        // The button asks before it opens, so a handler can keep the menu closed
        sort.onEvent(Openable.OPEN, event -> {
            if (loading.getAsBoolean()) {
                event.cancel();
            }
        });
        // --8<-- [end:dropdown]
        return sort;
    }

    /**
     * A button that opens a color picker in a flyout below itself.
     *
     * @param factory the factory
     * @param tint takes each color picked
     * @return the button
     */
    public static Button tintButton(final ComponentFactory factory, final IntConsumer tint) {
        // --8<-- [start:flyout]
        final ColorPicker picker = factory.colorPicker()
                .onEvent(ColorPicker.PICKED, event -> tint.accept(event.value()));
        final Flyout flyout = factory.flyout(picker);
        final Button button = factory.button("Tint");
        button.onEvent(Button.PRESSED, event -> flyout.open(button.bounds()));
        // --8<-- [end:flyout]
        return button;
    }

    static CheckBox checkBox(final ComponentFactory factory, final Settings settings) {
        // --8<-- [start:checkbox]
        final CheckBox music = factory.checkBox("Music")
                .setSelected(settings.music())
                .onEvent(Toggle.TOGGLED, event -> settings.setMusic(event.value()));
        // --8<-- [end:checkbox]
        return music;
    }

    static Slider slider(final ComponentFactory factory, final Settings settings) {
        // --8<-- [start:slider]
        final Slider volume = factory.slider()
                .setRange(0, 100)
                .setStep(1)
                .setValue(settings.volume())
                .setLabel(value -> "Volume: " + (int) value + "%")
                .onEvent(Ranged.CHANGED, event -> settings.setVolume(event.value()));
        volume.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
        // --8<-- [end:slider]
        return volume;
    }

    /**
     * A text box that marks every mention of a person, as a highlighter.
     *
     * @param factory the factory
     * @return the box
     */
    static TextBox mentions(final ComponentFactory factory) {
        // --8<-- [start:highlighter]
        // Every @name is drawn bold and blue, keeping the rest of the box's style from the theme
        final TextHighlighter mentions = (text, base) -> {
            final List<StyleRun> runs = new ArrayList<>();
            final Matcher matcher = Pattern.compile("@\\w+").matcher(text);
            while (matcher.find()) {
                runs.add(new StyleRun(matcher.start(), matcher.end(), base.withColor(0xFF61AFEF).withBold(true)));
            }
            return runs;
        };
        final TextBox message = factory.textBox()
                .setPlaceholder("Say something")
                .setHighlighter(mentions);
        // --8<-- [end:highlighter]
        return message;
    }

    /**
     * A form that sets a password: a box, a confirm box, a status line and a Save button that is enabled while both
     * boxes hold the same text.
     *
     * @param factory the factory
     * @param save takes the new password's chars and returns whether it was saved; the chars are zeroed after it
     *     returns
     * @return the form
     */
    public static StackPanel passwordForm(final ComponentFactory factory, final Predicate<char[]> save) {
        // --8<-- [start:password]
        final PasswordTextBox password = factory.passwordTextBox().setPlaceholder("Password").setMaxLength(64);
        final PasswordTextBox confirm = factory.passwordTextBox()
                .setPlaceholder("Repeat password")
                .setRevealable(false);
        final Label status = factory.label("");
        final Button saveButton = factory.button("Save").setEnabled(false);
        final StackPanel form = factory.stackPanel().setSpacing(4)
                .add(password).add(confirm).add(status).add(saveButton);
        // MODIFIED carries lengths, never text, so one handler can watch every field of the form
        form.onEvent(TextEntry.MODIFIED, event ->
                saveButton.setEnabled(!password.isEmpty() && password.matches(confirm)));
        // The reader gets a copy of the chars, which is zeroed as soon as it returns
        saveButton.onEvent(Button.PRESSED, event ->
                status.setText(password.withPassword(save::test) ? "Saved" : "Not saved"));
        // --8<-- [end:password]
        return form;
    }

    /**
     * A box for a six-digit PIN.
     *
     * @param factory the factory
     * @return the box
     */
    static PasswordTextBox pin(final ComponentFactory factory) {
        // --8<-- [start:pin]
        // Only digits, at most six; a paste with anything else, or one that would not fit, is refused whole
        final PasswordTextBox pin = factory.passwordTextBox()
                .setMaxLength(6)
                .setCodePointFilter(Character::isDigit)
                .setMaskCodePoint('\u2022')
                .setPlaceholder("PIN");
        // --8<-- [end:pin]
        return pin;
    }

    static Slider integerSlider(final ComponentFactory factory, final Settings settings) {
        // --8<-- [start:integers]
        // A step of 1 keeps the value whole; cast it when storing
        final Slider distance = factory.slider()
                .setRange(2, 32)
                .setStep(1)
                .setValue(settings.distance())
                .setLabel(value -> "Render distance: " + (int) value + " chunks")
                .onEvent(Ranged.CHANGED, event -> settings.setDistance(event.value().intValue()));
        // --8<-- [end:integers]
        return distance;
    }

    static ListView<String> optionRows(final ComponentFactory factory, final Settings settings) {
        // --8<-- [start:optionRows]
        final ListView<String> options = factory.<String>listView(name -> {
                    // Each control starts from the model and writes back to it
                    final Component<?> control = switch (name) {
                        case "Music" -> factory.toggleSwitch()
                                .setSelected(settings.music())
                                .onEvent(Toggle.TOGGLED, event -> settings.setMusic(event.value()));
                        case "Volume" -> factory.slider()
                                .setRange(0, 100)
                                .setStep(1)
                                .setValue(settings.volume())
                                .setLabel(value -> (int) value + "%")
                                .onEvent(Ranged.CHANGED, event -> settings.setVolume(event.value()))
                                .setWidth(100);
                        default -> factory.button("Reset");
                    };
                    return factory.stackPanel(Orientation.HORIZONTAL)
                            .add(factory.label(name).setAnchor(Anchor.MIDDLE_LEFT))
                            .add(factory.spacer(), 1)
                            .add(control.setAnchor(Anchor.MIDDLE_LEFT))
                            .setAnchor(Anchor.FILL);
                })
                .setRowHeight(24)
                .setItems(List.of("Music", "Volume", "Defaults"))
                // The rows hold controls, so there is nothing to select
                .onEvent(SingleSelection.SELECT, event -> event.cancel());

        // One handler around the list hears every row's button
        options.onEvent(Button.PRESSED, event -> {
            settings.setMusic(true);
            settings.setVolume(80);
            options.refresh();
        });
        // --8<-- [end:optionRows]
        return options;
    }

    /**
     * An options list whose rows hold a switch, a slider and a button.
     *
     * @return the screen
     */
    public static Screen optionRows() {
        final Settings settings = new Settings();
        return factory -> {
            final ListView<String> options = optionRows(factory, settings);
            options.setSize(240, 74);
            options.setAnchor(Anchor.CENTER);
            return factory.anchorPanel().add(options).setAnchor(Anchor.FILL);
        };
    }

    /**
     * A settings panel with every new control.
     *
     * @return the screen
     */
    public static Screen controls() {
        final Settings settings = new Settings();
        return factory -> {
            final StackPanel heading = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(4)
                    .add(factory.image(SpriteId.of("examplemod", "icon/star")).setImageSize(9, 9)
                            .setAnchor(Anchor.MIDDLE_LEFT))
                    .add(factory.label("Audio and video").setAnchor(Anchor.MIDDLE_LEFT));

            final Label status = factory.label("");
            final CheckBox music = checkBox(factory, settings);
            final CheckBox subtitles = factory.checkBox("Subtitles");
            final CheckBox locked = factory.checkBox("Locked option").setSelected(true).setEnabled(false);
            music.onEvent(Toggle.TOGGLED, event -> status.setText(event.value() ? "Music on" : "Music off"));

            final Slider distance = integerSlider(factory, settings);
            distance.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);

            final StackPanel column = factory.stackPanel().setSpacing(6).setPadding(8)
                    .add(heading)
                    .add(music)
                    .add(subtitles)
                    .add(locked)
                    .add(slider(factory, settings))
                    .add(distance)
                    .add(status);
            column.setWidth(220);
            column.setBackground(Paint.solid(0xFF2B2D31));
            return factory.anchorPanel().add(column.setAnchor(Anchor.CENTER)).setAnchor(Anchor.FILL);
        };
    }
}
