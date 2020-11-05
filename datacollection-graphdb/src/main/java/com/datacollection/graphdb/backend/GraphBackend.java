package com.datacollection.graphdb.backend;

import com.google.common.util.concurrent.ListenableFuture;
import com.datacollection.graphdb.Element;

import java.io.Closeable;
import java.util.Collection;

public interface GraphBackend<T extends Element> extends Closeable {

    Iterable<T> findAll();

    T findOne(T entity);

    ListenableFuture<? extends Iterable<T>> delete(T entity);

    ListenableFuture<? extends Iterable<T>> save(T entity);

    ListenableFuture<? extends Iterable<T>> saveAll(Collection<T> entities);

    @Override
    void close();
}
