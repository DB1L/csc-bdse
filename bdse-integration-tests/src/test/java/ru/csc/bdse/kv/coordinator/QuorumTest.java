package ru.csc.bdse.kv.coordinator;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import ru.csc.bdse.coordinator.CoordinatorHttpApiClient;
import ru.csc.bdse.kv.AbstractKeyValueApiTest;
import ru.csc.bdse.kv.KeyValueApi;
import ru.csc.bdse.kv.NodeAction;
import ru.csc.bdse.kv.NodeInfo;
import ru.csc.bdse.util.DockerUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class QuorumTest extends AbstractKeyValueApiTest {
    protected static GenericContainer[] node;

    @AfterClass
    public static void tearDown() {
        for (GenericContainer genericContainer : node) {
            genericContainer.stop();
        }
    }

    @Override
    protected KeyValueApi newKeyValueApi() {
        final List<String> nodes = Arrays.stream(node)
                .map(n -> n.getMappedPort(8080))
                .map(port -> "http://localhost:" + port).collect(Collectors.toList());
        return new CoordinatorHttpApiClient(nodes);
    }

    protected static GenericContainer[] cluster(int size, int wcl, int rcl) {
        final Network network = Network.newNetwork();
        final GenericContainer[] clulster = new GenericContainer[size];
        final List<String> nodes = IntStream.range(0, size)
                .mapToObj(id -> "node" + id)
                .collect(Collectors.toList());
        for (int i = 0; i < size; i++) {
            clulster[i] = DockerUtils.nodeInMemory(network, "node" + i, nodes, 1000, wcl, rcl);
            clulster[i].start();
        }
        return clulster;
    }

    ;

    protected void putNodesDown(int n) {
        final KeyValueApi api = newKeyValueApi();
        final List<NodeInfo> collect = new ArrayList<>(api.getInfo());
        Collections.shuffle(collect);
        collect.stream().limit(n).forEach(node -> api.action(node.getName(), NodeAction.DOWN));
    }

    public static class RF5WCL3RCL3OneDown extends QuorumTest {
        @BeforeClass
        public static void setUp() {
            node = cluster(5, 3, 3);
        }

        @Before
        public void putNodes() {
            putNodesDown(1);
        }
    }

    public static class RF5WCL3RCL3 extends QuorumTest {
        @BeforeClass
        public static void setUp() {
            node = cluster(5, 3, 3);
        }

        @Before
        public void putNodes() {
            putNodesDown(2);
        }
    }

    public static class RF1WCL1RCL1 extends QuorumTest {
        @BeforeClass
        public static void setUp() {
            node = cluster(1, 1, 1);
        }
    }

    public static class RF3WCL2RCL2 extends QuorumTest {
        @BeforeClass
        public static void setUp() {
            node = cluster(3, 2, 2);
        }

        @Before
        public void putNodes() {
            putNodesDown(1);
        }
    }

    public static class RF3WCL2RCL3 extends QuorumTest {
        @BeforeClass
        public static void setUp() {
            node = cluster(3, 2, 3);
        }

        @Test(expected = RuntimeException.class)
        public void testReadWithDownNode() {
            putNodesDown(1);
            final KeyValueApi api = newKeyValueApi();
            api.put("key", "key".getBytes());
            api.get("key");
        }
    }

    public static class RF3WCL3RCL1 extends QuorumTest {
        @BeforeClass
        public static void setUp() {
            node = cluster(3, 3, 1);
        }

        @Test(expected = RuntimeException.class)
        public void testWriteWithDownNode() {
            putNodesDown(1);
            final KeyValueApi api = newKeyValueApi();
            api.put("key", "key".getBytes());
        }
    }
}
