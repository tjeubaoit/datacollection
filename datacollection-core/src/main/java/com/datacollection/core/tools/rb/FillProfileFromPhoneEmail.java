package com.datacollection.core.tools.rb;

import au.com.bytecode.opencsv.CSVReader;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.io.FileHelper;
import com.datacollection.common.utils.Strings;
import com.datacollection.core.platform.elastic.ElasticClientProvider;
import com.datacollection.core.platform.elastic.ElasticConfig;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
@SuppressWarnings("unchecked")
public class FillProfileFromPhoneEmail {

    public static void main(String[] args) throws Exception {
        String input = "/home/anhtn/Desktop/pr.csv";
        Configuration conf = new Configuration();
        Client client = ElasticClientProvider.getDefault(new ElasticConfig(conf));

        List<String> results = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(input), ',', '\"')) {
            while (true) {
                String[] next = reader.readNext();
                if (next == null) break;

                Map<String, String> fb = new HashMap<>();
                boolean found = false;

                String[] emails = next[1].split("\n");
                for (String email : emails) {
                    System.out.println("Search for email: " + email);
                    SearchResponse resp = client.prepareSearch("graph-profiles")
                            .setQuery(QueryBuilders.boolQuery()
                                    .should(QueryBuilders.termQuery("email.id.keyword", email))
                                    .should(QueryBuilders.termQuery("_email.id.keyword", email))
                                    .minimumShouldMatch(1))
                            .setSize(100)
                            .get();
                    for (SearchHit hit : resp.getHits().getHits()) {
                        Map<String, Object> source = hit.getSource();
                        if (!source.containsKey("account")) continue;
                        for (Map map : (ArrayList<Map>) source.get("account")) {
                            if (!"fb.com".equals(map.get("type"))) continue;
                            fb.put(map.get("name").toString(), map.get("id").toString());
                            System.out.println("Found: " + map.get("id"));
                            found = true;
                            break;
                        }
                    }
                }

                if (found) {
                    results.add("\"" + Strings.join(fb.values(), "\n") + "\"");
                    continue;
                }

                String[] phones = next[0].split("\n");
                for (String phone : phones) {
                    System.out.println("Search for phone: " + phone);
                    SearchResponse resp = client.prepareSearch("graph-profiles")
                            .setQuery(QueryBuilders.boolQuery()
                                    .should(QueryBuilders.termQuery("phone.id.keyword", phone))
                                    .should(QueryBuilders.termQuery("_phones.id.keyword", phone))
                                    .minimumShouldMatch(1))
                            .setSize(100)
                            .get();
                    for (SearchHit hit : resp.getHits().getHits()) {
                        Map<String, Object> source = hit.getSource();
                        if (!source.containsKey("account")) continue;
                        for (Map map : (ArrayList<Map>) source.get("account")) {
                            if (!"fb.com".equals(map.get("type"))) continue;
                            fb.put(map.get("name").toString(), map.get("id").toString());
                            System.out.println("Found: " + map.get("id"));
                            break;
                        }
                    }
                }

                if (results.isEmpty()) {
                    results.add("");
                } else {
                    results.add("\"" + Strings.join(fb.keySet(), ",") + "\"");
                }
            }
        }

        String output = "/home/anhtn/Desktop/fbpr.csv";
        results.forEach(s -> FileHelper.unsafeWrite(output, s, true));
    }
}
