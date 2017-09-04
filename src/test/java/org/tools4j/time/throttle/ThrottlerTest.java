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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tools4j.spockito.Spockito;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link PeriodicResetThrottler}, {@link TimeSlidingWIndowThrottler} and
 * {@link CountSlidingWindowThrottler}.
 */
@RunWith(Spockito.class)
@Spockito.Unroll({
        "|  Updates/second | Running time (s) |",
        "|-----------------|------------------|",
        "|           0.25  |        10        |",
//        "|           0.5   |         5        |",
        "|           1     |         3        |",
//        "|           5     |        3        |",
        "|          10     |         3        |",
//        "|          50     |         3        |",
//        "|         100     |         3        |",
        "|        1000     |         3        |",
//        "|        2000     |         3        |",
//        "|        5000     |         3        |",
        "|       10000     |         3        |",
//        "|      100000     |         3        |",
//        "|      200000     |         3        |",
//        "|      500000     |         3        |",
//        "|     1000000     |         3        |",
//        "|      3000000     |         3        |",
})
public class ThrottlerTest {

    interface Constructor<T> {
        T create(T task, double maxUpdatesPerSecond);
    }

    @Test
    public void periodicReset_runnable(final double updatesPerSecond, final int runningTimeSeconds) {
        runnable(PeriodicResetThrottler::forRunnable, updatesPerSecond, runningTimeSeconds);
    }
    @Test
    public void timeSlidingWindow_runnable(final double updatesPerSecond, final int runningTimeSeconds) {
        runnable(TimeSlidingWIndowThrottler::forRunnable, updatesPerSecond, runningTimeSeconds);
    }
    @Test
    public void countSlidingWindow_runnable(final double updatesPerSecond, final int runningTimeSeconds) {
        runnable((t, f) -> CountSlidingWindowThrottler.forRunnable(t, f, 1, TimeUnit.SECONDS), updatesPerSecond, runningTimeSeconds);
    }
    private void runnable(final Constructor<Runnable> constructor,
                          final double updatesPerSecond, final int runningTimeSeconds) {
        final AtomicLong counter = new AtomicLong();
        final Runnable runnable = constructor.create(counter::incrementAndGet, updatesPerSecond);
        runTest(counter, runnable, updatesPerSecond, runningTimeSeconds);
    }

    @Test
    public void periodicReset_invokable(final double updatesPerSecond, final int runningTimeSeconds) {
        invokable(PeriodicResetThrottler::forInvokable, updatesPerSecond, runningTimeSeconds);
    }
    @Test
    public void timeSlidingWindow_invokable(final double updatesPerSecond, final int runningTimeSeconds) {
        invokable(TimeSlidingWIndowThrottler::forInvokable, updatesPerSecond, runningTimeSeconds);
    }
    @Test
    public void countSlidingWindow_invokable(final double updatesPerSecond, final int runningTimeSeconds) {
        invokable((t, f) -> CountSlidingWindowThrottler.forInvokable(t, f, 1, TimeUnit.SECONDS), updatesPerSecond, runningTimeSeconds);
    }
    public void invokable(final Constructor<Invokable> constructor,
                          final double updatesPerSecond, final int runningTimeSeconds) {
        final AtomicBoolean toggler = new AtomicBoolean();
        final AtomicLong allCounter = new AtomicLong();
        final AtomicLong counter = new AtomicLong();
        final Invokable invokable = constructor.create(() -> {
            allCounter.incrementAndGet();
            if (toggler.getAndSet(!toggler.get())) {
                counter.incrementAndGet();
                return true;
            }
            return false;
        }, updatesPerSecond);
        final AtomicLong resultCounter = new AtomicLong();
        runTest(counter, () -> {
            if (invokable.invoke()) {
                resultCounter.incrementAndGet();
            }
        }, updatesPerSecond, runningTimeSeconds);

        System.out.println("allCounter:\t" + allCounter);
        System.out.println("counter:\t" + counter);
        System.out.println("resultCounter:\t" + resultCounter);

        assertEquals("all-counter by two should be same as counter", counter.get(), allCounter.get() / 2);
        assertEquals("result-counter should same as counter", counter.get(), resultCounter.get());
    }

    private void runTest(final AtomicLong counter, final Runnable runnable,
                         final double updatesPerSecond, final int runningTimeSeconds) {
        final long runningTimeMillis = TimeUnit.SECONDS.toMillis(runningTimeSeconds);
        final long startTimeMillis = System.currentTimeMillis();
        long lastCount = 0;
        long lastTimeMillis = startTimeMillis;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            runnable.run();
            if ((i & 0xff) == 0) {
                final long timeMillis = System.currentTimeMillis();
                if (timeMillis - lastTimeMillis >= 500) {
                    final String time = "time=" + (timeMillis - startTimeMillis) + "ms";
                    final long deltaCount = counter.get() - lastCount;
                    final long deltaMillis = timeMillis - lastTimeMillis;
                    lastCount = counter.get();
                    lastTimeMillis = timeMillis;
                    System.out.println(time + ": counter=" + counter + ", increment=" + deltaCount);
                    final double updatesPerDeltaTime = updatesPerSecond * deltaMillis / 1000;
                    assertTrue(time + ": " + deltaCount + " should be within 1% of " + updatesPerDeltaTime,
                            Math.abs(updatesPerDeltaTime - deltaCount) <= (1 + updatesPerDeltaTime/100));
                }
                if (timeMillis - startTimeMillis >= runningTimeMillis) {
                    return;
                }
            }
        }
    }
}