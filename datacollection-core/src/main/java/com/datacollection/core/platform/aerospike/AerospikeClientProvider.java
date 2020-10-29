package com.datacollection.core.platform.aerospike;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Host;

import java.util.Map;
import java.util.TreeMap;


/**
 * Created by kumin on 22/05/2017.
 */
public class AerospikeClientProvider {

    private static Map<String, AerospikeClient> clients = new TreeMap<>();

    public static AerospikeClient getDefault(AerospikeConfig config){
        return clients.computeIfAbsent("default", k -> initClient(config));
    }

    public static synchronized AerospikeClient getOrCreate(String name, AerospikeConfig config){
        return clients.computeIfAbsent(name, k -> initClient(config));
    }

    private static AerospikeClient initClient(AerospikeConfig config){
        Host[] hosts = new Host[config.getHosts().size()];
        hosts = config.getHosts().toArray(hosts);
        return new AerospikeClient(config.getClientPolicy(), hosts);
    }
}
