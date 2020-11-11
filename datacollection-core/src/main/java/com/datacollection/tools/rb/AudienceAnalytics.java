package com.datacollection.tools.rb;

import com.datacollection.common.config.Configuration;
import com.datacollection.common.utils.Strings;
import com.datacollection.graphdb.GraphDatabase;
import com.datacollection.graphdb.GraphSession;
import com.datacollection.metric.Counter;
import com.datacollection.metric.CounterMetrics;
import com.datacollection.metric.Sl4jPublisher;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class AudienceAnalytics {

    static final String[] KEYWORDS = {
            "times city", "pack hill", "park hill premium",
            "imperia garden sky lake", "hòa bình green city",
            "hoàng thành tower", "536a minh khai"
    };

    public static void main(String[] args) throws Exception {
        Counter counter = new Counter();
        CounterMetrics counterMetrics = new CounterMetrics(
                new Sl4jPublisher(),
                "analytics",
                "rb",
                counter,
                1000);
//        counterMetrics.start();

//        Connection con = HBaseConnectionProvider.getDefault(HBaseConfig.loadConfig());
//        try (Table table = con.getTable(TableName.valueOf("g:vertices"))) {
//            for (int i = 0; i < 10; i++) {
//                String prefix = i + "|_log-fb.com";
//                System.out.println("Prefix: " + prefix);
//
//                Scan scan = new Scan();
////                scan.setRowPrefixFilter(prefix.getBytes());
//                ResultScanner scanner = table.getScanner(scan);
//
//                for (Result r : scanner) {
//                    handleResult(r);
//                    counter.inc();
//                }
//            }
//        }

        GraphSession session = GraphDatabase.open(new Configuration());
        session.vertices("_log-fb.com").forEach(v -> {
            String content = v.property("content", "").toString();
            handleResult(content);
        });
    }

    static void handleResult(String content) {
        if (Strings.containsOnce(content, KEYWORDS)) {
            System.out.println(content);
        }
    }
}
