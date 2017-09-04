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

import java.util.Objects;

@FunctionalInterface
public interface Invokable {
    /** @return true if some actual work was performed, and false if this was essentially a No-Op call*/
    boolean invoke();

    static Invokable forRunnable(final Runnable runnable) {
        Objects.requireNonNull(runnable);
        return () -> {
            runnable.run();
            return true;
        };
    }

    static Invokable withInitializer(final Invokable initializer, final Invokable delegate) {
        Objects.requireNonNull(initializer);
        Objects.requireNonNull(delegate);
        return new Invokable() {
            private Invokable initializerThenDelegate = () -> {
                final boolean result = initializer.invoke();
                this.initializerThenDelegate = delegate;
                return result;
            };
            @Override
            public boolean invoke() {
                return initializerThenDelegate.invoke();
            }
        };
    }
}
