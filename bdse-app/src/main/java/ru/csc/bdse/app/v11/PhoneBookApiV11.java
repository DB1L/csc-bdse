package ru.csc.bdse.app.v11;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.csc.bdse.app.PhoneBookApi;
import ru.csc.bdse.app.v11.proto.RecordV11OuterClass;
import ru.csc.bdse.kv.KeyValueApi;

import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PhoneBookApiV11 implements PhoneBookApi<RecordV11> {
    private final KeyValueApi kv;

    public PhoneBookApiV11(KeyValueApi kv) {
        this.kv = kv;
    }

    @Override
    public void put(RecordV11 record) {
        kv.put(regularKey(record), encode(record));
        kv.put(nickKey(record), encode(record));
    }

    @Override
    public void delete(RecordV11 record) {
        kv.delete(regularKey(record));
        kv.delete(nickKey(record));
    }

    @Override
    public Set<RecordV11> get(char literal) {
        // Entry can disappear in the middle of operation
        final Stream<Optional<RecordV11>> optionalStream = kv.getKeys(Character.toString(literal)).stream()
                .map(this::get);
        final Stream<RecordV11> flattenedStream = optionalStream
                .flatMap(opt -> opt.map(Stream::of).orElse(Stream.empty()));
        return flattenedStream.collect(Collectors.toSet());
    }

    private Optional<RecordV11> get(String key) {
        return kv.get(key).map(this::decode);
    }

    private String regularKey(RecordV11 record) {
        return record.lastName() + '#' + record.firstName();
    }

    private String nickKey(RecordV11 record) {
        return record.nickName() + '#' + record.lastName() + '#' + record.firstName();
    }

    private byte[] encode(RecordV11 record) {
        return RecordV11OuterClass.RecordV11.newBuilder()
                .setFirstName(record.firstName())
                .setNickName(record.nickName())
                .setLastName(record.lastName())
                .addAllPhone(record.phones().collect(Collectors.toList()))
                .build()
                .toByteArray();
    }

    private RecordV11 decode(byte[] data) {
        try {
            final RecordV11OuterClass.RecordV11 record = RecordV11OuterClass.RecordV11.parseFrom(data);
            return new RecordV11(
                    record.getNickName(),
                    record.getFirstName(),
                    record.getLastName(),
                    record.getPhoneList()
            );
        } catch (InvalidProtocolBufferException e) {
            throw new UncheckedIOException(e);
        }
    }
}
