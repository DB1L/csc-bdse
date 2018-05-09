package ru.csc.bdse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.csc.bdse.coordinator.Config;
import ru.csc.bdse.kv.InMemoryKeyValueApi;
import ru.csc.bdse.kv.KeyValueApi;
import ru.csc.bdse.kv.KeyValueApiHttpClient;
import ru.csc.bdse.kv.RedisKeyValueApi;
import ru.csc.bdse.partitioning.FirstLetterPartitioner;
import ru.csc.bdse.partitioning.ModNPartitioner;
import ru.csc.bdse.partitioning.Partitioner;
import ru.csc.bdse.util.KvEnv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private static String randomNodeName() {
        return "kvnode-" + UUID.randomUUID().toString().substring(4);
    }

    @Bean
    KeyValueApi localApi() {
        final String nodeName = KvEnv.get(KvEnv.KVNODE_NAME).orElseGet(Application::randomNodeName);
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
    Config config() {
        final int timeoutMills = KvEnv.get(KvEnv.TIMEOUT_MILLS)
                .map(Integer::parseInt).orElse(10000);

        final String[] nodeEntries = KvEnv.get(KvEnv.HOSTS).orElseThrow(IllegalArgumentException::new).split(",");

        final Map<String, KeyValueApi> apis = new HashMap<>();
        for (String nodeEntry : nodeEntries) {
            final String[] s = nodeEntry.split("@");
            final String id = s[0];
            final KeyValueApi api = new KeyValueApiHttpClient(s[1], timeoutMills);
            apis.put(id, api);
        }

        final int wcl = KvEnv.get(KvEnv.WCL).map(Integer::parseInt).orElse(apis.size());
        final int rcl = KvEnv.get(KvEnv.RCL).map(Integer::parseInt).orElse(apis.size());

        final KvEnv.Partitioners kvPartitioner = KvEnv.Partitioners
                .fromString(KvEnv.get(KvEnv.PARTITIONER).orElse(KvEnv.Partitioners.CONSISTENT.toString()));


        final Partitioner partitioner;
        switch (kvPartitioner) {
            case MOD_N:
                partitioner = new ModNPartitioner(apis.keySet());
                break;
            case CONSISTENT:
                partitioner = new FirstLetterPartitioner(apis.keySet());
                break;
            case FIRST_LETTER:
                partitioner = new FirstLetterPartitioner(apis.keySet());
                break;
            default:
                throw new IllegalArgumentException("Partitioner is invalid");
        }

        return new Config(apis, timeoutMills, wcl, rcl, partitioner);
    }
}
