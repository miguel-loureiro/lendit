package com.ims.services;

import com.ims.models.Item;
import com.ims.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class NotificationService {

    private final EmailService emailService;

    @Autowired
    public NotificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void sendItemRequestConfirmation(User user, Item item, int quantity) {
        String subject = "Item Request Confirmation";
        String content = String.format("""
            Dear %s,
            
            Your request for %d unit(s) of %s has been received and is being processed.
            
            Item Details:
            - Designation: %s
            - Barcode: %s
            - Requested Quantity: %d
            
            We will notify you once your request has been fulfilled.
            
            Best regards,
            Item Management System
            """,
                user.getUsername(),
                quantity,
                item.getDesignation(),
                item.getDesignation(),
                item.getBarcode(),
                quantity
        );

        try {
            emailService.sendSimpleMessage(user.getEmail(), subject, content);
            log.info("Sent request confirmation email to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send request confirmation email to {}", user.getEmail(), e);
            // Don't throw exception - notification failure shouldn't break the main flow
        }
    }

    public void sendRequestFulfillmentNotification(User user, Item item, int quantity, LocalDate returnDate) {
        String subject = "Item Request Fulfilled";
        String content = String.format("""
            Dear %s,
            
            Your request for %d unit(s) of %s has been fulfilled.
            
            Item Details:
            - Designation: %s
            - Barcode: %s
            - Quantity: %d
            - Return Date: %s
            
            Please remember to return the item(s) by the specified return date.
            
            Best regards,
            Item Management System
            """,
                user.getUsername(),
                quantity,
                item.getDesignation(),
                item.getDesignation(),
                item.getBarcode(),
                quantity,
                returnDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        );

        try {
            emailService.sendSimpleMessage(user.getEmail(), subject, content);
            log.info("Sent fulfillment notification email to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send fulfillment notification email to {}", user.getEmail(), e);
        }
    }

    public void sendItemReturnConfirmation(User user, Item item, int quantity, int remainingLoanQuantity) {
        String subject = "Item Return Confirmation";
        String content = String.format("""
            Dear %s,
            
            We confirm the return of %d unit(s) of %s.
            
            Item Details:
            - Designation: %s
            - Barcode: %s
            - Returned Quantity: %d
            %s
            
            Best regards,
            Item Management System
            """,
                user.getUsername(),
                quantity,
                item.getDesignation(),
                item.getDesignation(),
                item.getBarcode(),
                quantity,
                remainingLoanQuantity > 0 ?
                        String.format("- Remaining Loan Quantity: %d", remainingLoanQuantity) :
                        "Your loan has been completed."
        );

        try {
            emailService.sendSimpleMessage(user.getEmail(), subject, content);
            log.info("Sent return confirmation email to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send return confirmation email to {}", user.getEmail(), e);
        }
    }
}
