package com.mycompany.app.services;

import com.mycompany.app.client.AuthServiceClient;
import com.mycompany.app.dto.AccountCreationRequest;
import com.mycompany.app.dto.AccountDto;
import com.mycompany.app.dto.UserDto;
import com.mycompany.app.exceptions.CustomExceptionResponse;
import com.mycompany.app.model.Account;
import com.mycompany.app.repository.AccountRepository;
import com.mycompany.app.utilitis.secreteGeneratorAcount.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService {
    private final AccountRepository accountRepository;
    private final AuthServiceClient authServiceClient;
    private final ModelMapper mapper;
    private final AccountNumberGenerator accountNumberGenerator;

    @Override
    public Account createAccount(AccountCreationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUserEmail = authentication.getName();

        try {
            UserDto user = authServiceClient.getUserById(request.getUserId());
        } catch (Exception e) {
            throw new CustomExceptionResponse("User verification failed: " + e.getMessage());
        }

        Account account = new Account();
        account.setUserId(request.getUserId());
        account.setAccountNumber(accountNumberGenerator.generate());
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO);
        account.setCurrency(request.getCurrency());
        account.setActive(true);
        account.setCreatedAt(LocalDateTime.now());

        return accountRepository.save(account);
    }

    @Override
    public List<Account> getAccountsByUserId(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUserEmail = authentication.getName();

        try {
            UserDto user = authServiceClient.getUserById(userId);
        } catch (Exception e) {
            throw new CustomExceptionResponse("User verification failed: " + e.getMessage());
        }

        return accountRepository.findByUserId(userId);
    }

    @Override
    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomExceptionResponse("Account not found with ID: " + accountId));
    }

    @Override
    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new CustomExceptionResponse("Account not found with number: " + accountNumber));
    }

    @Override
    public void deleteAccount(Long accountId) {
        Account account = getAccountById(accountId);
        accountRepository.delete(account);
    }

    @Override
    public Account updateAccountStatus(Long accountId, boolean active) {
        Account account = getAccountById(accountId);
        account.setActive(active);
        account.setUpdatedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }

    @Override
    public AccountDto convertToDto(Account account) {
        return mapper.map(account, AccountDto.class);
    }
}
