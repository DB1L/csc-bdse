package ru.csc.bdse.coordinator;

import ru.csc.bdse.kv.KeyValueApi;
import ru.csc.bdse.kv.NodeAction;
import ru.csc.bdse.kv.NodeInfo;
import ru.csc.bdse.resolver.ConflictResolver;
import ru.csc.bdse.resolver.LastWriteWinsResolver;
import ru.csc.bdse.util.Constants;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CoordinatorKeyValueApi implements KeyValueApi {
    private final ExecutorService executorService = new ForkJoinPool();
    private final ConflictResolver conflictResolver = new LastWriteWinsResolver();

    private final KeyValueApi[] nodes;
    private final long timeoutMills;
    private final int writeConsistencyLevel;
    private final int readConsistencyLevel;

    CoordinatorKeyValueApi(Collection<KeyValueApi> nodes,
                           long timeoutMills,
                           int writeConsistencyLevel,
                           int readConsistencyLevel) {
        this.nodes = nodes.toArray(new KeyValueApi[nodes.size()]);
        this.timeoutMills = timeoutMills;
        this.writeConsistencyLevel = writeConsistencyLevel;
        this.readConsistencyLevel = readConsistencyLevel;
    }

    @Override
    public void put(String key, byte[] value) {
        executeWithQuorum(
                (BiFunction<KeyValueApi, Integer, Void>) (node, num) -> {
                    final RecordWithTimestamp record = new RecordWithTimestamp(
                            value,
                            System.nanoTime(),
                            false,
                            num);
                    node.put(key, record.encode());
                    return null;
                },
                writeConsistencyLevel
        );
    }

    @Override
    public Optional<byte[]> get(String key) {
        final Collection<RecordWithTimestamp> records = executeWithQuorum(
                (node, i) -> node.get(key).map(RecordWithTimestamp::decode),
                readConsistencyLevel
        ).stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        if (records.isEmpty()) {
            return Optional.empty();
        }

        final RecordWithTimestamp result = conflictResolver.resolve(records);
        if (result.isDeleted()) {
            return Optional.empty();
        } else {
            return Optional.of(result.payload());
        }
    }

    @Override
    public Set<String> getKeys(String prefix) {
        final Collection<Set<String>> quorumResult = executeWithQuorum((node, i) -> {
            final Set<String> keys = node.getKeys(prefix);
            return keys.stream().filter(key -> {
                final Optional<byte[]> get = node.get(key);
                return get.filter(bytes -> !RecordWithTimestamp.decode(bytes).isDeleted()).isPresent();
            }).collect(Collectors.toSet());
        }, readConsistencyLevel);
        return conflictResolver.resolveKeys(quorumResult);
    }

    @Override
    public void delete(String key) {
        executeWithQuorum((node, num) -> {
            final RecordWithTimestamp tumbstone = new RecordWithTimestamp(
                    Constants.EMPTY_BYTE_ARRAY,
                    System.nanoTime(),
                    true,
                    num);
            node.put(key, tumbstone.encode());
            return null;
        }, writeConsistencyLevel);
    }

    @Override
    public Set<NodeInfo> getInfo() {
        return Arrays.stream(nodes).flatMap(n -> n.getInfo().stream()).collect(Collectors.toSet());
    }

    @Override
    public void action(String node, NodeAction action) {
        Arrays.stream(nodes).forEach(n -> n.action(node, action));
    }

    private <R> Collection<R> executeWithQuorum(BiFunction<KeyValueApi, Integer, R> action, int quorum) {
        final CountDownLatch latch = new CountDownLatch(quorum);
        final Collection<R> result = new ArrayList<>();
        IntStream.range(0, nodes.length).forEach(i -> executorService.submit(() -> {
            final R apply = action.apply(nodes[i], i);
            synchronized (latch) {
                if (latch.getCount() > 0) {
                    result.add(apply);
                    latch.countDown();
                }
            }
        }));

        try {
            latch.await(timeoutMills, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        synchronized (latch) {
            if (latch.getCount() != 0) {
                throw new RuntimeException("Timeout has been exceeded");
            }
        }
        return result;
    }
}
