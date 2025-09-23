package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.Paiement;
import com.groupe2_ionic.eduka.models.enums.StatutPaiement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaiementReposiroty extends JpaRepository<Paiement, Integer> {

    /**
     * Récupère la liste des paiements effectués par un parrain donné,
     * triés du plus récent au plus ancien.
     */
    List<Paiement> findByParrainIdOrderByDatePaiementDesc(int parrainId);

    /**
     * Récupère les paiements associés à un parrainage spécifique,
     * triés par date de paiement décroissante.
     */
    List<Paiement> findByParrainageIdOrderByDatePaiementDesc(int parrainageId);

    /**
     * Récupère la liste des paiements selon un statut donné
     * (ex : EN_ATTENTE, REUSSI, ECHEC...).
     */
    List<Paiement> findByStatut(StatutPaiement statut);

    /**
     * Calcule la somme totale des montants payés pour un parrainage,
     * uniquement pour les paiements avec statut "REUSSI".
     */
    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.parrainage.id = :parrainageId AND p.statut = 'REUSSI'")
    BigDecimal sumMontantByParrainageIdAndStatutReussi(@Param("parrainageId") int parrainageId);

    /**
     * Récupère tous les paiements effectués dans une plage de dates donnée.
     */
    @Query("SELECT p FROM Paiement p WHERE p.datePaiement BETWEEN :dateDebut AND :dateFin")
    List<Paiement> findByDatePaiementBetween(@Param("dateDebut") LocalDate dateDebut, @Param("dateFin") LocalDate dateFin);

    /**
     * Recherche un paiement en fonction de son identifiant de transaction.
     * Utile pour vérifier les paiements venant de passerelles externes (API Mobile Money, PayPal...).
     */
    Optional<Paiement> findByTransactionId(String transactionId);

    /**
     * Récupère les paiements par statut, triés par date décroissante.
     */
    List<Paiement> findByStatutOrderByDatePaiementDesc(StatutPaiement statut);

    /**
     * Calcule la somme totale des paiements par statut donné.
     */
    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.statut = :statut")
    BigDecimal sumMontantByStatut(@Param("statut") StatutPaiement statut);

    /**
     * Compte le nombre de paiements réalisés entre deux dates.
     */
    long countByDatePaiementBetween(LocalDate dateDebut, LocalDate dateFin);

    /**
     * Calcule la somme des paiements effectués dans une plage de dates,
     * pour un statut spécifique (par ex : REUSSI uniquement).
     */
    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.datePaiement BETWEEN :dateDebut AND :dateFin AND p.statut = :statut")
    BigDecimal sumMontantByDatePaiementBetweenAndStatut(@Param("dateDebut") LocalDate dateDebut,
                                                        @Param("dateFin") LocalDate dateFin,
                                                        @Param("statut") StatutPaiement statut);

    /**
     * Récupère les paiements d’un parrain donné avec pagination,
     * triés du plus récent au plus ancien.
     */
    Page<Paiement> findByParrainIdOrderByDatePaiementDesc(Integer parrainId, Pageable pageable);

    /**
     * Récupère les paiements d’une organisation donnée avec pagination,
     * triés par date de paiement décroissante.
     */
    Page<Paiement> findByOrganisationIdOrderByDatePaiementDesc(Integer organisationId, Pageable pageable);

    /**
     * Récupère simplement la liste de tous les paiements effectués
     * par un parrain (sans tri particulier).
     */
    List<Paiement> findByParrainId(Integer parrainId);
}
