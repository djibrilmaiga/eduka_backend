package com.groupe2_ionic.eduka.controllers;

import com.groupe2_ionic.eduka.dto.EnfantDto;
import com.groupe2_ionic.eduka.dto.EnfantResponseDto;
import com.groupe2_ionic.eduka.services.EnfantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des enfants dans le système Eduka.
 *
 * Ce contrôleur fournit les endpoints pour :
 * - Créer, modifier et supprimer des enfants
 * - Consulter les informations des enfants
 * - Rechercher les enfants disponibles pour parrainage
 * - Obtenir des statistiques sur les enfants
 *
 * @author Groupe 2 Ionic
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/enfants")
@RequiredArgsConstructor
@Tag(name = "Enfants", description = "API de gestion des enfants")
public class EnfantController {

    private final EnfantService enfantService;

    /**
     * Crée un nouvel enfant dans le système.
     *
     * @param enfantDto Les données de l'enfant à créer
     * @return L'enfant créé avec ses informations complètes
     */
    @PostMapping
    @Operation(
            summary = "Créer un nouvel enfant",
            description = "Enregistre un nouvel enfant dans le système avec toutes ses informations personnelles et scolaires"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Enfant créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou organisation non validée"),
            @ApiResponse(responseCode = "404", description = "Organisation non trouvée"),
            @ApiResponse(responseCode = "409", description = "Un enfant avec ces informations existe déjà")
    })
    public ResponseEntity<EnfantResponseDto> creerEnfant(
            @Valid @RequestBody
            @Parameter(description = "Données de l'enfant à créer", required = true)
            EnfantDto enfantDto) {
        try {
            EnfantResponseDto enfantCree = enfantService.creerEnfant(enfantDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(enfantCree);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Modifie les informations d'un enfant existant.
     *
     * @param enfantId L'identifiant de l'enfant à modifier
     * @param enfantDto Les nouvelles données de l'enfant
     * @return L'enfant modifié avec ses informations mises à jour
     */
    @PutMapping("/{enfantId}")
    @Operation(
            summary = "Modifier un enfant",
            description = "Met à jour les informations personnelles et scolaires d'un enfant existant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enfant modifié avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Enfant non trouvé"),
            @ApiResponse(responseCode = "403", description = "Modification non autorisée")
    })
    public ResponseEntity<EnfantResponseDto> modifierEnfant(
            @PathVariable
            @Parameter(description = "Identifiant unique de l'enfant", required = true)
            int enfantId,
            @Valid @RequestBody
            @Parameter(description = "Nouvelles données de l'enfant", required = true)
            EnfantDto enfantDto) {
        try {
            EnfantResponseDto enfantModifie = enfantService.modifierEnfant(enfantId, enfantDto);
            return ResponseEntity.ok(enfantModifie);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Supprime un enfant du système.
     *
     * @param enfantId L'identifiant de l'enfant à supprimer
     * @param organisationId L'identifiant de l'organisation propriétaire
     * @return Réponse vide en cas de succès
     */
    @DeleteMapping("/{enfantId}")
    @Operation(
            summary = "Supprimer un enfant",
            description = "Supprime définitivement un enfant du système. Cette action est irréversible."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Enfant supprimé avec succès"),
            @ApiResponse(responseCode = "400", description = "Suppression impossible (enfant parrainé)"),
            @ApiResponse(responseCode = "404", description = "Enfant non trouvé"),
            @ApiResponse(responseCode = "403", description = "Suppression non autorisée")
    })
    public ResponseEntity<Void> supprimerEnfant(
            @PathVariable
            @Parameter(description = "Identifiant unique de l'enfant", required = true)
            int enfantId,
            @RequestParam
            @Parameter(description = "Identifiant de l'organisation propriétaire", required = true)
            int organisationId) {
        try {
            enfantService.supprimerEnfant(enfantId, organisationId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupère les informations détaillées d'un enfant.
     *
     * @param enfantId L'identifiant de l'enfant
     * @return Les informations complètes de l'enfant
     */
    @GetMapping("/{enfantId}")
    @Operation(
            summary = "Obtenir un enfant par ID",
            description = "Récupère toutes les informations détaillées d'un enfant spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enfant trouvé avec succès"),
            @ApiResponse(responseCode = "404", description = "Enfant non trouvé")
    })
    public ResponseEntity<EnfantResponseDto> getEnfantById(
            @PathVariable
            @Parameter(description = "Identifiant unique de l'enfant", required = true)
            int enfantId) {
        try {
            EnfantResponseDto enfant = enfantService.getEnfantById(enfantId);
            return ResponseEntity.ok(enfant);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Recherche les enfants disponibles pour parrainage avec filtres.
     *
     * @param page Numéro de la page (commence à 0)
     * @param size Nombre d'éléments par page
     * @param organisationNom Filtre par nom d'organisation (optionnel)
     * @param zone Filtre par zone géographique (optionnel)
     * @param niveauScolaire Filtre par niveau scolaire (optionnel)
     * @return Page d'enfants disponibles correspondant aux critères
     */
    @GetMapping("/disponibles")
    @Operation(
            summary = "Rechercher les enfants disponibles",
            description = "Récupère la liste paginée des enfants disponibles pour parrainage avec possibilité de filtrage"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
            @ApiResponse(responseCode = "400", description = "Paramètres de pagination invalides")
    })
    public ResponseEntity<Page<EnfantResponseDto>> getEnfantsDisponibles(
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Numéro de la page (commence à 0)", example = "0")
            int page,
            @RequestParam(defaultValue = "10")
            @Parameter(description = "Nombre d'éléments par page", example = "10")
            int size,
            @RequestParam(required = false)
            @Parameter(description = "Filtre par nom d'organisation")
            String organisationNom,
            @RequestParam(required = false)
            @Parameter(description = "Filtre par zone géographique")
            String zone,
            @RequestParam(required = false)
            @Parameter(description = "Filtre par niveau scolaire")
            String niveauScolaire) {

        Page<EnfantResponseDto> enfants = enfantService.getEnfantsDisponibles(
                page, size, organisationNom, zone, niveauScolaire);
        return ResponseEntity.ok(enfants);
    }

    /**
     * Récupère tous les enfants d'une organisation spécifique.
     *
     * @param organisationId L'identifiant de l'organisation
     * @param page Numéro de la page
     * @param size Nombre d'éléments par page
     * @return Page d'enfants de l'organisation
     */
    @GetMapping("/organisation/{organisationId}")
    @Operation(
            summary = "Enfants d'une organisation",
            description = "Récupère la liste paginée de tous les enfants appartenant à une organisation spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
            @ApiResponse(responseCode = "404", description = "Organisation non trouvée"),
            @ApiResponse(responseCode = "403", description = "Accès non autorisé à cette organisation")
    })
    public ResponseEntity<Page<EnfantResponseDto>> getEnfantsParOrganisation(
            @PathVariable
            @Parameter(description = "Identifiant unique de l'organisation", required = true)
            int organisationId,
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Numéro de la page", example = "0")
            int page,
            @RequestParam(defaultValue = "10")
            @Parameter(description = "Nombre d'éléments par page", example = "10")
            int size) {

        Page<EnfantResponseDto> enfants = enfantService.getEnfantsParOrganisation(organisationId, page, size);
        return ResponseEntity.ok(enfants);
    }

    /**
     * Récupère la liste des filleuls d'un parrain.
     *
     * @param parrainId L'identifiant du parrain
     * @return Liste des enfants parrainés
     */
    @GetMapping("/filleuls/{parrainId}")
    @Operation(
            summary = "Filleuls d'un parrain",
            description = "Récupère la liste de tous les enfants parrainés par un parrain spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des filleuls récupérée avec succès"),
            @ApiResponse(responseCode = "404", description = "Parrain non trouvé"),
            @ApiResponse(responseCode = "403", description = "Accès non autorisé aux informations de ce parrain")
    })
    public ResponseEntity<List<EnfantResponseDto>> getFilleulsParParrain(
            @PathVariable
            @Parameter(description = "Identifiant unique du parrain", required = true)
            int parrainId) {
        List<EnfantResponseDto> filleuls = enfantService.getFilleulsParParrain(parrainId);
        return ResponseEntity.ok(filleuls);
    }

    /**
     * Génère les statistiques des enfants pour une organisation.
     *
     * @param organisationId L'identifiant de l'organisation
     * @return Statistiques détaillées des enfants
     */
    @GetMapping("/statistiques/{organisationId}")
    @Operation(
            summary = "Statistiques des enfants",
            description = "Génère des statistiques détaillées sur les enfants d'une organisation (total, parrainés, par genre, par niveau scolaire, etc.)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistiques générées avec succès"),
            @ApiResponse(responseCode = "404", description = "Organisation non trouvée"),
            @ApiResponse(responseCode = "403", description = "Accès non autorisé aux statistiques de cette organisation")
    })
    public ResponseEntity<EnfantService.EnfantStatsDto> getStatistiquesEnfants(
            @PathVariable
            @Parameter(description = "Identifiant unique de l'organisation", required = true)
            int organisationId) {
        EnfantService.EnfantStatsDto stats = enfantService.getStatistiquesEnfants(organisationId);
        return ResponseEntity.ok(stats);
    }
}
