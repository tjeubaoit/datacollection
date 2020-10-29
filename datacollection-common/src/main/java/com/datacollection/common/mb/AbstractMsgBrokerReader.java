package com.datacollection.common.mb;

import com.datacollection.common.config.Properties;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public abstract class AbstractMsgBrokerReader implements MsgBrokerReader {

    private Properties props;
    private final List<MsgHandler> handlers = new LinkedList<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void configure(Properties props) {
        if (running()) throw new IllegalStateException("Cannot configure a consumer after it started");
        this.props = props;
    }

    @Override
    public final void start() {
        this.running.set(true);
        doStart();
    }

    @Override
    public final void stop() {
        this.running.set(false);
        doStop();
    }

    @Override
    public boolean running() {
        return this.running.get();
    }

    public final void addHandler(MsgHandler handler) {
        handlers.add(handler);
    }

    public Properties properties() {
        return this.props;
    }

    public List<MsgHandler> handlers() {
        return this.handlers;
    }

    protected abstract void doStart();

    protected abstract void doStop();

    protected void invokeHandlers(Records records) {
        for (MsgHandler handler : handlers()) {
            handler.handle(records);
        }
    }
}
