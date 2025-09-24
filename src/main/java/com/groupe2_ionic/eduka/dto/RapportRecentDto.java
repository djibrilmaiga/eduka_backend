package com.groupe2_ionic.eduka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class RapportRecentDto {
    private int id;
    private String titre;
    private String typeRapport;
    private String periode;
    private String contenu;
    private LocalDate dateCreation;
    private String organisationNom;
    private int nombreDocuments;
    private boolean enRetard;
    private int joursDepuisCreation;
}
