package com.groupe2_ionic.eduka.controllers;

import com.groupe2_ionic.eduka.dto.ParrainageDto;
import com.groupe2_ionic.eduka.dto.ParrainageResponseDto;
import com.groupe2_ionic.eduka.services.ParrainageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parrainages")
@RequiredArgsConstructor
public class ParrainageController {

    private final ParrainageService parrainageService;

    @PostMapping
    public ResponseEntity<ParrainageResponseDto> creerParrainage(@Valid @RequestBody ParrainageDto parrainageDto) {
        try {
            ParrainageResponseDto parrainage = parrainageService.creerParrainage(parrainageDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(parrainage);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{parrainageId}/terminer")
    public ResponseEntity<ParrainageResponseDto> terminerParrainage(
            @PathVariable int parrainageId,
            @RequestParam String motifFin) {
        try {
            ParrainageResponseDto parrainage = parrainageService.terminerParrainage(parrainageId, motifFin);
            return ResponseEntity.ok(parrainage);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{parrainageId}/suspendre")
    public ResponseEntity<ParrainageResponseDto> suspendreParrainage(
            @PathVariable int parrainageId,
            @RequestParam String motifSuspension) {
        try {
            ParrainageResponseDto parrainage = parrainageService.suspendreParrainage(parrainageId, motifSuspension);
            return ResponseEntity.ok(parrainage);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{parrainageId}/reactiver")
    public ResponseEntity<ParrainageResponseDto> reactiverParrainage(@PathVariable int parrainageId) {
        try {
            ParrainageResponseDto parrainage = parrainageService.reactiverParrainage(parrainageId);
            return ResponseEntity.ok(parrainage);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{parrainageId}")
    public ResponseEntity<ParrainageResponseDto> getParrainageById(@PathVariable int parrainageId) {
        try {
            ParrainageResponseDto parrainage = parrainageService.getParrainageById(parrainageId);
            return ResponseEntity.ok(parrainage);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/parrain/{parrainId}")
    public ResponseEntity<List<ParrainageResponseDto>> getParrainagesParParrain(@PathVariable int parrainId) {
        List<ParrainageResponseDto> parrainages = parrainageService.getParrainagesParParrain(parrainId);
        return ResponseEntity.ok(parrainages);
    }

    @GetMapping("/parrain/{parrainId}/actifs")
    public ResponseEntity<List<ParrainageResponseDto>> getParrainagesActifsParParrain(@PathVariable int parrainId) {
        List<ParrainageResponseDto> parrainages = parrainageService.getParrainagesActifsParParrain(parrainId);
        return ResponseEntity.ok(parrainages);
    }

    @GetMapping("/enfant/{enfantId}/historique")
    public ResponseEntity<List<ParrainageResponseDto>> getHistoriqueParrainagesEnfant(@PathVariable int enfantId) {
        List<ParrainageResponseDto> parrainages = parrainageService.getHistoriqueParrainagesEnfant(enfantId);
        return ResponseEntity.ok(parrainages);
    }

    @GetMapping("/enfant/{enfantId}/actifs")
    public ResponseEntity<List<ParrainageResponseDto>> getParrainagesActifsEnfant(@PathVariable int enfantId) {
        List<ParrainageResponseDto> parrainages = parrainageService.getParrainagesActifsEnfant(enfantId);
        return ResponseEntity.ok(parrainages);
    }

    @GetMapping("/verification")
    public ResponseEntity<Boolean> peutParrainerEnfant(
            @RequestParam int parrainId,
            @RequestParam int enfantId) {
        boolean peutParrainer = parrainageService.peutParrainerEnfant(parrainId, enfantId);
        return ResponseEntity.ok(peutParrainer);
    }

    @GetMapping("/statistiques/{parrainId}")
    public ResponseEntity<ParrainageService.ParrainageStatsDto> getStatistiquesParrainage(@PathVariable int parrainId) {
        ParrainageService.ParrainageStatsDto stats = parrainageService.getStatistiquesParrainage(parrainId);
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/{parrainageId}/recalculer-montant")
    public ResponseEntity<Void> recalculerMontantTotal(@PathVariable int parrainageId) {
        try {
            parrainageService.recalculerMontantTotal(parrainageId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
