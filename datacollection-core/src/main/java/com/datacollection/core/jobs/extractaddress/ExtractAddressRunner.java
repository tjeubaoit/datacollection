package com.datacollection.core.jobs.extractaddress;

import com.datacollection.common.config.Configuration;
import com.datacollection.common.config.Properties;
import com.datacollection.common.mb.MsgBrokerFactory;
import com.datacollection.common.mb.MsgHandler;
import com.datacollection.common.mb.MsgBrokerReader;
import com.datacollection.common.mb.Record;
import com.datacollection.common.mb.Records;
import com.datacollection.common.serialize.Deserializer;
import com.datacollection.common.serialize.Serialization;
import com.datacollection.common.utils.Threads;
import com.datacollection.common.utils.Utils;
import com.datacollection.core.platform.kafka.KafkaMsgBrokerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by kumin on 01/09/2017.
 * Updated by tjeubaoit on 28/09/2017
 */
public class ExtractAddressRunner implements MsgHandler {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final List<Province> provinces;
    private final MsgBrokerReader msgBrokerReader;
    private final Deserializer<Profile> deserializer;
    private final ExtractAddressRepository repository;
    private final AtomicBoolean flagStop = new AtomicBoolean(false);

    public ExtractAddressRunner() {
        Properties props = new Configuration().toSubProperties("extract_address");

        this.msgBrokerReader = this.createBrokerReader(props);
        this.msgBrokerReader.addHandler(this);
        this.deserializer = Serialization.create(props.getProperty("mb.deserializer"), Profile.class).deserializer();

        try {
            this.provinces = PrepareData.loadProvinces();
            this.repository = new CassandraRepository(props);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Utils.addShutdownHook(this::stopExtra);
    }

    private void stopExtra() {
        logger.info("Stop message queue consumer threads...");
        flagStop.set(true);
        repository.close();
    }

    private MsgBrokerReader createBrokerReader(Properties props) {
        MsgBrokerFactory factory = new KafkaMsgBrokerFactory();
        logger.info("MessageBrokerFactory class: " + factory.getClass().getName());

        MsgBrokerReader reader = factory.createReader();
        reader.configure(props);
        return reader;
    }

    @Override
    public void handle(Records records) {
        String provincesRegex = ExtractAddressHandler.buildProvincesRegex(provinces);
        Map<String, String> districtsRegexMap = ExtractAddressHandler.buildDistrictsRegex(provinces);

        for (Record record : records) {
            try {
                Profile profile = deserializer.deserialize(record.data());

                // ignore records
                if (profile.getPhones().isEmpty() && profile.getEmails().isEmpty()) {
                    continue;
                }

                Map<String, String> extras = profile.getExtras();
                String address = extras != null ? extras.get("address") : null;
                if (address != null) continue;

                Runnable task = new ExtractAddressHandler(profile.getUid(), provincesRegex,
                        districtsRegexMap, repository);
                while (!flagStop.get()) {
                    try {
                        task.run();
                        break;
                    } catch (RuntimeException e) {
                        logger.error("Task failed for record: " + new String(record.data()), e);
                        Threads.sleep(TimeUnit.SECONDS.toMillis(10));
                    }
                }
            } catch (IOException e) {
                logger.warn("Deserialize record error", e);
            }
        }
    }

    public static void main(String[] args) {
        ExtractAddressRunner aet = new ExtractAddressRunner();
        aet.msgBrokerReader.start();
    }
}
