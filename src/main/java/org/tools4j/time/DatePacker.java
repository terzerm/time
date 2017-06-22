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

import java.time.LocalDate;

import static org.tools4j.time.DateValidator.*;

public interface DatePacker {
    int pack(int year, int month, int day);
    int unpackYear(int packed);
    int unpackMonth(int packed);
    int unpackDay(int packed);
    Packing packing();

    default int packNull() {
        return 0;
    }
    default boolean unpackNull(int packed) {
        return packed == 0;
    }
    default int pack(final LocalDate localDate) {
        return pack(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
    }
    default LocalDate unpackLocalDate(final int packed) {
        return LocalDate.of(unpackYear(packed), unpackMonth(packed), unpackDay(packed));
    }

    default int packDaysSinceEpoch(final long daysSinceEpoch) {
        return Epoch.fromEpochDays(daysSinceEpoch, this);
    }

    default int packMillisSinceEpoch(final long millisSinceEpoch) {
        return Epoch.fromEpochMillis(millisSinceEpoch, this);
    }

    static DatePacker forPacking(final Packing packing) {
        return packing == Packing.BINARY ? BINARY : DECIMAL;
    }

    DatePacker BINARY = new DatePacker() {
        @Override
        public int pack(final int year, final int month, final int day) {
            checkValidDate(year, month, day);
            return (year << 9) | (month << 5) | day;
        }

        @Override
        public int unpackYear(final int packed) {
            return checkValidYear(packed >>> 9);
        }

        @Override
        public int unpackMonth(final int packed) {
            return checkValidMonth((packed >>> 5) & 0xf);
        }

        @Override
        public int unpackDay(final int packed) {
            return checkValidDate(packed >>> 9, (packed >>> 5) & 0xf, packed & 0x1f);
        }

        @Override
        public Packing packing() {
            return Packing.BINARY;
        }

        @Override
        public String toString() {
            return "DatePacker.BINARY";
        }
    };

    DatePacker DECIMAL = new DatePacker() {
        @Override
        public int pack(final int year, final int month, final int day) {
            checkValidDate(year, month, day);
            return (year * 10000) + (month * 100) + day;
        }

        @Override
        public int unpackYear(final int packed) {
            return checkValidYear(packed / 10000);
        }

        @Override
        public int unpackMonth(final int packed) {
            return checkValidMonth((packed / 100) % 100);
        }

        @Override
        public int unpackDay(final int packed) {
            int p = packed;
            final int y = p / 10000;
            p -= y * 10000;
            final int m = p / 100;
            final int d = p - m * 100;
            return checkValidDate(y, m, d);
        }

        @Override
        public Packing packing() {
            return Packing.DECIMAL;
        }

        @Override
        public String toString() {
            return "DatePacker.DECIMAL";
        }
    };
}
