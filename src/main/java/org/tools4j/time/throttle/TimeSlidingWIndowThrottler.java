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

public class TimeSlidingWIndowThrottler {

    private static final int SLIDING_WINDOW_SIZE = 32;
    private final static long DEFAULT_WINDOW_SLIDING_TIME_NANOS = TimeUnit.SECONDS.toNanos(1);

    private final long sliceSlidingTimeNanos;
    private final Invokable invokable;
    private final double nanosPerRun;
    private final SlidingCountWindow slidingCountWindow;
    private final Invokable invokableWithInitializer;

    private TimeSlidingWIndowThrottler(final Invokable invokable, final double maxInvocationsPerSecond) {
        this.invokable = Objects.requireNonNull(invokable);
        if (maxInvocationsPerSecond <= 0) {
            throw new IllegalArgumentException("maxInvocationsPerSecond must be positive: " + maxInvocationsPerSecond);
        }
        this.nanosPerRun = TimeFactors.NANOS_PER_SECOND / maxInvocationsPerSecond;
        this.sliceSlidingTimeNanos = ceilDiv(windowSlidingTimeFor(maxInvocationsPerSecond), SLIDING_WINDOW_SIZE);
        this.invokableWithInitializer = Invokable.withInitializer(this::initialRun, this::timedRun);
        this.slidingCountWindow = new SlidingCountWindow(SLIDING_WINDOW_SIZE);
    }

    public static Runnable forRunnable(final Runnable runnable, final double maxInvocationsPerSecond) {
        return forInvokable(Invokable.forRunnable(runnable), maxInvocationsPerSecond)::invoke;
    }

    public static Invokable forInvokable(final Invokable invokable, final double maxInvocationsPerSecond) {
        return new TimeSlidingWIndowThrottler(invokable, maxInvocationsPerSecond).invokableWithInitializer;
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
            if (invokable.invoke()) {
                slidingCountWindow.incrementCount();
                return true;
            }
        }
        return false;
    }
}
