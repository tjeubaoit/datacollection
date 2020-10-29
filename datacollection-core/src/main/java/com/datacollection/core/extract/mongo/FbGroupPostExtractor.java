package com.datacollection.core.extract.mongo;

import com.datacollection.common.config.Configuration;
import com.datacollection.core.extract.model.GenericModel;
import org.bson.Document;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class FbGroupPostExtractor extends MongoExtractor {

    public FbGroupPostExtractor(Configuration config) {
        super("fbgroup", config);
    }

    @Override
    protected GenericModel extractData(Document document) {
        String id = document.getObjectId("_id").toHexString();
        String type = GenericModel.TYPE_FB_GROUP_POST;

        document.put("Content", document.getString("Message"));
        document.put("_id", id);

        document.remove("Message");

        return new GenericModel(id, type, document);
    }
}
