package com.datacollection.extract.elastic;

import com.datacollection.common.config.Configuration;
import com.datacollection.extract.Extractor;
import com.datacollection.extract.model.GenericModel;
import com.datacollection.platform.elastic.ElasticClientProvider;
import com.datacollection.platform.elastic.ElasticConfig;
import com.datacollection.platform.jdbc.ConnectionProviders;
import com.datacollection.platform.jdbc.JdbcConfig;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
@author - kumin
 */
public class DmpExtractor extends Extractor {

    private Client client;
    private Map<String, String> interestMap = new HashMap<>();


    public DmpExtractor(Configuration config) {
        super("guid", config);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        ElasticConfig elasticConfig = new ElasticConfig(props);
        System.out.println(elasticConfig.getHosts());
        client = ElasticClientProvider.getOrCreate("dmp", elasticConfig);
        loadInterest();
    }

    @Override
    protected void onLoop() throws Exception {
        SearchResponse scroll = client.prepareSearch("dmp_pc", "dmp_mob")
                .setTypes("info")
                .setSize(1000)
                .setScroll(new TimeValue(6000000))
                .get();

        while (isNotCanceled()) {

            for (SearchHit hit : scroll.getHits()) {
                store(buildGenericModel(hit));
            }
            scroll = client.prepareSearchScroll(scroll.getScrollId()).setScroll(new TimeValue(6000000))
                    .execute().actionGet();

            if (scroll.getHits().getTotalHits() == 0) break;
        }
    }

    private GenericModel buildGenericModel(SearchHit hit) {
        Map<String, Object> source = hit.getSource();
        Map<String, Object> post = new LinkedHashMap<>();
        post.put("domain", "dmp");
        post.put("post_date", new Date());
        if (source.get("phones") != null) {
            post.put("phones", (Collection<String>) source.get("phones"));
        }
        if (source.get("emails") != null) {
            post.put("emails", (Collection<String>) source.get("emails"));
        }
        if (source.get("idSrc") != null) {
            post.put("viet_id", (Collection<String>) source.get("idSrc"));
        }

        String guid = source.get("id").toString();
        post.put("guid", guid);
        SearchResponse sr = client.prepareSearch("dmp_pc", "dmp_mob")
                .setTypes("demographics")
                .setQuery(QueryBuilders.matchPhraseQuery("id", guid))
                .get();

        if (sr.getHits().totalHits() > 0) {
            SearchHit dgHit = sr.getHits().getAt(0);
            if (dgHit.getSource().get("gender") != null) {
                post.put("gender", this.getGender(Integer.valueOf(dgHit.getSource().get("gender").toString())));
            }
            if (dgHit.getSource().get("age") != null) {
                post.put("age", this.getAge(Integer.valueOf(dgHit.getSource().get("age").toString())));
            }

            if (dgHit.getSource().get("interests") != null) {
                List<Integer> interests = (List<Integer>) dgHit.getSource().get("interests");
                Set<String> topics = new HashSet<>();
                for (Integer topicId : interests) {
                    if (this.getInterest(topicId.toString()) != null) {
                        topics.add(this.getInterest(topicId.toString()));
                    }
                }
                post.put("interest", topics.toString().replaceAll("\\[|\\]", ""));
            }
        }
        return new GenericModel(guid, GenericModel.TYPE_DMP, post);
    }

    public String getGender(int no) {
        switch (no) {
            case 1:
                return "m";
            case 2:
                return "f";
        }
        return null;
    }

    public String getAge(int no) {
        switch (no) {
            case 3:
                return "<18";
            case 4:
                return "18 -> 24";
            case 5:
                return "25 -> 34";
            case 6:
                return "35 -> 50";
            case 7:
                return ">50";
        }
        return null;
    }

    public void loadInterest() {
        logger.info("Loading Interest Map ");
        try {
            Connection sqlConnection = ConnectionProviders.getOrCreate("dmp-interest", new JdbcConfig(props));
            String query = "SELECT name, topics FROM map_interest";
            ResultSet rs = sqlConnection.prepareStatement(query).executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                String[] topicsSplit = rs.getString("topics").split(",");
                for (int i = 0; i < topicsSplit.length; i++) {
                    interestMap.put(topicsSplit[i], name);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getInterest(String topicId) {
        return interestMap.get(topicId);
    }

    public static void main(String[] args) {
        new DmpExtractor(new Configuration()).start();
//        DmpExtractor de = new DmpExtractor(new Configuration());
//        de.onInitialize();
//        System.out.println(de.interestMap);
    }
}
