package com.datacollection.core.collect.idgen;

import com.datacollection.common.cache.Cache;
import com.datacollection.common.cache.LRUCache;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.Strings;
import com.datacollection.core.platform.thrift.ThriftServer;
import org.apache.thrift.TException;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thrift.collect.IdGenerator;

import java.util.List;

/**
 * Demo Thrift server sử dụng cho service IdGenerator. Lưu ý
 * chỉ sử dụng cho môi trường dev vì phiên bản production sẽ sử
 * dụng Thrift server được implement bằng Go.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class SafeIdGenServer {

    public static final int DEFAULT_MAX_CACHE_SIZE = 10 * 1000 * 1000;
    private static final Logger logger = LoggerFactory.getLogger(SafeIdGenServer.class);

    public static void main(String[] args) {
        Configuration conf = new Configuration();
        ThriftServer server = new ThriftServer(conf, initThriftProcessor(conf));
        server.start();
    }

    static TProcessor initThriftProcessor(Properties props) {
        int maxCacheSize = props.getIntProperty("idgen.max.cache.size", DEFAULT_MAX_CACHE_SIZE);
        TMultiplexedProcessor processor = new TMultiplexedProcessor();
        processor.registerProcessor("IdGenerator", new IdGenerator.Processor<>(
                new IdGeneratorHandler(maxCacheSize)));

        return processor;
    }

    private static class IdGeneratorHandler implements IdGenerator.Iface {

        private final Cache<String, Long> cache;

        IdGeneratorHandler(int maxSize) {
            this.cache = new LRUCache<>(maxSize);
        }

        @Override
        public long generate(List<String> seeds, long def_val) throws TException {
            synchronized (this) {
                long finalVal = def_val;
                for (String seed : seeds) {
                    Long val = cache.get(seed);
                    if (val != null) {
                        finalVal = val;
                        break;
                    }
                }

                logger.info(Strings.format("Seeds: %s, val: %d", seeds, finalVal));
                for (String seed : seeds) {
                    cache.put(seed, finalVal);
                }

                return finalVal;
            }
        }
    }
}
