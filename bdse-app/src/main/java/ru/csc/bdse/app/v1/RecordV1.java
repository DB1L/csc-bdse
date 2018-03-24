package ru.csc.bdse.app.v1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.csc.bdse.app.Record;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class RecordV1 implements Record {
    private final String firstName;
    private final String lastName;
    private final String phone;

    @JsonCreator
    public RecordV1(@JsonProperty(value = "firstName", required = true) String firstName,
                    @JsonProperty(value = "lastName", required = true) String lastName,
                    @JsonProperty(value = "phone", required = true) String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    @JsonProperty
    public String firstName() {
        return firstName;
    }

    @JsonProperty
    public String lastName() {
        return lastName;
    }

    @JsonProperty
    public String phone() {
        return phone;
    }

    @Override
    @JsonIgnore
    public Set<Character> literals() {
        return lastName.chars().mapToObj(c -> (char) c).findFirst()
                .map(Collections::singleton)
                .orElse(Collections.emptySet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordV1 recordV1 = (RecordV1) o;
        return Objects.equals(firstName, recordV1.firstName) &&
                Objects.equals(lastName, recordV1.lastName) &&
                Objects.equals(phone, recordV1.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, phone);
    }

    @Override
    public String toString() {
        return "RecordV11{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
