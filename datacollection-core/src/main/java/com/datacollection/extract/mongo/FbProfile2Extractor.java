package com.datacollection.extract.mongo;

import com.datacollection.common.config.Configuration;
import com.datacollection.extract.model.GenericModel;
import org.bson.Document;

/**
 * Created by kumin on 24/11/2017.
 */
public class FbProfile2Extractor extends MongoExtractor {

    public FbProfile2Extractor(Configuration config) {
        super("fbprofile", config);
    }

    @Override
    protected GenericModel extractData(Document document) {
        String id = document.getObjectId("_id").toHexString();
        String type = GenericModel.TYPE_FB_PROFILE_NEW;

        document.put("_id", id);
        return new GenericModel(id, type, document);
    }
}
