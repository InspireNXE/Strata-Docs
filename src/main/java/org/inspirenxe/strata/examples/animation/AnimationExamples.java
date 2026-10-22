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
package org.inspirenxe.strata.examples.animation;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.animation.Animation;
import org.inspirenxe.strata.api.animation.Easing;
import org.inspirenxe.strata.api.animation.Interpolator;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.value.Ranged;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.ColorPicker;
import org.inspirenxe.strata.api.control.field.Slider;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.state.State;
import org.inspirenxe.strata.api.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * The animation guide's examples.
 */
public final class AnimationExamples {

    private AnimationExamples() {
    }

    /**
     * A card, a total and a volume slider whose motion the examples drive.
     */
    public static final class VolumeScreen implements Screen {
        /** The volume the slider is bound to, two ways. */
        public final State<Double> volume = State.of(0.0);
        private ComponentFactory ui;
        private Slider slider;
        private Label card;
        private Label total;
        private ColorPicker picker;

        @Override
        public Component<?> build(final ComponentFactory factory) {
            this.ui = factory;
            this.slider = factory.slider().bind(Ranged.VALUE, this.volume);
            this.card = factory.label("Saved");
            this.total = factory.label("0");
            this.picker = factory.colorPicker();
            final StackPanel column = factory.stackPanel();
            return column.add(this.slider).add(this.card).add(this.total).add(this.picker);
        }

        /**
         * Returns the slider.
         *
         * @return the slider
         */
        public Slider slider() {
            return this.slider;
        }

        /**
         * Returns the card.
         *
         * @return the card
         */
        public Label card() {
            return this.card;
        }

        /**
         * Returns the label that counts up.
         *
         * @return the label
         */
        public Label total() {
            return this.total;
        }

        /**
         * Returns the color picker.
         *
         * @return the picker
         */
        public ColorPicker picker() {
            return this.picker;
        }

        // --8<-- [start:property]
        /**
         * Moves the slider to a value over a quarter of a second.
         */
        public Animation slideTo(final double value) {
            return this.ui.animator().animate(this.slider, Ranged.VALUE, value, 250, Easing.EASE_OUT);
        }
        // --8<-- [end:property]

        // --8<-- [start:fade]
        /**
         * Fades the card out, then hides it. Closing the animation, or closing the screen, leaves it half faded.
         */
        public Animation fadeOut() {
            return this.ui.animator()
                    .animate(this.card, Component.OPACITY, 0f, 200, Easing.EASE_IN, Interpolator.FLOAT)
                    .onFinished(() -> this.card.setVisible(false));
        }
        // --8<-- [end:fade]

        // --8<-- [start:color]
        /**
         * Blends the picker's color to red, channel by channel, with an easing that overshoots a little.
         */
        public Animation warn() {
            final Easing springy = Easing.cubicBezier(0.34f, 1.56f, 0.64f, 1f);
            return this.ui.animator().animate(this.picker, ColorPicker.COLOR, 0xFFE04848, 300, springy,
                    Interpolator.ARGB);
        }
        // --8<-- [end:color]

        // --8<-- [start:run]
        /**
         * Counts a label up to a total. {@code run} gives the eased fraction, and the example turns it into a number.
         */
        public Animation countUp(final int to) {
            return this.ui.animator().run(400, Easing.EASE_OUT, fraction ->
                    this.total.setText(Text.of(Long.toString(Math.round(to * fraction)))));
        }
        // --8<-- [end:run]
    }

    // --8<-- [start:cause]
    /**
     * Lists, for every change of the slider, whether it came from an animation.
     */
    public static List<Boolean> watchMotion(final Slider slider) {
        final List<Boolean> fromMotion = new ArrayList<>();
        slider.onEvent(Ranged.CHANGED, event -> fromMotion.add(event.cause().first(Animation.class).isPresent()));
        return fromMotion;
    }
    // --8<-- [end:cause]
}
