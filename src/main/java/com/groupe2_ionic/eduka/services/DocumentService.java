package com.groupe2_ionic.eduka.services;

import com.groupe2_ionic.eduka.dto.DocumentResponseDto;
import com.groupe2_ionic.eduka.dto.DocumentUploadDto;
import com.groupe2_ionic.eduka.models.Document;
import com.groupe2_ionic.eduka.models.Organisation;
import com.groupe2_ionic.eduka.models.Rapport;
import com.groupe2_ionic.eduka.models.enums.TypeDocument;
import com.groupe2_ionic.eduka.repository.DocumentRepository;
import com.groupe2_ionic.eduka.repository.OrganisationRepository;
import com.groupe2_ionic.eduka.repository.RapportRepository;
import com.groupe2_ionic.eduka.services.utilitaires.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final RapportRepository rapportRepository;
    private final OrganisationRepository organisationRepository;
    private final FileStorageService fileStorageService;

    /**
     * Upload d'un document avec association automatique selon le type
     */
    @Transactional
    public DocumentResponseDto uploadDocument(MultipartFile file, DocumentUploadDto uploadDto) throws IOException {
        // Validation des données
        validateUploadRequest(file, uploadDto);

        // Déterminer le sous-dossier selon le type de document
        String subFolder = determineSubFolder(uploadDto.getType());

        // Stocker le fichier
        FileStorageService.StoredFile storedFile = fileStorageService.store(file, subFolder);

        // Créer l'entité Document
        Document document = new Document();
        document.setTypeDocument(uploadDto.getType());
        document.setType(uploadDto.getType().name()); // Maintenir la compatibilité
        document.setUrl(storedFile.url());
        document.setDate(LocalDate.now());

        // Associer au rapport si spécifié
        if (uploadDto.getRapportId() != null) {
            Optional<Rapport> rapport = rapportRepository.findById(uploadDto.getRapportId());
            if (rapport.isPresent()) {
                document.setRapport(rapport.get());
            } else {
                throw new IllegalArgumentException("Rapport non trouvé avec l'ID: " + uploadDto.getRapportId());
            }
        }

        // Associer à l'organisation si spécifié
        if (uploadDto.getOrganisationId() != null) {
            Optional<Organisation> organisation = organisationRepository.findById(uploadDto.getOrganisationId());
            if (organisation.isPresent()) {
                document.setOrganisation(organisation.get());
            } else {
                throw new IllegalArgumentException("Organisation non trouvée avec l'ID: " + uploadDto.getOrganisationId());
            }
        }

        // Sauvegarder en base
        Document savedDocument = documentRepository.save(document);

        return convertToResponseDto(savedDocument, storedFile);
    }

    /**
     * Récupération de tous les documents avec pagination
     */
    public Page<DocumentResponseDto> getAllDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable)
                .map(this::convertToResponseDto);
    }

    /**
     * Récupération des documents par organisation
     */
    public List<DocumentResponseDto> getDocumentsByOrganisation(int organisationId) {
        return documentRepository.findByOrganisationIdOrderByDateDesc(organisationId)
                .stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    /**
     * Récupération des documents par rapport
     */
    public List<DocumentResponseDto> getDocumentsByRapport(int rapportId) {
        return documentRepository.findByRapportIdOrderByDateDesc(rapportId)
                .stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    /**
     * Récupération des documents par type
     */
    public List<DocumentResponseDto> getDocumentsByType(TypeDocument type) {
        return documentRepository.findByTypeOrderByDateDesc(type.name())
                .stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    /**
     * Récupération d'un document par ID
     */
    public Optional<DocumentResponseDto> getDocumentById(int id) {
        return documentRepository.findById(id)
                .map(this::convertToResponseDto);
    }

    /**
     * Suppression d'un document
     */
    @Transactional
    public boolean deleteDocument(int id) {
        Optional<Document> documentOpt = documentRepository.findById(id);
        if (documentOpt.isPresent()) {
            Document document = documentOpt.get();
            try {
                // Supprimer le fichier physique
                fileStorageService.delete(document.getUrl());
                // Supprimer l'enregistrement en base
                documentRepository.delete(document);
                return true;
            } catch (IOException e) {
                throw new RuntimeException("Erreur lors de la suppression du fichier: " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Statistiques des documents
     */
    public DocumentStatisticsDto getDocumentStatistics(int organisationId) {
        long totalDocuments = documentRepository.countByOrganisationId(organisationId);
        long documentsJustificatifs = documentRepository.countByOrganisationIdAndTypeIn(
                organisationId,
                List.of("DOCUMENT_JUSTIFICATIF_INSCRIPTION", "CERTIFICAT_ORGANISATION", "AUTORISATION_LEGALE", "STATUTS_ORGANISATION")
        );
        long documentsRapports = documentRepository.countByOrganisationIdAndTypeIn(
                organisationId,
                List.of("PHOTO_ACTIVITE", "BULLETIN_SCOLAIRE", "LISTE_PRESENCE")
        );

        return new DocumentStatisticsDto(totalDocuments, documentsJustificatifs, documentsRapports);
    }

    // Méthodes utilitaires privées

    private void validateUploadRequest(MultipartFile file, DocumentUploadDto uploadDto) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est requis");
        }

        if (uploadDto.getType() == null) {
            throw new IllegalArgumentException("Le type de document est requis");
        }

        // Validation selon le type de document
        if (isDocumentJustificatif(uploadDto.getType()) && uploadDto.getOrganisationId() == null) {
            throw new IllegalArgumentException("L'ID de l'organisation est requis pour les documents justificatifs");
        }

        if (isDocumentRapport(uploadDto.getType()) && uploadDto.getRapportId() == null) {
            throw new IllegalArgumentException("L'ID du rapport est requis pour les documents de rapport");
        }
    }

    private String determineSubFolder(TypeDocument type) {
        return switch (type) {
            case DOCUMENT_JUSTIFICATIF_INSCRIPTION, CERTIFICAT_ORGANISATION, AUTORISATION_LEGALE, STATUTS_ORGANISATION ->
                    "documents-justificatifs";
            case PHOTO_ACTIVITE -> "photos-activites";
            case BULLETIN_SCOLAIRE -> "bulletins";
            case LISTE_PRESENCE -> "listes-presence";
            case RAPPORT_MENSUEL, RAPPORT_TRIMESTRIEL -> "rapports";
            default -> "autres";
        };
    }

    private boolean isDocumentJustificatif(TypeDocument type) {
        return type == TypeDocument.DOCUMENT_JUSTIFICATIF_INSCRIPTION ||
                type == TypeDocument.CERTIFICAT_ORGANISATION ||
                type == TypeDocument.AUTORISATION_LEGALE ||
                type == TypeDocument.STATUTS_ORGANISATION;
    }

    private boolean isDocumentRapport(TypeDocument type) {
        return type == TypeDocument.PHOTO_ACTIVITE ||
                type == TypeDocument.BULLETIN_SCOLAIRE ||
                type == TypeDocument.LISTE_PRESENCE;
    }

    private DocumentResponseDto convertToResponseDto(Document document) {
        return convertToResponseDto(document, null);
    }

    private DocumentResponseDto convertToResponseDto(Document document, FileStorageService.StoredFile storedFile) {
        DocumentResponseDto dto = new DocumentResponseDto();
        dto.setId(document.getId());
        dto.setType(document.getTypeDocument() != null ? document.getTypeDocument() : TypeDocument.valueOf(document.getType()));
        dto.setUrl(document.getUrl());
        dto.setDate(document.getDate());

        if (storedFile != null) {
            dto.setFileName(storedFile.fileName());
            dto.setFileSize(storedFile.size());
            dto.setContentType(storedFile.contentType());
        }

        if (document.getRapport() != null) {
            dto.setRapportId(document.getRapport().getId());
            dto.setRapportTitre(document.getRapport().getTitre());
        }

        if (document.getOrganisation() != null) {
            dto.setOrganisationId(document.getOrganisation().getId());
            dto.setOrganisationNom(document.getOrganisation().getNom());
        }

        return dto;
    }

    // DTO pour les statistiques
    public record DocumentStatisticsDto(
            long totalDocuments,
            long documentsJustificatifs,
            long documentsRapports
    ) {}
}
