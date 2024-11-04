package com.ims.services;

import com.ims.models.Item;
import com.ims.models.ItemRequest;
import com.ims.models.ItemRequestStatus;
import com.ims.models.User;
import com.ims.repository.ItemRepository;
import com.ims.repository.ItemRequestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final LoanService loanService;

    @Value("${item.request.expiration.hours:24}")
    private int requestExpirationHours;

    public ItemRequest createRequest(User user, Item item) {
        // Check if user already has pending request
        if (hasPendingRequest(user, item)) {
            throw new IllegalStateException("User already has a pending request for this item");
        }

        // If item is available and no one is waiting, allow direct loan
        if (item.isAvailableForDirectLoan()) {
            throw new IllegalStateException("Item is available for direct loan");
        }

        // Get next position in queue
        int nextPosition = requestRepository.findMaxPositionByItem(item).orElse(0) + 1;

        ItemRequest request = new ItemRequest();
        request.setUser(user);
        request.setItem(item);
        request.setQueuePosition(nextPosition);

        request = requestRepository.save(request);

        return request;
    }

    @Transactional
    public void processItemReturn(Item item) {
        // If item has pending requests and is now available
        if (item.getAvailableQuantity() > 0) {
            item.getNextPendingRequest().ifPresent(request -> {
                request.setStatus(ItemRequestStatus.NOTIFIED);
                requestRepository.save(request);
            });
        }
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    public void processExpiredRequests() {
        LocalDateTime expirationThreshold = LocalDateTime.now()
                .minusHours(requestExpirationHours);

        List<ItemRequest> expiredRequests = requestRepository
                .findByStatusAndRequestDateBefore(
                        ItemRequestStatus.NOTIFIED,
                        expirationThreshold
                );

        for (ItemRequest request : expiredRequests) {
            request.setStatus(ItemRequestStatus.EXPIRED);
            requestRepository.save(request);

            // Reorder remaining requests
            reorderRequests(request.getItem());

            // Notify next person if item is still available
            processItemReturn(request.getItem());
        }
    }

    private void reorderRequests(Item item) {
        List<ItemRequest> pendingRequests = item.getPendingRequests();
        for (int i = 0; i < pendingRequests.size(); i++) {
            ItemRequest request = pendingRequests.get(i);
            request.setQueuePosition(i + 1);
            requestRepository.save(request);
        }
    }

    private boolean hasPendingRequest(User user, Item item) {
        return requestRepository.existsByUserAndItemAndStatusIn(
                user,
                item,
                Set.of(ItemRequestStatus.PENDING, ItemRequestStatus.NOTIFIED)
        );
    }
}