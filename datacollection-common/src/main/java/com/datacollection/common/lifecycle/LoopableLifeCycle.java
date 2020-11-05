package com.datacollection.common.lifecycle;

import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.Threads;

import java.util.concurrent.TimeUnit;

/**
 * Deprecated. Schedule processors with Quartz in new versions
 */
@Deprecated
public abstract class LoopableLifeCycle extends AbstractLifeCycle {

    private long sleepAfterDone;
    private long sleepAfterFail;

    public LoopableLifeCycle(long sleepAfterDone, long sleepAfterFail) {
        setSleepTime(sleepAfterDone, sleepAfterFail);
    }

    public LoopableLifeCycle() {
        setSleepTime(60, 30);
    }

    public LoopableLifeCycle(Properties p) {
        this(p.getLongProperty("lifecycle.loop.done.sleep.s", 60),
                p.getLongProperty("lifecycle.loop.fail.sleep.s", 30));
    }

    @Override
    protected final void onProcess() {
        boolean success;
        while (!isCanceled()) {
            try {
                this.onLoop();
                success = true;
            } catch (Throwable t) {
                success = false;
                this.logger.error(t.getMessage(), t);
            }

            Threads.sleep(success ? sleepAfterDone : sleepAfterFail, TimeUnit.SECONDS);
        }
    }

    protected void setSleepTime(long sleepAfterDone, long sleepAfterFail) {
        this.sleepAfterDone = sleepAfterDone;
        this.sleepAfterFail = sleepAfterFail;
    }

    protected void setSleepTime(Properties p) {
        this.sleepAfterDone = p.getLongProperty("lifecycle.loop.done.sleep.s", 60);
        this.sleepAfterFail = p.getLongProperty("lifecycle.loop.fail.sleep.s", 30);
    }

    protected abstract void onLoop() throws Exception;
}
