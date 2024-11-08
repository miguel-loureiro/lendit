package com.ims.controllers;

import com.ims.models.Item;
import com.ims.models.dtos.request.*;
import com.ims.models.dtos.response.ItemUpdatedDto;
import com.ims.models.dtos.response.LoanCreatedDto;
import com.ims.models.dtos.response.LoanUpdatedDto;
import com.ims.models.dtos.response.UserCreatedDto;
import com.ims.services.ItemService;
import com.ims.services.LoanService;
import com.ims.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final ItemService itemService;
    private final UserService userService;
    private final LoanService loanService;

    @GetMapping("/items")
    public ResponseEntity<?> getAvailableItems (@RequestParam(name = "page", defaultValue = "0") int page,
                                                @RequestParam(name = "size", defaultValue = "10") int size) throws IOException {
    return itemService.getAllItems(page, size);
    }

    @PostMapping("/items/new")
    public ResponseEntity<Item> createItem(@RequestBody CreateItemDto createItemDto) {
        return itemService.createItem(createItemDto);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ItemUpdatedDto> updateItem(@PathVariable Integer id, @RequestBody UpdateItemDto updateDto) {
        return itemService.updateItem(id, updateDto);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Integer id) {
        return itemService.deleteItem(id);
    }

    @PutMapping("loans/extend/{id}")
    public ResponseEntity<LoanUpdatedDto> extendLoan(@PathVariable Integer id, @RequestBody ExtendLoanDto input) {
        LoanUpdatedDto loanUpdatedDto = loanService.extendLoan(id, input.getNewEndDate());
        return ResponseEntity.ok(loanUpdatedDto);
    }

    @PutMapping("loans/end/{id}")
    public ResponseEntity<LoanUpdatedDto> terminateLoan(@PathVariable Integer id) {
        LoanUpdatedDto loanUpdatedDto = loanService.terminateLoan(id);
        return ResponseEntity.ok(loanUpdatedDto);
    }

    @PostMapping("/clients/new")
    public ResponseEntity<UserCreatedDto> createClient(@RequestBody CreateUserDto createClientDto) {
        UserCreatedDto clientCreatedDto = userService.createUser(createClientDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(clientCreatedDto);
    }
}

