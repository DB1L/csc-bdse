package ru.csc.bdse.kv.cluster;

import org.junit.BeforeClass;
import org.testcontainers.containers.GenericContainer;
import ru.csc.bdse.coordinator.CoordinatorHttpApiClient;
import ru.csc.bdse.kv.KeyValueApi;
import ru.csc.bdse.util.KvEnv;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PartitioningTests extends AbstractClusterTest {
    protected KeyValueApi newKeyValueApi(GenericContainer[] containers) {
        final List<String> nodes = Arrays.stream(containers)
                .map(n -> n.getMappedPort(8080))
                .map(port -> "http://localhost:" + port).collect(Collectors.toList());
        return new CoordinatorHttpApiClient(nodes, controller());
    }

    static GenericContainer[] cluster(int size, KvEnv.Partitioners partitioner) {
        return cluster(size, 1, 1, partitioner);
    }

    @Override
    protected String controller() {
        return "partition";
    }

    public static class FirstLetterPartitionerTest extends PartitioningTests {
        @BeforeClass
        public static void setUp() {
            node = cluster(3, KvEnv.Partitioners.FIRST_LETTER);
        }

        /*@Test
        public void test() {
            final KeyValueApi api = newKeyValueApi();
            for (int i = 0; i < 1000; i++) {
                api.put(UUID.randomUUID().toString(), UUID.randomUUID().toString().getBytes());
            }


        }*/
    }
}
