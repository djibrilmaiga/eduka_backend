package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.Enfant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnfantRepository extends JpaRepository<Enfant, Integer> {
    @Query("SELECT e FROM Enfant e WHERE e.statutParrainage = false")
    Page<Enfant> findEnfantsDisponibles(Pageable pageable);

    @Query("SELECT e FROM Enfant e WHERE e.statutParrainage = false " +
            "AND (:organisationNom IS NULL OR e.organisation.nom LIKE %:organisationNom%) " +
            "AND (:zone IS NULL OR e.organisation.ville LIKE %:zone%) " +
            "AND (:niveauScolaire IS NULL OR e.niveauScolaire LIKE %:niveauScolaire%)")
    Page<Enfant> findEnfantsDisponiblesAvecFiltres(
            @Param("organisationNom") String organisationNom,
            @Param("zone") String zone,
            @Param("niveauScolaire") String niveauScolaire,
            Pageable pageable);

    List<Enfant> findByOrganisationId(int organisationId);

    Page<Enfant> findByOrganisationId(int organisationId, Pageable pageable);

    List<Enfant> findByTuteurId(int tuteurId);

    @Query("SELECT e FROM Enfant e WHERE e.statutParrainage = true")
    List<Enfant> findEnfantsParraines();

    @Query("SELECT COUNT(e) FROM Enfant e WHERE e.statutParrainage = false")
    long countEnfantsDisponibles();

    long countByOrganisationId(int organisationId);
    long countByOrganisationIdAndStatutParrainageTrue(int organisationId);
    long countByOrganisationIdAndStatutParrainageFalse(int organisationId);
}
