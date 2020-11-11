package com.datacollection.jobs.syncprofile;

import com.datacollection.common.concurrency.AllInOneFuture;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.config.Properties;
import com.datacollection.common.io.FileHelper;
import com.datacollection.common.lifecycle.LoopableLifeCycle;
import com.datacollection.common.utils.Strings;
import com.datacollection.common.utils.ThreadPool;
import com.datacollection.common.utils.Threads;
import com.datacollection.common.utils.TimeKey;
import com.datacollection.common.utils.Utils;
import com.datacollection.graphdb.GraphDatabase;
import com.datacollection.metric.MetricExporter;
import com.datacollection.platform.elastic.ElasticBulkInsert;
import com.datacollection.service.managejobs.JobDetail;
import com.datacollection.service.managejobs.JobManager;
import com.datacollection.service.notification.Message;
import com.datacollection.service.notification.NotificationService;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class SyncProfileToEs extends LoopableLifeCycle {

    private static final int DEFAULT_BULK_SIZE = 100;
    private static final int DEFAULT_QUEUE_SIZE = 256;
    private static final long MIN_VALID_EPOCH = 1511488473000L;

    private Properties props;
    private int esBulkSize;
    private ElasticBulkInsert ebi;
    private ExecutorService executor;
    private SyncProfileService syncService;
    private NotificationService notificationService;
    private MetricExporter metricExporter;
    private JobManager jobManager;

    public SyncProfileToEs(Properties p) {
        super(p);
        this.props = p;
    }

    @Override
    protected void onInitialize() {
        esBulkSize = props.getIntProperty("elastic.bulk.size", DEFAULT_BULK_SIZE);
        ebi = new ElasticBulkInsert(props);

        jobManager = JobManager.create(props);
        syncService = new GraphService(GraphDatabase.open(props));
        notificationService = NotificationService.create(props);
        metricExporter = new MetricExporter(props);

        executor = ThreadPool.builder()
                .setCoreSize(props.getIntProperty("threadpool.core.size",
                        Runtime.getRuntime().availableProcessors()))
                .setQueueSize(props.getIntProperty("threadpool.queue.size", DEFAULT_QUEUE_SIZE))
                .setDaemon(true)
                .setNamePrefix("sync-es-worker")
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        metricExporter.start();
    }

    @Override
    protected void onLoop() throws Exception {
        String indexPath = props.getProperty("data.path") + "/syncprofile.index";
        String timeIndex = loadLastTimeIndex(indexPath);

        // go to next time index from last time index
        timeIndex = TimeKey.nextTimeKey(timeIndex);
        if (!TimeKey.satisfyTime(timeIndex)) {
            logger.info("No data to process, current time index: " + timeIndex);
            return;
        }

        // check if current time index (job) was process by an other worker
        JobDetail job = jobManager.getJob("syncprofile_" + timeIndex);
        if (!job.isNewJob()) {
            boolean ok = false;
            if (Utils.notEquals(Utils.getHostName(), job.workerId)) {
                logger.info(Strings.format("Time index %s was %s by %s, go to next index",
                        timeIndex, job.status, job.workerId));
                ok = true;
            } else if (job.isFinished()) {
                logger.info("Time index " + timeIndex + " processed, go to next index");
                ok = true;
            }

            if (ok) {
                saveIndex(indexPath, timeIndex);
                return;
            }
        }

        // update job detail for avoid other workers re-process current time index
        setJobProcessing(job);
        logger.info("Start process data at time index: " + timeIndex);

        List<Future<?>> futures = new LinkedList<>();
        for (Message message : notificationService.getMessages("profile_" + timeIndex)) {
            String uid = message.getProperty("uid");
            if (!NumberUtils.isParsable(uid)) continue;
            while (isNotCanceled()) {
                try {
                    futures.add(executor.submit(() -> handleSyncProfile(uid)));
                    break;
                } catch (RejectedExecutionException e) {
                    //logger.debug("Worker pool is full, sleep 10ms");
                    Threads.sleep(1);
                }
            }

            // Stop immediately if canceled by user
            if (isCanceled()) throw new RuntimeException("Job was canceled by user");
        }

        // Wait for all current tasks finished before go to next time index
        AllInOneFuture.from(futures).get();
        ebi.flush();
        saveIndex(indexPath, timeIndex);

        // update job detail as processed in job manager
        setJobDone(job, futures.size());
        logger.info(Strings.format("Done process data at %s, total %d records",
                timeIndex, futures.size()));
    }

    @Override
    protected void onStop() {
        logger.info("Waiting for all workers stopped...");
        Threads.stopThreadPool(executor, 5, TimeUnit.SECONDS);

        ebi.close();
        syncService.close();
        notificationService.close();
        metricExporter.stop();
    }

    private void handleSyncProfile(String uid) {
        while (true) {
            try {
                SyncHandler handler = new SyncHandler(uid, syncService, ebi, esBulkSize);
                handler.handleSyncProfile(uid);
                break;
            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().contains("MapperParsingException")) break;
                if (isCanceled()) throw new RuntimeException("Job was canceled by user");

                logger.error("Sync profile " + uid + " failed", e);
                Threads.sleep(1, TimeUnit.SECONDS);
            }
        }
    }

    private void setJobProcessing(JobDetail job) {
        job.workerId = Utils.getHostName();
        job.status = JobDetail.STATUS_PROCESSING;
        job.startTime = new Date();
        jobManager.updateJob(job);
    }

    private void setJobDone(JobDetail job, int total) {
        job.endTime = new Date();
        job.status = JobDetail.STATUS_PROCESSED;
        job.attributes.put("total_record", total);
        jobManager.updateJob(job);
    }

    static void saveIndex(String indexPath, String timeIndex) {
        FileHelper.checkCreateNewFile(indexPath);
        FileHelper.unsafeWrite(indexPath, timeIndex, true);
    }

    static String loadLastTimeIndex(String indexPath) {
        try {
            return FileHelper.readLastLine(new File(indexPath)).trim();
        } catch (Throwable t) {
            Date date = new Date();
            date.setTime(MIN_VALID_EPOCH);
            return TimeKey.fromDate(date);
        }
    }

    public static void main(String[] args) {
        Properties p = new Configuration().toSubProperties("sync_profile");
        SyncProfileToEs processor = new SyncProfileToEs(p);
        processor.start();
    }
}
