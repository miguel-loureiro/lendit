package com.ims.services;

import com.ims.models.*;
import com.ims.repository.ItemRepository;
import com.ims.repository.ItemRequestRepository;
import com.ims.repository.LoanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;
    private final ItemRequestRepository requestRepository;

    public Loan createLoan(User user, Item item, LocalDateTime startDate, LocalDateTime endDate) {
        // Check if item is available
        if (!item.isAvailable()) {
            throw new IllegalStateException("Item is not available for loan");
        }

        // Check if user has been notified about availability
        Optional<ItemRequest> userRequest = requestRepository
                .findByUserAndItemAndStatus(user, item, ItemRequestStatus.NOTIFIED);

        // If there are pending requests and user is not next in line
        if (!item.getPendingRequests().isEmpty() && userRequest.isEmpty()) {
            throw new IllegalStateException("There are users waiting in line for this item");
        }

        // Create the loan
        Loan loan = new Loan(user, item, startDate, endDate);
        loan = loanRepository.save(loan);

        // Update request status if it existed
        userRequest.ifPresent(request -> {
            request.setStatus(ItemRequestStatus.FULFILLED);
            requestRepository.save(request);
        });

        return loan;
    }
}