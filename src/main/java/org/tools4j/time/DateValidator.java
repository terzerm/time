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

public final class DateValidator {

    public static boolean isValidYear(final long year) {
        return 1 <= year & year <= 9999;
    }

    public static boolean isValidYear(final int year) {
        return 1 <= year & year <= 9999;
    }

    public static boolean isValidMonth(final int month) {
        return 1 <= month & month <= 12;
    }

    public static boolean isValidDate(final int year, final int month, final int day) {
        if (isValidYear(year) & isValidMonth(month)) {
            return isValidDay(year, month, day);
        }
        return false;
    }

    //PRECONDITION: valid year and month
    private static boolean isValidDay(final int year, final int month, final int day) {
        if (day > 0) {
            if (day <= 28) {
                return true;
            }
            if (day <= 31) {
                if (month != 2) {
                    if (day <= 30) {
                        return true;
                    }
                    return (month < 8) ^ ((month & 0x1) == 0);
                }
                if (day <= 29) {
                    if ((year % 4) != 0) {
                        return false;
                    }
                    return ((year % 100) != 0) | ((year % 400) == 0);
                }
            }
        }
        return false;
    }

    public static int checkValidYear(final long year) {
        if (isValidYear(year)) {
            return (int)year;
        }
        throw new IllegalArgumentException("Invalid year, must be in [1,9999] but was: " + year);
    }

    public static int checkValidYear(final int year) {
        if (isValidYear(year)) {
            return year;
        }
        throw new IllegalArgumentException("Invalid year, must be in [1,9999] but was: " + year);
    }

    public static int checkValidMonth(final int month) {
        if (isValidMonth(month)) {
            return month;
        }
        throw new IllegalArgumentException("Invalid month, must be in [1,12] but was: " + month);
    }

    public static int checkValidDate(final int year, final int month, final int day) {
        checkValidYear(year);
        checkValidMonth(month);
        if (isValidDay(year, month, day)) {
            return day;
        }
        throw new IllegalArgumentException("Invalid day in date: " + year + "-" + month + "-" + day);
    }

    private DateValidator() {
        throw new RuntimeException("No DateValidator for you!");
    }
}
