package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
    /**
     * Trouve un utilisateur par email
     */
    Optional<Utilisateur> findByEmail(String email);

    /**
     * Trouve un utilisateur par téléphone
     */
    Optional<Utilisateur> findByTelephone(String telephone);

    /**
     * Trouve un utilisateur par email ou téléphone
     */
    @Query("SELECT u FROM Utilisateur u WHERE u.email = :identifier OR u.telephone = :identifier")
    Optional<Utilisateur> findByEmailOrTelephone(@Param("identifier") String email, @Param("identifier") String telephone);

    /**
     * Vérifie si un email existe déjà
     */
    boolean existsByEmail(String email);

    /**
     * Vérifie si un téléphone existe déjà
     */
    boolean existsByTelephone(String telephone);

    /**
     * Trouve un utilisateur actif par email
     */
    Optional<Utilisateur> findByEmailAndActifTrue(String email);

    /**
     * Trouve un utilisateur actif par téléphone
     */
    Optional<Utilisateur> findByTelephoneAndActifTrue(String telephone);
}
