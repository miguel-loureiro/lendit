package com.ims.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Enumerated(EnumType.STRING)
    private LoanStatus status = LoanStatus.ACTIVE;

    @Version
    private Long version;

    // Add actual return date
    private LocalDateTime returnDate;

    // Constructors, getters, and setters
    public Loan() {}

    public Loan(User user, Item item, LocalDate startDate, LocalDate endDate, LoanStatus status) {
        this.user = user;
        this.item = item;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public Loan(User user, Item item, LocalDate startDate, LocalDate endDate) {
        this.user = user;
        this.item = item;
        this.startDate = startDate;
        this.endDate = endDate;
    }


    @PrePersist
    @PreUpdate
    private void validateDates() {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        if (returnDate != null && returnDate.isBefore(startDate.atStartOfDay())) {
            throw new IllegalArgumentException("Return date cannot be before start date");
        }
    }
}
