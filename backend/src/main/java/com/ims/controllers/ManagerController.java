package com.ims.controllers;

import com.ims.models.Item;
import com.ims.models.dtos.request.CreateItemDto;
import com.ims.models.dtos.request.CreateLoanDto;
import com.ims.models.dtos.request.UpdateItemDto;
import com.ims.models.dtos.response.ItemUpdatedDto;
import com.ims.models.dtos.response.LoanCreatedDto;
import com.ims.services.ItemService;
import com.ims.services.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final ItemService itemService;
    private final LoanService loanService;

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

    @PostMapping("loans/new")
    public ResponseEntity<LoanCreatedDto> createLoan(@RequestBody CreateLoanDto createLoanDto) {
        LoanCreatedDto loanCreatedDto = loanService.startLoan(createLoanDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(loanCreatedDto);
    }
}
