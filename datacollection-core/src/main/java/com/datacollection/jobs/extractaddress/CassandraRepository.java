package com.datacollection.jobs.extractaddress;

import com.datacollection.platform.cassandra.AbstractRepository;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.IterableAdapter;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class CassandraRepository extends AbstractRepository implements ExtractAddressRepository {

    private final PreparedStatement psUpdateAddress;

    public CassandraRepository(Properties props) {
        super(props);
        this.psUpdateAddress = session.prepare(
                "UPDATE profiles SET address = ? WHERE uid = ? USING TIMESTAMP ?");
    }

    @Override
    public Iterable<String> findAllContentsByUid(long uid) {
        ResultSet rs = session.execute("select content from logs where uid = ?", uid);
        return new IterableAdapter<Row, String>(rs) {
            @Override
            protected String convert(Row row) {
                return row.getString("content");
            }
        };
    }

    @Override
    public void updateAddress(long uid, String address) {
        // Bởi vì address bóc tách được ở đây không có độ chính xác 100% nên độ ưu tiên của nó
        // sẽ thấp hơn address có được bởi job collect
        session.execute(psUpdateAddress.bind(address, uid, 1));
    }
}
