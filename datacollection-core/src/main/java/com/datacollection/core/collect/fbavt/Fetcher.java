package com.datacollection.core.collect.fbavt;

import com.datacollection.common.config.Configurable;
import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.Reflects;

/**
 * Facebook avatar URL Fetcher. Interface chung quy định cách
 * lấy avatar URL từ một per-app Facebook userId
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
interface Fetcher extends Configurable {

    /**
     * Fetch facebook avatar url from facebook cdn
     *
     * @param id per-app facebook id of user to retrieve avatar url
     * @return avatar url of user identified by id or null if not exist
     */
    String fetch(String id);

    static Fetcher create(Properties p) {
        Fetcher ins = Reflects.newInstance(p.getProperty("fbavatar.fetcher.class"));
        ins.configure(p);
        return ins;
    }
}
