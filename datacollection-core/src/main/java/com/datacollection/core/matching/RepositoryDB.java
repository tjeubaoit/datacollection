package com.datacollection.core.matching;

import java.util.Collection;
import java.util.Map;

/**
 * Created by kumin on 27/10/2017.
 */
public interface RepositoryDB {

    Collection<EntityLog> getListEntityLog(String type, String id, String source, int limitException);

    ProfileLog getProfileLog(String type, String uid);

    void addEntityProfile(String entityType, String entityValue, String relationship,
                          Map<String, Map<String, Object>> relationshipProps);
    void markSpamEntity(String entityType, String entityValue);

    boolean checkSpamEntity(String entityType, String entityValue);
}
