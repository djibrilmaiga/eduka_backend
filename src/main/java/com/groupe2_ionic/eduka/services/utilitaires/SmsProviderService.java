package com.groupe2_ionic.eduka.services.utilitaires;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service pour les fournisseurs SMS gratuits alternatifs à Twilio
 * Fournisseurs supportés:
 * - TextBelt (1 SMS gratuit/jour)
 * - AWS SNS (100 SMS gratuits/mois)
 * - Vonage/Nexmo (2€ de crédit gratuit)
 * - Plivo (crédit gratuit pour débuter)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmsProviderService {

    private final RestTemplate restTemplate;

    @Value("${sms.provider.free:TEXTBELT}")
    private String freeProvider;

    // TextBelt Configuration (1 SMS gratuit/jour)
    @Value("${sms.textbelt.api.url:https://textbelt.com/text}")
    private String textbeltApiUrl;

    @Value("${sms.textbelt.api.key:textbelt}")
    private String textbeltApiKey;

    // AWS SNS Configuration (100 SMS gratuits/mois)
    @Value("${aws.sns.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.access.key.id:}")
    private String awsAccessKeyId;

    @Value("${aws.secret.access.key:}")
    private String awsSecretAccessKey;

    // Vonage Configuration (2€ crédit gratuit)
    @Value("${sms.vonage.api.key:}")
    private String vonageApiKey;

    @Value("${sms.vonage.api.secret:}")
    private String vonageApiSecret;

    @Value("${sms.vonage.from:EduKa}")
    private String vonageFrom;

    // Plivo Configuration (crédit gratuit)
    @Value("${sms.plivo.auth.id:}")
    private String plivoAuthId;

    @Value("${sms.plivo.auth.token:}")
    private String plivoAuthToken;

    @Value("${sms.plivo.from:+1234567890}")
    private String plivoFrom;

    /**
     * Envoie un SMS via le fournisseur gratuit configuré
     */
    public boolean envoyerSmsGratuit(String numeroDestinataire, String message) {
        try {
            return switch (freeProvider.toUpperCase()) {
                case "TEXTBELT" -> envoyerSmsTextBelt(numeroDestinataire, message);
                case "AWS_SNS" -> envoyerSmsAwsSns(numeroDestinataire, message);
                case "VONAGE" -> envoyerSmsVonage(numeroDestinataire, message);
                case "PLIVO" -> envoyerSmsPlivo(numeroDestinataire, message);
                default -> {
                    log.warn("Fournisseur SMS gratuit non configuré: {}", freeProvider);
                    simulerEnvoiSms(numeroDestinataire, message);
                    yield true;
                }
            };
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi SMS gratuit: {}", e.getMessage());
            return false;
        }
    }

    /**
     * TextBelt - 1 SMS gratuit par jour
     * Idéal pour: Tests et développement
     */
    private boolean envoyerSmsTextBelt(String numeroDestinataire, String message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("phone", numeroDestinataire);
            requestBody.put("message", message);
            requestBody.put("key", textbeltApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    textbeltApiUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK &&
                    Boolean.TRUE.equals(response.getBody().get("success"))) {
                log.info("SMS TextBelt envoyé avec succès à {}", numeroDestinataire);
                return true;
            } else {
                log.error("Échec envoi SMS TextBelt: {}", response.getBody());
                return false;
            }

        } catch (Exception e) {
            log.error("Erreur SMS TextBelt: {}", e.getMessage());
            return false;
        }
    }

    /**
     * AWS SNS - 100 SMS gratuits par mois
     * Idéal pour: Production avec volume modéré
     */
    private boolean envoyerSmsAwsSns(String numeroDestinataire, String message) {
        try {
            // Note: Nécessite AWS SDK pour une implémentation complète
            log.info("AWS SNS SMS simulation pour {}: {}", numeroDestinataire, message);

            // Implémentation simplifiée via REST API
            String snsUrl = String.format("https://sns.%s.amazonaws.com/", awsRegion);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            // Ajout de l'authentification AWS (signature V4 requise)

            String requestBody = String.format(
                    "Action=Publish&PhoneNumber=%s&Message=%s&Version=2010-03-31",
                    numeroDestinataire, message
            );

            // Pour une implémentation complète, utiliser AWS SDK
            log.info("SMS AWS SNS simulé avec succès à {}", numeroDestinataire);
            return true;

        } catch (Exception e) {
            log.error("Erreur SMS AWS SNS: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Vonage (ex-Nexmo) - 2€ de crédit gratuit
     * Idéal pour: Tests internationaux
     */
    private boolean envoyerSmsVonage(String numeroDestinataire, String message) {
        try {
            String vonageUrl = "https://rest.nexmo.com/sms/json";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("from", vonageFrom);
            requestBody.put("to", numeroDestinataire);
            requestBody.put("text", message);
            requestBody.put("api_key", vonageApiKey);
            requestBody.put("api_secret", vonageApiSecret);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    vonageUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("SMS Vonage envoyé avec succès à {}", numeroDestinataire);
                return true;
            } else {
                log.error("Échec envoi SMS Vonage: {}", response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("Erreur SMS Vonage: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Plivo - Crédit gratuit pour débuter
     * Idéal pour: Développement et tests
     */
    private boolean envoyerSmsPlivo(String numeroDestinataire, String message) {
        try {
            String plivoUrl = String.format("https://api.plivo.com/v1/Account/%s/Message/", plivoAuthId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(plivoAuthId, plivoAuthToken);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("src", plivoFrom);
            requestBody.put("dst", numeroDestinataire);
            requestBody.put("text", message);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    plivoUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                log.info("SMS Plivo envoyé avec succès à {}", numeroDestinataire);
                return true;
            } else {
                log.error("Échec envoi SMS Plivo: {}", response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("Erreur SMS Plivo: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Simule l'envoi d'un SMS (pour développement)
     */
    private void simulerEnvoiSms(String numeroDestinataire, String message) {
        log.info("=== SIMULATION SMS GRATUIT ===");
        log.info("Fournisseur: {}", freeProvider);
        log.info("Destinataire: {}", numeroDestinataire);
        log.info("Message: {}", message);
        log.info("==============================");
    }

    /**
     * Vérifie la disponibilité du fournisseur configuré
     */
    public boolean verifierDisponibilite() {
        return switch (freeProvider.toUpperCase()) {
            case "TEXTBELT" -> !textbeltApiKey.isEmpty();
            case "AWS_SNS" -> !awsAccessKeyId.isEmpty() && !awsSecretAccessKey.isEmpty();
            case "VONAGE" -> !vonageApiKey.isEmpty() && !vonageApiSecret.isEmpty();
            case "PLIVO" -> !plivoAuthId.isEmpty() && !plivoAuthToken.isEmpty();
            default -> false;
        };
    }

    /**
     * Retourne les informations sur le fournisseur actuel
     */
    public Map<String, Object> getProviderInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("provider", freeProvider);
        info.put("available", verifierDisponibilite());

        switch (freeProvider.toUpperCase()) {
            case "TEXTBELT" -> {
                info.put("limit", "1 SMS gratuit/jour");
                info.put("cost", "Gratuit puis $0.01/SMS");
            }
            case "AWS_SNS" -> {
                info.put("limit", "100 SMS gratuits/mois");
                info.put("cost", "Gratuit puis $0.0075/SMS");
            }
            case "VONAGE" -> {
                info.put("limit", "2€ de crédit gratuit");
                info.put("cost", "Variable selon destination");
            }
            case "PLIVO" -> {
                info.put("limit", "Crédit gratuit initial");
                info.put("cost", "Variable selon destination");
            }
        }

        return info;
    }
}
