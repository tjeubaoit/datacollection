package com.datacollection.metric;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class Counter implements Counting {

    private final AtomicLong counter = new AtomicLong(0);

    @Override
    public long getCount() {
        return counter.get();
    }

    public void inc() {
        counter.incrementAndGet();
    }

    public void inc(long n) {
        counter.addAndGet(n);
    }

    public void dec() {
        counter.decrementAndGet();
    }

    public void dec(long n) {
        counter.addAndGet(n);
    }
}
