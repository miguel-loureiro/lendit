package com.ims.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer stockQuantity = 0;

    @Enumerated(EnumType.STRING)
    private ItemState state = ItemState.FREE;

    @Version
    @Column
    private Long version;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Loan> loans = new HashSet<>();

    // Default constructor
    public Item() {
    }

    // Full constructor
    public Item(String designation, String barcode, String brand, Category category, BigDecimal purchasePrice, Integer stockQuantity) {
        this.designation = designation;
        this.barcode = barcode;
        this.brand = brand;
        this.category = category;
        this.purchasePrice = purchasePrice;
        this.stockQuantity = stockQuantity;
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
        return Objects.equals(barcode, item.barcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(barcode);
    }
}