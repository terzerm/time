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

import org.tools4j.time.base.Epoch;
import org.tools4j.time.base.Garbage;
import org.tools4j.time.validate.TimeValidator;
import org.tools4j.time.validate.ValidationMethod;

import java.time.LocalTime;
import java.util.Objects;

import static org.tools4j.time.base.TimeFactors.MILLIS_PER_SECOND;

public interface TimePacker {
    int INVALID = -1;
    int NULL = Integer.MAX_VALUE;
    Packing packing();
    ValidationMethod validationMethod();
    @Garbage(Garbage.Type.RESULT)
    TimePacker forValidationMethod(ValidationMethod validationMethod);
    int pack(int hour, int minute, int second);
    int unpackHour(int packed);
    int unpackMinute(int packed);
    int unpackSecond(int packed);
    int packNull();
    boolean unpackNull(int packed);
    int pack(LocalTime localTime);
    @Garbage(Garbage.Type.RESULT)
    LocalTime unpackLocalTime(int packed);
    int packSecondsSinceEpoch(long secondsSinceEpoch);
    int packMillisSinceEpoch(long millisSinceEpoch);

    interface Default extends TimePacker {
        @Override
        default int packNull() {
            return NULL;
        }

        @Override
        default boolean unpackNull(final int packed) {
            return packed == NULL;
        }

        @Override
        default int pack(final LocalTime localTime) {
            return localTime == null ? packNull() :
                    pack(localTime.getHour(), localTime.getMinute(), localTime.getSecond());
        }

        @Override
        @Garbage(Garbage.Type.RESULT)
        default LocalTime unpackLocalTime(final int packed) {
            return unpackNull(packed) ? null :
                    LocalTime.of(unpackHour(packed), unpackMinute(packed), unpackSecond(packed));
        }

        @Override
        default int packSecondsSinceEpoch(final long secondsSinceEpoch) {
            return Epoch.fromEpochSeconds(secondsSinceEpoch, this);
        }

        @Override
        default int packMillisSinceEpoch(final long millisSinceEpoch) {
            return packSecondsSinceEpoch(millisSinceEpoch / MILLIS_PER_SECOND);
        }

        @Override
        @Garbage(Garbage.Type.RESULT)
        default TimePacker forValidationMethod(final ValidationMethod validationMethod) {
            return valueOf(packing(), validationMethod);
        }
    }

    TimePacker BINARY = new Default() {
        @Override
        public Packing packing() {
            return Packing.BINARY;
        }

        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
        }

        @Override
        public int pack(final int hour, final int minute, final int second) {
            return (hour << 12) | (minute << 6) | second;
        }

        @Override
        public int unpackHour(final int packed) {
            return packed >>> 12;
        }

        @Override
        public int unpackMinute(final int packed) {
            return (packed >>> 6) & 0x3f;
        }

        @Override
        public int unpackSecond(final int packed) {
            return packed & 0x3f;
        }

        @Override
        public String toString() {
            return "TimePacker.BINARY";
        }
    };

    TimePacker DECIMAL = new Default() {
        @Override
        public Packing packing() {
            return Packing.DECIMAL;
        }

        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
        }

        @Override
        public int pack(final int hour, final int minute, final int second) {
            return hour * 10000 + minute * 100 + second;
        }

        @Override
        public int unpackHour(final int packed) {
            return packed / 10000;
        }

        @Override
        public int unpackMinute(final int packed) {
            return (packed / 100) % 100;
        }

        @Override
        public int unpackSecond(final int packed) {
            return packed % 100;
        }

        @Override
        public String toString() {
            return "TimePacker.DECIMAL";
        }
    };

    class Validated implements Default {
        private final TimePacker packer;
        private final TimeValidator validator;

        public Validated(final TimePacker packer, final ValidationMethod validationMethod) {
            this(packer, TimeValidator.valueOf(validationMethod));
        }

        public Validated(final TimePacker packer, final TimeValidator validator) {
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
        public int pack(final int hour, final int minute, final int second) {
            if (validator.validateTime(hour, minute, second) != TimeValidator.INVALID) {
                return packer.pack(hour, minute, second);
            }
            return INVALID;
        }

        @Override
        public int unpackHour(final int packed) {
            return validator.validateHour(packer.unpackHour(packed));
        }

        @Override
        public int unpackMinute(final int packed) {
            return validator.validateMinute(packer.unpackMinute(packed));
        }

        @Override
        public int unpackSecond(final int packed) {
            return validator.validateSecond(packer.unpackSecond(packed));
        }
        @Override
        public String toString() {
            return "TimePacker.Validated." + packer.packing();
        }
    }

    static TimePacker valueOf(final Packing packing) {
        return packing == Packing.BINARY ? BINARY : DECIMAL;
    }

    @Garbage(Garbage.Type.RESULT)
    static TimePacker valueOf(final Packing packing, final ValidationMethod validationMethod) {
        switch (validationMethod) {
            case UNVALIDATED:
                return valueOf(packing);
            default:
                return new Validated(valueOf(packing), validationMethod);
        }
    }
}
