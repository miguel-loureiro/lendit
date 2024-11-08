package com.ims.models;

import lombok.Getter;

@Getter
public enum LoanStatus {
    ACTIVE("Active loan in progress"),    // Initial state when loan is created
    EXTENDED("Loan period extended"),     // When loan is extended beyond original end date
    RETURNED ("Item returned and loan terminated");        // When loan is returned and completed

    private final String description;

    LoanStatus(String description) {
        this.description = description;
    }

    public boolean canBeExtended() {
        return this == ACTIVE || this == EXTENDED;
    }

    public boolean canBeTerminated() {
        return this == ACTIVE || this == EXTENDED;
    }

    public boolean isActive() {
        return this == ACTIVE || this == EXTENDED;
    }

    public boolean isCompleted() {
        return this == RETURNED;
    }
}