package com.ims.controllers;

import com.ims.models.Item;
import com.ims.models.ItemRequest;
import com.ims.models.User;
import com.ims.models.dtos.request.CreateItemDto;
import com.ims.models.dtos.request.ItemRequestDto;
import com.ims.models.dtos.response.RequestedItemDto;
import com.ims.repository.ItemRepository;
import com.ims.repository.UserRepository;
import com.ims.services.ItemRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {

    private final ItemRequestService itemRequestService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @PostMapping("/request_item")
    public ResponseEntity<RequestedItemDto> createItemRequest(@RequestBody ItemRequestDto input) {
        return itemRequestService.createItemRequest(input);
    }


}
