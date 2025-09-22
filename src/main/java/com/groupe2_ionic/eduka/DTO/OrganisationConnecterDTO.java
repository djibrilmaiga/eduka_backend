package com.groupe2_ionic.eduka.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrganisationConnecterDTO {
    @Email(message = "L'email doit être valide.")
    @NotBlank(message = "L'email est requis.")
    private String email;

    @NotBlank(message = "Le mot de passe est requis.")
    private String password;
}