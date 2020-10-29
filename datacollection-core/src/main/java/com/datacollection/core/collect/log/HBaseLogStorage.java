package com.datacollection.core.collect.log;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ListenableFuture;
import com.datacollection.core.collect.model.Log;
import com.datacollection.common.concurrency.FutureAdapter;
import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.IterableAdapter;
import com.datacollection.common.utils.Strings;
import com.datacollection.core.platform.hbase.AbstractRepository;
import com.datacollection.core.platform.hbase.HBaseRuntimeException;
import com.datacollection.core.platform.hbase.HBaseUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Phiên bản cài đặt của LogStorage được lưu trữ trên HBase
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class HBaseLogStorage extends AbstractRepository implements LogStorage {

    private final TableName tableLogs;

    public HBaseLogStorage(Properties p) {
        super(p);
        this.tableLogs = TableName.valueOf(p.getProperty("collect.log.hbase.table", "logs"));
    }

    @Override
    public ListenableFuture<Log> addLog(String uid, Log log) {
        Future<?> fut = execute("AddLogs", () -> {
            try (Table table = connection.getTable(tableLogs)) {
                byte[] row = HBaseUtils.buildCompositeKeyWithBucket(
                        log.id(), // seed
                        uid.getBytes(),
                        log.id().getBytes());

                Put put = new Put(row);
                put.addColumn(CF, CQ_HIDDEN, HBaseUtils.EMPTY);
                log.properties().forEach((k, v)
                        -> put.addColumn(CF, k.getBytes(), v.toString().getBytes()));
                table.put(put);
            } catch (IOException e) {
                throw new HBaseRuntimeException(e);
            }
        });
        return FutureAdapter.from(fut, o -> log);
    }

    @Override
    public Iterable<Log> findLogByUid(String uid) {
        try (Table table = connection.getTable(tableLogs)) {
            List<Iterable<Log>> list = new ArrayList<>(HBaseUtils.DEFAULT_MAX_BUCKET);

            for (int i = 0; i < HBaseUtils.DEFAULT_MAX_BUCKET; i++) {
                Scan scan = new Scan();
                byte[] bucket = Strings.format("%03d", i).getBytes();
                scan.setRowPrefixFilter(HBaseUtils.createCompositeKey(bucket, uid.getBytes()));

                ResultScanner scanner = table.getScanner(scan);
                Iterable<Log> logs = IterableAdapter.from(scanner, result -> {
                    String type = Bytes.toString(result.getValue(CF, Log.TYPE.getBytes()));
                    String source = Bytes.toString(result.getValue(CF, Log.SOURCE.getBytes()));
                    String url = Bytes.toString(result.getValue(CF, Log.URL.getBytes()));

                    Log log = new Log(type, source, url);
                    result.getFamilyMap(CF).forEach((k, v)
                            -> log.putProperty(Bytes.toString(k), Bytes.toString(v)));

                    return log;
                });
                list.add(logs);
            }

            return Iterables.concat(list);
        } catch (IOException e) {
            throw new HBaseRuntimeException(e);
        }
    }
}
