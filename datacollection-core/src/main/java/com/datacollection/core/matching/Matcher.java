package com.datacollection.core.matching;

import com.datacollection.core.collect.Constants;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.config.Properties;
import com.datacollection.common.lifecycle.LoopableLifeCycle;
import com.datacollection.common.tasks.TaskManager;
import com.datacollection.common.utils.Strings;
import com.datacollection.common.utils.ThreadPool;
import com.datacollection.common.utils.Threads;
import com.datacollection.core.platform.hystrix.SyncCommand;
import com.datacollection.core.matching.algorithms.MatchingAlgorithm;
import com.datacollection.core.matching.algorithms.PhoneEmailMatching;
import com.datacollection.core.service.notification.Message;
import com.datacollection.core.service.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by kumin on 27/10/2017.
 */
public class Matcher extends LoopableLifeCycle {

    private static Logger logger = LoggerFactory.getLogger(Matcher.class);
    private int SPAM_THRESHOLD_LEVEL1;
    private int SPAM_THRESHOLD_LEVEL2;
    private int SPAM_THRESHOLD_UNIQUE_UID;
    private int SLOW_THRESHOLD;

    private RepositoryDB repositoryDB;
    private NotificationService notificationService;
    private TaskManager taskManager;
    private MatchingAlgorithm algorithm;
    private ExecutorService slowExecutor;

    private int timeMatchingInterval;
    private final String type;
    private final Properties p;
    private final boolean circuitBreakerEnabled;

    public Matcher(Properties p, String type, MatchingAlgorithm algorithm) {
        this.p = p;
        this.type = type;
        this.algorithm = algorithm;
        this.timeMatchingInterval = p.getIntProperty("time_matching_interval", 7200);
        this.circuitBreakerEnabled = p.getBoolProperty("circuit.breaker.enabled", false);

        SPAM_THRESHOLD_LEVEL1 = p.getIntProperty("spam.threshold.level1", 1000);
        SPAM_THRESHOLD_LEVEL2 = p.getIntProperty("spam.threshold.level2", 2000);

        SPAM_THRESHOLD_UNIQUE_UID = p.getIntProperty("spam.threshold.unique.uid", 3);
        SLOW_THRESHOLD = p.getIntProperty("slow.threshold", 500);
    }

    @Override
    protected void onInitialize() {
        int totalThread = p.getIntProperty("total.thread", Runtime.getRuntime().availableProcessors());
        double ratioThread = p.getDoubleProperty("ratio.thread", 0.5);

        int fastThread = (int) Math.round(totalThread * ratioThread);
        ExecutorService fastExecutor = ThreadPool.builder()
                .setCoreSize(fastThread)
                .setQueueSize(100)
                .setDaemon(true)
                .setNamePrefix("fast-matching-worker")
                .build();

        slowExecutor = ThreadPool.builder()
                .setCoreSize(totalThread - fastThread)
                .setQueueSize(100)
                .setDaemon(true)
                .setNamePrefix("slow-matching-worker")
                .build();

        taskManager = new TaskManager(p, fastExecutor);
        this.repositoryDB = new GraphRepository(p);
        this.notificationService = NotificationService.create(p);

    }

    @Override
    protected void onLoop() {
        AtomicLong total = new AtomicLong(0);
        AtomicLong succeeded = new AtomicLong();
        AtomicLong failed = new AtomicLong();

        long now = new Date().getTime();
        for (Message message : notificationService.getMessages(type)) {
            if (now - message.getVersion() / 1000 < timeMatchingInterval) continue;
            while (isNotCanceled()) {
                try {
                    executeInFastPool(() -> {
                        try {
                            total.incrementAndGet();
                            handleMessage(message);
                            succeeded.incrementAndGet();
                        } catch (Throwable t) {
                            failed.incrementAndGet();
                        } finally {
                            total.decrementAndGet();
                        }
                    });
                    break;
                } catch (RejectedExecutionException e) {
                    Threads.sleep(1);
                }
            }
        }

        logger.info(Strings.format("Total %d messages handled, %d succeeded, %d failed, %d processing",
                total.get(), succeeded.get(), failed.get(), (total.get() - succeeded.get() - failed.get())));
    }

    @Override
    protected void onStop() {
        Threads.stopThreadPool(taskManager.executor());
        Threads.stopThreadPool(slowExecutor);
//        notificationService.close();
    }

    public void handleMessage(Message message) {
        String type = message.getType();
        String value = message.getProperty("value");
        String source = message.getProperty("source");

        if (repositoryDB.checkSpamEntity(type, value)) {
            this.setMessageHandled(message);
            return;
        }

        if (message.getProperty("uid") != null) {
            this.addMessageRematching(message);
        }


        try {
            Collection<EntityLog> entityLogs = repositoryDB.getListEntityLog(type, value, source, SLOW_THRESHOLD);
            matchEntity(message, entityLogs, false);
        } catch (SpamEntityException e) {
            executeInSlowPool(() -> matchEntity(message, Collections.emptySet(), true));
            logger.warn("Detect entity has many logs, handle it in slow pool: " + message);
        }
    }

    private void matchEntity(Message message, Collection<EntityLog> entityLogs, boolean isSlow) {
        String type = message.getType();
        String value = message.getProperty("value");

        Collection<String> uids = new HashSet<>();
        if (isSlow) {
            if (isSpam(message, entityLogs, uids)) {
                repositoryDB.markSpamEntity(type, value);
                this.setMessageHandled(message);
                return;
            }
        } else {

            if (entityLogs.size() == 0) {
                this.setMessageHandled(message);
                return;
            }
            for (EntityLog entitylog : entityLogs) {
                uids.add(entitylog.uid);
            }
        }

        Map<String, ProfileLog> profileLogs = new HashMap<>();
        for (String uid : uids) {
            ProfileLog profile = repositoryDB.getProfileLog(type, uid);
            if (profile.entityLogs.isEmpty()) continue;
            profileLogs.put(profile.uid, profile);
        }
        Map<String, Double> scoreMatchingUids = algorithm.computeScore(profileLogs.values(), value);
        List<String> uidWithMaxScore = algorithm.getUidMaxScore(scoreMatchingUids);
        Double scoreMax = scoreMatchingUids.get(uidWithMaxScore.get(0));

        if (uidWithMaxScore.size() == 1) {
            logger.info(Thread.currentThread().getName() + " - " + value
                    + " was matched with " + uidWithMaxScore.get(0)
                    + " score:" + String.format("%6f", scoreMax));
        }

        // update matching results as entity relationships
        Map<String, Map<String, Object>> props = new HashMap<>();
        for (String uid : scoreMatchingUids.keySet()) {
            Map<String, Object> prop = new HashMap<>();
            prop.put("score", String.format("%6f", scoreMatchingUids.get(uid)));
            prop.put("max", scoreMax);
            props.put(uid, prop);
        }
        repositoryDB.addEntityProfile(type, value, Constants.HIDDEN_PREFIX + type, props);

        // add a message to notification service center to trigger a request that
        // some relationship of those profiles have been changed and need to be synced
        notifyProfilesNeedToBeSynced(scoreMatchingUids.keySet());
        this.setMessageHandled(message);
    }

    private void notifyProfilesNeedToBeSynced(Collection<String> profileUids) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH");
        profileUids.forEach(uid -> {
            Message msgProfile = new Message("profile_" + df.format(new Date()), String.valueOf(uid));
            msgProfile.putProperty("uid", String.valueOf(uid));
            notificationService.addMessage(msgProfile);
        });
    }

    private void executeInFastPool(Runnable task) {
        if (circuitBreakerEnabled) {
            new SyncCommand<>("matching", "FastMatching", () -> {
                task.run();
                return null;
            }).queue();
        } else {
            taskManager.submit(task);
        }
    }

    private void executeInSlowPool(Runnable task) {
        try {
            if (circuitBreakerEnabled) {
                new SyncCommand<>("matching", "SlowMatching", () -> {
                    task.run();
                    return null;
                }).queue();
            } else {
                slowExecutor.submit(task);
            }
        } catch (RejectedExecutionException ignored) {
        }
    }

    private boolean isSpam(Message message, Collection<EntityLog> entityLogs, Collection<String> uids) {
        try {
            entityLogs = repositoryDB.getListEntityLog(message.getType(), message.getProperty("value")
                    , message.getProperty("source"), SPAM_THRESHOLD_LEVEL2);
        } catch (SpamEntityException e) {
            return true;
        }
        entityLogs.forEach(log -> uids.add(log.uid));
        return (uids.size() >= SPAM_THRESHOLD_UNIQUE_UID) && (entityLogs.size() >= SPAM_THRESHOLD_LEVEL1);
    }

    /**
     * Set message is handled, remove it from notification service center
     *
     * @param message message to be removed
     */
    private void setMessageHandled(Message message) {
        notificationService.removeMessage(message);
    }

    /**
     * @param message
     */
    private void addMessageRematching(Message message) {
        ProfileLog profileLog = repositoryDB.getProfileLog(message.getType(), message.getProperty("uid"));
        profileLog.entityLogs.keySet().forEach(entity -> {
            if (!entity.equals(message.getProperty("value"))) {
                Message msg = new Message(message.getType(), message.getProperty("source"), entity);
                msg.putProperty("value", entity);
                msg.putProperty("source", message.getProperty("source"));
                msg.putProperty("ts", String.valueOf(new Date().getTime()));
                notificationService.addMessage(msg);
            }
        });
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Help:");
            System.out.println("Args: email");
            System.out.println("email/phone : matching for email or phone");
            return;
        }

//        String type = "phone";
        String type = args[0];
        Properties p = new Configuration().toSubProperties("matching", type + "_matching");

        MatchingAlgorithm algorithm = new PhoneEmailMatching();
        Matcher matcher = new Matcher(p, type, algorithm);
        matcher.start();
    }
}
