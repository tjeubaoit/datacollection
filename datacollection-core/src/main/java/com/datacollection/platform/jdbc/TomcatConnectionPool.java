package com.datacollection.platform.jdbc;

import com.datacollection.common.config.Properties;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class TomcatConnectionPool implements ConnectionProvider {

    private DataSource ds;

    @Override
    public void init(JdbcConfig jdbcConfig) throws SQLException {
        ds = new org.apache.tomcat.jdbc.pool.DataSource(
                new MyPoolProperties(jdbcConfig, JdbcConfigHelper.getConnectionPoolProperties()));

        Connection con = ds.getConnection();
//        PreparedStatement ps = con.prepareStatement("SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci");
//        ps.executeUpdate();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    private static class MyPoolProperties extends PoolProperties {

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        public MyPoolProperties(JdbcConfig jdbcConfig, Properties p) {
            this.setTestOnBorrow(p.getBoolProperty("pool.testOnBorrow", true));
            this.setTestOnReturn(p.getBoolProperty("pool.testOnReturn", true));
            this.setValidationInterval(p.getLongProperty("pool.validationInterval", 30000));
            this.setTimeBetweenEvictionRunsMillis(p.getIntProperty("pool.timeBetweenEvictionRunsMillis", 30000));
            this.setMinEvictableIdleTimeMillis(p.getIntProperty("pool.minEvictableIdleTimeMillis", 30000));
            this.setMaxIdle(p.getIntProperty("pool.maxIdle", 2));
            this.setMaxActive(p.getIntProperty("pool.maxActive", 5));
            this.setInitialSize(p.getIntProperty("pool.initialSize", 2));
            this.setMaxWait(p.getIntProperty("pool.maxWait", 5));
            this.setMinIdle(p.getIntProperty("pool.minIdle", 2));
            this.setRemoveAbandoned(p.getBoolProperty("pool.removeAbandoned", true));
            this.setRemoveAbandonedTimeout(p.getIntProperty("pool.removeAbandonedTimeout", 300));
            this.setDriverClassName("com.mysql.jdbc.Driver");
            this.setValidationQuery("SELECT 1");
            this.setLogAbandoned(false);

            this.setUrl(jdbcConfig.getConnectionUrl());
            this.setUsername(jdbcConfig.getUser());
            this.setPassword(jdbcConfig.getPassword());
        }
    }
}
