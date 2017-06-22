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

import java.util.function.Consumer;

/**
 * Defines different methods to pack multiple values into an integer value.
 */
public enum Packing {
    /**
     * The packing is performed by shifting the components to particular positions of an int or long value.
     * <p>
     * This packing method is more efficient than {@link #DECIMAL} but is usually not human readable.
     */
    BINARY,
    /**
     * The packing is performed by multiplying the components with powers of ten and then adding those multiples.
     * <p>
     * This packing method is less efficient than {@link #BINARY} but is usually human readable, for instance packing
     * th date 30st April 1979 as 19790430.
     */
    DECIMAL;

    private static Packing[] VALUES = values();

    public static final int length() {
        return VALUES.length;
    }

    public static final Packing valueByOrdinal(final int ordinal) {
        return VALUES[ordinal];
    }

    public static final void forEach(final Consumer<? super Packing> consumer) {
        for (final Packing packing : VALUES) {
            consumer.accept(packing);
        }
    }
}
