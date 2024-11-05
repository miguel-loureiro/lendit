package com.ims.controllers;

import com.ims.models.Item;
import com.ims.models.dtos.request.CreateItemDto;
import com.ims.services.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final ItemService itemService;

    @PostMapping("/items/new")
    public ResponseEntity<Item> createItem(@RequestBody CreateItemDto createItemDto) {
        return itemService.createItem(createItemDto);
    }
}
