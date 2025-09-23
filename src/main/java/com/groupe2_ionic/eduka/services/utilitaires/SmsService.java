package com.groupe2_ionic.eduka.services.utilitaires;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final RestTemplate restTemplate;
    private final SmsProviderService smsProviderService;

    @Value("${sms.provider:ORANGE_SMS}")
    private String smsProvider;

    @Value("${sms.use.free.provider:false}")
    private boolean useFreeProvider;

    @Value("${sms.orange.api.url:https://api.orange.com/smsmessaging/v1/outbound}")
    private String orangeSmsApiUrl;

    @Value("${sms.orange.access.token:}")
    private String orangeAccessToken;

    @Value("${sms.twilio.account.sid:}")
    private String twilioAccountSid;

    @Value("${sms.twilio.auth.token:}")
    private String twilioAuthToken;

    @Value("${sms.twilio.phone.number:}")
    private String twilioPhoneNumber;

    /**
     * Envoie un SMS selon le fournisseur configuré
     */
    public boolean envoyerSms(String numeroDestinataire, String message) {
        try {
            if (useFreeProvider) {
                return smsProviderService.envoyerSmsGratuit(numeroDestinataire, message);
            }

            return switch (smsProvider.toUpperCase()) {
                case "ORANGE_SMS" -> envoyerSmsOrange(numeroDestinataire, message);
                case "TWILIO" -> envoyerSmsTwilio(numeroDestinataire, message);
                default -> {
                    log.warn("Fournisseur SMS non configuré, simulation d'envoi");
                    simulerEnvoiSms(numeroDestinataire, message);
                    yield true;
                }
            };
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi SMS: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Envoie un SMS via l'API Orange SMS Mali
     */
    private boolean envoyerSmsOrange(String numeroDestinataire, String message) {
        try {
            // Validation du numéro malien
            if (!numeroDestinataire.matches("^\\+223[67]\\d{7}$")) {
                throw new RuntimeException("Numéro de téléphone malien invalide: " + numeroDestinataire);
            }

            // Préparation de la requête
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(orangeAccessToken);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("outboundSMSMessageRequest", Map.of(
                    "address", "tel:" + numeroDestinataire,
                    "senderAddress", "tel:+22300000000", // Numéro expéditeur Orange
                    "outboundSMSTextMessage", Map.of("message", message)
            ));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    orangeSmsApiUrl + "/requests", request, String.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                log.info("SMS Orange envoyé avec succès à {}", numeroDestinataire);
                return true;
            } else {
                log.error("Échec envoi SMS Orange: {}", response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("Erreur SMS Orange: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Envoie un SMS via Twilio (pour usage international)
     */
    private boolean envoyerSmsTwilio(String numeroDestinataire, String message) {
        try {
            String twilioUrl = "https://api.twilio.com/2010-04-01/Accounts/" + twilioAccountSid + "/Messages.json";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(twilioAccountSid, twilioAuthToken);

            String requestBody = "From=" + twilioPhoneNumber +
                    "&To=" + numeroDestinataire +
                    "&Body=" + message;

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(twilioUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                log.info("SMS Twilio envoyé avec succès à {}", numeroDestinataire);
                return true;
            } else {
                log.error("Échec envoi SMS Twilio: {}", response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("Erreur SMS Twilio: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Simule l'envoi d'un SMS (pour développement/test)
     */
    private void simulerEnvoiSms(String numeroDestinataire, String message) {
        log.info("=== SIMULATION SMS ===");
        log.info("Destinataire: {}", numeroDestinataire);
        log.info("Message: {}", message);
        log.info("======================");
    }

    /**
     * Envoie un SMS de notification de paiement
     */
    public boolean envoyerNotificationPaiement(String numeroTelephone, String nomParrain, double montant, String statut) {
        String message = String.format(
                "Cher(e) %s, votre paiement de %.0f FCFA a été %s. Merci pour votre soutien à l'éducation des enfants.",
                nomParrain, montant, statut.toLowerCase()
        );

        return envoyerSms(numeroTelephone, message);
    }

    /**
     * Envoie un SMS de rappel de paiement
     */
    public boolean envoyerRappelPaiement(String numeroTelephone, String nomParrain, String nomEnfant, double montant) {
        String message = String.format(
                "Cher(e) %s, rappel: votre contribution mensuelle de %.0f FCFA pour %s est due. Merci de procéder au paiement.",
                nomParrain, montant, nomEnfant
        );

        return envoyerSms(numeroTelephone, message);
    }

    /**
     * Envoie un SMS de validation d'inscription
     */
    public boolean envoyerValidationInscription(String numeroTelephone, String nomOrganisation, boolean valide) {
        String message = valide
                ? String.format("Félicitations %s ! Votre inscription a été validée. Vous pouvez maintenant accéder à toutes les fonctionnalités.", nomOrganisation)
                : String.format("Cher(e) %s, votre inscription nécessite des corrections. Consultez votre email pour plus de détails.", nomOrganisation);

        return envoyerSms(numeroTelephone, message);
    }
}
