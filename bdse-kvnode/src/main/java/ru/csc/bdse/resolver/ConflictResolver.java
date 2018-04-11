package ru.csc.bdse.resolver;

import ru.csc.bdse.coordinator.RecordWithTimestamp;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public interface ConflictResolver {
    RecordWithTimestamp resolve(Collection<RecordWithTimestamp> records);

    Set<String> resolveKeys(Collection<Set<String>> keySets);

    abstract class Stub implements ConflictResolver {
        @Override
        public Set<String> resolveKeys(Collection<Set<String>> keySets) {
            final Set<String> result = new HashSet<>();
            keySets.forEach(result::addAll);
            return result;
        }
    }
}
