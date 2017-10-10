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
package org.tools4j.time.pack;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tools4j.spockito.Spockito;
import org.tools4j.time.base.TimeFactors;
import org.tools4j.time.validate.ValidationMethod;

import java.time.*;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.*;
import static org.tools4j.time.validate.ValidationMethod.INVALIDATE_RESULT;
import static org.tools4j.time.validate.ValidationMethod.THROW_EXCEPTION;

/**
 * Unit test for {@link DateTimePacker}.
 */
public class DateTimePackerTest {

    private static final DateTimePacker[] PACKERS = initPackers();

    @RunWith(Spockito.class)
    @Spockito.Unroll({
            "|  localDate |  localTime   |",
            "| 2017-01-01 | 00:00:00.000 |",
            "| 2017-01-31 | 23:59:59.999 |",
            "| 2017-02-28 | 01:01:01.111 |",
            "| 2017-03-31 | 10:11:12.123 |",
            "| 2017-04-30 | 11:59:59.999 |",
            "| 2017-05-31 | 12:59:59.999 |",
            "| 2017-06-30 | 12:34:56.789 |",
            "| 2017-07-31 | 00:00:00.000 |",
            "| 2017-08-31 | 23:59:59.999 |",
            "| 2017-09-30 | 01:01:01.111 |",
            "| 2017-10-31 | 10:11:12.123 |",
            "| 2017-11-30 | 11:59:59.999 |",
            "| 2017-12-31 | 12:59:59.999 |",
            "| 2017-12-31 | 12:34:56.789 |",
            "| 2016-02-29 | 00:00:00.000 |",
            "| 2000-02-29 | 23:59:59.999 |",
            "| 1900-02-28 | 01:01:01.111 |",
            "| 1970-01-01 | 10:11:12.123 |",
            "| 1970-01-02 | 11:59:59.999 |",
            "| 1969-12-31 | 12:59:59.999 |",
            "| 1969-12-30 | 12:34:56.789 |",
            "| 1969-04-30 | 00:00:00.000 |",
            "| 1968-02-28 | 23:59:59.999 |",
            "| 1600-02-29 | 01:01:01.111 |",
            "| 0004-02-29 | 10:11:12.123 |",
            "| 0100-02-28 | 11:59:59.999 |",
            "| 0400-02-29 | 12:59:59.999 |",
            "| 0001-01-01 | 00:00:00.000 |",
            "| 9999-12-31 | 23:59:59.999 |",
    })
    public static class Valid {

        private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
        private static final DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        private static final DateTimeFormatter YYYYMMDDHHMMSSMMM = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

        @Test
        public void packDecimal(final LocalDate localDate, final LocalTime localTime) throws Exception {
            final LocalDateTime localDateTime = localDate.atTime(localTime);
            final long packedDateOnly = DateTimePacker.DECIMAL.pack(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
            final long packedDateTimeWithMillis = DateTimePacker.DECIMAL.pack(localDate, localTime);
            final long packedDateTimeNoMillis = DateTimePacker.DECIMAL.pack(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(),
                    localTime.getHour(), localTime.getMinute(), localTime.getSecond());
            assertEquals(Long.parseLong(localDateTime.format(YYYYMMDD)) * 100_00_00_000L, packedDateOnly);
            assertEquals(Long.parseLong(localDateTime.format(YYYYMMDDHHMMSS)) * 1000L, packedDateTimeNoMillis);
            assertEquals(Long.parseLong(localDateTime.format(YYYYMMDDHHMMSSMMM)), packedDateTimeWithMillis);
        }

        @Test
        public void packBinary(final LocalDate localDate, final LocalTime localTime) throws Exception {
            final long packed = DateTimePacker.BINARY.pack(localDate, localTime);
            final long datePart = (localDate.getYear() << 9) | (localDate.getMonthValue() << 5) | localDate.getDayOfMonth();
            final long timePart = (localTime.getHour() << 22) | (localTime.getMinute() << 16) | (localTime.getSecond() << 10) | (localTime.getNano() / TimeFactors.NANOS_PER_MILLI);
            final long expected = (datePart << 27) | timePart;
            assertEquals(expected, packed);
        }

        @Test
        public void packAndUnpackLocalDateTime(final LocalDate localDate, final LocalTime localTime) throws Exception {
            final LocalDateTime expected = localDate.atTime(localTime);
            for (final DateTimePacker packer : PACKERS) {
                final long packed = packer.pack(localDate, localTime);
                final LocalDateTime unpacked = packer.unpackLocalDateTime(packed);
                assertEquals(packer + ": " + localDate + " " + localTime + " -> " + packed, expected, unpacked);
            }
        }

        @Test
        public void packAndUnpackYearMonthDay(final LocalDate localDate) throws Exception {
            for (final DateTimePacker packer : PACKERS) {
                final long packed = packer.pack(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
                final int year = packer.unpackYear(packed);
                final int month = packer.unpackMonth(packed);
                final int day = packer.unpackDay(packed);
                assertEquals(packer + ": " + localDate + " -> " + packed + " [y]", localDate.getYear(), year);
                assertEquals(packer + ": " + localDate + " -> " + packed + " [m]", localDate.getMonthValue(), month);
                assertEquals(packer + ": " + localDate + " -> " + packed + " [d]", localDate.getDayOfMonth(), day);
            }
        }

        @Test
        public void packAndUnpackYearMonthDayHourMinuteSecond(final LocalDate localDate, final LocalTime localTime) throws Exception {
            for (final DateTimePacker packer : PACKERS) {
                final long packed = packer.pack(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(),
                        localTime.getHour(), localTime.getMinute(), localTime.getSecond());
                final int year = packer.unpackYear(packed);
                final int month = packer.unpackMonth(packed);
                final int day = packer.unpackDay(packed);
                final int hour = packer.unpackHour(packed);
                final int minute = packer.unpackMinute(packed);
                final int second = packer.unpackSecond(packed);
                assertEquals(packer + ": " + localDate + " " + localTime + " -> " + packed + " [y]", localDate.getYear(), year);
                assertEquals(packer + ": " + localDate + " " + localTime + " -> " + packed + " [m]", localDate.getMonthValue(), month);
                assertEquals(packer + ": " + localDate + " " + localTime + " -> " + packed + " [d]", localDate.getDayOfMonth(), day);
                assertEquals(packer + ": " + localDate + " " + localTime + " -> " + packed + " [h]", localTime.getHour(), hour);
                assertEquals(packer + ": " + localDate + " " + localTime + " -> " + packed + " [m]", localTime.getMinute(), minute);
                assertEquals(packer + ": " + localDate + " " + localTime + " -> " + packed + " [s]", localTime.getSecond(), second);
            }
        }

        @Test
        public void packAndUnpackYearMonthDayHourMinuteSecondMilli(final LocalDate localDate, final LocalTime localTime) throws Exception {
            for (final DateTimePacker packer : PACKERS) {
                final long packed = packer.pack(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(),
                        localTime.getHour(), localTime.getMinute(), localTime.getSecond(), localTime.getNano() / TimeFactors.NANOS_PER_MILLI);
                final int year = packer.unpackYear(packed);
                final int month = packer.unpackMonth(packed);
                final int day = packer.unpackDay(packed);
                final int hour = packer.unpackHour(packed);
                final int minute = packer.unpackMinute(packed);
                final int second = packer.unpackSecond(packed);
                final int milli = packer.unpackMilli(packed);
                assertEquals(packer + ": " + localDate + " " + localTime + " -> " + packed + " [y]", localDate.getYear(), year);
                assertEquals(packer + ": " + localDate + " " + localTime + " -> " + packed + " [m]", localDate.getMonthValue(), month);
                assertEquals(packer + ": " + localDate + " " + localTime + " -> " + packed + " [d]", localDate.getDayOfMonth(), day);
                assertEquals(packer + ": " + localDate + " " + localTime + " -> " + packed + " [h]", localTime.getHour(), hour);
                assertEquals(packer + ": " + localDate + " " + localTime + " -> " + packed + " [m]", localTime.getMinute(), minute);
                assertEquals(packer + ": " + localDate + " " + localTime + " -> " + packed + " [s]", localTime.getSecond(), second);
                assertEquals(packer + ": " + localDate + " " + localTime + " -> " + packed + " [S]", localTime.getNano() / TimeFactors.NANOS_PER_MILLI, milli);
            }
        }

        @Test
        public void packMillisSinceEpoch(final LocalDate localDate, final LocalTime localTime) throws Exception {
            for (final DateTimePacker packer : PACKERS) {
                final long packed = packer.packMillisSinceEpoch(localDate.atTime(localTime).toInstant(ZoneOffset.UTC).toEpochMilli());
                final LocalDateTime localDateTime = packer.unpackLocalDateTime(packed);
                assertEquals(packer + ": " + localDate + " " + localTime + " -> " + packed, localDate, localDateTime.toLocalDate());
                assertEquals(packer + ": " + localDate + " " + localTime + " -> " + packed, localTime, localDateTime.toLocalTime());
            }
        }
    }

    @RunWith(Spockito.class)
    @Spockito.Unroll({
            "|  year | month | day | hour | minute | second | milli |",
            "|     0 |    1  |   1 |    0 |     0  |      0 |     0 |",
            "|    -1 |    1  |   1 |    0 |     0  |      0 |     0 |",
            "| 10000 |    1  |   1 |    0 |     0  |      0 |     0 |",
            "|  2017 |    0  |   1 |    0 |     0  |      0 |     0 |",
            "|  2017 |   -1  |   1 |    0 |     0  |      0 |     0 |",
            "|  2017 |   13  |   1 |    0 |     0  |      0 |     0 |",
            "|  2017 |    1  |   0 |    0 |     0  |      0 |     0 |",
            "|  2017 |    4  |  -1 |    0 |     0  |      0 |     0 |",//NOTE: day=-1 is equivalent to day=31
            "|  2017 |    1  |  32 |    0 |     0  |      0 |     0 |",
            "|  2017 |    2  |  29 |    0 |     0  |      0 |     0 |",
            "|  2016 |    2  |  30 |    0 |     0  |      0 |     0 |",
            "|  2000 |    2  |  30 |    0 |     0  |      0 |     0 |",
            "|  1900 |    2  |  29 |    0 |     0  |      0 |     0 |",
            "|  1900 |    4  |  31 |    0 |     0  |      0 |     0 |",
            "|  1900 |    6  |  31 |    0 |     0  |      0 |     0 |",
            "|  1900 |    9  |  31 |    0 |     0  |      0 |     0 |",
            "|  1900 |   11  |  31 |    0 |     0  |      0 |     0 |",
            "|  2017 |    1  |   1 |   -1 |     1  |      1 |     0 |",
            "|  2017 |    1  |   1 |    0 |    -1  |      1 |     0 |",
            "|  2017 |    1  |   1 |    0 |     0  |     -1 |     0 |",
            "|  2017 |    1  |   1 |    0 |     0  |      0 |    -1 |",
            "|  2017 |    1  |   1 |   24 |     0  |      1 |     0 |",
            "|  2017 |    1  |   1 |    0 |    60  |      1 |     0 |",
            "|  2017 |    1  |   1 |    0 |     1  |     60 |     0 |",
    })
    @Spockito.Name("[{row}]: {year}/{month}/{day} {hour}:{minute}:{second}.{milli}")
    public static class Invalid {
        @Test(expected = DateTimeException.class)
        public void packIllegalYearMonthDayHourMinSecMilliBinary(final int year, final int month, final int day,
                                                                 final int hour, final int minute, final int second, final int milli) {
            DateTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).pack(year, month, day, hour, minute, second, milli);
        }

        @Test
        public void packInvalidYearMonthDayHourMinSecMilliBinary(final int year, final int month, final int day,
                                                                 final int hour, final int minute, final int second, final int milli) {
            final long packed = DateTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).pack(year, month, day, hour, minute, second, milli);
            assertEquals("should be invalid", DateTimePacker.INVALID, packed);
        }

        @Test(expected = DateTimeException.class)
        public void packIllegalYearMonthDayHourMinSecMilliDecimal(final int year, final int month, final int day,
                                                                  final int hour, final int minute, final int second, final int milli) {
            DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).pack(year, month, day, hour, minute, second, milli);
        }

        @Test
        public void packInvalidYearMonthDayHourMinSecMilliDecimal(final int year, final int month, final int day,
                                                                  final int hour, final int minute, final int second, final int milli) {
            final long packed = DateTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).pack(year, month, day, hour, minute, second, milli);
            assertEquals("should be invalid", DateTimePacker.INVALID, packed);
        }

        @Test(expected = DateTimeException.class)
        public void unpackIllegalYearMonthDayHourMinSecMilliBinary(final int year, final int month, final int day,
                                                                   final int hour, final int minute, final int second, final int milli) {
            final long packed = DateTimePacker.BINARY.pack(year, month, day, hour, minute, second, milli);
            assertNotEquals("should not be invalid", DateTimePacker.INVALID, packed);
            DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackDay(packed);
            DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackHour(packed);
            DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackMinute(packed);
            DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackSecond(packed);
            DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackMilli(packed);
        }

        @Test
        public void unpackInvalidYearMonthDayHourMinSecMilliBinary(final int year, final int month, final int day,
                                                                   final int hour, final int minute, final int second, final int milli) {
            final long packed = DateTimePacker.BINARY.pack(year, month, day, hour, minute, second, milli);
            assertNotEquals("should not be invalid", DateTimePacker.INVALID, packed);
            final int d = DateTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackDay(packed);
            final int h = DateTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackHour(packed);
            final int m = DateTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackMinute(packed);
            final int s = DateTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackSecond(packed);
            final int l = DateTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackMilli(packed);
            final int inv = MilliTimePacker.INVALID;
            assertTrue("at least one should be invalid", d == inv || h == inv || m == inv || s == inv || l == inv);
        }

        @Test(expected = DateTimeException.class)
        public void unpackIllegalYearMonthDayHourMinSecMilliDecimal(final int year, final int month, final int day,
                                                                    final int hour, final int minute, final int second, final int milli) {
            final long packed = DateTimePacker.DECIMAL.pack(year, month, day, hour, minute, second, milli);
            DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackDay(packed);
            DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackHour(packed);
            DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackMinute(packed);
            DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackSecond(packed);
            DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackMilli(packed);
        }

        @Test
        public void unpackInvalidYearMonthDayourMinSecMilliDecimal(final int year, final int month, final int day,
                                                                   final int hour, final int minute, final int second, final int milli) {
            final long packed = DateTimePacker.DECIMAL.pack(year, month, day, hour, minute, second, milli);
            assertNotEquals("should not be invalid", DateTimePacker.INVALID, packed);
            final int d = DateTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackDay(packed);
            final int h = DateTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackHour(packed);
            final int m = DateTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackMinute(packed);
            final int s = DateTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackSecond(packed);
            final int l = DateTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackMilli(packed);
            final int inv = MilliTimePacker.INVALID;
            assertTrue("at least one should be invalid", d == inv || h == inv || m == inv || s == inv || l == inv);
        }

        @Test(expected = DateTimeException.class)
        public void unpackIllegalLocalDateTimeBinary(final int year, final int month, final int day,
                                                     final int hour, final int minute, final int second, final int milli) {
            final long packed = DateTimePacker.BINARY.pack(year, month, day, hour, minute, second, milli);
            assertNotEquals("should not be invalid", DateTimePacker.INVALID, packed);
            DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackLocalDateTime(packed);
        }

        @Test(expected = DateTimeException.class)
        public void unpackInvalidLocalDateTimeBinary(final int year, final int month, final int day,
                                                     final int hour, final int minute, final int second, final int milli) {
            final long packed = DateTimePacker.BINARY.pack(year, month, day, hour, minute, second, milli);
            assertNotEquals("should not be invalid", DateTimePacker.INVALID, packed);
            DateTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackLocalDateTime(packed);
        }

        @Test(expected = DateTimeException.class)
        public void unpackIllegalLocalDateTimeDecimal(final int year, final int month, final int day,
                                                      final int hour, final int minute, final int second, final int milli) {
            final long packed = DateTimePacker.DECIMAL.pack(year, month, day, hour, minute, second, milli);
            DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackLocalDateTime(packed);
        }

        @Test(expected = DateTimeException.class)
        public void unpackInvalidLocalDateTimeDecimal(final int year, final int month, final int day,
                                                      final int hour, final int minute, final int second, final int milli) {
            final long packed = DateTimePacker.DECIMAL.pack(year, month, day, hour, minute, second, milli);
            assertNotEquals("should not be invalid", DateTimePacker.INVALID, packed);
            DateTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackLocalDateTime(packed);
        }
    }

    @RunWith(Spockito.class)
    @Spockito.Unroll({
            "| packing |",
            "|  BINARY |",
            "| DECIMAL |",
    })
    @Spockito.UseValueConverter
    public static class Special {
        @Test
        public void packAndUnpackNull(final Packing packing) throws Exception {
            final DateTimePacker packer = DateTimePacker.valueOf(packing);
            final long packed1 = packer.packNull();
            final long packed2 = packer.pack(null);
            final boolean isNull1 = packer.unpackNull(packed1);
            final boolean isNull2 = packer.unpackNull(packed2);
            assertEquals(packer + ".packNull()", DateTimePacker.NULL, packed1);
            assertEquals(packer + ".pack(null)", DateTimePacker.NULL, packed2);
            assertTrue(packer + ":unpackNull(packNull())", isNull1);
            assertTrue(packer + ":unpackNull(pack(null))", isNull2);
        }

        @Test
        public void packing(final Packing packing) throws Exception {
            final DateTimePacker packer = DateTimePacker.valueOf(packing);
            assertEquals(packing, packer.packing());
            assertEquals(packer, DateTimePacker.class.getField(packing.name()).get(null));
            assertEquals(DateTimePacker.class.getSimpleName() + "." + packing, packer.toString());
        }
    }

    private static DateTimePacker[] initPackers() {
        final DateTimePacker[] packers = new DateTimePacker[Packing.values().length * ValidationMethod.values().length];
        int index = 0;
        for (final Packing packing : Packing.values()) {
            for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                packers[index++] = DateTimePacker.valueOf(packing, validationMethod);
            }
        }
        return packers;
    }
}