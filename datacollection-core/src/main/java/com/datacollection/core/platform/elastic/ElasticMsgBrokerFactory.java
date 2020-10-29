package com.datacollection.core.platform.elastic;

import com.datacollection.common.mb.MsgBrokerFactory;
import com.datacollection.common.mb.MsgBrokerReader;
import com.datacollection.common.mb.MsgBrokerWriter;

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
