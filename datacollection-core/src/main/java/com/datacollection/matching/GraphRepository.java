package com.datacollection.matching;

import com.datacollection.collect.Constants;
import com.datacollection.common.config.Properties;
import com.datacollection.graphdb.Direction;
import com.datacollection.graphdb.Edge;
import com.datacollection.graphdb.GraphDatabase;
import com.datacollection.graphdb.GraphSession;
import com.datacollection.graphdb.TagManager;
import com.datacollection.graphdb.Vertex;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by kumin on 10/11/2017.
 */
public class GraphRepository implements RepositoryDB {

    private GraphSession session;
    private TagManager tagManager;

    public GraphRepository(Properties p) {
        session = GraphDatabase.open(p);
        tagManager = new TagManager(session);
        tagManager.createTags("spam");
    }


    @Override
    public Collection<EntityLog> getListEntityLog(String type, String value, String source, int limitException) {
        Collection<EntityLog> entityLogs = new HashSet<>();
        Vertex vertex = session.vertex(value, type).get();

        Map<String, String> logs = (Map<String, String>) vertex.properties();
        int limitCount = 0;
        for (String key : logs.keySet()) {
            if(!key.contains(source)) continue;
            limitCount++;
            if (limitCount >= limitException)
                throw new SpamEntityException("Number of logs of Entity is more than limit " + limitException);
            EntityLog entitylog = new EntityLog();
            entitylog.uid = logs.get(key);
            entityLogs.add(entitylog);
        }

        return entityLogs;
    }

    @Override
    public ProfileLog getProfileLog(String type, String uid) {
        ProfileLog profileLog = new ProfileLog();
        profileLog.uid = uid;

        Vertex profile = Vertex.create(String.valueOf(uid), Constants.PROFILE);
        Iterable<Edge> edgesHiddenType = session.edges(profile, Direction.OUT,
                Constants.HIDDEN_PREFIX + type);
        for (Edge edge : edgesHiddenType) {
            Map<String, ?> properties = edge.properties();
            int frequency = 0;
            for (String key : properties.keySet()) {
                if (key.startsWith(Constants.LOG + Constants.PART_DELIMITER)) {
                    frequency++;
                }
            }

            profileLog.entityLogs.put(edge.inVertex().id(), frequency==0?1:frequency);
        }

        return profileLog;
    }

    @Override
    public void addEntityProfile(String entityType, String entityValue, String relationship,
                                 Map<String, Map<String, Object>> relationshipProps) {
        Vertex entityVertex = Vertex.create(entityValue, entityType);
        relationshipProps.forEach((uid, props) -> {
            try {
                Vertex profileVertex = Vertex.create(uid, Constants.PROFILE);
                session.addEdge(relationship, profileVertex, entityVertex, props).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void markSpamEntity(String entityType, String entityValue) {
        tagManager.addTags(Vertex.create(entityValue, entityType,
                Collections.singletonMap("score", -1)), "spam");
    }

    @Override
    public boolean checkSpamEntity(String entityType, String entityValue) {
        for (String tag : tagManager.getTags(Vertex.create(entityValue, entityType))) {
            if (tag.equals("spam")) return true;
        }
        return false;
    }
}
