package com.fraudshield.controller;

import com.fraudshield.model.dto.DashboardStatsDto;
import com.fraudshield.model.entity.TransactionEntity;
import com.fraudshield.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final TransactionRepository transactionRepository;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        long total = transactionRepository.count();
        long approved = transactionRepository.countByDecision("APPROVE");
        long review = transactionRepository.countByDecision("REVIEW");
        long blocked = transactionRepository.countByDecision("BLOCK");

        double fraudRate = total > 0 ? ((double) blocked / total) * 100.0 : 0.0;
        BigDecimal totalVolume = transactionRepository.sumTotalVolume();
        if (totalVolume == null) totalVolume = BigDecimal.ZERO;

        BigDecimal blockedAmount = transactionRepository.sumBlockedAmount();
        if (blockedAmount == null) blockedAmount = BigDecimal.ZERO;

        Double avgLatency = transactionRepository.findAverageLatency();
        if (avgLatency == null) avgLatency = 8.5; // typical default ms

        Map<String, Long> decisionBreakdown = new LinkedHashMap<>();
        decisionBreakdown.put("APPROVE", approved);
        decisionBreakdown.put("REVIEW", review);
        decisionBreakdown.put("BLOCK", blocked);

        Map<String, Long> locationFraudStats = new LinkedHashMap<>();
        for (Object[] row : transactionRepository.countBlockedByLocation()) {
            if (row[0] != null) {
                locationFraudStats.put((String) row[0], ((Number) row[1]).longValue());
            }
        }

        Map<String, Long> categoryRiskStats = new LinkedHashMap<>();
        for (Object[] row : transactionRepository.countBlockedByCategory()) {
            if (row[0] != null) {
                categoryRiskStats.put((String) row[0], ((Number) row[1]).longValue());
            }
        }

        DashboardStatsDto stats = DashboardStatsDto.builder()
                .totalTransactions(total)
                .approvedCount(approved)
                .reviewCount(review)
                .blockedCount(blocked)
                .fraudRatePercentage(Math.round(fraudRate * 100.0) / 100.0)
                .totalVolume(totalVolume.setScale(2, RoundingMode.HALF_UP))
                .blockedAmount(blockedAmount.setScale(2, RoundingMode.HALF_UP))
                .avgLatencyMs(Math.round(avgLatency * 10.0) / 10.0)
                .decisionBreakdown(decisionBreakdown)
                .locationFraudStats(locationFraudStats)
                .categoryRiskStats(categoryRiskStats)
                .build();

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<TransactionEntity>> getRecentAlerts(@RequestParam(defaultValue = "15") int limit) {
        List<TransactionEntity> alerts = transactionRepository.findByDecisionOrderByCreatedAtDesc(
                "BLOCK", PageRequest.of(0, limit)
        );
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/high-risk-users")
    public ResponseEntity<List<Map<String, Object>>> getHighRiskUsers(@RequestParam(defaultValue = "10") int limit) {
        List<Object[]> rows = transactionRepository.findTopHighRiskUsers(PageRequest.of(0, limit));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", row[0]);
            map.put("blockedAmount", row[1]);
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }
}
