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
package org.inspirenxe.strata.examples.extending;

// --8<-- [start:library]
import org.inspirenxe.strata.api.component.registry.ComponentId;
import org.inspirenxe.strata.api.component.registry.ComponentLibrary;
import org.inspirenxe.strata.api.component.registry.ComponentRegistry;

/**
 * Registers the library's components. A library lists its class in
 * META-INF/services/org.inspirenxe.strata.api.component.registry.ComponentLibrary, so nobody has to call it; this
 * example is not listed, so the docs site's own registry stays empty.
 */
public final class SwitchLibrary implements ComponentLibrary {
    /** The switch's id, for code that does not compile against this library. */
    public static final ComponentId SWITCH = ComponentId.of("examplemod", "switch");

    @Override
    public void register(final ComponentRegistry registry) {
        registry.register(SWITCH, ToggleSwitch.class, factory -> new ToggleSwitchImpl(factory, ""));
    }
}
// --8<-- [end:library]
