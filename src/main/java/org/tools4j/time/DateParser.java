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

public interface DateParser {
    int INVALID = DateValidator.INVALID;
    long INVALID_EPOCH = DateValidator.INVALID_EPOCH;
    DateFormat format();
    ValidationMethod validationMethod();
    int toYear(CharSequence charSequence);
    int toYear(CharSequence charSequence, int offset);
    <S> int toYear(S source, CharReader<? super S> reader);
    <S> int toYear(S source, CharReader<? super S> reader, int offset);
    int toMonth(CharSequence charSequence);
    int toMonth(CharSequence charSequence, int offset);
    <S> int toMonth(S source, CharReader<? super S> reader);
    <S> int toMonth(S source, CharReader<? super S> reader, int offset);
    int toDay(CharSequence charSequence);
    int toDay(CharSequence charSequence, int offset);
    <S> int toDay(S source, CharReader<? super S> reader);
    <S> int toDay(S source, CharReader<? super S> reader, int offset);
    int toPacked(CharSequence charSequence, Packing packing);
    int toPacked(CharSequence charSequence, int offset, Packing packing);
    <S> int toPacked(S source, CharReader<? super S> reader, Packing packing);
    <S> int toPacked(S source, CharReader<? super S> reader, int offset, Packing packing);
    long toEpochDays(CharSequence charSequence);
    long toEpochDays(CharSequence charSequence, int offset);
    <S> long toEpochDays(S source, CharReader<? super S> reader);
    <S> long toEpochDays(S source, CharReader<? super S> reader, int offset);
    long toEpochMillis(CharSequence charSequence);
    long toEpochMillis(CharSequence charSequence, int offset);
    <S> long toEpochMillis(S source, CharReader<? super S> reader);
    <S> long toEpochMillis(S source, CharReader<? super S> reader, int offset);
    @Garbage(Garbage.Type.RESULT)
    LocalDate toLocalDate(CharSequence charSequence);
    @Garbage(Garbage.Type.RESULT)
    LocalDate toLocalDate(CharSequence charSequence, int offset);
    @Garbage(Garbage.Type.RESULT)
    <S> LocalDate toLocalDate(S source, CharReader<? super S> reader);
    @Garbage(Garbage.Type.RESULT)
    <S> LocalDate toLocalDate(S source, CharReader<? super S> reader, int offset);

    interface Default extends DateParser {
        @Override
        default int toYear(final CharSequence charSequence) {
            return toYear(charSequence, CharReader.CHAR_SEQUENCE);
        }
        @Override
        default int toYear(final CharSequence charSequence, final int offset) {
            return toYear(charSequence, CharReader.CHAR_SEQUENCE, offset);
        }
        @Override
        default <S> int toYear(final S source, final CharReader<? super S> reader) {
            return toYear(source, reader, 0);
        }
        @Override
        default int toMonth(final CharSequence charSequence) {
            return toMonth(charSequence, CharReader.CHAR_SEQUENCE);
        }
        @Override
        default int toMonth(final CharSequence charSequence, final int offset) {
            return toMonth(charSequence, CharReader.CHAR_SEQUENCE, offset);
        }
        @Override
        default <S> int toMonth(final S source, final CharReader<? super S> reader) {
            return toMonth(source, reader, 0);
        }
        @Override
        default int toDay(final CharSequence charSequence) {
            return toDay(charSequence, CharReader.CHAR_SEQUENCE);
        }
        @Override
        default int toDay(final CharSequence charSequence, final int offset) {
            return toDay(charSequence, CharReader.CHAR_SEQUENCE, offset);
        }
        @Override
        default <S> int toDay(final S source, final CharReader<? super S> reader) {
            return toDay(source, reader, 0);
        }
        @Override
        default int toPacked(final CharSequence charSequence, final Packing packing) {
            return toPacked(charSequence, CharReader.CHAR_SEQUENCE, packing);
        }
        @Override
        default int toPacked(final CharSequence charSequence, final int offset, final Packing packing) {
            return toPacked(charSequence, CharReader.CHAR_SEQUENCE, offset, packing);
        }
        @Override
        default <S> int toPacked(final S source, final CharReader<? super S> reader, final Packing packing) {
            return toPacked(source, reader, 0, packing);
        }
        @Override
        default <S> int toPacked(final S source, final CharReader<? super S> reader, final int offset, final Packing packing) {
            final int year = toYear(source, reader, offset);
            final int month = toMonth(source, reader, offset);
            final int day = toDay(source, reader, offset);
            return DatePacker.valueOf(packing).pack(year, month, day);
        }
        @Override
        default long toEpochDays(final CharSequence charSequence) {
            return toEpochDays(charSequence, CharReader.CHAR_SEQUENCE);
        }
        @Override
        default long toEpochDays(final CharSequence charSequence, final int offset) {
            return toEpochDays(charSequence, CharReader.CHAR_SEQUENCE, offset);
        }
        @Override
        default <S> long toEpochDays(final S source, final CharReader<? super S> reader) {
            return toEpochDays(source, reader, 0);
        }
        @Override
        default <S> long toEpochDays(final S source, final CharReader<? super S> reader, final int offset) {
            final int year = toYear(source, reader, offset);
            final int month = toMonth(source, reader, offset);
            final int day = toDay(source, reader, offset);
            return Epoch.toEpochDays(year, month, day);
        }
        @Override
        default long toEpochMillis(final CharSequence charSequence) {
            return toEpochMillis(charSequence, CharReader.CHAR_SEQUENCE);
        }
        @Override
        default long toEpochMillis(final CharSequence charSequence, final int offset) {
            return toEpochMillis(charSequence, CharReader.CHAR_SEQUENCE, offset);
        }
        @Override
        default <S> long toEpochMillis(final S source, final CharReader<? super S> reader) {
            return toEpochMillis(source, reader, 0);
        }
        @Override
        default <S> long toEpochMillis(final S source, final CharReader<? super S> reader, final int offset) {
            return toEpochDays(source, reader, offset) * TimeFactors.MILLIS_PER_DAY;
        }
        @Override
        default LocalDate toLocalDate(final CharSequence charSequence) {
            return toLocalDate(charSequence, CharReader.CHAR_SEQUENCE);
        }
        @Override
        default LocalDate toLocalDate(final CharSequence charSequence, final int offset) {
            return toLocalDate(charSequence, CharReader.CHAR_SEQUENCE, offset);
        }
        @Override
        default <S> LocalDate toLocalDate(final S source, final CharReader<? super S> reader) {
            return toLocalDate(source, reader, 0);
        }
        @Garbage(Garbage.Type.RESULT)
        @Override
        default <S> LocalDate toLocalDate(final S source, final CharReader<? super S> reader, final int offset) {
            final int year = toYear(source, reader, offset);
            final int month = toMonth(source, reader, offset);
            final int day = toDay(source, reader, offset);
            DateValidator.THROW_EXCEPTION.validateDay(year, month, day);
            return LocalDate.of(year, month, day);
        }
    }

    class Validated implements Default {
        private final DateParser parser;
        private final DateValidator validator;

        public Validated(final DateParser parser, final ValidationMethod validationMethod) {
            this(parser, DateValidator.valueOf(validationMethod));
        }

        public Validated(final DateParser parser, final DateValidator validator) {
            this.parser = Objects.requireNonNull(parser);
            this.validator = Objects.requireNonNull(validator);
        }

        @Override
        public DateFormat format() {
            return parser.format();
        }

        @Override
        public ValidationMethod validationMethod() {
            return validator.validationMethod();
        }

        @Override
        public <S> int toYear(final S source, final CharReader<? super S> reader, final int offset) {
            return validator.validateYear(parser.toYear(source, reader, offset));
        }

        @Override
        public <S> int toMonth(final S source, final CharReader<? super S> reader, final int offset) {
            return validator.validateMonth(parser.toMonth(source, reader, offset));
        }

        @Override
        public <S> int toDay(final S source, final CharReader<? super S> reader, final int offset) {
            final int year = parser.toYear(source, reader, offset);
            final int month = parser.toMonth(source, reader, offset);
            final int day = parser.toDay(source, reader, offset);
            return validator.validateDay(year, month, day);
        }

        @Override
        public <S> int toPacked(final S source, final CharReader<? super S> reader, final int offset, final Packing packing) {
            final int year = parser.toYear(source, reader, offset);
            final int month = parser.toMonth(source, reader, offset);
            final int day = parser.toDay(source, reader, offset);
            if (validator.validateDay(year, month, day) != DateValidator.INVALID) {
                return DatePacker.valueOf(packing).pack(year, month, day);
            }
            return INVALID;
        }

        @Override
        public <S> long toEpochDays(final S source, final CharReader<? super S> reader, final int offset) {
            final int year = parser.toYear(source, reader, offset);
            final int month = parser.toMonth(source, reader, offset);
            final int day = parser.toDay(source, reader, offset);
            if (validator.validateDay(year, month, day) != DateValidator.INVALID) {
                return Epoch.toEpochDays(year, month, day);
            }
            return INVALID_EPOCH;
        }
    }
}
