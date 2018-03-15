package ru.csc.bdse.app;

import ru.csc.bdse.app.v1.PhoneBookV1Client;
import ru.csc.bdse.app.v1.RecordV1;

import java.util.function.Supplier;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang.RandomStringUtils.randomNumeric;

public class PhoneBookApiV1Test extends AbstractPhoneBookFunctionalTest<RecordV1> {
    private static final String FIRST_NAME = randomAlphabetic(10);
    private static final String LAST_NAME = randomAlphabetic(10);
    private static final Supplier<RecordV1> SAME_KEY_GENERATOR = () ->
            new RecordV1(
                    FIRST_NAME,
                    LAST_NAME,
                    randomNumeric(10)
            );
    public static final Supplier<RecordV1> RANDOM_GENERATOR = () ->
            new RecordV1(
                    randomAlphabetic(10),
                    randomAlphabetic(10),
                    randomNumeric(10)
            );

    @Override
    protected Supplier<RecordV1> randomGenerator() {
        return RANDOM_GENERATOR;
    }

    @Override
    protected Supplier<RecordV1> sameKeyGenerator() {
        return SAME_KEY_GENERATOR;
    }

    @Override
    protected PhoneBookApi<RecordV1> newPhoneBookApi(int port) {
        return new PhoneBookV1Client("http://localhost:" + port);
    }

    @Override
    protected String version() {
        return "1.0";
    }
}
