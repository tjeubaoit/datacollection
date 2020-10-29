package com.datacollection.core.tools;

import au.com.bytecode.opencsv.CSVReader;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.DriverException;
import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.utils.NullProtector;
import com.datacollection.common.utils.Strings;
import com.datacollection.common.utils.Threads;
import com.datacollection.core.platform.cassandra.CassandraClusterProvider;
import com.datacollection.core.platform.cassandra.CassandraConfig;
import com.datacollection.core.platform.elastic.ElasticClientProvider;
import com.datacollection.core.platform.elastic.ElasticConfig;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class RbProfileSearcher {

    public static void main(String[] args) {
        Configuration config = new Configuration();
        Client client = ElasticClientProvider.getDefault(new ElasticConfig(config));

        session = CassandraClusterProvider.getDefault(new CassandraConfig(config)).connect("datacollection1");

        String input = "/home/anhtn/Desktop/api.csv";
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
            return fbUrl + "\t" +
                    "\t" +
                    company.replace("\n", "").replace("\"", "") + "\t" +
                    position.replace("\n", "").replace("\"", "") + "\t"
                    + phone.replace("\n", "").replace("\"", "");
        }
    }

    static void search(Client client, String input, String output) {
        try (CSVReader reader = new CSVReader(new FileReader(input));
             PrintWriter writer = new PrintWriter(new FileWriter(output))) {

            int count = 1;
            String[] split;
            while ((split = reader.readNext()) != null) {
                if (count == 1) {
                    count++;
                    continue;
                }
                if (split.length == 0) break;

                Profile p = new Profile();
                p.email = NullProtector.get(split, 3).orElse("").toLowerCase();
                p.phone = NullProtector.get(split, 2).orElse("");
                if (regexHelper.isPhone("0" + p.phone)) {
                    p.phone = "0" + p.phone;
                }

                System.out.println(String.format(Locale.US, "Process line %d, %s, %s",
                        count++, p.phone, p.email));

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
                        fbNameUrlMapping.put(hit.getSource().get("user_name").toString(), hit.getId());
                    }

                    p.fbUrl = Strings.join(fbNameUrlMapping.values(), "-");
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

                while (true) {
                    try {
                        advancedSearchWithCassandra(p);
                        break;
                    } catch (DriverException e) {
                        e.printStackTrace();
                        Threads.sleep(1000);
                    }
                }

                System.out.println(p.toString());

                writer.println(p);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Session session;
    static final ProfileRegexHelper regexHelper = ProfileRegexHelper.getDefault();

    static void advancedSearchWithCassandra(Profile p) {
        UUID guid = findGuidByPhone(p.phone);
        if (guid == null) guid = findGuidByEmail(p.email);
        if (guid == null) return;

        try {
            Row row;
            do {
                row = session.execute("select facebook from profiles where guid = ?", guid).one();
            } while (row == null);

            Map<String, String> fb = row.getMap("facebook", String.class, String.class);
            if (fb.size() > 3) return;

            Map<String, String> fbNameUrlMapping = new LinkedHashMap<>();
            for (String id : fb.keySet()) {
                fbNameUrlMapping.put(fb.get(id), id);
            }
            p.fbUrl = Strings.join(fbNameUrlMapping.values(), "-");
        } catch (RuntimeException e) {
            System.err.println("Fail with guid " + guid);
            throw e;
        }
    }

    static UUID findGuidByPhone(String phone) {
        if (regexHelper.isPhone(phone)) {
            if (phone.isEmpty()) return null;
            Row row = session.execute("select guid from phones where phone = ?", phone).one();
            return row != null ? row.getUUID("guid") : null;
        } else {
            Collection<String> phones = regexHelper.extractPhones(phone);
            for (String p : phones) {
                Row row = session.execute("select guid from phones where phone = ?", p).one();
                if (row != null) return row.getUUID("guid");
            }
        }
        return null;
    }

    static UUID findGuidByEmail(String email) {
        if (email.isEmpty()) return null;
        Row row = session.execute("select guid from emails where email = ?", email).one();
        return row != null ? row.getUUID("guid") : null;
    }
}
