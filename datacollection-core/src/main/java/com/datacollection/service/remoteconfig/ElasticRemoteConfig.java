package com.datacollection.service.remoteconfig;

import com.google.common.collect.ImmutableMap;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.config.Properties;
import com.datacollection.platform.elastic.ElasticClientProvider;
import com.datacollection.platform.elastic.ElasticConfig;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ElasticRemoteConfig extends RemoteConfiguration {

    private static Logger logger = LoggerFactory.getLogger(ElasticRemoteConfig.class);
    private static final String INDEX_TYPE = "config";

    private final Client client;
    private final String index;

    public ElasticRemoteConfig(Properties props) {
        ElasticConfig elasticConfig = new ElasticConfig(props.toSubProperties("remote_config"));
        client = ElasticClientProvider.getDefault(elasticConfig);
        index = elasticConfig.getElasticIndex();
    }

    @Override
    public String getProperty(String key) {
        SearchResponse sr = client.prepareSearch(index)
                .setTypes(INDEX_TYPE)
                .setQuery(QueryBuilders.matchPhraseQuery("_id", key))
                .execute()
                .actionGet();

        if (sr.getHits().totalHits() == 0) return null;
        return sr.getHits().getHits()[0].getSource().get("value").toString();
    }

    @Override
    public String setProperty(String key, String value) {
        client.prepareIndex(index, INDEX_TYPE)
                .setId(key)
                .setSource(ImmutableMap.of("value", value, "key", key))
                .execute().actionGet();
        return value;
    }

    public Map<String, String> getPropertiesByPrefix(String prefix) {
        Map<String, String> props = new HashMap<>();
        SearchResponse sr = client.prepareSearch(index)
                .setTypes(INDEX_TYPE)
                .setQuery(QueryBuilders.prefixQuery("key", prefix))
                .setSize(1000)
                .execute()
                .actionGet();

        for (SearchHit hit : sr.getHits()) {
            String propKey = hit.getSource().get("key").toString();
            String propValue = hit.getSource().get("value").toString();
            props.put(propKey, propValue);
        }

        return props;
    }

    public void setProperties(Map<String, String> props) {
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
        for (String key : props.keySet()) {
            Map<String, String> sourceMap = new HashMap<>();
            sourceMap.put("value", props.get(key));
            sourceMap.put("key", key);
            IndexRequest indexRequest = new IndexRequest(index, INDEX_TYPE, key).
                    source(sourceMap);
            bulkRequestBuilder.add(indexRequest);
        }
        BulkResponse bulkResponse = bulkRequestBuilder.get();
        if (!bulkResponse.hasFailures()) {
            logger.info("summit bulk took: " + bulkResponse.getTook());
        }
    }


    public static void main(String[] args) {
        ElasticRemoteConfig elasticRemoteConfig = new ElasticRemoteConfig(new Configuration());
        elasticRemoteConfig.setProperties(Collections.singletonMap("version.fb-post", "5"));
//        System.out.println(elasticRemoteConfig.getProperty("version.fb-cmt"));
        System.out.println(elasticRemoteConfig.getPropertiesByPrefix("version"));
    }
}
