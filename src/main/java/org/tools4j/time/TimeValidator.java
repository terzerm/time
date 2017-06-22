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

    public static boolean isValidHour(final int hour) {
        return 0 <= hour & hour < HOURS_PER_DAY;
    }

    public static boolean isValidMinute(final int minute) {
        return 0 <= minute & minute < MINUTES_PER_HOUR;
    }

    public static boolean isValidSecond(final int second) {
        return 0 <= second & second < SECONDS_PER_MINUTE;
    }

    public static boolean isValidMilli(final int milli) {
        return 0 <= milli & milli < MILLIS_PER_SECOND;
    }

    public static boolean isValidMicro(final int micro) {
        return 0 <= micro & micro < MICROS_PER_SECOND;
    }

    public static boolean isValidNano(final int nano) {
        return 0 <= nano & nano < NANOS_PER_SECOND;
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
