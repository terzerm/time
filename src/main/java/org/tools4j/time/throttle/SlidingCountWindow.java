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
package org.tools4j.time.throttle;

final class SlidingCountWindow {
    private final int n;
    private long[] timeNanos;
    private long[] count;
    private int start;
    private int end;

    SlidingCountWindow(final int len) {
        this.n = len + 1;
        this.timeNanos = new long[n];
        this.count = new long[n];
    }

    void init(final long nanoTime) {
        timeNanos[0] = nanoTime;
        count[0] = 0;
        timeNanos[1] = nanoTime;
        count[1] = 0;
        start = 0;
        end = 1;
    }

    void slide(final long nanoTime) {
        final long cnt = count[end];
        timeNanos[end] = nanoTime;
        end = incrementIndex(end);
        if (end == start) {
            start = incrementIndex(start);
        }
        timeNanos[end] = nanoTime;
        count[end] = cnt;

    }

    private int incrementIndex(final int index) {
        final int ix = index + 1;
        return ix < n ? ix : 0;
    }

    private int decrementIndex(final int index) {
        final int ix = index - 1;
        return ix >= 0 ? ix : n - 1;
    }

    long windowTime(final long timeNanos) {
        return timeNanos - this.timeNanos[start];
    }

    long sliceTime(final long timeNanos) {
        return timeNanos - this.timeNanos[end];
    }

    long windowCount() {
        return count[end] - count[start];
    }

    void incrementCount() {
        count[end]++;
    }

    long sliceCount() {
        return count[end] - count[decrementIndex(end)];
    }
}
