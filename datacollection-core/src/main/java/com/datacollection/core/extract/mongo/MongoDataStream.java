package com.datacollection.core.extract.mongo;

import com.mongodb.client.MongoCursor;
import com.datacollection.core.extract.DataStream;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.NoSuchElementException;

/**
 * Stream data from one Mongo collection
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class MongoDataStream implements DataStream<Document> {

    private final MongoFetcher fetcher;
    private MongoCursor<Document> cursor;
    private Object lastIndex;

    public MongoDataStream(MongoFetcher fetcher, Object fromIndex) {
        this.fetcher = fetcher;
        this.cursor = fetcher.fetchNextDocs(fromIndex);
    }

    @Override
    public boolean hasNext() {
        if (!cursor.hasNext()) {
            cursor.close();
            cursor = fetcher.fetchNextDocs(lastIndex);
        }
        return !checkPossibleLostData() && cursor.hasNext();
    }

    @Override
    public Document next() {
        if (hasNext()) {
            Document doc = cursor.next();
            lastIndex = fetcher.fetchIndex(doc);
            return doc;
        } else throw new NoSuchElementException();
    }

    @Override
    public void close() {
        this.cursor.close();
    }

    /**
     * <p>
     *     Nếu sử dụng field _id của Mongo collection để stream dữ liệu thì cần bảo đảm
     *     giá trị này luôn tăng. Tuy nhiên khi sử dụng _id với kiểu dữ liệu ObjectID
     *     tự tặng của Mongo, xảy ra trường hợp một số documents được write sau (so với
     *     last_id) nhưng lại có _id nhỏ hơn last_id nên các documents này không được
     *     đọc ở batch kế tiếp dẫn tới mất dữ liệu.
     * </p>
     * <p>
     *     Vấn đề này thường xảy ra với các documents được write đồng thời bởi các
     *     process hoặc machine khác nhau (khác process identifier và/hoặc machine
     *     identifier) trong cùng 1s. Lí do là lúc này các đối tượng ObjectID này sẽ có
     *     cùng timestamp và chỉ khác nhau giá trị bởi một counter tự tăng. Vì thế để
     *     ngăn chặn khả năng này xảy ra, ta chỉ cần bảo đảm sẽ chờ cho toàn bộ các quá
     *     trình write trong khoảng thời gian 1s được hoàn thành hết trước khi đọc.
     * </p>
     * <p>
     *     Để chắc chắn thì ở đây sẽ delay lại 60s, nghĩa là sẽ bỏ qua các documents
     *     mới được write trong khoảng 60s gần nhất cho tới lần đọc kế tiếp.
     * </p>
     * <p>
     *     Sub-class có thể override lại method này để cung cấp các rule check cụ thể
     *     hơn cho từng trường hợp.
     * </p>
     *
     * @return true: nếu có khả năng mất dữ liệu, quá trình đọc nên dừng lại và
     *          đợi cho tới lần đọc kế tiếp.
     *          false: nếu không có khả năng mất dữ liệu, quá trình đọc document
     *          kế tiếp là an toàn và nên tiếp tục.
     */
    protected boolean checkPossibleLostData() {
        // default only check if index is an ObjectId object
        if (lastIndex == null || !(lastIndex instanceof ObjectId)) return false;
        long timestamp = (long) ((ObjectId) lastIndex).getTimestamp() * 1000;
        return System.currentTimeMillis() - timestamp < 60000L;
    }
}
