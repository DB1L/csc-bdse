package ru.csc.bdse.kv.cluster;

import org.junit.AfterClass;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import ru.csc.bdse.coordinator.CoordinatorHttpApiClient;
import ru.csc.bdse.kv.AbstractKeyValueApiTest;
import ru.csc.bdse.kv.KeyValueApi;
import ru.csc.bdse.util.DockerUtils;
import ru.csc.bdse.util.KvEnv;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractClusterTest extends AbstractKeyValueApiTest {
    static GenericContainer[] node;

    @AfterClass
    public static void tearDown() {
        for (GenericContainer genericContainer : node) {
            genericContainer.stop();
        }
    }

    protected abstract String controller();

    @Override
    protected KeyValueApi newKeyValueApi() {
        final List<String> nodes = Arrays.stream(node)
                .map(n -> n.getMappedPort(8080))
                .map(port -> "http://localhost:" + port).collect(Collectors.toList());
        return new CoordinatorHttpApiClient(nodes, controller());
    }

    static GenericContainer[] cluster(int size, int wcl, int rcl, KvEnv.Partitioners partitioner) {
        final Network network = Network.newNetwork();
        final GenericContainer[] cluster = new GenericContainer[size];
        final List<String> nodes = IntStream.range(0, size)
                .mapToObj(id -> "node" + id)
                .collect(Collectors.toList());
        for (int i = 0; i < size; i++) {
            cluster[i] = DockerUtils.nodeInMemory(network, "node" + i, nodes, 5000, wcl, rcl, partitioner);
            cluster[i].start();
        }
        return cluster;
    }

    static GenericContainer[] cluster(int size, int wcl, int rcl) {
        return cluster(size, wcl, rcl, KvEnv.Partitioners.CONSISTENT);
    }
}
