package com.groupe2_ionic.eduka.controllers;

import com.groupe2_ionic.eduka.dto.*;
import com.groupe2_ionic.eduka.services.ParrainService;
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
@RequestMapping("/api/v1/parrains")
@RequiredArgsConstructor
@Tag(name = "Parrains", description = "APIs pour la gestion des parrains et leurs workflows")
public class ParrainController {

    private final ParrainService parrainService;

    @Operation(
            summary = "Inscription d'un nouveau parrain",
            description = "Permet à un parrain de créer un compte sur la plateforme. " +
                    "L'email et le téléphone doivent être uniques."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Parrain inscrit avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Email ou téléphone déjà utilisé")
    })
    @PostMapping("/inscription")
    public ResponseEntity<ParrainResponseDto> inscrireParrain(
            @Valid @RequestBody ParrainDto parrainDto) {
        try {
            ParrainResponseDto response = parrainService.inscrireParrain(parrainDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @Operation(
            summary = "Obtenir le profil d'un parrain",
            description = "Récupère les informations détaillées du profil d'un parrain"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profil récupéré avec succès"),
            @ApiResponse(responseCode = "404", description = "Parrain non trouvé")
    })
    @GetMapping("/{parrainId}/profil")
    public ResponseEntity<ParrainResponseDto> obtenirProfilParrain(
            @Parameter(description = "ID du parrain") @PathVariable int parrainId) {
        try {
            ParrainResponseDto response = parrainService.obtenirProfilParrain(parrainId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Mettre à jour le profil d'un parrain",
            description = "Permet à un parrain de modifier ses informations personnelles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profil mis à jour avec succès"),
            @ApiResponse(responseCode = "404", description = "Parrain non trouvé"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PutMapping("/{parrainId}/profil")
    public ResponseEntity<ParrainResponseDto> mettreAJourProfil(
            @Parameter(description = "ID du parrain") @PathVariable int parrainId,
            @Valid @RequestBody ParrainDto parrainDto) {
        try {
            ParrainResponseDto response = parrainService.mettreAJourProfil(parrainId, parrainDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Parcourir les enfants disponibles pour parrainage",
            description = "Permet aux parrains de filtrer et parcourir la liste des enfants " +
                    "disponibles pour parrainage par organisation, zone ou niveau scolaire"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des enfants récupérée avec succès")
    })
    @GetMapping("/enfants-disponibles")
    public ResponseEntity<Page<EnfantResponseDto>> parcourirEnfantsDisponibles(
            @Parameter(description = "Nom de l'organisation (optionnel)")
            @RequestParam(required = false) String organisationNom,
            @Parameter(description = "Zone géographique (optionnel)")
            @RequestParam(required = false) String zone,
            @Parameter(description = "Niveau scolaire (optionnel)")
            @RequestParam(required = false) String niveauScolaire,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<EnfantResponseDto> enfants = parrainService.parcourirEnfantsDisponibles(
                organisationNom, zone, niveauScolaire, pageable);
        return ResponseEntity.ok(enfants);
    }

    @Operation(
            summary = "Obtenir le détail d'un enfant",
            description = "Récupère les informations détaillées d'un enfant pour évaluation " +
                    "avant parrainage (profil, besoins, rapports récents, solde)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Détails de l'enfant récupérés"),
            @ApiResponse(responseCode = "404", description = "Enfant non trouvé"),
            @ApiResponse(responseCode = "409", description = "Enfant déjà parrainé")
    })
    @GetMapping("/enfants/{enfantId}")
    public ResponseEntity<EnfantResponseDto> obtenirDetailEnfant(
            @Parameter(description = "ID de l'enfant") @PathVariable int enfantId) {
        try {
            EnfantResponseDto response = parrainService.obtenirDetailEnfant(enfantId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("déjà parrainé")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Créer un parrainage",
            description = "Permet à un parrain de créer un parrainage pour un enfant spécifique. " +
                    "L'enfant ne doit pas être déjà parrainé."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Parrainage créé avec succès"),
            @ApiResponse(responseCode = "404", description = "Parrain ou enfant non trouvé"),
            @ApiResponse(responseCode = "409", description = "Enfant déjà parrainé")
    })
    @PostMapping("/{parrainId}/parrainages")
    public ResponseEntity<ParrainageResponseDto> creerParrainage(
            @Parameter(description = "ID du parrain") @PathVariable int parrainId,
            @Valid @RequestBody ParrainageDto parrainageDto) {
        try {
            ParrainageResponseDto response = parrainService.creerParrainage(parrainId, parrainageDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("déjà parrainé")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Obtenir l'historique des parrainages",
            description = "Récupère la liste de tous les parrainages d'un parrain " +
                    "(actifs, terminés, suspendus) triés par date de début décroissante"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historique récupéré avec succès")
    })
    @GetMapping("/{parrainId}/parrainages")
    public ResponseEntity<List<ParrainageResponseDto>> obtenirHistoriqueParrainages(
            @Parameter(description = "ID du parrain") @PathVariable int parrainId) {
        List<ParrainageResponseDto> parrainages = parrainService.obtenirHistoriqueParrainages(parrainId);
        return ResponseEntity.ok(parrainages);
    }

    @Operation(
            summary = "Obtenir l'historique des paiements",
            description = "Récupère la liste de tous les paiements effectués par un parrain " +
                    "triés par date de paiement décroissante"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historique des paiements récupéré")
    })
    @GetMapping("/{parrainId}/paiements")
    public ResponseEntity<List<PaiementResponseDto>> obtenirHistoriquePaiements(
            @Parameter(description = "ID du parrain") @PathVariable int parrainId) {
        List<PaiementResponseDto> paiements = parrainService.obtenirHistoriquePaiements(parrainId);
        return ResponseEntity.ok(paiements);
    }

    @Operation(
            summary = "Valider une demande de transfert résiduel",
            description = "Permet à un parrain de valider une demande de transfert de fonds " +
                    "résiduels en choisissant un nouvel enfant bénéficiaire"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfert validé avec succès"),
            @ApiResponse(responseCode = "404", description = "Transfert ou enfant non trouvé"),
            @ApiResponse(responseCode = "400", description = "Transfert non éligible pour validation")
    })
    @PostMapping("/{parrainId}/transferts/{transfertId}/valider")
    public ResponseEntity<Void> validerTransfertResiduel(
            @Parameter(description = "ID du parrain") @PathVariable int parrainId,
            @Parameter(description = "ID du transfert") @PathVariable int transfertId,
            @Parameter(description = "ID du nouvel enfant bénéficiaire")
            @RequestParam int nouvelEnfantId) {
        try {
            parrainService.validerTransfertResiduel(parrainId, transfertId, nouvelEnfantId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
