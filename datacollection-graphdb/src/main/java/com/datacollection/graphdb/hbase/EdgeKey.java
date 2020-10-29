package com.datacollection.graphdb.hbase;

import com.datacollection.graphdb.Direction;
import com.datacollection.core.platform.hbase.HBaseUtils;
import org.apache.hadoop.hbase.util.Bytes;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * Vì HBase không hỗ trợ trực tiếp composite, compound key như Cassandra
 * nên để lưu trữ nhiều thông tin vào row key trong HBase phải sử dụng
 * phương pháp ghép chuỗi. Class này giúp dễ dàng convert qua lại giữa
 * các thông tin của một cạnh (dst vertex, src vertex, label...) và dạng
 * mảng byte lưu trữ trong HBase
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class EdgeKey {

    public final String srcLabel;
    public final String srcId;
    public final Direction direction;
    public final String edgeLabel;
    public final String dstLabel;
    public final String dstId;
    private final byte[] rawKey;

    public byte[] toBytes() {
        return this.rawKey;
    }

    private EdgeKey(String srcLabel, String srcId,
                    Direction direction, String edgeLabel,
                    String dstLabel, String dstId) {
        this.srcLabel = srcLabel;
        this.srcId = srcId;
        this.direction = direction;
        this.edgeLabel = edgeLabel;
        this.dstLabel = dstLabel;
        this.dstId = dstId;
        this.rawKey = HBaseUtils.buildCompositeKeyWithBucket(
                srcId, // seed
                srcLabel.getBytes(),
                srcId.getBytes(),
                direction.equals(Direction.IN) ? Constants.DIRECTION_IN : Constants.DIRECTION_OUT,
                edgeLabel.getBytes(),
                dstLabel.getBytes(),
                dstId.getBytes()
        );
    }

    private EdgeKey(byte[] rawKey) {
        List<ByteBuffer> buffers = HBaseUtils.extractCompositeKeys(rawKey);
        this.rawKey = rawKey;

        this.srcLabel = Bytes.toString(buffers.get(1).array());
        this.srcId = Bytes.toString(buffers.get(2).array());

        this.edgeLabel = Bytes.toString(buffers.get(4).array());
        this.direction = Arrays.equals(buffers.get(3).array(), Constants.DIRECTION_IN)
                ? Direction.IN : Direction.OUT;

        this.dstLabel = Bytes.toString(buffers.get(5).array());
        this.dstId = Bytes.toString(buffers.get(6).array());
    }

    public static EdgeKey from(String srcLabel, String srcId,
                               Direction direction, String edgeLabel,
                               String dstLabel, String dstId) {
        return new EdgeKey(srcLabel, srcId, direction, edgeLabel, dstLabel, dstId);
    }

    public static EdgeKey from(byte[] rawKey) {
        return new EdgeKey(rawKey);
    }
}
