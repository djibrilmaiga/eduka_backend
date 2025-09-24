package com.groupe2_ionic.eduka.controllers;

import com.groupe2_ionic.eduka.dto.DepenseDto;
import com.groupe2_ionic.eduka.dto.DepenseResponseDto;
import com.groupe2_ionic.eduka.dto.DepenseDetailDto;
import com.groupe2_ionic.eduka.dto.DepenseStatistiqueDto;
import com.groupe2_ionic.eduka.services.DepenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/depenses")
@RequiredArgsConstructor
public class DepenseController {

    private final DepenseService depenseService;

    @PostMapping
    public ResponseEntity<DepenseResponseDto> creerDepense(@Valid @RequestBody DepenseDto depenseDto) {
        try {
            DepenseResponseDto depenseCree = depenseService.creerDepense(depenseDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(depenseCree);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{depenseId}")
    public ResponseEntity<DepenseResponseDto> modifierDepense(
            @PathVariable int depenseId,
            @Valid @RequestBody DepenseDto depenseDto) {
        try {
            DepenseResponseDto depenseModifiee = depenseService.modifierDepense(depenseId, depenseDto);
            return ResponseEntity.ok(depenseModifiee);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{depenseId}")
    public ResponseEntity<Void> supprimerDepense(@PathVariable int depenseId) {
        try {
            depenseService.supprimerDepense(depenseId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{depenseId}")
    public ResponseEntity<DepenseResponseDto> getDepenseById(@PathVariable int depenseId) {
        try {
            DepenseResponseDto depense = depenseService.getDepenseById(depenseId);
            return ResponseEntity.ok(depense);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/organisation/{organisationId}")
    public ResponseEntity<Page<DepenseResponseDto>> getDepensesParOrganisation(
            @PathVariable int organisationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<DepenseResponseDto> depenses = depenseService.getDepensesParOrganisation(organisationId, page, size);
        return ResponseEntity.ok(depenses);
    }

    @GetMapping("/enfant/{enfantId}")
    public ResponseEntity<Page<DepenseResponseDto>> getDepensesParEnfant(
            @PathVariable int enfantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<DepenseResponseDto> depenses = depenseService.getDepensesParEnfant(enfantId, page, size);
        return ResponseEntity.ok(depenses);
    }

    @GetMapping("/statistiques/{organisationId}")
    public ResponseEntity<List<DepenseStatistiqueDto>> getStatistiquesDepenses(@PathVariable int organisationId) {
        List<DepenseStatistiqueDto> statistiques = depenseService.getStatistiquesDepenses(organisationId);
        return ResponseEntity.ok(statistiques);
    }
}
