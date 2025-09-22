package com.groupe2_ionic.eduka.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class DocumentDto {
    
    @NotBlank(message = "Le type de document est obligatoire")
    private String type;
    
    @NotBlank(message = "L'URL du document est obligatoire")
    private String url;
    
    private Integer rapportId;
    private Integer organisationId;
}
