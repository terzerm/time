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
import java.util.Objects;

import static org.tools4j.time.TimeFactors.NANOS_PER_SECOND;

public interface NanoTimePacker {
    long INVALID = -1;
    long NULL = Long.MAX_VALUE;
    Packing packing();
    ValidationMethod validationMethod();
    @Garbage(Garbage.Type.RESULT)
    NanoTimePacker forValidationMethod(ValidationMethod validationMethod);
    long pack(int hour, int minute, int second, int nano);
    int unpackHour(long packed);
    int unpackMinute(long packed);
    int unpackSecond(long packed);
    int unpackNano(long packed);
    long packNull();
    boolean unpackNull(long packed);
    long pack(LocalTime localTime);
    @Garbage(Garbage.Type.RESULT)
    LocalTime unpackLocalTime(long packed);
    long packMillisSinceEpoch(long millisSinceEpoch);
    long packNanosSinceEpoch(long nanosSinceEpoch);

    interface Default extends NanoTimePacker {
        @Override
        default long packNull() {
            return NULL;
        }

        @Override
        default boolean unpackNull(final long packed) {
            return packed == NULL;
        }

        @Override
        default long pack(final LocalTime localTime) {
            return localTime == null ? packNull() :
                    pack(localTime.getHour(), localTime.getMinute(), localTime.getSecond(), localTime.getNano());
        }

        @Override
        @Garbage(Garbage.Type.RESULT)
        default LocalTime unpackLocalTime(final long packed) {
            return unpackNull(packed) ? null :
                    LocalTime.of(unpackHour(packed), unpackMinute(packed), unpackSecond(packed), unpackNano(packed));
        }

        @Override
        default long packMillisSinceEpoch(final long millisSinceEpoch) {
            return Epoch.fromEpochMillis(millisSinceEpoch, this);
        }

        @Override
        default long packNanosSinceEpoch(final long nanosSinceEpoch) {
            return Epoch.fromEpochNanos(nanosSinceEpoch, this);
        }

        @Override
        @Garbage(Garbage.Type.RESULT)
        default NanoTimePacker forValidationMethod(final ValidationMethod validationMethod) {
            return valueOf(packing(), validationMethod);
        }
    }

    NanoTimePacker BINARY = new Default() {
        @Override
        public Packing packing() {
            return Packing.BINARY;
        }

        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
        }

        @Override
        public long pack(final int hour, final int minute, final int second, final int nano) {
            return (((long)hour) << 42) | (((long)minute) << 36) | (((long)second) << 30) | nano;
        }

        @Override
        public int unpackHour(final long packed) {
            return (int)(packed >>> 42);
        }

        @Override
        public int unpackMinute(final long packed) {
            return (int)((packed >>> 36) & 0x3fL);
        }

        @Override
        public int unpackSecond(final long packed) {
            return (int)((packed >> 30) & 0x3fL);
        }

        @Override
        public int unpackNano(final long packed) {
            return (int)(packed & 0x3fffffffL);
        }

        @Override
        public String toString() {
            return "NanoTimePacker.BINARY";
        }
    };

    NanoTimePacker DECIMAL = new Default() {
        @Override
        public Packing packing() {
            return Packing.DECIMAL;
        }

        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
        }

        @Override
        public long pack(final int hour, final int minute, final int second, final int nano) {
            return hour * (10000L * NANOS_PER_SECOND) + minute * (100L * NANOS_PER_SECOND) + second * ((long)NANOS_PER_SECOND) + nano;
        }

        @Override
        public int unpackHour(final long packed) {
            return (int)(packed / (10000L * NANOS_PER_SECOND));
        }

        @Override
        public int unpackMinute(final long packed) {
            return (int)((packed / (100L * NANOS_PER_SECOND)) % 100L);
        }

        @Override
        public int unpackSecond(final long packed) {
            return (int)((packed / NANOS_PER_SECOND) % 100L);
        }

        @Override
        public int unpackNano(final long packed) {
            return (int)(packed % NANOS_PER_SECOND);
        }

        @Override
        public String toString() {
            return "NanoTimePacker.DECIMAL";
        }
    };

    class Validated implements Default {
        private final NanoTimePacker packer;
        private final TimeValidator validator;

        public Validated(final NanoTimePacker packer, final ValidationMethod validationMethod) {
            this(packer, TimeValidator.valueOf(validationMethod));
        }

        public Validated(final NanoTimePacker packer, final TimeValidator validator) {
            this.packer = Objects.requireNonNull(packer);
            this.validator = Objects.requireNonNull(validator);
        }

        @Override
        public Packing packing() {
            return packer.packing();
        }

        @Override
        public ValidationMethod validationMethod() {
            return validator.validationMethod();
        }

        @Override
        public long pack(final int hour, final int minute, final int second, final int nano) {
            if (validator.validateTimeWithNanos(hour, minute, second, nano) != TimeValidator.INVALID) {
                return packer.pack(hour, minute, second, nano);
            }
            return INVALID;
        }

        @Override
        public int unpackHour(final long packed) {
            return validator.validateHour(packer.unpackHour(packed));
        }

        @Override
        public int unpackMinute(final long packed) {
            return validator.validateMinute(packer.unpackMinute(packed));
        }

        @Override
        public int unpackSecond(final long packed) {
            return validator.validateSecond(packer.unpackSecond(packed));
        }

        @Override
        public int unpackNano(final long packed) {
            return validator.validateNano(packer.unpackNano(packed));
        }

        @Override
        public String toString() {
            return "NanoTimePacker.Validated." + packer.packing();
        }
    }

    static NanoTimePacker valueOf(final Packing packing) {
        return packing == Packing.BINARY ? BINARY : DECIMAL;
    }

    @Garbage(Garbage.Type.RESULT)
    static NanoTimePacker valueOf(final Packing packing, final ValidationMethod validationMethod) {
        switch (validationMethod) {
            case UNVALIDATED:
                return valueOf(packing);
            default:
                return new Validated(valueOf(packing), validationMethod);
        }
    }
}
