package ru.csc.bdse.resolver;

import ru.csc.bdse.coordinator.RecordWithTimestamp;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

public class LastWriteWinsResolver extends ConflictResolver.Stub {
    @Override
    public RecordWithTimestamp resolve(Collection<RecordWithTimestamp> records) {
        final RecordWithTimestamp recordWithMaxTime = Collections.max(records, Comparator.comparingLong(RecordWithTimestamp::createdAt));
        final long elementsWithMaxTime = records.stream().filter(record -> record.createdAt() == recordWithMaxTime.createdAt()).count();
        if (elementsWithMaxTime > 1) {
            final Map<RecordWithTimestamp, Long> frequencies = records.stream()
                    .filter(record -> record.createdAt() == recordWithMaxTime.createdAt())
                    .collect(groupingBy(Function.identity(), counting()));
            final Comparator<Map.Entry<RecordWithTimestamp, Long>> comparator = Comparator
                    .comparingLong((Map.Entry<RecordWithTimestamp, Long> e) -> e.getKey().createdAt())
                    .thenComparingLong(Map.Entry::getValue)
                    .thenComparingLong((Map.Entry<RecordWithTimestamp, Long> e) -> e.getKey().nodeNum());
            return frequencies.entrySet()
                    .stream()
                    .max(comparator)
                    .orElseThrow(IllegalStateException::new).getKey();
        } else {
            return recordWithMaxTime;
        }
    }
}
