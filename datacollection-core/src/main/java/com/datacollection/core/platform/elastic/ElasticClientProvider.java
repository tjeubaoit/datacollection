package com.datacollection.core.platform.elastic;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by kumin on 13/02/2017.
 */
public class ElasticClientProvider {

    private static Map<String, Client> clients = new TreeMap<>();

    public static Client getDefault(ElasticConfig config) {
        return getOrCreate("default", config);
    }

    public static synchronized Client getOrCreate(String name, ElasticConfig config) {
        synchronized (ElasticClientProvider.class) {
            return clients.computeIfAbsent(name, k -> initElasticClient(config));
        }
    }

    private static Client initElasticClient(ElasticConfig config) {
        Settings settings = Settings.builder().put("cluster.name", config.getClusterName()).build();
        TransportClient elastic_client = new PreBuiltTransportClient(settings);

        config.getHosts().forEach(host -> {
            try {
                elastic_client.addTransportAddress(new InetSocketTransportAddress(
                        InetAddress.getByName(host.getHostText()), host.getPort()));
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        });

        return elastic_client;
    }
}
