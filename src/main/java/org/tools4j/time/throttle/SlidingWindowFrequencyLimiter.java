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

import org.tools4j.time.base.TimeFactors;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class SlidingWindowFrequencyLimiter {

    private static final int SLIDING_WINDOW_SIZE = 32;
    private final static long DEFAULT_WINDOW_SLIDING_TIME_NANOS = TimeUnit.SECONDS.toNanos(1);

    private final long sliceSlidingTimeNanos;
    private final BooleanSupplier invokable;
    private final double nanosPerRun;
    private final SlidingCountWindow slidingCountWindow;

    private BooleanSupplier action;

    private SlidingWindowFrequencyLimiter(final BooleanSupplier invokable, final double maxInvocationsPerSecond) {
        this.invokable = Objects.requireNonNull(invokable);
        if (maxInvocationsPerSecond <= 0) {
            throw new IllegalArgumentException("maxInvocationsPerSecond must be positive: " + maxInvocationsPerSecond);
        }
        this.nanosPerRun = TimeFactors.NANOS_PER_SECOND / maxInvocationsPerSecond;
        this.sliceSlidingTimeNanos = ceilDiv(windowSlidingTimeFor(maxInvocationsPerSecond), SLIDING_WINDOW_SIZE);
        this.action = this::initialRun;
        this.slidingCountWindow = new SlidingCountWindow(SLIDING_WINDOW_SIZE);
    }

    public static Runnable forRunnable(final Runnable runnable, final double maxInvocationsPerSecond) {
        final BooleanSupplier booleanSupplier = forBooleanSupplier(() -> {
            runnable.run();
            return true;
        }, maxInvocationsPerSecond);
        return () -> booleanSupplier.getAsBoolean();
    }

    public static BooleanSupplier forBooleanSupplier(final BooleanSupplier invokable, final double maxInvocationsPerSecond) {
        final SlidingWindowFrequencyLimiter lim = new SlidingWindowFrequencyLimiter(invokable, maxInvocationsPerSecond);
        return () -> lim.action.getAsBoolean();
    }

    private static double nanosPerRun(final double maxInvocationsPerSecond) {
        return TimeFactors.NANOS_PER_SECOND / maxInvocationsPerSecond;
    }

    private static long windowSlidingTimeFor(final double maxInvocationsPerSecond) {
        return Math.max((long)Math.ceil(SLIDING_WINDOW_SIZE * nanosPerRun(maxInvocationsPerSecond)), DEFAULT_WINDOW_SLIDING_TIME_NANOS);
    }

    private static long ceilDiv(final long dividend, final long divisor) {
        return (dividend + divisor - 1) / divisor;
    }

    private boolean initialRun() {
        slidingCountWindow.init(System.nanoTime());
        action = this::timedRun;
        return false;
    }

    private boolean timedRun() {
        final long nanoTime = System.nanoTime();
        final long deltaTimeNanos = slidingCountWindow.windowTime(nanoTime);
        final double expectedDeltaTimeNanos = nanosPerRun * slidingCountWindow.windowCount();
        if (slidingCountWindow.sliceTime(nanoTime) >= sliceSlidingTimeNanos) {
            slidingCountWindow.slide(nanoTime);
        }
        if (deltaTimeNanos >= expectedDeltaTimeNanos) {
            if (invokable.getAsBoolean()) {
                slidingCountWindow.incrementCount();
                return true;
            }
        }
        return false;
    }

    private static class SlidingCountWindow {
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
        int incrementIndex(final int index) {
            final int ix = index + 1;
            return ix < n ? ix : 0;
        }
        long windowTime(final long timeNanos) {
            return timeNanos - this.timeNanos[start];
        }
        long windowCount() {
            return count[end] - count[start];
        }
        void incrementCount() {
            count[end]++;
        }
        long sliceTime(final long timeNanos) {
            return timeNanos - this.timeNanos[end];
        }
    }
}
