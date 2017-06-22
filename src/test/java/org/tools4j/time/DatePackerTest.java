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
import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link DatePacker}.
 */
public class DatePackerTest {

    private static final DatePacker[] PACKERS = {DatePacker.BINARY, DatePacker.DECIMAL};

    @RunWith(Spockito.class)
    @Spockito.Unroll({
            "|  localDate |",
            "| 2017-01-01 |",
            "| 2017-12-31 |",
            "| 0001-01-01 |",
            "| 1970-01-01 |",
            "| 1970-01-02 |",
            "| 1969-12-31 |",
            "| 1969-12-30 |",
            "| 1969-04-30 |",
            "| 1968-02-28 |",
            "| 1600-02-29 |",
            "| 0004-02-29 |",
            "| 0100-02-28 |",
            "| 0400-02-29 |",
    })
    public static class Unroll {
        @Test
        public void packAndUnpackLocalDate(final LocalDate localDate) throws Exception {
            for (final DatePacker packer : PACKERS) {
                final int packed = packer.pack(localDate);
                final LocalDate unpacked = packer.unpackLocalDate(packed);
                assertEquals(packer + ": " + localDate + " -> " + packed, localDate, unpacked);
            }
        }

        @Test
        public void packAndUnpackYearMonthDay(final LocalDate localDate) throws Exception {
            for (final DatePacker packer : PACKERS) {
                final int packed = packer.pack(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
                final int year = packer.unpackYear(packed);
                final int month = packer.unpackMonth(packed);
                final int day = packer.unpackDay(packed);
                assertEquals(packer + ": " + localDate + " -> " + packed + " [y]", localDate.getYear(), year);
                assertEquals(packer + ": " + localDate + " -> " + packed + " [m]", localDate.getMonthValue(), month);
                assertEquals(packer + ": " + localDate + " -> " + packed + " [d]", localDate.getDayOfMonth(), day);
            }
        }

        @Test
        public void packDaysSinceEpoch(final LocalDate localDate) throws Exception {
            for (final DatePacker packer : PACKERS) {
                final int packed = packer.packDaysSinceEpoch(localDate.toEpochDay());
                final LocalDate unpacked = packer.unpackLocalDate(packed);
                assertEquals(packer + ": " + localDate + " -> " + packed, localDate, unpacked);
            }
        }

        @Test
        public void packMillisSinceEpoch(final LocalDate localDate) throws Exception {
            for (final DatePacker packer : PACKERS) {
                final int packed = packer.packMillisSinceEpoch(localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());
                final LocalDate unpacked = packer.unpackLocalDate(packed);
                assertEquals(packer + ": " + localDate + " -> " + packed, localDate, unpacked);
            }
        }
    }

    @Test
    public void packAndUnpackNull() throws Exception {
        for (final DatePacker packer : PACKERS) {
            final int packed = packer.packNull();
            final boolean isNull = packer.unpackNull(packed);
            assertEquals(packer + ": pack null", 0, packed);
            assertTrue(packer + ": unpack null", isNull);
        }
    }

    @Test
    public void packing() throws Exception {
        assertEquals(Packing.BINARY, DatePacker.BINARY.packing());
        assertEquals(Packing.DECIMAL, DatePacker.DECIMAL.packing());
        for (final Packing packing : Packing.values()) {
            final DatePacker packer = DatePacker.forPacking(packing);
            assertEquals(packing, packer.packing());
            assertEquals(DatePacker.class.getSimpleName() + "." + packing, packer.toString());
        }
    }
}