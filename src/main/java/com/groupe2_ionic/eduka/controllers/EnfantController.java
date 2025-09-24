package com.groupe2_ionic.eduka.controllers;

import com.groupe2_ionic.eduka.dto.EnfantDto;
import com.groupe2_ionic.eduka.dto.EnfantResponseDto;
import com.groupe2_ionic.eduka.services.EnfantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enfants")
@RequiredArgsConstructor
public class EnfantController {

    private final EnfantService enfantService;

    @PostMapping
    public ResponseEntity<EnfantResponseDto> creerEnfant(@Valid @RequestBody EnfantDto enfantDto) {
        try {
            EnfantResponseDto enfantCree = enfantService.creerEnfant(enfantDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(enfantCree);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{enfantId}")
    public ResponseEntity<EnfantResponseDto> modifierEnfant(
            @PathVariable int enfantId,
            @Valid @RequestBody EnfantDto enfantDto) {
        try {
            EnfantResponseDto enfantModifie = enfantService.modifierEnfant(enfantId, enfantDto);
            return ResponseEntity.ok(enfantModifie);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{enfantId}")
    public ResponseEntity<Void> supprimerEnfant(
            @PathVariable int enfantId,
            @RequestParam int organisationId) {
        try {
            enfantService.supprimerEnfant(enfantId, organisationId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{enfantId}")
    public ResponseEntity<EnfantResponseDto> getEnfantById(@PathVariable int enfantId) {
        try {
            EnfantResponseDto enfant = enfantService.getEnfantById(enfantId);
            return ResponseEntity.ok(enfant);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/disponibles")
    public ResponseEntity<Page<EnfantResponseDto>> getEnfantsDisponibles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String organisationNom,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String niveauScolaire) {

        Page<EnfantResponseDto> enfants = enfantService.getEnfantsDisponibles(
                page, size, organisationNom, zone, niveauScolaire);
        return ResponseEntity.ok(enfants);
    }

    @GetMapping("/organisation/{organisationId}")
    public ResponseEntity<Page<EnfantResponseDto>> getEnfantsParOrganisation(
            @PathVariable int organisationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<EnfantResponseDto> enfants = enfantService.getEnfantsParOrganisation(organisationId, page, size);
        return ResponseEntity.ok(enfants);
    }

    @GetMapping("/filleuls/{parrainId}")
    public ResponseEntity<List<EnfantResponseDto>> getFilleulsParParrain(@PathVariable int parrainId) {
        List<EnfantResponseDto> filleuls = enfantService.getFilleulsParParrain(parrainId);
        return ResponseEntity.ok(filleuls);
    }

    @GetMapping("/statistiques/{organisationId}")
    public ResponseEntity<EnfantService.EnfantStatsDto> getStatistiquesEnfants(@PathVariable int organisationId) {
        EnfantService.EnfantStatsDto stats = enfantService.getStatistiquesEnfants(organisationId);
        return ResponseEntity.ok(stats);
    }
}
