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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.tools4j.time.validate.ValidationMethod.INVALIDATE_RESULT;
import static org.tools4j.time.validate.ValidationMethod.THROW_EXCEPTION;

/**
 * Unit test for {@link NanoTimePacker}.
 */
public class NanoTimePackerTest {

    private static final NanoTimePacker[] PACKERS = initPackers();
    private static final LocalDate[] DATES = {LocalDate.of(1931, 1, 1), LocalDate.of(1969, 12, 31), LocalDate.of(1970, 1,1), LocalDate.of(2017,06, 06), LocalDate.of(2036, 12, 31)};

    @RunWith(Spockito.class)
    @Spockito.Unroll({
            "|      localTime      |",
            "|  00:00:00.000000000 |",
            "|  23:59:59.999999999 |",
            "|  01:01:01.111111111 |",
            "|  10:11:12.123456789 |",
            "|  11:59:59.999999999 |",
            "|  12:59:59.999999999 |",
            "|  12:34:56.789012345 |",
    })
    public static class Valid {

        private static final DateTimeFormatter HHMMSSNNNNNNNNN = DateTimeFormatter.ofPattern("HHmmssnnnnnnnnn");

        @Test
        public void packDecimal(final LocalTime localTime) throws Exception {
            final long packed = NanoTimePacker.DECIMAL.pack(localTime);
            assertEquals(Long.parseLong(localTime.format(HHMMSSNNNNNNNNN)), packed);
        }

        @Test
        public void packBinary(final LocalTime localTime) throws Exception {
            final long packed = NanoTimePacker.BINARY.pack(localTime);
            assertEquals((((long)localTime.getHour()) << 42) | (((long)localTime.getMinute()) << 36) | (((long)localTime.getSecond()) << 30) | localTime.getNano(),
                    packed);
        }

        @Test
        public void packAndUnpackLocalTime(final LocalTime localTime) throws Exception {
            for (final NanoTimePacker packer : PACKERS) {
                final long packed = packer.pack(localTime);
                final LocalTime unpacked = packer.unpackLocalTime(packed);
                assertEquals(packer + ": " + localTime + " -> " + packed, localTime, unpacked);
            }
        }

        @Test
        public void packAndUnpackHourMinuteSecondNano(final LocalTime localTime) throws Exception {
            for (final NanoTimePacker packer : PACKERS) {
                final long packed = packer.pack(localTime.getHour(), localTime.getMinute(), localTime.getSecond(), localTime.getNano());
                final int hour = packer.unpackHour(packed);
                final int minute = packer.unpackMinute(packed);
                final int second = packer.unpackSecond(packed);
                final int nano = packer.unpackNano(packed);
                assertEquals(packer + ": " + localTime + " -> " + packed + " [h]", localTime.getHour(), hour);
                assertEquals(packer + ": " + localTime + " -> " + packed + " [m]", localTime.getMinute(), minute);
                assertEquals(packer + ": " + localTime + " -> " + packed + " [s]", localTime.getSecond(), second);
                assertEquals(packer + ": " + localTime + " -> " + packed + " [n]", localTime.getNano(), nano);
            }
        }

        @Test
        public void packEpochNano(final LocalTime localTime) throws Exception {
            for (final LocalDate date : DATES) {
                for (final NanoTimePacker packer : PACKERS) {
                    final Instant instant = localTime.atDate(date).toInstant(ZoneOffset.UTC);
                    final long packed = packer.packEpochNano(instant.toEpochMilli()
                            * TimeFactors.NANOS_PER_MILLI + (instant.getNano() % TimeFactors.NANOS_PER_MILLI));
                    final LocalTime unpacked = packer.unpackLocalTime(packed);
                    assertEquals(packer + ": " + localTime + " -> " + packed, localTime, unpacked);
                }
            }
        }

        @Test
        public void packEpochMilli(final LocalTime localTime) throws Exception {
            final int milliInNanos = (localTime.getNano() / TimeFactors.NANOS_PER_MILLI) * TimeFactors.NANOS_PER_MILLI;
            final LocalTime milliTime = localTime.withNano(milliInNanos);
            for (final LocalDate date : DATES) {
                for (final NanoTimePacker packer : PACKERS) {
                    final long packed = packer.packEpochMilli(localTime.atDate(date).toInstant(ZoneOffset.UTC).toEpochMilli());
                    final LocalTime unpacked = packer.unpackLocalTime(packed);
                    assertEquals(packer + ": " + localTime + " -> " + packed, milliTime, unpacked);
                }
            }
        }
    }

    @RunWith(Spockito.class)
    @Spockito.Unroll({
            "| hour | minute | second |       nano |",
            "|   -1 |     1  |      1 |          0 |",
            "|    0 |    -1  |      1 |          0 |",
            "|    0 |     0  |     -1 |          0 |",
            "|    0 |     0  |      0 |         -2 |",
            "|   24 |     0  |      1 |          0 |",
            "|    0 |    60  |      1 |          0 |",
            "|    0 |     1  |     60 |          0 |",
            "|    0 |     0  |     59 | 1000000000 |",
    })
    @Spockito.Name("[{row}]: {hour}:{minute}:{second}.{nano}")
    public static class Invalid {
        @Test(expected = DateTimeException.class)
        public void packIllegalHourMinuteSecondNanoBinary(final int hour, final int minute, final int second, final int nano) {
            NanoTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).pack(hour, minute, second, nano);
        }

        @Test
        public void packInvalidHourMinuteSecondNanoBinary(final int hour, final int minute, final int second, final int nano) {
            final long packed = NanoTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).pack(hour, minute, second, nano);
            assertEquals("should be invalid", NanoTimePacker.INVALID, packed);
        }

        @Test(expected = DateTimeException.class)
        public void packIllegalHourMinuteSecondNanoDecimal(final int hour, final int minute, final int second, final int nano) {
            NanoTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).pack(hour, minute, second, nano);
        }

        @Test
        public void packInvalidHourMinuteSecondNanoDecimal(final int hour, final int minute, final int second, final int nano) {
            final long packed = NanoTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).pack(hour, minute, second, nano);
            assertEquals("should be invalid", NanoTimePacker.INVALID, packed);
        }

        @Test(expected = DateTimeException.class)
        public void unpackIllegalLocalTimeBinary(final int hour, final int minute, final int second, final int nano) {
            final long packed = NanoTimePacker.BINARY.pack(hour, minute, second, nano);
            assertNotEquals("should not be invalid", NanoTimePacker.INVALID, packed);
            NanoTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackLocalTime(packed);
        }

        @Test(expected = DateTimeException.class)
        public void unpackInvalidLocalTimeBinary(final int hour, final int minute, final int second, final int nano) {
            final long packed = NanoTimePacker.BINARY.pack(hour, minute, second, nano);
            assertNotEquals("should not be invalid", NanoTimePacker.INVALID, packed);
            NanoTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackLocalTime(packed);
        }

        @Test(expected = DateTimeException.class)
        public void unpackIllegalLocalTimeDecimal(final int hour, final int minute, final int second, final int nano) {
            final long packed = NanoTimePacker.DECIMAL.pack(hour, minute, second, nano);
            assertNotEquals("should not be invalid", NanoTimePacker.INVALID, packed);
            NanoTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackLocalTime(packed);
        }

        @Test(expected = DateTimeException.class)
        public void unpackInvalidLocalTimeDecimal(final int hour, final int minute, final int second, final int nano) {
            final long packed = NanoTimePacker.DECIMAL.pack(hour, minute, second, nano);
            assertNotEquals("should not be invalid", NanoTimePacker.INVALID, packed);
            NanoTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackLocalTime(packed);
        }

        @Test(expected = DateTimeException.class)
        public void unpackIllegalHourMinuteSecondMilliBinary(final int hour, final int minute, final int second, final int nano) {
            final long packed = NanoTimePacker.BINARY.pack(hour, minute, second, nano);
            assertNotEquals("should not be invalid", NanoTimePacker.INVALID, packed);
            NanoTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackHour(packed);
            NanoTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackMinute(packed);
            NanoTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackSecond(packed);
            NanoTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackNano(packed);
        }

        @Test
        public void unpackInvalidHourMinuteSecondMilliBinary(final int hour, final int minute, final int second, final int nano) {
            final long packed = NanoTimePacker.BINARY.pack(hour, minute, second, nano);
            assertNotEquals("should not be invalid", NanoTimePacker.INVALID, packed);
            final int h = NanoTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackHour(packed);
            final int m = NanoTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackMinute(packed);
            final int s = NanoTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackSecond(packed);
            final int n = NanoTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackNano(packed);
            final long inv = NanoTimePacker.INVALID;
            assertTrue("at least one should be invalid", h == inv || m == inv || s == inv || n == inv);
        }

        @Test(expected = DateTimeException.class)
        public void unpackIllegalHourMinuteSecondMilliDecimal(final int hour, final int minute, final int second, final int nano) {
            final long packed = NanoTimePacker.DECIMAL.pack(hour, minute, second, nano);
            assertNotEquals("should not be invalid", NanoTimePacker.INVALID, packed);
            NanoTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackHour(packed);
            NanoTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackMinute(packed);
            NanoTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackSecond(packed);
            NanoTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackNano(packed);
        }

        @Test
        public void unpackInvalidHourMinuteSecondMilliDecimal(final int hour, final int minute, final int second, final int nano) {
            final long packed = NanoTimePacker.DECIMAL.pack(hour, minute, second, nano);
            assertNotEquals("should not be invalid", NanoTimePacker.INVALID, packed);
            final int h = NanoTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackHour(packed);
            final int m = NanoTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackMinute(packed);
            final int s = NanoTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackSecond(packed);
            final int l = NanoTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackNano(packed);
            final long inv = NanoTimePacker.INVALID;
            assertTrue("at least one should be invalid", h == inv || m == inv || s == inv || l == inv);
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
            final NanoTimePacker packer = NanoTimePacker.valueOf(packing);
            final long packed = packer.packNull();
            final boolean isNull = packer.unpackNull(packed);
            assertEquals(packer + ": pack null", NanoTimePacker.NULL, packed);
            assertTrue(packer + ": unpack null", isNull);
        }

        @Test
        public void packing(final Packing packing) throws Exception {
            final NanoTimePacker packer = NanoTimePacker.valueOf(packing);
            assertEquals(packing, packer.packing());
            assertEquals(packer, NanoTimePacker.class.getField(packing.name()).get(null));
            assertEquals(NanoTimePacker.class.getSimpleName() + "." + packing, packer.toString());
        }
    }

    private static NanoTimePacker[] initPackers() {
        final NanoTimePacker[] packers = new NanoTimePacker[Packing.values().length * ValidationMethod.values().length];
        int index = 0;
        for (final Packing packing : Packing.values()) {
            for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                packers[index++] = NanoTimePacker.valueOf(packing, validationMethod);
            }
        }
        return packers;
    }
}