package com.datacollection.core.platform.thrift;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface ServiceClientFactory<T extends TServiceClient> {

    T newClient(TProtocol protocol);
}
