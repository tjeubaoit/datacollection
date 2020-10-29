package com.datacollection.core.metric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Publish metrics qua log sử dụng Sl4j
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class Sl4jPublisher implements MetricPublisher {

    public static final int LEVEL_DEBUG = 2;
    public static final int LEVEL_INFO = 3;
    public static final int LEVEL_WARN = 4;
    public static final int LEVEL_ERROR = 5;

    private final Map<String, Logger> loggerMap = new HashMap<>();
    private final LogPrinter printer;

    public Sl4jPublisher(int level) {
        this.printer = create(level);
    }

    public Sl4jPublisher() {
        this(LEVEL_INFO);
    }

    @Override
    public void addMetric(Metric metric) {
        printer.print(findLogger(metric.group()), metric.toString());
    }

    private Logger findLogger(String group) {
        Logger logger = loggerMap.get(group);
        if (logger == null) {
            logger = LoggerFactory.getLogger(group);
            loggerMap.put(group, logger);
        }
        return logger;
    }

    private static LogPrinter create(int level) {
        switch (level) {
            case LEVEL_DEBUG:
                return (logger, msg, args) -> logger.debug(msg, args);
            case LEVEL_WARN:
                return (logger, msg, args) -> logger.warn(msg, args);
            case LEVEL_ERROR:
                return (logger, msg, args) -> logger.error(msg, args);
            default:
                return (logger, msg, args) -> logger.info(msg, args);
        }
    }

    private interface LogPrinter {
        void print(Logger logger, String msg, Object... args);
    }
}
