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

import static org.tools4j.time.base.EpochImpl.divMod;

/**
 * Converts dates to days since epoch and vice versa.
 */
public interface Epoch {

    long INVALID_EPOCH = DateValidator.INVALID_EPOCH;

    ValidationMethod validationMethod();

    long toEpochDay(int year, int month, int day);
    long toEpochDay(int packedDate, DatePacker datePacker);
    long toEpochSecond(int year, int month, int day);
    long toEpochSecond(int year, int month, int day, int hour, int minute);
    long toEpochSecond(int year, int month, int day, int hour, int minute, int second);
    long toEpochSecond(int packedDate, DatePacker datePacker);
    long toEpochSecond(int packedDate, DatePacker datePacker, int packedTime, TimePacker timePacker);
    long toEpochMilli(int year, int month, int day);
    long toEpochMilli(int year, int month, int day, int hour, int minute, int second, int milli);
    long toEpochMilli(int packedDate, DatePacker datePacker);
    long toEpochMilli(int packedDate, DatePacker datePacker, int packedMilliTime, MilliTimePacker milliTimePacker);
    long toEpochMilli(long packedDateTime, DateTimePacker dateTimePacker);
    long toEpochNano(int year, int month, int day);
    long toEpochNano(int year, int month, int day, int hour, int minute, int second, int nano);
    long toEpochNano(int packedDate, DatePacker datePacker, long packedNanoTime, NanoTimePacker nanoTimePacker);

    int fromEpochDay(long daysSinceEpoch, DatePacker datePacker);
    int fromEpochSecond(long secondsSinceEpoch, DatePacker datePacker);
    int fromEpochSecond(long secondsSinceEpoch, TimePacker timePacker);
    int fromEpochSecond(long secondsSinceEpoch, MilliTimePacker milliTimePacker);
    long fromEpochSecond(long secondsSinceEpoch, NanoTimePacker nanoTimePacker);
    long fromEpochSecond(long secondsSinceEpoch, DateTimePacker dateTimePacker);
    int fromEpochMilli(long millisSinceEpoch, DatePacker datePacker);
    int fromEpochMilli(long millisSinceEpoch, MilliTimePacker milliTimePacker);
    long fromEpochMilli(long millisSinceEpoch, NanoTimePacker nanoTimePacker);
    long fromEpochMilli(long millisSinceEpoch, DateTimePacker dateTimePacker);
    long fromEpochNano(long nanosSinceEpoch, NanoTimePacker nanoTimePacker);

    static Epoch valueOf(final ValidationMethod validationMethod) {
        return EpochImpl.valueOf(validationMethod);
    }

    interface Default extends Epoch {
        default long toEpochDay(final int packedDate, final DatePacker datePacker) {
            return toEpochDay(
                    datePacker.unpackYear(packedDate),
                    datePacker.unpackMonth(packedDate),
                    datePacker.unpackDay(packedDate)
            );
        }

        default long toEpochSecond(final int year, final int month, final int day) {
            return toEpochDay(year, month, day) * TimeFactors.SECONDS_PER_DAY;
        }

        default long toEpochSecond(final int year, final int month, final int day,
                                   final int hour, final int minute) {
            return toEpochSecond(year, month, day, hour, minute, 0);
        }

        default long toEpochSecond(final int year, final int month, final int day,
                                   final int hour, final int minute, final int second) {
            if (TimeValidator.INVALID == validationMethod().timeValidator().validateTime(hour, minute, second)) {
                return INVALID_EPOCH;
            }
            return toEpochSecond(year, month, day)
                    + hour * TimeFactors.SECONDS_PER_HOUR + minute * TimeFactors.SECONDS_PER_MINUTE + second;
        }

        default long toEpochSecond(final int packedDate, final DatePacker datePacker) {
            return toEpochDay(packedDate, datePacker) * TimeFactors.SECONDS_PER_DAY;
        }

        default long toEpochSecond(final int packedDate, final DatePacker datePacker,
                                   final int packedTime, final TimePacker timePacker) {
            return toEpochSecond(
                    datePacker.unpackYear(packedDate),
                    datePacker.unpackMonth(packedDate),
                    datePacker.unpackDay(packedDate),
                    timePacker.unpackHour(packedTime),
                    timePacker.unpackMinute(packedTime),
                    timePacker.unpackSecond(packedTime)
            );
        }

        default long toEpochMilli(final int year, final int month, final int day) {
            return toEpochDay(year, month, day) * TimeFactors.MILLIS_PER_DAY;
        }

        default long toEpochMilli(final int year, final int month, final int day,
                                  final int hour, final int minute, final int second, final int milli) {
            if (TimeValidator.INVALID == validationMethod().timeValidator().validateTimeWithMillis(hour, minute, second, milli)) {
                return INVALID_EPOCH;
            }
            return toEpochMilli(year, month, day)
                    + hour * TimeFactors.MILLIS_PER_HOUR + minute * TimeFactors.MILLIS_PER_MINUTE + second * TimeFactors.MILLIS_PER_SECOND + milli;
        }

        default long toEpochMilli(final int packedDate, final DatePacker datePacker) {
            return toEpochDay(packedDate, datePacker) * TimeFactors.MILLIS_PER_DAY;
        }

        default long toEpochMilli(final int packedDate, final DatePacker datePacker,
                                  final int packedMilliTime, final MilliTimePacker milliTimePacker) {
            return toEpochMilli(
                    datePacker.unpackYear(packedDate),
                    datePacker.unpackMonth(packedDate),
                    datePacker.unpackDay(packedDate),
                    milliTimePacker.unpackHour(packedMilliTime),
                    milliTimePacker.unpackMinute(packedMilliTime),
                    milliTimePacker.unpackSecond(packedMilliTime),
                    milliTimePacker.unpackMilli(packedMilliTime)
            );
        }

        default long toEpochMilli(final long packedDateTime, final DateTimePacker dateTimePacker) {
            return toEpochMilli(
                    dateTimePacker.unpackYear(packedDateTime),
                    dateTimePacker.unpackMonth(packedDateTime),
                    dateTimePacker.unpackDay(packedDateTime),
                    dateTimePacker.unpackHour(packedDateTime),
                    dateTimePacker.unpackMinute(packedDateTime),
                    dateTimePacker.unpackSecond(packedDateTime),
                    dateTimePacker.unpackMilli(packedDateTime)
            );
        }

        default long toEpochNano(final int year, final int month, final int day) {
            return toEpochDay(year, month, day) * TimeFactors.NANOS_PER_DAY;
        }

        default long toEpochNano(final int year, final int month, final int day,
                                 final int hour, final int minute, final int second, final int nano) {
            if (TimeValidator.INVALID == validationMethod().timeValidator().validateTimeWithNanos(hour, minute, second, nano)) {
                return INVALID_EPOCH;
            }
            return toEpochNano(year, month, day)
                    + hour * TimeFactors.NANOS_PER_HOUR + minute * TimeFactors.NANOS_PER_MINUTE + second * TimeFactors.NANOS_PER_SECOND + nano;
        }

        default long toEpochNano(final int packedDate, final DatePacker datePacker,
                                 final long packedNanoTime, final NanoTimePacker nanoTimePacker) {
            return toEpochNano(
                    datePacker.unpackYear(packedDate),
                    datePacker.unpackMonth(packedDate),
                    datePacker.unpackDay(packedDate),
                    nanoTimePacker.unpackHour(packedNanoTime),
                    nanoTimePacker.unpackMinute(packedNanoTime),
                    nanoTimePacker.unpackSecond(packedNanoTime),
                    nanoTimePacker.unpackNano(packedNanoTime)
            );
        }

        default int fromEpochSecond(final long secondsSinceEpoch, final DatePacker datePacker) {
            return fromEpochDay(Math.floorDiv(secondsSinceEpoch, TimeFactors.SECONDS_PER_DAY), datePacker);
        }

        default int fromEpochSecond(final long secondsSinceEpoch, final TimePacker timePacker) {
            final int timeInSeconds = (int) Math.floorMod(secondsSinceEpoch, TimeFactors.SECONDS_PER_DAY);
            return timePacker.pack(
                    divMod(timeInSeconds, TimeFactors.SECONDS_PER_HOUR, TimeFactors.HOURS_PER_DAY),
                    divMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE, TimeFactors.MINUTES_PER_HOUR),
                    Math.floorMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE)
            );
        }

        default int fromEpochSecond(final long secondsSinceEpoch, final MilliTimePacker milliTimePacker) {
            final int timeInSeconds = (int) Math.floorMod(secondsSinceEpoch, TimeFactors.SECONDS_PER_DAY);
            return milliTimePacker.pack(
                    divMod(timeInSeconds, TimeFactors.SECONDS_PER_HOUR, TimeFactors.HOURS_PER_DAY),
                    divMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE, TimeFactors.MINUTES_PER_HOUR),
                    Math.floorMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE),
                    0
            );
        }

        default long fromEpochSecond(final long secondsSinceEpoch, final NanoTimePacker nanoTimePacker) {
            final int timeInSeconds = (int) Math.floorMod(secondsSinceEpoch, TimeFactors.SECONDS_PER_DAY);
            return nanoTimePacker.pack(
                    divMod(timeInSeconds, TimeFactors.SECONDS_PER_HOUR, TimeFactors.HOURS_PER_DAY),
                    divMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE, TimeFactors.MINUTES_PER_HOUR),
                    Math.floorMod(timeInSeconds, TimeFactors.SECONDS_PER_MINUTE),
                    0
            );
        }

        default long fromEpochSecond(final long secondsSinceEpoch, final DateTimePacker dateTimePacker) {
            final DatePacker datePacker = DatePacker.valueOf(Packing.BINARY, dateTimePacker.validationMethod());
            final int packedDate = fromEpochSecond(secondsSinceEpoch, datePacker);
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

        default int fromEpochMilli(final long millisSinceEpoch, final DatePacker datePacker) {
            return fromEpochDay(Math.floorDiv(millisSinceEpoch, TimeFactors.MILLIS_PER_DAY), datePacker);
        }

        default int fromEpochMilli(final long millisSinceEpoch, final MilliTimePacker milliTimePacker) {
            final int timeInMillis = (int) Math.floorMod(millisSinceEpoch, TimeFactors.MILLIS_PER_DAY);
            return milliTimePacker.pack(
                    divMod(timeInMillis, TimeFactors.MILLIS_PER_HOUR, TimeFactors.HOURS_PER_DAY),
                    divMod(timeInMillis, TimeFactors.MILLIS_PER_MINUTE, TimeFactors.MINUTES_PER_HOUR),
                    divMod(timeInMillis, TimeFactors.MILLIS_PER_SECOND, TimeFactors.SECONDS_PER_MINUTE),
                    Math.floorMod(timeInMillis, TimeFactors.MILLIS_PER_SECOND)
            );
        }

        default long fromEpochMilli(final long millisSinceEpoch, final NanoTimePacker nanoTimePacker) {
            final int timeInMillis = (int) Math.floorMod(millisSinceEpoch, TimeFactors.MILLIS_PER_DAY);
            return nanoTimePacker.pack(
                    divMod(timeInMillis, TimeFactors.MILLIS_PER_HOUR, TimeFactors.HOURS_PER_DAY),
                    divMod(timeInMillis, TimeFactors.MILLIS_PER_MINUTE, TimeFactors.MINUTES_PER_HOUR),
                    divMod(timeInMillis, TimeFactors.MILLIS_PER_SECOND, TimeFactors.SECONDS_PER_MINUTE),
                    Math.floorMod(timeInMillis, TimeFactors.MILLIS_PER_SECOND) * TimeFactors.NANOS_PER_MILLI
            );
        }

        default long fromEpochMilli(final long millisSinceEpoch, final DateTimePacker dateTimePacker) {
            final DatePacker datePacker = DatePacker.valueOf(Packing.BINARY, dateTimePacker.validationMethod());
            final int packedDate = fromEpochMilli(millisSinceEpoch, datePacker);
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

        default long fromEpochNano(final long nanosSinceEpoch, final NanoTimePacker nanoTimePacker) {
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
