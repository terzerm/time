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
package org.tools4j.time;

import static org.tools4j.time.TimeFactors.*;

public final class TimeValidator {

    public static final int HOUR_MIN = 0;
    public static final int HOUR_MAX = HOURS_PER_DAY - 1;
    public static final int MINUTE_MIN = 0;
    public static final int MINUTE_MAX = MINUTES_PER_HOUR - 1;
    public static final int SECOND_MIN = 0;
    public static final int SECOND_MAX = SECONDS_PER_MINUTE - 1;
    public static final int MILLI_MIN = 0;
    public static final int MILLI_MAX = MILLIS_PER_SECOND - 1;
    public static final int MICRO_MIN = 0;
    public static final int MICRO_MAX = MICROS_PER_SECOND - 1;
    public static final int NANO_MIN = 0;
    public static final int NANO_MAX = NANOS_PER_SECOND - 1;

    public static boolean isValidHour(final int hour) {
        return HOUR_MIN <= hour & hour <= HOUR_MAX;
    }

    public static boolean isValidMinute(final int minute) {
        return MINUTE_MIN <= minute & minute <= MINUTE_MAX;
    }

    public static boolean isValidSecond(final int second) {
        return SECOND_MIN <= second & second <= SECOND_MAX;
    }

    public static boolean isValidMilli(final int milli) {
        return MILLI_MIN <= milli & milli <= MILLI_MAX;
    }

    public static boolean isValidMicro(final int micro) {
        return MICRO_MIN <= micro & micro <= MICRO_MAX;
    }

    public static boolean isValidNano(final int nano) {
        return NANO_MIN <= nano & nano <= NANO_MAX;
    }

    public static int checkValidHour(final int hour) {
        if (isValidHour(hour)) {
            return hour;
        }
        throw new IllegalArgumentException("Invalid hour, must be in [0,23] but was: " + hour);
    }

    public static int checkValidMinute(final int minute) {
        if (isValidMinute(minute)) {
            return minute;
        }
        throw new IllegalArgumentException("Invalid minute, must be in [0,59] but was: " + minute);
    }

    public static int checkValidSecond(final int second) {
        if (isValidSecond(second)) {
            return second;
        }
        throw new IllegalArgumentException("Invalid second, must be in [0,59] but was: " + second);
    }

    public static int checkValidMilli(final int milli) {
        if (isValidMilli(milli)) {
            return milli;
        }
        throw new IllegalArgumentException("Invalid milli second, must be in [0,999] but was: " + milli);
    }

    public static int checkValidMicro(final int micro) {
        if (isValidMicro(micro)) {
            return micro;
        }
        throw new IllegalArgumentException("Invalid micro second, must be in [0,999999] but was: " + micro);
    }

    public static int checkValidNano(final int nano) {
        if (isValidNano(nano)) {
            return nano;
        }
        throw new IllegalArgumentException("Invalid nano second, must be in [0,999999999] but was: " + nano);
    }

    public static void checkValidTime(final int hour, final int minute) {
        checkValidHour(hour);
        checkValidMinute(minute);
    }

    public static void checkValidTime(final int hour, final int minute, final int second) {
        checkValidHour(hour);
        checkValidMinute(minute);
        checkValidSecond(second);
    }

    public static void checkValidTimeWithMillis(final int hour, final int minute, final int second,
                                                final int milli) {
        checkValidHour(hour);
        checkValidMinute(minute);
        checkValidSecond(second);
        checkValidMilli(milli);
    }

    public static void checkValidTimeWithMicros(final int hour, final int minute, final int second,
                                                final int micro) {
        checkValidHour(hour);
        checkValidMinute(minute);
        checkValidSecond(second);
        checkValidMicro(micro);
    }

    public static void checkValidTimeWithNanos(final int hour, final int minute, final int second,
                                               final int nano) {
        checkValidHour(hour);
        checkValidMinute(minute);
        checkValidSecond(second);
        checkValidNano(nano);
    }

    private TimeValidator() {
        throw new RuntimeException("No TimeValidator for you!");
    }
}
