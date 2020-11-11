package com.datacollection;

import com.datacollection.common.config.Configuration;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class AppInfo {

    public static final int MODE_LOCAL = 1;
    public static final int MODE_SERVER = 2;

    public static int mode() {
        return "server".equals(System.getProperty("app.mode", "local"))
                ? MODE_SERVER : MODE_LOCAL;
    }

    public static String version() {
        return new Configuration().getProperty("app.version");
    }
}
