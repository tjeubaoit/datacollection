package com.datacollection.core.tools;

import com.datacollection.common.config.Configuration;
import com.datacollection.common.config.SubProperties;
import com.datacollection.common.utils.Strings;
import com.datacollection.core.platform.elastic.ElasticClientProvider;
import com.datacollection.core.platform.elastic.ElasticConfig;
import org.apache.commons.collections.map.HashedMap;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * Created by kumin on 04/04/2017.
 */
public class ProfileIndexing {

    private final String elasticIndex = "datacollection";
    private String parentType = "profile";
    private String childType = "log";

    public void reduceData() {
        Configuration config = new Configuration();
        SubProperties properties = config.toSubProperties("elastic");
        ElasticConfig elasticConfig = new ElasticConfig(properties);
        Client elasticClient = ElasticClientProvider.getDefault(elasticConfig);

        SearchResponse scrollRes = elasticClient.prepareSearch("datacollection-ecommerce2")
                .setTypes("profile")
                .setScroll(new TimeValue(6000000))
                .setSize(10000)
                .execute()
                .actionGet();
        System.out.println(scrollRes.getHits().getTotalHits());
        int count = 0;
        while (true) {
            BulkRequestBuilder bulkRequest = elasticClient.prepareBulk();
            for (SearchHit hit : scrollRes.getHits()) {
                count++;
//                if(count>300515) {
                System.out.println("Doc indexed:" + count);
                Map hitSource = hit.getSource();
                //System.out.println(hitSource);
                Collection<String> phones = (Collection<String>) hitSource.get("phones");
                Collection<String> emails = (Collection<String>) hitSource.get("emails");
                String name = hitSource.get("fb_name") != null ? hitSource.get("fb_name").toString() : "";
                String profileId = hitSource.get("fb_id") != null ? hitSource.get("fb_id").toString() : "";
                Map<String, Object> parentSource = new HashedMap();
                parentSource.put("phones", phones);
                parentSource.put("emails", emails);
                parentSource.put("user_name", name);


                String content = hitSource.get("content").toString();
                String url = hitSource.get("fb_post") != null ? hitSource.get("fb_post").toString() : "";
                String[] urlSpit = url.split("/");
                String historyId = urlSpit[urlSpit.length - 1];
                Map<String, Object> childSource = new HashedMap();
                childSource.put("content", content);
                //childSource.put("url", url);

                String script = String.format(Locale.US, "ctx._source.phones = (ctx._source.phones+[%s]).unique({it});" +
                                "ctx._source.emails = (ctx._source.emails+[%s]).unique({it});",
                        Strings.join(phones, "\",\"", "\"", "\""),
                        Strings.join(emails, "\",\"", "\"", "\""));

                //Upsert Parent
                IndexRequest indexRequestParent = new IndexRequest(this.elasticIndex, this.parentType, profileId).source(parentSource);
                UpdateRequest updateRequestParent = new UpdateRequest(this.elasticIndex, this.parentType, profileId)
                        //.doc(parentSource)
                        .upsert(indexRequestParent)
                        .script(new Script(script));

                bulkRequest.add(updateRequestParent);

                //Upsert Child
                IndexRequest indexRequestChild = new IndexRequest(this.elasticIndex, this.childType, historyId).source(childSource).parent(profileId);
                UpdateRequest updateRequestChild = new UpdateRequest(this.elasticIndex, this.childType, historyId)
                        .doc(childSource)
                        .parent(profileId)
                        .upsert(indexRequestChild);

                bulkRequest.add(updateRequestChild);
//                }
            }
            if (bulkRequest.numberOfActions() > 0) {
                System.out.println("Inserting...");
                BulkResponse bulkResponse = bulkRequest.get();
                if (bulkResponse.hasFailures()) {
                    System.out.println(bulkResponse.buildFailureMessage());
                }
                elasticClient.admin().indices().prepareRefresh(this.elasticIndex).get();
            }

            scrollRes = elasticClient.prepareSearchScroll(scrollRes.getScrollId()).setScroll(new TimeValue(6000000)).execute().actionGet();
            if (scrollRes.getHits().getTotalHits() == 0) break;
        }

        elasticClient.close();
    }

    public static void main(String[] args) {
        ProfileIndexing profileIndexing = new ProfileIndexing();
        profileIndexing.reduceData();
    }
}
