package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrganisationRepository extends JpaRepository<Organisation, Integer> {
    Optional<Organisation> findByEmail(String email);
    Optional<Organisation> findByTelephone(String telephone);

    List<Organisation> findByNomContainingIgnoreCase(String nom);
    List<Organisation> findByValidateurId(int adminId);

    List<Organisation> findByActifFalse(); // Organisations en attente de validation
    List<Organisation> findByActifTrue();  // Organisations validées

    List<Organisation> findByVille(String ville);
    List<Organisation> findByPays(String pays);

    @Query("SELECT COUNT(o) FROM Organisation o WHERE o.actif = true")
    long countOrganisationsValidees();

    @Query("SELECT COUNT(o) FROM Organisation o WHERE o.actif = false")
    long countOrganisationsEnAttente();// Organisations validées
}
