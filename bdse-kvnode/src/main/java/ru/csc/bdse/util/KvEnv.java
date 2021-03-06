package ru.csc.bdse.util;

import java.util.Optional;

/**
 * @author semkagtn
 */
public class KvEnv {

    private KvEnv() {

    }

    public static final String KVNODE_NAME = "KVNODE_NAME";
    public static final String IN_MEMORY = "IN_MEMORY";
    public static final String REDIS_HOSTNAME = "REDIS_HOSTNAME";
    public static final String REDIS_PORT = "REDIS_PORT";
    public static final String TIMEOUT_MILLS = "TIMEOUT_MILLS";
    public static final String WCL = "WCL";
    public static final String RCL = "RCL";
    public static final String HOSTS = "HOSTS";

    public static Optional<String> get(final String name) {
        return Optional.ofNullable(System.getenv(name));
    }
}
