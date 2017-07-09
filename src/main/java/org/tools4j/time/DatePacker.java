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
import java.util.Objects;

public interface DatePacker {
    int INVALID = -1;
    int NULL = 0;
    Packing packing();
    ValidationMethod validationMethod();
    @Garbage(Garbage.Type.RESULT)
    DatePacker forValidationMethod(ValidationMethod validationMethod);
    int pack(int year, int month, int day);
    int unpackYear(int packed);
    int unpackMonth(int packed);
    int unpackDay(int packed);
    int packNull();
    boolean unpackNull(int packed);
    int pack(LocalDate localDate);
    @Garbage(Garbage.Type.RESULT)
    LocalDate unpackLocalDate(int packed);

    default int packDaysSinceEpoch(final long daysSinceEpoch) {
        return Epoch.fromEpochDays(daysSinceEpoch, this);
    }

    default int packMillisSinceEpoch(final long millisSinceEpoch) {
        return Epoch.fromEpochMillis(millisSinceEpoch, this);
    }

    interface Default extends DatePacker {
        default int packNull() {
            return NULL;
        }
        default boolean unpackNull(final int packed) {
            return packed == NULL;
        }
        default int pack(final LocalDate localDate) {
            return localDate == null ? packNull() : pack(
                    localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth()
            );
        }
        default LocalDate unpackLocalDate(final int packed) {
            return unpackNull(packed) ? null : LocalDate.of(
                    unpackYear(packed), unpackMonth(packed), unpackDay(packed)
            );
        }

        default int packDaysSinceEpoch(final long daysSinceEpoch) {
            return Epoch.fromEpochDays(daysSinceEpoch, this);
        }

        default int packMillisSinceEpoch(final long millisSinceEpoch) {
            return Epoch.fromEpochMillis(millisSinceEpoch, this);
        }

        @Override
        default DatePacker forValidationMethod(final ValidationMethod validationMethod) {
            return new Validated(this, validationMethod);
        }
    }

    DatePacker BINARY = new DatePacker.Default() {
        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
        }

        @Override
        public Packing packing() {
            return Packing.BINARY;
        }

        @Override
        public int pack(final int year, final int month, final int day) {
            return ((year & 0x3fff) << 9) | ((month & 0xf) << 5) | (day & 0x1f);
        }

        @Override
        public int unpackYear(final int packed) {
            return (packed >>> 9) & 0x3fff;
        }

        @Override
        public int unpackMonth(final int packed) {
            return (packed >>> 5) & 0xf;
        }

        @Override
        public int unpackDay(final int packed) {
            return packed & 0x1f;
        }

        @Override
        public String toString() {
            return "DatePacker.BINARY";
        }
    };

    DatePacker DECIMAL = new DatePacker.Default() {
        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
        }

        @Override
        public int pack(final int year, final int month, final int day) {
            return (year * 10000) + (month * 100) + day;
        }

        @Override
        public int unpackYear(final int packed) {
            return packed / 10000;
        }

        @Override
        public int unpackMonth(final int packed) {
            return (packed / 100) % 100;
        }

        @Override
        public int unpackDay(final int packed) {
            return packed % 100;
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

    class Validated implements DatePacker.Default {
        private final DatePacker packer;
        private final DateValidator validator;

        public Validated(final DatePacker packer, final ValidationMethod validationMethod) {
            this(packer, DateValidator.valueOf(validationMethod));
        }

        public Validated(final DatePacker packer, final DateValidator validator) {
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
        public int pack(final int year, final int month, final int day) {
            if (validator.validateDay(year, month, day) == DateValidator.INVALID) {
                return INVALID;
            }
            return packer.pack(year, month, day);
        }

        @Override
        public int unpackYear(final int packed) {
            return validator.validateYear(packer.unpackYear(packed));
        }

        @Override
        public int unpackMonth(final int packed) {
            return validator.validateMonth(packer.unpackMonth(packed));
        }

        @Override
        public int unpackDay(final int packed) {
            final int year = packer.unpackYear(packed);
            final int month = packer.unpackMonth(packed);
            final int day  = packer.unpackDay(packed);
            return validator.validateDay(year, month, day);
        }

        @Override
        public String toString() {
            return packer.toString();
        }
    }


    static DatePacker valueOf(final Packing packing) {
        return packing == Packing.BINARY ? BINARY : DECIMAL;
    }

    static DatePacker valueOf(final Packing packing, final ValidationMethod validationMethod) {
        switch (validationMethod) {
            case UNVALIDATED:
                return valueOf(packing);
            default:
                return new Validated(valueOf(packing), validationMethod);
        }
    }

}
