package ru.csc.bdse.app;

import org.junit.After;
import org.junit.Before;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import ru.csc.bdse.app.v11.PhoneBookV11Client;
import ru.csc.bdse.app.v11.RecordV11;
import ru.csc.bdse.util.DockerUtils;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang.RandomStringUtils.randomNumeric;

public class PhoneBookApiV11Test extends AbstractPhoneBookFunctionalTest<RecordV11> {
    private static final int APP_PORT = 8799;

    private static final String NICK_NAME = randomAlphabetic(10);
    private static final String FIRST_NAME = randomAlphabetic(10);
    private static final String LAST_NAME = randomAlphabetic(10);
    private static final Supplier<RecordV11> SAME_KEY_GENERATOR = () ->
            new RecordV11(
                    NICK_NAME,
                    FIRST_NAME,
                    LAST_NAME,
                    Stream.generate(() -> randomNumeric(10)).limit(10).collect(Collectors.toList())
            );
    public static final Supplier<RecordV11> RANDOM_GENERATOR = () ->
            new RecordV11(
                    randomAlphabetic(10),
                    randomAlphabetic(10),
                    randomAlphabetic(10),
                    Stream.generate(() -> randomNumeric(10)).limit(10).collect(Collectors.toList())
            );

    private GenericContainer redis;
    private GenericContainer node;
    private GenericContainer app;

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

        app = DockerUtils.app(network, APP_PORT, nodeName, nodePort, "1.1");
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
    protected Supplier<RecordV11> randomGenerator() {
        return RANDOM_GENERATOR;
    }

    @Override
    protected Supplier<RecordV11> sameKeyGenerator() {
        return SAME_KEY_GENERATOR;
    }

    @Override
    protected PhoneBookApi<RecordV11> newPhoneBookApi() {
        return new PhoneBookV11Client("http://localhost:" + app.getMappedPort(APP_PORT));
    }
}
