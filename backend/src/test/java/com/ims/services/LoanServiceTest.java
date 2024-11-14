package com.ims.services;

import com.ims.exceptions.InvalidLoanStateException;
import com.ims.exceptions.InvalidQuantityException;
import com.ims.models.*;
import com.ims.models.dtos.request.CreateLoanDto;
import com.ims.models.dtos.request.ExtendLoanDto;
import com.ims.models.dtos.request.TerminateLoanDto;
import com.ims.models.dtos.response.LoanCreatedDto;
import com.ims.models.dtos.response.LoanDetailDto;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private LoanService loanService;

    private User testUser;
    private Item testItem;
    private Loan testLoan;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testUser");

        testItem = new Item();
        testItem.setId(1);
        testItem.setDesignation("Test Item");
        testItem.setBarcode("12345");

        testLoan = new Loan(testUser, testItem, 1,
                LocalDate.now(), LocalDate.now().plusDays(7));
        testLoan.setId(1);

        // Setup SecurityContext mock
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getActiveAndExtendedLoansForCurrentUser_Success() {
        // Arrange
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(loanRepository.findActiveAndExtendedLoansByUser(eq(testUser), any()))
                .thenReturn(List.of(testLoan));

        // Act
        List<LoanDetailDto> result = loanService.getActiveAndExtendedLoansForCurrentUser();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        LoanDetailDto dto = result.get(0);
        assertEquals(testItem.getDesignation(), dto.getItemDesignation());
        assertEquals(testItem.getBarcode(), dto.getItemBarcode());
    }

    @Test
    void findActiveLoan_Success() {
        // Arrange
        when(loanRepository.findByUserIdAndItemIdAndReturnDateIsNullAndEndDateGreaterThanEqual(
                eq(1), eq(1), any(LocalDate.class)
        )).thenReturn(Optional.of(testLoan));

        // Act
        Optional<Loan> result = loanService.findActiveLoan(1, 1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testLoan, result.get());
    }

    @Test
    void findActiveLoan_NullParameters_ThrowsException() {
        // Assert
        assertThrows(IllegalArgumentException.class, () ->
                loanService.findActiveLoan(null, 1));
        assertThrows(IllegalArgumentException.class, () ->
                loanService.findActiveLoan(1, null));
    }

    @Test
    void createLoan_Success() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));

        // Act
        loanService.createLoan(1, 1, 1, LocalDate.now(), LocalDate.now().plusDays(7));

        // Assert
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void createLoan_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        // Assert
        assertThrows(IllegalArgumentException.class, () ->
                loanService.createLoan(1, 1, 1, LocalDate.now(), LocalDate.now().plusDays(7)));
    }

    @Test
    void endLoan_Success() {
        // Arrange
        when(loanRepository.findById(1)).thenReturn(Optional.of(testLoan));

        // Act
        loanService.endLoan(1);

        // Assert
        verify(loanRepository, times(1)).save(any(Loan.class));
        assertNotNull(testLoan.getReturnDate());
        assertEquals(LoanStatus.RETURNED, testLoan.getStatus());
    }

    @Test
    void endLoan_AlreadyClosed_ThrowsException() {
        // Arrange
        testLoan.setReturnDate(LocalDate.now());
        when(loanRepository.findById(1)).thenReturn(Optional.of(testLoan));

        // Assert
        assertThrows(InvalidLoanStateException.class, () ->
                loanService.endLoan(1));
    }

    @Test
    void extendLoan_Success() {
        // Arrange
        LocalDate newEndDate = LocalDate.now().plusDays(14);
        when(loanRepository.findById(1)).thenReturn(Optional.of(testLoan));

        // Act
        LoanUpdatedDto result = loanService.extendLoan(1, newEndDate);

        // Assert
        verify(loanRepository, times(1)).save(any(Loan.class));
        assertEquals(newEndDate, testLoan.getEndDate());
        assertEquals(1, testLoan.getExtensionCount());
        assertNotNull(result);
        assertEquals(testItem.getDesignation(), result.getItemDesignation());
    }

    @Test
    void updateLoanQuantity_Success() {
        // Arrange
        when(loanRepository.findById(1)).thenReturn(Optional.of(testLoan));

        // Act
        loanService.updateLoanQuantity(1, 1);

        // Assert
        verify(loanRepository, times(1)).save(any(Loan.class));
        assertEquals(1, testLoan.getQuantity());
    }

    @Test
    void updateLoanQuantity_InvalidQuantity_ThrowsException() {
        // Arrange
        when(loanRepository.findById(1)).thenReturn(Optional.of(testLoan));

        // Assert
        assertThrows(InvalidQuantityException.class, () ->
                loanService.updateLoanQuantity(1, -1));
    }

    @Test
    void updateLoanQuantity_ZeroQuantity_EndsLoan() {
        // Arrange
        when(loanRepository.findById(1)).thenReturn(Optional.of(testLoan));

        // Act
        loanService.updateLoanQuantity(1, 0);

        // Assert
        verify(loanRepository, times(1)).save(any(Loan.class));
        assertNotNull(testLoan.getReturnDate());
        assertEquals(LoanStatus.RETURNED, testLoan.getStatus());
    }
}