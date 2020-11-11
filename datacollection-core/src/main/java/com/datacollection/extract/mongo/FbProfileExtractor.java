package com.datacollection.extract.mongo;

import com.datacollection.common.config.Configuration;
import com.datacollection.extract.model.GenericModel;
import org.bson.Document;

/**
 * Created by kumin on 23/05/2017.
 */
public class FbProfileExtractor extends MongoExtractor {

    public FbProfileExtractor(Configuration config) {
        super("fbprofile", config);
    }

    @Override
    protected GenericModel extractData(Document document) {
        String id = document.getObjectId("_id").toHexString();
        String type = GenericModel.TYPE_FB_PROFILE;

        document.put("_id", id);

        document.remove("fetchFriends");
        document.remove("isBackUp");
        document.remove("text");

        return new GenericModel(id, type, document);
    }
}
