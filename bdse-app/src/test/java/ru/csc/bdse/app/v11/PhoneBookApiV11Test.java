package ru.csc.bdse.app.v11;

import ru.csc.bdse.app.PhoneBookApi;
import ru.csc.bdse.app.PhoneBookApiTest;
import ru.csc.bdse.kv.InMemoryKeyValueApi;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang.RandomStringUtils.randomNumeric;

public class PhoneBookApiV11Test extends PhoneBookApiTest<RecordV11> {
    public static final Supplier<RecordV11> GENERATOR = () ->
            new RecordV11(
                    randomAlphabetic(10),
                    randomAlphabetic(10),
                    randomAlphabetic(10),
                    Stream.generate(() -> randomNumeric(10)).limit(10).collect(Collectors.toList())
            );

    @Override
    protected Supplier<RecordV11> generator() {
        return GENERATOR;
    }

    @Override
    protected PhoneBookApi<RecordV11> newApi() {
        return new PhoneBookApiV11(new InMemoryKeyValueApi("test"));
    }
}
