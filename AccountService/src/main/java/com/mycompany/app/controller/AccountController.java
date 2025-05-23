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

import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_ERROR_MESSAGE;
import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_SUCCESS_MESSAGE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping("${api.prefix}/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final IAccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createAccount(@RequestBody AccountCreationRequest request) {
        try {
            Account account = accountService.createAccount(request);
            AccountDto dto = accountService.convertToDto(account);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, dto));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(
                    new ApiResponse(REQUEST_ERROR_MESSAGE, "An unexpected error occurred: " + e.getMessage())
            );
        }
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse> getAccountById(@PathVariable Long accountId) {
        try {
            Account account = accountService.getAccountById(accountId);
            AccountDto dto = accountService.convertToDto(account);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, dto));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(
                    new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to retrieve account: " + e.getMessage())
            );
        }
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<ApiResponse> getAccountByNumber(@PathVariable String accountNumber) {
        try {
            Account account = accountService.getAccountByNumber(accountNumber);
            AccountDto dto = accountService.convertToDto(account);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, dto));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(
                    new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to retrieve account: " + e.getMessage())
            );
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getAccountByUserId(@PathVariable Long userId) {
        try {
            List<Account> accounts = accountService.getAccountsByUserId(userId);
            List<AccountDto> dtos = accounts.stream().map(accountService::convertToDto).collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, dtos));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(
                    new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to retrieve accounts: " + e.getMessage())
            );
        }
    }

    @PutMapping("/{accountId}/status")
    public ResponseEntity<ApiResponse> updateAccountStatus(@PathVariable Long accountId, @RequestParam boolean active) {
        try {
            Account account = accountService.updateAccountStatus(accountId, active);
            AccountDto dto = accountService.convertToDto(account);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, dto));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(
                    new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to update account status: " + e.getMessage())
            );
        }
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<ApiResponse> deleteAccount(@PathVariable Long accountId) {
        try {
            accountService.deleteAccount(accountId);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, null));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(
                    new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to delete account: " + e.getMessage())
            );
        }
    }
}
