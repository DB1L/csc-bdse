package ru.csc.bdse.util;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.images.RemoteDockerImage;
import org.testcontainers.images.builder.ImageFromDockerfile;
import ru.csc.bdse.kv.RedisKeyValueApiTest;

import java.io.File;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

public class DockerUtils {
    private static final int STARTUP_TIMEOUT_SEC = 30;

    public static GenericContainer redis(Network network, String networkAlias) {
        return new GenericContainer(new RemoteDockerImage("redis", "latest"))
                .withCommand("redis-server", "--appendonly", "yes")
                .withExposedPorts(RedisKeyValueApiTest.REDIS_PORT)
                .withNetwork(network)
                .withNetworkAliases(networkAlias)
                .withStartupTimeout(Duration.of(STARTUP_TIMEOUT_SEC, SECONDS));
    }

    public static GenericContainer nodeWithRedis(Network network, String nodeAlias, int port, String redisHostName) {
        return new GenericContainer(
                new ImageFromDockerfile()
                        .withFileFromFile("target/bdse-kvnode-0.0.1-SNAPSHOT.jar", new File
                                ("../bdse-kvnode/target/bdse-kvnode-0.0.1-SNAPSHOT-boot.jar"))
                        .withFileFromClasspath("Dockerfile", "kvnode/Dockerfile"))
                .withEnv(KvEnv.KVNODE_NAME, nodeAlias)
                .withEnv(KvEnv.REDIS_HOSTNAME, redisHostName)
                .withEnv(KvEnv.REDIS_PORT, String.valueOf(RedisKeyValueApiTest.REDIS_PORT))
                .withExposedPorts(port)
                .withNetwork(network)
                .withNetworkAliases(nodeAlias)
                .withLogConsumer(f -> System.out.print(((OutputFrame) f).getUtf8String()))
                .withStartupTimeout(Duration.of(STARTUP_TIMEOUT_SEC, SECONDS));
    }

    public static GenericContainer app(Network network, int port, String nodeAlias, int nodePort, String version) {
        return new GenericContainer(
                new ImageFromDockerfile()
                        .withFileFromFile("target/bdse-app-0.0.1-SNAPSHOT.jar", new File
                                ("../bdse-app/target/bdse-app-0.0.1-SNAPSHOT-boot.jar"))
                        .withFileFromClasspath("Dockerfile", "app/Dockerfile"))
                .withEnv(AppEnv.KVNODE_URL, "http://" + nodeAlias + ":" + nodePort)
                .withEnv(AppEnv.PHONE_BOOK_VERSION, version)
                .withExposedPorts(port)
                .withNetwork(network)
                .withLogConsumer(f -> System.out.print(((OutputFrame) f).getUtf8String()))
                .withStartupTimeout(Duration.of(STARTUP_TIMEOUT_SEC, SECONDS));
    }
}
