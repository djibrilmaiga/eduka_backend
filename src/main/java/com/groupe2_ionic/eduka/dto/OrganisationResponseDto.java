package com.groupe2_ionic.eduka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrganisationResponseDto {
    private int id;
    private String nom;
    private String email;
    private String telephone;
    private String nomRepresentant;
    private String prenomRepresentant;
    private String fonctionRepresentant;
    private String ville;
    private String pays;
    private Boolean actif;
    private LocalDate dateInscription;
    private String validateurNom;
    private int nombreEnfants;
    private int nombrePaiements;
}
