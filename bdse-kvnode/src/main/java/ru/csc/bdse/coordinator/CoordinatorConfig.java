package ru.csc.bdse.coordinator;

import ru.csc.bdse.kv.KeyValueApi;

import java.util.List;
import java.util.Map;

public class CoordinatorConfig {
    private final Map<String, KeyValueApi> apis;
    private final long timeoutMills;
    private final int writeConsistencyLevel;
    private final int readConsistencyLevel;

    public CoordinatorConfig(Map<String, KeyValueApi> apis,
                             long timeoutMills,
                             int writeConsistencyLevel,
                             int readConsistencyLevel) {
        this.apis = apis;
        this.timeoutMills = timeoutMills;
        this.writeConsistencyLevel = writeConsistencyLevel;
        this.readConsistencyLevel = readConsistencyLevel;
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
