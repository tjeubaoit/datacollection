package com.datacollection.platform.kafka;

import com.datacollection.core.MsgBrokerFactory;
import com.datacollection.core.MsgBrokerReader;
import com.datacollection.core.MsgBrokerWriter;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class KafkaMsgBrokerFactory implements MsgBrokerFactory {

    @Override
    public MsgBrokerReader createReader() {
        return new KafkaMsgBrokerReader();
    }

    @Override
    public MsgBrokerWriter createWriter() {
        return new KafkaMsgBrokerWriter();
    }
}
