package com.datacollection.platform.kafka;

import com.datacollection.common.config.ConfigurationException;
import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.Utils;

import java.io.IOException;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class KafkaConfig {

    public static final String PRODUCER_ENV_KEY = "kafka.producer.conf";
    public static final String CONSUMER_ENV_KEY = "kafka.consumer.conf";

    public static Properties producerProperties() {
        return load(PRODUCER_ENV_KEY, "kafka-producer.properties");
    }

    public static Properties consumerProperties() {
        return load(CONSUMER_ENV_KEY, "kafka-consumer.properties");
    }

    private static Properties load(String envKey, String defaultFileInClassPath) {
        final String path = System.getProperty(envKey);
        if (path != null) return Utils.loadPropsOrDefault(path);

        try {
            final ClassLoader cl = KafkaConfig.class.getClassLoader();
            return Utils.loadProps(cl.getResourceAsStream(defaultFileInClassPath));
        } catch (IOException e) {
            throw new ConfigurationException("Cannot find kafka properties in classpath");
        }
    }
}
