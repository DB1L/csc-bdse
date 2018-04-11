package ru.csc.bdse.kv;

import org.junit.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import ru.csc.bdse.util.DockerUtils;

/**
 * Test have to be implemented
 *
 * @author alesavin
 */
public class KeyValueApiHttpClientNonFunctionalTest extends AbstractKeyValueApiTest {

    private static final String NODE_NAME = "node-0";
    private static GenericContainer redis;
    private static GenericContainer node;

    @BeforeClass
    public static void setUp() {
        final Network network = Network.newNetwork();

        final String redisHost = "redis";
        redis = DockerUtils.redis(network, redisHost);
        redis.start();

        node = DockerUtils.nodeWithRedis(network, NODE_NAME, redisHost);
        node.start();
    }

    @AfterClass
    public static void tearDown() {
        node.stop();
        redis.stop();
    }

    @Override
    protected KeyValueApi newKeyValueApi() {
        return new KeyValueApiHttpClient("http://localhost:" + node.getMappedPort(8080));
    }
}


