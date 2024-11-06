package com.ims.services;

import com.ims.models.*;
import com.ims.models.dtos.request.CreateLoanDto;
import com.ims.models.dtos.response.LoanCreatedDto;
import com.ims.repository.ItemRepository;
import com.ims.repository.ItemRequestRepository;
import com.ims.repository.LoanRepository;
import com.ims.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
@Transactional
public class LoanService {
    private final LoanRepository loanRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public LoanService(LoanRepository loanRepository, ItemRepository itemRepository, UserRepository userRepository) {
        this.loanRepository = loanRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    public LoanCreatedDto startLoan(CreateLoanDto createLoanDto) {
        // Fetch the user and item
        User user = userRepository.findById(createLoanDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Item item = itemRepository.findById(createLoanDto.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        // Check if the item is available for direct loan
        if (!item.isAvailableForDirectLoan()) {
            throw new IllegalStateException("Item is not available for direct loan");
        }

        // Check if the user has any active or overdue loans for the same item
        if (user.hasActiveLoanForItem(item)) {
            throw new IllegalStateException("User already has an active loan for this item");
        }

        // Create the new loan
        Loan loan = new Loan(user, item, createLoanDto.getStartDate(), createLoanDto.getEndDate());
        loan = loanRepository.save(loan);

        // Update the item's available quantity
        item.addActiveLoan(loan);
        itemRepository.save(item);

        // Create and return the LoanCreatedDto
        return LoanCreatedDto.builder()
                .itemDesignation(item.getDesignation())
                .username(user.getUsername())
                .startDate(loan.getStartDate())
                .endDate(loan.getEndDate())
                .build();
    }
}