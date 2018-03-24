package ru.csc.bdse.app;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import ru.csc.bdse.app.v1.PhoneBookV1Client;
import ru.csc.bdse.app.v1.RecordV1;
import ru.csc.bdse.app.v11.PhoneBookV11Client;
import ru.csc.bdse.app.v11.RecordV11;
import ru.csc.bdse.util.DockerUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Test have to be implemented
 *
 * @author alesavin
 */
public class PhoneBookCompatibilitiesTest {
    private PhoneBookApi<RecordV1> apiV1;
    private PhoneBookApi<RecordV11> apiV11;

    private GenericContainer redis;
    private GenericContainer node;
    private GenericContainer appV1;
    private GenericContainer appV11;

    @Before
    public void setUp() {
        final Network network = Network.newNetwork();

        final String redisHost = "redis";
        redis = DockerUtils.redis(network, redisHost);
        redis.start();

        final String nodeName = "node-0";
        node = DockerUtils.nodeWithRedis(network, nodeName, redisHost);
        node.start();

        appV1 = DockerUtils.app(network, nodeName, "1.0");
        appV1.start();

        appV11 = DockerUtils.app(network, nodeName, "1.1");
        appV11.start();

        apiV1 = new PhoneBookV1Client("http://localhost:" + appV1.getMappedPort(8080));
        apiV11 = new PhoneBookV11Client("http://localhost:" + appV11.getMappedPort(8080));
    }

    @After
    public void tearDown() {
        apiV1 = null;
        apiV11 = null;

        appV1.close();
        appV11.close();
        node.close();
        redis.close();
    }

    @Test
    public void write10read11() {
        final RecordV1 record = PhoneBookApiV1Test.RANDOM_GENERATOR.get();
        apiV1.put(record);

        //noinspection ConstantConditions
        final Set<RecordV11> records = apiV11.get(record.literals().stream().findFirst().get());
        Assert.assertEquals(Collections.singleton(forwardMap(record)), records);
    }

    @Test
    public void write11read10() {
        final RecordV11 record = PhoneBookApiV11Test.RANDOM_GENERATOR.get();
        apiV11.put(record);

        //noinspection ConstantConditions
        final Set<RecordV1> records = apiV1.get(record.literals().stream().findFirst().get());
        Assert.assertEquals(Collections.singleton(backwardMap(record)), records);
    }

    @Test
    public void write10erasure11() {
        final RecordV1 record = PhoneBookApiV1Test.RANDOM_GENERATOR.get();
        apiV1.put(record);

        //noinspection ConstantConditions
        final Set<RecordV11> records = apiV11.get(record.literals().stream().findFirst().get());
        records.forEach(apiV11::delete);

        //noinspection ConstantConditions
        final Set<RecordV1> recordV1s = apiV1.get(record.literals().stream().findAny().get());
        Assert.assertEquals(Collections.emptySet(), recordV1s);
    }

    private RecordV11 forwardMap(RecordV1 recordV1) {
        return new RecordV11(
                "",
                recordV1.firstName(),
                recordV1.lastName(),
                Collections.singletonList(recordV1.phone())
        );
    }

    private RecordV1 backwardMap(RecordV11 recordV11) {
        final List<String> phones = recordV11.phones().collect(Collectors.toList());
        return new RecordV1(
                recordV11.firstName(),
                recordV11.lastName(),
                phones.get(phones.size() - 1)
        );
    }
}