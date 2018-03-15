package ru.csc.bdse.app;

import ru.csc.bdse.app.v11.PhoneBookV11Client;
import ru.csc.bdse.app.v11.RecordV11;

import java.util.function.Supplier;

public class PhoneBookApiV11NonFunctionalTest extends PhoneBookNonFunctionalTest<RecordV11> {

    @Override
    protected Supplier<RecordV11> randomGenerator() {
        return PhoneBookApiV11Test.RANDOM_GENERATOR;
    }

    @Override
    protected PhoneBookApi<RecordV11> newPhoneBookApi(int port) {
        return new PhoneBookV11Client("http://localhost:" + port);
    }

    @Override
    protected String version() {
        return "1.1";
    }
}
