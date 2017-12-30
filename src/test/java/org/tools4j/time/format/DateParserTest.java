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
import org.tools4j.time.validate.DateValidator;
import org.tools4j.time.validate.ValidationMethod;

import java.time.DateTimeException;
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
    private static final char BAD_SEPARATOR = ':';
    private static final Map<DateFormat, String> PATTERN_BY_FORMAT = patternByFormat();
    private static final DateParser[] PARSERS = initParsers();

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
        public void parseYear(final LocalDate localDate) throws Exception {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                assertEquals("input=" + input, localDate.getYear(), parser.parseYear(input));
                assertEquals("input=" + input, localDate.getYear(), parser.parseYear(input, AsciiReader.CHAR_SEQUENCE));
                assertEquals("input=BLA" + input, localDate.getYear(), parser.parseYear("BLA" + input, 3));
            }
        }

        @Test
        public void parseMonth(final LocalDate localDate) throws Exception {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                assertEquals("input=" + input, localDate.getMonthValue(), parser.parseMonth(input));
                assertEquals("input=" + input, localDate.getMonthValue(), parser.parseMonth(input, AsciiReader.CHAR_SEQUENCE));
                assertEquals("input=BLA" + input, localDate.getMonthValue(), parser.parseMonth("BLA" + input, 3));
            }
        }

        @Test
        public void parseDay(final LocalDate localDate) throws Exception {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                assertEquals("input=" + input, localDate.getDayOfMonth(), parser.parseDay(input));
                assertEquals("input=" + input, localDate.getDayOfMonth(), parser.parseDay(input, AsciiReader.CHAR_SEQUENCE));
                assertEquals("input=BLA" + input, localDate.getDayOfMonth(), parser.parseDay("BLA" + input, 3));
            }
        }

        @Test
        public void parseAsPackedDate(final LocalDate localDate) throws Exception {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                for (final Packing packing : Packing.values()) {
                    final int expected = DatePacker.valueOf(packing).pack(localDate);
                    assertEquals("input=" + input, expected, parser.parseAsPackedDate(input, packing));
                    assertEquals("input=" + input, expected, parser.parseAsPackedDate(input, AsciiReader.CHAR_SEQUENCE, packing));
                    assertEquals("input=BLA" + input, expected, parser.parseAsPackedDate("BLA" + input, 3, packing));
                }
            }
        }

        @Test
        public void parseAsEpochDay(final LocalDate localDate) throws Exception {
            for (final DateParser parser : PARSERS) {
                final long epochDay = localDate.toEpochDay();
                final String input = formatInput(parser, localDate);
                assertEquals("input=" + input, epochDay, parser.parseAsEpochDay(input));
                assertEquals("input=" + input, epochDay, parser.parseAsEpochDay(input, AsciiReader.CHAR_SEQUENCE));
                assertEquals("input=BLA" + input, epochDay, parser.parseAsEpochDay("BLA" + input, 3));
            }
        }

        @Test
        public void parseAsEpochMilli(final LocalDate localDate) throws Exception {
            for (final DateParser parser : PARSERS) {
                final long epochMilli = localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
                final String input = formatInput(parser, localDate);
                assertEquals("input=" + input, epochMilli, parser.parseAsEpochMilli(input));
                assertEquals("input=" + input, epochMilli, parser.parseAsEpochMilli(input, AsciiReader.CHAR_SEQUENCE));
                assertEquals("input=BLA" + input, epochMilli, parser.parseAsEpochMilli("BLA" + input, 3));
                assertEquals("input=BLABLA" + input, epochMilli, parser.parseAsEpochMilli("BLABLA" + input, AsciiReader.CHAR_SEQUENCE, 6));
            }
        }

        @Test
        public void parseAsLocalDate(final LocalDate localDate) throws Exception {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                assertEquals("input=" + input, localDate, parser.parseAsLocalDate(input));
                assertEquals("input=" + input, localDate, parser.parseAsLocalDate(input, AsciiReader.CHAR_SEQUENCE));
                assertEquals("input=BLA" + input, localDate, parser.parseAsLocalDate("BLA" + input, 3));
            }
        }

        @Test
        public void parseSeparator(final LocalDate localDate) throws Exception {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                for (int sep = 0; sep <= 1; sep++) {
                    assertEquals("input=" + input, parser.separator(), parser.parseSeparator(input, sep));
                    assertEquals("input=" + input, parser.separator(), parser.parseSeparator(input, AsciiReader.CHAR_SEQUENCE, sep));
                    assertEquals("input=BLA" + input, parser.separator(), parser.parseSeparator("BLA" + input, 3, sep));
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
    
    enum InvalidPart {
        YEAR, MONTH, DAY, SEPARATOR
    }

    @RunWith(Spockito.class)
    @Spockito.Unroll({
            "|  year | month | day | invalidPart |",
            "|     0 |    1  |   1 |     YEAR    |",
            "|    -1 |    1  |   1 |     YEAR    |",
            "|  -999 |    1  |   1 |     YEAR    |",
            "|  2017 |    0  |   1 |    MONTH    |",
            "|  2017 |   -1  |   1 |    MONTH    |",
            "|  2017 |   13  |   1 |    MONTH    |",
            "|  2017 |    1  |   0 |     DAY     |",
            "|  2017 |    4  |  -1 |     DAY     |",//NOTE: day=-1 is equivalent to day=31
            "|  2017 |    1  |  32 |     DAY     |",
            "|  2017 |    2  |  29 |     DAY     |",
            "|  2016 |    2  |  30 |     DAY     |",
            "|  2000 |    2  |  30 |     DAY     |",
            "|  1900 |    2  |  29 |     DAY     |",
            "|  1900 |    4  |  31 |     DAY     |",
            "|  1900 |    6  |  31 |     DAY     |",
            "|  1900 |    9  |  31 |     DAY     |",
            "|  1900 |   11  |  31 |     DAY     |",
            "|  1900 |   11  |  30 |  SEPARATOR  |",
    })
    @Spockito.Name("[{row}]: {year}/{month}/{day}, invalidPart={invalidPart}")
    public static class Invalid {
        @Test
        public void parseYear(final int year, final int month, final int day, final InvalidPart invalidPart) throws Exception {
            final boolean isInvalid = invalidPart == InvalidPart.YEAR;
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, year, month, day, invalidPart);
                DateTimeException exception = null;
                int result = DateValidator.YEAR_MAX + 1;
                try {
                    result = parser.parseYear(input);
                } catch (final DateTimeException e) {
                    exception = e;
                }
                if (isInvalid) {
                    assertValue(parser, input, exception, result);
                } else {
                    assertEquals("Wrong year for input=" + input, year, result);
                }
            }
        }

        @Test
        public void parseMonth(final int year, final int month, final int day, final InvalidPart invalidPart) throws Exception {
            final boolean isInvalid = invalidPart == InvalidPart.MONTH;
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, year, month, day, invalidPart);
                DateTimeException exception = null;
                int result = DateValidator.YEAR_MAX + 1;
                try {
                    result = parser.parseMonth(input);
                } catch (final DateTimeException e) {
                    exception = e;
                }
                if (isInvalid) {
                    assertValue(parser, input, exception, result);
                } else {
                    assertEquals("Wrong month for input=" + input, month, result);
                }
            }
        }

        @Test
        public void parseDay(final int year, final int month, final int day, final InvalidPart invalidPart) throws Exception {
            final boolean isInvalid = invalidPart != InvalidPart.SEPARATOR;
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, year, month, day, invalidPart);
                DateTimeException exception = null;
                int result = DateValidator.YEAR_MAX + 1;
                try {
                    result = parser.parseDay(input);
                } catch (final DateTimeException e) {
                    exception = e;
                }
                if (isInvalid) {
                    assertValue(parser, input, exception, result);
                } else {
                    assertEquals("Wrong day for input=" + input, day, result);
                }
            }
        }

        @Test
        public void parseAsPackedDate(final int year, final int month, final int day, final InvalidPart invalidPart) throws Exception {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, year, month, day, invalidPart);
                for (final Packing packing : Packing.values()) {
                    DateTimeException exception = null;
                    int result = DateValidator.YEAR_MAX + 1;
                    try {
                        result = parser.parseAsPackedDate(input, packing);
                    } catch (final DateTimeException e) {
                        exception = e;
                    }
                    if (!isValid(parser, invalidPart)) {
                        assertValue(parser, input, exception, result);
                    }
                }
            }
        }

        @Test
        public void parseAsEpochDay(final int year, final int month, final int day, final InvalidPart invalidPart) throws Exception {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, year, month, day, invalidPart);
                DateTimeException exception = null;
                long result = DateValidator.YEAR_MAX + 1;
                try {
                    result = parser.parseAsEpochDay(input);
                } catch (final DateTimeException e) {
                    exception = e;
                }
                if (!isValid(parser, invalidPart)) {
                    assertEpoch(parser, input, exception, result);
                }
            }
        }

        @Test
        public void parseAsEpochMilli(final int year, final int month, final int day, final InvalidPart invalidPart) throws Exception {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, year, month, day, invalidPart);
                DateTimeException exception = null;
                long result = DateValidator.YEAR_MAX + 1;
                try {
                    result = parser.parseAsEpochMilli(input);
                } catch (final DateTimeException e) {
                    exception = e;
                }
                if (!isValid(parser, invalidPart)) {
                    assertEpoch(parser, input, exception, result);
                }
            }
        }

        @Test
        public void parseAsLocalDate(final int year, final int month, final int day, final InvalidPart invalidPart) throws Exception {
            for (final DateParser parser : PARSERS) {
                final boolean shouldFail =
                        invalidPart != InvalidPart.SEPARATOR ||
                        invalidPart == InvalidPart.SEPARATOR && parser.validationMethod() != ValidationMethod.UNVALIDATED &&
                                (parser.format().hasSeparators() && parser.separator() != DateParser.NO_SEPARATOR);

                final String input = formatInput(parser, year, month, day, invalidPart);
                try {
                    parser.parseAsLocalDate(input);
                    if (shouldFail) {
                        fail("toLocalDate should ALWAYS throw exception for parser=" + parser + " and input=" + input);
                    }
                } catch (final DateTimeException e) {
                    if (!shouldFail) {
                        throw new AssertionError(
                                "toLocalDate should NEVER throw exception for parser=" + parser +
                                        " and input=" + input + " but we caught exception=" + e, e);
                    }
                }
            }
        }

        @Test
        public void parseSeparator(final int year, final int month, final int day, final InvalidPart invalidPart) throws Exception {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, year, month, day, invalidPart);
                for (int sep = 0; sep <= 1; sep++) {
                    DateTimeException exception = null;
                    char result = (char)(Byte.MAX_VALUE + 1);
                    try {
                        result = parser.parseSeparator(input, sep);
                    } catch (final DateTimeException e) {
                        exception = e;
                    }
                    if (!isValid(parser, invalidPart) && invalidPart == InvalidPart.SEPARATOR) {
                        assertSeparator(parser, input, exception, result);
                    } else {
                        final char expected = parser.format().hasSeparators() ?
                                (invalidPart == InvalidPart.SEPARATOR ? BAD_SEPARATOR : parser.separator()) :
                                DateParser.NO_SEPARATOR;
                        assertEquals("input=" + input + ", parser=" + parser, expected, result);
                    }
                }
            }
        }

        @Test
        public void isValid(final int year, final int month, final int day, final InvalidPart invalidPart) throws Exception {
            for (final DateParser parser : PARSERS) {
                final boolean isValid = isValid(parser, invalidPart);
                final String input = formatInput(parser, year, month, day, invalidPart);
                assertEquals("input=" + input + ", parser=" + parser, isValid, parser.isValid(input));
                assertEquals("input=" + input + ", parser=" + parser, isValid, parser.isValid(input, AsciiReader.CHAR_SEQUENCE));
                assertEquals("input=BLA" + input + ", parser=" + parser, isValid, parser.isValid("BLA" + input, 3));
            }
        }

        private static boolean isValid(final DateParser parser, final InvalidPart invalidPart) {
            return invalidPart == InvalidPart.SEPARATOR &&
                    (!parser.format().hasSeparators() || parser.separator() == DateParser.NO_SEPARATOR);
        }

        private static void assertEpoch(final DateParser parser, final String input,
                                        final DateTimeException exception, final long result) {
            assertResult(parser, input, exception, result, DateParser.INVALID_EPOCH);
        }
        private static void assertSeparator(final DateParser parser, final String input,
                                            final DateTimeException exception, final char result) {
            assertResult(parser, input, exception, (byte)result, DateParser.INVALID_SEPARATOR);
        }
        private static void assertValue(final DateParser parser, final String input,
                                        final DateTimeException exception, final int result) {
            assertResult(parser, input, exception, result, DateParser.INVALID);
        }
        private static void assertResult(final DateParser parser, final String input,
                                         final DateTimeException exception,
                                         final long result, final long invalidValue) {
            switch (parser.validationMethod()) {
                case UNVALIDATED:
                    assertNull("Unvalidating parser should not throw an exception for input='" +
                            input + "' and parser=" + parser, exception);
                    assertNotEquals("Unvalidating parser should not return INVALID for input='" +
                            input + "' and parser=" + parser, invalidValue, result);
                    break;
                case INVALIDATE_RESULT:
                    assertNull("Invalidate-result parser should not throw an exception for input='" +
                            input + "' and parser=" + parser, exception);
                    assertEquals("Invalidate-result parser should return INVALID for input='" +
                            input + "' and parser=" + parser, invalidValue, result);
                    break;
                case THROW_EXCEPTION:
                    assertNotNull("Throw-exception parser should throw an exception for input='" +
                            input + "' and parser=" + parser, exception);
                    break;
                default:
                    throw new RuntimeException("Unsupported validation method: " + parser.validationMethod());
            }
        }

        private static String formatInput(final DateParser parser,
                                          final int year, final int month, final int day, final InvalidPart invalidPart) {
            final char separator = invalidPart == InvalidPart.SEPARATOR ? BAD_SEPARATOR : parser.separator();
            final String standardPattern = PATTERN_BY_FORMAT.get(parser.format());
            final String currentPattern = standardPattern.replace(DateParser.DEFAULT_SEPARATOR, separator);
            return currentPattern
                    .replace("yyyy", toFixedLength(4, year))
                    .replace("MM", toFixedLength(2, month))
                    .replace("dd", toFixedLength(2, day));
        }

        private static final String toFixedLength(final int length, final int value) {
            final StringBuilder sb = new StringBuilder(length);
            sb.append(value);
            while (sb.length() < length) {
                sb.insert(0, '0');
            }
            return sb.substring(0, length);
        }
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

        @Test
        public void to_String() throws Exception {
            String expected;
            for (final DateFormat format : DateFormat.values()) {
                expected = "SimpleDateParser[format=" + format + ", separator=" + separatorString(format) + "]";
                assertEquals(expected, DateParser.valueOf(format).toString());
                for (final char separator : SEPARATORS) {
                    expected = "SimpleDateParser[format=" + format + ", separator=" + separatorString(format, separator) + "]";
                    assertEquals(expected, DateParser.valueOf(format, separator).toString());
                    for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                        final String master = validationMethod == ValidationMethod.UNVALIDATED ? "" +
                                "SimpleDateParser[format=%s, separator=%s]" :
                                "ValidatingDateParser[format=%s, separator=%s, validationMethod=%s]";
                        expected = String.format(master, format, separatorString(format), validationMethod);
                        assertEquals(expected, DateParser.valueOf(format, validationMethod).toString());
                        expected = String.format(master, format, separatorString(format, separator), validationMethod);
                        assertEquals(expected, DateParser.valueOf(format, separator, validationMethod).toString());
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

    static String separatorString(final DateFormat format) {
        return separatorString(format, DateFormatter.DEFAULT_SEPARATOR);
    }

    static String separatorString(final DateFormat format, final char separator) {
        return format.hasSeparators() && separator != DateFormatter.NO_SEPARATOR ?
                "'" + separator + "'" : "<none>";
    }
}