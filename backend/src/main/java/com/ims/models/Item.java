package com.ims.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Version
    @Column
    private Long version;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("requestDate DESC")
    private Set<ItemRequest> requests = new LinkedHashSet<>();

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Loan> loans = new HashSet<>();

    // Constructor with all fields
    public Item(String designation, String barcode, String brand, Category category, BigDecimal purchasePrice, int stockQuantity) {
        this.designation = Objects.requireNonNull(designation, "Designation cannot be null");
        this.barcode = Objects.requireNonNull(barcode, "Barcode cannot be null");
        this.brand = Objects.requireNonNull(brand, "Brand cannot be null");
        this.category = Objects.requireNonNull(category, "Category cannot be null");
        this.purchasePrice = Objects.requireNonNull(purchasePrice, "Purchase price cannot be null");
        this.stockQuantity = stockQuantity;
    }

    // Default constructor (required by JPA)
    public Item() {
        this.purchasePrice = BigDecimal.ZERO;
        this.stockQuantity = 0;
    }

    // Check only stock quantity
    public boolean isAvailable() {
        return getAvailableQuantity() > 0;
    }

    // Check stock and pending requests
    public boolean isAvailableForDirectLoan() {
        return getAvailableQuantity() > 0 && getPendingRequests().isEmpty();
    }

    // Calculate available quantity considering active loans
    public int getAvailableQuantity() {
        return stockQuantity - getActiveLoans().size();
    }

    // Get list of active loans
    public List<Loan> getActiveLoans() {
        return loans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE || loan.getStatus() == LoanStatus.OVERDUE)
                .collect(Collectors.toList());
    }

    // Add a new active loan to the item
    public void addActiveLoan(Loan loan) {
        loans.add(loan);
        stockQuantity--;
    }

    // Get list of pending requests
    public List<ItemRequest> getPendingRequests() {
        return requests.stream()
                .filter(request -> request.getStatus() == ItemRequestStatus.PENDING)
                .collect(Collectors.toList());
    }

    // Get the next pending request, if available
    public Optional<ItemRequest> getNextPendingRequest() {
        return requests.stream()
                .filter(request -> request.getStatus() == ItemRequestStatus.PENDING)
                .findFirst();
    }
}