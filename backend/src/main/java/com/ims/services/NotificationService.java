package com.ims.services;

import com.ims.models.Item;
import com.ims.models.Loan;
import com.ims.models.LoanStatus;
import com.ims.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    private final LoanService loanService;

    @Autowired
    public NotificationService(EmailService emailService, LoanService loanService) {
        this.emailService = emailService;
        this.loanService = loanService;
    }

    @Async("notificationExecutor")
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

    @Async("notificationExecutor")
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

    @Async("notificationExecutor")
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

    // Scheduled reminder that runs every day at 10:00 AM
    @Scheduled(cron = "${notification.schedule.reminders:0 0 10 * * ?}")
    public void sendReturnReminders() {
        log.info("Starting scheduled return reminder check");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<LoanStatus> activeStatuses = Arrays.asList(LoanStatus.ACTIVE, LoanStatus.EXTENDED); // Adjust as necessary

        try {
            List<Loan> dueSoonLoans = loanService.findActiveLoansByReturnDate(tomorrow, activeStatuses);
            // Process the dueSoonLoans list as needed
        } catch (Exception e) {
            log.error("Error while sending return reminders", e);
        }
    }

    @Async("notificationExecutor")
    private void sendReturnReminder(Loan loan) {
        String subject = "Return Reminder: Items Due Tomorrow";
        String content = String.format("""
            Dear %s,
            
            This is a reminder that you have items due for return tomorrow.
            
            Loan Details:
            - Item: %s
            - Barcode: %s
            - Quantity: %d
            - Due Date: %s
            
            Please ensure to return the items by the due date to avoid any penalties.
            
            If you have already returned these items, please disregard this message.
            
            Best regards,
            Item Management System
            """,
                loan.getUser().getUsername(),
                loan.getItem().getDesignation(),
                loan.getItem().getBarcode(),
                loan.getQuantity(),
                loan.getReturnDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
        );

        try {
            emailService.sendSimpleMessage(loan.getUser().getEmail(), subject, content);
            log.debug("Sent reminder for loan ID: {}", loan.getId());
        } catch (Exception e) {
            log.error("Failed to send reminder for loan ID: {}", loan.getId(), e);
        }
    }
}
