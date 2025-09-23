package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.Organisation;
import com.groupe2_ionic.eduka.models.enums.StatutValidation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Organisation> findByStatutValidation(StatutValidation statut, Pageable pageable);
    List<Organisation> findByValidateurIdOrderByDateValidationDesc(Integer adminId);
    long countByStatutValidation(StatutValidation statut);

    @Query("SELECT COUNT(o) FROM Organisation o WHERE o.actif = true")
    long countOrganisationsValidees();

    @Query("SELECT COUNT(o) FROM Organisation o WHERE o.actif = false")
    long countOrganisationsEnAttente();

    @Query("SELECT o FROM Organisation o WHERE o.statutValidation = :statut ORDER BY o.dateInscription DESC")
    List<Organisation> findByStatutValidationOrderByDateInscription(StatutValidation statut);

    @Query("SELECT COUNT(e) FROM Organisation o JOIN o.enfants e WHERE o.id = :organisationId")
    long countEnfantsByOrganisationId(Integer organisationId);

    @Query("SELECT COUNT(p) FROM Organisation o JOIN o.paiements p WHERE o.id = :organisationId")
    long countPaiementsByOrganisationId(Integer organisationId);
}
