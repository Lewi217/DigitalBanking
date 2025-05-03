package com.mycompany.app.dto;

import lombok.Data;

@Data
public class AccountDto {
    private Long id;
    private String accountNumber;
    private String userId;
    private String accountType;
    private String balance;
    private String currency;
    private boolean active;
}
