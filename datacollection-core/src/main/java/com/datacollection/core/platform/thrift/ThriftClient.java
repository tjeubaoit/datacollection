package com.datacollection.core.platform.thrift;

import com.google.common.net.HostAndPort;
import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.Strings;
import com.datacollection.common.utils.Utils;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ThriftClient implements Closeable {

    static final Logger logger = LoggerFactory.getLogger(ThriftClient.class);
    static final AtomicInteger counter = new AtomicInteger();

    private TTransport transport;
    private TProtocol protocol;

    public ThriftClient(Properties props) {
        final HostAndPort hostAndPort = HostAndPort.fromString(props.getProperty("thrift.client.host"));
        final boolean nonBlocking = props.getBoolProperty("thrift.mode.nonBlocking", false);

        transport = new TSocket(hostAndPort.getHostText(), hostAndPort.getPort());
        if (nonBlocking) {
            logger.info(Strings.format("Start thrift client %d in non-blocking mode",
                    counter.incrementAndGet()));
            transport = new TFastFramedTransport(transport);
        }
        protocol = new TBinaryProtocol(transport);
    }

    public void connect() throws TTransportException {
        transport.open();
        Utils.addShutdownHook(this::close);
    }

    public TTransport getTransport() {
        return transport;
    }

    public TProtocol getProtocol() {
        return protocol;
    }

    @Override
    public void close() {
        transport.close();
    }
}
