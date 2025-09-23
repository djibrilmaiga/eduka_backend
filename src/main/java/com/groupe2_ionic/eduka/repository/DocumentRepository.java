package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Integer> {

    /**
     * Récupère les documents d'une organisation, triés par date décroissante.
     */
    List<Document> findByOrganisationIdOrderByDateDesc(int organisationId);

    /**
     * Compte le nombre total de documents d'une organisation.
     */
    long countByOrganisationId(int organisationId);

    /**
     * Récupère les documents liés à un rapport spécifique,
     * triés par date décroissante.
     */
    List<Document> findByRapportIdOrderByDateDesc(int rapportId);

    /**
     * Compte le nombre de documents liés à un rapport spécifique.
     */
    long countByRapportId(int rapportId);

    /**
     * Récupère les documents d'un type donné (ex: "PDF", "IMAGE", "WORD"),
     * triés par date décroissante.
     */
    List<Document> findByTypeOrderByDateDesc(String type);

    /**
     * Récupère les documents dont le type est inclus dans une liste donnée.
     * (Ex: ["PDF", "IMAGE"]).
     */
    List<Document> findByTypeIn(List<String> types);

    /**
     * Compte le nombre de documents appartenant à une organisation
     * et dont le type est inclus dans une liste donnée.
     * Exemple : nombre de documents "PDF" et "WORD" pour une organisation donnée.
     */
    @Query("SELECT COUNT(d) FROM Document d WHERE d.organisation.id = :organisationId AND d.type IN :types")
    long countByOrganisationIdAndTypeIn(@Param("organisationId") int organisationId, @Param("types") List<String> types);

    /**
     * Récupère les documents enregistrés entre deux dates.
     * Exemple : tous les documents ajoutés entre janvier et mars 2025.
     */
    List<Document> findByDateBetween(LocalDate dateDebut, LocalDate dateFin);

    /**
     * Récupère les documents d'une organisation, enregistrés entre deux dates.
     * Exemple : documents ajoutés par une organisation en 2024.
     */
    List<Document> findByOrganisationIdAndDateBetween(int organisationId, LocalDate dateDebut, LocalDate dateFin);

    /**
     * Récupère les documents liés à un rapport et filtrés par type.
     * Exemple : récupérer uniquement les "PDF" liés à un rapport donné.
     */
    List<Document> findByRapportIdAndType(int rapportId, String type);
}
