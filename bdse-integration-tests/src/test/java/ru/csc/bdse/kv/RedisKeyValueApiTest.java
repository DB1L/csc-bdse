package ru.csc.bdse.kv;

import org.junit.ClassRule;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.RemoteDockerImage;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

public class RedisKeyValueApiTest extends AbstractKeyValueApiTest {
    public static final int REDIS_PORT = 6379;

    @ClassRule
    public static final GenericContainer redis = new GenericContainer(new RemoteDockerImage("redis", "latest"))
            .withCommand("redis-server", "--appendonly", "yes")
            .withExposedPorts(REDIS_PORT)
            .withStartupTimeout(Duration.of(30, SECONDS));

    @Override
    protected KeyValueApi newKeyValueApi() {
        return new RedisKeyValueApi(
                "redis-node-0",
                redis.getContainerIpAddress(),
                redis.getMappedPort(REDIS_PORT)
        );
    }
}
