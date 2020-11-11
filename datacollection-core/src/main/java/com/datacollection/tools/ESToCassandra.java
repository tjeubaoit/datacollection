package com.datacollection.tools;

import com.datacollection.collect.CollectService;
import com.datacollection.collect.GraphCollectService;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.CLIArgumentParser;
import com.datacollection.common.utils.Threads;
import com.datacollection.extract.model.GenericModel;
import com.datacollection.platform.elastic.ElasticClientProvider;
import com.datacollection.platform.elastic.ElasticConfig;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Utility tool: reindex data from ElasticSearch to Cassandra
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
@SuppressWarnings("unchecked")
public class ESToCassandra {

    public static void main(String[] args) {
        CLIArgumentParser parser = new CLIArgumentParser.Builder()
                .addOpt("t", "type", true, "ES index type", false)
                .addOpt("i", "index", true, "ES index name", false)
                .addOpt("s", "skip", true, "Number items to skip", false)
                .build();
        CLIArgumentParser.Result result = parser.parse(args);

        String index = result.getOptionValue("index","datacollection-ecommerce2");
        String type = result.getOptionValue("index","profile");
        String[] types = type.split(",");
        int skip = Integer.parseInt(result.getOptionValue("skip", "0"));

        Properties props = new Configuration().toSubProperties("datacollection");
        Logger logger = LoggerFactory.getLogger(ESToCassandra.class);

        CollectService service = new GraphCollectService(props);

        ElasticConfig elasticConfig = new ElasticConfig(props);
        Client client = ElasticClientProvider.getDefault(elasticConfig);

        SearchResponse scrollResp = client.prepareSearch(index).setTypes(types)
                .setQuery(QueryBuilders.matchAllQuery())
                .setSize(1000)
                .setScroll(new TimeValue(30, TimeUnit.MINUTES))
                .execute().actionGet();
        long totalPost = scrollResp.getHits().getTotalHits();
        logger.info("Total post: " + totalPost);

        int count = 0;
        Processor processor = Processor.create(index);
        logger.info("Processor created: " + processor.getClass().getSimpleName());

        while (true) {
            for (SearchHit hit : scrollResp.getHits()) {
                if (count++ < skip) {
                    System.out.println("Skip line: " + count);
                    continue;
                }
                while (true) {
                    try {
                        long now = System.currentTimeMillis();
                        processor.process(service, hit);
                        System.out.println("Processing: " + count + ", took: "
                                + (System.currentTimeMillis() - now));
                        break;
                    } catch (RuntimeException e) {
                        logger.error("Process record error: " + hit.getId(), e);
                        Threads.sleep(5000);
                    }
                }
            }

            System.out.println("Start scroll next docs");
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
                    .setScroll(new TimeValue(30, TimeUnit.MINUTES))
                    .execute().actionGet();

            //Break condition: No hits are returned
            if (scrollResp.getHits().getHits().length == 0) break;
        }

        client.close();
    }

    private interface Processor {

        static Processor create(String name) {
            switch (name) {
                case "datacollection-webid":
                case "datacollection-log":
                    return new WeblogProcessor();
                case "datacollection-raw":
                    return new ExcelProcessor();
                case "datacollection-ecommerce":
                    return new EcommerceProcessor();
                default:
                    throw new IllegalArgumentException();
            }
        }

        void process(CollectService service, SearchHit hit);
    }

    private static class WeblogProcessor implements Processor {

        @Override
        public void process(CollectService service, SearchHit hit) {
            Map<String, Object> source = hit.getSource();
            GenericModel model = new GenericModel();

            String domain = source.getOrDefault("domain", "").toString();
            if (domain.isEmpty() || domain.equals("gg")) return;
            source.remove("domain");

            model.setType("fr.weblog");
            model.getPost().putAll(source);
            model.getPost().put("adsid", hit.getId());

            service.collect(model);
        }
    }

    private static class ExcelProcessor implements Processor {

        @Override
        public void process(CollectService service, SearchHit hit) {
            Map<String, Object> source = hit.getSource();
            GenericModel model = new GenericModel();
            model.setType("excel");

            Set<String> emails = new HashSet<>();
            Set<String> phones = new HashSet<>();

            if (source.containsKey("phone")) {
                for (String phone : (Collection<String>) source.get("phone")) {
                    phone = phone.trim();
                    if (phone.isEmpty()) continue;
                    if (!phone.startsWith("0")) phone = "0" + phone;
                    phones.add(phone);
                }
            }

            if (source.containsKey("email")) {
                String email = source.get("email").toString().trim();
                if (!email.isEmpty()) emails.add(email);
            }

            if (phones.isEmpty() && emails.isEmpty()) {
                System.out.println("No phones or emails");
                return;
            }

            model.getPost().put("phones", phones);
            model.getPost().put("emails", emails);

            if (source.containsKey("name")) {
                source.put("full_name", source.get("name"));
                source.remove("name");
            }
            source.remove("email");
            source.remove("phone");
            model.getPost().putAll(source);

            service.collect(model);
        }
    }

    private static class EcommerceProcessor implements Processor {

        @Override
        public void process(CollectService service, SearchHit hit) {
            Map<String, Object> source = hit.getSource();
            String id = hit.getId();

            GenericModel model = new GenericModel();
            model.setType("ecommerce");
            model.getPost().put("sid", id);
            model.getPost().putAll(source);

            service.collect(model);
        }
    }
}
