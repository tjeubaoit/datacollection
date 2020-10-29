package com.datacollection.common.mb;

import com.datacollection.common.config.Configurable;
import com.datacollection.common.config.Properties;

/**
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface MsgBrokerReader extends Configurable {

    void configure(Properties p);

    void start();

    void stop();

    boolean running();

    void addHandler(MsgHandler handler);
}
