package com.datacollection.core.collect;

import com.datacollection.common.config.Properties;
import com.datacollection.common.lifecycle.AbstractLifeCycle;
import com.datacollection.common.mb.MsgBrokerFactory;
import com.datacollection.common.mb.MsgBrokerReader;
import com.datacollection.common.mb.MsgHandler;
import com.datacollection.common.serialize.Deserializer;
import com.datacollection.common.serialize.Serialization;
import com.datacollection.common.utils.Reflects;
import com.datacollection.core.extract.model.GenericModel;
import com.datacollection.core.metric.Counter;
import com.datacollection.core.metric.CounterMetrics;
import com.datacollection.core.metric.MetricExporter;
import com.datacollection.core.metric.Sl4jPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract class cho các Collectors. Collector nạp các thuộc tính từ config,
 * quy định cách thức dữ liệu được đọc (từ Message Broker), khởi tạo Deserializer
 * để deserialize dữ liệu từ Message Broker, khởi tạo CollectService, metrics...
 * </p>
 * Các collector (SimpleCollector, WalCollector) kế thừa lại class này và quy
 * định cách thức dữ liệu thực sự được xử lý. Collector kế thừa từ AbstractLifeCycle
 * nên hoạt động theo các giai đoạn: initialize -> start -> process -> stop.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public abstract class Collector extends AbstractLifeCycle implements MsgHandler {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * Chứa các thuộc tính của ứng dụng
     */
    protected final Properties props;
    /**
     * Lưu trữ tổng số records đã được xử lý bởi Collector, dùng làm metric để thống kê
     */
    protected final Counter counter = new Counter();
    private CounterMetrics counterMetrics;
    private MetricExporter metricExporter;

    private Deserializer<GenericModel> deserializer;
    private CollectService service;
    private MsgBrokerReader msgBrokerReader;

    /**
     * Khởi tạo Collector từ tập các thuộc tính
     *
     * @param props các thuộc tính dùng để khởi tạo Collector
     */
    public Collector(Properties props) {
        this.props = props;
    }

    @Override
    protected void onInitialize() {
        // init message queue
        msgBrokerReader = createMsgBrokerReader(props);
        msgBrokerReader.addHandler(this);
        deserializer = Serialization.create(props.getProperty("mb.deserializer"),
                GenericModel.class).deserializer();

        // init main services
        service = new GraphCollectService(props);

        // init monitoring
        counterMetrics = new CounterMetrics(new Sl4jPublisher(), "default-metric-group",
                "collector", counter, 1000);
        metricExporter = new MetricExporter(props);
    }

    @Override
    public void onStart() {
        msgBrokerReader.start();
        counterMetrics.start();
        metricExporter.start();
    }

    @Override
    public void onStop() {
        msgBrokerReader.stop();
        counterMetrics.stop();
        metricExporter.stop();
//        service.close();
    }

    /**
     * Factory method tạo ra một object implement MsgBrokerReader (dùng để
     * đọc dữ liệu từ Message Broker) từ tập các thuộc tính. Method này đã tự
     * động được gọi trong onInitialize(), class con kế thừa lại class này
     * không nên tự gọi lại method này mà chỉ nên override lại nếu muốn thay
     * đổi cách tạo ra đối tượng MsgBrokerReader.
     *
     * @param props tập các thuộc tính
     * @return object implement MsgBrokerReader
     */
    protected MsgBrokerReader createMsgBrokerReader(Properties props) {
        MsgBrokerFactory factory = Reflects.newInstance(props.getProperty("mb.factory.class"));
        logger.info("MsgBrokerFactory class: " + factory.getClass().getName());

        MsgBrokerReader reader = factory.createReader();
        // Khởi tạo MsgBroker với tập các thuộc tính trước khi sử dụng
        reader.configure(props);
        return reader;
    }

    /**
     * @return Tập các thuộc tính
     */
    public Properties getProps() {
        return props;
    }

    /**
     * @return Lấy về object Counter, dùng để lưu tổng số records mà Collector xử lý được
     */
    public Counter getCounter() {
        return counter;
    }

    /**
     * @return Lấy về object CounterMetrics, dùng để quản lý toàn bộ metrics
     */
    public CounterMetrics getCounterMetrics() {
        return counterMetrics;
    }

    /**
     * @return Lấy về object Deserializer dùng để deserialize dữ liệu từ MessageBroker
     */
    public Deserializer<GenericModel> getDeserializer() {
        return deserializer;
    }

    /**
     * @return Lấy về object CollectService dùng để xử lý các records
     */
    public CollectService getService() {
        return service;
    }

    /**
     * @return Lấy về object MsgBrokerReader đã được tạo ra ở giai đoạn init
     */
    public MsgBrokerReader getMsgBrokerReader() {
        return msgBrokerReader;
    }
}
