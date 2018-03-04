package ru.csc.bdse.util;

import java.util.Optional;

/**
 * @author semkagtn
 */
public class Env {

    private Env() {

    }

    public static final String KVNODE_NAME = "KVNODE_NAME";
    public static final String IN_MEMORY = "IN_MEMORY";
    public static final String REDIS_HOSTNAME = "REDIS_HOSTNAME";
    public static final String REDIS_PORT = "REDIS_PORT";

    public static Optional<String> get(final String name) {
        return Optional.ofNullable(System.getenv(name));
    }
}
