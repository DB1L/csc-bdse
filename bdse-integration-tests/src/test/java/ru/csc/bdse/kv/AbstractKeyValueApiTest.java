package ru.csc.bdse.kv;

import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.csc.bdse.util.Constants;
import ru.csc.bdse.util.Random;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @author semkagtn
 */
public abstract class AbstractKeyValueApiTest {
    private static final int ATTEMPTS = 100;
    private static final int THREADS = 10;

    protected abstract KeyValueApi newKeyValueApi();

    private final KeyValueApi api = newKeyValueApi();

    @Before
    public void reset() {
        api.getInfo().stream().map(NodeInfo::getName)
                .forEach(nodeName -> api.action(nodeName, NodeAction.UP));
        api.getKeys("").forEach(api::delete);
    }

    @Test
    public void createValue() {
        SoftAssertions softAssert = new SoftAssertions();

        String key = Random.nextKey();
        byte[] value = Random.nextValue();

        Optional<byte[]> oldValue = api.get(key);
        softAssert.assertThat(oldValue.isPresent()).as("old value").isFalse();

        api.put(key, value);
        byte[] newValue = api.get(key).orElse(Constants.EMPTY_BYTE_ARRAY);
        assertThat(newValue).as("new value").isEqualTo(value);

        softAssert.assertAll();
    }

    @Test
    public void updateValue() {
        SoftAssertions softAssert = new SoftAssertions();

        String key = Random.nextKey();
        byte[] oldValue = Random.nextValue();
        byte[] newValue = Random.nextValue();

        api.put(key, oldValue);
        byte[] actualOldValue = api.get(key).orElse(Constants.EMPTY_BYTE_ARRAY);
        softAssert.assertThat(actualOldValue).as("old value").isEqualTo(oldValue);

        api.put(key, newValue);
        byte[] actualNewValue = api.get(key).orElse(Constants.EMPTY_BYTE_ARRAY);
        softAssert.assertThat(actualNewValue).as("new value").isEqualTo(newValue);

        softAssert.assertAll();
    }

    @Test
    public void deleteValue() {
        SoftAssertions softAssert = new SoftAssertions();

        String key = Random.nextKey();
        byte[] value = Random.nextValue();

        api.put(key, value);
        byte[] actualOldValue = api.get(key).orElse(Constants.EMPTY_BYTE_ARRAY);
        softAssert.assertThat(actualOldValue).as("old value").isEqualTo(value);

        api.delete(key);
        Optional<byte[]> actualNewValue = api.get(key);
        softAssert.assertThat(actualNewValue.isPresent()).as("new value").isFalse();

        softAssert.assertAll();
    }

    @Test
    public void deleteNonexistentValue() {
        SoftAssertions softAssert = new SoftAssertions();

        String nonexistentKey = Random.nextKey();
        Optional<byte[]> actualOldValue = api.get(nonexistentKey);
        softAssert.assertThat(actualOldValue.isPresent()).as("old value").isFalse();

        api.delete(nonexistentKey);
        Optional<byte[]> actualNewValue = api.get(nonexistentKey);
        softAssert.assertThat(actualNewValue.isPresent()).as("new value").isFalse();

        softAssert.assertAll();
    }

    @Test
    public void getClusterInfoValue() {
        SoftAssertions softAssert = new SoftAssertions();

        api.getInfo().forEach(n -> api.action(n.getName(), NodeAction.UP));
        Set<NodeInfo> info = api.getInfo();
        softAssert.assertThat(info).as("size").isNotEmpty();
        softAssert.assertThat(info.iterator().next().getStatus()).as("status").isEqualTo(NodeStatus.UP);

        softAssert.assertAll();
    }

    @Test
    public void getKeysByPrefix() {
        SoftAssertions softAssert = new SoftAssertions();

        String prefix1 = "prefix1";
        String key1 = prefix1 + Random.nextKey();
        String key2 = prefix1 + Random.nextKey();
        Set<String> prefix1Keys = new HashSet<>();
        prefix1Keys.add(key1);
        prefix1Keys.add(key2);

        String prefix2 = "prefix2";
        String key3 = prefix2 + Random.nextKey();
        Set<String> prefix2Keys = Collections.singleton(key3);
        byte[] value = Random.nextValue();

        api.put(key1, value);
        api.put(key2, value);
        api.put(key3, value);

        Set<String> actualPrefix1Keys = api.getKeys(prefix1);
        softAssert.assertThat(actualPrefix1Keys).as("prefix1").isEqualTo(prefix1Keys);

        Set<String> actualPrefix2Keys = api.getKeys(prefix2);
        softAssert.assertThat(actualPrefix2Keys).as("prefix2").isEqualTo(prefix2Keys);

        softAssert.assertAll();
    }

    @Test
    public void concurrentPuts() throws InterruptedException {
        final Thread[] threads = new Thread[THREADS];
        for (int i = 0; i < threads.length; ++i) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < ATTEMPTS; ++j) {
                    final byte[] value = String.valueOf(j).getBytes();
                    api.put("key", value);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        final Optional<byte[]> finalResult = api.get("key");
        Assert.assertTrue(finalResult.isPresent());

        Assert.assertEquals(String.valueOf(ATTEMPTS - 1), finalResult.map(String::new).orElse("NAN"));
    }

    @Test
    public void concurrentDeleteAndKeys() {
        final String prefix = "prefix";
        final List<String> keys = Stream.generate(UUID::randomUUID)
                .map(UUID::toString)
                .map(u -> prefix + u)
                .limit(ATTEMPTS)
                .collect(Collectors.toList());

        for (String key : keys) {
            api.put(key, key.getBytes());
        }

        final ExecutorService deleters = new ForkJoinPool(THREADS);
        for (String key : keys) {
            deleters.submit(() -> api.delete(key));
        }

        deleters.shutdown();

        while (!deleters.isTerminated()) {
            final Set<String> currentKeys = api.getKeys(prefix);
            Assert.assertTrue(keys.containsAll(currentKeys));
        }
    }

    @Test
    public void actionUpDown() {
        for (int i = 0; i < 10; ++i) {
            api.getInfo().forEach(n -> api.action(n.getName(), NodeAction.DOWN));
            api.getInfo().forEach(n -> Assert.assertEquals(NodeStatus.DOWN, n.getStatus()));

            api.getInfo().forEach(n -> api.action(n.getName(), NodeAction.UP));
            api.getInfo().forEach(n -> Assert.assertEquals(NodeStatus.UP, n.getStatus()));
        }
    }

    @Test(expected = RuntimeException.class)
    public void putWithStoppedNode() {
        api.getInfo().forEach(n -> api.action(n.getName(), NodeAction.DOWN));
        api.put("key", "value".getBytes());
    }

    @Test(expected = RuntimeException.class)
    public void getWithStoppedNode() {
        api.getInfo().forEach(n -> api.action(n.getName(), NodeAction.DOWN));
        api.get("key");
    }

    @Test(expected = RuntimeException.class)
    public void getKeysByPrefixWithStoppedNode() {
        api.getInfo().forEach(n -> api.action(n.getName(), NodeAction.DOWN));
        api.getKeys("prefix");
    }

    @Test
    public void deleteByTombstone() {
        // TODO use tombstones to mark as deleted (optional)
    }

    // @Test
    // Turned off, because it is time-consuming
    public void loadMillionKeys() throws InterruptedException {
        final int million = 1_000_000;
        final Thread[] threads = new Thread[THREADS];
        for (int i = 0; i < threads.length; ++i) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < million / THREADS; ++j) {
                    api.put(UUID.randomUUID().toString(), RandomStringUtils.randomAlphanumeric(50).getBytes());
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        Assert.assertEquals(api.getKeys("").size(), million);
    }
}
