package com.ims.services;

import com.ims.models.Item;
import com.ims.models.dtos.ItemDesignationAndCategoryDto;
import com.ims.models.dtos.request.CreateItemDto;
import com.ims.repository.ItemRepository;
import com.ims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class ItemService {

    @Autowired
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public ResponseEntity<Page<ItemDesignationAndCategoryDto>> getAllItems(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) throws IOException {

        Pageable paging = PageRequest.of(page, size, Sort.by("designation").ascending());
        Page<Item> itemsPage = itemRepository.findAll(paging);

        Page<ItemDesignationAndCategoryDto> itemDtos = itemsPage.map(ItemDesignationAndCategoryDto::new);

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
}
