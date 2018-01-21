/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2018 tools4j.org (Marco Terzer)
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
import org.tools4j.time.base.Epoch;
import org.tools4j.time.base.TimeFactors;
import org.tools4j.time.pack.DatePacker;
import org.tools4j.time.pack.Packing;
import org.tools4j.time.validate.DateValidator;
import org.tools4j.time.validate.ValidationMethod;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.tools4j.time.format.DateParserTest.separatorString;

/**
 * Unit test for {@link DateFormatter}.
 */
public class DateFormatterTest {

    private static final char[] SEPARATORS = {DateFormatter.NO_SEPARATOR, '-', '/', '.', '_'};
    private static final char BAD_SEPARATOR = ':';
    private static final Map<DateFormat, String> PATTERN_BY_FORMAT = patternByFormat();
    private static final DateFormatter[] FORMATTERS = initFormatters();

    @RunWith(Spockito.class)
    @Spockito.Unroll({
            "|  localDate |",
            "| 2017-01-01 |",
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
        public void format(final LocalDate localDate) throws Exception {
            for (final DateFormatter formatter : FORMATTERS) {
                final int len = formatter.format().length();
                final String expected = expected(formatter, localDate);
                final StringBuilder actual1 = new StringBuilder();
                formatter.format(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(),
                        actual1);
                assertEquals("input=" + localDate, expected, actual1.toString());
                final StringBuilder actual2 = new StringBuilder("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                formatter.format(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(),
                        actual2, AsciiWriter.STRING_BUILDER);
                assertEquals("input=" + localDate, expected + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(expected.length()),
                        actual2.toString());
                final StringBuilder actual3 = new StringBuilder("ABCDE");
                formatter.format(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(),
                        actual3, AsciiWriter.STRING_BUILDER, 3);
                assertEquals("input=" + localDate, "ABC" + expected, actual3.toString());
            }
        }

        @Test
        public void formatPackedDate(final LocalDate localDate) throws Exception {
            for (final DateFormatter formatter : FORMATTERS) {
                final int len = formatter.format().length();
                for (final Packing packing : Packing.values()) {
                    final String expected = expected(formatter, localDate);
                    final int packed = DatePacker.valueOf(packing).pack(localDate);
                    final StringBuilder actual1 = new StringBuilder();
                    assertEquals("format=" + formatter.format(), len,
                            formatter.formatPackedDate(packed, packing, actual1));
                    assertEquals("input=" + localDate, expected, actual1.toString());
                    final StringBuilder actual2 = new StringBuilder("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                    assertEquals("format=" + formatter.format(), len,
                            formatter.formatPackedDate(packed, packing, actual2, AsciiWriter.STRING_BUILDER));
                    assertEquals("input=" + localDate,
                            expected + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(expected.length()),
                            actual2.toString());
                    final StringBuilder actual3 = new StringBuilder("ABCDE");
                    assertEquals("format=" + formatter.format(), len,
                            formatter.formatPackedDate(packed, packing, actual3, AsciiWriter.STRING_BUILDER, 3));
                    assertEquals("input=" + localDate, "ABC" + expected, actual3.toString());
                }
            }
        }

        @Test
        public void formatEpochDay(final LocalDate localDate) throws Exception {
            for (final DateFormatter formatter : FORMATTERS) {
                final int len = formatter.format().length();
                final String expected = expected(formatter, localDate);
                final StringBuilder actual1 = new StringBuilder();
                assertEquals("format=" + formatter.format(), len,
                        formatter.formatEpochDay(localDate.toEpochDay(), actual1));
                assertEquals("input=" + localDate, expected, actual1.toString());
                final StringBuilder actual2 = new StringBuilder("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                assertEquals("format=" + formatter.format(), len,
                        formatter.formatEpochDay(localDate.toEpochDay(),
                                actual2, AsciiWriter.STRING_BUILDER));
                assertEquals("input=" + localDate,
                        expected + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(expected.length()),
                        actual2.toString());
                final StringBuilder actual3 = new StringBuilder("ABCDE");
                assertEquals("format=" + formatter.format(), len,
                        formatter.formatEpochDay(localDate.toEpochDay(),
                                actual3, AsciiWriter.STRING_BUILDER, 3));
                assertEquals("input=" + localDate, "ABC" + expected, actual3.toString());
            }
        }

        @Test
        public void formatEpochMilli(final LocalDate localDate) throws Exception {
            for (final DateFormatter formatter : FORMATTERS) {
                final int len = formatter.format().length();
                final String expected = expected(formatter, localDate);
                final StringBuilder actual1 = new StringBuilder();
                assertEquals("format=" + formatter.format(), len, formatter.formatEpochMilli(localDate.toEpochDay() * TimeFactors.MILLIS_PER_DAY, actual1));
                assertEquals("input=" + localDate, expected, actual1.toString());
                final StringBuilder actual2 = new StringBuilder("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                assertEquals("format=" + formatter.format(), len,
                        formatter.formatEpochMilli(localDate.toEpochDay() * TimeFactors.MILLIS_PER_DAY,
                        actual2, AsciiWriter.STRING_BUILDER));
                assertEquals("input=" + localDate,
                        expected + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(expected.length()),
                        actual2.toString());
                final StringBuilder actual3 = new StringBuilder("ABCDE");
                final int randomMillis = (int)(TimeFactors.MILLIS_PER_DAY * Math.random());
                assertEquals("format=" + formatter.format(), len,
                        formatter.formatEpochMilli(localDate.toEpochDay() * TimeFactors.MILLIS_PER_DAY + randomMillis,
                        actual3, AsciiWriter.STRING_BUILDER, 3));
                assertEquals("input=" + localDate, "ABC" + expected, actual3.toString());
            }
        }

        @Test
        public void formatLocalDate(final LocalDate localDate) throws Exception {
            for (final DateFormatter formatter : FORMATTERS) {
                final int len = formatter.format().length();
                final String expected = expected(formatter, localDate);
                final StringBuilder actual1 = new StringBuilder();
                assertEquals("format=" + formatter.format(), len,
                        formatter.formatLocalDate(localDate, actual1));
                assertEquals("input=" + localDate, expected, actual1.toString());
                final StringBuilder actual2 = new StringBuilder("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                assertEquals("format=" + formatter.format(), len,
                        formatter.formatLocalDate(localDate, actual2, AsciiWriter.STRING_BUILDER));
                assertEquals("input=" + localDate,
                        expected + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(expected.length()),
                        actual2.toString());
                final StringBuilder actual3 = new StringBuilder("ABCDE");
                assertEquals("format=" + formatter.format(), len, formatter.formatLocalDate(localDate, actual3, AsciiWriter.STRING_BUILDER, 3));
                assertEquals("input=" + localDate, "ABC" + expected, actual3.toString());
            }
        }

        private static String expected(final DateFormatter formatter, final LocalDate localDate) {
            final String standardPattern = PATTERN_BY_FORMAT.get(formatter.format());
            final String currentPattern = standardPattern.replace(DateParser.DEFAULT_SEPARATOR, formatter.separator());
            final DateTimeFormatter javaFormatter = DateTimeFormatter.ofPattern(currentPattern);
            return javaFormatter.format(localDate);
        }
    }

    @RunWith(Spockito.class)
    @Spockito.Unroll({
            "|  year | month | day |",
            "|     0 |    1  |   1 |",
            "|    -1 |    1  |   1 |",
            "|  -999 |    1  |   1 |",
            "|  2017 |    0  |   1 |",
            "|  2017 |   -1  |   1 |",
            "|  2017 |   13  |   1 |",
            "|  2017 |    1  |   0 |",
            "|  2017 |    2  |  -2 |",//NOTE: day=-2 is equivalent to day=30
            "|  2017 |    1  |  32 |",
            "|  2017 |    2  |  29 |",
            "|  2016 |    2  |  30 |",
            "|  2000 |    2  |  30 |",
            "|  1900 |    2  |  29 |",
            "|  1900 |    4  |  31 |",
            "|  1900 |    6  |  31 |",
            "|  1900 |    9  |  31 |",
            "|  1900 |   11  |  31 |",
            "| 10000 |    1  |   1 |",
    })
    @Spockito.Name("[{row}]: {year}/{month}/{day}")
    public static class Invalid {
        @Test
        public void format(final int year, final int month, final int day) throws Exception {
            for (final DateFormatter formatter : FORMATTERS) {
                DateTimeException exception = null;
                int result = 0;
                try {
                    result = formatter.format(year, month, day, new StringBuilder());
                } catch (final DateTimeException e) {
                    exception = e;
                }
                assertResult(formatter, String.format("%d/%d/%d", year, month, day),
                        exception, result);
            }
        }

        @Test
        public void formatPackedDate(final int year, final int month, final int day) throws Exception {
            for (final DateFormatter formatter : FORMATTERS) {
                for (final Packing packing : Packing.values()) {
                    final int packedDate = DatePacker.valueOf(packing).pack(year, month, day);
                    DateTimeException exception = null;
                    int result = 0;
                    try {
                        result = formatter.formatPackedDate(packedDate, packing, new StringBuilder());
                    } catch (final DateTimeException e) {
                        exception = e;
                    }
                    assertResult(formatter, String.format("%d/%d/%d", year, month, day),
                            exception, result);
                }
            }
        }

        @Test
        public void formatEpochDay(final int year, final int month, final int day) throws Exception {
            if (!DateValidator.isValidYear(year)) {
                final long epochDays = Epoch.valueOf(ValidationMethod.UNVALIDATED).toEpochDay(year, month, day);
                for (final DateFormatter formatter : FORMATTERS) {
                    DateTimeException exception = null;
                    int result = 0;
                    try {
                        result = formatter.formatEpochDay(epochDays, new StringBuilder());
                    } catch (final DateTimeException e) {
                        exception = e;
                    }
                    assertResult(formatter, String.format("%d/%d/%d", year, month, day),
                            exception, result);
                }
            }
        }

        @Test
        public void formatEpochMilli(final int year, final int month, final int day) throws Exception {
            if (!DateValidator.isValidYear(year)) {
                final long epochDays = Epoch.valueOf(ValidationMethod.UNVALIDATED).toEpochMilli(year, month, day);
                for (final DateFormatter formatter : FORMATTERS) {
                    DateTimeException exception = null;
                    int result = 0;
                    try {
                        result = formatter.formatEpochMilli(epochDays, new StringBuilder());
                    } catch (final DateTimeException e) {
                        exception = e;
                    }
                    assertResult(formatter, String.format("%d/%d/%d", year, month, day),
                            exception, result);
                }
            }
        }

        @Test
        public void formatLocalDate(final int year, final int month, final int day) throws Exception {
            if (!DateValidator.isValidYear(year)) {
                final LocalDate localDate = LocalDate.of(year, month, day);
                for (final DateFormatter formatter : FORMATTERS) {
                    DateTimeException exception = null;
                    int result = 0;
                    try {
                        result = formatter.formatLocalDate(localDate, new StringBuilder());
                    } catch (final DateTimeException e) {
                        exception = e;
                    }
                    assertResult(formatter, String.format("%d/%d/%d", year, month, day),
                            exception, result);
                }
            }
        }

        private static void assertResult(final DateFormatter formatter,
                                         final Object input,
                                         final DateTimeException exception,
                                         final int result) {
            switch (formatter.validationMethod()) {
                case UNVALIDATED:
                    assertNull("Unvalidating formatter should not throw an exception for input='" +
                            input + "' and formatter=" + formatter, exception);
                    assertNotEquals("Unvalidating parser should not return INVALID for input='" +
                            input + "' and formatter=" + formatter, DateFormatter.INVALID, result);
                    break;
                case INVALIDATE_RESULT:
                    assertNull("Invalidate-result formatter should not throw an exception for input='" +
                            input + "' and formatter=" + formatter, exception);
                    assertEquals("Invalidate-result formatter should return INVALID for input='" +
                            input + "' and formatter=" + formatter, DateFormatter.INVALID, result);
                    break;
                case THROW_EXCEPTION:
                    assertNotNull("Throw-exception formatter should throw an exception for input='" +
                            input + "' and formatter=" + formatter, exception);
                    break;
                default:
                    throw new RuntimeException("Unsupported validation method: " + formatter.validationMethod());
            }
        }
    }

    public static class Special {
        @Test
        public void format() throws Exception {
            for (final DateFormat format : DateFormat.values()) {
                assertSame(format, DateFormatter.valueOf(format).format());
                for (final char separator : SEPARATORS) {
                    assertSame(format, DateFormatter.valueOf(format, separator).format());
                    for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                        assertSame(format, DateFormatter.valueOf(format, validationMethod).format());
                        assertSame(format, DateFormatter.valueOf(format, separator, validationMethod).format());
                    }
                }
            }
        }

        @Test
        public void separator() throws Exception {
            char expected;
            for (final DateFormat format : DateFormat.values()) {
                expected = format.hasSeparators() ? DateFormatter.DEFAULT_SEPARATOR : DateFormatter.NO_SEPARATOR;
                assertEquals(expected, DateFormatter.valueOf(format).separator());
                for (final char separator : SEPARATORS) {
                    expected = format.hasSeparators() ? separator : DateFormatter.NO_SEPARATOR;
                    assertEquals(expected, DateFormatter.valueOf(format, separator).separator());
                    for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                        expected = format.hasSeparators() ? DateFormatter.DEFAULT_SEPARATOR : DateFormatter.NO_SEPARATOR;
                        assertEquals(expected, DateFormatter.valueOf(format, validationMethod).separator());
                        expected = format.hasSeparators() ? separator : DateFormatter.NO_SEPARATOR;
                        assertEquals(expected, DateFormatter.valueOf(format, separator, validationMethod).separator());
                    }
                }
            }
        }

        @Test
        public void to_String() throws Exception {
            String expected;
            for (final DateFormat format : DateFormat.values()) {
                expected = "SimpleDateFormatter[format=" + format + ", separator=" + separatorString(format) + "]";
                assertEquals(expected, DateFormatter.valueOf(format).toString());
                for (final char separator : SEPARATORS) {
                    expected = "SimpleDateFormatter[format=" + format + ", separator=" + separatorString(format, separator) + "]";
                    assertEquals(expected, DateFormatter.valueOf(format, separator).toString());
                    for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                        expected = "SimpleDateFormatter[format=" + format + ", separator=" + separatorString(format) + "]";
                        assertEquals(expected, DateFormatter.valueOf(format, validationMethod).toString());
                        expected = "SimpleDateFormatter[format=" + format + ", separator=" + separatorString(format, separator) + "]";
                        assertEquals(expected, DateFormatter.valueOf(format, separator, validationMethod).toString());
                    }
                }
            }
        }

        @Test
        public void validationMethod() throws Exception {
            for (final DateFormat format : DateFormat.values()) {
                assertSame(ValidationMethod.UNVALIDATED, DateFormatter.valueOf(format).validationMethod());
                for (final char separator : SEPARATORS) {
                    assertSame(ValidationMethod.UNVALIDATED, DateFormatter.valueOf(format, separator).validationMethod());
                    for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                        assertSame(validationMethod, DateFormatter.valueOf(format, validationMethod).validationMethod());
                        assertSame(validationMethod, DateFormatter.valueOf(format, separator, validationMethod).validationMethod());
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

    private static DateFormatter[] initFormatters() {
        final DateFormatter[] formatters = new DateFormatter[DateFormat.values().length * SEPARATORS.length * ValidationMethod.values().length];
        int index = 0;
        for (final DateFormat format : DateFormat.values()) {
            for (final char separator : SEPARATORS) {
                for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                    formatters[index++] = DateFormatter.valueOf(format, separator, validationMethod);
                }
            }
        }
        return formatters;
    }
}