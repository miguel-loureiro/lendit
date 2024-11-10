package com.ims.models.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReturnedItemDto {
    private String username;
    private String designation;
    private String barcode;
    private int returnedQuantity;
    private int remainingLoanQuantity;
}
