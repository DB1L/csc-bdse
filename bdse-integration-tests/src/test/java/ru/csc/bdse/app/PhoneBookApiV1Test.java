package ru.csc.bdse.app;

import org.junit.After;
import org.junit.Before;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import ru.csc.bdse.app.v1.PhoneBookV1Client;
import ru.csc.bdse.app.v1.RecordV1;
import ru.csc.bdse.util.DockerUtils;

import java.util.function.Supplier;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang.RandomStringUtils.randomNumeric;

public class PhoneBookApiV1Test extends AbstractPhoneBookFunctionalTest<RecordV1> {
    private static final String FIRST_NAME = randomAlphabetic(10);
    private static final String LAST_NAME = randomAlphabetic(10);
    private static final Supplier<RecordV1> SAME_KEY_GENERATOR = () ->
            new RecordV1(
                    FIRST_NAME,
                    LAST_NAME,
                    randomNumeric(10)
            );
    public static final Supplier<RecordV1> RANDOM_GENERATOR = () ->
            new RecordV1(
                    randomAlphabetic(10),
                    randomAlphabetic(10),
                    randomNumeric(10)
            );

    private GenericContainer redis;
    private GenericContainer node;
    private GenericContainer app;

    @Override
    protected Supplier<RecordV1> randomGenerator() {
        return RANDOM_GENERATOR;
    }

    @Override
    protected Supplier<RecordV1> sameKeyGenerator() {
        return SAME_KEY_GENERATOR;
    }

    @Before
    public void setUp() {
        final Network network = Network.newNetwork();

        final String redisHost = "redis";
        redis = DockerUtils.redis(network, redisHost);
        redis.start();

        final String nodeName = "node-0";
        final int nodePort = 8080;
        node = DockerUtils.nodeWithRedis(network, nodeName, nodePort, redisHost);
        node.start();

        app = DockerUtils.app(network, 8090, nodeName, nodePort, "1.0");
        app.start();

        super.setUp();
    }

    @After
    public void tearDown() {
        app.close();
        node.close();
        redis.close();
    }

    @Override
    protected PhoneBookApi<RecordV1> newPhoneBookApi() {
        return new PhoneBookV1Client("http://localhost:" + app.getMappedPort(8090));
    }
}
