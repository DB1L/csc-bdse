package ru.csc.bdse.app.v1;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.csc.bdse.app.PhoneBookApi;
import ru.csc.bdse.app.v1.proto.RecordV1OuterClass;
import ru.csc.bdse.kv.KeyValueApi;

import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PhoneBookApiV1 implements PhoneBookApi<RecordV1> {
    private final KeyValueApi kv;

    public PhoneBookApiV1(KeyValueApi kv) {
        this.kv = kv;
    }

    @Override
    public void put(RecordV1 record) {
        kv.put(key(record), encode(record));
    }

    @Override
    public void delete(RecordV1 record) {
        kv.delete(key(record));
    }

    @Override
    public Set<RecordV1> get(char literal) {
        // Entry can disappear in the middle of operation
        final Stream<Optional<RecordV1>> optionalStream = kv.getKeys(Character.toString(literal)).stream()
                .map(this::get);
        final Stream<RecordV1> flattenedStream = optionalStream
                .flatMap(opt -> opt.map(Stream::of).orElse(Stream.empty()));
        return flattenedStream.collect(Collectors.toSet());
    }

    private Optional<RecordV1> get(String lastName) {
        return kv.get(lastName).map(this::decode);
    }

    private String key(RecordV1 recordV1) {
        return recordV1.lastName() + '#' + recordV1.firstName();
    }

    private byte[] encode(RecordV1 record) {
        return RecordV1OuterClass.RecordV1.newBuilder()
                .setFirstName(record.firstName())
                .setLastName(record.lastName())
                .setPhone(record.phone())
                .build()
                .toByteArray();
    }

    private RecordV1 decode(byte[] data) {
        try {
            final RecordV1OuterClass.RecordV1 record = RecordV1OuterClass.RecordV1.parseFrom(data);
            return new RecordV1(record.getFirstName(), record.getLastName(), record.getPhone());
        } catch (InvalidProtocolBufferException e) {
            throw new UncheckedIOException(e);
        }
    }
}
