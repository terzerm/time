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

import static org.tools4j.time.TimeFactors.NANOS_PER_SECOND;
import static org.tools4j.time.TimeValidator.*;

public interface NanoTimePacker {
    long pack(int hour, int minute, int second, int nano);
    int unpackHour(long packed);
    int unpackMinute(long packed);
    int unpackSecond(long packed);
    int unpackNano(long packed);
    Packing packing();

    default long packNull() {
        return -1L;
    }
    default boolean unpackNull(long packed) {
        return packed == -1L;
    }

    default long pack(final LocalTime localTime) {
        return pack(localTime.getHour(), localTime.getMinute(), localTime.getSecond(), localTime.getNano());
    }

    default LocalTime unpackLocalTime(final long packed) {
        return LocalTime.of(unpackHour(packed), unpackMinute(packed), unpackSecond(packed), unpackNano(packed));
    }

    default long packMillisSinceEpoch(final long millisSinceEpoch) {
        return Epoch.fromEpochMillis(millisSinceEpoch, this);
    }

    default long packNanosSinceEpoch(final long nanosSinceEpoch) {
        return Epoch.fromEpochNanos(nanosSinceEpoch, this);
    }

    static NanoTimePacker forPacking(final Packing packing) {
        return packing == Packing.BINARY ? BINARY : DECIMAL;
    }

    NanoTimePacker BINARY = new NanoTimePacker() {
        @Override
        public long pack(final int hour, final int minute, final int second, final int nano) {
            checkValidTimeWithNanos(hour, minute, second, nano);
            return ((hour & 0x3fL) << 42) | ((minute & 0x3fL) << 36) | ((second & 0x3fL) << 30) | (nano & 0x3fffffffL);
        }

        @Override
        public int unpackHour(final long packed) {
            return checkValidHour((int)(packed >>> 42));
        }

        @Override
        public int unpackMinute(final long packed) {
            return checkValidMinute((int)((packed >>> 36) & 0x3f));
        }

        @Override
        public int unpackSecond(final long packed) {
            return checkValidSecond((int)((packed >>> 30) & 0x3f));
        }

        @Override
        public int unpackNano(final long packed) {
            return checkValidNano((int)(packed & 0x3fffffff));
        }

        @Override
        public Packing packing() {
            return Packing.BINARY;
        }

        @Override
        public String toString() {
            return "NanoTimePacker.BINARY";
        }
    };

    NanoTimePacker DECIMAL = new NanoTimePacker() {
        @Override
        public long pack(final int hour, final int minute, final int second, final int nano) {
            checkValidTimeWithNanos(hour, minute, second, nano);
            return hour * (10000L * NANOS_PER_SECOND) + minute * (100L * NANOS_PER_SECOND) +
                    second * ((long)NANOS_PER_SECOND) + nano;
        }

        @Override
        public int unpackHour(final long packed) {
            return checkValidHour((int)(packed / (10000L * NANOS_PER_SECOND)));
        }

        @Override
        public int unpackMinute(final long packed) {
            return checkValidMinute((int)((packed / (100L * NANOS_PER_SECOND)) % 100));
        }

        @Override
        public int unpackSecond(final long packed) {
            return checkValidSecond((int)((packed / NANOS_PER_SECOND) % 100));
        }

        @Override
        public int unpackNano(final long packed) {
            return checkValidNano((int)(packed % NANOS_PER_SECOND));
        }

        @Override
        public Packing packing() {
            return Packing.DECIMAL;
        }

        @Override
        public String toString() {
            return "NanoTimePacker.DECIMAL";
        }
    };
}
