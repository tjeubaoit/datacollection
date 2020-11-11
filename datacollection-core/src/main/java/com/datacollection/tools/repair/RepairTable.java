package com.datacollection.tools.repair;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datacollection.common.config.Configuration;
import com.datacollection.platform.cassandra.CassandraClusterProvider;
import com.datacollection.platform.cassandra.CassandraConfig;

import java.util.Collections;

/**
 * Created by kumin on 09/11/2017.
 */
public class RepairTable {
    public static void main(String[] args) {
        Configuration config = new Configuration();
        CassandraConfig  cassandraConfig = new CassandraConfig(config);
        Session session = CassandraClusterProvider.getOrCreate("repair table", cassandraConfig).connect(cassandraConfig.getKeyspace());

        ResultSet rs = session.execute("SELECT * FROM idx_emails;");
        rs.forEach(row -> {
            Long puid = row.getLong("puid");
            Long uid = row.getLong("uid");
            String id = row.getString("email");
            System.out.println(id);
            if (puid != 0)
                session.executeAsync("UPDATE idx_emails SET uids = uids + ? WHERE email = ?",
                        Collections.singletonMap("facebook.com", puid), id);
            else if (uid != 0)
                session.executeAsync("UPDATE idx_emails SET uids = uids + ? WHERE email = ?",
                        Collections.singletonMap("facebook.com", uid), id);
        });
        System.out.println("done!");
        session.close();
    }
}
