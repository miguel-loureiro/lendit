package com.ims.services;

import com.ims.exceptions.*;
import com.ims.models.*;
import com.ims.models.dtos.request.ItemRequestDto;
import com.ims.models.dtos.request.ItemReturnDto;
import com.ims.models.dtos.response.RequestedItemDto;
import com.ims.models.dtos.response.ReturnedItemDto;
import com.ims.repository.ItemRepository;
import com.ims.repository.ItemRequestRepository;
import com.ims.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ConcurrentModificationException;
import java.util.List;

@Service
@Transactional
@Slf4j
public class ItemRequestService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final LoanService loanService;
    private final NotificationService notificationService;

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 100;

    @Autowired
    public ItemRequestService(UserRepository userRepository,
                              ItemRepository itemRepository,
                              ItemRequestRepository itemRequestRepository,
                              LoanService loanService, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.itemRequestRepository = itemRequestRepository;
        this.loanService = loanService;
        this.notificationService = notificationService;
    }

    public ResponseEntity<RequestedItemDto> createItemRequest(ItemRequestDto input) {

        String userEmail = getCurrentUserEmail();

        validateRequestQuantity(input.getQuantity());

        User user = findAndValidateUser(userEmail);

        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                return attemptCreateItemRequest(input, user);
            } catch (ObjectOptimisticLockingFailureException e) {
                attempts++;
                if (attempts >= MAX_RETRIES) {
                    throw new ConcurrentModificationException(
                            "Failed to create item request after " + MAX_RETRIES + " attempts due to concurrent modifications");
                }
                try {
                    Thread.sleep(INITIAL_BACKOFF_MS * (long) Math.pow(2, attempts - 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ServiceException("Request interrupted while waiting to retry", ie);
                }
                log.warn("Optimistic lock failure, attempt {}/{}", attempts, MAX_RETRIES);
            }
        }
        throw new ServiceException("Unexpected exit from retry loop");
    }

    public ResponseEntity<ReturnedItemDto> returnItem(ItemReturnDto input) {

        String userEmail = getCurrentUserEmail();

        User user = findAndValidateUser(userEmail);
        Item item = itemRepository.findByDesignationOrBarcode(input.getDesignation(), input.getBarcode())
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        validateReturnQuantity(input.getReturnQuantity());

        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                return attemptReturnItem(input, user, item);
            } catch (ObjectOptimisticLockingFailureException e) {
                attempts++;
                if (attempts >= MAX_RETRIES) {
                    throw new ConcurrentModificationException(
                            "Failed to process item return after " + MAX_RETRIES + " attempts due to concurrent modifications");
                }
                try {
                    Thread.sleep(INITIAL_BACKOFF_MS * (long) Math.pow(2, attempts - 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ServiceException("Return process interrupted while waiting to retry", ie);
                }
                log.warn("Optimistic lock failure during return, attempt {}/{}", attempts, MAX_RETRIES);
            }
        }
        throw new ServiceException("Unexpected exit from return retry loop");
    }

    private static String getCurrentUserEmail() {

        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    private ResponseEntity<ReturnedItemDto> attemptReturnItem(ItemReturnDto input, User user, Item item) {
        // Verify active loan exists for this user and item
        Loan activeLoan = loanService.findActiveLoan(user.getId(), item.getId())
                .orElseThrow(() -> new LoanNotFoundException("No active loan found for this item and user"));

        // Validate return quantity against loan quantity
        if (input.getReturnQuantity() > activeLoan.getQuantity()) {
            throw new InvalidQuantityException(
                    String.format("Return quantity (%d) exceeds borrowed quantity (%d)",
                            input.getReturnQuantity(), activeLoan.getQuantity()));
        }

        // Update item's available quantity
        int newAvailableQuantity = item.getAvailableQuantity() + input.getReturnQuantity();
        if (newAvailableQuantity > item.getTotalQuantity()) {
            throw new InvalidQuantityException("Return would exceed total item quantity");
        }
        item.setAvailableQuantity(newAvailableQuantity);
        itemRepository.save(item);
        log.debug("Updated available quantity for item: {} - New Available: {}", item.getId(), newAvailableQuantity);

        // Close loan or update quantity
        if (input.getReturnQuantity() == activeLoan.getQuantity()) {
            loanService.endLoan(activeLoan.getId());
        } else {
            loanService.updateLoanQuantity(activeLoan.getId(),
                    activeLoan.getQuantity() - input.getReturnQuantity());
        }

        // Check for pending requests that can now be fulfilled
        processPendingRequests(item);

        // Send return confirmation
        notificationService.sendItemReturnConfirmation(
                user,
                item,
                input.getReturnQuantity(),
                activeLoan.getQuantity() - input.getReturnQuantity()
        );

        return ResponseEntity.ok(ReturnedItemDto.builder()
                .username(user.getUsername())
                .designation(item.getDesignation())
                .barcode(item.getBarcode())
                .returnedQuantity(input.getReturnQuantity())
                .remainingLoanQuantity(activeLoan.getQuantity() - input.getReturnQuantity())
                .build());
    }

    private void processPendingRequests(Item item) {
        List<ItemRequest> pendingRequests = itemRequestRepository
                .findByItemAndStatusOrderByQueuePosition(item, ItemRequestStatus.PENDING);

        for (ItemRequest request : pendingRequests) {
            if (canFulfillRequest(item, request.getRequestedQuantity())) {
                try {
                    fulfillRequest(request);
                } catch (Exception e) {
                    log.error("Failed to fulfill pending request {} after item return", request.getId(), e);
                    // Continue processing other requests even if one fails
                }
            } else {
                break; // Stop if we can't fulfill the next request
            }
        }
    }

    private void validateReturnQuantity(int returnQuantity) {
        if (returnQuantity <= 0) {
            throw new InvalidQuantityException("Return quantity must be greater than 0");
        }
    }

    private ResponseEntity<RequestedItemDto> attemptCreateItemRequest(ItemRequestDto input, User user) {
        Item item = itemRepository.findByDesignationOrBarcode(input.getDesignation(), input.getBarcode())
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        validateItemQuantities(item);
        validateAvailableQuantity(input.getQuantity(), item);

        // Create initial request without altering available quantity here
        ItemRequest itemRequest = createAndSaveInitialRequest(input, user, item);

        // Send confirmation email
        notificationService.sendItemRequestConfirmation(user, item, input.getQuantity());

        // Ensure fulfillRequest is only called if the request is pending
        if (canFulfillRequest(item, input.getQuantity()) && itemRequest.getStatus() == ItemRequestStatus.PENDING) {
            fulfillRequest(itemRequest); // this will handle the quantity reduction
        }

        return ResponseEntity.ok(RequestedItemDto.builder()
                .username(user.getUsername())
                .designation(item.getDesignation())
                .barcode(item.getBarcode())
                .requestedQuantity(input.getQuantity())
                .build());
    }

    @Transactional
    private void fulfillRequest(ItemRequest itemRequest) {
        // Check if the item request is already fulfilled
        if (itemRequest.getStatus() == ItemRequestStatus.FULFILLED) {
            log.debug("Item request with ID {} is already fulfilled, skipping.", itemRequest.getId());
            return;
        }

        Item item = itemRequest.getItem();
        validateItemQuantities(item);

        if (!canFulfillRequest(item, itemRequest.getRequestedQuantity())) {
            throw new InsufficientQuantityException(itemRequest.getRequestedQuantity(), item.getAvailableQuantity());
        }

        LocalDate returnDate = LocalDate.now().plusDays(30);

        // Create loan first - if this fails, the transaction will roll back
        createLoanForRequest(itemRequest, returnDate);

        // Update item quantity - this now happens exactly once
        log.debug("Calling updateItemQuantity for item: {} with requestedQuantity: {}", item.getId(), itemRequest.getRequestedQuantity());
        updateItemQuantity(item, itemRequest.getRequestedQuantity());
        log.debug("Completed updateItemQuantity for item: {}", item.getId());
        // Update request status to fulfilled
        itemRequest.setStatus(ItemRequestStatus.FULFILLED);
        itemRequest.setReturnDate(returnDate);
        itemRequestRepository.save(itemRequest);

        // Send fulfillment notification
        notificationService.sendRequestFulfillmentNotification(
                itemRequest.getUser(),
                itemRequest.getItem(),
                itemRequest.getRequestedQuantity(),
                returnDate
        );
    }

    private ItemRequest createAndSaveInitialRequest(ItemRequestDto input, User user, Item item) {
        LocalDate now = LocalDate.now();
        ItemRequest itemRequest = ItemRequest.builder()
                .user(user)
                .item(item)
                .requestedQuantity(input.getQuantity())
                .requestDate(now)
                .returnDate(now.plusDays(30))
                .queuePosition(getNextQueuePosition(item))
                .status(ItemRequestStatus.PENDING)
                .build();

        return itemRequestRepository.save(itemRequest);
    }

    private void updateItemQuantity(Item item, int requestedQuantity) {
        int newAvailableQuantity = item.getAvailableQuantity() - requestedQuantity;
        if (newAvailableQuantity < 0) {
            log.error("Attempted operation would result in negative available quantity. Item: {}, Requested: {}, Available: {}",
                    item.getId(), requestedQuantity, item.getAvailableQuantity());
            throw new InvalidQuantityException("Operation would result in negative quantity");
        }

        item.setAvailableQuantity(newAvailableQuantity);
        itemRepository.save(item);

        log.debug("Updated available quantity for item: {} - New Available: {}", item.getId(), newAvailableQuantity);
    }

    private void validateRequestQuantity(int requestedQuantity) {
        if (requestedQuantity <= 0) {
            throw new InvalidQuantityException("Requested quantity must be greater than 0");
        }
    }

    private User findAndValidateUser(String currentUserEmail) {
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Currently authenticated user not found XIXIXIXIXI in database: " + currentUserEmail));
    }

    private void validateItemQuantities(Item item) {
        if (item.getAvailableQuantity() > item.getTotalQuantity()) {
            throw new InvalidQuantityException("Available quantity cannot exceed total quantity");
        }
        if (item.getAvailableQuantity() < 0) {
            throw new InvalidQuantityException("Available quantity cannot be negative");
        }
    }

    private void validateAvailableQuantity(int requestedQuantity, Item item) {
        if (requestedQuantity > item.getTotalQuantity()) {
            throw new InsufficientQuantityException(
                    String.format("Requested quantity (%d) exceeds total item quantity (%d)",
                            requestedQuantity, item.getTotalQuantity()));
        }
    }

    private boolean canFulfillRequest(Item item, int requestedQuantity) {
        boolean canFulfill = item.getAvailableQuantity() >= requestedQuantity;
        log.debug("Checking fulfillment for item: {} - Requested: {}, Available: {}, Can Fulfill: {}",
                item.getId(), requestedQuantity, item.getAvailableQuantity(), canFulfill);
        return canFulfill;
    }

    private void createLoanForRequest(ItemRequest itemRequest, LocalDate returnDate) {
        try {
            loanService.createLoan(
                    itemRequest.getUser().getId(),
                    itemRequest.getItem().getId(),
                    itemRequest.getRequestedQuantity(),
                    LocalDate.now(),
                    returnDate
            );
        } catch (Exception e) {
            throw new LoanCreationException("Failed to create loan", e);
        }
    }

    private Integer getNextQueuePosition(Item item) {
        return itemRequestRepository.findMaxQueuePositionForItem(item.getId())
                .map(pos -> pos + 1)
                .orElse(1);
    }
}