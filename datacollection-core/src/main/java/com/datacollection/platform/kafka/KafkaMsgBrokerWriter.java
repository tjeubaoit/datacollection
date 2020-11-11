package com.datacollection.platform.kafka;

import com.datacollection.common.concurrency.FutureAdapter;
import com.datacollection.common.config.Properties;
import com.datacollection.core.MsgBrokerWriter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.log4j.Level;

import java.util.concurrent.Future;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class KafkaMsgBrokerWriter implements MsgBrokerWriter {

    static final String KEY_KAFKA_TOPIC = "kafka.producer.topic";

    private String topic;
    private Producer<String, byte[]> producer;

    public KafkaMsgBrokerWriter(Properties props) {
        this.configure(props);
    }

    public KafkaMsgBrokerWriter() {
    }

    @Override
    public void configure(Properties p) {
        this.topic = p.getProperty(KEY_KAFKA_TOPIC, "test");
        this.producer = new KafkaProducer<>(KafkaConfig.producerProperties());
    }

    @Override
    public Future<Long> write(byte[] b) {
        return FutureAdapter.from(producer.send(new ProducerRecord<>(topic, b)), RecordMetadata::offset);
    }

    @Override
    public void close() {
        this.producer.close();
    }

    static {
        org.apache.log4j.Logger.getLogger("org").setLevel(Level.WARN);
        org.apache.log4j.Logger.getLogger("akka").setLevel(Level.WARN);
        org.apache.log4j.Logger.getLogger("kafka").setLevel(Level.WARN);
    }
}
