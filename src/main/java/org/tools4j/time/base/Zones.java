/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 tools4j.org (Marco Terzer)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.tools4j.time.base;

import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneRules;
import java.util.Arrays;

/**
 * Provides methods dealing with {@link ZoneId} and its subtypes {@link ZoneOffset} and {@code ZoneRegion}.
 */
public class Zones {

    private static final Field STANDARD_OFFSET_EPOCH_SECOND_ARRAY = zoneRulesField("standardTransitions");
    private static final Field STANDARD_OFFSET_ZONE_OFFSET_ARRAY = zoneRulesField("standardOffsets");
    private static final Field GENERAL_OFFSET_EPOCH_SECOND_ARRAY = zoneRulesField("savingsInstantTransitions");
    private static final Field GENERAL_OFFSET_ZONE_OFFSET_ARRAY = zoneRulesField("wallOffsets");

    public static int standardOffsetSeconds(final int epochSeconds, final ZoneId zoneId) {
        if (zoneId instanceof ZoneOffset) {
            return ((ZoneOffset)zoneId).getTotalSeconds();
        }
        return standardOffsetSeconds(epochSeconds, zoneId.getRules());
    }

    public static int standardOffsetSeconds(final int epochSeconds, final ZoneRules zoneRules) {
        if (zoneRules.isFixedOffset()) {
            return zoneRules.getStandardOffset(null).getTotalSeconds();
        }
        return offsetSeconds(epochSeconds, zoneRules, STANDARD_OFFSET_EPOCH_SECOND_ARRAY, STANDARD_OFFSET_ZONE_OFFSET_ARRAY);
    }

    public static int offsetSeconds(final int epochSeconds, final ZoneId zoneId) {
        if (zoneId instanceof ZoneOffset) {
            return ((ZoneOffset)zoneId).getTotalSeconds();
        }
        return offsetSeconds(epochSeconds, zoneId.getRules());
    }

    public static int offsetSeconds(final int epochSeconds, final ZoneRules zoneRules) {
        if (zoneRules.isFixedOffset()) {
            return zoneRules.getStandardOffset(null).getTotalSeconds();
        }
        return offsetSeconds(epochSeconds, zoneRules, GENERAL_OFFSET_EPOCH_SECOND_ARRAY, GENERAL_OFFSET_ZONE_OFFSET_ARRAY);
    }

    /*
     * See ZoneRules.getOffset(..) and ZoneRules.getStandardOffset(..)
     */
    private static final int offsetSeconds(final int epochSeconds, final ZoneRules zoneRules,
                                           final Field zoneRulesEpochSecondsArray,
                                           final Field zoneRulesOffsetArray) {
        try {
            final long[] epochSecondsArray = (long[]) zoneRulesEpochSecondsArray.get(zoneRules);
            final ZoneOffset[] offsetArray = (ZoneOffset[])zoneRulesOffsetArray.get(zoneRules);
            int index = Arrays.binarySearch(epochSecondsArray, epochSeconds);
            if (index < 0) {
                // switch negative insert position to start of matched range
                index = -index - 2;
            }
            return offsetArray[index + 1].getTotalSeconds();
        } catch (final Exception e) {
            throw new RuntimeException("Could not determine offset for zoneRules=" + zoneRules + ", e=" + e, e);
        }
    }

    private static Field zoneRulesField(final String fieldName) {
        try {
            final Field field = ZoneRules.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (final NoSuchFieldException e) {
            throw new RuntimeException("Could not access ZoneRules field " + fieldName + ", e=" + e, e);
        }
    }

    private Zones() {
        throw new RuntimeException("No Zones for you!");
    }
}
