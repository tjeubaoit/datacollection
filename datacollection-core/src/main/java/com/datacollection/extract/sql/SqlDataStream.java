package com.datacollection.extract.sql;

import com.datacollection.extract.DataStream;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by kumin on 12/04/2017.
 * Updated by tjeubaoit on 18/04/2017.
 */
public class SqlDataStream implements DataStream<ResultSetAdapter> {

    private final SQLFetcher fetcher;
    private ResultSet resultSet;
    private Object lastId;
    private boolean hasNext;

    public SqlDataStream(SQLFetcher fetcher, Object fromId) {
        try {
            this.fetcher = fetcher;
            this.resultSet = fetcher.fetchNextRows(fromId);
            this.hasNext = resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasNext() {
        try {
            if (!hasNext && lastId != null) {
                this.close();
                resultSet = fetcher.fetchNextRows(lastId);
                hasNext = resultSet.next();
            }
            return hasNext;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResultSetAdapter next() {
        try {
            lastId = fetcher.fetchIndex(resultSet);
            ResultSetAdapter resultSetAdapter = new ResultSetAdapter(resultSet);
            hasNext = resultSet.next();

            return resultSetAdapter;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            if (resultSet != null) this.resultSet.close();
        } catch (SQLException ignored) {
        }
    }
}
