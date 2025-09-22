package com.groupe2_ionic.eduka.dto;

import com.groupe2_ionic.eduka.models.enums.Genre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class EnfantResponseDto {
    private int id;
    private String nom;
    private String prenom;
    private Genre genre;
    private LocalDate dateNaissance;
    private int age;
    private String niveauScolaire;
    private String histoire;
    private String photoProfil;
    private Boolean statutParrainage;
    private BigDecimal solde;
    private Boolean consentementPedagogique;
    private String organisationNom;
    private String tuteurNom;
    private String ecoleNom;
    private int nombreParrainages;
    private int nombreRapports;
}
