package com.ims.services;

import com.ims.models.*;
import com.ims.models.dtos.request.CreateLoanDto;
import com.ims.models.dtos.request.ExtendLoanDto;
import com.ims.models.dtos.request.TerminateLoanDto;
import com.ims.models.dtos.response.LoanCreatedDto;
import com.ims.models.dtos.response.LoanUpdatedDto;
import com.ims.repository.ItemRepository;
import com.ims.repository.LoanRepository;
import com.ims.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoanService loanService;

    @Mock
    private User user;
    @Mock
    private Item item;
    @Mock
    private Loan loan;

    @BeforeEach
    void setUp() {

    }
/*
    @Test
    void startLoan_Success() {
        CreateLoanDto createLoanDto = new CreateLoanDto(1, 1, LocalDate.now(), LocalDate.now().plusDays(7));

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1)).thenReturn(Optional.of(item));
        System.out.println("Is item a mock? " + Mockito.mockingDetails(item).getMock());
        when(item.isAvailableForDirectLoan()).thenReturn(true); // This will now work as item is a mock
        when(user.hasActiveLoanForItem(item)).thenReturn(false);
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        LoanCreatedDto result = loanService.startLoan(createLoanDto);

        assertNotNull(result);
        assertEquals("testItem", result.getItemDesignation());
        assertEquals("testUser  ", result.getUsername());
        assertEquals(loan.getStartDate(), result.getStartDate());
        assertEquals(loan.getEndDate(), result.getEndDate());
        verify(loanRepository).save(any(Loan.class));
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void startLoan_UserNotFound() {
        CreateLoanDto createLoanDto = new CreateLoanDto(1, 1, LocalDate.now(), LocalDate.now().plusDays(7));

        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> loanService.startLoan(createLoanDto));
    }

    @Test
    void startLoan_ItemNotFound() {
        CreateLoanDto createLoanDto = new CreateLoanDto(1, 1, LocalDate.now(), LocalDate.now().plusDays(7));

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> loanService.startLoan(createLoanDto));
    }

    @Test
    void startLoan_ItemNotAvailable() {
        CreateLoanDto createLoanDto = new CreateLoanDto(1, 1, LocalDate.now(), LocalDate.now().plusDays(7));

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1)).thenReturn(Optional.of(item));
        when(item.isAvailableForDirectLoan()).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> loanService.startLoan(createLoanDto));
    }

    @Test
    void extendLoan_Success() {
        ExtendLoanDto extendLoanDto = new ExtendLoanDto(1, LocalDate.now().plusDays(10));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setEndDate(LocalDate.now().plusDays(7));

        when(loanRepository.findById(1)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        LoanUpdatedDto result = loanService.extendLoan(extendLoanDto);

        assertNotNull(result);
        assertEquals(LocalDate.now().plusDays(10), result.getEndDate());
        assertEquals(LoanStatus.EXTENDED, loan.getStatus());
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void extendLoan_LoanNotFound() {
        ExtendLoanDto extendLoanDto = new ExtendLoanDto(1, LocalDate.now().plusDays(10));

        when(loanRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> loanService.extendLoan(extendLoanDto));
    }

    @Test
    void extendLoan_CannotExtendLoan() {
        ExtendLoanDto extendLoanDto = new ExtendLoanDto(1, LocalDate.now().plusDays(10));
        loan.setStatus(LoanStatus.TERMINATED); // Assuming COMPLETED cannot be extended

        when(loanRepository.findById(1)).thenReturn(Optional.of(loan));

        assertThrows(IllegalStateException.class, () -> loanService.extendLoan(extendLoanDto));
    }

    @Test
    void terminateLoan_Success() {
        TerminateLoanDto terminateLoanDto = new TerminateLoanDto(1, LocalDate.now());
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setReturnDate(null);

        when(loanRepository.findById(1)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        LoanUpdatedDto result = loanService.terminateLoan(terminateLoanDto);

        assertNotNull(result);
        assertEquals(LoanStatus.TERMINATED, loan.getStatus());
        assertEquals(LocalDate.now(), loan.getReturnDate());
        verify(loanRepository).save(any(Loan.class));
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void terminateLoan_LoanNotFound() {
        TerminateLoanDto terminateLoanDto = new TerminateLoanDto(1, LocalDate.now());

        when(loanRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> loanService.terminateLoan(terminateLoanDto));
    }

    @Test
    void terminateLoan_CannotTerminateLoan() {
        TerminateLoanDto terminateLoanDto = new TerminateLoanDto(1, LocalDate.now());
        loan.setStatus(LoanStatus.TERMINATED); // Assuming OVERDUE cannot be terminated

        when(loanRepository.findById(1)).thenReturn(Optional.of(loan));

        assertThrows(IllegalStateException.class, () -> loanService.terminateLoan(terminateLoanDto));
    }

 */
}