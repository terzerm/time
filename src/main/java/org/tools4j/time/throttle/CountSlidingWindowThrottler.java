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

public class CountSlidingWindowThrottler {

    private static final int SLIDING_WINDOW_MIN_SIZE = 4;
    private static final int SLIDING_WINDOW_MAX_SIZE = 1024;
    private static final long CYCLE_MIN_COUNT = 1024;
    private static final long CYCLE_MIN_MILLIS = 50;
    private static final long CYCLE_MAX_MILLIS = 4000;

    private final long sliceMaxCount;
    private final Invokable invokable;
    private final double nanosPerRun;
    private final SlidingCountWindow slidingCountWindow;

    private final Invokable invokableWithInitializer;

    private CountSlidingWindowThrottler(final Invokable invokable, final double maxInvocationsPerSecond,
                                        final int slidingWindowSize, final long sliceMaxCount) {
        if (maxInvocationsPerSecond <= 0) {
            throw new IllegalArgumentException("maxInvocationsPerSecond must be positive: " + maxInvocationsPerSecond);
        }
        if (sliceMaxCount <= 0) {
            throw new IllegalArgumentException("sliceMaxCount must be positive: " + sliceMaxCount);
        }
        this.invokable = Objects.requireNonNull(invokable);
        this.nanosPerRun = TimeFactors.NANOS_PER_SECOND / maxInvocationsPerSecond;
        this.sliceMaxCount = sliceMaxCount;
        this.invokableWithInitializer = Invokable.withInitializer(this::initialRun, this::timedRun);
        this.slidingCountWindow = new SlidingCountWindow(slidingWindowSize);
    }

    public static Runnable forRunnable(final Runnable runnable, final double maxInvocationsPerSecond) {
        return forInvokable(Invokable.forRunnable(runnable), maxInvocationsPerSecond)::invoke;
    }

    public static Runnable forRunnable(final Runnable runnable, final double maxInvocationsPerSecond,
                                       final int slidingWindowSize, final long sliceMaxCount) {
        return forInvokable(Invokable.forRunnable(runnable), maxInvocationsPerSecond, slidingWindowSize, sliceMaxCount)::invoke;
    }

    public static Runnable forRunnable(final Runnable runnable, final double maxInvocationsPerSecond,
                                       final long windowCycleTime, final TimeUnit cycleTimeUnit) {
        return forInvokable(Invokable.forRunnable(runnable), maxInvocationsPerSecond, windowCycleTime, cycleTimeUnit)::invoke;
    }

    public static Invokable forInvokable(final Invokable invokable, final double maxInvocationsPerSecond) {
        final long cycleMillis = Math.min(
                CYCLE_MAX_MILLIS,
                Math.max(CYCLE_MIN_MILLIS, (long) (CYCLE_MIN_COUNT * 1000 / maxInvocationsPerSecond))
        );
        return forInvokable(invokable, maxInvocationsPerSecond, cycleMillis, TimeUnit.MILLISECONDS);
    }

    public static Invokable forInvokable(final Invokable invokable, final double maxInvocationsPerSecond,
                                         final long windowCycleTime, final TimeUnit cycleTimeUnit) {
        final long cycleMillis = cycleTimeUnit.toMillis(windowCycleTime);
        final int slidingWindowSize = Math.min(
                SLIDING_WINDOW_MAX_SIZE,
                Math.max(SLIDING_WINDOW_MIN_SIZE, (int)Math.sqrt(maxInvocationsPerSecond * cycleMillis / 1000))
        );
        final long slizeMaxCount = Math.max(1, (long)(maxInvocationsPerSecond * cycleMillis / 1000 / slidingWindowSize));
//        System.out.println("cycleMillis=" + cycleMillis + ", slidingWindowSize=" + slidingWindowSize + ", sliceMaxCount=" + slizeMaxCount);
        return forInvokable(invokable, maxInvocationsPerSecond, slidingWindowSize, slizeMaxCount);
    }
    public static Invokable forInvokable(final Invokable invokable, final double maxInvocationsPerSecond,
                                         final int slidingWindowSize, final long sliceMaxCount) {
        return new CountSlidingWindowThrottler(
                invokable, maxInvocationsPerSecond, slidingWindowSize, sliceMaxCount
        ).invokableWithInitializer;
    }

    private boolean initialRun() {
        slidingCountWindow.init(System.nanoTime());
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
            if (invokable.invoke()) {
                slidingCountWindow.incrementLastSliceCount();
                return true;
            }
        }
        return false;
    }

}
