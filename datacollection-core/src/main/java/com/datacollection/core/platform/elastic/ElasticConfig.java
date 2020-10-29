package com.datacollection.core.platform.elastic;

import com.google.common.net.HostAndPort;
import com.datacollection.common.config.Properties;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by kumin on 13/02/2017.
 */
public class ElasticConfig {

    private Collection<HostAndPort> hosts = new ArrayList<>();
    private String clusterName;
    private String elasticIndex;

    public ElasticConfig(Properties p) {
        Collection<String> addresses = p.getCollection("elastic.hosts");
        addresses.forEach(addr -> hosts.add(HostAndPort.fromString(addr)));
        this.clusterName = p.getProperty("elastic.cluster.name");
        this.elasticIndex = p.getProperty("elastic.index.name");
    }

    /**
     * Use for test
     */
    public ElasticConfig(Collection<HostAndPort> hosts, String clusterName) {
        this.hosts = hosts;
        this.clusterName = clusterName;
    }

    public  ElasticConfig(){
    }

    public Collection<HostAndPort> getHosts() {
        return hosts;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getElasticIndex() {
        return elasticIndex;
    }
}
