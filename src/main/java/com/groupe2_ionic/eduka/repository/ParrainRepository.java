package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.Parrain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParrainRepository extends JpaRepository<Parrain, Integer> {
    Optional<Parrain> findByEmail(String email);
    Optional<Parrain> findByTelephone(String telephone);

    List<Parrain> findByPays(String pays);

    @Query("SELECT p FROM Parrain p WHERE p.actif = true")
    List<Parrain> findAllActifs();

    @Query("SELECT p FROM Parrain p WHERE p.ville = :ville AND p.actif = true")
    List<Parrain> findByVilleAndActif(@Param("ville") String ville);
}
