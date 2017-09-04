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

public class PeriodicResetThrottler {

    private static final long DEFAULT_RESET_TIME_NANOS = TimeUnit.SECONDS.toNanos(1);

    private final double nanosPerRun;
    private final long resetTimeNanos;
    private final Invokable invokable;
    private final Invokable invokableWithInitializer;

    private long lastCheckTimeNanos;
    private long countSinceLastCheck;

    private PeriodicResetThrottler(final Invokable invokable, final double maxInvocationsPerSecond, final long resetTimeNanos) {
        if (maxInvocationsPerSecond <= 0) {
            throw new IllegalArgumentException("maxInvocationsPerSecond must be positive: " + maxInvocationsPerSecond);
        }
        this.invokable = Objects.requireNonNull(invokable);
        this.nanosPerRun = nanosPerRun(maxInvocationsPerSecond);
        if (resetTimeNanos < nanosPerRun) {
            throw new IllegalArgumentException("resetTimeNanos must be at least nanosPerRun: " + resetTimeNanos + " < " + nanosPerRun);
        }
        this.resetTimeNanos = resetTimeNanos;
        this.invokableWithInitializer = Invokable.withInitializer(this::initialRun, this::timedRun);
    }
    ;
    public static Runnable forRunnable(final Runnable runnable, final double maxInvocationsPerSecond) {
        return forRunnable(runnable, maxInvocationsPerSecond, defaultResetTimeNanos(maxInvocationsPerSecond), TimeUnit.NANOSECONDS);
    }

    public static Runnable forRunnable(final Runnable runnable, final double maxInvocationsPerSecond,
                                       final long resetTime, final TimeUnit resetTimeUnit) {
        return forInvokable(Invokable.forRunnable(runnable), maxInvocationsPerSecond, resetTime, resetTimeUnit)::invoke;
    }

    public static Invokable forInvokable(final Invokable invokable, final double maxInvocationsPerSecond) {
        return forInvokable(invokable, maxInvocationsPerSecond, defaultResetTimeNanos(maxInvocationsPerSecond), TimeUnit.NANOSECONDS);
    }

    public static Invokable forInvokable(final Invokable invokable, final double maxInvocationsPerSecond,
                                         final long resetTime, final TimeUnit resetTimeUnit) {
        final long resetTimeNanos = resetTimeUnit.toNanos(resetTime);
        return new PeriodicResetThrottler(invokable, maxInvocationsPerSecond, resetTimeNanos).invokableWithInitializer;
    }

    private static double nanosPerRun(final double maxInvocationsPerSecond) {
        return TimeFactors.NANOS_PER_SECOND / maxInvocationsPerSecond;
    }

    private static long defaultResetTimeNanos(final double maxInvocationsPerSecond) {
        return Math.max((long)Math.ceil(nanosPerRun(maxInvocationsPerSecond)), DEFAULT_RESET_TIME_NANOS);
    }

    private boolean initialRun() {
        lastCheckTimeNanos = System.nanoTime();
        countSinceLastCheck = 0;
        return false;
    }

    private boolean timedRun() {
        final long nanoTime = System.nanoTime();
        final long deltaTimeNanos = nanoTime - lastCheckTimeNanos;
        final double expectedDeltaTimeNanos = nanosPerRun * countSinceLastCheck;
        int inc = 0;
        if (deltaTimeNanos >= expectedDeltaTimeNanos) {
            if (invokable.invoke()) {
                inc++;
            }
        }
        if (deltaTimeNanos >= resetTimeNanos) {
            lastCheckTimeNanos = nanoTime;
            countSinceLastCheck = inc;
        } else {
            countSinceLastCheck += inc;
        }
        return inc > 0;
    }
}