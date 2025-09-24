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

    List<Paiement> findByParrainIdOrderByDatePaiementDesc(int parrainId);

    List<Paiement> findByParrainageIdOrderByDatePaiementDesc(int parrainageId);

    List<Paiement> findByStatut(StatutPaiement statut);

    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.parrainage.id = :parrainageId AND p.statut = 'REUSSI'")
    BigDecimal sumMontantByParrainageIdAndStatutReussi(@Param("parrainageId") int parrainageId);

    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.parrainage.id = :parrainageId AND p.statut = 'REUSSI'")
    BigDecimal sumMontantByParrainageId(@Param("parrainageId") int parrainageId);

    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.parrain.id = :parrainId AND p.statut = 'REUSSI'")
    BigDecimal sumMontantByParrainId(@Param("parrainId") int parrainId);

    @Query("SELECT p FROM Paiement p WHERE p.datePaiement BETWEEN :dateDebut AND :dateFin")
    List<Paiement> findByDatePaiementBetween(@Param("dateDebut") LocalDate dateDebut, @Param("dateFin") LocalDate dateFin);

    Optional<Paiement> findByTransactionId(String transactionId);

    List<Paiement> findByStatutOrderByDatePaiementDesc(StatutPaiement statut);

    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.statut = :statut")
    BigDecimal sumMontantByStatut(@Param("statut") StatutPaiement statut);

    long countByDatePaiementBetween(LocalDate dateDebut, LocalDate dateFin);

    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.datePaiement BETWEEN :dateDebut AND :dateFin AND p.statut = :statut")
    BigDecimal sumMontantByDatePaiementBetweenAndStatut(@Param("dateDebut") LocalDate dateDebut,
                                                        @Param("dateFin") LocalDate dateFin,
                                                        @Param("statut") StatutPaiement statut);

    @Query("SELECT COUNT(p) FROM Paiement p WHERE p.parrain.id = :parrainId AND p.statut = :statut")
    long countByParrainIdAndStatut(@Param("parrainId") int parrainId, @Param("statut") StatutPaiement statut);

    List<Paiement> findByParrainIdAndStatut(int parrainId, StatutPaiement statut);

    Page<Paiement> findByParrainIdOrderByDatePaiementDesc(Integer parrainId, Pageable pageable);

    Page<Paiement> findByOrganisationIdOrderByDatePaiementDesc(Integer organisationId, Pageable pageable);

    List<Paiement> findByParrainId(Integer parrainId);
}
