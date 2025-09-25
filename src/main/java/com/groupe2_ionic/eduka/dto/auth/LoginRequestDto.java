package com.groupe2_ionic.eduka.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO pour les requêtes de connexion
 */
@Data
@Schema(description = "Requête de connexion utilisateur")
public class LoginRequestDto {

    @NotBlank(message = "L'identifiant est obligatoire")
    @Schema(description = "Email ou numéro de téléphone", example = "user@example.com")
    private String identifier;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Schema(description = "Mot de passe", example = "password123")
    private String password;
}
