package ru.csc.bdse.app.v11;

import org.springframework.web.client.RestTemplate;
import ru.csc.bdse.app.PhoneBookApi;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PhoneBookV11Client implements PhoneBookApi<RecordV11> {
    private final String baseUrl;
    private final RestTemplate rest = new RestTemplate();

    public PhoneBookV11Client(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void put(RecordV11 record) {
        final String url = baseUrl + "/records";
        rest.postForObject(url, record, Void.class);
    }

    @Override
    public void delete(RecordV11 record) {
        final String url = baseUrl + "/records/delete";
        rest.postForObject(url, record, Void.class);
    }

    @Override
    public Set<RecordV11> get(char literal) {
        final String url = baseUrl + "/records/" + literal;
        return new HashSet<>(Arrays.asList(rest.getForObject(url, RecordV11[].class)));
    }
}
