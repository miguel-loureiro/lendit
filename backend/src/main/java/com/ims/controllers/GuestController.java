package com.ims.controllers;

import com.ims.models.dtos.ItemDesignationAndCategoryDto;
import com.ims.repository.UserRepository;
import com.ims.services.ItemService;
import com.ims.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequestMapping("/guest")
@RestController
public class GuestController {

    private final ItemService itemService;


    public GuestController( ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/items")
    public ResponseEntity<Page<ItemDesignationAndCategoryDto>> getAllItems(@RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "10") int size) throws IOException {
       return ResponseEntity.ok(itemService.getAllItems(page, size).getBody());
    }
}
