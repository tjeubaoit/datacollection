package com.datacollection.extract.mongo;

import com.mongodb.client.MongoCursor;
import org.bson.Document;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface MongoFetcher {

    MongoCursor<Document> fetchNextDocs(Object fromIndex);

    Object fetchIndex(Document doc);
}
