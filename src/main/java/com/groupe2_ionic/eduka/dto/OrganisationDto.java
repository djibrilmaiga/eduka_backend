package com.groupe2_ionic.eduka.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrganisationDto {
    
    @NotBlank(message = "Le nom de l'organisation est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;
    
    @Email(message = "Format d'email invalide")
    private String email;
    
    @Size(min = 8, max = 15, message = "Le téléphone doit contenir entre 8 et 15 caractères")
    private String telephone;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;
    
    @NotBlank(message = "Le nom du représentant est obligatoire")
    private String nomRepresentant;
    
    @NotBlank(message = "Le prénom du représentant est obligatoire")
    private String prenomRepresentant;
    
    @NotBlank(message = "La fonction du représentant est obligatoire")
    private String fonctionRepresentant;
    
    private String ville;
    private String pays;
}
