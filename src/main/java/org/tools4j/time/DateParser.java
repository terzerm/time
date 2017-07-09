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

public interface DateParser {
    int INVALID = -1;
    long INVALID_EPOCH = Long.MIN_VALUE;
    LocalDate INVALID_LOCAL_DATE = null;

    DateFormat getFormat();
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
    int toPacked(CharSequence charSequence, DatePacker packer);
    int toPacked(CharSequence charSequence, int offset, DatePacker packer);
    <S> int toPacked(S source, CharReader<? super S> reader, DatePacker packer);
    <S> int toPacked(S source, CharReader<? super S> reader, int offset, DatePacker packer);
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
        default <S> int toYear(final S source, final CharReader<? super S> reader, final int offset) {
            final int packed = toPacked(source, reader, offset, DatePacker.BINARY);
            return DatePacker.BINARY.unpackYear(packed);
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
        default <S> int toMonth(final S source, final CharReader<? super S> reader, final int offset) {
            final int packed = toPacked(source, reader, offset, DatePacker.BINARY);
            return DatePacker.BINARY.unpackMonth(packed);
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
        default <S> int toDay(final S source, final CharReader<? super S> reader, final int offset) {
            final int packed = toPacked(source, reader, offset, DatePacker.BINARY);
            return DatePacker.BINARY.unpackDay(packed);
        }
        @Override
        default int toPacked(final CharSequence charSequence, final DatePacker packer) {
            return toPacked(charSequence, CharReader.CHAR_SEQUENCE, packer);
        }
        @Override
        default int toPacked(final CharSequence charSequence, final int offset, final DatePacker packer) {
            return toPacked(charSequence, CharReader.CHAR_SEQUENCE, offset, packer);
        }
        @Override
        default <S> int toPacked(final S source, final CharReader<? super S> reader, final DatePacker packer) {
            return toPacked(source, reader, 0, packer);
        }
        @Override
        default <S> int toPacked(final S source, final CharReader<? super S> reader, final int offset, final DatePacker packer) {
            final int year = toYear(source, reader, offset);
            final int month = toMonth(source, reader, offset);
            final int day = toDay(source, reader, offset);
            return packer.pack(year, month, day);
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
        @Override
        default <S> LocalDate toLocalDate(final S source, final CharReader<? super S> reader, final int offset) {
            final int year = toYear(source, reader, offset);
            final int month = toMonth(source, reader, offset);
            final int day = toDay(source, reader, offset);
            return LocalDate.of(year, month, day);
        }
    }
}
