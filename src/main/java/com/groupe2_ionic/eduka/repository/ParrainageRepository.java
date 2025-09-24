package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.Parrainage;
import com.groupe2_ionic.eduka.models.enums.StatutParrainage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParrainageRepository extends JpaRepository<Parrainage, Integer> {

    List<Parrainage> findByParrainIdOrderByDateDebutDesc(int parrainId);

    List<Parrainage> findByEnfantIdOrderByDateDebutDesc(int enfantId);

    List<Parrainage> findByStatut(StatutParrainage statut);

    @Query("SELECT p FROM Parrainage p WHERE p.parrain.id = :parrainId AND p.statut = :statut")
    List<Parrainage> findByParrainIdAndStatut(@Param("parrainId") int parrainId, @Param("statut") StatutParrainage statut);

    @Query("SELECT COUNT(p) FROM Parrainage p WHERE p.parrain.id = :parrainId AND p.statut = 'ACTIF'")
    long countParrainagesActifsByParrainId(@Param("parrainId") int parrainId);

    @Query("SELECT COUNT(p) FROM Parrainage p WHERE p.enfant.id = :enfantId AND p.statut = 'ACTIF'")
    long countParrainagesActifsByEnfantId(@Param("enfantId") int enfantId);

    @Query("SELECT p FROM Parrainage p WHERE p.enfant.id = :enfantId AND p.statut = :statut")
    List<Parrainage> findByEnfantIdAndStatut(@Param("enfantId") int enfantId, @Param("statut") StatutParrainage statut);

    @Query("SELECT p FROM Parrainage p WHERE p.parrain.id = :parrainId AND p.enfant.id = :enfantId AND p.statut = :statut")
    List<Parrainage> findByParrainIdAndEnfantIdAndStatut(@Param("parrainId") int parrainId, @Param("enfantId") int enfantId, @Param("statut") StatutParrainage statut);

    @Query("SELECT COUNT(p) FROM Parrainage p WHERE p.parrain.id = :parrainId")
    long countByParrainId(@Param("parrainId") int parrainId);

    @Query("SELECT COUNT(p) FROM Parrainage p WHERE p.parrain.id = :parrainId AND p.statut = :statut")
    long countByParrainIdAndStatut(@Param("parrainId") int parrainId, @Param("statut") StatutParrainage statut);
}
