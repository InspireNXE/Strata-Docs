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
package org.inspirenxe.strata.examples.adapter;

// --8<-- [start:metrics]
import org.inspirenxe.strata.api.text.TextMetrics;
import org.inspirenxe.strata.api.text.TextSpan;
import org.inspirenxe.strata.api.text.TextStyle;

/**
 * Text metrics for an engine with a fixed-width bitmap font: every code point is 6 units wide, one more when bold,
 * and lines are 9 units tall.
 */
public final class MonospaceTextMetrics implements TextMetrics {

    @Override
    public float width(final TextSpan span) {
        final int advance = span.style().bold() ? 7 : 6;
        return span.text().codePointCount(0, span.text().length()) * (float) advance;
    }

    @Override
    public float lineHeight(final TextStyle style) {
        return 9f;
    }
}
// --8<-- [end:metrics]
