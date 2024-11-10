package com.ims.models.dtos.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemReturnDto {
    private String username;
    private String designation;
    private String barcode;
    private int returnQuantity;
}
