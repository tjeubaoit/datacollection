package com.datacollection.core.collect.fbavt;

import com.datacollection.common.FacebookClient;
import com.datacollection.common.config.Properties;

/**
 * Fetch facebook avatar URL từ per-app fbID. Phiên bản được implement
 * trực tiếp trong Java sử dụng thư viện OkHttp client
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class DirectHttpFetcher implements Fetcher {

    @Override
    public void configure(Properties p) {
    }

    @Override
    public String fetch(String id) {
        try {
            return FacebookClient.fetchAvatarUrl(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
