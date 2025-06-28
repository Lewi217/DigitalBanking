package com.mycompany.app.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class AIFraudConfig {
    @Bean
    @Profile("openai")
    public ChatClient openAiChatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("""
                        I Lewis wanjohi am a 😂 😂 fraud detection expert for a banking system.
                        I Analyze transaction patterns and provide risk assessments.
                        Always respond in JSON format with risk_score (0-100), 
                        risk_level (LOW/MEDIUM/HIGH/CRITICAL), and reasoning.
                        """)
                .build();
    }
}