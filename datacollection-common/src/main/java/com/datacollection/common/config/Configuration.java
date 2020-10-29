package com.datacollection.common.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Là class chính dùng để load config cho toàn bộ các module, service của
 * ứng dụng từ một file. Kế thừa từ class Properties nên Configuraion
 * hoàn toàn tương thích để làm tham số truyền vào các method có input là
 * một Properties. Mặc định khi khởi tạo Configuration sẽ load các properties
 * từ file có tên <i>config.properties</i> đầu tiên tìm được trong classpath.
 * Tùy thuộc vào tham số truyền vào mà Configuration có thể load thêm các giá
 * trị từ một file ngoài (đường dẫn tuyệt đối hoặc thông qua biến môi trường).
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class Configuration extends Properties {

    private static final String CONF_ENV = "app.conf";
    private static final String DEFAULT_NAME = "config.properties";

    /**
     * Load properties and update into JVM system properties
     *
     * @param props        object contains properties to load
     * @param propertyKeys property keys that will be updated
     */
    public static void setSystemPropertiesFromConfig(Properties props, String... propertyKeys) {
        for (String key : propertyKeys) {
            if (System.getProperty(key) != null) continue;
            System.setProperty(key, props.getProperty(key));
        }
    }

    /**
     * Set default resource path use to load properties when init Configuration
     *
     * @param path Path to file use as default resource to load properties
     *             when init new Configuration object with empty constructor
     */
    public static void setDefaultResourcePath(String path) {
        defaultResPath = path;
    }

    private static final Properties defaultProps = new Properties();
    private static String defaultResPath = System.getProperty(CONF_ENV);

    static {
        try {
            // default properties from resource file
            defaultProps.load(Configuration.class.getClassLoader().getResourceAsStream(DEFAULT_NAME));
        } catch (IOException e) {
            Throwable t = new IOException("Cannot find config.properties in classpath");
            t.printStackTrace(new PrintStream(System.out));
        }
    }

    /**
     * Khởi tạo Configuration object từ default resource
     */
    public Configuration() {
        this(defaultResPath);
    }

    /**
     * Khởi tạo Configuration object từ đường dẫn tới file resource
     *
     * @param path đường dẫn tới file resource chứa các properties dùng
     *             để khởi tạo Configuration
     */
    public Configuration(String path) {
        try {
            // extra properties from file file will have higher priority
            addResource(path);
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Khởi tạo Configuration object từ một đối tượng InputStream
     *
     * @param is InputStream object chứa các properites dùng để khởi tạo Configuration
     */
    public Configuration(InputStream is) {
        try {
            addResource(is);
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Load bổ sung các thuộc tính từ một resource
     *
     * @param is InputStream object chứa các properites
     * @throws IOException nếu có lỗi xảy ra
     */
    public void addResource(InputStream is) throws IOException {
        this.addResource(is, false);
    }

    /**
     * Load bổ sung các thuộc tính từ một resource
     *
     * @param is             InputStream object chứa các properites
     * @param closeAfterLoad có đóng InputStream lại sau khi đọc hay không
     * @throws IOException nếu có lỗi xảy ra
     */
    public void addResource(InputStream is, boolean closeAfterLoad) throws IOException {
        try {
            this.load(is);
        } finally {
            if (closeAfterLoad) is.close();
        }
    }

    /**
     * Load bổ sung các thuộc tính từ một resource
     *
     * @param path đường dẫn tới File resource chứa các thuộc tính
     * @throws IOException nếu có lỗi xảy ra
     */
    public void addResource(String path) throws IOException {
        if (path != null) {
            this.addResource(new FileInputStream(path), true);
        }
    }

    @Override
    public String getProperty(String key) {
        String value = super.getProperty(key);
        return (value != null) ? value : defaultProps.getProperty(key);
    }

    @Override
    public synchronized String toString() {
        Set<Object> keySet = new LinkedHashSet<>();
        keySet.addAll(this.keySet());
        keySet.addAll(defaultProps.keySet());

        List<Object> list = new ArrayList<>(keySet);
        list.sort(Comparator.comparing(Object::toString));

        StringBuilder sb = new StringBuilder("Configuration properties:\n");
        list.forEach(key -> sb.append(String.format(Locale.US, "\t%s = %s\n",
                key.toString(), getProperty(key.toString(), ""))));
        return sb.toString();
    }
}
