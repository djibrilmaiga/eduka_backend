package com.groupe2_ionic.eduka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class RapportResponseDto {
    private int id;
    private String titre;
    private String typeRapport;
    private String periode;
    private String contenu;
    private LocalDate date;
    private String enfantNom;
    private String enfantPrenom;
    private String organisationNom;
    private int nombreDocuments;
}
