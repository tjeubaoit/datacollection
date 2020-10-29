package com.datacollection.core.service.managejobs;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class JobDetail {

    public static final String STATUS_NEW = "new";
    public static final String STATUS_PROCESSING = "processing";
    public static final String STATUS_PROCESSED = "processed";

    public final String name;
    public String workerId;
    public Date startTime;
    public Date endTime;
    public String status;
    public final Map<String, Object> attributes = new HashMap<>();

    public JobDetail(String name) {
        this.name = name;
    }

    public boolean isNewJob() {
        return STATUS_NEW.equals(status);
    }

    public boolean isFinished() {
        return STATUS_PROCESSED.equals(status);
    }
}
