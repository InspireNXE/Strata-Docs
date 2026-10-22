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

import org.inspirenxe.strata.api.animation.Animation;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.examples.animation.AnimationExamples;
import org.inspirenxe.strata.examples.animation.AnimationExamples.VolumeScreen;
import org.inspirenxe.strata.testing.ScreenTester;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that the animation guide's examples do what the page says.
 */
class AnimationExamplesTest {

    @Test
    void aPropertyMovesByTheClockAndTheStateHearsOnlyWhereItEnds() {
        final VolumeScreen screen = new VolumeScreen();
        try (ScreenTester tester = ScreenTester.open(screen)) {
            final List<Boolean> fromMotion = AnimationExamples.watchMotion(screen.slider());
            final List<Double> heard = new ArrayList<>();
            screen.volume.subscribe(heard::add);

            final Animation animation = screen.slideTo(0.8);
            assertTrue(animation.isRunning());
            assertEquals(0.0, screen.slider().value());

            tester.advance(125);
            final double halfway = screen.slider().value();
            assertTrue(halfway > 0.0 && halfway < 0.8, "halfway was " + halfway);
            // The slider reports every step, each with the animation on its cause; the state hears nothing yet
            assertFalse(fromMotion.isEmpty());
            assertTrue(fromMotion.stream().allMatch(motion -> motion));
            assertEquals(List.of(), heard);

            tester.settle();
            assertFalse(animation.isRunning());
            assertEquals(0.8, screen.slider().value());
            assertEquals(List.of(0.8), heard);
        }
    }

    @Test
    void closingStopsWhereItIsAndTheStateGetsThatValue() {
        final VolumeScreen screen = new VolumeScreen();
        try (ScreenTester tester = ScreenTester.open(screen)) {
            final Animation animation = screen.slideTo(1.0);
            tester.advance(100);
            final double stoppedAt = screen.slider().value();
            assertTrue(stoppedAt > 0.0 && stoppedAt < 1.0);
            animation.close();
            tester.advance(1000);
            assertEquals(stoppedAt, screen.slider().value());
            assertEquals(stoppedAt, screen.volume.get());
        }
    }

    @Test
    void finishJumpsToTheEndAndRunsTheActions() {
        final VolumeScreen screen = new VolumeScreen();
        try (ScreenTester tester = ScreenTester.open(screen)) {
            final Label card = screen.card();
            final Animation fade = screen.fadeOut();
            tester.advance(100);
            assertTrue(card.opacity() < 1f && card.opacity() > 0f);
            assertTrue(card.isVisible());
            fade.finish();
            assertEquals(0f, card.opacity());
            assertFalse(card.isVisible());
        }
    }

    @Test
    void closedAnimationsNeverRunTheirFinishedActions() {
        final VolumeScreen screen = new VolumeScreen();
        try (ScreenTester tester = ScreenTester.open(screen)) {
            final Animation fade = screen.fadeOut();
            tester.advance(50);
            fade.close();
            tester.settle();
            assertTrue(screen.card().isVisible());
            assertTrue(screen.card().opacity() > 0f);
        }
    }

    @Test
    void colorsBlendChannelByChannelAndRunCountsThroughTheFractions() {
        final VolumeScreen screen = new VolumeScreen();
        try (ScreenTester tester = ScreenTester.open(screen)) {
            screen.warn();
            screen.countUp(200);
            tester.settle();
            assertEquals(0xFFE04848, screen.picker().color());
            assertEquals("200", screen.total().text().plain());
        }
    }

    @Test
    void settleRunsNoFrameWhenNothingMoves() {
        final VolumeScreen screen = new VolumeScreen();
        try (ScreenTester tester = ScreenTester.open(screen)) {
            tester.settle();
            assertEquals(0.0, screen.slider().value());
        }
    }
}
