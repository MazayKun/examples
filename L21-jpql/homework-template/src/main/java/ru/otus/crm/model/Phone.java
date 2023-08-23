package ru.otus.crm.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "phone")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Phone {

    @Id
    @SequenceGenerator(name = "phone_id_custom_generator", allocationSize = 1)
    @GeneratedValue(generator = "phone_id_custom_generator")
    @Column(name = "id")
    private Long id;

    @Column(name = "number")
    private String number;

    @ManyToOne(cascade = CascadeType.ALL)
    private Client client;

    public Phone(String number) {
        this.number = number;
    }

    public Phone(Long id, String number) {
        this.id = id;
        this.number = number;
    }

    public Phone clone(Client client) {
        return new Phone(this.id, this.number, client);
    }

    @Override
    public String toString() {
        return "Phone{" +
                "id=" + id +
                ", number='" + number + '\'' +
                ", client_id=" + (client == null ? null : client.getId()) +
                '}';
    }
}
