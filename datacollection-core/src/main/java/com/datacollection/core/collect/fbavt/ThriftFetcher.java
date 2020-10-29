package com.datacollection.core.collect.fbavt;

import com.datacollection.common.config.Properties;
import com.datacollection.core.platform.thrift.ServiceClientPool;
import com.datacollection.core.platform.thrift.ThriftRuntimeException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;

/**
 * Fetch facebook avatar URL từ per-app fbID. Phiên bản được implement
 * bằng cách call tới một Thrift server (viết bằng Golang)
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ThriftFetcher implements Fetcher {

    private ServiceClientPool<thrift.collect.FbAvatarService.Client> clientPool;

    @Override
    public void configure(Properties p) {
        this.clientPool = new ServiceClientPool<>(ThriftFetcher::newClient, p);
    }

    @Override
    public String fetch(String id) {
        try {
            return clientPool.getClient().fetchAvatarUrl(id);
        } catch (TException e) {
            throw new ThriftRuntimeException(e);
        }
    }

    private static thrift.collect.FbAvatarService.Client newClient(TProtocol protocol) {
        return new thrift.collect.FbAvatarService.Client(
                new TMultiplexedProtocol(protocol, "FbAvatarService"));
    }
}
