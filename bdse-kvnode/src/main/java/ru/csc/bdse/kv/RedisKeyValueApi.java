package ru.csc.bdse.kv;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class RedisKeyValueApi implements KeyValueApi {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final JedisPool jedisPool;
    private final String nodeId;

    private NodeStatus status = NodeStatus.UP;

    public RedisKeyValueApi(String nodeId, String host, int port) {
        this.nodeId = nodeId;
        this.jedisPool = new JedisPool(host, port);
    }

    @Override
    public void put(String key, byte[] value) {
        withExclusiveJedis(j -> j.set(key.getBytes(), value));
    }

    @Override
    public Optional<byte[]> get(String key) {
        return withExclusiveJedis(j -> Optional.ofNullable(j.get(key.getBytes())));
    }

    @Override
    public Set<String> getKeys(String prefix) {
        return withExclusiveJedis(j -> j.keys(prefix + "*"));
    }

    @Override
    public void delete(String key) {
        withExclusiveJedis(j -> j.del(key.getBytes()));
    }

    @Override
    public Set<NodeInfo> getInfo() {
        lock.readLock().lock();
        try {
            return Collections.singleton(new NodeInfo(nodeId, status));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void action(String node, NodeAction action) {
        if (node.equals(nodeId)) {
            lock.writeLock().lock();
            try {
                switch (action) {
                    case DOWN:
                        status = NodeStatus.DOWN;
                        break;
                    case UP:
                        status = NodeStatus.UP;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid action \"" + action + "\" for node \"" + node + "\"");
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    private <R> R withExclusiveJedis(Function<Jedis, R> function) {
        lock.readLock().lock();
        try (final Jedis jedis = jedisPool.getResource()) {
            assertNodeIsUp();
            return function.apply(jedis);
        } finally {
            lock.readLock().unlock();
        }
    }

    private void assertNodeIsUp() {
        if (status == NodeStatus.DOWN) {
            throw new IllegalStateException("Node \"" + nodeId + "\" is down");
        }
    }
}
