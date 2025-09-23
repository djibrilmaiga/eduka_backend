package com.groupe2_ionic.eduka.controllers;

import com.groupe2_ionic.eduka.dto.ValidationOrganisationDto;
import com.groupe2_ionic.eduka.dto.ValidationResponseDto;
import com.groupe2_ionic.eduka.dto.AdminDashboardDto;
import com.groupe2_ionic.eduka.dto.RapportGlobalDto;
import com.groupe2_ionic.eduka.dto.PaiementResponseDto;
import com.groupe2_ionic.eduka.dto.TransfertFondResponseDto;
import com.groupe2_ionic.eduka.services.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admins")
@RequiredArgsConstructor
@Tag(name = "Administration", description = "APIs pour la gestion administrative")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/{adminId}/organisations/{organisationId}/valider")
    @Operation(summary = "Valider ou rejeter l'inscription d'une organisation",
            description = "Permet à un administrateur de valider ou rejeter l'inscription d'une organisation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation effectuée avec succès"),
            @ApiResponse(responseCode = "404", description = "Organisation ou administrateur non trouvé"),
            @ApiResponse(responseCode = "400", description = "Organisation déjà traitée ou données invalides")
    })
    public ResponseEntity<ValidationResponseDto> validerOrganisation(
            @Parameter(description = "ID de l'administrateur") @PathVariable Integer adminId,
            @Parameter(description = "ID de l'organisation à valider") @PathVariable Integer organisationId,
            @Valid @RequestBody ValidationOrganisationDto validationDto) {

        try {
            ValidationResponseDto response = adminService.validerOrganisation(organisationId, adminId, validationDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/organisations/en-attente")
    @Operation(summary = "Récupérer les organisations en attente de validation",
            description = "Retourne la liste paginée des organisations en attente de validation")
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    public ResponseEntity<Page<ValidationResponseDto>> getOrganisationsEnAttente(Pageable pageable) {
        Page<ValidationResponseDto> organisations = adminService.getOrganisationsEnAttente(pageable);
        return ResponseEntity.ok(organisations);
    }

    @GetMapping("/{adminId}/historique-validations")
    @Operation(summary = "Récupérer l'historique des validations d'un admin",
            description = "Retourne l'historique des validations effectuées par un administrateur")
    @ApiResponse(responseCode = "200", description = "Historique récupéré avec succès")
    public ResponseEntity<List<ValidationResponseDto>> getHistoriqueValidations(
            @Parameter(description = "ID de l'administrateur") @PathVariable Integer adminId) {

        List<ValidationResponseDto> historique = adminService.getHistoriqueValidations(adminId);
        return ResponseEntity.ok(historique);
    }

    @GetMapping("/statistiques-validation")
    @Operation(summary = "Récupérer les statistiques de validation",
            description = "Retourne les statistiques globales des validations d'organisations")
    @ApiResponse(responseCode = "200", description = "Statistiques récupérées avec succès")
    public ResponseEntity<AdminService.ValidationStatsDto> getStatistiquesValidation() {
        AdminService.ValidationStatsDto stats = adminService.getStatistiquesValidation();
        return ResponseEntity.ok(stats);
    }

   /* @GetMapping("/dashboard")
    @Operation(summary = "Récupérer le tableau de bord administrateur",
            description = "Retourne les statistiques globales pour le tableau de bord admin")
    @ApiResponse(responseCode = "200", description = "Tableau de bord récupéré avec succès")
    public ResponseEntity<AdminDashboardDto> getDashboard() {
        AdminDashboardDto dashboard = adminService.getDashboard();
        return ResponseEntity.ok(dashboard);
    }*/

    @GetMapping("/rapport-global")
    @Operation(summary = "Générer un rapport global",
            description = "Génère un rapport global pour une période donnée")
    @ApiResponse(responseCode = "200", description = "Rapport généré avec succès")
    public ResponseEntity<RapportGlobalDto> genererRapportGlobal(
            @Parameter(description = "Date de début")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @Parameter(description = "Date de fin")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {

        RapportGlobalDto rapport = adminService.genererRapportGlobal(dateDebut, dateFin);
        return ResponseEntity.ok(rapport);
    }

   /* @GetMapping("/superviser-paiements")
    @Operation(summary = "Superviser les paiements",
            description = "Retourne les paiements suspects ou en échec nécessitant une supervision")
    @ApiResponse(responseCode = "200", description = "Paiements récupérés avec succès")
    public ResponseEntity<List<PaiementResponseDto>> superviserPaiements() {
        List<PaiementResponseDto> paiements = adminService.superviserPaiements();
        return ResponseEntity.ok(paiements);
    }*/

   /* @GetMapping("/superviser-transferts")
    @Operation(summary = "Superviser les transferts",
            description = "Retourne les transferts en attente nécessitant une supervision")
    @ApiResponse(responseCode = "200", description = "Transferts récupérés avec succès")
    public ResponseEntity<List<TransfertFondResponseDto>> superviserTransferts() {
        List<TransfertFondResponseDto> transferts = adminService.superviserTransferts();
        return ResponseEntity.ok(transferts);
    }*/

    @GetMapping("/export-comptable")
    @Operation(summary = "Exporter les données comptables",
            description = "Exporte les données comptables au format CSV ou PDF")
    @ApiResponse(responseCode = "200", description = "Export généré avec succès")
    public ResponseEntity<byte[]> exporterDonneesComptables(
            @Parameter(description = "Date de début")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @Parameter(description = "Date de fin")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @Parameter(description = "Format d'export (CSV ou PDF)")
            @RequestParam(defaultValue = "CSV") String format) {

        byte[] export = adminService.exporterDonneesComptables(dateDebut, dateFin, format);

        String contentType = format.equalsIgnoreCase("PDF") ? "application/pdf" : "text/csv";
        String filename = "export_comptable_" + dateDebut + "_" + dateFin + "." + format.toLowerCase();

        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .header("Content-Disposition", "attachment; filename=" + filename)
                .body(export);
    }
}
