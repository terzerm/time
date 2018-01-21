/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2018 tools4j.org (Marco Terzer)
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
package org.tools4j.time.zone;

import org.tools4j.time.base.TimeFactors;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.tools4j.time.base.TimeFactors.NANOS_PER_HOUR;
import static org.tools4j.time.base.TimeFactors.NANOS_PER_SECOND;
import static org.tools4j.time.base.TimeFactors.SECONDS_PER_HOUR;

/**
 * Provides methods dealing with {@link ZoneId} and its subtypes {@link ZoneOffset} and {@code ZoneRegion}.
 */
public final class Zones {



    private Zones() {
        throw new RuntimeException("No Zones for you!");
    }

    public static void main(String... args) {
//        DateFormatter dateFormatter = DateFormatter.valueOf(DateFormat.DD_MM_YYYY);
//        dateFormatter.formatEpochMilli(System.currentTimeMillis(), System.out);System.out.println();
//        dateFormatter.formatEpochMilli(System.currentTimeMillis(), Zone.utc(), System.out);System.out.println();
//        dateFormatter.formatEpochMilli(System.currentTimeMillis(), Zone.systemDefault(), System.out);System.out.println();
//        dateFormatter.formatEpochMilli(System.currentTimeMillis(), Zone.newYork(), System.out);System.out.println();

//        //AU Daylight
        final Zone zone = Zone.systemDefault();
//        2016	Sunday, April 3, 3:00 am	Sunday, October 2, 2:00 am
//        2017	Sunday, April 2, 3:00 am	Sunday, October 1, 2:00 am
//        2018	Sunday, April 1, 3:00 am	Sunday, October 7, 2:00 am
        final ZonedDateTime dlEnd16 = ZonedDateTime.of(2016, 4, 3, 3, 0, 0, 0, zone.zoneId());
        final ZonedDateTime dlStar16 = ZonedDateTime.of(2016, 10, 2, 2, 0, 0, 0, zone.zoneId());
        final ZonedDateTime dlEnd17 = ZonedDateTime.of(2017, 4, 2, 3, 0, 0, 0, zone.zoneId());
        final ZonedDateTime dlStar17 = ZonedDateTime.of(2017, 10, 1, 2, 0, 0, 0, zone.zoneId());
        final ZonedDateTime dlEnd18 = ZonedDateTime.of(2018, 4, 1, 3, 0, 0, 0, zone.zoneId());
        final ZonedDateTime dlStar18 = ZonedDateTime.of(2018, 10, 7, 2, 0, 0, 0, zone.zoneId());

        final long[] nanoOffsets = {-2*NANOS_PER_HOUR-1, -2*NANOS_PER_HOUR, -1*NANOS_PER_HOUR-1, -1*NANOS_PER_HOUR, -1, 0, 1, NANOS_PER_SECOND};
        for (ZonedDateTime zdt : new ZonedDateTime[]{dlEnd16, dlStar16, dlEnd17, dlStar17, dlEnd18, dlStar18}) {
            for (long nanosOff : nanoOffsets) {
                final LocalDateTime ldt = zdt.toLocalDateTime().plusNanos(nanosOff);
                System.out.println(ldt + ":\t+" + zone.offsetSeconds(
                        ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth(),
                        ldt.getHour(), ldt.getMinute(), ldt.getSecond(), ldt.getNano()
                ) / SECONDS_PER_HOUR);
            }
            System.out.println();
        }
    }
}
