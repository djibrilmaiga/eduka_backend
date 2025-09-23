package com.groupe2_ionic.eduka.controllers;

import com.groupe2_ionic.eduka.dto.DocumentResponseDto;
import com.groupe2_ionic.eduka.dto.DocumentUploadDto;
import com.groupe2_ionic.eduka.models.enums.TypeDocument;
import com.groupe2_ionic.eduka.services.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Gestion des documents (justificatifs et rapports)")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload d'un document", description = "Téléverse un document avec ses métadonnées")
    public ResponseEntity<?> uploadDocument(
            @Parameter(description = "Fichier à téléverser") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Type de document") @RequestParam("type") TypeDocument type,
            @Parameter(description = "Description du document") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "ID du rapport (pour documents de rapport)") @RequestParam(value = "rapportId", required = false) Integer rapportId,
            @Parameter(description = "ID de l'organisation (pour documents justificatifs)") @RequestParam(value = "organisationId", required = false) Integer organisationId
    ) {
        try {
            DocumentUploadDto uploadDto = new DocumentUploadDto(type, description, rapportId, organisationId);
            DocumentResponseDto result = documentService.uploadDocument(file, uploadDto);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'upload: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "Liste tous les documents", description = "Récupère tous les documents avec pagination")
    public ResponseEntity<Page<DocumentResponseDto>> getAllDocuments(
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de page") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DocumentResponseDto> documents = documentService.getAllDocuments(pageable);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un document par ID")
    public ResponseEntity<DocumentResponseDto> getDocumentById(@PathVariable int id) {
        Optional<DocumentResponseDto> document = documentService.getDocumentById(id);
        return document.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/organisation/{organisationId}")
    @Operation(summary = "Documents d'une organisation", description = "Récupère tous les documents d'une organisation")
    public ResponseEntity<List<DocumentResponseDto>> getDocumentsByOrganisation(@PathVariable int organisationId) {
        List<DocumentResponseDto> documents = documentService.getDocumentsByOrganisation(organisationId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/rapport/{rapportId}")
    @Operation(summary = "Documents d'un rapport", description = "Récupère tous les documents associés à un rapport")
    public ResponseEntity<List<DocumentResponseDto>> getDocumentsByRapport(@PathVariable int rapportId) {
        List<DocumentResponseDto> documents = documentService.getDocumentsByRapport(rapportId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Documents par type", description = "Récupère tous les documents d'un type donné")
    public ResponseEntity<List<DocumentResponseDto>> getDocumentsByType(@PathVariable TypeDocument type) {
        List<DocumentResponseDto> documents = documentService.getDocumentsByType(type);
        return ResponseEntity.ok(documents);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un document", description = "Supprime un document et son fichier associé")
    public ResponseEntity<?> deleteDocument(@PathVariable int id) {
        try {
            boolean deleted = documentService.deleteDocument(id);
            if (deleted) {
                return ResponseEntity.ok("Document supprimé avec succès");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    @GetMapping("/organisation/{organisationId}/statistics")
    @Operation(summary = "Statistiques des documents", description = "Récupère les statistiques des documents d'une organisation")
    public ResponseEntity<DocumentService.DocumentStatisticsDto> getDocumentStatistics(@PathVariable int organisationId) {
        DocumentService.DocumentStatisticsDto stats = documentService.getDocumentStatistics(organisationId);
        return ResponseEntity.ok(stats);
    }
}
