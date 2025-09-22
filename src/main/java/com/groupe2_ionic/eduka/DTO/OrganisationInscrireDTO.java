package com.groupe2_ionic.eduka.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrganisationInscrireDTO {
    @NotBlank(message = "Le numéro de téléphone est requis.")
    private String telephone;

    @Email(message = "L'email doit être valide.")
    @NotBlank(message = "L'email est requis.")
    private String email;

    @NotBlank(message = "Le mot de passe est requis.")
    private String password;

    @NotBlank(message = "Le nom de l'organisation est requis.")
    private String nom;

    @NotBlank(message = "Le nom du représentant est requis.")
    private String nomRepresentant;

    @NotBlank(message = "Le prénom du représentant est requis.")
    private String prenomRepresentant;

    @NotBlank(message = "La fonction du représentant est requise.")
    private String fonctionRepresentant;

    private String ville;
    private String pays;
}