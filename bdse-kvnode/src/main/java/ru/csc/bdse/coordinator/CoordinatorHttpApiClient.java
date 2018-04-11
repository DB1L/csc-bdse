package ru.csc.bdse.coordinator;

import ru.csc.bdse.kv.KeyValueApi;
import ru.csc.bdse.kv.KeyValueApiHttpClient;
import ru.csc.bdse.kv.NodeAction;
import ru.csc.bdse.kv.NodeInfo;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CoordinatorHttpApiClient implements KeyValueApi {
    public static final int ATTEMPTS = 10;
    private final List<KeyValueApi> apis;
    private int currentCoordinator = 0;

    public CoordinatorHttpApiClient(List<String> coordinators) {
        apis = coordinators.stream()
                .map(c -> new KeyValueApiHttpClient(c + "/coordinator"))
                .collect(Collectors.toList());
    }

    @Override
    public void put(String key, byte[] value) {
        tryWithCoordinators(a -> {
            a.put(key, value);
            return true;
        });
    }

    @Override
    public Optional<byte[]> get(String key) {
        return tryWithCoordinators(a -> a.get(key));
    }

    @Override
    public Set<String> getKeys(String prefix) {
        return tryWithCoordinators(a -> a.getKeys(prefix));
    }

    @Override
    public void delete(String key) {
        tryWithCoordinators(a -> {
            a.delete(key);
            return true;
        });
    }

    @Override
    public Set<NodeInfo> getInfo() {
        return tryWithCoordinators(KeyValueApi::getInfo);
    }

    @Override
    public void action(String node, NodeAction action) {
        tryWithCoordinators(a -> {
            a.action(node, action);
            return true;
        });
    }

    private <R> R tryWithCoordinators(Function<KeyValueApi, R> action) {
        RuntimeException lastException = new IllegalStateException("Default exception");

        for (int attempt = 0; attempt < ATTEMPTS; currentCoordinator = (currentCoordinator + 1) % apis.size(), ++attempt) {
            try {
                return action.apply(apis.get(currentCoordinator));
            } catch (RuntimeException e) {
                lastException = e;
            }
        }
        throw lastException;
    }
}
