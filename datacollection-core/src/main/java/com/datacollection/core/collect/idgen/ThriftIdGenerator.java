package com.datacollection.core.collect.idgen;

import com.datacollection.common.config.Properties;
import com.datacollection.core.platform.thrift.ServiceClientPool;
import com.datacollection.core.platform.thrift.ThriftRuntimeException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import thrift.collect.IdGenerator;

import java.util.List;

/**
 * Production implementation of RemoteIdGenerator, call to remote Thrift server
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ThriftIdGenerator implements RemoteIdGenerator {

    private ServiceClientPool<IdGenerator.Client> clientPool;

    @Override
    public void configure(Properties p) {
        this.clientPool = new ServiceClientPool<>(ThriftIdGenerator::newClient, p);
    }

    @Override
    public long generate(List<String> seeds, long defVal) {
        try {
            if (seeds.isEmpty()) return defVal;
            IdGenerator.Client client = clientPool.getClient();
            return client.generate(seeds, defVal);
        } catch (TException e) {
            throw new ThriftRuntimeException(e);
        }
    }

    private static IdGenerator.Client newClient(TProtocol protocol) {
        return new IdGenerator.Client(new TMultiplexedProtocol(protocol, "IdGenerator"));
    }
}
