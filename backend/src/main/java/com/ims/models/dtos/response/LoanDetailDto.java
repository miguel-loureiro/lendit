package com.ims.models.dtos.response;

import com.ims.models.Item;
import com.ims.models.LoanStatus;
import com.ims.models.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LoanDetailDto {

        private Integer id;
        private String itemDesignation;
        private String itemBarcode;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDate initialEndDate;
        private LoanStatus status;
        private LocalDate previousExtendedDate;
        private LocalDate returnDate;
        private Integer extensionCount;
        private Integer quantity;
}
