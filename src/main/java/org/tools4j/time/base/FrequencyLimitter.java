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
package org.tools4j.time.base;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class FrequencyLimitter {

    private final long resetTimeNanos = TimeUnit.SECONDS.toNanos(1);
    private final BooleanSupplier invokable;
    private final double nanosPerRun;

    private BooleanSupplier action;
    private long lastCheckTimeNanos;
    private long countSinceLastCheck;

    private FrequencyLimitter(final BooleanSupplier invokable, final int maxInvocationsPerSecond) {
        if (maxInvocationsPerSecond <= 0) {
            throw new IllegalArgumentException("maxInvocationsPerSecond must be positive: " + maxInvocationsPerSecond);
        }
        this.invokable = Objects.requireNonNull(invokable);
        this.nanosPerRun = 1e9 / maxInvocationsPerSecond;
        this.action = this::initialRun;
    }

    public static Runnable forRunnable(final Runnable runnable, final int maxInvocationsPerSecond) {
        final BooleanSupplier booleanSupplier = forBooleanSupplier(() -> {
            runnable.run();
            return true;
        }, maxInvocationsPerSecond);
        return () -> booleanSupplier.getAsBoolean();
    }

    public static BooleanSupplier forBooleanSupplier(final BooleanSupplier invokable, final int maxInvocationsPerSecond) {
        final FrequencyLimitter lim = new FrequencyLimitter(invokable, maxInvocationsPerSecond);
        return () -> lim.action.getAsBoolean();
    }

    private boolean initialRun() {
        lastCheckTimeNanos = System.nanoTime();
        countSinceLastCheck = 0;
        action = this::timedRun;
        return false;
    }

    private boolean timedRun() {
        final long nanoTime = System.nanoTime();
        final long deltaTimeNanos = nanoTime - lastCheckTimeNanos;
        final double expectedDeltaTimeNanos = nanosPerRun * countSinceLastCheck;
        int inc = 0;
        if (deltaTimeNanos >= expectedDeltaTimeNanos) {
            if (invokable.getAsBoolean()) {
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
