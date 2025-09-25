package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
/**
 * Repository pour l'entité OtpCode
 * Gère la persistance des codes OTP
 */
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
    /**
     * Trouve le dernier code OTP valide pour un téléphone
     */
    @Query("SELECT o FROM OtpCode o WHERE o.telephone = :telephone AND o.used = false AND o.expiryDate > :now ORDER BY o.createdAt DESC")
    Optional<OtpCode> findLatestValidOtpByTelephone(@Param("telephone") String telephone, @Param("now") LocalDateTime now);

    /**
     * Trouve un code OTP spécifique pour un téléphone
     */
    Optional<OtpCode> findByTelephoneAndCodeAndUsedFalse(String telephone, String code);

    /**
     * Marque tous les codes OTP d'un téléphone comme utilisés
     */
    @Modifying
    @Query("UPDATE OtpCode o SET o.used = true WHERE o.telephone = :telephone")
    void markAllAsUsedByTelephone(@Param("telephone") String telephone);

    /**
     * Supprime les codes OTP expirés
     */
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiryDate < :now")
    void deleteExpiredOtps(@Param("now") LocalDateTime now);

    /**
     * Compte les tentatives récentes pour un téléphone (dernière heure)
     */
    @Query("SELECT COUNT(o) FROM OtpCode o WHERE o.telephone = :telephone AND o.createdAt > :since")
    long countRecentAttemptsByTelephone(@Param("telephone") String telephone, @Param("since") LocalDateTime since);
}
