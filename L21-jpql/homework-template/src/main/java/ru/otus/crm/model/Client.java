package ru.otus.crm.model;


import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@Entity
@Table(name = "client")
public class Client implements Cloneable {

    @Id
    @SequenceGenerator(name = "client_id_custom_generator", allocationSize = 1)
    @GeneratedValue(generator = "client_id_custom_generator")
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    private Address address;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private Set<Phone> phones = new HashSet<>();

    public void addPhone(Phone phone) {
        phones.add(phone);
        phone.setClient(this);
    }

    public Client(String name, Address address) {
        this.name = name;
        this.address = address;
    }

    @Override
    public Client clone() {
        Client clone = new Client();
        clone.setId(this.id);
        clone.setName(this.name);
        clone.setAddress(this.address.clone());
        clone.setPhones(this.getPhones().stream()
                .map(phone -> phone.clone(clone))
                .collect(Collectors.toSet()));
        return clone;
    }
}
