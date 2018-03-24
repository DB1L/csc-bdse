package ru.csc.bdse.app;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import ru.csc.bdse.util.DockerUtils;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.Assert.fail;

/**
 * Test have to be implemented
 *
 * @author alesavin
 */
public abstract class PhoneBookNonFunctionalTest<R extends Record> {
    protected abstract Supplier<R> randomGenerator();

    protected abstract PhoneBookApi<R> newPhoneBookApi(int port);

    protected abstract String version();

    @Test
    public void putGetErasureWithStoppedNode() {
        final Network network = Network.newNetwork();
        //Node is not started
        final GenericContainer app = DockerUtils.app(network, "node-0", version());
        app.start();

        final PhoneBookApi<R> api = newPhoneBookApi(app.getMappedPort(8080));
        final R record = randomGenerator().get();

        try {
            api.put(record);
            fail("Exception should be thrown before");
        } catch (HttpServerErrorException e) {
            Assert.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.getStatusCode());
        }

        try {
            //noinspection ConstantConditions
            api.get(record.literals().stream().findFirst().get());
            fail("Exception should be thrown before");
        } catch (HttpServerErrorException e) {
            Assert.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.getStatusCode());
        }

        try {
            //noinspection ConstantConditions
            api.delete(record);
            fail("Exception should be thrown before");
        } catch (HttpServerErrorException e) {
            Assert.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.getStatusCode());
        }

        app.close();
    }

    @Test
    public void dataWasSavedIfAppRestarts() {
        final Network network = Network.newNetwork();

        final String redisHost = "redis";
        final GenericContainer redis = DockerUtils.redis(network, redisHost);
        redis.start();

        final String nodeName = "node-0";
        final GenericContainer node = DockerUtils.nodeWithRedis(network, nodeName, redisHost);
        node.start();

        final GenericContainer app = DockerUtils.app(network, nodeName, version());
        app.start();

        final R record = randomGenerator().get();
        final PhoneBookApi<R> api = newPhoneBookApi(app.getMappedPort(8080));
        api.put(record);

        app.stop();
        try {
            //noinspection ConstantConditions
            api.get(record.literals().stream().findFirst().get());
            fail("Exception should be thrown before");
        } catch (ResourceAccessException ignored) {
        }

        app.start();
        final PhoneBookApi<R> apiAfterRestart = newPhoneBookApi(app.getMappedPort(8080));
        //noinspection ConstantConditions
        final Set<R> result = apiAfterRestart.get(record.literals().stream().findFirst().get());
        Assert.assertEquals(Collections.singleton(record), result);

        app.close();
        node.close();
        redis.close();
    }

    @Test
    public void dataWasSavedIfKvNodeRestarts() {
        final Network network = Network.newNetwork();

        final String redisHost = "redis";
        final GenericContainer redis = DockerUtils.redis(network, redisHost);
        redis.start();

        final String nodeName = "node-0";
        final GenericContainer node = DockerUtils.nodeWithRedis(network, nodeName, redisHost);
        node.start();

        final GenericContainer app = DockerUtils.app(network, nodeName, version());
        app.start();

        final R record = randomGenerator().get();
        final PhoneBookApi<R> api = newPhoneBookApi(app.getMappedPort(8080));
        api.put(record);

        node.stop();
        try {
            //noinspection ConstantConditions
            api.get(record.literals().stream().findFirst().get());
            fail("Exception should be thrown before");
        } catch (HttpServerErrorException e) {
            Assert.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.getStatusCode());
        }

        node.start();
        //noinspection ConstantConditions
        final Set<R> result = api.get(record.literals().stream().findFirst().get());
        Assert.assertEquals(Collections.singleton(record), result);

        app.close();
        node.close();
        redis.close();
    }
}