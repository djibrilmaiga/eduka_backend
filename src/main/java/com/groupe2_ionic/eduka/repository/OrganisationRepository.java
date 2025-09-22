package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganisationRepository extends JpaRepository<Organisation, Integer> {
    List<Organisation> findByNomContainingIgnoreCase(String nom);
    List<Organisation> findByValidateurId(int adminId);
    Optional <Organisation> findByEmail(String email);
    Optional <Organisation> findByTelephone(String telephone);
}
