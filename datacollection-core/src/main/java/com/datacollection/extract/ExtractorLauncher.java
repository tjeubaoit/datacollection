package com.datacollection.extract;

import com.datacollection.common.config.Configuration;
import com.datacollection.common.utils.Reflects;
import com.datacollection.common.utils.Strings;
import com.datacollection.common.utils.Utils;
import com.datacollection.extract.mongo.OrgProfileExtractor;
import com.datacollection.metric.MetricExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ExtractorLauncher {

    public static void main(String[] args) throws Exception {
        Configuration config = new Configuration();
        Logger logger = LoggerFactory.getLogger(ExtractorLauncher.class);

        if (args.length == 0) {
            logger.info("Run in develop mode");
            startExtractors(
                    new OrgProfileExtractor(config)
            );
        } else {
            logger.info("Run in server mode, start extractors: " + Strings.join(Arrays.asList(args), " "));
            Extractor[] extractors = new Extractor[args.length];
            for (int i = 0; i < args.length; i++) {
                extractors[i] = Reflects.newInstance(args[i], new Class<?>[]{Configuration.class}, config);
            }
            startExtractors(extractors);
        }

        MetricExporter metricExporter = new MetricExporter(config.toSubProperties("extract"));
        metricExporter.start();

        Utils.addShutdownHook(() -> {
            logger.info("Stop metric exporter server...");
            metricExporter.stop();
            logger.info("Metric exporter stopped");
        });

        metricExporter.join();
    }

    static void startExtractors(Extractor... extractors) {
        for (Extractor extractor : extractors) {
            new Thread(extractor).start();
        }
    }
}
