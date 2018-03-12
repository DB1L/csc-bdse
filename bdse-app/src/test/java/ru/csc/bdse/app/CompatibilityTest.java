package ru.csc.bdse.app;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.csc.bdse.app.v1.PhoneBookApiV1;
import ru.csc.bdse.app.v1.PhoneBookApiV1Test;
import ru.csc.bdse.app.v1.RecordV1;
import ru.csc.bdse.app.v11.PhoneBookApiV11;
import ru.csc.bdse.app.v11.PhoneBookApiV11Test;
import ru.csc.bdse.app.v11.RecordV11;
import ru.csc.bdse.kv.InMemoryKeyValueApi;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CompatibilityTest {
    // TODO: 12.03.18 implement integration tests based on this class
    private PhoneBookApi<RecordV1> apiV1;
    private PhoneBookApi<RecordV11> apiV11;

    @Before
    public void setUp() {
        final InMemoryKeyValueApi kv = new InMemoryKeyValueApi("test");
        apiV1 = new PhoneBookApiV1(kv);
        apiV11 = new PhoneBookApiV11(kv);
    }

    @After
    public void tearDown() {
        apiV1 = null;
        apiV11 = null;
    }

    @Test
    public void backwardRead() {
        final RecordV1 record = PhoneBookApiV1Test.GENERATOR.get();
        apiV1.put(record);
        final Set<RecordV11> records = apiV11.get(record.literals().stream().findFirst().get());
        Assert.assertEquals(Collections.singleton(forwardMap(record)), records);
    }

    @Test
    public void forwardRead() {
        final RecordV11 record = PhoneBookApiV11Test.GENERATOR.get();
        apiV11.put(record);
        final Set<RecordV1> records = apiV1.get(record.literals().stream().findFirst().get());
        Assert.assertEquals(Collections.singleton(backwardMap(record)), records);
    }

    @Test
    public void backwardDelete() {
        final RecordV1 record = PhoneBookApiV1Test.GENERATOR.get();
        apiV1.put(record);

        final Set<RecordV11> records = apiV11.get(record.literals().stream().findFirst().get());
        records.forEach(apiV11::delete);

        final Set<RecordV1> recordV1s = apiV1.get(record.literals().stream().findAny().get());
        Assert.assertEquals(Collections.emptySet(), recordV1s);
    }

    private RecordV11 forwardMap(RecordV1 recordV1) {
        return new RecordV11(
                "",
                recordV1.firstName(),
                recordV1.lastName(),
                Collections.singletonList(recordV1.phone())
        );
    }

    private RecordV1 backwardMap(RecordV11 recordV11) {
        final List<String> phones = recordV11.phones().collect(Collectors.toList());
        return new RecordV1(
                recordV11.firstName(),
                recordV11.lastName(),
                phones.get(phones.size() - 1)
        );
    }
}
