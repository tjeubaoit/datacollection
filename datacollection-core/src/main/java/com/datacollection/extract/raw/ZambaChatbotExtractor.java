package com.datacollection.extract.raw;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.config.Properties;
import com.datacollection.extract.model.GenericModel;
import com.datacollection.metric.Counter;
import com.datacollection.metric.CounterMetrics;
import com.datacollection.metric.Sl4jPublisher;
import com.datacollection.platform.kafka.KafkaMsgBrokerWriter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ZambaChatbotExtractor {

    private KafkaMsgBrokerWriter kafkaWriter;
    private CounterMetrics counterMetrics;
    private Counter counter;

    public ZambaChatbotExtractor(Properties props){
        kafkaWriter = new KafkaMsgBrokerWriter();
        kafkaWriter.configure(props);

        counter = new Counter();
        counterMetrics = new CounterMetrics(new Sl4jPublisher(), "default-metric-group",
                "zamba-chatbot-extrctor", counter, 1000);
        counterMetrics.start();
    }

    public void readFileData(String pathFile){
        try (BufferedReader br = new BufferedReader(new FileReader(pathFile))){
            String line;
            int countLine = 0;
            ObjectMapper om = new ObjectMapper();
            while ((line = br.readLine())!=null){
                countLine++;
                counter.inc();
                kafkaWriter.write(om.writeValueAsBytes(extractData(line,
                        String.valueOf(countLine)+new Date().getTime())));
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public GenericModel extractData(String line, String id) throws IOException {
        ObjectMapper om = new ObjectMapper();
        Map<String, Object> post = om.readValue(line, HashMap.class);
        return new GenericModel(id, GenericModel.TYPE_ZAMBA_CHATBOT, post);
    }

    public static void main(String[] args) {
        Properties props = new Configuration().toSubProperties("zamba_chatbot");
        ZambaChatbotExtractor ze = new ZambaChatbotExtractor(props);
        ze.readFileData("/home/kumin/Desktop/zbfb/user_profile.txt");
    }

}

