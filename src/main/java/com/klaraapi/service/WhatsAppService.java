package com.klaraapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class WhatsAppService {

    @Value("${waha.base-url:http://localhost:3000}")
    private String wahaBaseUrl;

    @Value("${waha.session:default}")
    private String wahaSession;

    @Value("${waha.api-key:}")
    private String wahaApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    @Retryable(
            retryFor = {ResourceAccessException.class, WhatsAppSendException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void sendWelcomeMessage(String phone, String name) {
        String greetingName = name.contains(" ") ? name.substring(0, name.indexOf(' ')) : name;
        String message = buildWelcomeMessage(greetingName);

        String url = wahaBaseUrl + "/api/sendText";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (wahaApiKey != null && !wahaApiKey.isBlank()) {
            headers.set("X-Api-Key", wahaApiKey);
        }

        Map<String, Object> body = Map.of(
                "session", wahaSession,
                "chatId", phone.replace("+", "") + "@c.us",
                "text", message
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        log.info("Sending WhatsApp welcome message to {} (will retry on failure)", phone);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("WhatsApp welcome message sent successfully to {}", phone);
            } else {
                throw new WhatsAppSendException("WAHA returned status " + response.getStatusCode());
            }
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to WAHA for {}: {}", phone, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error sending WhatsApp message to {}: {}", phone, e.getMessage());
            throw new WhatsAppSendException("Failed to send WhatsApp message", e);
        }
    }

    @Recover
    public void recover(ResourceAccessException ex, String phone, String name) {
        log.error("All attempts to send WhatsApp welcome message to {} have been exhausted. Manual intervention may be needed. Error: {}", phone, ex.getMessage());
    }

    @Recover
    public void recover(WhatsAppSendException ex, String phone, String name) {
        log.error("All attempts to send WhatsApp welcome message to {} have been exhausted. Manual intervention may be needed. Error: {}", phone, ex.getMessage());
    }

    private String buildWelcomeMessage(String name) {
        return "Olá, " + name + "! Aqui é a Klara 💚\n\n" +
                "Seja muito bem-vindo(a) à plataforma que vai te ajudar a enxergar seu dinheiro com clareza e tomar decisões mais inteligentes sobre o seu futuro financeiro.\n\n" +
                "A partir de agora, você tem tudo o que precisa para organizar, acompanhar e evoluir nas suas finanças — de um jeito simples e sem enrolação.\n\n" +
                "Feito com 💚 para quem quer clareza nas finanças.";
    }

    public static class WhatsAppSendException extends RuntimeException {
        public WhatsAppSendException(String message) {
            super(message);
        }
        public WhatsAppSendException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
