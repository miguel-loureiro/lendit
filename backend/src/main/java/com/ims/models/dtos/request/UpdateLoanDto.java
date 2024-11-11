package com.ims.models.dtos.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateLoanDto {

    private Integer id;
    private LocalDate newReturnDate;
}
