package ru.csc.bdse.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.csc.bdse.kv.KeyValueApi;
import ru.csc.bdse.kv.KeyValueApiHttpClient;
import ru.csc.bdse.util.AppEnv;

import java.util.Collections;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        final SpringApplication application = new SpringApplication(Application.class);
        final String version = AppEnv.get(AppEnv.PHONE_BOOK_VERSION).orElse("1.0");
        application.setDefaultProperties(Collections.singletonMap("phone.version", version));
        application.run(args);
    }

    @Bean
    KeyValueApi node() {
        final String nodeUrl = AppEnv.get(AppEnv.KVNODE_URL).orElse("localhost:8080");
        return new KeyValueApiHttpClient(nodeUrl);
    }
}
