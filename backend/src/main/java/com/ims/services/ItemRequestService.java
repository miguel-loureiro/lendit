package com.ims.services;

import com.ims.exceptions.*;
import com.ims.models.Item;
import com.ims.models.ItemRequest;
import com.ims.models.ItemRequestStatus;
import com.ims.models.User;
import com.ims.models.dtos.request.ItemRequestDto;
import com.ims.models.dtos.response.RequestedItemDto;
import com.ims.repository.ItemRepository;
import com.ims.repository.ItemRequestRepository;
import com.ims.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class ItemRequestService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final LoanService loanService;

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 100;

    @Autowired
    public ItemRequestService(UserRepository userRepository,
                              ItemRepository itemRepository,
                              ItemRequestRepository itemRequestRepository,
                              LoanService loanService) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.itemRequestRepository = itemRequestRepository;
        this.loanService = loanService;
    }

    public ResponseEntity<RequestedItemDto> createItemRequest(ItemRequestDto input) {
        validateRequestQuantity(input.getRequestedQuantity());

        User user = findAndValidateUser(input.getUsername());

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

    private ResponseEntity<RequestedItemDto> attemptCreateItemRequest(ItemRequestDto input, User user) {
        Item item = itemRepository.findByDesignationOrBarcode(input.getDesignation(), input.getBarcode())
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        validateItemQuantities(item);
        validateAvailableQuantity(input.getRequestedQuantity(), item);

        // Create initial request without altering available quantity here
        ItemRequest itemRequest = createAndSaveInitialRequest(input, user, item);

        // Ensure fulfillRequest is only called if the request is pending
        if (canFulfillRequest(item, input.getRequestedQuantity()) && itemRequest.getStatus() == ItemRequestStatus.PENDING) {
            fulfillRequest(itemRequest); // this will handle the quantity reduction
        }

        return ResponseEntity.ok(RequestedItemDto.builder()
                .username(user.getUsername())
                .designation(item.getDesignation())
                .barcode(item.getBarcode())
                .requestedQuantity(input.getRequestedQuantity())
                .build());
    }

    @Transactional
    private ItemRequest fulfillRequest(ItemRequest itemRequest) {
        // Check if the item request is already fulfilled
        if (itemRequest.getStatus() == ItemRequestStatus.FULFILLED) {
            log.debug("Item request with ID {} is already fulfilled, skipping.", itemRequest.getId());
            return itemRequest;
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
        return itemRequestRepository.save(itemRequest);
    }

    private ItemRequest createAndSaveInitialRequest(ItemRequestDto input, User user, Item item) {
        LocalDate now = LocalDate.now();
        ItemRequest itemRequest = ItemRequest.builder()
                .user(user)
                .item(item)
                .requestedQuantity(input.getRequestedQuantity())
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

    private User findAndValidateUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
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
        // Implementation depends on your queueing strategy
        return itemRequestRepository.findMaxQueuePositionForItem(item.getId())
                .map(pos -> pos + 1)
                .orElse(1);
    }
}