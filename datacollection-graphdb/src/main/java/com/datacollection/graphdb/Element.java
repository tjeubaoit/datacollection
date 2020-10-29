package com.datacollection.graphdb;

import java.util.Map;

/**
 * Interface chung cho mọi đối tượng trong graph, bao gồm cả cạnh và đỉnh
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface Element {

    String id();

    String label();

    /**
     * Lấy thuộc tính của đối tượng theo key
     *
     * @param key key của thuộc tính cần đọc giá trị
     * @return giá trị cần đọc
     */
    Object property(String key);

    /**
     * @return Lấy về toàn bộ thuộc tính dưới dạng Map
     */
    Map<String, ?> properties();

    /**
     * Thêm hoặc update một thuộc tính
     *
     * @param key   key của thuộc tính cần thêm/update
     * @param value giá trị của thuộc tính cần thêm/update
     */
    void putProperty(String key, Object value);

    /**
     * Thêm hoặc update nhiều thuộc tính
     *
     * @param map danh sách các thuộc tính cần thêm/update dưới dạng map
     */
    void putProperties(Map<String, ?> map);
}
