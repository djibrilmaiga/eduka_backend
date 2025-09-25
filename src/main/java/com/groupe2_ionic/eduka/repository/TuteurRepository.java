package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.Tuteur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository pour l'entité Tuteur
 * Fournit les méthodes d'accès aux données pour les tuteurs
 */
public interface TuteurRepository extends JpaRepository<Tuteur, Integer> {
    /**
     * Trouve un tuteur par son numéro de téléphone
     */
    Optional<Tuteur> findByTelephone(String telephone);

    /**
     * Vérifie si un tuteur existe avec ce numéro de téléphone
     */
    boolean existsByTelephone(String telephone);
}
