package com.datacollection.core.platform.thrift;

import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.Utils;
import org.apache.thrift.TServiceClient;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ServiceClientPool<T extends TServiceClient> implements AutoCloseable {

    private final ServiceClientFactory<T> clientFactory;
    private final Properties props;
    private final ThreadLocal<T> clientThreadLocal = new ThreadLocal<>();
    private final List<T> clients = new ArrayList<>();

    public ServiceClientPool(ServiceClientFactory<T> clientFactory, Properties props) {
        this.clientFactory = clientFactory;
        this.props = props;
        Utils.addShutdownHook(this::close);
    }

    public final T getClient() {
        T client = clientThreadLocal.get();
        if (client == null) {
            ThriftClient thriftClient = ThriftClientProvider.getOrCreate(Thread.currentThread().getName(), props);
            client = clientFactory.newClient(thriftClient.getProtocol());
            clientThreadLocal.set(client);
            clients.add(client);
        }
        return client;
    }

    @Override
    public void close() {
        clients.forEach(client -> {
            client.getInputProtocol().getTransport().close();
            client.getOutputProtocol().getTransport().close();
        });
    }
}
