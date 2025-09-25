package com.groupe2_ionic.eduka.security.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration des propriétés JWT
 * Centralise la configuration des tokens JWT avec des valeurs par défaut sécurisées
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Clé secrète pour signer les tokens JWT
     * DOIT être définie comme variable d'environnement JWT_SECRET
     */
    private String secret = "default-secret-key-change-in-production-minimum-256-bits-required";

    /**
     * Configuration du token d'accès
     */
    private AccessToken accessToken = new AccessToken();

    /**
     * Configuration du token de rafraîchissement
     */
    private RefreshToken refreshToken = new RefreshToken();

    @Data
    public static class AccessToken {
        /**
         * Durée de validité du token d'accès en millisecondes
         * Par défaut: 15 minutes (900000 ms)
         */
        private long expiration = 900000L; // 15 minutes
    }

    @Data
    public static class RefreshToken {
        /**
         * Durée de validité du token de rafraîchissement en millisecondes
         * Par défaut: 7 jours (604800000 ms)
         */
        private long expiration = 604800000L; // 7 jours
    }
}
