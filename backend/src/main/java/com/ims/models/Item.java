package com.ims.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "items")
@Getter
@Setter
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String designation;
    @Column(nullable = true)
    private String description;
    @Column(nullable = true)
    private String category;
    @Column(nullable = true)
    private String brand;
    @Column(nullable = false, unique = true, length = 13)
    private String barcode;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice;
    @Column(nullable = false)
    private Integer totalQuantity;
    @Column(nullable = false)
    private Integer availableQuantity;
    @Column(nullable = false)
    private boolean availableForDirectLoan;
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Loan> activeLoans = new HashSet<>();
    @Version
    private Long version;

    public Item() {
        this.availableQuantity = this.totalQuantity;
        this.availableForDirectLoan = true;
    }

    public Item(String designation, String barcode, String brand, String category, BigDecimal purchasePrice, int totalQuantity) {
        this.designation = designation;
        this.barcode = barcode;
        this.brand = brand;
        this.category = category;
        this.purchasePrice = purchasePrice;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = totalQuantity;
        this.availableForDirectLoan = true;
    }

    public void addActiveLoan(Loan loan) {
        activeLoans.add(loan);
    }

    public void removeActiveLoan(Loan loan) {
        activeLoans.remove(loan);
    }

    public boolean isAvailableForDirectLoan(Integer requestedQuantity) {
        return availableQuantity >= requestedQuantity && availableForDirectLoan;
    }
}
