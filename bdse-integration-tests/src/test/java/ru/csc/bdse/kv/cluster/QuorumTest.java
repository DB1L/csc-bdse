package ru.csc.bdse.kv.cluster;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.csc.bdse.kv.KeyValueApi;
import ru.csc.bdse.kv.NodeAction;
import ru.csc.bdse.kv.NodeInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class QuorumTest extends AbstractClusterTest {

    void putNodesDown(int n) {
        final KeyValueApi api = newKeyValueApi();
        final List<NodeInfo> collect = new ArrayList<>(api.getInfo());
        Collections.shuffle(collect);
        collect.stream().limit(n).forEach(node -> api.action(node.getName(), NodeAction.DOWN));
    }

    @Override
    protected String controller() {
        return "coordinator";
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
