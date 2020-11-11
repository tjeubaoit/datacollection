package com.datacollection.platform.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface ConnectionProvider {

    void init(JdbcConfig jdbcConfig) throws SQLException;

    Connection getConnection() throws SQLException;
}
