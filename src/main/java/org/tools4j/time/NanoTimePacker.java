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

import static org.tools4j.time.TimeFactors.*;

public interface NanoTimePacker {
    long pack(int hour, int minute, int second, int nano);
    int unpackHour(long packed);
    int unpackMinute(long packed);
    int unpackSecond(long packed);
    int unpackNano(long packed);

    default long packNull() {
        return -1L;
    }
    default boolean unpackNull(long packed) {
        return packed == -1L;
    }

    default long pack(final LocalTime localTime) {
        return pack(localTime.getHour(), localTime.getMinute(), localTime.getSecond(), localTime.getNano());
    }

    default LocalTime unpackLocalTime(final int packed) {
        return LocalTime.of(unpackHour(packed), unpackMinute(packed), unpackSecond(packed), unpackNano(packed));
    }

    default long packMillisSinceEpoch(final long millisSinceEpoch) {
        final long timeInMillis = millisSinceEpoch % MILLIS_PER_DAY;
        return pack(
                (int)((timeInMillis / MILLIS_PER_HOUR) % 24),
                (int)((timeInMillis / MILLIS_PER_MINUTE) % 60),
                (int)((timeInMillis / MILLIS_PER_SECOND) % 60),
                (int)((timeInMillis % 1000) * NANOS_PER_MILLI)
        );
    }

    default long packNanosSinceEpoch(final long nanosSinceEpoch) {
        final long timeInNanos = nanosSinceEpoch % NANOS_PER_DAY;
        return pack(
                (int)((timeInNanos / NANOS_PER_HOUR) % 24),
                (int)((timeInNanos / NANOS_PER_MINUTE) % 60),
                (int)((timeInNanos / NANOS_PER_SECOND) % 60),
                (int)(timeInNanos % NANOS_PER_SECOND)
        );
    }

    static NanoTimePacker forPacking(final Packing packing) {
        return packing == Packing.BINARY ? BINARY : DECIMAL;
    }

    NanoTimePacker BINARY = new NanoTimePacker() {
        @Override
        public long pack(final int hour, final int minute, final int second, final int nano) {
            return ((hour & 0x3fL) << 42) | ((minute & 0x3fL) << 36) | ((second & 0x3fL) << 30) | (nano & 0x3fffffffL);
        }

        @Override
        public int unpackHour(final long packed) {
            return (int)((packed >>> 42) & 0x3f);
        }

        @Override
        public int unpackMinute(final long packed) {
            return (int)((packed >>> 36) & 0x3f);
        }

        @Override
        public int unpackSecond(final long packed) {
            return (int)((packed >>> 30) & 0x3f);
        }

        @Override
        public int unpackNano(final long packed) {
            return (int)(packed & 0x3fffffff);
        }

    };

    NanoTimePacker DECIMAL = new NanoTimePacker() {
        @Override
        public long pack(final int hour, final int minute, final int second, final int nano) {
            return ((hour % 60) * 10000 * NANOS_PER_SECOND) + ((minute % 60) * 100 * NANOS_PER_SECOND) +
                    ((second % 60) * NANOS_PER_SECOND) + (nano % NANOS_PER_SECOND);
        }

        @Override
        public int unpackHour(final long packed) {
            return (int)((packed / (10000 * NANOS_PER_SECOND)) % 60);
        }

        @Override
        public int unpackMinute(final long packed) {
            return (int)((packed / (100 * NANOS_PER_SECOND)) % 60);
        }

        @Override
        public int unpackSecond(final long packed) {
            return (int)((packed / NANOS_PER_SECOND) % 60);
        }

        @Override
        public int unpackNano(final long packed) {
            return (int)(packed % NANOS_PER_SECOND);
        }
    };
}
