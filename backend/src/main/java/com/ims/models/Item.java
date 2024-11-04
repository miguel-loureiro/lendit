package com.ims.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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

    @Version
    @Column
    private Long version;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    @OrderBy("requestDate DESC")
    private Set<ItemRequest> requests = new LinkedHashSet<>();

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Loan> loans = new HashSet<>(); // Add this field to track loans

    public Item(String designation, String barcode, String brand, Category category, BigDecimal purchasePrice, int stockQuantity) {
    }

    public Item(String designation, String barcode, String brand, Category category, BigDecimal purchasePrice, Integer stockQuantity, Long version) {
        this.designation = designation;
        this.barcode = barcode;
        this.brand = brand;
        this.category = category;
        this.purchasePrice = purchasePrice;
        this.stockQuantity = stockQuantity;
        this.version = version;
    }

    public Item() {
    }
    
    public boolean isAvailable() {
        return getAvailableQuantity() > 0; // Check only stock quantity
    }

    public boolean isAvailableForDirectLoan() {
        return getAvailableQuantity() > 0 && getPendingRequests().isEmpty(); // Check stock and pending requests
    }

    public int getAvailableQuantity() {
        return stockQuantity - getActiveLoans().size();
    }

    public List<Loan> getActiveLoans() {
        return loans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE ||
                        loan.getStatus() == LoanStatus.OVERDUE)
                .collect(Collectors.toList());
    }

    public List<ItemRequest> getPendingRequests() {
        return requests.stream()
                .filter(request -> request.getStatus() == ItemRequestStatus.PENDING)
                .collect(Collectors.toList());
    }

    public Optional<ItemRequest> getNextPendingRequest() {
        return requests.stream()
                .filter(request -> request.getStatus() == ItemRequestStatus.PENDING)
                .findFirst();
    }
}