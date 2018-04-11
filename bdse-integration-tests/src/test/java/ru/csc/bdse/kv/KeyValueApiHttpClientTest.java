package ru.csc.bdse.kv;

import org.junit.ClassRule;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import ru.csc.bdse.util.DockerUtils;

/**
 * @author semkagtn
 */
public class KeyValueApiHttpClientTest extends AbstractKeyValueApiTest {

    @ClassRule
    public static final GenericContainer node = DockerUtils.nodeInMemory(Network.newNetwork(), "node-0");

    @Override
    protected KeyValueApi newKeyValueApi() {
        return new KeyValueApiHttpClient("http://localhost:" + node.getMappedPort(8080));
    }
}
