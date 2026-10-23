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
package org.inspirenxe.strata.examples.vulkan;

import org.inspirenxe.strata.api.Strata;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.component.CursorKind;
import org.inspirenxe.strata.api.input.Modifiers;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.theme.Themes;
import org.inspirenxe.strata.examples.quickstart.SettingsScreen;
import org.inspirenxe.strata.vulkan.StrataVulkan;
import org.inspirenxe.strata.vulkan.VulkanFont;
import org.inspirenxe.strata.vulkan.VulkanImage;
import org.inspirenxe.strata.vulkan.VulkanPlatform;
import org.inspirenxe.strata.vulkan.VulkanRenderer;
import org.inspirenxe.strata.vulkan.VulkanTarget;
import org.inspirenxe.strata.vulkan.VulkanTextMetrics;
import org.inspirenxe.strata.vulkan.VulkanWindow;
import org.inspirenxe.strata.vulkan.glfw.StrataGlfw;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;

import java.io.IOException;
import java.nio.file.Path;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_F11;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_4_BIT;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;

/**
 * The Vulkan platform page's examples. They compile against the adapter; the device, queue, window and command
 * buffers come from the application around them.
 */
public final class VulkanExamples {

    private VulkanExamples() {
    }

    static StrataVulkan setup(final VkDevice device, final VkQueue graphicsQueue, final int queueFamily,
                              final int swapchainFormat, final long window) {
        // --8<-- [start:setup]
        // One renderer per device and target: here dynamic rendering into the swapchain's format,
        // with two frames in flight
        final VulkanRenderer renderer = new VulkanRenderer(device, graphicsQueue, queueFamily,
                VulkanTarget.dynamic(swapchainFormat), 2);
        // GLFW's clipboard and key repeats
        final VulkanPlatform platform = StrataGlfw.platform(window, VulkanTextMetrics.defaults()).build();
        final StrataVulkan strata = new StrataVulkan(platform, renderer, Themes.flat(), 1f);
        // Strata.screens() opens screens on this view
        strata.setDefault();
        // Input, size and display scale from the window, at 2 UI units per display unit
        final StrataGlfw glfw = StrataGlfw.attach(window, strata, 2f);
        // The close button asks the screens first, and the title follows the top screen's
        glfw.manageWindow("Settings");
        Strata.screens().open(new SettingsScreen());
        // --8<-- [end:setup]
        return strata;
    }

    static void frame(final StrataVulkan strata, final VkCommandBuffer commands) {
        // --8<-- [start:frame]
        // Inside the frame's pass, after the scene and before vkCmdEndRendering:
        strata.render(commands);
        // Strata leaves its pipeline, viewport and scissor bound; set your own again before drawing more
        // --8<-- [end:frame]
    }

    static void shutdown(final VkDevice device, final StrataVulkan strata, final VulkanRenderer renderer) {
        // --8<-- [start:shutdown]
        vkDeviceWaitIdle(device);
        strata.close();
        renderer.close();
        // --8<-- [end:shutdown]
    }

    static VulkanTarget targets(final int swapchainFormat, final long renderPass) {
        // --8<-- [start:targets]
        // Dynamic rendering whose pass also has a depth attachment, which Strata leaves alone
        final VulkanTarget withDepth = VulkanTarget.dynamic(swapchainFormat, VK_FORMAT_D32_SFLOAT,
                VK_FORMAT_UNDEFINED);
        // Subpass 0 of a render pass whose color attachment encodes to sRGB, multisampled
        final VulkanTarget subpass = VulkanTarget.renderPass(renderPass, 0, true).withSamples(VK_SAMPLE_COUNT_4_BIT);
        // --8<-- [end:targets]
        return withDepth.isDynamic() ? subpass : withDepth;
    }

    static VulkanTextMetrics fonts() throws IOException {
        // --8<-- [start:fonts]
        final VulkanFont text = VulkanFont.builder(9f)
                .regular(Path.of("fonts/Inter-Regular.ttf"))
                .bold(Path.of("fonts/Inter-Bold.ttf"))
                // Characters Inter lacks come from here; italic is made by slanting the regular face
                .fallback(Path.of("fonts/NotoSansSymbols2-Regular.ttf"), 0)
                .build();
        final VulkanFont code = VulkanFont.load(Path.of("fonts/JetBrainsMono-Regular.ttf"), 9f);
        final VulkanTextMetrics metrics = new VulkanTextMetrics(text, code);
        // --8<-- [end:fonts]
        return metrics;
    }

    static void forward(final long window, final StrataVulkan strata) {
        // --8<-- [start:forward]
        // The application keeps GLFW's callbacks and hands Strata what it should see
        final StrataGlfw glfw = StrataGlfw.create(window, strata, 2f);
        glfwSetKeyCallback(window, (handle, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_F11) {
                // The application's own key
                return;
            }
            glfw.key(key, scancode, action, mods);
        });
        // --8<-- [end:forward]
    }

    static void otherWindow(final StrataVulkan strata, final float pointerX, final float pointerY) {
        // --8<-- [start:other]
        strata.setWindow(new VulkanWindow() {
            @Override
            public boolean showsCursor(final CursorKind cursor) {
                return cursor == CursorKind.ARROW || cursor == CursorKind.TEXT || cursor == CursorKind.HAND;
            }

            @Override
            public void showCursor(final CursorKind cursor) {
                // Set the windowing library's cursor
            }
        });
        // Positions arrive in framebuffer pixels; the view turns them into UI units
        final boolean overStrata = strata.screens().pointerMoved(strata.unitX(pointerX), strata.unitY(pointerY),
                Modifiers.NONE);
        // --8<-- [end:other]
        if (overStrata) {
            strata.screens().pointerLeft();
        }
    }

    static Screen image(final VulkanRenderer renderer, final long minimapView) {
        // --8<-- [start:image]
        // A view of an image the application renders each frame, shown at 128 by 128 units
        final VulkanImage minimap = renderer.image(minimapView, 128f, 128f);
        final Screen screen = factory -> {
            final Component<?> map = factory.image(minimap);
            return factory.stackPanel().add(factory.label("Map")).add(map);
        };
        // When the screen no longer needs it: minimap.close()
        // --8<-- [end:image]
        return screen;
    }
}
