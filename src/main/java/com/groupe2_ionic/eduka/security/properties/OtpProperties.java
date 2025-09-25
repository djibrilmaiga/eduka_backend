package com.groupe2_ionic.eduka.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration des propriétés OTP (One-Time Password)
 * Gère les paramètres de génération et validation des codes OTP
 */
@Data
@Component
@ConfigurationProperties(prefix = "otp")
public class OtpProperties {

    /**
     * Durée de validité de l'OTP en minutes
     * Par défaut: 5 minutes
     */
    private int expirationMinutes = 5;

    /**
     * Nombre maximum de tentatives de validation OTP
     * Par défaut: 3 tentatives
     */
    private int maxAttempts = 3;

    /**
     * Longueur du code OTP généré
     * Par défaut: 6 chiffres
     */
    private int codeLength = 6;
}
