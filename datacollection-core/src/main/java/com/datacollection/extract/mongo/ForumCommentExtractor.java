package com.datacollection.extract.mongo;

import com.datacollection.common.config.Configuration;
import com.datacollection.extract.model.GenericModel;
import org.bson.Document;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ForumCommentExtractor extends MongoExtractor {

    public ForumCommentExtractor(Configuration config) {
        super("forum", config);
    }

    @Override
    protected GenericModel extractData(Document document) {
        String id = document.getObjectId("_id").toHexString();
        String type = GenericModel.TYPE_FORUM_COMMENT;

        document.put("_id", id);
        document.put("Content", document.getString("Comment"));
        document.remove("Comment");

        return new GenericModel(id, type, document);
    }
}
