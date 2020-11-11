package com.datacollection.extract.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.datacollection.common.config.Configuration;
import com.datacollection.extract.DataStream;
import com.datacollection.extract.model.GenericModel;
import org.bson.Document;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ForumArticleExtractor extends MongoExtractor {

    static final long MIN_EPOCH = 1262278800000L;
    static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ForumArticleExtractor(Configuration config) {
        super("forum", config);
    }

    @Override
    protected GenericModel extractData(Document document) {
        String id = document.getString("_id");
        String type = GenericModel.TYPE_FORUM_ARTICLE;

        return new GenericModel(id, type, document);
    }

    @Override
    protected DataStream<Document> openDataStream() {
        String strIndex = loadIndex();
        Date lastIndex;
        try {
            lastIndex = df.parse(strIndex);
        } catch (Exception e) {
            lastIndex = new Date(MIN_EPOCH);
        }
        logger.info("Last index: " + df.format(lastIndex));

        return new MongoDataStream(new MongoFetcher() {
            @Override
            public MongoCursor<Document> fetchNextDocs(Object fromIndex) {
                return database.getCollection(collection)
                        .find(Filters.gt("GetDate", fromIndex))
                        .sort(new BasicDBObject("GetDate", 1))
                        .limit(batchSize)
                        .iterator();
            }

            @Override
            public Object fetchIndex(Document doc) {
                return doc.getDate("GetDate");
            }
        }, lastIndex);
    }

    @Override
    protected void onRecordProcessed(GenericModel model, long queueOrder, Object attachment) {
        Document doc = (Document) attachment;
        storeIndex(df.format(doc.getDate("GetDate")), queueOrder);
    }
}
