package com.mycompany.app.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountCreationRequest {
    private Long userId;
    private String accountType;
    private BigDecimal initialDeposit;
    private String currency;
}
