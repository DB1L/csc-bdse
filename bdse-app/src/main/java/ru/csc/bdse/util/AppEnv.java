package ru.csc.bdse.util;

import java.util.Optional;

public class AppEnv {
    private AppEnv() {

    }

    public static final String KVNODE_URL = "KVNODE_URL";
    public static final String PHONE_BOOK_VERSION = "PHONE_BOOK_VERSION";

    public static Optional<String> get(final String name) {
        return Optional.ofNullable(System.getenv(name));
    }
}
