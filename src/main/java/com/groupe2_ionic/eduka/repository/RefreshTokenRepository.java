package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité RefreshToken
 * Gère la persistance des tokens de rafraîchissement
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    /**
     * Trouve un token de rafraîchissement par sa valeur
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Trouve tous les tokens actifs d'un utilisateur
     */
    List<RefreshToken> findByUserIdAndRevokedFalse(Integer userId);

    /**
     * Révoque tous les tokens d'un utilisateur
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.userId = :userId")
    void revokeAllTokensByUserId(@Param("userId") Integer userId);

    /**
     * Supprime les tokens expirés
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Vérifie si un token existe et n'est pas révoqué
     */
    boolean existsByTokenAndRevokedFalse(String token);
}
