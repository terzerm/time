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
import java.util.function.BooleanSupplier;

public class SlidingCountWindowFrequencyLimiter {

    private static final int SLIDING_WINDOW_MIN_SIZE = 4;
    private static final int SLIDING_WINDOW_MAX_SIZE = 1024;
    private static final long CYCLE_MIN_COUNT = 512;
    private static final long CYCLE_MIN_MILLIS = 20;
    private static final long CYCLE_MAX_MILLIS = 4000;

    private final long sliceMaxCount;
    private final BooleanSupplier invokable;
    private final double nanosPerRun;
    private final SlidingCountWindow slidingCountWindow;

    private BooleanSupplier action;

    private SlidingCountWindowFrequencyLimiter(final BooleanSupplier invokable, final double maxInvocationsPerSecond,
                                               final int slidingWindowSize, final long sliceMaxCount) {
        if (maxInvocationsPerSecond <= 0) {
            throw new IllegalArgumentException("maxInvocationsPerSecond must be positive: " + maxInvocationsPerSecond);
        }
        if (sliceMaxCount <= 0) {
            throw new IllegalArgumentException("sliceMaxCount must be positive: " + sliceMaxCount);
        }
        this.invokable = Objects.requireNonNull(invokable);
        this.nanosPerRun = TimeFactors.NANOS_PER_SECOND / maxInvocationsPerSecond;
        this.sliceMaxCount = sliceMaxCount;//Math.max(1, (long)Math.ceil(maxInvocationsPerSecond / slidingWindowSize));
        this.action = this::initialRun;
        this.slidingCountWindow = new SlidingCountWindow(slidingWindowSize);
    }

    public static Runnable forRunnable(final Runnable runnable, final double maxInvocationsPerSecond) {
        final BooleanSupplier booleanSupplier = forBooleanSupplier(() -> {
            runnable.run();
            return true;
        }, maxInvocationsPerSecond);
        return () -> booleanSupplier.getAsBoolean();
    }

    public static Runnable forRunnable(final Runnable runnable, final double maxInvocationsPerSecond,
                                       final int slidingWindowSize, final long sliceMaxCount) {
        final BooleanSupplier booleanSupplier = forBooleanSupplier(() -> {
            runnable.run();
            return true;
        }, maxInvocationsPerSecond, slidingWindowSize, sliceMaxCount);
        return () -> booleanSupplier.getAsBoolean();
    }

    public static BooleanSupplier forBooleanSupplier(final BooleanSupplier invokable, final double maxInvocationsPerSecond) {
        final long cycleMillis = Math.min(
                CYCLE_MAX_MILLIS,
                Math.max(CYCLE_MIN_MILLIS, (long) (CYCLE_MIN_COUNT * 1000 / maxInvocationsPerSecond))
        );
        final int slidingWindowSize = Math.min(
                SLIDING_WINDOW_MAX_SIZE,
                Math.max(SLIDING_WINDOW_MIN_SIZE, (int)Math.sqrt(maxInvocationsPerSecond * cycleMillis / 1000))
        );
        final long slizeMaxCount = Math.max(1, (long)(maxInvocationsPerSecond * cycleMillis / 1000 / slidingWindowSize));
        //FIXME remove below line
        System.out.println("cycleMillis=" + cycleMillis + ", slidingWindowSize=" + slidingWindowSize + ", sliceMaxCount=" + slizeMaxCount);
        return forBooleanSupplier(invokable, maxInvocationsPerSecond, slidingWindowSize, slizeMaxCount);
    }

    public static BooleanSupplier forBooleanSupplier(final BooleanSupplier invokable, final double maxInvocationsPerSecond,
                                                     final int slidingWindowSize, final long sliceMaxCount) {
        final SlidingCountWindowFrequencyLimiter lim = new SlidingCountWindowFrequencyLimiter(
                invokable, maxInvocationsPerSecond, slidingWindowSize, sliceMaxCount);
        return () -> lim.action.getAsBoolean();
    }

    private static double nanosPerRun(final double maxInvocationsPerSecond) {
        return TimeFactors.NANOS_PER_SECOND / maxInvocationsPerSecond;
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
        if (slidingCountWindow.sliceCount() >= sliceMaxCount) {
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
        int decrementIndex(final int index) {
            final int ix = index - 1;
            return ix >= 0 ? ix : n-1;
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
        long sliceCount() {
            return count[end] - count[decrementIndex(end)];
        }
    }
}
