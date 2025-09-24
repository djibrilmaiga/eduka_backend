package com.groupe2_ionic.eduka.controllers;

import com.groupe2_ionic.eduka.dto.TransfertFondDto;
import com.groupe2_ionic.eduka.dto.TransfertFondResponseDto;
import com.groupe2_ionic.eduka.models.enums.StatutTransfert;
import com.groupe2_ionic.eduka.services.TransfertFondService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transferts")
@RequiredArgsConstructor
public class TransfertFondController {

    private final TransfertFondService transfertFondService;

    @PostMapping
    public ResponseEntity<TransfertFondResponseDto> creerDemandeTransfert(@Valid @RequestBody TransfertFondDto transfertDto) {
        try {
            TransfertFondResponseDto transfertCree = transfertFondService.creerDemandeTransfert(transfertDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(transfertCree);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{transfertId}/validation")
    public ResponseEntity<TransfertFondResponseDto> validerTransfert(
            @PathVariable int transfertId,
            @RequestParam int parrainId,
            @RequestParam(required = false) String commentaire,
            @RequestParam boolean approuve) {
        try {
            TransfertFondResponseDto transfertValide = transfertFondService.validerTransfert(
                    transfertId, parrainId, commentaire, approuve);
            return ResponseEntity.ok(transfertValide);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{transfertId}")
    public ResponseEntity<TransfertFondResponseDto> getTransfertById(@PathVariable int transfertId) {
        try {
            TransfertFondResponseDto transfert = transfertFondService.getTransfertById(transfertId);
            return ResponseEntity.ok(transfert);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/organisation/{organisationId}")
    public ResponseEntity<Page<TransfertFondResponseDto>> getTransfertsParOrganisation(
            @PathVariable int organisationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<TransfertFondResponseDto> transferts = transfertFondService.getTransfertsParOrganisation(organisationId, page, size);
        return ResponseEntity.ok(transferts);
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<Page<TransfertFondResponseDto>> getTransfertsParStatut(
            @PathVariable StatutTransfert statut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<TransfertFondResponseDto> transferts = transfertFondService.getTransfertsParStatut(statut, page, size);
        return ResponseEntity.ok(transferts);
    }

    @GetMapping("/parrain/{parrainId}")
    public ResponseEntity<Page<TransfertFondResponseDto>> getTransfertsParParrain(
            @PathVariable int parrainId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<TransfertFondResponseDto> transferts = transfertFondService.getTransfertsParParrain(parrainId, page, size);
        return ResponseEntity.ok(transferts);
    }

    @GetMapping("/en-attente")
    public ResponseEntity<List<TransfertFondResponseDto>> getTransfertsEnAttente() {
        List<TransfertFondResponseDto> transfertsEnAttente = transfertFondService.getTransfertsEnAttente();
        return ResponseEntity.ok(transfertsEnAttente);
    }
}
