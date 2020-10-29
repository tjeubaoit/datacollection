package com.datacollection.core.extract.mongo;

import com.datacollection.common.config.Configuration;
import com.datacollection.core.extract.model.GenericModel;
import org.bson.Document;

/**
 * Created by caoquy on 09/02/2017.
 */
public class FbProfilePostExtractor extends MongoExtractor {

    public FbProfilePostExtractor(Configuration config) {
        super("fbprofile", config);
    }

    @Override
    protected GenericModel extractData(Document document) {
        String id = document.getObjectId("_id").toHexString();
        String type = GenericModel.TYPE_FB_PROFILE_POST;

        document.put("Content", document.getString("postContent"));
        document.put("_id", id);

        document.remove("content");
        document.remove("postContent");

        return new GenericModel(id, type, document);
    }
}
