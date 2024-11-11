package com.ims.services;

import com.ims.exceptions.InvalidLoanStateException;
import com.ims.exceptions.InvalidQuantityException;
import com.ims.exceptions.LoanNotFoundException;
import com.ims.exceptions.ServiceException;
import com.ims.models.*;
import com.ims.models.dtos.response.LoanDetailDto;
import com.ims.models.dtos.response.LoanUpdatedDto;
import com.ims.repository.ItemRepository;
import com.ims.repository.LoanRepository;
import com.ims.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LoanService {
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    /**
     * Retrieves all active and extended loans for the current user.
     *
     * @return List of loans that are either active or extended
     */
    public List<LoanDetailDto> getActiveAndExtendedLoansForCurrentUser () {
        String currentUsername = getCurrentUsernameString();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("User  not found"));

        List<Loan> loans = loanRepository.findActiveAndExtendedLoansByUser (user, List.of(LoanStatus.ACTIVE, LoanStatus.EXTENDED));

        // Map Loan entities to LoanDetailDto
        return loans.stream()
                .map(this::mapToLoanDetailDto)
                .collect(Collectors.toList());
    }

    /**
     * Finds an active loan for a specific user and item.
     * A loan is considered active if it has not been returned and has not expired.
     *
     * @param userId The ID of the user who borrowed the item
     * @param itemId The ID of the borrowed item
     * @return Optional containing the active loan if found, empty otherwise
     */
    public Optional<Loan> findActiveLoan(Integer userId, Integer itemId) {
        if (userId == null || itemId == null) {
            throw new IllegalArgumentException("User ID and Item ID must not be null");
        }

        return loanRepository.findByUserIdAndItemIdAndReturnDateIsNullAndEndDateGreaterThanEqual(
                userId,
                itemId,
                LocalDate.now()
        );
    }

    public Loan createLoan(Integer userId, Integer itemId, Integer requestedQuantity, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

            Loan loan = new Loan(user, item, requestedQuantity, startDate, endDate);
            item.addActiveLoan(loan);
            return loanRepository.save(loan);
    }

    /**
     * Closes a loan by setting its return date to the current date.
     * Validates the loan exists and is active before closing.
     *
     * @param loanId The ID of the loan to close
     * @return
     * @throws LoanNotFoundException     if the loan doesn't exist
     * @throws InvalidLoanStateException if the loan is already closed
     */
    @Transactional
    public LoanUpdatedDto endLoan(Integer loanId) {
        if (loanId == null) {
            throw new IllegalArgumentException("Loan ID must not be null");
        }

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with ID: " + loanId));

        if (loan.getReturnDate() != null) {
            throw new InvalidLoanStateException("Loan is already closed");
        }

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);

        try {
            loanRepository.save(loan);
            log.debug("Successfully closed loan with ID: {}", loanId);
        } catch (Exception e) {
            log.error("Failed to close loan with ID: {}", loanId, e);
            throw new ServiceException("Failed to close loan", e);
        }
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

    /**
     * Updates the quantity of items in an active loan.
     * Validates the loan exists, is active, and the new quantity is valid.
     *
     * @param loanId The ID of the loan to update
     * @param newQuantity The new quantity for the loan
     * @throws LoanNotFoundException if the loan doesn't exist
     * @throws InvalidLoanStateException if the loan is already closed
     * @throws InvalidQuantityException if the new quantity is invalid
     */
    @Transactional
    public void updateLoanQuantity(Integer loanId, int newQuantity) {
        if (loanId == null) {
            throw new IllegalArgumentException("Loan ID must not be null");
        }

        if (newQuantity < 0) {
            throw new InvalidQuantityException("New quantity cannot be negative");
        }

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with ID: " + loanId));

        if (loan.getReturnDate() != null) {
            throw new InvalidLoanStateException("Cannot update quantity of a closed loan");
        }

        if (newQuantity == 0) {
            endLoan(loanId);
            return;
        }

        if (newQuantity > loan.getQuantity()) {
            throw new InvalidQuantityException(
                    String.format("New quantity (%d) cannot be greater than original quantity (%d)",
                            newQuantity, loan.getQuantity()));
        }

        loan.setQuantity(newQuantity);

        try {
            loanRepository.save(loan);
            log.debug("Successfully updated quantity for loan ID: {} to {}", loanId, newQuantity);
        } catch (Exception e) {
            log.error("Failed to update quantity for loan ID: {}", loanId, e);
            throw new ServiceException("Failed to update loan quantity", e);
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

    private static String getCurrentUsernameString() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return username;
    }

    private LoanDetailDto mapToLoanDetailDto(Loan loan) {
        LoanDetailDto dto = new LoanDetailDto();
        dto.setId(loan.getId());
        dto.setItemDesignation(loan.getItem().getDesignation());
        dto.setItemBarcode(loan.getItem().getBarcode());
        dto.setStartDate(loan.getStartDate());
        dto.setEndDate(loan.getEndDate());
        dto.setInitialEndDate(loan.getInitialEndDate());
        dto.setStatus(loan.getStatus());
        dto.setPreviousExtendedDate(loan.getPreviousExtendedDate());
        dto.setReturnDate(loan.getReturnDate());
        dto.setExtensionCount(loan.getExtensionCount());
        dto.setQuantity(loan.getQuantity());
        return dto;
    }
}