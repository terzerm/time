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

import org.tools4j.time.pack.DatePacker;
import org.tools4j.time.pack.MilliTimePacker;
import org.tools4j.time.pack.NanoTimePacker;
import org.tools4j.time.pack.TimePacker;
import org.tools4j.time.validate.DateValidator;

import java.time.chrono.IsoChronology;

import static java.lang.Math.floorDiv;
import static java.lang.Math.floorMod;
import static java.time.temporal.ChronoField.YEAR;

/**
 * Converts dates to days since epoch and vice versa.
 */
public final class Epoch {

    /**
     * The number of days in a 400 year cycle.
     */
    private static final int DAYS_PER_CYCLE = 146097;

    /**
     * The number of days from year zero to year 1970.
     * There are five 400 year cycles from year zero to 2000.
     * There are 7 leap years from 1970 to 2000.
     */
    private static final long DAYS_0000_TO_1970 = (DAYS_PER_CYCLE * 5L) - (30L * 365L + 7L);

    public static long toEpochDays(final int year, final int month, final int day) {
        //see LocalDate.toEpochDay
        long y = YEAR.checkValidIntValue(year);
        long m = month;
        long total = 0;
        total += 365 * y;
        if (y >= 0) {
            total += (y + 3) / 4 - (y + 99) / 100 + (y + 399) / 400;
        } else {
            total -= y / -4 - y / -100 + y / -400;
        }
        total += ((367 * m - 362) / 12);
        total += day - 1;
        if (m > 2) {
            total--;
            if (!IsoChronology.INSTANCE.isLeapYear(year)) {
                total--;
            }
        }
        return total - DAYS_0000_TO_1970;
    }

    public static long toEpochDays(final int packedDate, final DatePacker datePacker) {
        return toEpochDays(
                datePacker.unpackYear(packedDate),
                datePacker.unpackMonth(packedDate),
                datePacker.unpackDay(packedDate)
        );
    }

    public static long toEpochSeconds(final int year, final int month, final int day) {
        return toEpochDays(year, month, day) * TimeFactors.SECONDS_PER_DAY;
    }

    public static long toEpochSeconds(final int year, final int month, final int day,
                                      final int hour, final int minute) {
        return toEpochSeconds(year, month, day, hour, minute, 0);
    }

    public static long toEpochSeconds(final int year, final int month, final int day,
                                      final int hour, final int minute, final int second) {
        return toEpochSeconds(year, month, day)
                + hour * TimeFactors.SECONDS_PER_HOUR + minute * TimeFactors.SECONDS_PER_MINUTE + second;
    }

    public static long toEpochSeconds(final int packedDate, final DatePacker datePacker) {
        return toEpochDays(packedDate, datePacker) * TimeFactors.SECONDS_PER_DAY;
    }

    public static long toEpochSeconds(final int packedDate, final DatePacker datePacker,
                                      final int packedTime, final TimePacker timePacker) {
        return toEpochSeconds(
                datePacker.unpackYear(packedDate),
                datePacker.unpackMonth(packedDate),
                datePacker.unpackDay(packedDate),
                timePacker.unpackHour(packedTime),
                timePacker.unpackMinute(packedTime),
                timePacker.unpackSecond(packedTime)
        );
    }

    public static long toEpochMillis(final int year, final int month, final int day) {
        return toEpochDays(year, month, day) * TimeFactors.MILLIS_PER_DAY;
    }

    public static long toEpochMillis(final int year, final int month, final int day,
                                     final int hour, final int minute, final int second, final int milli) {
        return toEpochMillis(year, month, day)
                + hour * TimeFactors.MILLIS_PER_HOUR + minute * TimeFactors.MILLIS_PER_MINUTE + second * TimeFactors.MILLIS_PER_SECOND + milli;
    }

    public static long toEpochMillis(final int packedDate, final DatePacker datePacker) {
        return toEpochDays(packedDate, datePacker) * TimeFactors.MILLIS_PER_DAY;
    }

    public static long toEpochMillis(final int packedDate, final DatePacker datePacker,
                                     final int packedMilliTime, final MilliTimePacker milliTimePacker) {
        return toEpochMillis(
                datePacker.unpackYear(packedDate),
                datePacker.unpackMonth(packedDate),
                datePacker.unpackDay(packedDate),
                milliTimePacker.unpackHour(packedMilliTime),
                milliTimePacker.unpackMinute(packedMilliTime),
                milliTimePacker.unpackSecond(packedMilliTime),
                milliTimePacker.unpackMilli(packedMilliTime)
        );
    }

    public static long toEpochNanos(final int year, final int month, final int day) {
        return toEpochDays(year, month, day) * TimeFactors.NANOS_PER_DAY;
    }

    public static long toEpochNanos(final int year, final int month, final int day,
                                    final int hour, final int minute, final int second, final int nano) {
        return toEpochNanos(year, month, day)
                + hour * TimeFactors.NANOS_PER_HOUR + minute * TimeFactors.NANOS_PER_MINUTE + second * TimeFactors.NANOS_PER_SECOND + nano;
    }

    public static long toEpochNanos(final int packedDate, final DatePacker datePacker,
                                    final long packedNanoTime, final NanoTimePacker nanoTimePacker) {
        return toEpochNanos(
                datePacker.unpackYear(packedDate),
                datePacker.unpackMonth(packedDate),
                datePacker.unpackDay(packedDate),
                nanoTimePacker.unpackHour(packedNanoTime),
                nanoTimePacker.unpackMinute(packedNanoTime),
                nanoTimePacker.unpackSecond(packedNanoTime),
                nanoTimePacker.unpackNano(packedNanoTime)
        );
    }

    public static int fromEpochDays(final long daysSinceEpoch, final DatePacker datePacker) {
        //see LocalDate.ofEpochDay(..)
        long zeroDay = daysSinceEpoch + DAYS_0000_TO_1970;
        // find the march-based year
        zeroDay -= 60;  // adjust to 0000-03-01 so leap day is at end of four year cycle
        long adjust = 0;
        if (zeroDay < 0) {
            // adjust negative years to positive for calculation
            long adjustCycles = (zeroDay + 1) / DAYS_PER_CYCLE - 1;
            adjust = adjustCycles * 400;
            zeroDay += -adjustCycles * DAYS_PER_CYCLE;
        }
        long yearEst = (400 * zeroDay + 591) / DAYS_PER_CYCLE;
        long doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400);
        if (doyEst < 0) {
            // fix estimate
            yearEst--;
            doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400);
        }
        yearEst += adjust;  // reset any negative year
        final int marchDoy0 = (int) doyEst;

        // convert march-based values back to january-based
        final int marchMonth0 = (marchDoy0 * 5 + 2) / 153;
        final int month = (marchMonth0 + 2) % 12 + 1;
        final int day = marchDoy0 - (marchMonth0 * 306 + 5) / 10 + 1;
        yearEst += marchMonth0 / 10;

        // check year now we are certain it is correct
        final int year = validateYear(yearEst);
        return datePacker.pack(year, month, day);
    }

    public static int fromEpochSeconds(final long secondsSinceEpoch, final DatePacker datePacker) {
        return fromEpochDays(Math.floorDiv(secondsSinceEpoch, TimeFactors.SECONDS_PER_DAY), datePacker);
    }

    public static int fromEpochSeconds(final long secondsSinceEpoch, final TimePacker timePacker) {
        final int timeInSeconds = (int) Math.floorMod(secondsSinceEpoch, TimeFactors.SECONDS_PER_DAY);
        return timePacker.pack(
                divMod(timeInSeconds, TimeFactors.SECONDS_PER_HOUR, TimeFactors.HOURS_PER_DAY),
                divMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE, TimeFactors.MINUTES_PER_HOUR),
                Math.floorMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE)
        );
    }

    public static long fromEpochSeconds(final long secondsSinceEpoch, final MilliTimePacker milliTimePacker) {
        final int timeInSeconds = (int) Math.floorMod(secondsSinceEpoch, TimeFactors.SECONDS_PER_DAY);
        return milliTimePacker.pack(
                divMod(timeInSeconds, TimeFactors.SECONDS_PER_HOUR, TimeFactors.HOURS_PER_DAY),
                divMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE, TimeFactors.MINUTES_PER_HOUR),
                Math.floorMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE),
                0
        );
    }

    public static long fromEpochSeconds(final long secondsSinceEpoch, final NanoTimePacker nanoTimePacker) {
        final int timeInSeconds = (int) Math.floorMod(secondsSinceEpoch, TimeFactors.SECONDS_PER_DAY);
        return nanoTimePacker.pack(
                divMod(timeInSeconds, TimeFactors.SECONDS_PER_HOUR, TimeFactors.HOURS_PER_DAY),
                divMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE, TimeFactors.MINUTES_PER_HOUR),
                Math.floorMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE),
                0
        );
    }

    public static int fromEpochMillis(final long millisSinceEpoch, final DatePacker datePacker) {
        return fromEpochDays(Math.floorDiv(millisSinceEpoch, TimeFactors.MILLIS_PER_DAY), datePacker);
    }

    public static int fromEpochMillis(final long millisSinceEpoch, final MilliTimePacker milliTimePacker) {
        final int timeInMillis = (int) Math.floorMod(millisSinceEpoch, TimeFactors.MILLIS_PER_DAY);
        return milliTimePacker.pack(
                divMod(timeInMillis, TimeFactors.MILLIS_PER_HOUR, TimeFactors.HOURS_PER_DAY),
                divMod(timeInMillis, TimeFactors.MILLIS_PER_MINUTE, TimeFactors.MINUTES_PER_HOUR),
                divMod(timeInMillis, TimeFactors.MILLIS_PER_SECOND, TimeFactors.SECONDS_PER_MINUTE),
                Math.floorMod(timeInMillis, TimeFactors.MILLIS_PER_SECOND)
        );
    }

    public static long fromEpochMillis(final long millisSinceEpoch, final NanoTimePacker nanoTimePacker) {
        final int timeInMillis = (int) Math.floorMod(millisSinceEpoch, TimeFactors.MILLIS_PER_DAY);
        return nanoTimePacker.pack(
                divMod(timeInMillis, TimeFactors.MILLIS_PER_HOUR, TimeFactors.HOURS_PER_DAY),
                divMod(timeInMillis, TimeFactors.MILLIS_PER_MINUTE, TimeFactors.MINUTES_PER_HOUR),
                divMod(timeInMillis, TimeFactors.MILLIS_PER_SECOND, TimeFactors.SECONDS_PER_MINUTE),
                Math.floorMod(timeInMillis, TimeFactors.MILLIS_PER_SECOND) * TimeFactors.NANOS_PER_MILLI
        );
    }

    public static long fromEpochNanos(final long nanosSinceEpoch, final NanoTimePacker nanoTimePacker) {
        final int timeInSeconds = divMod(nanosSinceEpoch, TimeFactors.NANOS_PER_SECOND, TimeFactors.SECONDS_PER_DAY);
        return nanoTimePacker.pack(
                divMod(timeInSeconds, TimeFactors.SECONDS_PER_HOUR, TimeFactors.HOURS_PER_DAY),
                divMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE, TimeFactors.MINUTES_PER_HOUR),
                Math.floorMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE),
                (int) Math.floorMod(nanosSinceEpoch, TimeFactors.NANOS_PER_SECOND)
        );
    }

    private static int validateYear(final long year) {
        if (DateValidator.YEAR_MIN <= year & year <= DateValidator.YEAR_MAX) {
            return (int)year;
        }
        throw new IllegalArgumentException("Invalid year, must be in [1,9999] but was: " + year);
    }

    private static int divMod(final int value, final int divisor, final int moduloDivisor) {
        return floorMod(floorDiv(value, divisor), moduloDivisor);
    }

    private static int divMod(final long value, final long divisor, final int moduloDivisor) {
        return (int) floorMod(floorDiv(value, divisor), moduloDivisor);
    }

    private Epoch() {
        throw new RuntimeException("No Epoch for you!");
    }
}
