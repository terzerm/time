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

import static org.tools4j.time.TimeFactors.NANOS_PER_MILLI;
import static org.tools4j.time.TimeValidator.*;

public interface MilliTimePacker {
    int pack(int hour, int minute, int second, int milli);
    int unpackHour(int packed);
    int unpackMinute(int packed);
    int unpackSecond(int packed);
    int unpackMilli(int packed);
    Packing packing();

    default int packNull() {
        return -1;
    }

    default boolean unpackNull(final int packed) {
        return packed == -1;
    }

    default int pack(final LocalTime localTime) {
        return pack(localTime.getHour(), localTime.getMinute(), localTime.getSecond(), localTime.getNano() / NANOS_PER_MILLI);
    }

    default LocalTime unpackLocalTime(final int packed) {
        return LocalTime.of(unpackHour(packed), unpackMinute(packed), unpackSecond(packed), unpackMilli(packed) * NANOS_PER_MILLI);
    }

    default int packMillisSinceEpoch(final long millisSinceEpoch) {
        return Epoch.fromEpochMillis(millisSinceEpoch, this);
    }

    static MilliTimePacker forPacking(final Packing packing) {
        return packing == Packing.BINARY ? BINARY : DECIMAL;
    }

    MilliTimePacker BINARY = new MilliTimePacker() {
        @Override
        public int pack(final int hour, final int minute, final int second, final int milli) {
            checkValidTimeWithMillis(hour, minute, second, milli);
            return (hour << 22) | (minute << 16) | (second << 10) | milli;
        }

        @Override
        public int unpackHour(final int packed) {
            return checkValidHour(packed >>> 22);
        }

        @Override
        public int unpackMinute(final int packed) {
            return checkValidMinute((packed >>> 16) & 0x3f);
        }

        @Override
        public int unpackSecond(final int packed) {
            return checkValidSecond((packed >> 10) & 0x3f);
        }

        @Override
        public int unpackMilli(final int packed) {
            return checkValidMilli(packed & 0x3ff);
        }

        @Override
        public Packing packing() {
            return Packing.BINARY;
        }

        @Override
        public String toString() {
            return "MilliTimePacker.BINARY";
        }
    };

    MilliTimePacker DECIMAL = new MilliTimePacker() {
        @Override
        public int pack(final int hour, final int minute, final int second, final int milli) {
            checkValidTimeWithMillis(hour, minute, second, milli);
            return hour * 10000000 + minute * 100000 + second * 1000 + milli;
        }

        @Override
        public int unpackHour(final int packed) {
            return checkValidHour(packed / 10000000);
        }

        @Override
        public int unpackMinute(final int packed) {
            return checkValidMinute((packed / 100000) % 60);
        }

        @Override
        public int unpackSecond(final int packed) {
            return checkValidSecond((packed / 1000) % 60);
        }

        @Override
        public int unpackMilli(final int packed) {
            return checkValidMilli(packed % 1000);
        }

        @Override
        public Packing packing() {
            return Packing.DECIMAL;
        }

        @Override
        public String toString() {
            return "MilliTimePacker.DECIMAL";
        }
    };
}
