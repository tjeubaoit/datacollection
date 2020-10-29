package com.datacollection.core.platform.mongo;

import com.mongodb.AuthenticationMechanism;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.Strings;
import com.datacollection.common.utils.Utils;

import java.util.Collection;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class MongoConfig {

    private MongoClientURI clientURI;
    private MongoCredential credential;
    private Collection<String> hosts;
    private String databaseName;

    public MongoConfig(Properties p) {
        String uri = p.getProperty("mongo.uri");
        if (uri != null) {
            initFromURI(uri);
        } else {
            this.hosts = p.getCollection("mongo.hosts");
            this.databaseName = p.getProperty("mongo.db");

            String mechanism = p.getProperty("mongo.mechanism");
            if (Strings.isNullOrEmpty(mechanism)) return;

            String username = p.getProperty("mongo.username");
            String password = p.getProperty("mongo.password");
            this.credential = createCredential(mechanism, username, databaseName, password);
        }
    }

    public MongoConfig() {
    }

    private void initFromURI(String uri) {
        this.clientURI = new MongoClientURI(uri, loadClientOptions());
        this.credential = clientURI.getCredentials();
        this.hosts = clientURI.getHosts();
        this.databaseName = clientURI.getDatabase();
    }

    public Collection<String> getHosts() {
        return hosts;
    }

    public void setHosts(Collection<String> hosts) {
        this.hosts = hosts;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public MongoClientURI getClientURI() {
        return clientURI;
    }

    public void setClientURI(MongoClientURI clientURI) {
        this.clientURI = clientURI;
    }

    public MongoCredential getCredential() {
        return credential;
    }

    public void setCredential(MongoCredential credential) {
        this.credential = credential;
    }

    static MongoCredential createCredential(String mechanismName, String username,
                                            String database, String password) {
        AuthenticationMechanism mechanism = AuthenticationMechanism.fromMechanismName(mechanismName);
        switch (mechanism) {
            case PLAIN:
                return MongoCredential.createPlainCredential(username, database, password.toCharArray());
            case GSSAPI:
                return MongoCredential.createGSSAPICredential(username);
            case MONGODB_CR:
                return MongoCredential.createMongoCRCredential(username, database, password.toCharArray());
            case SCRAM_SHA_1:
                return MongoCredential.createScramSha1Credential(username, database, database.toCharArray());
            case MONGODB_X509:
                return MongoCredential.createMongoX509Credential(username);
            default:
                return MongoCredential.createCredential(username, database, password.toCharArray());
        }
    }

    static Properties advancedProperties() {
        final String path = System.getProperty("mongo.conf", "config/mongo.properties");
        return Utils.loadPropsOrDefault(path);
    }

    static MongoClientOptions.Builder loadClientOptions() {
        Properties p = MongoConfig.advancedProperties();
        MongoClientOptions.Builder builder = MongoClientOptions.builder();
        if ("secondary".equals(p.getProperty("readPreference"))) {
            builder.readPreference(ReadPreference.secondary());
        }

        builder.connectionsPerHost(p.getIntProperty("maxConnectionsPerHost", 100));
        builder.serverSelectionTimeout(p.getIntProperty("serverSelectionTimeout", 30000));
        builder.maxWaitTime(p.getIntProperty("maxWaitTime", 120000));
        builder.connectTimeout(p.getIntProperty("connectTimeout", 10000));
        builder.socketTimeout(p.getIntProperty("socketTimeout", 0));
        builder.socketKeepAlive(p.getBoolProperty("socketKeepAlive", false));
        builder.localThreshold(p.getIntProperty("localThreshold", 15));

        return builder;
    }
}
