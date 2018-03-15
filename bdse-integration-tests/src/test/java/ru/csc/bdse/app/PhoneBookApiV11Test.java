package ru.csc.bdse.app;

import ru.csc.bdse.app.v11.PhoneBookApiV11;
import ru.csc.bdse.app.v11.RecordV11;
import ru.csc.bdse.kv.InMemoryKeyValueApi;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang.RandomStringUtils.randomNumeric;

public class PhoneBookApiV11Test extends AbstractPhoneBookFunctionalTest<RecordV11> {
    private static final String NICK_NAME = randomAlphabetic(10);
    private static final String FIRST_NAME = randomAlphabetic(10);
    private static final String LAST_NAME = randomAlphabetic(10);
    private static final Supplier<RecordV11> SAME_KEY_GENERATOR = () ->
            new RecordV11(
                    NICK_NAME,
                    FIRST_NAME,
                    LAST_NAME,
                    Stream.generate(() -> randomNumeric(10)).limit(10).collect(Collectors.toList())
            );
    public static final Supplier<RecordV11> RANDOM_GENERATOR = () ->
            new RecordV11(
                    randomAlphabetic(10),
                    randomAlphabetic(10),
                    randomAlphabetic(10),
                    Stream.generate(() -> randomNumeric(10)).limit(10).collect(Collectors.toList())
            );

    @Override
    protected Supplier<RecordV11> randomGenerator() {
        return RANDOM_GENERATOR;
    }

    @Override
    protected Supplier<RecordV11> sameKeyGenerator() {
        return SAME_KEY_GENERATOR;
    }

    // TODO: 15.03.18 it must be integration test
    @Override
    protected PhoneBookApi<RecordV11> newPhoneBookApi() {
        return new PhoneBookApiV11(new InMemoryKeyValueApi("test"));
    }
}
