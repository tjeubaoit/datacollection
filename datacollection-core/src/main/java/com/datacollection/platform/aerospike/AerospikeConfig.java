package com.datacollection.platform.aerospike;

import com.aerospike.client.Host;
import com.aerospike.client.async.EventLoops;
import com.aerospike.client.async.EventPolicy;
import com.aerospike.client.async.NioEventLoops;
import com.aerospike.client.policy.ClientPolicy;
import com.google.common.net.HostAndPort;
import com.datacollection.common.config.Properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by kumin on 22/05/2017.
 */
public class AerospikeConfig {

    private List<Host> hosts = new ArrayList<>();
    private ClientPolicy policy;
    private String namespace;
    private EventLoops eventLoops;

    public AerospikeConfig(Properties p){
        Collection<String> hostAddresses = p.getCollection("aerospike.hosts");
        for (String hostPortString : hostAddresses) {
            HostAndPort hostAndPort = HostAndPort.fromString(hostPortString);
            this.hosts.add(new Host(hostAndPort.getHostText(), hostAndPort.getPort()));
        }
        this.namespace = p.getProperty("aerospike.namespace");

        int eventLoopSize = p.getIntProperty("aerospike.eventloop.size",
                Runtime.getRuntime().availableProcessors());
        EventPolicy eventPolicy = new EventPolicy();
        eventPolicy.minTimeout = 5000;
        this.eventLoops = new NioEventLoops(eventPolicy, eventLoopSize);

        this.policy = new ClientPolicy();
        policy.eventLoops = eventLoops;

//        policy.readPolicyDefault.socketTimeout = 50;
//        policy.readPolicyDefault.totalTimeout = 110;
//        policy.readPolicyDefault.sleepBetweenRetries = 10;
//        policy.writePolicyDefault.socketTimeout = 200;
//        policy.writePolicyDefault.totalTimeout = 450;
//        policy.writePolicyDefault.sleepBetweenRetries = 50;
//
//        policy.writePolicyDefault.setTimeout(5000);
        policy.maxConnsPerNode = eventLoopSize * 40;
    }

    public List<Host> getHosts() {
        return hosts;
    }

    public ClientPolicy getClientPolicy() {
        return policy;
    }

    public String getNamespace() {
        return namespace;
    }

    public EventLoops getEventLoops() {
        return eventLoops;
    }
}
