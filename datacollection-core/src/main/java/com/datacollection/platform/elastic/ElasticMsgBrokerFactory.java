package com.datacollection.platform.elastic;

import com.datacollection.core.MsgBrokerFactory;
import com.datacollection.core.MsgBrokerReader;
import com.datacollection.core.MsgBrokerWriter;

public class ElasticMsgBrokerFactory implements MsgBrokerFactory {
    @Override
    public MsgBrokerReader createReader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MsgBrokerWriter createWriter() {
        return new ElasticMsgBrokerWriter();
    }
}
