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
package org.tools4j.time.format;

import org.tools4j.time.base.Garbage;
import org.tools4j.time.base.TimeFactors;
import org.tools4j.time.pack.Packing;
import org.tools4j.time.validate.DateValidator;
import org.tools4j.time.validate.ValidationMethod;

import java.time.LocalDate;

public interface DateParser {
    int INVALID = DateValidator.INVALID;
    long INVALID_EPOCH = DateValidator.INVALID_EPOCH;
    byte INVAILD_SEPARATOR = -1;
    byte NO_SEPARATOR = Byte.MIN_VALUE;
    DateFormat format();
    ValidationMethod validationMethod();
    int toYear(CharSequence charSequence);
    int toYear(CharSequence charSequence, int offset);
    <S> int toYear(S source, AsciiReader<? super S> reader);
    <S> int toYear(S source, AsciiReader<? super S> reader, int offset);
    int toMonth(CharSequence charSequence);
    int toMonth(CharSequence charSequence, int offset);
    <S> int toMonth(S source, AsciiReader<? super S> reader);
    <S> int toMonth(S source, AsciiReader<? super S> reader, int offset);
    int toDay(CharSequence charSequence);
    int toDay(CharSequence charSequence, int offset);
    <S> int toDay(S source, AsciiReader<? super S> reader);
    <S> int toDay(S source, AsciiReader<? super S> reader, int offset);
    int toPacked(CharSequence charSequence, Packing packing);
    int toPacked(CharSequence charSequence, int offset, Packing packing);
    <S> int toPacked(S source, AsciiReader<? super S> reader, Packing packing);
    <S> int toPacked(S source, AsciiReader<? super S> reader, int offset, Packing packing);
    long toEpochDays(CharSequence charSequence);
    long toEpochDays(CharSequence charSequence, int offset);
    <S> long toEpochDays(S source, AsciiReader<? super S> reader);
    <S> long toEpochDays(S source, AsciiReader<? super S> reader, int offset);
    long toEpochMillis(CharSequence charSequence);
    long toEpochMillis(CharSequence charSequence, int offset);
    <S> long toEpochMillis(S source, AsciiReader<? super S> reader);
    <S> long toEpochMillis(S source, AsciiReader<? super S> reader, int offset);
    @Garbage(Garbage.Type.RESULT)
    LocalDate toLocalDate(CharSequence charSequence);
    @Garbage(Garbage.Type.RESULT)
    LocalDate toLocalDate(CharSequence charSequence, int offset);
    @Garbage(Garbage.Type.RESULT)
    <S> LocalDate toLocalDate(S source, AsciiReader<? super S> reader);
    @Garbage(Garbage.Type.RESULT)
    <S> LocalDate toLocalDate(S source, AsciiReader<? super S> reader, int offset);
    char toSeparator(CharSequence charSequence, int separatorIndex);
    char toSeparator(CharSequence charSequence, int offset, int separatorIndex);
    <S> char toSeparator(S source, AsciiReader<? super S> reader, int separatorIndex);
    <S> char toSeparator(S source, AsciiReader<? super S> reader, int offset, int separatorIndex);
    boolean isValid(CharSequence charSequence);
    boolean isValid(CharSequence charSequence, int offset);
    <S> boolean isValid(S source, AsciiReader<? super S> reader);
    <S> boolean isValid(S source, AsciiReader<? super S> reader, int offset);

    interface Default extends DateParser {
        @Override
        default int toYear(final CharSequence charSequence) {
            return toYear(charSequence, AsciiReader.CHAR_SEQUENCE);
        }
        @Override
        default int toYear(final CharSequence charSequence, final int offset) {
            return toYear(charSequence, AsciiReader.CHAR_SEQUENCE, offset);
        }
        @Override
        default <S> int toYear(final S source, final AsciiReader<? super S> reader) {
            return toYear(source, reader, 0);
        }
        @Override
        default int toMonth(final CharSequence charSequence) {
            return toMonth(charSequence, AsciiReader.CHAR_SEQUENCE);
        }
        @Override
        default int toMonth(final CharSequence charSequence, final int offset) {
            return toMonth(charSequence, AsciiReader.CHAR_SEQUENCE, offset);
        }
        @Override
        default <S> int toMonth(final S source, final AsciiReader<? super S> reader) {
            return toMonth(source, reader, 0);
        }
        @Override
        default int toDay(final CharSequence charSequence) {
            return toDay(charSequence, AsciiReader.CHAR_SEQUENCE);
        }
        @Override
        default int toDay(final CharSequence charSequence, final int offset) {
            return toDay(charSequence, AsciiReader.CHAR_SEQUENCE, offset);
        }
        @Override
        default <S> int toDay(final S source, final AsciiReader<? super S> reader) {
            return toDay(source, reader, 0);
        }
        @Override
        default int toPacked(final CharSequence charSequence, final Packing packing) {
            return toPacked(charSequence, AsciiReader.CHAR_SEQUENCE, packing);
        }
        @Override
        default int toPacked(final CharSequence charSequence, final int offset, final Packing packing) {
            return toPacked(charSequence, AsciiReader.CHAR_SEQUENCE, offset, packing);
        }
        @Override
        default <S> int toPacked(final S source, final AsciiReader<? super S> reader, final Packing packing) {
            return toPacked(source, reader, 0, packing);
        }
        @Override
        default long toEpochDays(final CharSequence charSequence) {
            return toEpochDays(charSequence, AsciiReader.CHAR_SEQUENCE);
        }
        @Override
        default long toEpochDays(final CharSequence charSequence, final int offset) {
            return toEpochDays(charSequence, AsciiReader.CHAR_SEQUENCE, offset);
        }
        @Override
        default <S> long toEpochDays(final S source, final AsciiReader<? super S> reader) {
            return toEpochDays(source, reader, 0);
        }
        @Override
        default long toEpochMillis(final CharSequence charSequence) {
            return toEpochMillis(charSequence, AsciiReader.CHAR_SEQUENCE);
        }
        @Override
        default long toEpochMillis(final CharSequence charSequence, final int offset) {
            return toEpochMillis(charSequence, AsciiReader.CHAR_SEQUENCE, offset);
        }
        @Override
        default <S> long toEpochMillis(final S source, final AsciiReader<? super S> reader) {
            return toEpochMillis(source, reader, 0);
        }
        @Override
        default <S> long toEpochMillis(final S source, final AsciiReader<? super S> reader, final int offset) {
            return toEpochDays(source, reader, offset) * TimeFactors.MILLIS_PER_DAY;
        }
        @Override
        default LocalDate toLocalDate(final CharSequence charSequence) {
            return toLocalDate(charSequence, AsciiReader.CHAR_SEQUENCE);
        }
        @Garbage(Garbage.Type.RESULT)
        @Override
        default LocalDate toLocalDate(final CharSequence charSequence, final int offset) {
            return toLocalDate(charSequence, AsciiReader.CHAR_SEQUENCE, offset);
        }
        @Garbage(Garbage.Type.RESULT)
        @Override
        default <S> LocalDate toLocalDate(final S source, final AsciiReader<? super S> reader) {
            return toLocalDate(source, reader, 0);
        }
        @Override
        default char toSeparator(final CharSequence charSequence, final int separatorIndex) {
            return toSeparator(charSequence, AsciiReader.CHAR_SEQUENCE, 0, separatorIndex);
        }
        @Override
        default char toSeparator(final CharSequence charSequence, final int offset, final int separatorIndex) {
            return toSeparator(charSequence, AsciiReader.CHAR_SEQUENCE, offset, separatorIndex);
        }
        @Override
        default <S> char toSeparator(final S source, final AsciiReader<? super S> reader, final int separatorIndex) {
            return toSeparator(source, reader, 0, separatorIndex);
        }
        @Override
        default boolean isValid(final CharSequence charSequence) {
            return isValid(charSequence, AsciiReader.CHAR_SEQUENCE, 0);
        }
        @Override
        default boolean isValid(final CharSequence charSequence, final int offset) {
            return isValid(charSequence, AsciiReader.CHAR_SEQUENCE, offset);
        }
        @Override
        default <S> boolean isValid(final S source, final AsciiReader<? super S> reader) {
            return isValid(source, reader, 0);
        }
    }
}
