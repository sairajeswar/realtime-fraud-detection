package com.fraudshield.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudshield.model.dto.TransactionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaTransactionConsumer {

    private final TransactionProcessingOrchestrator orchestrator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(
            topics = "${fraud.kafka.inbound-topic:transactions.inbound}",
            groupId = "fraud-engine-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTransactionEvent(String message) {
        try {
            log.info("Kafka consumer received transaction payload from topic");
            TransactionRequest request = objectMapper.readValue(message, TransactionRequest.class);
            orchestrator.processTransaction(request);
        } catch (Exception e) {
            log.error("Failed to parse/process message from Kafka: {}", e.getMessage());
        }
    }
}
