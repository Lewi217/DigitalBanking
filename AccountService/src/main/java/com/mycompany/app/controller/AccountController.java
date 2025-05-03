package com.mycompany.app.controller;

import com.mycompany.app.dto.AccountCreationRequest;
import com.mycompany.app.dto.AccountDto;
import com.mycompany.app.exceptions.CustomExceptionResponse;
import com.mycompany.app.model.Account;
import com.mycompany.app.response.ApiResponse;
import com.mycompany.app.services.IAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/accounts")
public class AccountController {
    private final IAccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createAccount(@RequestBody AccountCreationRequest request){
        try{
            Account account = accountService.createAccount(request);
            AccountDto accountDto = accountService.convertToDto(account);
            return ResponseEntity.ok(new ApiResponse("Account created successfully", accountDto));
        }catch(CustomExceptionResponse e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Error creating account", e.getMessage()));
        }
    }
    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse> getAccountById(@PathVariable Long accountId){
        try{
            Account account = accountService.getAccountById(accountId);
            AccountDto accountDto = accountService.convertToDto(account);
            return ResponseEntity.ok(new ApiResponse("Account retrieved successfully", accountDto));
        }catch(CustomExceptionResponse e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Error retrieving account", e.getMessage()));
        }
    }
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<ApiResponse> getAccountByNumber(@PathVariable String accountNumber){
        try{
            Account account = accountService.getAccountByNumber(accountNumber);
            AccountDto accountDto = accountService.convertToDto(account);
            return ResponseEntity.ok(new ApiResponse("Account retrieved successfully", accountDto));
        }catch(CustomExceptionResponse e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Error retrieving account", e.getMessage()));
        }
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getAccountByUserId(@PathVariable Long userId){
        try{
            List<Account> accounts = accountService.getAccountsByUserId(userId);
            List<AccountDto> accountsDtos = accounts.stream()
                    .map(accountService::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse("Accounts retrieved successfully", accountsDtos));
        } catch(CustomExceptionResponse e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Error retrieving accounts", e.getMessage()));
        }
    }
    @PutMapping("/{accountId}/status")
    public ResponseEntity<ApiResponse> updateAccountStatus(
            @PathVariable Long accountId,
            @RequestParam boolean active
    ){
        try{
            Account account = accountService.updateAccountStatus(accountId,active);
            AccountDto accountDto = accountService.convertToDto(account);
            return ResponseEntity.ok(new ApiResponse("Account status updated successfully", accountDto));
        }catch(CustomExceptionResponse e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Error updating account status", e.getMessage()));
        }
    }
    @DeleteMapping("/{accountId}")
    public ResponseEntity<ApiResponse> deleteAccount(@PathVariable Long accountId){
        try{
            accountService.deleteAccount(accountId);
            return ResponseEntity.ok(new ApiResponse("Account deleted successfully", null));
        }catch(CustomExceptionResponse e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Error deleting account", e.getMessage()));
        }
    }
}
