package com.ims.controllers;

import com.ims.models.Item;
import com.ims.models.ItemRequest;
import com.ims.models.Loan;
import com.ims.models.User;
import com.ims.models.dtos.request.CreateItemDto;
import com.ims.models.dtos.request.ItemRequestDto;
import com.ims.models.dtos.request.ItemReturnDto;
import com.ims.models.dtos.request.UpdateLoanDto;
import com.ims.models.dtos.response.LoanDetailDto;
import com.ims.models.dtos.response.LoanUpdatedDto;
import com.ims.models.dtos.response.RequestedItemDto;
import com.ims.models.dtos.response.ReturnedItemDto;
import com.ims.repository.ItemRepository;
import com.ims.repository.UserRepository;
import com.ims.services.ItemRequestService;
import com.ims.services.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {

    private final ItemRequestService itemRequestService;
    private final LoanService loanService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @PostMapping("/item/request")
    public ResponseEntity<RequestedItemDto> requestItem(@RequestBody ItemRequestDto input) {
        return itemRequestService.createItemRequest(input);
    }

    @PutMapping("/item/return")
    public ResponseEntity<ReturnedItemDto> returnItem(@RequestBody ItemReturnDto input) {
        return itemRequestService.returnItem(input);
    }

    @GetMapping("/loan/list")
    public ResponseEntity<List<LoanDetailDto>> getActiveAndExtendedLoans() {
        List<LoanDetailDto> loans = loanService.getActiveAndExtendedLoansForCurrentUser ();
        return ResponseEntity.ok(loans);
    }

    @PutMapping("/loan/extend/{id}")
    public ResponseEntity<LoanUpdatedDto> extendLoan(@RequestParam Integer id,  @RequestBody UpdateLoanDto input) {
        LoanUpdatedDto newLoan = loanService.extendLoan(id, input.getNewReturnDate());
        return ResponseEntity.ok(newLoan);
    }
}
