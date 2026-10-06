package com.fraudshield.repository;

import com.fraudshield.model.entity.TransactionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    Optional<TransactionEntity> findByTransactionId(String transactionId);

    List<TransactionEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    List<TransactionEntity> findByDecisionOrderByCreatedAtDesc(String decision, Pageable pageable);

    @Query("SELECT t FROM TransactionEntity t ORDER BY t.createdAt DESC")
    List<TransactionEntity> findRecentTransactions(Pageable pageable);

    long countByDecision(String decision);

    @Query("SELECT SUM(t.amount) FROM TransactionEntity t")
    BigDecimal sumTotalVolume();

    @Query("SELECT SUM(t.amount) FROM TransactionEntity t WHERE t.decision = 'BLOCK'")
    BigDecimal sumBlockedAmount();

    @Query("SELECT AVG(t.processingLatencyMs) FROM TransactionEntity t")
    Double findAverageLatency();

    @Query("SELECT t.location, COUNT(t) FROM TransactionEntity t WHERE t.decision = 'BLOCK' GROUP BY t.location")
    List<Object[]> countBlockedByLocation();

    @Query("SELECT t.merchantCategory, COUNT(t) FROM TransactionEntity t WHERE t.decision = 'BLOCK' GROUP BY t.merchantCategory")
    List<Object[]> countBlockedByCategory();

    @Query("SELECT t.userId, SUM(t.amount) FROM TransactionEntity t WHERE t.decision = 'BLOCK' GROUP BY t.userId ORDER BY SUM(t.amount) DESC")
    List<Object[]> findTopHighRiskUsers(Pageable pageable);
}
