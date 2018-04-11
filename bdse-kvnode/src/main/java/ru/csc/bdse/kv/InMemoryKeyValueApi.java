package ru.csc.bdse.kv;

import org.springframework.web.bind.annotation.RequestMapping;
import ru.csc.bdse.util.Require;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static ru.csc.bdse.kv.NodeAction.UP;

/**
 * Trivial in-memory implementation of the storage unit.
 *
 * @author semkagtn
 */
public class InMemoryKeyValueApi implements KeyValueApi {

    private final String name;
    private final ConcurrentMap<String, byte[]> map = new ConcurrentHashMap<>();
    private NodeStatus status = NodeStatus.UP;

    public InMemoryKeyValueApi(final String name) {
        Require.nonEmpty(name, "empty name");
        this.name = name;
    }

    @Override
    public void put(final String key, final byte[] value) {
        assertNodeIsUp();
        Require.nonEmpty(key, "empty key");
        Require.nonNull(value, "null value");
        map.put(key, value);
    }

    @Override
    public Optional<byte[]> get(final String key) {
        assertNodeIsUp();
        Require.nonEmpty(key, "empty key");
        return Optional.ofNullable(map.get(key));
    }

    @Override
    public Set<String> getKeys(String prefix) {
        assertNodeIsUp();
        Require.nonNull(prefix, "null prefix");
        return map.keySet()
                .stream()
                .filter(key -> key.startsWith(prefix))
                .collect(Collectors.toSet());
    }

    @Override
    public void delete(final String key) {
        assertNodeIsUp();
        Require.nonEmpty(key, "empty key");
        map.remove(key);
    }

    @Override
    public Set<NodeInfo> getInfo() {
        return Collections.singleton(new NodeInfo(name, status));
    }

    @Override
    public void action(String node, NodeAction action) {
        if (node.equals(name)) {
            if (action == UP) {
                status = NodeStatus.UP;
            } else {
                status = NodeStatus.DOWN;
            }
        }
    }

    private void assertNodeIsUp() {
        if (status == NodeStatus.DOWN) {
            throw new IllegalStateException("Node \"" + name + "\" is down");
        }
    }
}
