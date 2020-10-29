package com.datacollection.core.platform.jdbc;

import com.datacollection.common.utils.Reflects;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

public class ConnectionProviders {

    private static Map<String, ConnectionProvider> clients = new TreeMap<>();

    public static synchronized Connection getOrCreate(String name, JdbcConfig config) {
        try {
            if (clients.containsKey(name)) {
                return clients.get(name).getConnection();
            } else {
                ConnectionProvider connectionProvider = init(config);
                clients.put(name, connectionProvider);
                return connectionProvider.getConnection();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static ConnectionProvider init(JdbcConfig jdbcConfig) {
        try {
            ConnectionProvider connectionProvider = Reflects.newInstance(jdbcConfig.getProviderClassName());
            connectionProvider.init(jdbcConfig);
            return connectionProvider;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}