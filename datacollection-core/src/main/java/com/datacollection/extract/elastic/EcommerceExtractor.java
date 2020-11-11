package com.datacollection.extract.elastic;

import com.datacollection.common.config.Configuration;
import com.datacollection.core.MockMsgBrokerFactory;
import com.datacollection.common.utils.Threads;
import com.datacollection.extract.Extractor;
import com.datacollection.extract.model.GenericModel;
import com.datacollection.platform.elastic.ElasticClientProvider;
import com.datacollection.platform.elastic.ElasticConfig;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class EcommerceExtractor extends Extractor {

    private Client client;
    private final int esScrollSize;
    private final String esIndex;
    private final int esScrollTimeout;

    public EcommerceExtractor(Configuration config) {
        super("ecommerce", config);
        esIndex = props.getProperty("es.index.name", "ecommerce");
        esScrollSize = props.getIntProperty("es.scroll.size", 1000);
        esScrollTimeout = props.getIntProperty("es.scroll.timeout.minutes", 5);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        client = ElasticClientProvider.getDefault(new ElasticConfig(props));
    }

    @Override
    protected void onLoop() {
        int defSkip = props.getIntProperty("es.skip", 0);
        int skip = Integer.parseInt(loadIndex(String.valueOf(defSkip)));
        logger.info("Skip " + skip + " documents");
        Threads.sleep(1000);

        SearchResponse scrollResp = client.prepareSearch(esIndex).setTypes("profile")
                .setQuery(QueryBuilders.matchAllQuery())
//                .setQuery(QueryBuilders.termQuery("source_name", "enbac"))
                .setSize(esScrollSize)
                .setScroll(new TimeValue(esScrollTimeout, TimeUnit.MINUTES))
                .execute().actionGet();
        long totalPost = scrollResp.getHits().getTotalHits();
        logger.info("Total post: " + totalPost);

        int count = 0;
        while (isNotCanceled()) {
            for (SearchHit hit : scrollResp.getHits()) {
                if (count++ < skip) continue;
                final int c = count;

                // send current count value as attachment to keep in index
                store(convertToGenericModel(hit), c);
            }

            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
                    .setScroll(new TimeValue(esScrollTimeout, TimeUnit.MINUTES))
                    .execute().actionGet();

            //Break condition: No hits are returned
            if (scrollResp.getHits().getHits().length == 0) break;
        }

        // ES ecommerce extractor do not need loop
        this.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        client.close();
    }

    @Override
    protected void onRecordProcessed(GenericModel model, long queueOrder, Object attachment) {
        storeIndex(attachment.toString(), queueOrder);
    }

    private GenericModel convertToGenericModel(SearchHit hit) {
        Map<String, Object> source = hit.getSource();
        String id = hit.getId();

        GenericModel model = new GenericModel();
        model.setType(GenericModel.TYPE_ECOMMERCE);
        model.getPost().put("url", "es://ecommerce/profile/" + id);
        model.getPost().putAll(source);

        return model;
    }

    public static void main(String[] args) {
        Configuration conf = new Configuration();
        Extractor extractor = new EcommerceExtractor(conf);
        extractor.setMsgBrokerFactory(new MockMsgBrokerFactory());
        extractor.start();
    }
}
