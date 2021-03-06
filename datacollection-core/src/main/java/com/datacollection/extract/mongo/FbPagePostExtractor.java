package com.datacollection.extract.mongo;

import com.datacollection.common.config.Configuration;
import com.datacollection.extract.model.GenericModel;
import org.bson.Document;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class FbPagePostExtractor extends MongoExtractor {

    public FbPagePostExtractor(Configuration config) {
        super("fbpage", config);
    }

    @Override
    protected GenericModel extractData(Document document) {
        String id = document.getObjectId("_id").toHexString();
        String type = GenericModel.TYPE_FB_FANPAGE_POST;

        document.put("Content", document.getString("Message"));
        document.put("_id", id);

        document.remove("Message");

        return new GenericModel(id, type, document);
    }
}
