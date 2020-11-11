package com.datacollection.platform.jdbc;

import com.datacollection.common.config.Properties;

public class JdbcConfig {

    private String connectionUrl;
    private String user;
    private String password;
    private String providerClassName;

    public JdbcConfig(Properties config) {
        this.connectionUrl = config.getProperty("jdbc.url");
        this.user = config.getProperty("jdbc.user");
        this.password = config.getProperty("jdbc.password");
        this.providerClassName = config.getProperty("jdbc.provider.class");
    }

    public JdbcConfig() {
    }

    public String getProviderClassName() {
        return providerClassName;
    }

    public void setProviderClassName(String providerClassName) {
        this.providerClassName = providerClassName;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "connectionUrl= " + connectionUrl
                + "; user= " + user
                + "; pass= " + password + "\n";
    }
}
