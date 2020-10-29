package com.datacollection.core.platform.kafka;

import com.datacollection.common.mb.MsgBrokerFactory;
import com.datacollection.common.mb.MsgBrokerReader;
import com.datacollection.common.mb.MsgBrokerWriter;

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
