package com.datacollection.tools.rb;

import com.datacollection.common.config.Configuration;
import com.datacollection.common.io.FileHelper;
import com.datacollection.graphdb.Direction;
import com.datacollection.graphdb.GraphDatabase;
import com.datacollection.graphdb.GraphSession;
import com.datacollection.graphdb.Vertex;
import com.datacollection.graphdb.VertexSet;
import com.datacollection.platform.elastic.ElasticClientProvider;
import com.datacollection.platform.elastic.ElasticConfig;
import org.elasticsearch.client.Client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
@SuppressWarnings("unchecked")
public class FillProfileFromGuid {

    static final String[] files = {
            "pr_MOB", "pr_PC"
    };

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Client client = ElasticClientProvider.getDefault(new ElasticConfig(conf));
        GraphSession session = GraphDatabase.open(conf);

        Map<String, Map<String, String>> data = new HashMap<>();
        int count = 0;
        int found = 0;

        for (String file : files) {
            String path = "/home/anhtn/Desktop/" + file;
            try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String guid = line.trim();
                    if (!data.containsKey(guid)) {
                        data.put(guid, new HashMap<>());
                    }
                    Map<String, String> props = data.get(guid);

//                    SearchResponse resp = client.prepareSearch("graph-profiles")
//                            .setQuery(QueryBuilders.termQuery("guid.id.keyword", guid))
//                            .get();
//                    for (SearchHit hit : resp.getHits().getHits()) {
//                        Map<String, Object> source = hit.getSource();
//                        if (source.containsKey("_email")) {
//                            List<String> emails = new ArrayList<>();
//                            for (Map map : (ArrayList<Map>) source.get("_email")) {
//                                emails.add(map.get("id").toString());
//                            }
//                            props.put("email", Strings.join(emails, ","));
//                        }
//                        if (source.containsKey("email")) {
//                            List<String> emails = new ArrayList<>();
//                            for (Map map : (ArrayList<Map>) source.get("email")) {
//                                emails.add(map.get("id").toString());
//                            }
//                            props.put("email", Strings.join(emails, ","));
//                        }
//                        if (source.containsKey("_phone")) {
//                            List<String> phones = new ArrayList<>();
//                            for (Map map : (ArrayList<Map>) source.get("_phone")) {
//                                phones.add(map.get("id").toString());
//                            }
//                            props.put("phone", Strings.join(phones, ","));
//                        }
//                        if (source.containsKey("phone")) {
//                            List<String> phones = new ArrayList<>();
//                            for (Map map : (ArrayList<Map>) source.get("phone")) {
//                                phones.add(map.get("id").toString());
//                            }
//                            props.put("phone", Strings.join(phones, ","));
//                        }
//                        if (source.containsKey("fullname")) {
//                            for (Map map : (ArrayList<Map>) source.get("fullname")) {
//                                props.put("name", map.get("id").toString());
//                                break;
//                            }
//                        }
//                        if (source.containsKey("user_name")) {
//                            for (Map map : (ArrayList<Map>) source.get("user_name")) {
//                                props.put("name", map.get("id").toString());
//                                break;
//                            }
//                        }
//                        if (source.containsKey("username")) {
//                            for (Map map : (ArrayList<Map>) source.get("username")) {
//                                props.put("name", map.get("id").toString());
//                                break;
//                            }
//                        }
//                    }

                    long now = System.currentTimeMillis();
                    Vertex vGuid = Vertex.create(guid, "guid");
                    Vertex vProfile = session.vertices(vGuid, Direction.OUT, "profile").first();
                    if (vProfile != null) {
                        System.out.println("Found: " + guid + ", total: " + ++found);
                        VertexSet emailSet = session.verticesByAdjVertexLabels(
                                vProfile, Direction.OUT, "email");
                        emailSet.forEach(vEmail -> props.put("email", vEmail.id()));

                        VertexSet phoneSet = session.verticesByAdjVertexLabels(
                                vProfile, Direction.OUT, "phone");
                        phoneSet.forEach(vPhone -> props.put("phone", vPhone.id()));
                    }
                    System.out.println("Process line: " + ++count
                            + ", took: " + (System.currentTimeMillis() - now)
                            + ", " + props);
                }
            }
        }

        String format = "%s\t\"%s\"\t\"%s\"\t%s\t%s\t%s\t%s";
        for (Map<String, String> map : data.values()) {
            if (!map.containsKey("email") && !map.containsKey("phone")) continue;
//            String line = Strings.format(format,
//                    map.getOrDefault("name", ""),
//                    map.getOrDefault("email", ""),
//                    map.getOrDefault("phone", ""),
//                    map.getOrDefault("536a minh khai", ""),
//                    map.getOrDefault("parkhill", ""),
//                    map.getOrDefault("parkhill premium", ""),
//                    map.getOrDefault("greencity", ""));
//            FileHelper.unsafeWrite("/home/anhtn/Desktop/zamba.csv", line, true);
            FileHelper.unsafeWrite("/home/anhtn/Desktop/pr.csv", map.toString(), true);
        }
    }
}
