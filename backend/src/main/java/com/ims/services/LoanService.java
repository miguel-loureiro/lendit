package com.ims.services;

import com.ims.models.*;
import com.ims.models.dtos.request.CreateLoanDto;
import com.ims.models.dtos.request.ExtendLoanDto;
import com.ims.models.dtos.request.TerminateLoanDto;
import com.ims.models.dtos.response.LoanCreatedDto;
import com.ims.models.dtos.response.LoanUpdatedDto;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public Loan createLoan(Integer userId, Integer itemId, Integer requestedQuantity, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

            Loan loan = new Loan(user, item, requestedQuantity, startDate, endDate);
            item.addActiveLoan(loan);
            return loanRepository.save(loan);
    }

    public LoanUpdatedDto terminateLoan(Integer loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
        Item item = loan.getItem();

        // Update the item's available quantity
        item.removeActiveLoan(loan);

        // Update the loan status and return date
        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(LocalDate.now());
        loanRepository.save(loan);
        return createLoanUpdatedDto(loan);
    }

    public LoanUpdatedDto extendLoan(Integer loanId, LocalDate newEndDate) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        // Update the loan end date and extension count
        loan.setPreviousExtendedDate(loan.getEndDate());
        loan.setEndDate(newEndDate);
        loan.setExtensionCount(loan.getExtensionCount() + 1);
        loanRepository.save(loan);
        return createLoanUpdatedDto(loan);
    }

    private LoanUpdatedDto createLoanUpdatedDto(Loan loan) {
        LocalDate returnDate = loan.getReturnDate();
        return LoanUpdatedDto.builder()
                .itemDesignation(loan.getItem().getDesignation())
                .username(loan.getUser().getUsername())
                .startDate(loan.getStartDate())
                .endDate(loan.getEndDate())
                .status(loan.getStatus())
                .returnDate(returnDate)
                .build();
    }
}