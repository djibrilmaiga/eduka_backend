package com.groupe2_ionic.eduka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class ParrainResponseDto {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String ville;
    private String pays;
    private String photoProfil;
    private Boolean anonyme;
    private Boolean actif;
    private LocalDate dateInscription;
    private int nombreParrainages;
    private int nombrePaiements;
}
