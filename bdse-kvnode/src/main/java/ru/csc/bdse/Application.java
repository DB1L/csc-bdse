package ru.csc.bdse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.csc.bdse.coordinator.CoordinatorConfig;
import ru.csc.bdse.kv.InMemoryKeyValueApi;
import ru.csc.bdse.kv.KeyValueApi;
import ru.csc.bdse.kv.KeyValueApiHttpClient;
import ru.csc.bdse.kv.RedisKeyValueApi;
import ru.csc.bdse.partitioning.PartitionConfig;
import ru.csc.bdse.partitioning.PartitionController;
import ru.csc.bdse.util.KvEnv;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private static String randomNodeName() {
        return "kvnode-" + UUID.randomUUID().toString().substring(4);
    }

    @Bean
    String nodeName() {
        return KvEnv.get(KvEnv.KVNODE_NAME).orElseGet(Application::randomNodeName);
    }

    @Bean
    KeyValueApi localApi(String nodeName) {
        final boolean inMemory = KvEnv.get(KvEnv.IN_MEMORY).map(Boolean::parseBoolean).orElse(false);
        if (inMemory) {
            return new InMemoryKeyValueApi(nodeName);
        } else {
            final String redisHostname = KvEnv.get(KvEnv.REDIS_HOSTNAME).orElse("localhost");
            final int redisPort = KvEnv.get(KvEnv.REDIS_PORT).map(Integer::parseInt).orElse(6379);
            return new RedisKeyValueApi(nodeName, redisHostname, redisPort);
        }
    }

    @Bean
    CoordinatorConfig coordinatorConfig(String nodeName, KeyValueApi localApi) {
        final List<String> nodesNames = KvEnv.get(KvEnv.HOSTS)
                .map(h -> Arrays.stream(h.split(",")))
                .orElse(Stream.empty())
                .collect(Collectors.toList());

        final int wcl = KvEnv.get(KvEnv.WCL).map(Integer::parseInt).orElse(nodesNames.size());
        final int rcl = KvEnv.get(KvEnv.RCL).map(Integer::parseInt).orElse(nodesNames.size());
        final int timeoutMills = KvEnv.get(KvEnv.TIMEOUT_MILLS)
                .map(Integer::parseInt).orElse(10000);

        final Map<String, KeyValueApi> apis = nodesNames.stream()
                .collect(Collectors.toMap(
                        s -> s,
                        (Function<String, KeyValueApi>) s -> new KeyValueApiHttpClient("http://" + s + ":8080", timeoutMills)
                ));

        if (apis.isEmpty()) {
            apis.put(nodeName, localApi);
        }

        return new CoordinatorConfig(apis, timeoutMills, wcl, rcl);
    }

    /*@Bean
    PartitionConfig partitionConfig(KeyValueApi localApi, CoordinatorConfig config) {
        final List<String> nodesPaths = KvEnv.get(KvEnv.HOSTS)
                .map(h -> Arrays.stream(h.split(",")))
                .orElse(Stream.empty())
                .collect(Collectors.toList());

        final int timeoutMills = KvEnv.get(KvEnv.TIMEOUT_MILLS)
                .map(Integer::parseInt).orElse(10000);

        final List<KeyValueApi> apis = nodesPaths.stream()
                .map(p -> new KeyValueApiHttpClient(p, timeoutMills))
                .collect(Collectors.toList());

        if (apis.isEmpty()) {
            apis.add(localApi);
        }

        return new PartitionConfig(apis, timeoutMills);
    }*/
}
