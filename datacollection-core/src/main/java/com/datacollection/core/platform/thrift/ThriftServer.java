package com.datacollection.core.platform.thrift;

import com.google.common.net.HostAndPort;
import com.datacollection.common.config.Properties;
import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ThriftServer {

    private static final Logger logger = LoggerFactory.getLogger(ThriftServer.class);

    private final TProcessor processor;
    private final Properties props;

    public ThriftServer(Properties props, TProcessor processor) {
        this.processor = processor;
        this.props = props;
    }

    public void start() {
        final HostAndPort hostAndPort = HostAndPort.fromString(props.getProperty("thrift.client.host"));
        final boolean nonBlocking = props.getBoolProperty("thrift.mode.nonBlocking", false);

        final InetSocketAddress socketAddress = new InetSocketAddress(
                hostAndPort.getHostText(), hostAndPort.getPort());
        if (nonBlocking) {
            startNonBlockingServer(processor, socketAddress);
        } else {
            startBlockingServer(processor, socketAddress);
        }
    }

    static void startNonBlockingServer(TProcessor processor, InetSocketAddress socketAddress) {
        try {
            TNonblockingServerTransport transport = new TNonblockingServerSocket(socketAddress);
            TServer server = new TNonblockingServer(
                    new TNonblockingServer.Args(transport).processor(processor));

            logger.info("Start thrift non-blocking server at " + socketAddress);
            server.serve();
        } catch (TTransportException e) {
            throw new ThriftRuntimeException(e);
        }
    }

    static void startBlockingServer(TProcessor processor, InetSocketAddress socketAddress) {
        try {
            TServerTransport transport = new TServerSocket(socketAddress);
//            TServer server = new TSimpleServer(new TSimpleServer.Args(serverTransport).processor(processor));

            // Use this for a multithreaded server
            TServer server = new TThreadPoolServer(
                    new TThreadPoolServer.Args(transport).processor(processor));

            logger.info("Start thrift blocking server at " + socketAddress);
            server.serve();
        } catch (TTransportException e) {
            throw new ThriftRuntimeException(e);
        }
    }
}

