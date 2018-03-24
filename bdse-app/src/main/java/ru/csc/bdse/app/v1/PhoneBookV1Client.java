package ru.csc.bdse.app.v1;

import org.springframework.web.client.RestTemplate;
import ru.csc.bdse.app.PhoneBookApi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PhoneBookV1Client implements PhoneBookApi<RecordV1> {
    private final String baseUrl;
    private final RestTemplate rest = new RestTemplate();

    // TODO: 12.03.18 add error messages 
    public PhoneBookV1Client(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void put(RecordV1 record) {
        final String url = baseUrl + "/records";
        rest.postForObject(url, record, Void.class);
    }

    @Override
    public void delete(RecordV1 record) {
        final String url = baseUrl + "/records/delete";
        rest.postForObject(url, record, Void.class);
    }

    @Override
    public Set<RecordV1> get(char literal) {
        final String url = baseUrl + "/records/" + literal;
        return new HashSet<>(Arrays.asList(rest.getForObject(url, RecordV1[].class)));
    }
}
