package com.datacollection.core.platform.thrift;

import com.datacollection.common.config.Properties;
import org.apache.thrift.transport.TTransportException;

import java.util.Map;
import java.util.TreeMap;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ThriftClientProvider {

    private static Map<String, ThriftClient> clients = new TreeMap<>();

    public static ThriftClient getDefault(Properties props) {
        return getOrCreate("default", props);
    }

    public static synchronized ThriftClient getOrCreate(String name, Properties props) {
        return clients.computeIfAbsent(name, k -> initThriftClient(props));
    }

    static ThriftClient initThriftClient(Properties props) {
        try {
            ThriftClient thriftClient = new ThriftClient(props);
            thriftClient.connect();
            return thriftClient;
        } catch (TTransportException e) {
            throw new ThriftRuntimeException(e);
        }
    }
}
