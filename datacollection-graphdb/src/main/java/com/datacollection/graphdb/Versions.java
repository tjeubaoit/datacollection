package com.datacollection.graphdb;

import java.util.Map;
import java.util.TreeMap;

/**
 * Quản lý version trong đồ thị. Mỗi cạnh hoặc đỉnh trong đồ thị đều có thể được
 * thêm một giá trị là version được lưu trữ dưới dạng một thuộc tính ẩn. Version
 * được xác định theo từng nguồn hay kiểu (type) khác nhau. Nghĩa là cùng một
 * cạnh hay một đỉnh có thể có nhiều version khác nhau tùy thuộc vào nguồn để xác
 * định ra cạnh/đỉnh đó.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class Versions {

    /**
     * Dùng để phân biệt giữa thuộc tính lưu trữ version và các
     * thuộc tính thông thường trong đồ thị
     */
    public static final String VERSION_PREFIX = "_v";
    /**
     * Phân cách giữa phần prefix và phần metadata chứa thông tin version
     */
    public static final String DELIMITER = "@";
    /**
     * Version mặc định cho cạnh/đỉnh nếu không được xác định cụ thể
     */
    public static final int MIN_VERSION = 1;

    /**
     * Set version cho một đối tượng trong đồ thị (graph element)
     *
     * @param type    type cho biết nguồn gốc xác định version
     * @param version số nguyên int version của element theo type đang xét
     * @param element element cần set version
     */
    public static void setVersion(String type, int version, Element element) {
        String key = VERSION_PREFIX + DELIMITER + type;
        element.putProperty(key, String.valueOf(version));
    }

    /**
     * Lấy thông tin version của một element theo một type cụ thể
     *
     * @param type    type cần lấy thông tin version
     * @param element GraphElement cần lấy thông tin version
     * @return số nguyên int là version của element theo type cần lấy
     */
    public static int getVersion(String type, Element element) {
        String key = VERSION_PREFIX + DELIMITER + type;
        return Integer.parseInt(element.property(key).toString());
    }

    /**
     * Lấy tất cả các version hiện có của mọt element
     *
     * @param element GraphElement cần lấy thông tin version
     * @return Map object chứa các version theo các type khác nhau
     * của element dưới dạng key-value
     */
    public static Map<String, Integer> getAllVersions(Element element) {
        Map<String, Integer> map = new TreeMap<>();
        String prefix = VERSION_PREFIX + DELIMITER;

        for (Map.Entry<String, ?> e : element.properties().entrySet()) {
            try {
                String key = e.getKey();
                if (!key.startsWith(prefix)) continue;
                String type = key.split(DELIMITER)[1];
                map.put(type, Integer.valueOf(e.getValue().toString()));
            } catch (IndexOutOfBoundsException | NullPointerException | NumberFormatException ignored) {
            }
        }
        return map;
    }

    /**
     * Kiểm tra một graph element là có hợp lệ hay không. Một element là hợp lệ
     * nếu nó có ít nhất một version là mới nhất theo một type nào đó. Version
     * mới nhất theo từng type được xác định bởi tham số truyền vào
     *
     * @param element              GraphElement cần kiểm tra
     * @param latestVersionMapping Map chứa danh sách các version mới nhất theo
     *                             từng type dưới dạng key-value
     * @return true nếu element là không hợp lệ (out-of-date) và false nếu ngược lại
     */
    public static boolean checkElementOutOfDate(Element element, Map<String, Integer> latestVersionMapping) {
        Map<String, Integer> versions = Versions.getAllVersions(element);
        for (Map.Entry<String, Integer> e : versions.entrySet()) {
            String type = e.getKey();
            // true if at least one type is latest version
            if (e.getValue() >= latestVersionMapping.getOrDefault(type, Versions.MIN_VERSION)) {
                return false;
            }
        }
        return true;
    }
}
