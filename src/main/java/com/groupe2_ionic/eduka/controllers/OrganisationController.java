package com.groupe2_ionic.eduka.controllers;

import com.groupe2_ionic.eduka.dto.*;
import com.groupe2_ionic.eduka.services.OrganisationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organisations")
@RequiredArgsConstructor
@Tag(name = "Organisations", description = "APIs pour la gestion des organisations et leurs workflows terrain")
public class OrganisationController {

    private final OrganisationService organisationService;

    @Operation(
            summary = "Inscription d'une nouvelle organisation",
            description = "Permet à une organisation de créer un compte sur la plateforme. " +
                    "Le compte sera en attente de validation par un administrateur."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Organisation inscrite avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Email ou téléphone déjà utilisé")
    })
    @PostMapping("/inscription")
    public ResponseEntity<OrganisationResponseDto> inscrireOrganisation(
            @Valid @RequestBody OrganisationDto organisationDto) {
        try {
            OrganisationResponseDto response = organisationService.inscrireOrganisation(organisationDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @Operation(
            summary = "Obtenir le tableau de bord de l'organisation",
            description = "Récupère les statistiques et métriques clés de l'organisation " +
                    "(nombre d'enfants, parrainages, rapports, dépenses)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tableau de bord récupéré avec succès"),
            @ApiResponse(responseCode = "404", description = "Organisation non trouvée")
    })
    @GetMapping("/{organisationId}/dashboard")
    public ResponseEntity<OrganisationDashboardDto> obtenirTableauDeBord(
            @Parameter(description = "ID de l'organisation") @PathVariable int organisationId) {
        try {
            OrganisationDashboardDto dashboard = organisationService.obtenirTableauDeBord(organisationId);
            return ResponseEntity.ok(dashboard);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Enregistrer un nouvel enfant",
            description = "Permet à une organisation validée d'enregistrer un enfant " +
                    "après avoir obtenu le consentement du parent/tuteur"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Enfant enregistré avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Organisation non validée"),
            @ApiResponse(responseCode = "404", description = "Organisation non trouvée")
    })
    @PostMapping("/{organisationId}/enfants")
    public ResponseEntity<EnfantResponseDto> enregistrerEnfant(
            @Parameter(description = "ID de l'organisation") @PathVariable int organisationId,
            @Valid @RequestBody EnfantDto enfantDto) {
        try {
            EnfantResponseDto response = organisationService.enregistrerEnfant(organisationId, enfantDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("non validée")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Obtenir la liste des enfants de l'organisation",
            description = "Récupère la liste paginée de tous les enfants enregistrés par l'organisation"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des enfants récupérée avec succès")
    })
    @GetMapping("/{organisationId}/enfants")
    public ResponseEntity<Page<EnfantResponseDto>> obtenirEnfantsOrganisation(
            @Parameter(description = "ID de l'organisation") @PathVariable int organisationId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<EnfantResponseDto> enfants = organisationService.obtenirEnfantsOrganisation(organisationId, pageable);
        return ResponseEntity.ok(enfants);
    }

    @Operation(
            summary = "Créer un besoin pour un enfant",
            description = "Permet à une organisation de définir un nouveau besoin " +
                    "(frais scolaires, fournitures, soins médicaux) pour un enfant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Besoin créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Enfant n'appartient pas à cette organisation"),
            @ApiResponse(responseCode = "404", description = "Organisation ou enfant non trouvé")
    })
    @PostMapping("/{organisationId}/besoins")
    public ResponseEntity<BesoinResponseDto> creerBesoin(
            @Parameter(description = "ID de l'organisation") @PathVariable int organisationId,
            @Valid @RequestBody BesoinDto besoinDto) {
        try {
            BesoinResponseDto response = organisationService.creerBesoin(organisationId, besoinDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("n'appartient pas")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Enregistrer une dépense",
            description = "Permet à une organisation d'enregistrer une dépense effectuée " +
                    "pour un enfant avec justificatif (photo du reçu)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Dépense enregistrée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Enfant n'appartient pas à cette organisation"),
            @ApiResponse(responseCode = "404", description = "Organisation ou enfant non trouvé")
    })
    @PostMapping("/{organisationId}/depenses")
    public ResponseEntity<DepenseResponseDto> enregistrerDepense(
            @Parameter(description = "ID de l'organisation") @PathVariable int organisationId,
            @Valid @RequestBody DepenseDto depenseDto) {
        try {
            DepenseResponseDto response = organisationService.enregistrerDepense(organisationId, depenseDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("n'appartient pas")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Créer un rapport pour un enfant",
            description = "Permet à une organisation de créer un rapport pédagogique " +
                    "ou financier pour un enfant (mensuel, trimestriel)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rapport créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Enfant n'appartient pas à cette organisation"),
            @ApiResponse(responseCode = "404", description = "Organisation ou enfant non trouvé")
    })
    @PostMapping("/{organisationId}/rapports")
    public ResponseEntity<RapportResponseDto> creerRapport(
            @Parameter(description = "ID de l'organisation") @PathVariable int organisationId,
            @Valid @RequestBody RapportDto rapportDto) {
        try {
            RapportResponseDto response = organisationService.creerRapport(organisationId, rapportDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("n'appartient pas")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Obtenir les rapports de l'organisation",
            description = "Récupère la liste de tous les rapports créés par l'organisation " +
                    "triés par date de création décroissante"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des rapports récupérée avec succès")
    })
    @GetMapping("/{organisationId}/rapports")
    public ResponseEntity<List<RapportResponseDto>> obtenirRapportsOrganisation(
            @Parameter(description = "ID de l'organisation") @PathVariable int organisationId) {
        List<RapportResponseDto> rapports = organisationService.obtenirRapportsOrganisation(organisationId);
        return ResponseEntity.ok(rapports);
    }

    @Operation(
            summary = "Créer une demande de transfert résiduel",
            description = "Permet à une organisation de demander le transfert de fonds résiduels " +
                    "d'un enfant (abandon scolaire, besoins satisfaits) vers un autre enfant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Demande de transfert créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Solde insuffisant ou données invalides"),
            @ApiResponse(responseCode = "403", description = "Enfant n'appartient pas à cette organisation"),
            @ApiResponse(responseCode = "404", description = "Organisation ou enfant non trouvé")
    })
    @PostMapping("/{organisationId}/transferts")
    public ResponseEntity<TransfertFondResponseDto> creerDemandeTransfertResiduel(
            @Parameter(description = "ID de l'organisation") @PathVariable int organisationId,
            @Valid @RequestBody TransfertFondDto transfertDto) {
        try {
            TransfertFondResponseDto response = organisationService.creerDemandeTransfertResiduel(organisationId, transfertDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("n'appartient pas")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (e.getMessage().contains("Solde insuffisant")) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.notFound().build();
        }
    }
}
