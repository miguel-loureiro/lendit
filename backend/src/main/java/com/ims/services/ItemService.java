package com.ims.services;

import com.ims.models.Item;
import com.ims.models.dtos.ItemDesignationAndCategoryDto;
import com.ims.repository.ItemRepository;
import com.ims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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

        Page<ItemDesignationAndCategoryDto> itemDtos = itemsPage.map(item -> new ItemDesignationAndCategoryDto(item));

        return ResponseEntity.ok(itemDtos);
    }
}
