package com.datacollection.core.collect;

import com.google.common.util.concurrent.ListenableFuture;
import com.datacollection.core.extract.model.GenericModel;

import java.io.Closeable;

/**
 * Service xử lý chính toàn bộ dữ liệu phía dưới một Collector
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface CollectService extends Closeable {

    /**
     * Thực hiện transform + process + store dữ liệu
     *
     * @param genericModel object GenericModel chứa dữ liệu raw cần xử lý
     * @return Future biểu diễn trạng thái của quá trình xử lý
     */
    ListenableFuture<?> collect(GenericModel genericModel);

    @Override
    void close();
}
