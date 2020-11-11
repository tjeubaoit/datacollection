package com.datacollection.jobs.fbavatar;

import com.datacollection.collect.Constants;
import com.datacollection.collect.fbavt.FbAvatarService;
import com.datacollection.collect.model.Photo;
import com.datacollection.graphdb.Direction;
import com.datacollection.graphdb.GraphSession;
import com.datacollection.graphdb.Vertex;
import com.datacollection.graphdb.VertexSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UpdateHandlerGraph extends UpdateHandler {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<String> fbIds;
    private Vertex profile;
    private static final String VERSION_KEY = "_v@avatarupdate";
    private GraphSession session;
    private FbAvatarService fbAvatarService;

    public UpdateHandlerGraph(List<String> fbIds,
                              Vertex profile,
                              int version,
                              GraphSession session,
                              FbAvatarService fbAvatarService) {
        super(version, session);
        this.fbIds = fbIds;
        this.profile = profile;
        this.session = session;
        this.fbAvatarService = fbAvatarService;
    }

    @Override
    public void run() {
        try {
            VertexSet facebooks = session.verticesByAdjVertexLabels(profile, Direction.OUT, "fb.com");
            for (Vertex facebook : facebooks) {
                String fbId = facebook.id();
                String avatarUrl = fbAvatarService.getAvatarUrl(fbId);
                if (avatarUrl == null) continue;
                Photo newPhoto = new Photo(avatarUrl, "domain", Constants.FACEBOOK,
                        "_ts", System.currentTimeMillis());
                //check photo exist in graph
                Optional<Vertex> photoOption = session.vertex(newPhoto.id(), "photo");
                if (photoOption.isPresent()) {
                    Vertex photoProfile = session.verticesByAdjVertexLabels(photoOption.get(),
                            Direction.IN, "profile")
                            .first();
                    System.out.println(profile + ":" + photoProfile);
                    updateAvatarExist(profile, photoProfile, newPhoto);
                } else {
                    updateAvatarNotExist(profile, newPhoto);
                }
                logger.info("Update Avatar Is Success!");
                break;
            }
        } catch (Throwable ignore) {

        }
    }


}
