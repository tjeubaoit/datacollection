package com.datacollection.common.config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Cung cấp các method mở rộng từ class gốc của Java java.util.Properties cho phép
 * lấy ra các giá trị không phải String mà không cần phải tự convert/ép kiểu
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
@SuppressWarnings("unchecked")
public class Properties extends java.util.Properties {

    /**
     * Tạo ra SubProperties từ Properties hiện tại
     *
     * @param group nhóm của SubProperties cần tạo
     * @param name  tên của SubProperties cần tạo
     * @return new object SubProperties
     */
    public SubProperties toSubProperties(String group, String name) {
        return new SubProperties(group, name, this);
    }

    /**
     * Tạo ra SubProperties từ Properties hiện tại với group là null
     *
     * @param name tên của SubProperties cần tạo
     * @return new object SubProperties
     */
    public SubProperties toSubProperties(String name) {
        return new SubProperties(name, this);
    }

    /**
     * Tạo ra SubProperties từ Properties hiện tại với tên lấy từ tên class
     *
     * @param clazz tên class dùng để làm tên của SubProperties
     * @return new object SubProperties
     */
    public SubProperties toSubProperties(Class<?> clazz) {
        return new SubProperties(clazz, this);
    }

    public <T> T getProperty(String key, T defVal) {
        try {
            return (T) get(key);
        } catch (Exception ignored) {
            return defVal;
        }
    }

    /**
     * Lấy về giá trị có kiểu int
     *
     * @param key    key của giá trị cần lấy
     * @param defVal giá trị mặc định trả về
     * @return số int của giá trị cần lấy hoặc giá trị mặc định nếu
     * không tìm thấy giá trị nào ứng với key cần tìm
     */
    public int getIntProperty(String key, int defVal) {
        try {
            return Integer.parseInt(getProperty(key));
        } catch (Exception ignored) {
            return defVal;
        }
    }

    /**
     * Lấy về giá trị có kiểu long
     *
     * @param key    key của giá trị cần lấy
     * @param defVal giá trị mặc định trả về
     * @return số long của giá trị cần lấy hoặc giá trị mặc định nếu
     * không tìm thấy giá trị nào ứng với key cần tìm
     */
    public long getLongProperty(String key, long defVal) {
        try {
            return Long.parseLong(getProperty(key));
        } catch (Exception ignored) {
            return defVal;
        }
    }

    /**
     * Lấy về giá trị có kiểu double
     *
     * @param key    key của giá trị cần lấy
     * @param defVal giá trị mặc định trả về
     * @return số double của giá trị cần lấy hoặc giá trị mặc định nếu
     * không tìm thấy giá trị nào ứng với key cần tìm
     */
    public double getDoubleProperty(String key, double defVal) {
        try {
            return Double.parseDouble(getProperty(key));
        } catch (Exception ignored) {
            return defVal;
        }
    }

    /**
     * Lấy về giá trị có kiểu bool
     *
     * @param key    key của giá trị cần lấy
     * @param defVal giá trị mặc định trả về
     * @return số double của giá trị cần lấy hoặc giá trị mặc định nếu
     * không tìm thấy giá trị nào ứng với key cần tìm
     */
    public boolean getBoolProperty(String key, boolean defVal) {
        try {
            return Boolean.parseBoolean(getProperty(key));
        } catch (Exception ignored) {
            return defVal;
        }
    }

    /**
     * Lấy về giá trị có kiểu list String
     *
     * @param key key của giá trị cần lấy
     * @return list String các giá trị được phân cách bởi dấu phẩy
     */
    public List<String> getCollection(String key) {
        try {
            return Arrays.asList(getProperty(key).split(","));
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    /**
     * Lấy về giá trị có kiểu list String
     *
     * @param key       key của giá trị cần lấy
     * @param delimiter chuỗi dùng để phân tách các giá trị
     * @return list String các giá trị được phân cách bởi delimiter
     */
    public List<String> getCollection(String key, String delimiter) {
        try {
            return Arrays.asList(getProperty(key).split(delimiter));
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    /**
     * Lấy về giá trị có kiểu DateTime theo format yyyy-MM-dd HH:mm:ss
     *
     * @param key    key của giá trị cần lấy
     * @param defVal giá trị mặc định trả về
     * @return object Date của giá trị cần lấy hoặc giá trị mặc định nếu
     * không tìm thấy giá trị nào ứng với key cần tìm hoặc value
     * ở dạng raw không phù hợp để convert thành Date
     */
    public Date getDateTime(String key, Date defVal) {
        return getDateTime(key, "yyyy-MM-dd HH:mm:ss", defVal);
    }

    /**
     * Lấy về giá trị có kiểu DateTime
     *
     * @param key    key của giá trị cần lấy
     * @param format format để convert từ giá trị raw String về object Date
     * @param defVal giá trị mặc định trả về
     * @return object Date của giá trị cần lấy hoặc giá trị mặc định nếu
     * không tìm thấy giá trị nào ứng với key cần tìm hoặc value
     * ở dạng raw không phù hợp để convert thành Date
     */
    public Date getDateTime(String key, String format, Date defVal) {
        try {
            DateFormat df = new SimpleDateFormat(format);
            return df.parse(getProperty(key));
        } catch (Exception e) {
            return defVal;
        }
    }

    /**
     * Lấy về giá trị có kiểu Class
     *
     * @param key    key của giá trị cần lấy
     * @param defVal giá trị mặc định trả về
     * @return object Class
     */
    public Class<?> getClass(String key, Class<?> defVal) {
        try {
            return Class.forName(getProperty(key));
        } catch (Exception ignored) {
            return defVal;
        }
    }

    /**
     * Lấy về một hoặc nhiều class
     *
     * @param key key của giá trị cần lấy
     * @return danh sách các object Classs
     * @throws ClassNotFoundException nếu có lỗi xảy ra khi convert
     *                                từ tên class thành object Class
     */
    public Collection<Class<?>> getClasses(String key) throws ClassNotFoundException {
        List<Class<?>> classes = new LinkedList<>();
        for (String className : getCollection(key)) {
            classes.add(Class.forName(className));
        }
        return classes;
    }

    /**
     * Convert Properties thành object Map
     *
     * @return object Map chứa tất cả các thuộc tính
     */
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<>();
        for (Object key : keySet()) {
            map.put(key.toString(), this.get(key));
        }
        return map;
    }
}
