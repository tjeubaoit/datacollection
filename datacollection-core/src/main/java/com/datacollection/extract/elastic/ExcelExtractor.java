package com.datacollection.extract.elastic;

import com.datacollection.common.config.Configuration;
import com.datacollection.extract.Extractor;
import com.datacollection.extract.model.GenericModel;
import com.datacollection.platform.elastic.ElasticClientProvider;
import com.datacollection.platform.elastic.ElasticConfig;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;

public class ExcelExtractor extends Extractor {

    private Client client;
    private String eIndex;

    public ExcelExtractor(Configuration config) {
        super("excel", config);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        ElasticConfig elasticConfig = new ElasticConfig(props);
        eIndex = elasticConfig.getElasticIndex();
        client = ElasticClientProvider.getDefault(elasticConfig);
    }

    @Override
    protected void onLoop() throws Exception {
        SearchResponse scroll = client.prepareSearch("datacollection-rawexcel")
                .setTypes("excel")
                .setSize(1000)
                .setScroll(new TimeValue(6000000))
                .get();

        while (isNotCanceled()) {

            for (SearchHit hit : scroll.getHits()) {
                store(new GenericModel(hit.getId(),GenericModel.TYPE_EXCEL,hit.getSource()));
            }
            scroll = client.prepareSearchScroll(scroll.getScrollId()).setScroll(new TimeValue(6000000))
                    .execute().actionGet();

            if (scroll.getHits().getTotalHits() == 0) break;
        }
    }

//    public static void main(String[] args) {
//        Configuration conf = new Configuration();
//        Extractor extractor = new ExcelExtractor(conf);
//        extractor.setMsgBrokerFactory(new MockMsgBrokerFactory());
//        extractor.start();
//    }
}
