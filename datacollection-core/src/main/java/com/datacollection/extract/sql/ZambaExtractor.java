package com.datacollection.extract.sql;

import com.datacollection.common.TextUtils;
import com.datacollection.common.config.Configuration;
import com.datacollection.extract.DataStream;
import com.datacollection.extract.StreamExtractor;
import com.datacollection.extract.model.GenericModel;
import com.datacollection.extract.model.ZambaPost;
import com.datacollection.platform.jdbc.ConnectionProviders;
import com.datacollection.platform.jdbc.JdbcConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by kumin on 12/04/2017.
 * Updated by tjeubaoit on 25/09/2017
 */
public class ZambaExtractor extends StreamExtractor<ResultSetAdapter> {
    static final String KEY_BATCH_SIZE = "jdbc.batch.size";

    private Connection sqlConnect;

    public ZambaExtractor(Configuration config) {
        super("zamba", config);
        this.sqlConnect = ConnectionProviders.getOrCreate(group, new JdbcConfig(this.props));
    }

    @Override
    protected DataStream<ResultSetAdapter> openDataStream() {
        int lastId = Integer.parseInt(loadIndex("-1"));
        int batchSize = props.getIntProperty(KEY_BATCH_SIZE, 1000);

        return new SqlDataStream(new SQLFetcher() {
            @Override
            public ResultSet fetchNextRows(Object fromIndex) {
                String query = "SELECT p.id, p.url, p.subject, p.description, p.contents, p.postdate, p.area, p.price, " +
                        "p.address, p.location, p.userId, c.name, c.phone, c.email, c.gender, c.birthday, c.address, " +
                        "c.facebook, c.UrlContainContact " +
                        "FROM posts AS p " +
                        "INNER JOIN contact AS c ON p.userId = c.id " +
                        "WHERE p.id > ? " +
                        "ORDER BY p.id ASC LIMIT ?";
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
        ZambaPost zamba = new ZambaPost();
        try {
            zamba.userId = String.valueOf(rs.getInt("userId"));
            zamba.userName = rs.getString("name");
            zamba.content = parseContent(rs);
            zamba.subject = rs.getString("subject");
            zamba.price = String.valueOf(rs.getDouble("price"));
            zamba.area = rs.getString("area");
            zamba.postTime = rs.getDate("postdate").getTime();
            zamba.url = rs.getString("url");
            zamba.domain = TextUtils.parseDomain(zamba.url);

            Map<String, Object> post = new HashMap<>();
            post.put("name", rs.getString("name"));
            post.put("gender", rs.getString("gender"));
            post.put("birthday", rs.getString("birthday"));
            post.put("address", rs.getString("address"));
            post.put("location", rs.getString("location"));
            post.put("facebook", rs.getString("facebook"));
            post.put("email", rs.getString("email"));
            post.put("phone", rs.getString("phone"));
            post.put("profile_url", rs.getString("UrlContainContact"));

            post.putAll(zamba.toMap());

            String id = String.valueOf(rs.getInt("id"));
            String type = GenericModel.TYPE_ZAMBA;

            return new GenericModel(id, type, post);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String parseContent(ResultSetAdapter rs) {
        return Optional.ofNullable(rs.getString("contents")).orElse("")
                + Optional.ofNullable(rs.getString("description")).orElse("");
    }
}
