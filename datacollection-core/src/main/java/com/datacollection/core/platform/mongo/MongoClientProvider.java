package com.datacollection.core.platform.mongo;

import com.google.common.net.HostAndPort;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class MongoClientProvider {

    private static Map<String, MongoClient> clients = new TreeMap<>();

    public static synchronized MongoClient getOrCreate(String name, MongoConfig mongoConf) {
        return clients.computeIfAbsent(name, k -> initMongoClient(mongoConf));
    }

    static MongoClient initMongoClient(MongoConfig mongoConf) {
        if (mongoConf.getClientURI() != null) {
            // connect via client URI
            return new MongoClient(mongoConf.getClientURI());
        } else {
            // connect without client URI, use separate properties
            List<ServerAddress> seeds = new ArrayList<>(mongoConf.getHosts().size());
            for (String hotPortString : mongoConf.getHosts()) {
                HostAndPort hostAndPort = HostAndPort.fromString(hotPortString);
                seeds.add(new ServerAddress(hostAndPort.getHostText(), hostAndPort.getPort()));
            }

            List<MongoCredential> credentials = mongoConf.getCredential() != null
                    ? Collections.singletonList(mongoConf.getCredential()) : Collections.emptyList();

            return new MongoClient(seeds, credentials, MongoConfig.loadClientOptions().build());
        }
    }
}
