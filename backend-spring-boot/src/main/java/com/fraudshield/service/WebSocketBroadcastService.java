package com.fraudshield.service;

import com.fraudshield.model.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastTransaction(TransactionResponse response) {
        try {
            // Push to live transaction feed
            messagingTemplate.convertAndSend("/topic/transactions", response);

            // Push to alerts channel if suspicious
            if ("BLOCK".equalsIgnoreCase(response.getDecision()) || "REVIEW".equalsIgnoreCase(response.getDecision())) {
                messagingTemplate.convertAndSend("/topic/alerts", response);
            }
        } catch (Exception ex) {
            log.debug("WebSocket broadcast skipped (client not connected): {}", ex.getMessage());
        }
    }
}
