package com.groupe2_ionic.eduka.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class TuteurDto {
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String nom;
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String prenom;
    
    @Email(message = "Format d'email invalide")
    private String email;
    
    @Size(min = 8, max = 15, message = "Le téléphone doit contenir entre 8 et 15 caractères")
    private String telephone;
    
    private String password;
    private String adresse;
    private String profession;
    private String lienParente;
}
