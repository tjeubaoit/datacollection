package com.datacollection.collect.idgen;

import com.datacollection.common.config.Properties;

import java.util.List;

/**
 * Mock implementation of RemoteIdGenerator, use only for testing.
 * Do nothing and always return default value.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class MockRemoteIdGenerator implements RemoteIdGenerator {

    @Override
    public long generate(List<String> seeds, long defVal) {
        return defVal;
    }

    @Override
    public void configure(Properties p) {
    }
}
