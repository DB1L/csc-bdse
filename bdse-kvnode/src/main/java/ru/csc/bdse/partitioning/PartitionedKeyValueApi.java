package ru.csc.bdse.partitioning;

import ru.csc.bdse.kv.KeyValueApi;
import ru.csc.bdse.kv.NodeAction;
import ru.csc.bdse.kv.NodeInfo;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class PartitionedKeyValueApi implements KeyValueApi {
    private final Map<String, KeyValueApi> partitions;
    private final Map<String, ExecutorService> executors;

    private final long timeoutInMillis;
    private final Partitioner partitioner;

    PartitionedKeyValueApi(Map<String, KeyValueApi> partitions, long timeoutInMillis, Partitioner partitioner) {
        this.partitions = partitions;
        this.timeoutInMillis = timeoutInMillis;
        this.partitioner = partitioner;
        this.executors = partitions.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, e -> Executors.newSingleThreadExecutor()));
    }


    @Override
    public void put(String key, byte[] value) {
        executePartitioned(partitioner.getPartition(key), keyValueApi -> {
            keyValueApi.put(key, value);
            return null;
        });
    }

    @Override
    public Optional<byte[]> get(String key) {
        return executePartitioned(partitioner.getPartition(key), keyValueApi -> keyValueApi.get(key));
    }

    @Override
    public Set<String> getKeys(String prefix) {
        return execute(keyValueApi -> keyValueApi.getKeys(prefix));
    }

    @Override
    public void delete(String key) {
        executePartitioned(partitioner.getPartition(key), keyValueApi -> {
            keyValueApi.delete(key);
            return null;
        });
    }

    @Override
    public Set<NodeInfo> getInfo() {
        return execute(KeyValueApi::getInfo);
    }

    @Override
    public void action(String node, NodeAction action) {
        executePartitioned(node, keyValueApi -> {
            keyValueApi.action(node, action);
            return null;
        });
    }

    private <R> R executePartitioned(String partition, Function<KeyValueApi, R> function) {
        try {
            return executors.get(partition).submit(
                    () -> function.apply(partitions.get(partition))
            ).get(timeoutInMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private <R> Set<R> execute(Function<KeyValueApi, Set<R>> function) {
        final CountDownLatch latch = new CountDownLatch(partitions.size());
        final Set<R> result = new ConcurrentSkipListSet<>();
        final List<Future> futures = new ArrayList<>();
        executors.forEach((s, executorService) -> futures.add(executorService.submit(() -> {
            result.addAll(function.apply(partitions.get(s)));
            latch.countDown();
        })));

        try {
            latch.await(timeoutInMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            futures.forEach(future -> future.cancel(true));
        }
        return result;
    }
}
