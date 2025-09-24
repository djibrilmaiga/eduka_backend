package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.Rapport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RapportRepository extends JpaRepository<Rapport, Integer> {

    List<Rapport> findByOrganisationIdOrderByDateDesc(int organisationId);
    Page<Rapport> findByOrganisationId(int organisationId, Pageable pageable);
    List<Rapport> findByEnfantId(int enfantId);
    List<Rapport> findByTypeRapport(String typeRapport);
    List<Rapport> findByPeriode(String periode);

    List<Rapport> findByDateBetween(LocalDate dateDebut, LocalDate dateFin);
    List<Rapport> findByOrganisationIdAndDateBetween(int organisationId, LocalDate dateDebut, LocalDate dateFin);

    Page<Rapport> findByEnfantId(int enfantId, Pageable pageable);
    Page<Rapport> findByEnfantIdInOrderByDateDesc(List<Integer> enfantIds, Pageable pageable);
    Page<Rapport> findByTypeRapport(String typeRapport, Pageable pageable);
    long countByEnfantIdIn(List<Integer> enfantIds);

    Long countByOrganisationId(int organisationId);
    Long countByEnfantId(int enfantId);

    Optional<Rapport> findFirstByEnfantIdOrderByDateDesc(int id);
}
