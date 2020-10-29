package com.datacollection.core.service.managejobs;

import com.datacollection.common.config.Properties;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface JobManager {

    JobDetail getJob(String name);

    void updateJob(JobDetail job);

    static JobManager create(Properties p) {
        return new JobManagerImpl(p);
    }
}
