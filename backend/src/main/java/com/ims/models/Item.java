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
    private String designation; // e.g., "ThinkPad X1 Carbon", "Logitech MX Master 3"

    @Column(nullable = false, unique = true, length = 13)
    private String barcode; // Standardized EAN-13 barcode

    @Column(nullable = false)
    private String brand; // e.g., "Lenovo", "Dell", "Logitech"

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Category category; // LAPTOP, DESKTOP, PERIPHERAL, NETWORK_EQUIPMENT, etc.

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice; // Price in decimal format

    @Column(nullable = false)
    private Integer stockQuantity; // Current quantity in stock

    @Version
    @Column
    private Long version;

    @Getter
    @ManyToMany(mappedBy = "items")
    private Set<User> users = new HashSet<>();

    public Item(String designation, String barcode, String brand, Category category, BigDecimal purchasePrice, Integer stockQuantity, Long version, Set<User> users) {
        this.designation = designation;
        this.barcode = barcode;
        this.brand = brand;
        this.category = category;
        this.purchasePrice = purchasePrice;
        this.stockQuantity = stockQuantity;
        this.version = version;
        this.users = users;
    }

    public Item(String designation, String barcode, String brand, Category category, BigDecimal purchasePrice, Integer stockQuantity) {
        this.designation = designation;
        this.barcode = barcode;
        this.brand = brand;
        this.category = category;
        this.purchasePrice = purchasePrice;
        this.stockQuantity = stockQuantity;
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

    // Constructor with essential fields

    // Default constructor
    public Item() {
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
}