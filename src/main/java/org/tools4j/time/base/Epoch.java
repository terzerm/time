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

import org.tools4j.time.pack.*;
import org.tools4j.time.validate.DateValidator;
import org.tools4j.time.validate.TimeValidator;
import org.tools4j.time.validate.ValidationMethod;

import static org.tools4j.time.base.DefaultEpoch.divMod;

/**
 * Converts dates to days since epoch and vice versa.
 */
public interface Epoch {

    long INVALID_EPOCH = DateValidator.INVALID_EPOCH;

    ValidationMethod validationMethod();

    long toEpochDays(int year, int month, int day);
    long toEpochDays(int packedDate, DatePacker datePacker);
    long toEpochSeconds(int year, int month, int day);
    long toEpochSeconds(int year, int month, int day, int hour, int minute);
    long toEpochSeconds(int year, int month, int day, int hour, int minute, int second);
    long toEpochSeconds(int packedDate, DatePacker datePacker);
    long toEpochSeconds(int packedDate, DatePacker datePacker, int packedTime, TimePacker timePacker);
    long toEpochMillis(int year, int month, int day);
    long toEpochMillis(int year, int month, int day, int hour, int minute, int second, int milli);
    long toEpochMillis(int packedDate, DatePacker datePacker);
    long toEpochMillis(int packedDate, DatePacker datePacker, int packedMilliTime, MilliTimePacker milliTimePacker);
    long toEpochMillis(long packedDateTime, DateTimePacker dateTimePacker);
    long toEpochNanos(int year, int month, int day);
    long toEpochNanos(int year, int month, int day, int hour, int minute, int second, int nano);
    long toEpochNanos(int packedDate, DatePacker datePacker, long packedNanoTime, NanoTimePacker nanoTimePacker);

    int fromEpochDays(long daysSinceEpoch, DatePacker datePacker);
    int fromEpochSeconds(long secondsSinceEpoch, DatePacker datePacker);
    int fromEpochSeconds(long secondsSinceEpoch, TimePacker timePacker);
    int fromEpochSeconds(long secondsSinceEpoch, MilliTimePacker milliTimePacker);
    long fromEpochSeconds(long secondsSinceEpoch, NanoTimePacker nanoTimePacker);
    long fromEpochSeconds(long secondsSinceEpoch, DateTimePacker dateTimePacker);
    int fromEpochMillis(long millisSinceEpoch, DatePacker datePacker);
    int fromEpochMillis(long millisSinceEpoch, MilliTimePacker milliTimePacker);
    long fromEpochMillis(long millisSinceEpoch, NanoTimePacker nanoTimePacker);
    long fromEpochNanos(long nanosSinceEpoch, NanoTimePacker nanoTimePacker);
    long fromEpochMillis(long millisSinceEpoch, DateTimePacker dateTimePacker);

    static Epoch valueOf(final ValidationMethod validationMethod) {
        return DefaultEpoch.valueOf(validationMethod);
    }

    interface Default extends Epoch {
        default long toEpochDays(final int packedDate, final DatePacker datePacker) {
            return toEpochDays(
                    datePacker.unpackYear(packedDate),
                    datePacker.unpackMonth(packedDate),
                    datePacker.unpackDay(packedDate)
            );
        }

        default long toEpochSeconds(final int year, final int month, final int day) {
            return toEpochDays(year, month, day) * TimeFactors.SECONDS_PER_DAY;
        }

        default long toEpochSeconds(final int year, final int month, final int day,
                                    final int hour, final int minute) {
            return toEpochSeconds(year, month, day, hour, minute, 0);
        }

        default long toEpochSeconds(final int year, final int month, final int day,
                                    final int hour, final int minute, final int second) {
            if (TimeValidator.INVALID == validationMethod().timeValidator().validateTime(hour, minute, second)) {
                return INVALID_EPOCH;
            }
            return toEpochSeconds(year, month, day)
                    + hour * TimeFactors.SECONDS_PER_HOUR + minute * TimeFactors.SECONDS_PER_MINUTE + second;
        }

        default long toEpochSeconds(final int packedDate, final DatePacker datePacker) {
            return toEpochDays(packedDate, datePacker) * TimeFactors.SECONDS_PER_DAY;
        }

        default long toEpochSeconds(final int packedDate, final DatePacker datePacker,
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

        default long toEpochMillis(final int year, final int month, final int day) {
            return toEpochDays(year, month, day) * TimeFactors.MILLIS_PER_DAY;
        }

        default long toEpochMillis(final int year, final int month, final int day,
                                   final int hour, final int minute, final int second, final int milli) {
            if (TimeValidator.INVALID == validationMethod().timeValidator().validateTimeWithMillis(hour, minute, second, milli)) {
                return INVALID_EPOCH;
            }
            return toEpochMillis(year, month, day)
                    + hour * TimeFactors.MILLIS_PER_HOUR + minute * TimeFactors.MILLIS_PER_MINUTE + second * TimeFactors.MILLIS_PER_SECOND + milli;
        }

        default long toEpochMillis(final int packedDate, final DatePacker datePacker) {
            return toEpochDays(packedDate, datePacker) * TimeFactors.MILLIS_PER_DAY;
        }

        default long toEpochMillis(final int packedDate, final DatePacker datePacker,
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

        default long toEpochMillis(final long packedDateTime, final DateTimePacker dateTimePacker) {
            return toEpochMillis(
                    dateTimePacker.unpackYear(packedDateTime),
                    dateTimePacker.unpackMonth(packedDateTime),
                    dateTimePacker.unpackDay(packedDateTime),
                    dateTimePacker.unpackHour(packedDateTime),
                    dateTimePacker.unpackMinute(packedDateTime),
                    dateTimePacker.unpackSecond(packedDateTime),
                    dateTimePacker.unpackMilli(packedDateTime)
            );
        }

        default long toEpochNanos(final int year, final int month, final int day) {
            return toEpochDays(year, month, day) * TimeFactors.NANOS_PER_DAY;
        }

        default long toEpochNanos(final int year, final int month, final int day,
                                  final int hour, final int minute, final int second, final int nano) {
            if (TimeValidator.INVALID == validationMethod().timeValidator().validateTimeWithNanos(hour, minute, second, nano)) {
                return INVALID_EPOCH;
            }
            return toEpochNanos(year, month, day)
                    + hour * TimeFactors.NANOS_PER_HOUR + minute * TimeFactors.NANOS_PER_MINUTE + second * TimeFactors.NANOS_PER_SECOND + nano;
        }

        default long toEpochNanos(final int packedDate, final DatePacker datePacker,
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

        default int fromEpochSeconds(final long secondsSinceEpoch, final DatePacker datePacker) {
            return fromEpochDays(Math.floorDiv(secondsSinceEpoch, TimeFactors.SECONDS_PER_DAY), datePacker);
        }

        default int fromEpochSeconds(final long secondsSinceEpoch, final TimePacker timePacker) {
            final int timeInSeconds = (int) Math.floorMod(secondsSinceEpoch, TimeFactors.SECONDS_PER_DAY);
            return timePacker.pack(
                    divMod(timeInSeconds, TimeFactors.SECONDS_PER_HOUR, TimeFactors.HOURS_PER_DAY),
                    divMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE, TimeFactors.MINUTES_PER_HOUR),
                    Math.floorMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE)
            );
        }

        default int fromEpochSeconds(final long secondsSinceEpoch, final MilliTimePacker milliTimePacker) {
            final int timeInSeconds = (int) Math.floorMod(secondsSinceEpoch, TimeFactors.SECONDS_PER_DAY);
            return milliTimePacker.pack(
                    divMod(timeInSeconds, TimeFactors.SECONDS_PER_HOUR, TimeFactors.HOURS_PER_DAY),
                    divMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE, TimeFactors.MINUTES_PER_HOUR),
                    Math.floorMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE),
                    0
            );
        }

        default long fromEpochSeconds(final long secondsSinceEpoch, final NanoTimePacker nanoTimePacker) {
            final int timeInSeconds = (int) Math.floorMod(secondsSinceEpoch, TimeFactors.SECONDS_PER_DAY);
            return nanoTimePacker.pack(
                    divMod(timeInSeconds, TimeFactors.SECONDS_PER_HOUR, TimeFactors.HOURS_PER_DAY),
                    divMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE, TimeFactors.MINUTES_PER_HOUR),
                    Math.floorMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE),
                    0
            );
        }

        default long fromEpochSeconds(final long secondsSinceEpoch, final DateTimePacker dateTimePacker) {
            final DatePacker datePacker = DatePacker.valueOf(Packing.BINARY, dateTimePacker.validationMethod());
            final int packedDate = fromEpochSeconds(secondsSinceEpoch, datePacker);
            final int timeInSeconds = (int) Math.floorMod(secondsSinceEpoch, TimeFactors.SECONDS_PER_DAY);
            return dateTimePacker.pack(
                    datePacker.unpackYear(packedDate),
                    datePacker.unpackMonth(packedDate),
                    datePacker.unpackDay(packedDate),
                    divMod(timeInSeconds, TimeFactors.SECONDS_PER_HOUR, TimeFactors.HOURS_PER_DAY),
                    divMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE, TimeFactors.MINUTES_PER_HOUR),
                    Math.floorMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE)
            );
        }

        default int fromEpochMillis(final long millisSinceEpoch, final DatePacker datePacker) {
            return fromEpochDays(Math.floorDiv(millisSinceEpoch, TimeFactors.MILLIS_PER_DAY), datePacker);
        }

        default int fromEpochMillis(final long millisSinceEpoch, final MilliTimePacker milliTimePacker) {
            final int timeInMillis = (int) Math.floorMod(millisSinceEpoch, TimeFactors.MILLIS_PER_DAY);
            return milliTimePacker.pack(
                    divMod(timeInMillis, TimeFactors.MILLIS_PER_HOUR, TimeFactors.HOURS_PER_DAY),
                    divMod(timeInMillis, TimeFactors.MILLIS_PER_MINUTE, TimeFactors.MINUTES_PER_HOUR),
                    divMod(timeInMillis, TimeFactors.MILLIS_PER_SECOND, TimeFactors.SECONDS_PER_MINUTE),
                    Math.floorMod(timeInMillis, TimeFactors.MILLIS_PER_SECOND)
            );
        }

        default long fromEpochMillis(final long millisSinceEpoch, final NanoTimePacker nanoTimePacker) {
            final int timeInMillis = (int) Math.floorMod(millisSinceEpoch, TimeFactors.MILLIS_PER_DAY);
            return nanoTimePacker.pack(
                    divMod(timeInMillis, TimeFactors.MILLIS_PER_HOUR, TimeFactors.HOURS_PER_DAY),
                    divMod(timeInMillis, TimeFactors.MILLIS_PER_MINUTE, TimeFactors.MINUTES_PER_HOUR),
                    divMod(timeInMillis, TimeFactors.MILLIS_PER_SECOND, TimeFactors.SECONDS_PER_MINUTE),
                    Math.floorMod(timeInMillis, TimeFactors.MILLIS_PER_SECOND) * TimeFactors.NANOS_PER_MILLI
            );
        }

        default long fromEpochMillis(final long millisSinceEpoch, final DateTimePacker dateTimePacker) {
            final DatePacker datePacker = DatePacker.valueOf(Packing.BINARY, dateTimePacker.validationMethod());
            final int packedDate = fromEpochMillis(millisSinceEpoch, datePacker);
            final int timeInMillis = (int) Math.floorMod(millisSinceEpoch, TimeFactors.MILLIS_PER_DAY);
            return dateTimePacker.pack(
                    datePacker.unpackYear(packedDate),
                    datePacker.unpackMonth(packedDate),
                    datePacker.unpackDay(packedDate),
                    divMod(timeInMillis, TimeFactors.MILLIS_PER_HOUR, TimeFactors.HOURS_PER_DAY),
                    divMod(timeInMillis, TimeFactors.MILLIS_PER_MINUTE, TimeFactors.MINUTES_PER_HOUR),
                    divMod(timeInMillis, TimeFactors.MILLIS_PER_SECOND, TimeFactors.SECONDS_PER_MINUTE),
                    Math.floorMod(timeInMillis, TimeFactors.MILLIS_PER_SECOND)
            );
        }

        default long fromEpochNanos(final long nanosSinceEpoch, final NanoTimePacker nanoTimePacker) {
            final int timeInSeconds = divMod(nanosSinceEpoch, TimeFactors.NANOS_PER_SECOND, TimeFactors.SECONDS_PER_DAY);
            return nanoTimePacker.pack(
                    divMod(timeInSeconds, TimeFactors.SECONDS_PER_HOUR, TimeFactors.HOURS_PER_DAY),
                    divMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE, TimeFactors.MINUTES_PER_HOUR),
                    Math.floorMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE),
                    (int) Math.floorMod(nanosSinceEpoch, TimeFactors.NANOS_PER_SECOND)
            );
        }
    }

}
