package com.groupe2_ionic.eduka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class TuteurResponseDto {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String profession;
    private String lienParente;
    private Boolean actif;
    private LocalDate dateInscription;
    private String enfantNom;
}
