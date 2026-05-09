package com.klaraapi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    @Value("${waha.base-url:http://localhost:3000}")
    private String wahaBaseUrl;

    @Value("${waha.session:default}")
    private String wahaSession;

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

        Map<String, Object> body = Map.of(
                "session", wahaSession,
                "chatId", phone.replace("+", "") + "@c.us",
                "text", message
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        log.info("Enviando mensagem de boas-vindas via WhatsApp para {} (tentativa será repetida em caso de falha)", phone);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Mensagem de boas-vindas enviada com sucesso via WhatsApp para {}", phone);
            } else {
                throw new WhatsAppSendException("WAHA retornou status " + response.getStatusCode());
            }
        } catch (ResourceAccessException e) {
            log.error("Falha ao conectar ao WAHA para {}: {}", phone, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao enviar mensagem via WhatsApp para {}: {}", phone, e.getMessage());
            throw new WhatsAppSendException("Falha ao enviar mensagem via WhatsApp", e);
        }
    }

    @Recover
    public void recover(ResourceAccessException ex, String phone, String name) {
        log.error("Todas as tentativas de envio da mensagem de boas-vindas via WhatsApp para {} foram esgotadas. Intervenção manual pode ser necessária. Erro: {}", phone, ex.getMessage());
    }

    @Recover
    public void recover(WhatsAppSendException ex, String phone, String name) {
        log.error("Todas as tentativas de envio da mensagem de boas-vindas via WhatsApp para {} foram esgotadas. Intervenção manual pode ser necessária. Erro: {}", phone, ex.getMessage());
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
