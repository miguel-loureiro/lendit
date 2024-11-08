package com.ims.services;

import com.ims.models.Item;
import com.ims.models.ItemRequest;
import com.ims.models.ItemRequestStatus;
import com.ims.models.dtos.ItemResponseDto;
import com.ims.models.dtos.request.CreateItemDto;
import com.ims.models.dtos.request.UpdateItemDto;
import com.ims.models.dtos.response.ItemCreatedDto;
import com.ims.models.dtos.response.ItemUpdatedDto;
import com.ims.repository.ItemRepository;
import com.ims.repository.ItemRequestRepository;
import com.ims.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    @Autowired
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public ResponseEntity<Page<ItemResponseDto>> getAllItems(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) throws IOException {

        Pageable paging = PageRequest.of(page, size, Sort.by("designation").ascending());
        Page<Item> itemsPage = itemRepository.findAll(paging);

        Page<ItemResponseDto> itemDtos = itemsPage.map(ItemResponseDto::new);

        return ResponseEntity.ok(itemDtos);
    }

    public ResponseEntity<Item> createItem(@RequestBody CreateItemDto createItemDto) {
        // Check if designation or barcode already exists
        if (itemRepository.findByDesignation(createItemDto.getDesignation()).isPresent()) {
            throw new IllegalArgumentException("An item with this designation already exists.");
        }
        if (itemRepository.findByBarcode(createItemDto.getBarcode()).isPresent()) {
            throw new IllegalArgumentException("An item with this barcode already exists.");
        }

        // Convert DTO to Item entity
        Item item = createItemDto.toItem();

        // Save the item to the repository
        Item savedItem = itemRepository.save(item);

        // Return the created item with a 201 Created status
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    public ResponseEntity<ItemUpdatedDto> updateItem(Integer id, UpdateItemDto updateDto) {
        log.info("Attempting to update item with ID: {}", id);

        // Find the existing item
        Item existingItem = itemRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Item not found with ID: {}", id);
                    return new EntityNotFoundException("Item not found with id: " + id);
                });

        // Check if the new designation or barcode already exists for a different item
        checkExistingDesignationAndBarcode(updateDto, existingItem);

        // Update the item
        existingItem.setDesignation(updateDto.getDesignation());
        existingItem.setBarcode(updateDto.getBarcode());
        existingItem.setBrand(updateDto.getBrand());
        existingItem.setCategory(updateDto.getCategory());
        existingItem.setPurchasePrice(updateDto.getPurchasePrice());
        existingItem.setStockQuantity(updateDto.getStockQuantity());

        // Save the updated item (JPA will handle the version check here)
        Item updatedItem = itemRepository.save(existingItem);

        // Convert to response DTO
        ItemUpdatedDto responseDto = ItemUpdatedDto.builder()
                .id(updatedItem.getId())
                .designation(updatedItem.getDesignation())
                .description(updatedItem.getDescription())
                .category(updatedItem.getCategory())
                .brand(updatedItem.getBrand())
                .barcode(updatedItem.getBarcode())
                .purchasePrice(updatedItem.getPurchasePrice())
                .stockQuantity(updatedItem.getStockQuantity())
                .version(updatedItem.getVersion())
                .build();

        log.info("Successfully updated item with ID: {}", id);
        return ResponseEntity.ok(responseDto);
    }

    public ResponseEntity<Void> deleteItem(Integer id) {
        log.info("Attempting to delete item with ID: {}", id);

        Optional<Item> itemOptional = itemRepository.findById(id);
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();

            // Check if the item has nay pending request
            List<ItemRequest> pendingRequests = itemRequestRepository.findByItemAndStatusIn(item, List.of(ItemRequestStatus.PENDING));
            if (!pendingRequests.isEmpty()) {
                log.error("Cannot delete item with ID {} due to pending requests", id);
                throw new IllegalStateException("Cannot delete item with pending requests");
            }
            // No pending requests, so delete the item
            itemRepository.delete(item);
            log.info("Successfully deleted item with ID: {}", id);
            return ResponseEntity.noContent().<Void>build();

        } else {
            log.error("Item not found with ID: {}", id);
            throw new EntityNotFoundException("Item not found with id: " + id);
        }
    }

    private void checkExistingDesignationAndBarcode(UpdateItemDto updateDto, Item existingItem) {
        if (!existingItem.getDesignation().equals(updateDto.getDesignation()) &&
                itemRepository.findByDesignation(updateDto.getDesignation()).isPresent()) {
            throw new IllegalArgumentException("An item with this designation already exists.");
        }

        if (!existingItem.getBarcode().equals(updateDto.getBarcode()) &&
                itemRepository.findByBarcode(updateDto.getBarcode()).isPresent()) {
            throw new IllegalArgumentException("An item with this barcode already exists.");
        }
    }
}
