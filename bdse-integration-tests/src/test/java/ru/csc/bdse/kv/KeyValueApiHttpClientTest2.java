package ru.csc.bdse.kv;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import ru.csc.bdse.util.DockerUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test have to be implemented
 *
 * @author alesavin
 */
public class KeyValueApiHttpClientTest2 extends AbstractKeyValueApiTest {
    private static final int ATTEMPTS = 1000;
    private static final int THREADS = 20;
    
    private static final String NODE_NAME = "node-0";
    private static final int NODE_PORT = 8080;

    private static GenericContainer redis;
    private static GenericContainer node;

    @BeforeClass
    public static void setUp() {
        final Network network = Network.newNetwork();

        final String redisHost = "redis";
        redis = DockerUtils.redis(network, redisHost);
        redis.start();

        node = DockerUtils.nodeWithRedis(network, NODE_NAME, NODE_PORT, redisHost);
        node.start();
    }

    @Before
    public void bringUp() {
        api.action(NODE_NAME, NodeAction.UP);
    }

    @AfterClass
    public static void tearDown() {
        node.stop();
        redis.stop();
    }

    private KeyValueApi api = newKeyValueApi();

    protected KeyValueApi newKeyValueApi() {
        final String baseUrl = "http://localhost:" + node.getMappedPort(NODE_PORT);
        return new KeyValueApiHttpClient(baseUrl);
    }

    @Test
    public void concurrentPuts() throws InterruptedException {
        final Thread[] threads = new Thread[THREADS];
        for (int i = 0; i < threads.length; ++i) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < ATTEMPTS; ++j) {
                    final byte[] value = String.valueOf(j).getBytes();
                    api.put("key", value);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        final Optional<byte[]> finalResult = api.get("key");
        Assert.assertTrue(finalResult.isPresent());

        Assert.assertEquals(String.valueOf(ATTEMPTS - 1), finalResult.map(String::new).orElse("NAN"));
    }

    @Test
    public void concurrentDeleteAndKeys() {
        final String prefix = "prefix";
        final List<String> keys = Stream.generate(UUID::randomUUID)
                .map(UUID::toString)
                .map(u -> prefix + u)
                .limit(ATTEMPTS)
                .collect(Collectors.toList());

        for (String key : keys) {
            api.put(key, key.getBytes());
        }

        final ExecutorService deleters = new ForkJoinPool(THREADS);
        for (String key : keys) {
            deleters.submit(() -> api.delete(key));
        }

        deleters.shutdown();

        while (!deleters.isTerminated()) {
            final Set<String> currentKeys = api.getKeys(prefix);
            Assert.assertTrue(keys.containsAll(currentKeys));
        }
    }

    @Test
    public void actionUpDown() {
        final NodeInfo nodeInfo = api.getInfo().stream().findAny().orElseThrow(IllegalStateException::new);
        Assert.assertEquals(NodeStatus.UP, nodeInfo.getStatus());

        for (int i = 0; i < 10; ++i) {
            api.action(nodeInfo.getName(), NodeAction.DOWN);
            final NodeInfo n1 = api.getInfo().stream().findAny().orElseThrow(IllegalStateException::new);
            Assert.assertEquals(NodeStatus.DOWN, n1.getStatus());

            api.action(nodeInfo.getName(), NodeAction.UP);
            final NodeInfo n2 = api.getInfo().stream().findAny().orElseThrow(IllegalStateException::new);
            Assert.assertEquals(NodeStatus.UP, n2.getStatus());
        }
    }

    @Test(expected = RuntimeException.class)
    public void putWithStoppedNode() {
        final NodeInfo nodeInfo = api.getInfo().stream().findAny().orElseThrow(IllegalStateException::new);
        api.action(nodeInfo.getName(), NodeAction.DOWN);

        api.put("key", "value".getBytes());
    }

    @Test(expected = RuntimeException.class)
    public void getWithStoppedNode() {
        final NodeInfo nodeInfo = api.getInfo().stream().findAny().orElseThrow(IllegalStateException::new);
        api.action(nodeInfo.getName(), NodeAction.DOWN);

        api.get("key");
    }

    @Test(expected = RuntimeException.class)
    public void getKeysByPrefixWithStoppedNode() {
        final NodeInfo nodeInfo = api.getInfo().stream().findAny().orElseThrow(IllegalStateException::new);
        api.action(nodeInfo.getName(), NodeAction.DOWN);

        api.getKeys("prefix");
    }

    @Test
    public void deleteByTombstone() {
        // TODO use tombstones to mark as deleted (optional)
    }

    @Test
    public void loadMillionKeys() throws InterruptedException {
        final int million = 1_000_000;
        final Thread[] threads = new Thread[THREADS];
        for (int i = 0; i < threads.length; ++i) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < million / THREADS; ++j) {
                    api.put(UUID.randomUUID().toString(), RandomStringUtils.randomAlphanumeric(50).getBytes());
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        Assert.assertEquals(api.getKeys("").size(), million);
    }
}


