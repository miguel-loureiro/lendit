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

    public LoanUpdatedDto extendLoan(ExtendLoanDto extendLoanDto) {
        Loan loan = loanRepository.findById(extendLoanDto.getLoanId())
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
        System.out.println("Loan status: " + loan.getStatus());

        // Can only extend ACTIVE or EXTENDED loans
        if (!loan.getStatus().canBeExtended()) {
            throw new IllegalStateException("Can only extend active or extended loans");
        }

        validateExtensionDates(loan, extendLoanDto.getNewEndDate());

        // Create a new version of the loan with updated status and end date
        LocalDate originalEndDate = loan.getEndDate();
        loan.setEndDate(extendLoanDto.getNewEndDate());
        loan.setStatus(LoanStatus.EXTENDED);
        loan.setPreviousExtendedDate(originalEndDate);

        // Save will automatically increment the version number due to @Version
        loan = loanRepository.save(loan);

        return createLoanUpdatedDto(loan);
    }

    public LoanUpdatedDto terminateLoan(TerminateLoanDto terminateLoanDto) {
        Loan loan = loanRepository.findById(terminateLoanDto.getLoanId())
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
        // Can terminate both ACTIVE and EXTENDED loans
        if (loan.getStatus() != LoanStatus.ACTIVE && loan.getStatus() != LoanStatus.EXTENDED) {
            throw new IllegalStateException("Can only terminate active or extended loans");
        }
        LocalDate returnDate = terminateLoanDto.getReturnDate() != null ?
                terminateLoanDto.getReturnDate() : LocalDate.now();
        validateTerminationDate(loan, returnDate);
        // Update loan status and return date while maintaining history
        loan.terminate(returnDate);
        // Save will automatically increment the version number due to @Version
        loan = loanRepository.save(loan);
        // Update item availability but keep the loan record
        Item item = loan.getItem();
        item.removeActiveLoan(loan);
        itemRepository.save(item);
        return createLoanUpdatedDto(loan);
    }

    private void validateExtensionDates(Loan loan, LocalDate newEndDate) {
        if (newEndDate.isBefore(loan.getEndDate())) {
            throw new IllegalArgumentException("New end date must be after current end date");
        }

        if (newEndDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("New end date cannot be in the past");
        }

        System.out.println("Current loan end date: " + loan.getEndDate());
        System.out.println("New end date: " + newEndDate);
    }

    private void validateTerminationDate(Loan loan, LocalDate returnDate) {
        if (returnDate.isBefore(loan.getStartDate())) {
            throw new IllegalArgumentException("Return date cannot be before loan start date");
        }
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