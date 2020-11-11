package com.datacollection.extract.sql;

import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.common.config.Configuration;
import com.datacollection.extract.DataStream;
import com.datacollection.extract.StreamExtractor;
import com.datacollection.extract.model.GenericModel;
import com.datacollection.platform.jdbc.ConnectionProviders;
import com.datacollection.platform.jdbc.JdbcConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class NewDbExtractor extends StreamExtractor<ResultSetAdapter> {

    private static final String KEY_BATCH_SIZE = "jdbc.batch.size";
    private final ProfileRegexHelper regexHelper = ProfileRegexHelper.getDefault();
    private Connection sqlConnect;

    public NewDbExtractor(Configuration config) {
        super("newdb", config);
        this.sqlConnect = ConnectionProviders.getOrCreate(group, new JdbcConfig(this.props));
    }

    @Override
    protected DataStream<ResultSetAdapter> openDataStream() {
        int lastId = Integer.parseInt(loadIndex("-1"));
        int batchSize = props.getIntProperty(KEY_BATCH_SIZE, 1000);

        return new SqlDataStream(new SQLFetcher() {
            @Override
            public ResultSet fetchNextRows(Object fromIndex) {
                String query = "SELECT n.id, n.url, n.title, n.content, n.source, n.create_time, n.description " +
                        "FROM news as n " +
                        "WHERE n.id > ? " +
                        "ORDER BY n.id ASC LIMIT ?";
                try {
                    PreparedStatement ps = sqlConnect.prepareStatement(query);
                    ps.setInt(1, (Integer) fromIndex);
                    ps.setInt(2, batchSize);
                    return ps.executeQuery();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Object fetchIndex(ResultSet rs) {
                try {
                    return rs.getObject("id");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }, lastId);
    }

    @Override
    protected GenericModel extractData(ResultSetAdapter rs) {
        try {
            Collection<String> phones = this.extractPhones(parseContent(rs));
            Collection<String> emails = this.extractEmails(parseContent(rs));

            if(phones.isEmpty()&&emails.isEmpty()) return null;
            Map<String, Object> post = new LinkedHashMap<>();
            post.put("url", rs.getString("url"));
            post.put("domain", rs.getString("source"));
            post.put("create_time", rs.getDate("create_time"));
            post.put("phones", phones);
            post.put("emails", emails);

            String id = String.valueOf(rs.getInt("id"));
//            String type = GenericModel.TYPE_NEWSDB;

            return new GenericModel(id, null, post);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String parseContent(ResultSetAdapter rs) {
        return Optional.ofNullable(rs.getString("content")).orElse("")
                + Optional.ofNullable(rs.getString("description")).orElse("");
    }

    private Set<String> extractEmails(String content) {
        return regexHelper.extractEmails(content);
    }

    private Set<String> extractPhones(String content) {
        Set<String> phonesExtract = regexHelper.extractPhones(content);
        Set<String> phones = new HashSet<>();
        for (String phone : phonesExtract) {
            phone = phone.trim().replaceAll("[^0-9]", "");
            phone = phone.startsWith("0") ? phone : "0" + phone;
            phones.addAll(regexHelper.extractPhones(phone));
        }
        return phones;
    }
}
