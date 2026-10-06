package com.fraudshield.controller;

import com.fraudshield.model.dto.TransactionRequest;
import com.fraudshield.model.dto.TransactionResponse;
import com.fraudshield.service.TransactionProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/simulate")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SimulationController {

    private final TransactionProducerService producerService;

    @PostMapping("/normal")
    public ResponseEntity<TransactionResponse> simulateNormal() {
        TransactionRequest req = TransactionRequest.builder()
                .transactionId("TXN-NORM-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .userId("U102")
                .amount(BigDecimal.valueOf(450.00))
                .location("Hyderabad")
                .latitude(17.3850)
                .longitude(78.4867)
                .deviceId("DEV_APPLE_12")
                .merchantCategory("Grocery")
                .timestamp("14:30")
                .build();
        return ResponseEntity.ok(producerService.dispatchTransaction(req));
    }

    @PostMapping("/midnight-attack")
    public ResponseEntity<TransactionResponse> simulateMidnightAttack() {
        TransactionRequest req = TransactionRequest.builder()
                .transactionId("TXN-MIDNIGHT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .userId("U102")
                .amount(BigDecimal.valueOf(95000.00))
                .location("Mumbai")
                .latitude(19.0760)
                .longitude(72.8777)
                .deviceId("DEV_UNKNOWN_998")
                .merchantCategory("Electronics")
                .timestamp("02:15")
                .build();
        return ResponseEntity.ok(producerService.dispatchTransaction(req));
    }

    @PostMapping("/impossible-travel")
    public ResponseEntity<List<TransactionResponse>> simulateImpossibleTravel() {
        List<TransactionResponse> results = new ArrayList<>();

        // Txn 1: Legitimate in Delhi
        TransactionRequest req1 = TransactionRequest.builder()
                .transactionId("TXN-LEGIT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .userId("U891")
                .amount(BigDecimal.valueOf(1200.00))
                .location("Delhi")
                .latitude(28.6139)
                .longitude(77.2090)
                .deviceId("DEV_SAMSUNG_S23")
                .merchantCategory("Food")
                .timestamp("10:00")
                .build();
        results.add(producerService.dispatchTransaction(req1));

        // Txn 2: 15 minutes later in London (impossible physical travel speed)
        TransactionRequest req2 = TransactionRequest.builder()
                .transactionId("TXN-IMPOSS-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .userId("U891")
                .amount(BigDecimal.valueOf(88000.00))
                .location("London")
                .latitude(51.5074)
                .longitude(-0.1278)
                .deviceId("DEV_FOREIGN_PROXY")
                .merchantCategory("Jewelry")
                .timestamp("10:15")
                .build();
        results.add(producerService.dispatchTransaction(req2));

        return ResponseEntity.ok(results);
    }

    @PostMapping("/card-testing-burst")
    public ResponseEntity<List<TransactionResponse>> simulateBurst() {
        List<TransactionResponse> results = new ArrayList<>();
        String victim = "U345";
        for (int i = 1; i <= 5; i++) {
            TransactionRequest req = TransactionRequest.builder()
                    .transactionId("TXN-BURST-" + i + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase())
                    .userId(victim)
                    .amount(BigDecimal.valueOf(99.00 * i))
                    .location("Unknown-VPN")
                    .latitude(37.7749)
                    .longitude(-122.4194)
                    .deviceId("BOTNET_EMULATOR_" + i)
                    .merchantCategory("Crypto")
                    .timestamp("03:45")
                    .build();
            results.add(producerService.dispatchTransaction(req));
        }
        return ResponseEntity.ok(results);
    }
}
