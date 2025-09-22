package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.Paiement;
import com.groupe2_ionic.eduka.models.enums.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PaiementReposiroty extends JpaRepository<Paiement, Integer> {
    List<Paiement> findByParrainIdOrderByDatePaiementDesc(int parrainId);

    List<Paiement> findByParrainageIdOrderByDatePaiementDesc(int parrainageId);

    List<Paiement> findByStatut(StatutPaiement statut);

    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.parrainage.id = :parrainageId AND p.statut = 'REUSSI'")
    BigDecimal sumMontantByParrainageIdAndStatutReussi(@Param("parrainageId") int parrainageId);

    @Query("SELECT p FROM Paiement p WHERE p.datePaiement BETWEEN :dateDebut AND :dateFin")
    List<Paiement> findByDatePaiementBetween(@Param("dateDebut") LocalDate dateDebut, @Param("dateFin") LocalDate dateFin);
}
