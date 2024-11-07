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

    private LocalDate returnDate;

    // Constructors
    public Loan() {}

    public Loan(User user, Item item, LocalDate startDate, LocalDate endDate) {
        this.user = user;
        this.item = item;
        this.startDate = startDate;
        this.endDate = endDate;
        this.initialEndDate = endDate;  // Set original end date same as end date when creating
        this.status = LoanStatus.ACTIVE;
    }

    public Loan(User user, Item item, LocalDate startDate, LocalDate endDate, LoanStatus status) {
        this.user = user;
        this.item = item;
        this.startDate = startDate;
        this.endDate = endDate;
        this.initialEndDate = endDate;  // Set original end date same as end date when creating
        this.status = status;
    }

    // Method to extend the loan
    public void extend(LocalDate newEndDate) {
        if (!this.status.canBeExtended()) {
            throw new IllegalStateException("Cannot extend loan with status: " + this.status.getDescription());
        }
        this.previousExtendedDate = this.endDate;
        this.endDate = newEndDate;
        this.status = LoanStatus.EXTENDED;
    }

    // Method to terminate the loan
    public void terminate(LocalDate returnDate) {
        if (!this.status.canBeTerminated()) {
            throw new IllegalStateException("Cannot terminate loan with status: " + this.status.getDescription());
        }
        this.returnDate = returnDate;
        this.status = LoanStatus.TERMINATED;
    }

    // Method to check if loan is overdue
    public boolean isOverdue() {
        return LocalDate.now().isAfter(endDate) && !status.isCompleted();
    }

    // Method to get extension duration in days
    public long getExtensionDuration() {
        if (status != LoanStatus.EXTENDED) {
            return 0;
        }
        return ChronoUnit.DAYS.between(initialEndDate, endDate);
    }

    // Method to get total loan duration in days
    public long getTotalDuration() {
        if (returnDate == null) {
            return ChronoUnit.DAYS.between(startDate, LocalDate.now());
        }
        return ChronoUnit.DAYS.between(startDate, returnDate);
    }

    @PrePersist
    @PreUpdate
    private void validateDates() {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        if (returnDate != null && returnDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Return date cannot be before start date");
        }
        if (initialEndDate == null) {
            initialEndDate = endDate;
        }
    }

    // Helper method to check if loan has been extended
    public boolean isExtended() {
        return status == LoanStatus.EXTENDED;
    }

    // Helper method to get days overdue
    public long getDaysOverdue() {
        if (!isOverdue()) {
            return 0;
        }
        return ChronoUnit.DAYS.between(endDate, LocalDate.now());
    }

    // Helper method to check if loan was returned on time
    public boolean wasReturnedOnTime() {
        if (returnDate == null || !status.isCompleted()) {
            return false;
        }
        return !returnDate.isAfter(endDate);
    }

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", user=" + user.getUsername() +
                ", item=" + item.getDesignation() +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", initialEndDate=" + initialEndDate +
                ", status=" + status +
                ", version=" + version +
                ", returnDate=" + returnDate +
                '}';
    }
}