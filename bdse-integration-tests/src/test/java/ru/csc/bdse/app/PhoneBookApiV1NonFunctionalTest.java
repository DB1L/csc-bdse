package ru.csc.bdse.app;

import ru.csc.bdse.app.v1.PhoneBookV1Client;
import ru.csc.bdse.app.v1.RecordV1;

import java.util.function.Supplier;

public class PhoneBookApiV1NonFunctionalTest extends PhoneBookNonFunctionalTest<RecordV1> {

    @Override
    protected Supplier<RecordV1> randomGenerator() {
        return PhoneBookApiV1Test.RANDOM_GENERATOR;
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
