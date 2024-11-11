package com.ims.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "loans")
@Getter
@Setter
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
    @Column(nullable = false)
    private LocalDate startDate;
    @Column(nullable = false)
    private LocalDate endDate;
    @Column(nullable = false)
    private LocalDate initialEndDate; // Added to track the initial end date
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status = LoanStatus.ACTIVE;
    @Column
    private LocalDate previousExtendedDate; // To track the original end date before extension
    @Version
    private Long version;
    @Column
    private LocalDate returnDate;
    @Column
    private Integer extensionCount = 0;
    @Column(nullable = false)
    private Integer quantity;

    // Constructors
    public Loan() {}

    public Loan(User user, Item item, Integer quantity, LocalDate startDate, LocalDate endDate) {
        this.user = user;
        this.item = item;
        this.quantity = quantity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.initialEndDate = endDate;  // Set original end date same as end date when creating
        this.status = LoanStatus.ACTIVE;
    }

    public Loan(User user, Item item, Integer requestedQuantity, LocalDate startDate, LocalDate endDate, LoanStatus status) {
        this.user = user;
        this.item = item;
        this.quantity = quantity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.initialEndDate = endDate;  // Set original end date same as end date when creating
        this.status = status;
    }
}