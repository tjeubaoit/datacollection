package com.datacollection.tools;

import com.datacollection.common.config.Configuration;
import com.datacollection.common.utils.Strings;
import com.datacollection.platform.elastic.ElasticClientProvider;
import com.datacollection.platform.elastic.ElasticConfig;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ProfileSearcher {

    public static void main(String[] args) {
        Configuration config = new Configuration();
        Client client = ElasticClientProvider.getDefault(new ElasticConfig(config));

        String input = "/home/anhtn/Desktop/bds.txt";
        String output = "/home/anhtn/Desktop/bds.csv";
        search(client, input, output);
    }

    static class Profile {
        String phone = "";
        String email = "";
        String fbName = "";
        String fbUrl = "";
        String address = "";
        String city = "";
        String company = "";
        String position = "";
        String name = "";

        @Override
        public String toString() {
            return email + "\t" +
                    phone + "\t" +
                    fbName + "\t" +
                    fbUrl + "\t" +
                    name + "\t" +
                    address + "\t" +
                    city + "\t" +
                    company + "\t" +
                    position + "\t";
        }
    }

    static void search(Client client, String input, String output) {
        try (BufferedReader reader = new BufferedReader(new FileReader(input));
             PrintWriter writer = new PrintWriter(new FileWriter(output))) {

            String line;
            int count = 1;
            while ((line = reader.readLine()) != null) {
                if (Strings.isNullOrEmpty(line)) break;
                String[] split = line.split("\t");

                Profile p = new Profile();
                p.email = split[0];
                p.phone = split[1];

                // search in facebook, forum data
                SearchResponse response = client.prepareSearch("datacollection-*")
                        .setTypes("phone", "email")
                        .setQuery(QueryBuilders.termsQuery("_uid",
                                "email#" + p.email, "phone#" + p.phone))
                        .execute().actionGet();

                Set<String> parents = new HashSet<>();
                for (SearchHit hit : response.getHits().getHits()) {
                    parents.add(hit.getFields().get("_parent").getValue());

                    if (hit.getIndex().startsWith("datacollection-forum")) {
                        p.address = hit.getSource().getOrDefault("address", "").toString();
                        p.city = hit.getSource().getOrDefault("city", "").toString();
                    }
                }

                Map<String, String> fbNameUrlMapping = new LinkedHashMap<>();
                if (!parents.isEmpty()) {
                    response = client.prepareSearch("datacollection-*")
                            .setTypes("profile")
                            .setQuery(QueryBuilders.termsQuery("_id", parents))
                            .execute().actionGet();

                    for (SearchHit hit : response.getHits().getHits()) {
                        if (hit.getIndex().startsWith("datacollection-forum")) continue;
                        String profileUrl = "https://facebook.com/" + hit.getId();
                        fbNameUrlMapping.put(hit.getSource().get("user_name").toString(), profileUrl);
                    }

                    p.fbName = Strings.join(fbNameUrlMapping.keySet(), "\n", "\"", "\"");
                    p.fbUrl = Strings.join(fbNameUrlMapping.values(), "\n", "\"", "\"");
                }

                // search in ecommerce data
                SearchResponse response1 = client.prepareSearch("datacollection-ecommerce")
                        .setTypes("profile")
                        .setQuery(QueryBuilders.boolQuery()
                                .should(QueryBuilders.termQuery("phones", p.phone))
                                .should(QueryBuilders.termQuery("_id", p.email))
                                .minimumShouldMatch(1))
                        .execute().actionGet();

                for (SearchHit hit : response1.getHits().getHits()) {
                    p.name = hit.getSource().getOrDefault("full_name", "").toString();
                    p.address = hit.getSource().getOrDefault("address", "").toString();
                }

                // search in raw data
                SearchResponse response2 = client.prepareSearch("datacollection-raw")
                        .setTypes("excel")
                        .setQuery(QueryBuilders.boolQuery()
                                .should(QueryBuilders.termQuery("phone", p.phone))
                                .should(QueryBuilders.termQuery("email", p.email))
                                .minimumShouldMatch(1))
                        .execute().actionGet();

                for (SearchHit hit : response2.getHits().getHits()) {
                    p.name = hit.getSource().getOrDefault("name", "").toString();
                    p.address = hit.getSource().getOrDefault("address", " ").toString();
                    p.company = hit.getSource().getOrDefault("company", " ").toString();
                    p.position = hit.getSource().getOrDefault("position", " ").toString();
                }

                System.out.println("Process line: " + count++);
                System.out.println(p.toString());

                writer.println(p);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
