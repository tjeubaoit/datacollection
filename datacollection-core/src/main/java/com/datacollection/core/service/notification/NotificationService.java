package com.datacollection.core.service.notification;

import com.google.common.util.concurrent.ListenableFuture;
import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.Reflects;

import java.io.Closeable;
import java.io.Flushable;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by kumin on 23/11/2017.
 */
public interface NotificationService extends Closeable, Flushable {

    ListenableFuture<Iterable<Message>> addMessages(Collection<Message> messages);

    default ListenableFuture<Iterable<Message>> addMessage(Message msg) {
        return addMessages(Collections.singleton(msg));
    }

    ListenableFuture<Message> removeMessage(Message msg);

    Iterable<Message> getMessages(String type);

    @Override
    default void close() {
    }

    @Override
    default void flush() {
    }

    static NotificationService create(Properties p) {
        return Reflects.newInstance(p.getProperty("notification.storage.class"),
                new Class<?>[]{Properties.class}, p);
    }
}
