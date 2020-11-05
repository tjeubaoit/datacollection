package com.datacollection.core;

import com.google.common.util.concurrent.Futures;
import com.datacollection.common.config.Properties;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mock message queue writer use for testing
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class MockMsgBrokerWriter implements MsgBrokerWriter {

    private final AtomicLong counter = new AtomicLong();

    @Override
    public void configure(Properties p) {
    }

    @Override
    public Future<Long> write(byte[] b) {
        System.out.println(new String(b));
        return Futures.immediateFuture(counter.incrementAndGet());
    }
}
