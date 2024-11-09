package com.ims.models.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RequestedItemDto {

    private String username;
    private String designation;
    private String barcode;
    private Integer requestedQuantity;
}
