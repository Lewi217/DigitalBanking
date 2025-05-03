package com.mycompany.app.services;

import com.mycompany.app.dto.AccountCreationRequest;
import com.mycompany.app.dto.AccountDto;
import com.mycompany.app.model.Account;

import java.util.List;

public interface IAccountService {
    Account createAccount(AccountCreationRequest request);
    Account getAccountById(Long accountId);
    Account getAccountByNumber(String accountNumber);
    List<Account> getAccountsByUserId(Long userId);
    void deleteAccount(Long accountId);
    Account updateAccountStatus(Long accountId, boolean active);
    AccountDto convertToDto(Account account);
}
