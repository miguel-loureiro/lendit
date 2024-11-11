package com.ims.models.dtos.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemReturnDto {

    @NotEmpty
    @NotNull
    private String designation;
    @NotEmpty
    @NotNull
    private String barcode;
    @NotEmpty
    @NotNull
    private int returnQuantity;
}
