package com.fraudshield.controller;

import com.fraudshield.model.dto.TransactionRequest;
import com.fraudshield.model.dto.TransactionResponse;
import com.fraudshield.model.entity.TransactionEntity;
import com.fraudshield.repository.TransactionRepository;
import com.fraudshield.service.TransactionProducerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionProducerService producerService;
    private final TransactionRepository transactionRepository;

    @PostMapping
    public ResponseEntity<TransactionResponse> submitTransaction(@Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = producerService.dispatchTransaction(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TransactionEntity>> getRecentTransactions(
            @RequestParam(defaultValue = "50") int limit) {
        List<TransactionEntity> list = transactionRepository.findRecentTransactions(PageRequest.of(0, limit));
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionEntity> getTransactionById(@PathVariable String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
