package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.Depense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DepenseRepository extends JpaRepository<Depense, Integer> {
    List<Depense> findByOrganisationIdOrderByDateEnregistrementDesc(int organisationId);
    Page<Depense> findByOrganisationId(int organisationId, Pageable pageable);
    List<Depense> findByEnfantId(int enfantId);
    List<Depense> findByTypeDepense(String typeDepense);

    List<Depense> findByDateEnregistrementBetween(LocalDate dateDebut, LocalDate dateFin);
    List<Depense> findByOrganisationIdAndDateEnregistrementBetween(int organisationId, LocalDate dateDebut, LocalDate dateFin);

    Page<Depense> findByEnfantIdInOrderByDateEnregistrementDesc(List<Integer> enfantIds, Pageable pageable);
    long countByEnfantIdIn(List<Integer> enfantIds);
    Page<Depense> findByEnfantId(int enfantId, Pageable pageable);

    @Query("SELECT SUM(d.montant) FROM Depense d WHERE d.organisation.id = :organisationId")
    BigDecimal sumMontantByOrganisationId(@Param("organisationId") int organisationId);

    @Query("SELECT SUM(d.montant) FROM Depense d WHERE d.enfant.id = :enfantId")
    BigDecimal sumMontantByEnfantId(@Param("enfantId") int enfantId);

    Long countByOrganisationId(int organisationId);
    Long countByEnfantId(int enfantId);
}
