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
import org.tools4j.time.validate.DateValidator;
import org.tools4j.time.validate.ValidationMethod;

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
    int packDaysSinceEpoch(long daysSinceEpoch);
    int packMillisSinceEpoch(long millisSinceEpoch);

    static DatePacker valueOf(final Packing packing) {
        return Instances.valueOf(packing, ValidationMethod.UNVALIDATED);
    }

    static DatePacker valueOf(final Packing packing, final ValidationMethod validationMethod) {
        return Instances.valueOf(packing, validationMethod);
    }

    interface Default extends DatePacker {
        @Override
        default int packNull() {
            return NULL;
        }

        @Override
        default boolean unpackNull(final int packed) {
            return packed == NULL;
        }

        @Override
        default int pack(final LocalDate localDate) {
            return localDate == null ? packNull() : pack(
                    localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth()
            );
        }

        @Override
        @Garbage(Garbage.Type.RESULT)
        default LocalDate unpackLocalDate(final int packed) {
            return unpackNull(packed) ? null : LocalDate.of(
                    unpackYear(packed), unpackMonth(packed), unpackDay(packed)
            );
        }

        @Override
        default int packDaysSinceEpoch(final long daysSinceEpoch) {
            return Epoch.valueOf(validationMethod()).fromEpochDays(daysSinceEpoch, this);
        }

        @Override
        default int packMillisSinceEpoch(final long millisSinceEpoch) {
            return Epoch.valueOf(validationMethod()).fromEpochMillis(millisSinceEpoch, this);
        }

        @Override
        @Garbage(Garbage.Type.RESULT)
        default DatePacker forValidationMethod(final ValidationMethod validationMethod) {
            return valueOf(packing(), validationMethod);
        }
    }

    DatePacker BINARY = new DatePacker.Default() {
        @Override
        public Packing packing() {
            return Packing.BINARY;
        }

        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
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
        public Packing packing() {
            return Packing.DECIMAL;
        }

        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
        }

        @Override
        public int pack(final int year, final int month, final int day) {
            return (year * 100_00) + (month * 100) + day;
        }

        @Override
        public int unpackYear(final int packed) {
            return packed / 100_00;
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
            if (validator.validateDay(year, month, day) != DateValidator.INVALID) {
                return packer.pack(year, month, day);
            }
            return INVALID;
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
            final int day = packer.unpackDay(packed);
            return validator.validateDay(year, month, day);
        }

        @Override
        public String toString() {
            return "DatePacker.Validated." + packer.packing();
        }

    }

    final class Instances {
        private static final DatePacker[][] BY_PACKING_AND_VALIDATION_METHOD = instancesByPackingAndValidationMethod();

        private static DatePacker valueOf(final Packing packing, final ValidationMethod validationMethod) {
            return BY_PACKING_AND_VALIDATION_METHOD[packing.ordinal()][validationMethod.ordinal()];
        }

        private static DatePacker[][] instancesByPackingAndValidationMethod() {
            final DatePacker[][] instances = new DatePacker[Packing.length()][ValidationMethod.length()];
            final int vOrdUnvalidated = ValidationMethod.UNVALIDATED.ordinal();
            instances[Packing.BINARY.ordinal()][vOrdUnvalidated] = BINARY;
            instances[Packing.DECIMAL.ordinal()][vOrdUnvalidated] = DECIMAL;
            for (int pOrd = 0; pOrd < Packing.length(); pOrd++) {
                for (int vOrd = 0; vOrd < ValidationMethod.length(); vOrd++) {
                    if (vOrd != vOrdUnvalidated) {
                        instances[pOrd][vOrd] = new Validated(instances[pOrd][vOrdUnvalidated], ValidationMethod.valueByOrdinal(vOrd));
                    }
                }
            }
            return instances;
        }
    }

}
