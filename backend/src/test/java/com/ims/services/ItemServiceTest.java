package com.ims.services;

import com.ims.models.*;
import com.ims.models.dtos.ItemResponseDto;
import com.ims.models.dtos.request.CreateItemDto;
import com.ims.models.dtos.request.UpdateItemDto;
import com.ims.models.dtos.response.ItemUpdatedDto;
import com.ims.repository.ItemRepository;
import com.ims.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

public class ItemServiceTest {

    @InjectMocks
    private ItemService itemService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    private Item existingItem;
    private UpdateItemDto updateDto;
    private Item item;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        item = new Item();
        item.setId(1);

        existingItem = new Item();
        existingItem.setId(1);
        existingItem.setDesignation("Old Designation");
        existingItem.setBarcode("123456789");
        existingItem.setBrand("Old Brand");
        existingItem.setCategory("Storage device");
        existingItem.setPurchasePrice(BigDecimal.valueOf(199.0));
        existingItem.setTotalQuantity(10);
        existingItem.setVersion(1L);

        updateDto = UpdateItemDto.builder()
                .designation("New Designation")
                .barcode("987654321")
                .brand("New Brand")
                .category("Laptop")
                .purchasePrice(BigDecimal.valueOf(356.83))
                .stockQuantity(15)
                .build();
    }

    private Item createItemWithActiveLoan() {
        Item item = new Item();
        item.setId(1);
        item.setActiveLoans(createActiveLoans());
        return item;
    }

    private Item createItemWithPendingRequests() {
        Item item = new Item();
        item.setId(1);
        item.setActiveLoans(new HashSet<>());
        return item;
    }

    private Set<Loan> createActiveLoans() {
        Set<Loan> activeLoanSet = new HashSet<>();
        Loan activeLoan = new Loan();
        activeLoan.setStatus(LoanStatus.ACTIVE);
        activeLoanSet.add(activeLoan);
        return activeLoanSet;
    }

    private Set<ItemRequest> createPendingRequests() {
        Set<ItemRequest> pendingRequestSet = new HashSet<>();
        ItemRequest pendingRequest = new ItemRequest();
        pendingRequest.setStatus(ItemRequestStatus.PENDING);
        pendingRequestSet.add(pendingRequest);
        return pendingRequestSet;
    }

    @Test
    public void getAllItems_ShouldReturnPagedItems() throws IOException {
        // Arrange
        Item item1 = new Item("Item1", "1234567890123", "Brand1", "Laptop", BigDecimal.valueOf(100.00), 10);
        Item item2 = new Item("Item2", "1234567890124", "Brand2", "Peripheral", BigDecimal.valueOf(200.00), 20);
        Page<Item> itemsPage = new PageImpl<>(Arrays.asList(item1, item2));

        when(itemRepository.findAll(any(Pageable.class))).thenReturn(itemsPage);

        // Act
        ResponseEntity<Page<ItemResponseDto>> response = itemService.getAllItems(0, 10);

        // Assert
        assertEquals(OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).getContent().size());
        verify(itemRepository).findAll(any(Pageable.class));
    }

    @Test
    public void createItem_ShouldReturnCreatedItem_WhenItemIsValid() {
        // Arrange
        CreateItemDto createItemDto = new CreateItemDto("Item1", "1234567890123", "Brand1", "Storage device", BigDecimal.valueOf(100.00), 10);
        Item itemToSave = createItemDto.toItem();
        Item savedItem = new Item("Item1", "1234567890123", "Brand1", "Desktop", BigDecimal.valueOf(100.00), 10);
        savedItem.setId(1); // Assuming the ID is generated after saving

        when(itemRepository.findByDesignation(createItemDto.getDesignation())).thenReturn(Optional.empty());
        when(itemRepository.findByBarcode(createItemDto.getBarcode())).thenReturn(Optional.empty());
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        // Act
        ResponseEntity<Item> response = itemService.createItem(createItemDto);

        // Assert
        assertEquals(CREATED, response.getStatusCode());
        assertEquals(savedItem, response.getBody());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    public void createItem_ShouldThrowException_WhenDesignationExists() {
        // Arrange
        CreateItemDto createItemDto = new CreateItemDto("Item1", "1234567890123", "Brand1", "Peripheral", BigDecimal.valueOf(100.00), 10);
        when(itemRepository.findByDesignation(createItemDto.getDesignation())).thenReturn(Optional.of(new Item()));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            itemService.createItem(createItemDto);
        });
        assertEquals("An item with this designation already exists.", exception.getMessage());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    public void testCreateItem_ExistingBarcode() {
        // Arrange
        CreateItemDto createItemDto = new CreateItemDto();
        createItemDto.setDesignation("Test Item");
        createItemDto.setBarcode("123456789012");
        createItemDto.setBrand("Test Brand");
        createItemDto.setCategory("Desktop");
        createItemDto.setPurchasePrice(BigDecimal.TEN);
        createItemDto.setStockQuantity(10);

        when(itemRepository.findByDesignation(createItemDto.getDesignation())).thenReturn(Optional.empty());
        when(itemRepository.findByBarcode(createItemDto.getBarcode())).thenReturn(Optional.of(new Item()));

        // Act and Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> itemService.createItem(createItemDto));
        assertEquals("An item with this barcode already exists.", exception.getMessage());
    }


    @Test
    public void testUpdateItem_Success() {
        when(itemRepository.findById(1)).thenReturn(Optional.of(existingItem));
        when(itemRepository.findByDesignation("New Designation")).thenReturn(Optional.empty());
        when(itemRepository.findByBarcode("987654321")).thenReturn(Optional.empty());
        when(itemRepository.save(any(Item.class))).thenReturn(existingItem);

        ResponseEntity<ItemUpdatedDto> response = itemService.updateItem(1, updateDto);

        assertEquals(OK, response.getStatusCode());
        ItemUpdatedDto responseDto = response.getBody();
        assertNotNull(responseDto);
        assertEquals("New Designation", responseDto.getDesignation());
        assertEquals("987654321", responseDto.getBarcode());
    }

    @Test
    public void testUpdateItem_ItemNotFound() {
        when(itemRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.updateItem(1, updateDto));
    }

    @Test
    public void testUpdateItem_DesignationAlreadyExists() {
        when(itemRepository.findById(1)).thenReturn(Optional.of(existingItem));
        when(itemRepository.findByDesignation("New Designation")).thenReturn(Optional.of(new Item()));

        assertThrows(IllegalArgumentException.class, () -> itemService.updateItem(1, updateDto));
    }

    @Test
    public void testUpdateItem_BarcodeAlreadyExists() {
        when(itemRepository.findById(1)).thenReturn(Optional.of(existingItem));
        when(itemRepository.findByDesignation("New Designation")).thenReturn(Optional.empty());
        when(itemRepository.findByBarcode("987654321")).thenReturn(Optional.of(new Item()));

        assertThrows(IllegalArgumentException.class, () -> itemService.updateItem(1, updateDto));
    }

    @Test
    public void testDeleteItem_Success() {
        // Arrange
        Integer itemId = 1;
        Item item = new Item();
        item.setId(itemId);

        // Mock no active loans or pending requests
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // Act
        ResponseEntity<Void> response = itemService.deleteItem(itemId);

        // Assert
        assertEquals(ResponseEntity.noContent().build(), response);
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, times(1)).delete(item);
    }

    @Test
    public void testDeleteItem_ItemNotFound() {
        // Arrange
        Integer itemId = 1;
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(EntityNotFoundException.class, () -> itemService.deleteItem(itemId));
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, never()).delete(any());
    }

    @Test
    public void testDeleteItem_ItemHasActiveLoan() {
        // Arrange
        Integer itemId = 1;
        Item item = createItemWithActiveLoan();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // Act and Assert
        assertThrows(IllegalStateException.class, () -> itemService.deleteItem(itemId));
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, never()).delete(any());
    }

    @Test
    public void testDeleteItem_ItemHasPendingRequests() {
        // Arrange
        Integer itemId = 1;
        Item item = createItemWithPendingRequests();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // Act and Assert
        assertThrows(IllegalStateException.class, () -> itemService.deleteItem(itemId));
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, never()).delete(any());
    }
}
