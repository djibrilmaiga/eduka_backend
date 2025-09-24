package com.groupe2_ionic.eduka.controllers;

import com.groupe2_ionic.eduka.dto.RapportDto;
import com.groupe2_ionic.eduka.dto.RapportResponseDto;
import com.groupe2_ionic.eduka.dto.RapportRecentDto;
import com.groupe2_ionic.eduka.services.RapportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rapports")
@RequiredArgsConstructor
public class RapportController {

    private final RapportService rapportService;

    @PostMapping
    public ResponseEntity<RapportResponseDto> creerRapport(@Valid @RequestBody RapportDto rapportDto) {
        try {
            RapportResponseDto rapportCree = rapportService.creerRapport(rapportDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(rapportCree);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{rapportId}")
    public ResponseEntity<RapportResponseDto> modifierRapport(
            @PathVariable int rapportId,
            @Valid @RequestBody RapportDto rapportDto) {
        try {
            RapportResponseDto rapportModifie = rapportService.modifierRapport(rapportId, rapportDto);
            return ResponseEntity.ok(rapportModifie);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{rapportId}")
    public ResponseEntity<Void> supprimerRapport(@PathVariable int rapportId) {
        try {
            rapportService.supprimerRapport(rapportId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{rapportId}")
    public ResponseEntity<RapportResponseDto> getRapportById(@PathVariable int rapportId) {
        try {
            RapportResponseDto rapport = rapportService.getRapportById(rapportId);
            return ResponseEntity.ok(rapport);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/organisation/{organisationId}")
    public ResponseEntity<Page<RapportResponseDto>> getRapportsParOrganisation(
            @PathVariable int organisationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<RapportResponseDto> rapports = rapportService.getRapportsParOrganisation(organisationId, page, size);
        return ResponseEntity.ok(rapports);
    }

    @GetMapping("/enfant/{enfantId}")
    public ResponseEntity<Page<RapportResponseDto>> getRapportsParEnfant(
            @PathVariable int enfantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<RapportResponseDto> rapports = rapportService.getRapportsParEnfant(enfantId, page, size);
        return ResponseEntity.ok(rapports);
    }

    @GetMapping("/type/{typeRapport}")
    public ResponseEntity<Page<RapportResponseDto>> getRapportsParType(
            @PathVariable String typeRapport,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<RapportResponseDto> rapports = rapportService.getRapportsParType(typeRapport, page, size);
        return ResponseEntity.ok(rapports);
    }

    @GetMapping("/recents/{organisationId}")
    public ResponseEntity<List<RapportRecentDto>> getRapportsRecents(
            @PathVariable int organisationId,
            @RequestParam(defaultValue = "5") int limit) {

        List<RapportRecentDto> rapportsRecents = rapportService.getRapportsRecents(organisationId, limit);
        return ResponseEntity.ok(rapportsRecents);
    }
}
