package com.datacollection.jobs.extractaddress;

import java.io.Closeable;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface ExtractAddressRepository extends Closeable {

    Iterable<String> findAllContentsByUid(long uid);

    void updateAddress(long uid, String address);

    void close();
}
