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

import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.DropDownButton;
import org.inspirenxe.strata.api.control.display.Avatar;
import org.inspirenxe.strata.api.control.display.InfoBar;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.pane.NavigationView;
import org.inspirenxe.strata.api.control.field.ColorPicker;
import org.inspirenxe.strata.api.control.field.DatePicker;
import org.inspirenxe.strata.api.control.field.PasswordTextBox;
import org.inspirenxe.strata.api.control.field.Slider;
import org.inspirenxe.strata.api.control.menu.Flyout;
import org.inspirenxe.strata.api.input.Key;
import org.inspirenxe.strata.api.input.KeyAction;
import org.inspirenxe.strata.api.input.Modifiers;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.event.Subscription;
import org.inspirenxe.strata.api.overlay.Overlay;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.api.theme.Theme;
import org.inspirenxe.strata.api.theme.ThemeException;
import org.inspirenxe.strata.api.theme.ThemeId;
import org.inspirenxe.strata.api.theme.ThemeRef;
import org.inspirenxe.strata.api.theme.Themes;
import org.inspirenxe.strata.core.StrataCore;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.state.State;
import org.inspirenxe.strata.examples.adapter.ToyAdapter;
import org.inspirenxe.strata.examples.binding.BindingExamples;
import org.inspirenxe.strata.examples.adapter.ToyPlatform;
import org.inspirenxe.strata.examples.controls.ControlExamples;
import org.inspirenxe.strata.examples.events.EventExamples;
import org.inspirenxe.strata.examples.extending.ExtendingExamples;
import org.inspirenxe.strata.examples.layout.LayoutExamples;
import org.inspirenxe.strata.examples.quickstart.SettingsScreen;
import org.inspirenxe.strata.examples.screens.ScreenExamples;
import org.inspirenxe.strata.examples.theming.ThemeExamples;
import org.inspirenxe.strata.swing.SwingSnapshot;
import org.inspirenxe.strata.spi.ScreenHost;
import org.inspirenxe.strata.swing.SwingThemeSource;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that the documentation's examples do what the pages say.
 */
class ExamplesTest {

    @Test
    void everyExampleScreenOpensWithItsTheme() {
        final Theme theme = ExtendingExamples.checkBoxTheme();
        for (final var screen : List.of(new SettingsScreen(), LayoutExamples.anchorPanel(), LayoutExamples.stackPanel(),
                LayoutExamples.gridPanel(), LayoutExamples.weightedGrid(), LayoutExamples.spacer(),
                LayoutExamples.absolutePanel(), LayoutExamples.marginAndPadding(), LayoutExamples.wrapping(),
                LayoutExamples.sizes(), EventExamples.dragging(), new EventExamples.ConfirmScreen(),
                ExtendingExamples.customComponents(), new ScreenExamples.ShoppingList(), new ScreenExamples.TitleScreen(),
                ThemeExamples.sampler(0xFF2B2D31), ControlExamples.controls(), LayoutExamples.frame(),
                LayoutExamples.spread(), ControlExamples.optionRows())) {
            SwingSnapshot.render(screen, theme, 320, 200, 1f);
        }
        SwingSnapshot.render(ThemeExamples.sampler(0xFF2B2D31), ThemeExamples.derived(), 320, 200, 1f);
        SwingSnapshot.render(ThemeExamples.sampler(0xFF2B2D31), ThemeExamples.fromScratch(), 320, 200, 1f);
    }

    @Test
    void anEmptyThemeListsEveryMissingKey() {
        final ThemeException e = assertThrows(ThemeException.class,
                () -> SwingSnapshot.render(new SettingsScreen(), Theme.builder("empty").build(), 320, 200, 1f));
        assertTrue(e.missingKeys().containsAll(List.of("button.normal", "button.hovered", "button.pressed",
                "button.disabled", "textbox.normal", "textbox.focused", "textbox.disabled")));
    }

    @Test
    void overridesFromCodeReachEveryRefUntilClosed() {
        final SwingThemeSource source = new SwingThemeSource(ExamplesTest.class.getClassLoader());
        final List<Subscription> subscriptions = ThemeExamples.fromCode();
        try {
            final Theme flat = source.resolve(ThemeRef.of(Themes.FLAT));
            assertEquals(Paint.solid(0xFF2E7D32), flat.paint(Button.STYLE_NORMAL).orElseThrow());
            assertEquals(Optional.of("code"), flat.layer(Button.STYLE_NORMAL));
            assertEquals(Optional.of("base"), flat.layer(Button.STYLE_PRESSED));

            final Theme shop = source.resolve(ThemeRef.of(ThemeId.of("examplemod", "shop")));
            assertEquals(Paint.solid(0xF0101010), shop.paint(Window.STYLE_BACKGROUND).orElseThrow());
            assertEquals(Paint.solid(0xFF2E7D32), shop.paint(Button.STYLE_NORMAL).orElseThrow());
        } finally {
            subscriptions.forEach(Subscription::close);
        }
        assertEquals(Themes.flat().paint(Button.STYLE_NORMAL),
                source.resolve(ThemeRef.of(Themes.FLAT)).paint(Button.STYLE_NORMAL));
        assertThrows(ThemeException.class, () -> source.resolve(ThemeRef.of(ThemeId.of("examplemod", "shop"))));
    }

    @Test
    void theSwitchNeedsItsKeys() {
        final ThemeException e = assertThrows(ThemeException.class,
                () -> SwingSnapshot.render(ExtendingExamples.customComponents(), Themes.flat(), 320, 200, 1f));
        assertEquals(java.util.Set.of("examplemod.switch.track", "examplemod.switch.track.on", "examplemod.switch.knob"),
                e.missingKeys());
    }

    @Test
    void theBookingRowEnablesBookOnceADayAndATimeAreSet() {
        final ScreenHost host = StrataCore.createScreenHost(new ToyPlatform(), Themes.flat());
        host.resize(400, 200);
        final List<LocalDateTime> booked = new ArrayList<>();
        final StackPanel[] row = new StackPanel[1];
        host.open(factory -> row[0] = ControlExamples.booking(factory, booked::add));
        host.layout();
        final DatePicker day = (DatePicker) row[0].children().get(0);
        final Button book = (Button) row[0].children().get(2);
        assertFalse(book.isEnabled());
        final LocalDate tomorrow = LocalDate.now().plusDays(1);
        day.setDate(tomorrow);
        assertTrue(book.isEnabled());
        book.requestFocus();
        host.key(Key.ENTER, KeyAction.PRESS, Modifiers.NONE);
        assertEquals(List.of(tomorrow.atTime(9, 0)), booked);
    }

    @Test
    void theSortButtonOpensItsMenuUnlessTheListIsLoading() {
        final ScreenHost host = StrataCore.createScreenHost(new ToyPlatform(), Themes.flat());
        host.resize(400, 200);
        final boolean[] loading = {true};
        final List<String> sorted = new ArrayList<>();
        final DropDownButton[] sort = new DropDownButton[1];
        host.open(factory -> sort[0] = ControlExamples.sortButton(factory, () -> loading[0], sorted::add));
        host.layout();
        sort[0].requestFocus();
        host.key(Key.ENTER, KeyAction.PRESS, Modifiers.NONE);
        assertFalse(sort[0].isOpen());
        loading[0] = false;
        host.key(Key.ENTER, KeyAction.PRESS, Modifiers.NONE);
        assertTrue(sort[0].isOpen());
        // A key opens the menu on its first item, and Enter chooses it
        host.key(Key.ENTER, KeyAction.PRESS, Modifiers.NONE);
        assertEquals(List.of("name"), sorted);
        assertFalse(sort[0].isOpen());
    }

    @Test
    void theSellerLineShowsInitialsAndTheUnreadCount() {
        final ScreenHost host = StrataCore.createScreenHost(new ToyPlatform(), Themes.flat());
        host.resize(300, 100);
        final StackPanel[] line = new StackPanel[1];
        host.open(factory -> line[0] = ControlExamples.sellerLine(factory, "Ana Lima", 3));
        host.layout();
        final Avatar avatar = (Avatar) line[0].children().getFirst();
        assertEquals("AL", avatar.initials());
        assertEquals(3, avatar.badge());
        assertEquals(32f, avatar.bounds().width());
        assertEquals(32f, avatar.bounds().height());
    }

    @Test
    void theSaveBarTriesAgainAndStaysOpenWhileSaving() {
        final ScreenHost host = StrataCore.createScreenHost(new ToyPlatform(), Themes.flat());
        host.resize(400, 100);
        final boolean[] saving = {true};
        final List<String> saves = new ArrayList<>();
        final InfoBar[] bar = new InfoBar[1];
        host.open(factory -> bar[0] = ControlExamples.saveFailed(factory, () -> saving[0], () -> saves.add("save")));
        host.layout();
        ((Button) bar[0].action().orElseThrow()).press();
        assertEquals(List.of("save"), saves);
        // The close button is the only tab stop after the action
        host.key(Key.TAB, KeyAction.PRESS, Modifiers.NONE);
        host.key(Key.TAB, KeyAction.PRESS, Modifiers.NONE);
        host.key(Key.ENTER, KeyAction.PRESS, Modifiers.NONE);
        assertTrue(bar[0].isOpen());
        saving[0] = false;
        host.key(Key.ENTER, KeyAction.PRESS, Modifiers.NONE);
        assertFalse(bar[0].isOpen());
    }

    @Test
    void theSettingsNavigationShowsPagesAndSignsOut() {
        final ScreenHost host = StrataCore.createScreenHost(new ToyPlatform(), Themes.flat());
        host.resize(400, 300);
        final List<String> signedOut = new ArrayList<>();
        final NavigationView[] view = new NavigationView[1];
        host.open(factory -> view[0] = ControlExamples.settings(factory, () -> signedOut.add("signed out")));
        host.layout();
        assertEquals(0, view[0].selectedIndex());
        assertEquals(NavigationView.DisplayMode.EXPANDED, view[0].shownMode());
        // The keyboard reaches Sign out, the last item, and Enter activates it without selecting it
        view[0].requestFocus();
        host.key(Key.END, KeyAction.PRESS, Modifiers.NONE);
        host.key(Key.ENTER, KeyAction.PRESS, Modifiers.NONE);
        assertEquals(List.of("signed out"), signedOut);
        assertEquals(0, view[0].selectedIndex());
    }

    @Test
    void theTintButtonOpensAColorPickerBelowItself() {
        final ScreenHost host = StrataCore.createScreenHost(new ToyPlatform(), Themes.flat());
        host.resize(400, 300);
        final Button[] button = new Button[1];
        host.open(factory -> button[0] = ControlExamples.tintButton(factory, color -> { }));
        host.layout();
        button[0].requestFocus();
        host.key(Key.ENTER, KeyAction.PRESS, Modifiers.NONE);
        final Overlay flyout = host.factory().overlays().open().getFirst();
        assertTrue(flyout.content() instanceof Flyout);
        assertEquals(button[0].bounds().bottom(), flyout.content().bounds().y());
        assertTrue(((Flyout) flyout.content()).content().orElseThrow() instanceof ColorPicker);
    }

    @Test
    void thePasswordFormSavesOnlyMatchingPasswordsAndZeroesWhatItGave() {
        final ScreenHost host = StrataCore.createScreenHost(new ToyPlatform(), Themes.flat());
        host.resize(320, 200);
        final List<char[]> given = new ArrayList<>();
        final StackPanel[] form = new StackPanel[1];
        host.open(factory -> form[0] = ControlExamples.passwordForm(factory, chars -> {
            given.add(chars);
            return new String(chars).equals("pears");
        }));
        host.layout();
        final PasswordTextBox password = (PasswordTextBox) form[0].children().get(0);
        final PasswordTextBox confirm = (PasswordTextBox) form[0].children().get(1);
        final Button save = (Button) form[0].children().get(3);

        password.requestFocus();
        "pears".codePoints().forEach(codePoint -> host.charTyped(codePoint, Modifiers.NONE));
        assertFalse(save.isEnabled());
        confirm.requestFocus();
        "pears".codePoints().forEach(codePoint -> host.charTyped(codePoint, Modifiers.NONE));
        assertTrue(save.isEnabled());

        save.requestFocus();
        host.key(Key.ENTER, KeyAction.PRESS, Modifiers.NONE);
        assertEquals(1, given.size());
        assertArrayEquals(new char[5], given.getFirst(), "the chars handed out are zeroed afterwards");
    }

    @Test
    void theToyAdapterDrawsAndTakesInput() {
        final ToyAdapter adapter = new ToyAdapter(Themes.flat(), 2f);
        adapter.onResize(640, 400);
        adapter.open(new SettingsScreen());
        final List<String> commands = adapter.onFrame();
        assertTrue(commands.stream().anyMatch(command -> command.startsWith("text 'Save'")), commands::toString);

        // Tab focuses the text box, the first tabbable component; typing goes into it
        assertTrue(adapter.onKey(9, true, 0));
        assertTrue(adapter.onChar('H'));
        assertTrue(adapter.onChar('i'));
        assertTrue(adapter.onFrame().stream().anyMatch(command -> command.startsWith("text 'Hi'")));
    }

    @Test
    void theSettingsScreenFollowsItsModelBothWays() {
        final ScreenHost host = StrataCore.createScreenHost(new ToyPlatform(), Themes.flat());
        host.resize(400, 300);
        final BindingExamples.SettingsModel model = new BindingExamples.SettingsModel();
        final List<String> saved = new ArrayList<>();
        final BindingExamples.SettingsScreen screen = new BindingExamples.SettingsScreen(model,
                saving -> saved.add(saving.name.get()));
        final StackPanel[] column = new StackPanel[1];
        host.open(factory -> column[0] = (StackPanel) screen.build(factory));
        host.layout();
        final CheckBox music = (CheckBox) column[0].children().get(0);
        final Slider volume = (Slider) column[0].children().get(1);
        final Label greeting = (Label) column[0].children().get(3);
        final Button save = (Button) column[0].children().get(4);
        assertTrue(music.isSelected());
        assertEquals(0.8, volume.value(), 1e-9);
        assertEquals("Hello, Ana", greeting.text().plain());

        model.name.set(" ");
        host.flush();
        assertFalse(save.isEnabled());
        model.name.set("Ben");
        host.flush();
        assertTrue(save.isEnabled());
        assertEquals("Hello, Ben", greeting.text().plain());

        music.requestFocus();
        host.key(Key.SPACE, KeyAction.PRESS, Modifiers.NONE);
        host.key(Key.SPACE, KeyAction.RELEASE, Modifiers.NONE);
        assertFalse(model.music.get(), "the user's change reaches the model");
        host.flush();
        assertFalse(volume.isEnabled(), "and the slider bound to it follows");

        save.press();
        assertEquals(List.of("Ben"), saved);
    }

    @Test
    void theBoundListKeepsItsSelectionWhenTheItemsChange() {
        final ScreenHost host = StrataCore.createScreenHost(new ToyPlatform(), Themes.flat());
        host.resize(300, 200);
        final State<List<String>> names = State.of(List.of("Ana", "Ben", "Cleo"));
        final State<String> chosen = State.of("Ben");
        final ListView<?>[] list = new ListView<?>[1];
        host.open(factory -> list[0] = BindingExamples.list(factory, names, chosen));
        host.layout();
        assertEquals(1, list[0].selectedIndex());
        names.set(List.of("Ben", "Dev"));
        host.flush();
        assertEquals(0, list[0].selectedIndex());
        list[0].select(1);
        assertEquals("Dev", chosen.get());
    }
}
