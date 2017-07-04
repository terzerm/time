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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tools4j.spockito.Spockito;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link MilliTimePacker}.
 */
public class MilliTimePackerTest {

    private static final MilliTimePacker[] PACKERS = {MilliTimePacker.BINARY, MilliTimePacker.DECIMAL};
    private static final LocalDate[] DATES = {LocalDate.of(1, 1, 1), LocalDate.of(1969, 12, 31), LocalDate.of(1970, 1,1), LocalDate.of(2017,06, 06), LocalDate.of(9999, 12, 31)};

    @RunWith(Spockito.class)
    @Spockito.Unroll({
            "|   localTime   |",
            "|  00:00:00.000 |",
            "|  23:59:59.999 |",
            "|  01:01:01.111 |",
            "|  10:11:12.123 |",
            "|  11:59:59.999 |",
            "|  12:59:59.999 |",
            "|  12:34:56.789 |",
    })
    public static class Valid {

        private static final DateTimeFormatter HHMMSSMMM = DateTimeFormatter.ofPattern("HHmmssSSS");

        @Test
        public void packDecimal(final LocalTime localTime) throws Exception {
            final int packed = MilliTimePacker.DECIMAL.pack(localTime);
            assertEquals(Integer.parseInt(localTime.format(HHMMSSMMM)), packed);
        }

        @Test
        public void packBinary(final LocalTime localTime) throws Exception {
            final int packed = MilliTimePacker.BINARY.pack(localTime);
            assertEquals((localTime.getHour() << 22) | (localTime.getMinute() << 16) | (localTime.getSecond() << 10) | (localTime.getNano() / TimeFactors.NANOS_PER_MILLI),
                    packed);
        }

        @Test
        public void packAndUnpackLocalTime(final LocalTime localTime) throws Exception {
            for (final MilliTimePacker packer : PACKERS) {
                final int packed = packer.pack(localTime);
                final LocalTime unpacked = packer.unpackLocalTime(packed);
                assertEquals(packer + ": " + localTime + " -> " + packed, localTime, unpacked);
            }
        }

        @Test
        public void packAndUnpackHourMinuteSecondMilli(final LocalTime localTime) throws Exception {
            for (final MilliTimePacker packer : PACKERS) {
                final int packed = packer.pack(localTime.getHour(), localTime.getMinute(), localTime.getSecond(), localTime.getNano() / TimeFactors.NANOS_PER_MILLI);
                final int hour = packer.unpackHour(packed);
                final int minute = packer.unpackMinute(packed);
                final int second = packer.unpackSecond(packed);
                final int milli = packer.unpackMilli(packed);
                assertEquals(packer + ": " + localTime + " -> " + packed + " [h]", localTime.getHour(), hour);
                assertEquals(packer + ": " + localTime + " -> " + packed + " [m]", localTime.getMinute(), minute);
                assertEquals(packer + ": " + localTime + " -> " + packed + " [s]", localTime.getSecond(), second);
                assertEquals(packer + ": " + localTime + " -> " + packed + " [S]", localTime.getNano() / TimeFactors.NANOS_PER_MILLI, milli);
            }
        }

        @Test
        public void packMillisSinceEpoch(final LocalTime localTime) throws Exception {
            for (final LocalDate date : DATES) {
                for (final MilliTimePacker packer : PACKERS) {
                    final int packed = packer.packMillisSinceEpoch(localTime.atDate(date).toInstant(ZoneOffset.UTC).toEpochMilli());
                    final LocalTime unpacked = packer.unpackLocalTime(packed);
                    assertEquals(packer + ": " + localTime + " -> " + packed, localTime, unpacked);
                }
            }
        }
    }

    @RunWith(Spockito.class)
    @Spockito.Unroll({
            "| hour | minute | second | milli |",
            "|   -1 |     1  |      1 |     0 |",
            "|    0 |    -1  |      1 |     0 |",
            "|    0 |     0  |     -1 |     0 |",
            "|    0 |     0  |      0 |    -1 |",
            "|   24 |     0  |      1 |     0 |",
            "|    0 |    60  |      1 |     0 |",
            "|    0 |     1  |     60 |     0 |",
            "|    0 |     0  |     59 |  1000 |",
    })
    @Spockito.Name("[{row}]: {hour}:{minute}:{second}.{milli}")
    public static class Invalid {
        @Test(expected = IllegalArgumentException.class)
        public void packIllegalHourMinuteSecondMilliBinary(final int hour, final int minute, final int second, final int milli) {
            MilliTimePacker.BINARY.pack(hour, minute, second, milli);
        }

        @Test(expected = IllegalArgumentException.class)
        public void packIllegalHourMinuteSecondMilliDecimal(final int hour, final int minute, final int second, final int milli) {
            MilliTimePacker.DECIMAL.pack(hour, minute, second, milli);
        }

        @Test(expected = IllegalArgumentException.class)
        public void unpackIllegalHourMinuteSecondMilliBinary(final int hour, final int minute, final int second, final int milli) {
            final int packed = (hour << 22) | (minute << 16) | (second << 10) | milli;
            MilliTimePacker.BINARY.unpackLocalTime(packed);
        }

        @Test(expected = IllegalArgumentException.class)
        public void unpackIllegalHourMinuteSecondMilliDecimal(final int hour, final int minute, final int second, final int milli) {
            final int packed = hour * 10000000 + minute * 100000 + second * 1000 + milli;
            MilliTimePacker.DECIMAL.unpackLocalTime(packed);
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
            final MilliTimePacker packer = MilliTimePacker.forPacking(packing);
            final int packed = packer.packNull();
            final boolean isNull = packer.unpackNull(packed);
            assertEquals(packer + ": pack null", -1, packed);
            assertTrue(packer + ": unpack null", isNull);
        }

        @Test
        public void packing(final Packing packing) throws Exception {
            final MilliTimePacker packer = MilliTimePacker.forPacking(packing);
            assertEquals(packing, packer.packing());
            assertEquals(packer, MilliTimePacker.class.getField(packing.name()).get(null));
            assertEquals(MilliTimePacker.class.getSimpleName() + "." + packing, packer.toString());
        }
    }
}