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

import org.tools4j.time.pack.Packing;
import org.tools4j.time.validate.ValidationMethod;

import java.util.Objects;

final class DefaultDateParser implements DateParser.Default {

    private final DateFormat format;

    private DefaultDateParser(final DateFormat format) {
        this.format = Objects.requireNonNull(format);
    }

    @Override
    public DateFormat format() {
        return format;
    }

    @Override
    public ValidationMethod validationMethod() {
        return ValidationMethod.UNVALIDATED;
    }

    @Override
    public <S> int toYear(final S source, final CharReader<? super S> reader, final int offset) {
        final int off = offset + format.offsetYear();
        final char ch1 = reader.readChar(source, off);
        final char ch2 = reader.readChar(source, off + 1);
        final char ch3 = reader.readChar(source, off + 2);
        final char ch4 = reader.readChar(source, off + 3);
        if (isDigit(ch1) & isDigit(ch2) & isDigit(ch3) & isDigit(ch4)) {
            final int year = digit(ch1) * 1000 + digit(ch2) * 100 + digit(ch3) * 10 + digit(ch4);
            if (year > 0) {
                return year;
            }
        }
        return INVALID;
    }

    @Override
    public <S> int toMonth(final S source, final CharReader<? super S> reader, final int offset) {
        final int off = offset + format.offsetMonth();
        final char ch1 = reader.readChar(source, off);
        final char ch2 = reader.readChar(source, off + 1);
        if ((ch1 == '0' & '1' <= ch2 & ch2 <= '9') | (ch1 == '1' & '0' <= ch2 & ch2 <= '2')) {
            return digit(ch1) * 10 + digit(ch2);
        }
        return INVALID;
    }

    @Override
    public <S> int toDay(final S source, final CharReader<? super S> reader, final int offset) {
        final int off = offset + format.offsetDay();
        final char ch1 = reader.readChar(source, off);
        final char ch2 = reader.readChar(source, off + 1);
        if ((ch1 == '0' & '1' <= ch2 & ch2 <= '9') |
                ('1' <= ch1 & ch1 <= '2' & '0' <= ch2 & ch2 <= '9') |
                (ch1 == '3' & '0' <= ch2 & ch2 <= '1')) {
            return digit(ch1) * 10 + digit(ch2);
        }
        return INVALID;
    }

    @Override
    public <S> int toPacked(final S source, final CharReader<? super S> reader, final int offset, final Packing packer) {
        return 0;
    }

    private static int digit(final char ch) {
        return ch - '0';
    }

    private static boolean isDigit(final char ch) {
        return '0' <= ch & ch <= '9';
    }

}
