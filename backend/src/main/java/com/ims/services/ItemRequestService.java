package com.ims.services;

import com.ims.models.Item;
import com.ims.models.ItemRequest;
import com.ims.models.ItemRequestStatus;
import com.ims.models.User;
import com.ims.models.dtos.request.ItemRequestDto;
import com.ims.repository.ItemRepository;
import com.ims.repository.ItemRequestRepository;
import com.ims.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final LoanService loanService;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemRequest createItemRequest(ItemRequestDto input) {
        User user = userRepository.findByUsername(input.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Item item = itemRepository.findByDesignationOrBarcode(input.getDesignation(), input.getBarcode())
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        // Check if the item is available for direct loan
        if (item.isAvailableForDirectLoan(input.getRequestedQuantity())) {
            // Create the loan and fulfill the request
            createLoanAndFulfillRequest(user, item, input.getRequestedQuantity());
            return null; // No need to create a request
        } else {
            // Create a new item request and add it to the wait-list
            return createWaitlistRequest(user, item, input.getRequestedQuantity());
        }
    }

    private void createLoanAndFulfillRequest(User user, Item item, Integer requestedQuantity) {
        // Create the loan
        loanService.createLoan(
                user.getId(),
                item.getId(),
                requestedQuantity,
                LocalDate.now(),
                LocalDate.now().plusDays(30)
        );

        // Fulfill the request
        fulfillItemRequest(requestedQuantity);
    }

    private ItemRequest createWaitlistRequest(User user, Item item, Integer requestedQuantity) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setUser(user);
        itemRequest.setItem(item);
        itemRequest.setRequestedQuantity(requestedQuantity);
        itemRequest.setRequestDate(LocalDate.now());
        itemRequest.setQueuePosition(getNextQueuePosition(item));
        itemRequest.setStatus(ItemRequestStatus.WAITING);
        return itemRequestRepository.save(itemRequest);
    }

    public void fulfillItemRequest(Integer itemRequestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(itemRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Item request not found"));
        // Check if the item is available for direct loan
        if (itemRequest.getItem().isAvailableForDirectLoan(itemRequest.getRequestedQuantity())) {
            // Create the loan and fulfill the request
            createLoanAndFulfillRequest(itemRequest.getUser(), itemRequest.getItem(), itemRequest.getRequestedQuantity());
        } else {
            // Update the request status to WAITING
            itemRequest.setStatus(ItemRequestStatus.WAITING);
            itemRequestRepository.save(itemRequest);
        }
    }

    public void handleItemReturn(Integer itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        // Find the next request in the wait-list for the item
        ItemRequest nextRequest = findNextWaitlistRequest(item);
        if (nextRequest != null) {
            // Create the loan and fulfill the request
            createLoanAndFulfillRequest(nextRequest.getUser(), item, nextRequest.getRequestedQuantity());
        } else {
            // No more requests in the wait-list, mark the item as available
            item.setAvailableForDirectLoan(true);
            itemRepository.save(item);
        }
    }

    private ItemRequest findNextWaitlistRequest(Item item) {
        List<ItemRequest> waitlistRequests = itemRequestRepository.findByItemAndStatusOrderByQueuePositionAsc(item, ItemRequestStatus.WAITING);
        if (!waitlistRequests.isEmpty()) {
            return waitlistRequests.get(0);
        }
        return null;
    }

    private int getNextQueuePosition(Item item) {
        // Logic to determine the next queue position for the item request
        return itemRequestRepository.countByItemAndStatusIn(item, List.of(ItemRequestStatus.WAITING, ItemRequestStatus.FULFILLED)) + 1;
    }
}