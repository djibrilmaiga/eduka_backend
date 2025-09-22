package com.groupe2_ionic.eduka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class RapportDto {
    
    @NotBlank(message = "Le titre est obligatoire")
    private String titre;
    
    @NotBlank(message = "Le type de rapport est obligatoire")
    private String typeRapport;
    
    @NotBlank(message = "La période est obligatoire")
    private String periode;
    
    private String contenu;
    
    @NotNull(message = "L'ID de l'enfant est obligatoire")
    private Integer enfantId;
    
    @NotNull(message = "L'ID de l'organisation est obligatoire")
    private Integer organisationId;
}
