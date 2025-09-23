package com.groupe2_ionic.eduka.dto;

import com.groupe2_ionic.eduka.models.enums.TypeDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DocumentUploadDto {
    private TypeDocument type;
    private String description;
    private Integer rapportId; // Optionnel, pour les documents de rapport
    private Integer organisationId; // Optionnel, pour les documents justificatifs
}
