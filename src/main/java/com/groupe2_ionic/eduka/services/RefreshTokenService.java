package com.groupe2_ionic.eduka.services;

import com.groupe2_ionic.eduka.models.RefreshToken;
import com.groupe2_ionic.eduka.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service de gestion des tokens de rafraîchissement
 * Fournit des méthodes pour la gestion du cycle de vie des refresh tokens
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Trouve un refresh token par sa valeur
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Vérifie si un token est valide (existe et n'est pas révoqué/expiré)
     */
    public boolean isTokenValid(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(refreshToken -> !refreshToken.getRevoked() &&
                        refreshToken.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    /**
     * Révoque un token spécifique
     */
    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                    log.info("Token révoqué: {}", token.substring(0, 10) + "...");
                });
    }

    /**
     * Révoque tous les tokens d'un utilisateur
     */
    @Transactional
    public void revokeAllUserTokens(Integer userId) {
        refreshTokenRepository.revokeAllTokensByUserId(userId);
        log.info("Tous les tokens révoqués pour l'utilisateur ID: {}", userId);
    }

    /**
     * Récupère tous les tokens actifs d'un utilisateur
     */
    public List<RefreshToken> findActiveTokensByUserId(Integer userId) {
        return refreshTokenRepository.findByUserIdAndRevokedFalse(userId);
    }

    /**
     * Nettoyage automatique des tokens expirés (exécuté toutes les heures)
     */
    @Scheduled(fixedRate = 3600000) // 1 heure
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Démarrage du nettoyage automatique des tokens expirés");

        try {
            refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            log.info("Nettoyage des tokens expirés terminé");
        } catch (Exception e) {
            log.error("Erreur lors du nettoyage des tokens expirés: {}", e.getMessage());
        }
    }

    /**
     * Statistiques des tokens
     */
    public TokenStats getTokenStats() {
        long totalTokens = refreshTokenRepository.count();
        long activeTokens = refreshTokenRepository.findAll().stream()
                .filter(token -> !token.getRevoked() &&
                        token.getExpiryDate().isAfter(LocalDateTime.now()))
                .count();

        return new TokenStats(totalTokens, activeTokens, totalTokens - activeTokens);
    }

    /**
     * Classe pour les statistiques des tokens
     */
    public record TokenStats(long total, long active, long expired) {}
}
