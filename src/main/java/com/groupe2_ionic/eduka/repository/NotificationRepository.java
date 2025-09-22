package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.Notification;
import com.groupe2_ionic.eduka.models.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    /**
     * Trouve toutes les notifications d'un destinataire, triées par date décroissante.
     */
    List<Notification> findByDestinataireOrderByDateDesc(Utilisateur destinataire);

    /**
     * Trouve toutes les notifications d'un destinataire avec pagination.
     */
    Page<Notification> findByDestinataireOrderByDateDesc(Utilisateur destinataire, Pageable pageable);

    /**
     * Trouve les notifications non lues d'un destinataire.
     */
    List<Notification> findByDestinataireAndLuFalseOrderByDateDesc(Utilisateur destinataire);

    /**
     * Compte le nombre de notifications non lues d'un destinataire.
     */
    long countByDestinataireAndLuFalse(Utilisateur destinataire);

    /**
     * Supprime toutes les notifications d'un destinataire.
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.destinataire.id = :destinataireId")
    void deleteAllByDestinataireId(@Param("destinataireId") int destinataireId);

    /**
     * Marque toutes les notifications d'un destinataire comme lues.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.lu = true WHERE n.destinataire.id = :destinataireId AND n.lu = false")
    void markAllAsReadByDestinataireId(@Param("destinataireId") int destinataireId);
}
