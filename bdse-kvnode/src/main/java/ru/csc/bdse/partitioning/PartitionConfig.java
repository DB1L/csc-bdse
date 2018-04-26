package ru.csc.bdse.partitioning;

import ru.csc.bdse.kv.KeyValueApi;

import java.util.Map;

public class PartitionConfig {
    private final Map<String, KeyValueApi> partitions;
    private final long timeoutMillis;
    private final Partitioner partitioner;

    public PartitionConfig(Map<String, KeyValueApi> partitions, long timeoutMillis, Partitioner partitioner) {
        this.partitions = partitions;
        this.timeoutMillis = timeoutMillis;
        this.partitioner = partitioner;
    }

    public Map<String, KeyValueApi> partitions() {
        return this.partitions;
    }

    public long timeoutMillis() {
        return this.timeoutMillis;
    }

    public Partitioner partitioner() {
        return this.partitioner;
    }
}
