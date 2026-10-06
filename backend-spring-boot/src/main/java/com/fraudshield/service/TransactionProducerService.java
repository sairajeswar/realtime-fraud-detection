package com.fraudshield.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudshield.model.dto.TransactionRequest;
import com.fraudshield.model.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TransactionProcessingOrchestrator orchestrator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${fraud.kafka.inbound-topic:transactions.inbound}")
    private String inboundTopic;

    public TransactionResponse dispatchTransaction(TransactionRequest request) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(request);
            kafkaTemplate.send(inboundTopic, request.getUserId(), jsonPayload);
            log.debug("Sent transaction {} to Kafka topic {}", request.getTransactionId(), inboundTopic);
        } catch (Exception ex) {
            log.warn("Kafka broker dispatch fallback: Kafka unreachable ({}), processing synchronously", ex.getMessage());
        }

        // Return processed result directly for real-time API response guarantee
        return orchestrator.processTransaction(request);
    }
}
