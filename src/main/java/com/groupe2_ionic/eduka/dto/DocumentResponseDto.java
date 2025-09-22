package com.groupe2_ionic.eduka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class DocumentResponseDto {
    private int id;
    private String type;
    private String url;
    private LocalDate date;
    private String rapportTitre;
    private String organisationNom;
}
