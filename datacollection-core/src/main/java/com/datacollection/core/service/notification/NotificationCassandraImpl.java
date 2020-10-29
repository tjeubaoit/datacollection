package com.datacollection.core.service.notification;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.util.concurrent.ListenableFuture;
import com.datacollection.common.concurrency.FutureAdapter;
import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.IterableAdapter;
import com.datacollection.core.platform.cassandra.AbstractRepository;

import java.util.Collection;

/**
 * Created by kumin on 23/11/2017.
 */
public class NotificationCassandraImpl extends AbstractRepository implements NotificationService {

    private final PreparedStatement psInsert, psDelete;

    public NotificationCassandraImpl(Properties p) {
        super(p);
        this.psInsert = session.prepare("INSERT INTO messages (type, key, p, h) VALUES (?, ?, ?, ?)");
        this.psDelete = session.prepare("DELETE FROM messages USING TIMESTAMP ? WHERE type = ? AND key = ?");
    }

    @Override
    public ListenableFuture<Iterable<Message>> addMessages(Collection<Message> messages) {
        BatchStatement bs = new BatchStatement();
        for (Message msg : messages) {
            bs.add(psInsert.bind(
                    msg.getType(),
                    msg.getKey(),
                    msg.getProperties(), ""));
        }
        return FutureAdapter.from(session.executeAsync(bs), rs -> messages);
    }

    @Override
    public ListenableFuture<Message> removeMessage(Message msg) {
        return FutureAdapter.from(session.executeAsync(psDelete.bind(
                msg.getVersion() + 1,
                msg.getType(),
                msg.getKey())), rs -> msg);
    }

    @Override
    public Iterable<Message> getMessages(String type) {
        String query = "SELECT type, key, p, writetime(h) as ts FROM messages WHERE type = ?";
        ResultSet rs = session.execute(query, type);
        return new IterableAdapter<Row, Message>(rs) {
            @Override
            protected Message convert(Row row) {
                Message msg = new Message(row.getString("type"));
                msg.setKey(row.getString("key"));
                msg.setVersion(row.getLong("ts"));
                msg.putProperties(row.getMap("p", String.class, String.class));

                return msg;
            }
        };
    }
}
