package com.datacollection.common.mb;

/**
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class MockMsgBrokerFactory implements MsgBrokerFactory {

    @Override
    public MsgBrokerReader createReader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MsgBrokerWriter createWriter() {
        return new MockMsgBrokerWriter();
    }
}
