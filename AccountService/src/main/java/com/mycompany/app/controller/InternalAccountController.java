package com.mycompany.app.controller;

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
@RequestMapping("/internal/accounts")
@RequiredArgsConstructor
public class InternalAccountController {
    private final IAccountService accountService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getUserAccounts(@PathVariable("userId") Long userId) {
        if (userId == null || userId <= 0) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Invalid user ID"));
        }

        try {
            List<Account> accounts = accountService.getAccountsByUserId(userId);

            if (accounts == null) {
                accounts = List.of();
            }

            List<AccountDto> dtos = accounts.stream()
                    .map(accountService::convertToDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, dtos));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to retrieve accounts: " + e.getMessage()));
        }
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse> getAccountById(@PathVariable("accountId") Long accountId) {
        try {
            Account account = accountService.getAccountById(accountId);
            AccountDto dto = accountService.convertToDto(account);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, dto));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to retrieve account: " + e.getMessage()));
        }
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<ApiResponse> getAccountByNumber(@PathVariable("accountNumber") String accountNumber) {
        try {
            Account account = accountService.getAccountByNumber(accountNumber);
            AccountDto dto = accountService.convertToDto(account);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, dto));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to retrieve account: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse> healthCheck() {
        return ResponseEntity.ok(new ApiResponse("Service is healthy", "OK"));
    }
}
