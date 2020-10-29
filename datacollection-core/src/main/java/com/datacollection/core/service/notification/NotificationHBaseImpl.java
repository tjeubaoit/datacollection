package com.datacollection.core.service.notification;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ListenableFuture;
import com.datacollection.core.collect.Constants;
import com.datacollection.common.concurrency.FutureAdapter;
import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.IterableAdapter;
import com.datacollection.common.utils.Utils;
import com.datacollection.core.platform.hbase.AbstractRepository;
import com.datacollection.core.platform.hbase.HBaseRuntimeException;
import com.datacollection.core.platform.hbase.HBaseUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class NotificationHBaseImpl extends AbstractRepository implements NotificationService {

    private final TableName tableMessages;

    public NotificationHBaseImpl(Properties props) {
        super(props);
        this.tableMessages = TableName.valueOf(props.getProperty("notification.hbase.table", "messages"));
    }

    @Override
    public ListenableFuture<Iterable<Message>> addMessages(Collection<Message> messages) {
        Future<?> fut = execute("AddNotification", () -> {
            try (Table table = connection.getTable(tableMessages)) {
                List<Put> puts = new ArrayList<>(messages.size());
                for (Message msg : messages) {
                    // always use bucket with new version
                    byte[] row = buildRowKey(msg.getType(), msg.getKey());

                    Put put = msg.getVersion() > 0 ? new Put(row, msg.getVersion()) : new Put(row);
                    put.addColumn(CF, CQ_HIDDEN, HBaseUtils.EMPTY);
                    msg.getProperties().forEach((k, v) -> put.addColumn(CF, k.getBytes(), v.getBytes()));

                    puts.add(put);
                }
                table.put(puts);
            } catch (IOException e) {
                throw new HBaseRuntimeException(e);
            }
        });
        return FutureAdapter.from(fut, o -> messages);
    }

    @Override
    public ListenableFuture<Message> removeMessage(Message msg) {
        Future<?> fut = execute("RemoveNotification", () -> {
            try (Table table = connection.getTable(tableMessages)) {
                byte[] row = msg.getBucket() > 0
                        ? buildRowKey(msg.getType(), msg.getKey())
                        // compatible with old version that do not use bucket
                        : HBaseUtils.createCompositeKey(msg.getType().getBytes(), msg.getKey().getBytes());

                Delete delete = new Delete(row);
                if (msg.getVersion() > 0) delete.setTimestamp(msg.getVersion() + 1);
                table.delete(delete);
            } catch (IOException e) {
                throw new HBaseRuntimeException(e);
            }
        });
        return FutureAdapter.from(fut, o -> msg);
    }

    @Override
    public Iterable<Message> getMessages(String type) {
        try (Table table = connection.getTable(tableMessages)) {
            List<Iterable<Message>> list = new ArrayList<>(HBaseUtils.DEFAULT_MAX_BUCKET);

            for (int i = -1; i < HBaseUtils.DEFAULT_MAX_BUCKET; i++) {
                Scan scan = new Scan();
                if (i == -1) { // compatible with old version that do not use bucket
                    scan.setRowPrefixFilter(type.getBytes());
                } else {
                    byte[] bucket = String.valueOf(i).getBytes();
                    scan.setRowPrefixFilter(HBaseUtils.createCompositeKey(bucket, type.getBytes()));
                }

                ResultScanner scanner = table.getScanner(scan);
                Iterable<Message> messages = IterableAdapter.from(scanner,
                        NotificationHBaseImpl::convertResultToMessage);
                list.add(messages);
            }

            return Iterables.concat(list);
        } catch (IOException e) {
            throw new HBaseRuntimeException(e);
        }
    }

    private static Message convertResultToMessage(Result result) {
        List<ByteBuffer> buffers = HBaseUtils.extractCompositeKeys(result.getRow());
        boolean useBucket = buffers.size() > 2;
        String type, key;

        if (useBucket) {
            type = Bytes.toString(buffers.get(1).array());
            key = Bytes.toString(buffers.get(2).array());
        } else { // compatible with old version that do not use bucket
            type = Bytes.toString(buffers.get(0).array());
            key = Bytes.toString(buffers.get(1).array());
        }

        Message msg = new Message(type);
        msg.setKey(key);
        msg.setBucket(useBucket ? Integer.parseInt(Bytes.toString(buffers.get(0).array())) : -1);

        result.getFamilyMap(CF).forEach((k, v) -> {
            String kas = Bytes.toString(k);
            if (kas.startsWith(Constants.SYSTEM_PREFIX)) return;
            msg.putProperty(Bytes.toString(k), Bytes.toString(v));
        });

        Cell cell = result.getColumnLatestCell(CF, CQ_HIDDEN);
        msg.setVersion(cell != null ? cell.getTimestamp() : Utils.reverseTimestamp());

        return msg;
    }

    private static byte[] buildRowKey(String type, String key) {
        return HBaseUtils.buildCompositeKeyWithBucket(key, type.getBytes(), key.getBytes());
    }
}
