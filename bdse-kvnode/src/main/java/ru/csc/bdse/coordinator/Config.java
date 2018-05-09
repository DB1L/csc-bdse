package ru.csc.bdse.coordinator;

import ru.csc.bdse.kv.KeyValueApi;
import ru.csc.bdse.partitioning.Partitioner;

import java.util.Map;

public class Config {
    private final Map<String, KeyValueApi> apis;
    private final long timeoutMills;
    private final int writeConsistencyLevel;
    private final int readConsistencyLevel;
    private final Partitioner partitioner;

    public Config(Map<String, KeyValueApi> apis,
                  long timeoutMills,
                  int writeConsistencyLevel,
                  int readConsistencyLevel,
                  Partitioner partitioner) {
        this.apis = apis;
        this.timeoutMills = timeoutMills;
        this.writeConsistencyLevel = writeConsistencyLevel;
        this.readConsistencyLevel = readConsistencyLevel;
        this.partitioner = partitioner;
    }

    public Partitioner partitioner() {
        return partitioner;
    }

    public Map<String, KeyValueApi> apis() {
        return apis;
    }

    public long timeoutMills() {
        return timeoutMills;
    }

    public int writeConsistencyLevel() {
        return writeConsistencyLevel;
    }

    public int readConsistencyLevel() {
        return readConsistencyLevel;
    }
}
