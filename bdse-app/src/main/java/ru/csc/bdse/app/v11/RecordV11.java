package ru.csc.bdse.app.v11;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.csc.bdse.app.Record;

import java.util.*;
import java.util.stream.Stream;

public class RecordV11 implements Record {
    private final String nickName;
    private final String firstName;
    private final String lastName;
    private final List<String> phones;

    @JsonCreator
    public RecordV11(@JsonProperty(value = "nickName", required = true) String nickName,
                     @JsonProperty(value = "firstName", required = true) String firstName,
                     @JsonProperty(value = "lastName", required = true) String lastName,
                     @JsonProperty(value = "phones", required = true) List<String> phones) {
        this.nickName = nickName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phones = new ArrayList<>(phones);
    }

    @JsonProperty
    public String nickName() {
        return nickName;
    }

    @JsonProperty
    public String firstName() {
        return firstName;
    }

    @JsonProperty
    public String lastName() {
        return lastName;
    }

    @JsonIgnore
    public Stream<String> phones() {
        return phones.stream();
    }

    @JsonProperty("phones")
    public List<String> phonesList() {
        return phones;
    }

    @Override
    @JsonIgnore
    public Set<Character> literals() {
        final Set<Character> result = new HashSet<>();
        lastName.chars().mapToObj(c -> (char) c).findFirst().ifPresent(result::add);
        nickName.chars().mapToObj(c -> (char) c).findFirst().ifPresent(result::add);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordV11 recordV11 = (RecordV11) o;
        return Objects.equals(nickName, recordV11.nickName) &&
                Objects.equals(firstName, recordV11.firstName) &&
                Objects.equals(lastName, recordV11.lastName) &&
                Objects.equals(phones, recordV11.phones);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickName, firstName, lastName, phones);
    }

    @Override
    public String toString() {
        return "RecordV11{" +
                "nickName='" + nickName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phones=" + phones +
                '}';
    }
}
