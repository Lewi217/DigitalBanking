package com.mycompany.app.config;

import com.mycompany.app.client.NotificationServiceClient;
import com.mycompany.app.events.TransactionCreatedEvent;
import com.mycompany.app.model.FraudAnalysis;
import com.mycompany.app.service.AIEnhancedFraudAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AIFraudDetectionListener {

    private final AIEnhancedFraudAnalysisService fraudAnalysisService;
    private final NotificationServiceClient notificationClient;

    @EventListener
    @Async("fraudDetectionExecutor")
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        try {
                    event.getTransaction().getId();

            FraudAnalysis analysis = fraudAnalysisService.analyzeTransactionWithAI(
                    event.getTransaction());
            if (analysis.getRiskLevel().ordinal() >= 2) {
                sendFraudAlert(analysis);
            }

            log.info("AI fraud analysis completed. Risk Level: {}, Score: {}",
                    analysis.getRiskLevel(), analysis.getRiskScore());

        } catch (Exception e) {
        }
    }

    private void sendFraudAlert(FraudAnalysis analysis) {
        try {
            notificationClient.sendTransactionNotification(
                    analysis.getAccountId(),
                    "AI_FRAUD_ALERT",
                    analysis.getAmount().toString(),
                    analysis.getAccountId()
            );
        } catch (Exception e) {
        }
    }
}
