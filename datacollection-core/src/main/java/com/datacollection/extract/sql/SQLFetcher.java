package com.datacollection.extract.sql;

import java.sql.ResultSet;

/**
 * Created by kumin on 12/04/2017.
 */
public interface SQLFetcher {

    ResultSet fetchNextRows(Object fromIndex);

    Object fetchIndex(ResultSet rs);
}
