package com.datacollection.core.tools.customer;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import com.datacollection.common.config.Configuration;
import com.datacollection.core.platform.elastic.ElasticBulkInsert;
import com.datacollection.core.platform.elastic.ElasticClientProvider;
import com.datacollection.core.platform.elastic.ElasticConfig;
import com.datacollection.core.platform.mongo.MongoClientProvider;
import com.datacollection.core.platform.mongo.MongoConfig;
import org.bson.Document;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerPotential {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Rule rule;
    private ElasticBulkInsert elasticBulkInsert;

    public CustomerPotential() {
        rule = new Rule();
        rule.loadRule("data/rules.txt");

        elasticBulkInsert = new ElasticBulkInsert(new Configuration());
    }

    public void findFromElastic(String outputFile) {
        Client client = ElasticClientProvider.getDefault(new ElasticConfig(new Configuration()));
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        boolQuery.must(QueryBuilders.matchPhraseQuery("tag.id", "company"));
        SearchResponse sr = client.prepareSearch("graph-profiles")
                .setTypes("profiles")
                .setQuery(QueryBuilders.matchPhraseQuery("tag.id", "company"))
                .setSize(1000)
                .setScroll(new TimeValue(6000000))
                .execute()
                .actionGet();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {

            while (true) {
                for (SearchHit hit : sr.getHits()) {
                    Map<String, Object> source = hit.getSource();
                    if (source.get("company") == null) continue;
                    Map<String, String> company = ((List<Map<String, String>>) source.get("company")).get(0);
                    String companyName = company.get("id");
                    if (!rule.containWords(companyName) || !rule.notContainWords(companyName)) continue;
                    System.out.println(company.get("id"));
                    String name = company.get("id") != null ? company.get("id") : "";
                    name = name.replace("\"", "");
                    String manager = company.get("manager") != null ? company.get("manager") : "";
                    manager = manager.replace("\"", "");
                    String activedate = company.get("activatedate") != null ? company.get("activatedate") : "";
                    String permitdate = company.get("permitdate") != null ? company.get("permitdate") : "";

                    String phone = "";
                    String address = "";
                    String email = "";
                    String taxcode = "";

                    if (source.get("phone") != null) {
                        Map<String, String> phoneMap = ((List<Map<String, String>>) source.get("phone")).get(0);
                        phone = phoneMap.get("id");
                    }
                    if (source.get("phone") != null) {
                        Map<String, String> phoneMap = ((List<Map<String, String>>) source.get("phone")).get(0);
                        phone = phoneMap.get("id");
                    }

                    if (source.get("email") != null) {
                        Map<String, String> emailMap = ((List<Map<String, String>>) source.get("email")).get(0);
                        email = emailMap.get("id");
                    }

                    if (source.get("address") != null) {
                        Map<String, String> addressMap = ((List<Map<String, String>>) source.get("address")).get(0);
                        address = addressMap.get("id").split(">")[0].replaceAll("\"", "");
                    }

                    if (source.get("description") != null) {

                    }

                    if (source.get("taxcode") != null) {
                        List<Map<String, String>> taxcodes = (List<Map<String, String>>) source.get("taxcode");
                        for (Map<String, String> taxcodeMap : taxcodes) {
                            taxcode += taxcodeMap.get("id") + ",";
                        }
                    }

                    String csvLine = "\"" + name + "\",\"" + phone + "\",\"" + email + "\",\"" + address + "\",\"" + manager + "\",\"" +
                            activedate + "\",\"" + permitdate + "\",\"" + taxcode + "\"\n";
                    bw.write(csvLine);
                }

                sr = client.prepareSearchScroll(sr.getScrollId()).setScroll(new TimeValue(6000000)).get();

                if (sr.getHits().getHits().length == 0) break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void findFromMongodb() {
        logger.info("getting org from mongodb");

        Map<String, Map<String, Object>> sourceMap = new HashMap<>();

        MongoConfig mongoConfig = new MongoConfig(new Configuration().toSubProperties("org"));
        MongoClient client = MongoClientProvider.getOrCreate("customer", mongoConfig);
        MongoCollection<Document> collection = client.getDatabase("datacollection")
                .getCollection("orgs");

        FindIterable<Document> documents = collection.find();
//        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
        int counter = 0;
        for (Document document : documents) {
            counter++;
            Map<String, Object> source = new HashMap<>();

            if (document.getString("Title") == null) continue;
            String companyName = document.getString("Title");
//            if (!rule.containWords(companyName) || !rule.notContainWords(companyName)) continue;
            System.out.println(companyName + ":" + counter);
            companyName = companyName.replace("\"", "");

            String manager = getInfoString(document, "Manager");
            String activedate = getInfoString(document, "ActivateDate");
            String permitdate = getInfoString(document, "PermitDate");
            String phone = getInfoString(document, "Phone");
            String address = getInfoString(document, "Address");
            String email = getInfoString(document, "Email");
            String taxcode = getInfoString(document, "TaxCode");
            String website = getInfoString(document, "Website");
            String description = getInfoString(document, "Description");
            List<String> products = new ArrayList<>();
            if (document.get("ProductOrServices") != null) {
                List<Document> productOrServices = (List<Document>) document.get("ProductOrServices");
                for (Document ps : productOrServices) {
                    products.add(ps.getString("Name"));
                }
            }
            String branchMain = getInfoString(document, "BranchMain");
            List<String> branchs = new ArrayList<>();
            if (document.get("BranchList") != null) {
                List<Document> branchList = (List<Document>) document.get("BranchList");
                for (Document bl : branchList) {
                    branchs.add(bl.getString("Name"));
                }
            }

            source.put("name", companyName);
            source.put("manager", manager);
            source.put("activedate", activedate);
            source.put("permitdate", permitdate);
            source.put("phone", phone);
            source.put("email", email);
            source.put("address", address);
            source.put("taxcode", taxcode);
            source.put("website", website);
            source.put("product", products);
            source.put("branchmain", branchMain);
            source.put("branchs", branchs);
            source.put("description", description);

            sourceMap.put(companyName.toLowerCase(), source);
//                String csvLine = "\"" + companyName + "\",\"" + phone + "\",\"" + email + "\",\"" + address + "\",\"" + manager + "\",\"" +
//                        activedate + "\",\"" + permitdate + "\",\"" + taxcode + "\",\"" + website + "\",\"" + products + "\"\n";
//                bw.write(csvLine);
        }


        logger.info("getting fanpage for org");
        findFanpageFromMongoByOtherWay(sourceMap);
        logger.info("inserting into elastic");

        for (String key : sourceMap.keySet()) {
            elasticBulkInsert.addRequest("customer", key, sourceMap.get(key));
            if (elasticBulkInsert.bulkSize() > 500) {
                BulkResponse bulkResponse = elasticBulkInsert.submitBulk();
                logger.info("it take " + bulkResponse.getTook() + " s to summit bulk!");
            }
        }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public String getInfoString(Document doc, String key) {
        String info = doc.getString(key) != null ?
                doc.getString(key) : "";
        return info.replace("\"", "");
    }

    public void findFanpageFromMongoByOtherWay(Map<String, Map<String, Object>> sourceMap) {
        MongoConfig mongoConfig = new MongoConfig(new Configuration().toSubProperties("fbpage"));
        MongoClient client = MongoClientProvider.getOrCreate("customer", mongoConfig);
        MongoCollection<Document> collection = client.getDatabase("fbpages").getCollection("links");
//        try (
//                BufferedReader br = new BufferedReader(new FileReader("data/companies.txt"));
//                BufferedWriter bw = new BufferedWriter(new FileWriter("data/companypage.csv"))
//        ) {
//            List<String> companies = new ArrayList<>();
//            Map<String, String> companyMapFanpage = new HashMap<>();
//            String line;
        int counter = 0;
//            while ((line = br.readLine()) != null) {
//                line = line.toLowerCase();
//                companies.add(line);
//            }
        MongoIterable<Document> documents = collection.find();
        for (Document document : documents) {
            System.out.println(++counter);
            if (document.getString("Name") == null) continue;
            String pageName = document.getString("Name").toLowerCase();
            String pageLink = document.getString("Link");
            if (sourceMap.get(pageName) != null) {
                System.out.println("Found It:" + pageName + ":" + pageLink);
                sourceMap.get(pageName).put("fanpage", pageLink);
            }
        }

//            for (String company : companies) {
//                bw.write("\"" + company + "\",\"" +
//                        (companyMapFanpage.get(company) != null ? companyMapFanpage.get(company) : "") + "\"\n");
//            }

//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public static void main(String[] args) {
        CustomerPotential cp = new CustomerPotential();
        cp.findFromMongodb();
    }

}
