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
import org.tools4j.time.validate.TimeValidator;
import org.tools4j.time.validate.ValidationMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import static org.tools4j.time.base.TimeFactors.MILLIS_PER_SECOND;
import static org.tools4j.time.base.TimeFactors.NANOS_PER_MILLI;

public interface DateTimePacker {
    long INVALID = -1;
    long NULL = Long.MAX_VALUE;
    Packing packing();
    ValidationMethod validationMethod();
    @Garbage(Garbage.Type.RESULT)
    DateTimePacker forValidationMethod(ValidationMethod validationMethod);
    long pack(int year, int month, int day);
    long pack(int year, int month, int day, int hour, int minute, int second);
    long pack(int year, int month, int day, int hour, int minute, int second, int milli);
    int unpackYear(long packed);
    int unpackMonth(long packed);
    int unpackDay(long packed);
    int unpackHour(long packed);
    int unpackMinute(long packed);
    int unpackSecond(long packed);
    int unpackMilli(long packed);
    long packNull();
    boolean unpackNull(long packed);
    long pack(LocalDateTime localDateTime);
    long pack(LocalDate localDate, LocalTime localTime);
    @Garbage(Garbage.Type.RESULT)
    LocalDateTime unpackLocalDateTime(long packed);
    long packMillisSinceEpoch(long millisSinceEpoch);

    static DateTimePacker valueOf(final Packing packing) {
        return Instances.valueOf(packing, ValidationMethod.UNVALIDATED);
    }

    static DateTimePacker valueOf(final Packing packing, final ValidationMethod validationMethod) {
        return Instances.valueOf(packing, validationMethod);
    }

    interface Default extends DateTimePacker {
        @Override
        default long packNull() {
            return NULL;
        }

        @Override
        default boolean unpackNull(final long packed) {
            return packed == NULL;
        }

        @Override
        default long pack(final int year, final int month, final int day) {
            return pack(year, month, day, 0, 0, 0, 0);
        }

        @Override
        default long pack(final int year, final int month, final int day,
                          final int hour, final int minute, final int second) {
            return pack(year, month, day, hour, minute, second, 0);
        }

        @Override
        default long pack(final LocalDateTime localDateTime) {
            return localDateTime == null ? packNull() :
                    pack(localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth(),
                            localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond(),
                            localDateTime.getNano() / NANOS_PER_MILLI);
        }

        @Override
        default long pack(final LocalDate localDate, final LocalTime localTime) {
            return localDate == null ? packNull() :
                    localTime == null ?
                            pack(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth()) :
                            pack(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(),
                                    localTime.getHour(), localTime.getMinute(), localTime.getSecond(),
                                    localTime.getNano() / NANOS_PER_MILLI);
        }

        @Override
        @Garbage(Garbage.Type.RESULT)
        default LocalDateTime unpackLocalDateTime(final long packed) {
            return unpackNull(packed) ? null :
                    LocalDateTime.of(unpackYear(packed), unpackMonth(packed), unpackDay(packed),
                            unpackHour(packed), unpackMinute(packed), unpackSecond(packed),
                            unpackMilli(packed) * NANOS_PER_MILLI);
        }

        @Override
        default long packMillisSinceEpoch(final long millisSinceEpoch) {
            return Epoch.valueOf(validationMethod()).fromEpochMillis(millisSinceEpoch, this);
        }

        @Override
        @Garbage(Garbage.Type.RESULT)
        default DateTimePacker forValidationMethod(final ValidationMethod validationMethod) {
            return valueOf(packing(), validationMethod);
        }
    }

    DateTimePacker BINARY = new Default() {
        @Override
        public Packing packing() {
            return Packing.BINARY;
        }

        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
        }

        @Override
        public long pack(final int year, final int month, final int day) {
            return ((year & 0x3fffL) << 36) | ((month & 0xfL) << 32) | ((day & 0x1fL) << 27);
        }

        @Override
        public long pack(final int year, final int month, final int day,
                         final int hour, final int minute, final int second, final int milli) {
            return ((year & 0x3fffL) << 36) | ((month & 0xfL) << 32) | ((day & 0x1fL) << 27) |
                    ((hour & 0x1fL) << 22) | ((minute & 0x3fL) << 16) | ((second & 0x3fL)  << 10) | (milli & 0x3ffL);
        }

        @Override
        public int unpackYear(final long packed) {
            return (int)((packed >>> 36) & 0x3fffL);
        }

        @Override
        public int unpackMonth(final long packed) {
            return (int)((packed >>> 32) & 0xfL);
        }

        @Override
        public int unpackDay(final long packed) {
            return (int)((packed >>> 27) & 0x1fL);
        }

        @Override
        public int unpackHour(final long packed) {
            return (int)((packed >>> 22) & 0x1fL);
        }

        @Override
        public int unpackMinute(final long packed) {
            return (int)((packed >>> 16) & 0x3fL);
        }

        @Override
        public int unpackSecond(final long packed) {
            return (int)((packed >> 10) & 0x3fL);
        }

        @Override
        public int unpackMilli(final long packed) {
            return (int)(packed & 0x3ffL);
        }

        @Override
        public String toString() {
            return "DateTimePacker.BINARY";
        }
    };

    DateTimePacker DECIMAL = new Default() {
        @Override
        public Packing packing() {
            return Packing.DECIMAL;
        }

        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
        }

        @Override
        public long pack(final int year, final int month, final int day) {
            return (year * 100_00_00_00_00_000L) + (month * 100_00_00_00_000L) + (day * 100_00_00_000L);
        }

        @Override
        public long pack(final int year, final int month, final int day,
                         final int hour, final int minute, final int second, final int milli) {
            return (year * 100_00_00_00_00_000L) + (month * 100_00_00_00_000L) + (day * 100_00_00_000L) +
                    (hour * 100_00_000L) + (minute * 100_000L) + (second * 1000L) + milli;
        }

        @Override
        public int unpackYear(final long packed) {
            return (int)(packed / 100_00_00_00_00_000L);
        }

        @Override
        public int unpackMonth(final long packed) {
            return (int)((packed / 100_00_00_00_000L) % 100L);
        }

        @Override
        public int unpackDay(final long packed) {
            return (int)((packed / 100_00_00_000L) % 100L);
        }

        @Override
        public int unpackHour(final long packed) {
            return (int)((packed / 100_00_000L) % 100L);
        }

        @Override
        public int unpackMinute(final long packed) {
            return (int)((packed / 100_000L) % 100L);
        }

        @Override
        public int unpackSecond(final long packed) {
            return (int)((packed / 1_000L) % 100L);
        }

        @Override
        public int unpackMilli(final long packed) {
            return (int)(packed % MILLIS_PER_SECOND);
        }

        @Override
        public String toString() {
            return "DateTimePacker.DECIMAL";
        }
    };

    class Validated implements Default {
        private final DateTimePacker packer;
        private final DateValidator dateValidator;
        private final TimeValidator timeValidator;

        public Validated(final DateTimePacker packer, final ValidationMethod validationMethod) {
            this(packer, DateValidator.valueOf(validationMethod), TimeValidator.valueOf(validationMethod));
        }

        public Validated(final DateTimePacker packer, final DateValidator dateValidator, final TimeValidator timeValidator) {
            this.packer = Objects.requireNonNull(packer);
            this.dateValidator = Objects.requireNonNull(dateValidator);
            this.timeValidator = Objects.requireNonNull(timeValidator);
        }

        @Override
        public Packing packing() {
            return packer.packing();
        }

        @Override
        public ValidationMethod validationMethod() {
            return timeValidator.validationMethod();
        }

        @Override
        public long pack(final int year, final int month, final int day) {
            if (dateValidator.validateDay(year, month, day) != DateValidator.INVALID) {
                return packer.pack(year, month, day);
            }
            return INVALID;
        }

        @Override
        public long pack(final int year, final int month, final int day, final int hour, final int minute, final int second, final int milli) {
            if (dateValidator.validateDay(year, month, day) != DateValidator.INVALID &
                    timeValidator.validateTimeWithMillis(hour, minute, second, milli) != TimeValidator.INVALID) {
                return packer.pack(year, month, day, hour, minute, second, milli);
            }
            return INVALID;
        }

        @Override
        public int unpackYear(final long packed) {
            return dateValidator.validateYear(packer.unpackYear(packed));
        }

        @Override
        public int unpackMonth(final long packed) {
            return dateValidator.validateMonth(packer.unpackMonth(packed));
        }

        @Override
        public int unpackDay(final long packed) {
            final int year = packer.unpackYear(packed);
            final int month = packer.unpackMonth(packed);
            final int day = packer.unpackDay(packed);
            return dateValidator.validateDay(year, month, day);
        }

        @Override
        public int unpackHour(final long packed) {
            return timeValidator.validateHour(packer.unpackHour(packed));
        }

        @Override
        public int unpackMinute(final long packed) {
            return timeValidator.validateMinute(packer.unpackMinute(packed));
        }

        @Override
        public int unpackSecond(final long packed) {
            return timeValidator.validateSecond(packer.unpackSecond(packed));
        }

        @Override
        public int unpackMilli(final long packed) {
            return timeValidator.validateMilli(packer.unpackMilli(packed));
        }

        @Override
        public String toString() {
            return "DateTimePacker.Validated." + packer.packing();
        }
    }

    final class Instances {

        private static final DateTimePacker[][] BY_PACKING_AND_VALIDATION_METHOD = instancesByPackingAndValidationMethod();

        private static DateTimePacker valueOf(final Packing packing, final ValidationMethod validationMethod) {
            return BY_PACKING_AND_VALIDATION_METHOD[packing.ordinal()][validationMethod.ordinal()];
        }

        private static DateTimePacker[][] instancesByPackingAndValidationMethod() {
            final DateTimePacker[][] instances = new DateTimePacker[Packing.length()][ValidationMethod.length()];
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
