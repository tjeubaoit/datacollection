package com.datacollection.common.mb;

public class MultiMsgBrokerFactory implements MsgBrokerFactory {

    @Override
    public MsgBrokerReader createReader() {
        return null;
    }

    @Override
    public MsgBrokerWriter createWriter() {
        return new MultiMsgBrokerWriter();
    }
}
