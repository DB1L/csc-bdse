package ru.csc.bdse.app.v1;

import ru.csc.bdse.app.PhoneBookApi;
import ru.csc.bdse.app.PhoneBookApiTest;
import ru.csc.bdse.kv.InMemoryKeyValueApi;

import java.util.function.Supplier;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang.RandomStringUtils.randomNumeric;

public class PhoneBookApiV1Test extends PhoneBookApiTest<RecordV1> {
    public static final Supplier<RecordV1> GENERATOR = () ->
            new RecordV1(
                    randomAlphabetic(10),
                    randomAlphabetic(10),
                    randomNumeric(10)
            );

    @Override
    public Supplier<RecordV1> generator() {
        return GENERATOR;
    }

    @Override
    public PhoneBookApi<RecordV1> newApi() {
        return new PhoneBookApiV1(new InMemoryKeyValueApi("test"));
    }
}
