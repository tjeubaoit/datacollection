package com.datacollection.jobs.syncprofile;

import com.datacollection.common.utils.DateTimes;
import com.datacollection.platform.hystrix.SyncCommand;
import com.datacollection.platform.elastic.ElasticBulkInsert;
import org.elasticsearch.action.bulk.BulkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
class SyncHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SyncHandler.class);
    private final SyncProfileService syncService;
    private final ElasticBulkInsert ebi;
    private final int esBulkSize;
    private final String uid;

    public SyncHandler(String uid, SyncProfileService syncService,
                       ElasticBulkInsert ebi, int esBulkSize) {
        this.uid = uid;
        this.syncService = syncService;
        this.ebi = ebi;
        this.esBulkSize = esBulkSize;
    }

    @Override
    public void run() {
        try {
            handleSyncProfile(uid);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("MapperParsingException")) return;
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    void handleSyncProfile(String uid) {
//        Map<String, Object> source = syncService.findProfileByUidAsMap(uid);
        Map<String, Object> source = new SyncCommand<>("syncprofile", "GraphTraversal",
                () -> syncService.findProfileByUidAsMap(uid)).execute();

        if (source.containsKey("birthday")) {
            Set<Map<String, Object>> set = (Set<Map<String, Object>>) source.get("birthday");
            for (Map<String, Object> bd : set) {
                String id = bd.get("id").toString();
                if (DateTimes.safeParse(id, "yyyy-MM-dd") == null) {
                    source.remove("birthday");
                    break;
                }
            }
        }

        ebi.addRequest("profiles", uid, source);
        if (ebi.bulkSize() > esBulkSize) {
            BulkResponse response = ebi.submitBulk();
            logger.info(Thread.currentThread().getName() + " - Submit bulk took "
                    + response.getTook().getSecondsFrac());
        }
    }
}
