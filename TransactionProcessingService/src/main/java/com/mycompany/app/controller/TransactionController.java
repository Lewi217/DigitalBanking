package com.mycompany.app.controller;

import com.mycompany.app.dto.TransactionDto;
import com.mycompany.app.dto.TransactionRequest;
import com.mycompany.app.exceptions.CustomExceptionResponse;
import com.mycompany.app.response.ApiResponse;
import com.mycompany.app.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_ERROR_MESSAGE;
import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_SUCCESS_MESSAGE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static sun.security.timestamp.TSResponse.BAD_REQUEST;

@RestController
@RequestMapping("${api.prefix}/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> create(@RequestBody TransactionRequest req) {
        try {
            TransactionDto transactionDto = transactionService.process(req);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, transactionDto));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "An unexpected error occurred: " + e.getMessage()));
        }
    }
    @GetMapping("/history/{accountId}")
    public ResponseEntity<ApiResponse> history(@PathVariable String accountId) {
        try {
            List<TransactionDto> list = transactionService.history(accountId);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, list));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to retrieve transaction history: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> search(@RequestParam Instant from, @RequestParam Instant to) {
        try {
            List<TransactionDto> results = transactionService.search(from, to);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, results));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Error searching transactions: " + e.getMessage()));
        }
    }
}
