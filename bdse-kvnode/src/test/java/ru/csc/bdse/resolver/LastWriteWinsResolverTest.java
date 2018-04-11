package ru.csc.bdse.resolver;

import org.junit.*;
import ru.csc.bdse.coordinator.RecordWithTimestamp;

import java.util.ArrayList;
import java.util.Collection;

public class LastWriteWinsResolverTest {
    private ConflictResolver resolver;

    @Before
    public void setUp() {
        resolver = new LastWriteWinsResolver();
    }

    @After
    public void tearDown() {
        resolver = null;
    }

    @Test
    public void lastWriteWins() {
        final Collection<RecordWithTimestamp> records = new ArrayList<>();
        records.add(new RecordWithTimestamp("aaa".getBytes(), 1, false, 3));
        records.add(new RecordWithTimestamp("aaa".getBytes(), 2, false, 2));

        final RecordWithTimestamp expectedWinner = new RecordWithTimestamp("bbb".getBytes(), 3, false, 1);
        records.add(expectedWinner);

        final RecordWithTimestamp winner = resolver.resolve(records);
        assertRecords(expectedWinner, winner);
    }

    @Test
    public void lastWriteAndMostFrequentWins() {
        final Collection<RecordWithTimestamp> records = new ArrayList<>();

        final RecordWithTimestamp expectedWinner = new RecordWithTimestamp("aaa".getBytes(), 4, false, 1);
        records.add(expectedWinner);

        records.add(new RecordWithTimestamp("ccc".getBytes(), 2, false, 2));
        records.add(new RecordWithTimestamp("aaa".getBytes(), 4, false, 3));
        records.add(new RecordWithTimestamp("bbb".getBytes(), 4, false, 4));

        final RecordWithTimestamp winner = resolver.resolve(records);
        assertRecords(expectedWinner, winner);
    }

    @Test
    public void lastWriteMostFrequentAndMaxNodeWins() {
        final Collection<RecordWithTimestamp> records = new ArrayList<>();

        final RecordWithTimestamp expectedWinner = new RecordWithTimestamp("aaa".getBytes(), 4, false, 4);
        records.add(expectedWinner);

        records.add(new RecordWithTimestamp("bbb".getBytes(), 4, false, 3));
        records.add(new RecordWithTimestamp("ccc".getBytes(), 4, false, 2));
        records.add(new RecordWithTimestamp("bbb".getBytes(), 3, false, 1));
        records.add(new RecordWithTimestamp("ccc".getBytes(), 3, false, 5));

        final RecordWithTimestamp winner = resolver.resolve(records);
        assertRecords(expectedWinner, winner);
    }

    private void assertRecords(RecordWithTimestamp expected, RecordWithTimestamp actual) {
        Assert.assertEquals(expected.createdAt(), actual.createdAt());
        Assert.assertEquals(expected.isDeleted(), actual.isDeleted());
        Assert.assertArrayEquals(expected.payload(), actual.payload());
        Assert.assertEquals(expected.nodeNum(), actual.nodeNum());
    }
}
