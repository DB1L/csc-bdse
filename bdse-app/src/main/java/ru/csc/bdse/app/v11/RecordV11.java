package ru.csc.bdse.app.v11;

import ru.csc.bdse.app.Record;

import java.util.*;
import java.util.stream.Stream;

public class RecordV11 implements Record {
    private final String nickName;
    private final String firstName;
    private final String lastName;
    private final List<String> phone;

    public RecordV11(String nickName, String firstName, String lastName, List<String> phones) {
        this.nickName = nickName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = new ArrayList<>(phones);
    }

    public String nickName() {
        return nickName;
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public Stream<String> phones() {
        return phone.stream();
    }

    @Override
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
                Objects.equals(phone, recordV11.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickName, firstName, lastName, phone);
    }

    @Override
    public String toString() {
        return "RecordV11{" +
                "nickName='" + nickName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone=" + phone +
                '}';
    }
}
