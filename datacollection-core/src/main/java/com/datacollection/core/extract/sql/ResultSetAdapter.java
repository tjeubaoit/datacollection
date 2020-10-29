package com.datacollection.core.extract.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ResultSetAdapter {

    private Map<String, Object> map = new LinkedHashMap<>();

    public ResultSetAdapter(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            map.put(metaData.getColumnLabel(i), rs.getObject(i));
        }
    }

    public Integer getInt(String columnLabel) {
        return get(columnLabel);
    }

    public Long getLong(String columnLabel) {
        return get(columnLabel);
    }

    public String getString(String columnLabel) {
        return get(columnLabel);
    }

    public Date getDate(String columnLabel) {
        return get(columnLabel);
    }

    public Float getFloat(String columnLabel) {
        return get(columnLabel);
    }

    public Double getDouble(String columnLabel) {
        return get(columnLabel);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String columnLabel) {
        try {
            return (T) map.get(columnLabel);
        } catch (NullPointerException e) {
            return null;
        }
    }
}
