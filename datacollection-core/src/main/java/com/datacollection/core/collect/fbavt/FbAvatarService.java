package com.datacollection.core.collect.fbavt;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.Strings;
import com.datacollection.core.platform.hystrix.SyncCommand;
import com.datacollection.core.platform.aerospike.AerospikeClientProvider;
import com.datacollection.core.platform.aerospike.AerospikeConfig;

import java.io.Closeable;

/**
 * Service chứa các method để làm việc với Facebook avatar
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class FbAvatarService implements Closeable {

    private final AerospikeClient client;
    private final String aerospikeNamespace;
    private final String aerospikeSet;
    private final Fetcher fetcher;

    public FbAvatarService(Properties p) {
        this.client = AerospikeClientProvider.getDefault(new AerospikeConfig(p));
        this.aerospikeNamespace = p.getProperty("aerospike.namespace");
        this.aerospikeSet = p.getProperty("fbavatar.aerospike.set");
        this.fetcher = Fetcher.create(p);
    }

    /**
     * Get avatar url from a facebook user id
     *
     * @param id facebook user id
     * @return avatar url or null if failed
     */
    public String getAvatarUrl(String id) {
        String url = new SyncCommand<>("fbavatar", "FetchAvatarUrlAerospike",
                () -> fetchCachedUrl(id)).execute();
        if (Strings.isNonEmpty(url)) return url;

        SyncCommand<String> cmd = new SyncCommand<>("fbavatar", "FetchAvatarUrlFbCdn",
                () -> fetchUrlFromFbCdn(id));
        url = cmd.execute();
        if (url == null) throw cmd.getCheckedException();
        return url;
    }

    /**
     * Fetch avatar url from Facebook CDN
     *
     * @param id id of facebook account to get avatar
     * @return fetched avatar url or null if failed
     */
    public String fetchUrlFromFbCdn(String id) {
        return fetcher.fetch(id);
    }

    /**
     * Fetch cached avatar url in aerospike
     *
     * @param id facebook user id
     * @return avatar image url or null if no data found
     */
    public String fetchCachedUrl(String id) {
        Key key = new Key(aerospikeNamespace, aerospikeSet, id);
        Record record = client.get(null, key);
        return record != null ? record.bins.get("url").toString() : "";
    }

    @Override
    public void close() {
        client.close();
    }

    public static void main(String[] args) {
        Configuration conf = new Configuration();
        FbAvatarService service = new FbAvatarService(conf);

        for (int i = 0; i < 100; i++) {
            System.out.println(service.fetchUrlFromFbCdn("100002999510116"));
            System.out.println(service.fetchUrlFromFbCdn("255626431558023"));
        }

//        AerospikeClient client = AerospikeClientProvider.getDefault(new AerospikeConfig(conf));
//        Key key = new Key("memcachedata", "fbavatar1", "255626431558023");
//        Record record = client.get(null, key);
//        System.out.println(record.bins);

//        key = new Key("memcachedata", "fbavatar1", "260529631143299");
//        record = client.get(null, key);
//        System.out.println(record.bins);
    }
}
