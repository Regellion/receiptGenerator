package com.receipt.generator.dto;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingReceiptDto {
    private Long id;
    private Long roomId;
    private String userName;
    private Date startDate;
    private Date endDate;
}
