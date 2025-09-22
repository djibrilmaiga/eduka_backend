package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.Tuteur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TuteurRepository extends JpaRepository<Tuteur, Integer> {
    Optional<Tuteur> findByTelephone(String telephone);

    Optional<Tuteur> findByNomAndPrenomAndTelephone(String nom, String prenom, String telephone);
}
