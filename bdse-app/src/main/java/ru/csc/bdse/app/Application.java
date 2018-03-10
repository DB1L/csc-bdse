package ru.csc.bdse.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.csc.bdse.kv.InMemoryKeyValueApi;
import ru.csc.bdse.kv.KeyValueApi;

import java.util.Collections;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        final SpringApplication application = new SpringApplication(Application.class);
        application.setDefaultProperties(Collections.singletonMap("phone.version", "1.0"));
        application.run(args);
    }

    @Bean
    KeyValueApi node() {
        return new InMemoryKeyValueApi("a");
    }
}
