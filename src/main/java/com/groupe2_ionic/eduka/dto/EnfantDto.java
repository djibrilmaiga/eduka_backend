package com.groupe2_ionic.eduka.dto;

import com.groupe2_ionic.eduka.models.enums.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class EnfantDto {
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String nom;
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String prenom;
    
    @NotNull(message = "Le genre est obligatoire")
    private Genre genre;
    
    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;
    
    @NotBlank(message = "Le niveau scolaire est obligatoire")
    private String niveauScolaire;
    
    private String histoire;
    private String photoProfil;
    
    @NotNull(message = "L'ID de l'organisation est obligatoire")
    private Integer organisationId;

    // Infos du tuteur
    private Integer tuteurId;
    private String nomTuteur;
    private String prenomTuteur;
    private String telephoneTuteur;

    // Infos de l'école
    private Integer ecoleId;
    private String nomEcole;
    private String villeEcole;
    private String paysEcole;
    private Boolean consentementPedagogique = false;
}
