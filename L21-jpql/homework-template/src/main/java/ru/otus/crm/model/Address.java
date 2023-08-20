package ru.otus.crm.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class Address implements Cloneable {

    @Id
    @SequenceGenerator(name = "address_id_custom_generator", allocationSize = 1)
    @GeneratedValue(generator = "address_id_custom_generator")
    @Column(name = "id")
    private Long id;

    @Column(name = "street")
    private String street;

    public Address(String street) {
        this.street = street;
    }

    @Override
    public Address clone() {
        return new Address(this.id, this.street);
    }
}
