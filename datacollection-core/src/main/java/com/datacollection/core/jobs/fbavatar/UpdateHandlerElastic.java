package com.datacollection.core.jobs.fbavatar;

import com.datacollection.core.collect.Constants;
import com.datacollection.core.collect.fbavt.FbAvatarService;
import com.datacollection.core.collect.model.Photo;
import com.datacollection.graphdb.GraphSession;
import com.datacollection.graphdb.Vertex;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UpdateHandlerElastic extends UpdateHandler {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<String> fbIds;
    private Vertex profile;
    private Client elasticClient;
    private String elasticIndex;
    private FbAvatarService fbAvatarService;

    public UpdateHandlerElastic(List<String> fbIds,
                         Vertex profile,
                         int version,
                         Client elasticClient,
                         String elasticIndex,
                         GraphSession session,
                         FbAvatarService fbAvatarService) {
        super(version, session);
        this.fbIds = fbIds;
        this.profile = profile;
        this.fbAvatarService = fbAvatarService;
        this.elasticClient = elasticClient;
        this.elasticIndex = elasticIndex;
    }

    @Override
    public void run() {
        try {
            for (String fbId : fbIds) {
                String avatarUrl = fbAvatarService.getAvatarUrl(fbId);
                if (avatarUrl == null) continue;
                Photo newPhoto = new Photo(avatarUrl, "domain", Constants.FACEBOOK,
                        "_ts", System.currentTimeMillis());
                String photoProfile = checkAvatarExist(avatarUrl);
                if (photoProfile != null) {
                    Vertex photoProfileVertex = Vertex.create(photoProfile, "profile");
                    updateAvatarExist(profile, photoProfileVertex, newPhoto);
                } else {
                    updateAvatarNotExist(profile, newPhoto);
                }
                break;
            }
        } catch (Exception ignore) {
            logger.warn(ignore.getMessage(), ignore);
        }
    }

    public String checkAvatarExist(String url) {
        SearchResponse sr = elasticClient.prepareSearch(elasticIndex)
                .setTypes("profiles")
                .setQuery(QueryBuilders.termQuery("photo.url", url))
                .setSize(1000)
                .execute()
                .actionGet();
        if (sr.getHits().getTotalHits() == 0) return null;
        SearchHit hit = sr.getHits().getAt(0);
        return hit.getId();
    }


}
