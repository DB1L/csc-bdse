package ru.csc.bdse.app.v11;

import com.google.protobuf.InvalidProtocolBufferException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.csc.bdse.app.PhoneBookApi;
import ru.csc.bdse.app.v11.proto.RecordV11OuterClass;
import ru.csc.bdse.kv.KeyValueApi;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@ConditionalOnProperty(name = "phone.version", havingValue = "1.1")
public class PhoneBookApiV11 implements PhoneBookApi<RecordV11> {
    private final KeyValueApi kv;

    public PhoneBookApiV11(KeyValueApi kv) {
        this.kv = kv;
    }

    @Override
    @RequestMapping(method = RequestMethod.POST, value = "/records")
    public void put(@RequestBody RecordV11 record) {
        kv.put(regularKey(record), encode(record));
        kv.put(nickKey(record), encode(record));
    }

    @Override
    @RequestMapping(method = RequestMethod.POST, value = "/records/delete")
    public void delete(@RequestBody RecordV11 record) {
        kv.delete(regularKey(record));
        kv.delete(nickKey(record));
    }

    @Override
    @RequestMapping(method = RequestMethod.GET, value = "/records/{literal}")
    public Set<RecordV11> get(@PathVariable char literal) {
        // Entry can disappear in the middle of operation
        final Stream<Optional<RecordV11>> optionalStream = kv.getKeys(Character.toString(literal)).stream()
                .map(this::get);
        final Stream<RecordV11> flattenedStream = optionalStream
                .flatMap(opt -> opt.map(Stream::of).orElse(Stream.empty()));
        return flattenedStream.collect(Collectors.toSet());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handle(IllegalArgumentException e) {
        return Optional.ofNullable(e.getMessage()).orElse("");
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String handle(IOException e) {
        return Optional.ofNullable(e.getMessage()).orElse("");
    }

    private Optional<RecordV11> get(String key) {
        return kv.get(key).map(this::decode);
    }

    private String regularKey(RecordV11 record) {
        return record.lastName() + '-' + record.firstName();
    }

    private String nickKey(RecordV11 record) {
        return record.nickName() + '-' + record.lastName() + '-' + record.firstName();
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
