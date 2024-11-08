package com.ims.models;

public enum ItemRequestStatus {
    PENDING, // The request has been created but not yet processed.
    NOTIFIED, // The user has been notified that the item is available for loan.
    FULFILLED, // The request has been successfully fulfilled, and the item is now loaned to the user.
    WAITING, // The request is waiting because the item is currently on an active loan and cannot be loaned to the user yet.
    CANCELLED // The request has been cancelled by the user or the system.
}
