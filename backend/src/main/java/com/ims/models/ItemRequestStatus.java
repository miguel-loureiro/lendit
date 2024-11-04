package com.ims.models;

public enum ItemRequestStatus {
    PENDING,    // Waiting in queue
    NOTIFIED,   // Item available, waiting for user pickup
    FULFILLED,  // Request converted to loan
    EXPIRED,    // User didn't pick up in time
    CANCELLED   // User cancelled request
}
