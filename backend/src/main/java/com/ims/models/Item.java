package com.ims.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "items")
@Getter
@Setter
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String designation;

    @Column(nullable = false, unique = true, length = 13)
    private String barcode;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice = BigDecimal.ZERO; // Default to zero

    @Column(nullable = false)
    private Integer stockQuantity = 0; // Default to zero

    @Version
    @Column
    private Long version;

    @ManyToMany(mappedBy = "items")
    private Set<User> users = new HashSet<>();

    // Default constructor
    public Item() {}

    // Full constructor with default values for version and users
    public Item(String designation, String barcode, String brand, Category category, BigDecimal purchasePrice, Integer stockQuantity, Long version, Set<User> users) {
        this.designation = designation;
        this.barcode = barcode;
        this.brand = brand;
        this.category = category;
        this.purchasePrice = (purchasePrice != null) ? purchasePrice : BigDecimal.ZERO;
        this.stockQuantity = (stockQuantity != null) ? stockQuantity : 0;
        this.version = version;
        this.users = (users != null) ? users : new HashSet<>();
    }

    // Constructor with essential fields only, setting default values for others
    public Item(String designation, String barcode, String brand, Category category, BigDecimal purchasePrice, Integer stockQuantity) {
        this(designation, barcode, brand, category, purchasePrice, stockQuantity, null, null);
    }

    // Constructor with minimum fields only, setting default values for others
    public Item(String designation, Category category) {
        this.designation = designation;
        this.barcode = "0000000000000";
        this.brand = "";
        this.category = category;
        this.stockQuantity = 0;
    }


    @PrePersist
    @PreUpdate
    private void validateFields() {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        if (purchasePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Purchase price cannot be negative");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return barcode.equals(item.barcode);
    }

    @Override
    public int hashCode() {
        return barcode.hashCode();
    }

    @Override
    public String toString() {
        return "Item{" +
                "designation='" + designation + '\'' +
                ", barcode='" + barcode + '\'' +
                ", brand='" + brand + '\'' +
                ", category=" + category +
                ", purchasePrice=" + purchasePrice +
                ", stockQuantity=" + stockQuantity +
                '}';
    }

    public enum Category {
        LAPTOP,
        DESKTOP,
        MONITOR,
        PERIPHERAL,
        NETWORK_EQUIPMENT,
        SERVER,
        STORAGE_DEVICE
    }
}