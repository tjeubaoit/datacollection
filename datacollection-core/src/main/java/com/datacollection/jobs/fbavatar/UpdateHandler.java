package com.datacollection.jobs.fbavatar;

import com.datacollection.collect.model.Photo;
import com.datacollection.graphdb.Edge;
import com.datacollection.graphdb.GraphSession;
import com.datacollection.graphdb.Versions;
import com.datacollection.graphdb.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class UpdateHandler implements Runnable{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String VERSION_KEY = "_v@avatarupdate";
    private int version;
    private GraphSession session;

    public UpdateHandler(int version, GraphSession session){
        this.version = version;
        this.session = session;
    }

    public void updateAvatarExist(Vertex profile, Vertex photoProfile, Photo newPhoto) {
        if (profile.id().equals(photoProfile.id())) {
            session.addVertex(newPhoto.id(), newPhoto.label(), newPhoto.properties());
        } else {
            //link two profile
            Edge siblingEdge1 = Edge.create("duplicate", profile, photoProfile);
            Edge siblingEdge2 = Edge.create("duplicate", photoProfile, profile);
            Versions.setVersion(VERSION_KEY, version, siblingEdge1);
            session.addEdge(siblingEdge1);
            session.addEdge(siblingEdge2);
        }
    }

    public void updateAvatarNotExist(Vertex profile, Photo newPhoto) {
//        logger.warn("Avatar have changed!");
        //create new Photo vertex and connect to profile
        Vertex newPhotoVertex = Vertex.create(newPhoto.id(), newPhoto.label(), newPhoto.properties());
        session.addVertex(newPhotoVertex);
        Edge photoEdge = Edge.create("photo", profile, newPhotoVertex);
        Versions.setVersion(VERSION_KEY, version, photoEdge);
        Edge photoProfileEdge = Edge.create("profile", newPhotoVertex, profile);
        Versions.setVersion(VERSION_KEY, version, photoProfileEdge);

        session.addEdge(photoEdge);
        session.addEdge(photoProfileEdge);
    }
}
