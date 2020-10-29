package com.datacollection.core.platform.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class PrestoConnections implements ConnectionProvider {

    private JdbcConfig jdbcConfig;

    @Override
    public void init(JdbcConfig jdbcConfig) throws SQLException {
        try {
            Class.forName("com.facebook.presto.jdbc.PrestoDriver");
            this.jdbcConfig = jdbcConfig;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcConfig.getConnectionUrl(),
                jdbcConfig.getUser(), jdbcConfig.getPassword());
    }
}
