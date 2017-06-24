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

import java.time.LocalTime;

import static org.tools4j.time.TimeFactors.MILLIS_PER_SECOND;
import static org.tools4j.time.TimeValidator.*;

public interface TimePacker {
    int pack(int hour, int minute, int second);
    int unpackHour(int packed);
    int unpackMinute(int packed);
    int unpackSecond(int packed);
    Packing packing();

    default int packNull() {
        return -1;
    }

    default boolean unpackNull(final int packed) {
        return packed == -1;
    }

    default int pack(final LocalTime localTime) {
        return pack(localTime.getHour(), localTime.getMinute(), localTime.getSecond());
    }

    default LocalTime unpackLocalTime(final int packed) {
        return LocalTime.of(unpackHour(packed), unpackMinute(packed), unpackSecond(packed));
    }

    default int packSecondsSinceEpoch(final long secondsSinceEpoch) {
        return Epoch.fromEpochSeconds(secondsSinceEpoch, this);
    }

    default int packMillisSinceEpoch(final long millisSinceEpoch) {
        return packSecondsSinceEpoch(millisSinceEpoch / MILLIS_PER_SECOND);
    }

    static TimePacker forPacking(final Packing packing) {
        return packing == Packing.BINARY ? BINARY : DECIMAL;
    }

    TimePacker BINARY = new TimePacker() {
        @Override
        public int pack(final int hour, final int minute, final int second) {
            checkValidTime(hour, minute, second);
            return (hour << 12) | (minute << 6) | second;
        }

        @Override
        public int unpackHour(final int packed) {
            return checkValidHour(packed >>> 12);
        }

        @Override
        public int unpackMinute(final int packed) {
            return checkValidMinute((packed >>> 6) & 0x3f);
        }

        @Override
        public int unpackSecond(final int packed) {
            return checkValidSecond(packed & 0x3f);
        }

        @Override
        public Packing packing() {
            return Packing.BINARY;
        }

        @Override
        public String toString() {
            return "TimePacker.BINARY";
        }
    };

    TimePacker DECIMAL = new TimePacker() {
        @Override
        public int pack(final int hour, final int minute, final int second) {
            checkValidTime(hour, minute, second);
            return hour * 10000 + minute * 100 + second;
        }

        @Override
        public int unpackHour(final int packed) {
            return checkValidHour(packed / 10000);
        }

        @Override
        public int unpackMinute(final int packed) {
            return checkValidMinute((packed / 100) % 100);
        }

        @Override
        public int unpackSecond(final int packed) {
            return checkValidSecond(packed % 100);
        }

        @Override
        public Packing packing() {
            return Packing.DECIMAL;
        }

        @Override
        public String toString() {
            return "TimePacker.DECIMAL";
        }
    };
}
