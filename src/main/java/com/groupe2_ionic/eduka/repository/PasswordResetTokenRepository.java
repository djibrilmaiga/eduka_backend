package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository pour l'entité PasswordResetToken
 * Gère la persistance des tokens de réinitialisation de mot de passe
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long>{
    /**
     * Trouve un token de réinitialisation par sa valeur
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Trouve un token valide (non utilisé et non expiré) par sa valeur
     */
    @Query("SELECT p FROM PasswordResetToken p WHERE p.token = :token AND p.used = false AND p.expiryDate > :now")
    Optional<PasswordResetToken> findValidTokenByValue(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * Marque tous les tokens d'un utilisateur comme utilisés
     */
    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.used = true WHERE p.userId = :userId")
    void markAllAsUsedByUserId(@Param("userId") Integer userId);

    /**
     * Supprime les tokens expirés
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Compte les tentatives récentes pour un email (dernière heure)
     */
    @Query("SELECT COUNT(p) FROM PasswordResetToken p WHERE p.email = :email AND p.createdAt > :since")
    long countRecentAttemptsByEmail(@Param("email") String email, @Param("since") LocalDateTime since);

    /**
     * Vérifie si un token existe et est valide
     */
    boolean existsByTokenAndUsedFalseAndExpiryDateAfter(String token, LocalDateTime now);
}
