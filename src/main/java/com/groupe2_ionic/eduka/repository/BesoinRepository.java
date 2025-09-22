package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.Besoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BesoinRepository extends JpaRepository<Besoin, Integer> {
    List<Besoin> findByEnfantId(int enfantId);
    List<Besoin> findByEnfantOrganisationId(int organisationId);
    List<Besoin> findByType(String type);

    @Query("SELECT SUM(b.montant) FROM Besoin b WHERE b.enfant.id = :enfantId")
    BigDecimal sumMontantByEnfantId(@Param("enfantId") int enfantId);

    @Query("SELECT SUM(b.montant) FROM Besoin b WHERE b.enfant.organisation.id = :organisationId")
    BigDecimal sumMontantByOrganisationId(@Param("organisationId") int organisationId);

    Long countByEnfantOrganisationId(int organisationId);
}
