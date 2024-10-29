package com.ims.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "items")
public class Item {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    @Getter
    @Column(unique = true, nullable = false)
    private String designation;

    @Getter
    @Setter
    private String brand;

    @Setter
    @Getter
    @Column(unique = true, nullable = false, length = 13)
    private String barcode;

    @Setter
    @Getter
    @Column
    private String price;

    @Column
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date lendStart;

    @Setter
    @Getter
    @Column
    private String itemImageUrl;

    @Version
    @Getter
    @Setter
    @Column
    private Long version;

    @Getter
    @ManyToMany(mappedBy = "items")
    private Set<User> users = new HashSet<>();

    public Item() {

    }

    public Item(Integer id, String designation, String brand, String barcode, String price, Date lendStart, Long version, Set<User> users) {
        this.id = id;
        this.designation = designation;
        this.brand = brand;
        this.barcode = barcode;
        this.price = price;
        this.lendStart = lendStart;
        this.version = version;
        this.users = users;
    }

    public Item(Integer id, String designation) {
        this.id = id;
        this.designation = designation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id) || Objects.equals(designation, item.designation) || Objects.equals(barcode, item.barcode) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, designation, barcode);
    }

}
