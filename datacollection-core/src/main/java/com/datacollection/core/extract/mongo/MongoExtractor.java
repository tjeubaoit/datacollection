package com.datacollection.core.extract.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.datacollection.common.config.Configuration;
import com.datacollection.core.extract.DataStream;
import com.datacollection.core.extract.StreamExtractor;
import com.datacollection.core.platform.mongo.MongoClientProvider;
import com.datacollection.core.platform.mongo.MongoConfig;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Stream mongo documents by _id (ObjectId)
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public abstract class MongoExtractor extends StreamExtractor<Document> {

    protected static final String DEFAULT_OBJECT_ID_STR = "0000ef100000000000000000";
    protected static final String KEY_BATCH_SIZE = "mongo.batch.size";
    protected static final String KEY_COLLECTION = "mongo.collection";

    protected final MongoConfig mongoConfig;
    protected final MongoDatabase database;
    protected final String collection;
    protected final int batchSize;

    public MongoExtractor(String group, Configuration config) {
        super(group, config);
        this.mongoConfig = new MongoConfig(props);
        this.database = MongoClientProvider.getOrCreate(group, mongoConfig)
                .getDatabase(mongoConfig.getDatabaseName());
        this.collection = props.getProperty(KEY_COLLECTION);
        this.batchSize = props.getIntProperty(KEY_BATCH_SIZE, 500);
    }

    @Override
    protected DataStream<Document> openDataStream() {
        Object lastIndex;
        String index = loadIndex(DEFAULT_OBJECT_ID_STR);
        try {
            lastIndex = new ObjectId(index);
        } catch (Exception e) {
            lastIndex = index;
        }
        logger.info("Last index: " + lastIndex);

        return new MongoDataStream(new MongoFetcher() {
            @Override
            public MongoCursor<Document> fetchNextDocs(Object fromIndex) {
                return database.getCollection(collection)
                        .find(Filters.gt("_id", fromIndex))
                        .sort(new BasicDBObject("_id", 1))
                        .limit(batchSize)
                        .iterator();
            }

            @Override
            public Object fetchIndex(Document doc) {
                return doc.get("_id");
            }
        }, lastIndex);
    }
}
