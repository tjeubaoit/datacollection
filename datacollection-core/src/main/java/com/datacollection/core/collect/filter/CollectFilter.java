package com.datacollection.core.collect.filter;

import com.datacollection.core.collect.model.GraphModel;
import com.datacollection.common.config.Configurable;
import com.datacollection.common.config.Properties;

/**
 * Đại diện cho một bộ lọc dữ liệu của collector, các records nếu
 * không được accept bởi các filters sẽ bị bỏ qua và không được
 * collector xử lý
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface CollectFilter extends Configurable {

    @Override
    default void configure(Properties p) {
    }

    /**
     * Cho biết một record (đã được transform về dạng GraphModel) có
     * hợp lệ để được xử lý hay không
     *
     * @param gm GraphModel cần kiểm tra
     * @return true nếu GraphModel object là hợp lệ và được phép xử
     * lý bởi Collector hoặc false ngược lại
     */
    boolean accept(GraphModel gm);
}
