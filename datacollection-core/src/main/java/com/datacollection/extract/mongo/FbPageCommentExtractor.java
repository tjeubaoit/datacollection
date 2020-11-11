package com.datacollection.extract.mongo;

import com.mongodb.BasicDBObject;
import com.datacollection.common.config.Configuration;
import com.datacollection.extract.model.GenericModel;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class FbPageCommentExtractor extends MongoExtractor {

    public FbPageCommentExtractor(Configuration config) {
        super("fbpage", config);
    }

    @Override
    protected GenericModel extractData(Document document) {
        String id = document.getObjectId("_id").toHexString();
        String type = GenericModel.TYPE_FB_FANPAGE_COMMENT;

        String postFbId = document.getString("PostFbId");
        if (postFbId == null) {
            ObjectId postId = document.getObjectId("PostId");
            if (postId != null) {
                Document postDoc = database.getCollection("posts")
                        .find(new BasicDBObject("_id", postId))
                        .first();
                if (postDoc != null) {
                    document.put("FromId", postDoc.getString("FromId"));
                    document.put("FromName", postDoc.getString("FromName"));
                    document.put("ToId", postDoc.getString("ToId"));
                    document.put("ToName", postDoc.getString("ToName"));
                    document.put("Type", postDoc.getString("Type"));
                    document.put("PostFbId", postDoc.getString("PostFbId"));
                    document.put("PageFbId", postDoc.getString("PageFbId"));
                }
            }
        }

        document.put("Content", document.getString("Message"));
        document.put("_id", id);

        document.remove("Message");

        return new GenericModel(id, type, document);
    }
}
