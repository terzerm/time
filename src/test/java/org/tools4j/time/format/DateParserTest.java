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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tools4j.spockito.Spockito;
import org.tools4j.time.pack.DatePacker;
import org.tools4j.time.pack.Packing;
import org.tools4j.time.validate.ValidationMethod;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit test for {@link DateParser}.
 */
public class DateParserTest {

    private static final char[] SEPARATORS = {DateParser.NO_SEPARATOR, '-', '/', '.', '_'};
    private static final Map<DateFormat, String> PATTERN_BY_FORMAT = patternByFormat();
    private static final DateParser[] PARSERS = initParsers();

    @RunWith(Spockito.class)
    @Spockito.Unroll({
            "|  localDate |",
//            "| 2017-01-01 |",
            "| 2017-01-31 |",
            "| 2017-02-28 |",
            "| 2017-03-31 |",
            "| 2017-04-30 |",
            "| 2017-05-31 |",
            "| 2017-06-30 |",
            "| 2017-07-31 |",
            "| 2017-08-31 |",
            "| 2017-09-30 |",
            "| 2017-10-31 |",
            "| 2017-11-30 |",
            "| 2017-12-31 |",
            "| 2017-12-31 |",
            "| 2016-02-29 |",
            "| 2000-02-29 |",
            "| 1900-02-28 |",
            "| 1970-01-01 |",
            "| 1970-01-02 |",
            "| 1969-12-31 |",
            "| 1969-12-30 |",
            "| 1969-04-30 |",
            "| 1968-02-28 |",
            "| 1600-02-29 |",
            "| 0004-02-29 |",
            "| 0100-02-28 |",
            "| 0400-02-29 |",
            "| 0001-01-01 |",
            "| 9999-12-31 |",
    })
    public static class Valid {
        @Test
        public void toYear(final LocalDate localDate) throws Exception {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                assertEquals("input=" + input, localDate.getYear(), parser.toYear(input));
                assertEquals("input=" + input, localDate.getYear(), parser.toYear(input, AsciiReader.CHAR_SEQUENCE));
                assertEquals("input=BLA" + input, localDate.getYear(), parser.toYear("BLA" + input, 3));
            }
        }

        @Test
        public void toMonth(final LocalDate localDate) throws Exception {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                assertEquals("input=" + input, localDate.getMonthValue(), parser.toMonth(input));
                assertEquals("input=" + input, localDate.getMonthValue(), parser.toMonth(input, AsciiReader.CHAR_SEQUENCE));
                assertEquals("input=BLA" + input, localDate.getMonthValue(), parser.toMonth("BLA" + input, 3));
            }
        }

        @Test
        public void toDay(final LocalDate localDate) throws Exception {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                assertEquals("input=" + input, localDate.getDayOfMonth(), parser.toDay(input));
                assertEquals("input=" + input, localDate.getDayOfMonth(), parser.toDay(input, AsciiReader.CHAR_SEQUENCE));
                assertEquals("input=BLA" + input, localDate.getDayOfMonth(), parser.toDay("BLA" + input, 3));
            }
        }

        @Test
        public void toPacked(final LocalDate localDate) throws Exception {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                for (final Packing packing : Packing.values()) {
                    final int expected = DatePacker.valueOf(packing).pack(localDate);
                    assertEquals("input=" + input, expected, parser.toPacked(input, packing));
                    assertEquals("input=" + input, expected, parser.toPacked(input, AsciiReader.CHAR_SEQUENCE, packing));
                    assertEquals("input=BLA" + input, expected, parser.toPacked("BLA" + input, 3, packing));
                }
            }
        }

        @Test
        public void toEpochDays(final LocalDate localDate) throws Exception {
            for (final DateParser parser : PARSERS) {
                final long epochDay = localDate.toEpochDay();
                final String input = formatInput(parser, localDate);
                assertEquals("input=" + input, epochDay, parser.toEpochDays(input));
                assertEquals("input=" + input, epochDay, parser.toEpochDays(input, AsciiReader.CHAR_SEQUENCE));
                assertEquals("input=BLA" + input, epochDay, parser.toEpochDays("BLA" + input, 3));
            }
        }

        @Test
        public void toEpochMillis(final LocalDate localDate) throws Exception {
            for (final DateParser parser : PARSERS) {
                final long epochMilli = localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
                final String input = formatInput(parser, localDate);
                assertEquals("input=" + input, epochMilli, parser.toEpochMillis(input));
                assertEquals("input=" + input, epochMilli, parser.toEpochMillis(input, AsciiReader.CHAR_SEQUENCE));
                assertEquals("input=BLA" + input, epochMilli, parser.toEpochMillis("BLA" + input, 3));
                assertEquals("input=BLABLA" + input, epochMilli, parser.toEpochMillis("BLABLA" + input, AsciiReader.CHAR_SEQUENCE, 6));
            }
        }

        @Test
        public void toLocalDate(final LocalDate localDate) throws Exception {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                assertEquals("input=" + input, localDate, parser.toLocalDate(input));
                assertEquals("input=" + input, localDate, parser.toLocalDate(input, AsciiReader.CHAR_SEQUENCE));
                assertEquals("input=BLA" + input, localDate, parser.toLocalDate("BLA" + input, 3));
            }
        }

        @Test
        public void toSeparator(final LocalDate localDate) throws Exception {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                for (int sep = 0; sep <= 1; sep++) {
                    assertEquals("input=" + input, parser.separator(), parser.toSeparator(input, sep));
                    assertEquals("input=" + input, parser.separator(), parser.toSeparator(input, AsciiReader.CHAR_SEQUENCE, sep));
                    assertEquals("input=BLA" + input, parser.separator(), parser.toSeparator("BLA" + input, 3, sep));
                }
            }
        }

        @Test
        public void isValid(final LocalDate localDate) throws Exception {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                assertTrue("input=" + input, parser.isValid(input));
                assertTrue("input=" + input, parser.isValid(input, AsciiReader.CHAR_SEQUENCE));
                assertTrue("input=BLA" + input, parser.isValid("BLA" + input, 3));
            }
        }

        private static String formatInput(final DateParser parser, final LocalDate localDate) {
            final String standardPattern = PATTERN_BY_FORMAT.get(parser.format());
            final String currentPattern = standardPattern.replace(DateParser.DEFAULT_SEPARATOR, parser.separator());
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(currentPattern);
            return formatter.format(localDate);
        }
    }

    @RunWith(Spockito.class)
    @Spockito.Unroll({
            "|  year | month | day |",
            "|     0 |    1  |   1 |",
            "|    -1 |    1  |   1 |",
            "| 10000 |    1  |   1 |",
            "|  2017 |    0  |   1 |",
            "|  2017 |   -1  |   1 |",
            "|  2017 |   13  |   1 |",
            "|  2017 |    1  |   0 |",
            "|  2017 |    4  |  -1 |",//NOTE: day=-1 is equivalent to day=31
            "|  2017 |    1  |  32 |",
            "|  2017 |    2  |  29 |",
            "|  2016 |    2  |  30 |",
            "|  2000 |    2  |  30 |",
            "|  1900 |    2  |  29 |",
            "|  1900 |    4  |  31 |",
            "|  1900 |    6  |  31 |",
            "|  1900 |    9  |  31 |",
            "|  1900 |   11  |  31 |",
    })
    @Spockito.Name("[{row}]: {year}/{month}/{day}")
    public static class Invalid {
//        @Test(expected = DateTimeException.class)
//        public void packIllegalYearMonthDayBinary(final int year, final int month, final int day) {
//            DateParser.BINARY.forValidationMethod(THROW_EXCEPTION).pack(year, month, day);
//        }
//
//        @Test
//        public void packInvalidYearMonthDayBinary(final int year, final int month, final int day) {
//            final int packed = DateParser.BINARY.forValidationMethod(INVALIDATE_RESULT).pack(year, month, day);
//            assertEquals("should be invalid", DateParser.INVALID, packed);
//        }
    }

    public static class Special {
        @Test
        public void format() throws Exception {
            for (final DateFormat format : DateFormat.values()) {
                assertSame(format, DateParser.valueOf(format).format());
                for (final char separator : SEPARATORS) {
                    assertSame(format, DateParser.valueOf(format, separator).format());
                    for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                        assertSame(format, DateParser.valueOf(format, validationMethod).format());
                        assertSame(format, DateParser.valueOf(format, separator, validationMethod).format());
                    }
                }
            }
        }

        @Test
        public void separator() throws Exception {
            char expected;
            for (final DateFormat format : DateFormat.values()) {
                expected = format.hasSeparators() ? DateParser.DEFAULT_SEPARATOR : DateParser.NO_SEPARATOR;
                assertEquals(expected, DateParser.valueOf(format).separator());
                for (final char separator : SEPARATORS) {
                    expected = format.hasSeparators() ? separator : DateParser.NO_SEPARATOR;
                    assertEquals(expected, DateParser.valueOf(format, separator).separator());
                    for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                        expected = format.hasSeparators() ? DateParser.DEFAULT_SEPARATOR : DateParser.NO_SEPARATOR;
                        assertEquals(expected, DateParser.valueOf(format, validationMethod).separator());
                        expected = format.hasSeparators() ? separator : DateParser.NO_SEPARATOR;
                        assertEquals(expected, DateParser.valueOf(format, separator, validationMethod).separator());
                    }
                }
            }
        }

        @Test
        public void validationMethod() throws Exception {
            for (final DateFormat format : DateFormat.values()) {
                assertSame(ValidationMethod.UNVALIDATED, DateParser.valueOf(format).validationMethod());
                for (final char separator : SEPARATORS) {
                    assertSame(ValidationMethod.UNVALIDATED, DateParser.valueOf(format, separator).validationMethod());
                    for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                        assertSame(validationMethod, DateParser.valueOf(format, validationMethod).validationMethod());
                        assertSame(validationMethod, DateParser.valueOf(format, separator, validationMethod).validationMethod());
                    }
                }
            }
        }
    }

    private static Map<DateFormat, String> patternByFormat() {
        final Map<DateFormat, String> formatters = new EnumMap<>(DateFormat.class);
        formatters.put(DateFormat.YYYYMMDD, "yyyyMMdd");
        formatters.put(DateFormat.MMDDYYYY, "MMddyyyy");
        formatters.put(DateFormat.DDMMYYYY, "ddMMyyyy");
        formatters.put(DateFormat.YYYY_MM_DD, "yyyy-MM-dd");
        formatters.put(DateFormat.MM_DD_YYYY, "MM-dd-yyyy");
        formatters.put(DateFormat.DD_MM_YYYY, "dd-MM-yyyy");
        return formatters;
    }

    private static DateParser[] initParsers() {
        final DateParser[] parsers = new DateParser[DateFormat.values().length * SEPARATORS.length * ValidationMethod.values().length];
        int index = 0;
        for (final DateFormat format : DateFormat.values()) {
            for (final char separator : SEPARATORS) {
                for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                    parsers[index++] = DateParser.valueOf(format, separator, validationMethod);
                }
            }
        }
        return parsers;
    }
}