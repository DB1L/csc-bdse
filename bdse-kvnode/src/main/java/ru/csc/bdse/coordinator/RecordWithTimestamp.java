package ru.csc.bdse.coordinator;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import ru.csc.bdse.bdse.proto.RecordWithTimestampOuterClass;

import java.io.UncheckedIOException;
import java.util.Arrays;

public class RecordWithTimestamp {
    private final byte[] payload;
    private final long createdAt;
    private final boolean isDeleted;
    private final int nodeNum;

    public RecordWithTimestamp(byte[] payload, long createdAt, boolean isDeleted, int nodeNum) {
        this.payload = payload;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
        this.nodeNum = nodeNum;
    }

    public byte[] payload() {
        return payload;
    }

    public long createdAt() {
        return createdAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    byte[] encode() {
        return RecordWithTimestampOuterClass.RecordWithTimestamp.newBuilder()
                .setPayload(ByteString.copyFrom(payload))
                .setCreatedAt(createdAt)
                .setIsDeleted(isDeleted)
                .build()
                .toByteArray();
    }

    static RecordWithTimestamp decode(byte[] data) {
        try {
            final RecordWithTimestampOuterClass.RecordWithTimestamp record =
                    RecordWithTimestampOuterClass.RecordWithTimestamp.parseFrom(data);
            return new RecordWithTimestamp(
                    record.getPayload().toByteArray(),
                    record.getCreatedAt(),
                    record.getIsDeleted(),
                    record.getNodeNum());
        } catch (InvalidProtocolBufferException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String toString() {
        return "RecordWithTimestamp{" +
                "payload=" + Arrays.toString(payload) +
                ", createdAt=" + createdAt +
                ", isDeleted=" + isDeleted +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordWithTimestamp that = (RecordWithTimestamp) o;
        return Arrays.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(payload);
    }

    public int nodeNum() {
        return this.nodeNum;
    }
}
