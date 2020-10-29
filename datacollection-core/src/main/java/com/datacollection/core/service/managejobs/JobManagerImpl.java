package com.datacollection.core.service.managejobs;

import com.datacollection.common.config.Configuration;
import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.DateTimes;
import com.datacollection.common.utils.Maps;
import com.datacollection.core.platform.elastic.ElasticClientProvider;
import com.datacollection.core.platform.elastic.ElasticConfig;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
class JobManagerImpl implements JobManager {

    static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    static final String INDEX_TYPE = "jobs";

    private final Client client;
    private final String esIndex;

    JobManagerImpl(Properties p) {
        ElasticConfig esConfig = new ElasticConfig(p.toSubProperties("job_manager"));
        this.client = ElasticClientProvider.getDefault(esConfig);
        this.esIndex = esConfig.getElasticIndex();
    }

    @SuppressWarnings("unchecked")
    @Override
    public JobDetail getJob(String name) {
        GetResponse resp = client.prepareGet(esIndex, INDEX_TYPE, name)
                .execute().actionGet();
        Map<String, Object> source = resp.getSource();

        JobDetail job = new JobDetail(name);
        if (source != null) {
            job.status = source.getOrDefault("status", JobDetail.STATUS_NEW).toString();
            job.workerId = Maps.getOrNull(source, "worker");
            job.startTime = DateTimes.safeParse(Maps.getOrNull(source, "start_time"), DATE_TIME_FORMAT);
            job.endTime = DateTimes.safeParse(Maps.getOrNull(source, "end_time"), DATE_TIME_FORMAT);
        } else {
            job.status = JobDetail.STATUS_NEW;
        }
        return job;
    }

    @Override
    public void updateJob(JobDetail job) {
        Map<String, Object> source = new HashMap<>();
        source.put("status", job.status);
        source.put("worker", job.workerId);
        source.put("start_time", job.startTime);
        source.put("end_time", job.endTime);
        source.putAll(job.attributes);

        client.prepareIndex(esIndex, INDEX_TYPE, job.name)
                .setSource(source)
                .execute().actionGet();
        client.admin().indices().prepareRefresh(esIndex).execute().actionGet();
    }

    public static void main(String[] args) throws Exception {
        JobManager jobManager = new JobManagerImpl(new Configuration());
        for (int i = 0; i <= 12; i++) {
            String hour = i < 10 ? "0" + i : String.valueOf(i);
            JobDetail job = new JobDetail("syncprofile_2017-12-23-" + hour);
            job.status = JobDetail.STATUS_PROCESSED;
            job.workerId = "SVR765B";
            jobManager.updateJob(job);
        }
    }
}
