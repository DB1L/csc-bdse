package ru.csc.bdse.app;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class PhoneBookApiTest<R extends Record> {
    // TODO: 12.03.18 implement integration tests based on this class
    protected abstract Supplier<R> generator();

    protected abstract PhoneBookApi<R> newApi();

    @Test
    public void testPutGet() {
        final PhoneBookApi<R> api = newApi();
        final R record = generator().get();
        api.put(record);
        final Set<R> result = api.get(record.literals().stream().findFirst().get());
        Assert.assertEquals(Collections.singleton(record), result);
    }

    @Test
    public void testPutDeleteGet() {
        final PhoneBookApi<R> api = newApi();
        final R record = generator().get();
        api.put(record);
        api.delete(record);
        final Set<R> result = api.get(record.literals().stream().findFirst().get());
        Assert.assertEquals(Collections.emptySet(), result);
    }

    @Test
    public void testPutGetMany() {
        final PhoneBookApi<R> api = newApi();
        final Set<R> records = Stream.generate(generator()).limit(1000).collect(Collectors.toSet());
        records.forEach(api::put);

        final Map<Character, Set<R>> recordsByLiterals = new HashMap<>();

        for (R record : records) {
            for (Character lit : record.literals()) {
                recordsByLiterals.putIfAbsent(lit, new HashSet<>());
                recordsByLiterals.get(lit).add(record);
            }
        }

        for (Character lit : recordsByLiterals.keySet()) {
            Assert.assertEquals(recordsByLiterals.get(lit), api.get(lit));
        }
    }

    // TODO: 3/10/18 MORE TESTS
}