package com.groupe2_ionic.eduka.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO pour les requêtes de mot de passe oublié
 */
@Data
@Schema(description = "Requête de réinitialisation de mot de passe")
public class ForgotPasswordRequestDto {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Schema(description = "Adresse email de l'utilisateur", example = "user@example.com")
    private String email;
}
