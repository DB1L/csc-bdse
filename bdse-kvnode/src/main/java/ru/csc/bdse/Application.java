package ru.csc.bdse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.csc.bdse.kv.InMemoryKeyValueApi;
import ru.csc.bdse.kv.KeyValueApi;
import ru.csc.bdse.kv.RedisKeyValueApi;
import ru.csc.bdse.util.Env;

import java.util.UUID;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private static String randomNodeName() {
        return "kvnode-" + UUID.randomUUID().toString().substring(4);
    }

    @Bean
    KeyValueApi node() {
        final String nodeName = Env.get(Env.KVNODE_NAME).orElseGet(Application::randomNodeName);
        final boolean inMemory = Env.get(Env.IN_MEMORY).map(Boolean::parseBoolean).orElse(false);

        if (inMemory) {
            return new InMemoryKeyValueApi(nodeName);
        } else {
            final String redisHostname = Env.get(Env.REDIS_HOSTNAME).orElse("localhost");
            final int redisPort = Env.get(Env.REDIS_PORT).map(Integer::parseInt).orElse(6379);
            return new RedisKeyValueApi(nodeName, redisHostname, redisPort);
        }
    }
}
