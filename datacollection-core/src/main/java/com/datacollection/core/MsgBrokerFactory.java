package com.datacollection.core;

/**
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface MsgBrokerFactory {

    MsgBrokerReader createReader();

    MsgBrokerWriter createWriter();
}
