package com.groupe2_ionic.eduka.dto;

import com.groupe2_ionic.eduka.models.enums.TypeDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class DocumentResponseDto {
    private int id;
    private TypeDocument type;
    private String url;
    private LocalDate date;
    private String rapportTitre;
    private String organisationNom;

    private Integer rapportId;
    private Integer organisationId;
    private String fileName;
    private Long fileSize;
    private String contentType;
}
