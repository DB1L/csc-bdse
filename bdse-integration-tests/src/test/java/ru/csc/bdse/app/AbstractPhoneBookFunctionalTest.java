package ru.csc.bdse.app;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test have to be implemented
 *
 * @author alesavin
 */
public abstract class AbstractPhoneBookFunctionalTest<R extends Record> {
    protected abstract Supplier<R> randomGenerator();

    protected abstract Supplier<R> sameKeyGenerator();

    protected abstract PhoneBookApi<R> newPhoneBookApi();

    private PhoneBookApi<R> api = newPhoneBookApi();

    @Test
    public void getFromEmptyBook() {
        final R record = randomGenerator().get();
        //noinspection ConstantConditions
        final Set<R> result = api.get(record.literals().stream().findFirst().get());
        Assert.assertEquals(Collections.emptySet(), result);
    }

    @Test
    public void putAndGet() {
        final R record = randomGenerator().get();
        api.put(record);

        //noinspection ConstantConditions
        final Set<R> result = api.get(record.literals().stream().findFirst().get());
        Assert.assertEquals(Collections.singleton(record), result);
    }

    @Test
    public void erasure() {
        final R record = randomGenerator().get();
        api.put(record);

        //noinspection ConstantConditions
        final Set<R> existedResult = api.get(record.literals().stream().findFirst().get());
        Assert.assertEquals(Collections.singleton(record), existedResult);

        api.delete(record);
        //noinspection ConstantConditions
        final Set<R> nonExistedResult = api.get(record.literals().stream().findFirst().get());
        Assert.assertEquals(Collections.emptySet(), nonExistedResult);
    }

    @Test
    public void update() {
        final R record1 = sameKeyGenerator().get();
        api.put(record1);

        //noinspection ConstantConditions
        final Set<R> firstResult = api.get(record1.literals().stream().findFirst().get());
        Assert.assertEquals(Collections.singleton(record1), firstResult);

        final R record2 = sameKeyGenerator().get();
        api.put(record2);
        api.put(record2);

        //noinspection ConstantConditions
        final Set<R> secondResult = api.get(record1.literals().stream().findFirst().get());
        Assert.assertEquals(Collections.singleton(record2), secondResult);
    }

    @Test
    public void testPutGetMany() {
        final Set<R> records = Stream.generate(randomGenerator()).limit(1000).collect(Collectors.toSet());
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
}