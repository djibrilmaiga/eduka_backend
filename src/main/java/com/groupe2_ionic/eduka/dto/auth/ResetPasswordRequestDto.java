package com.groupe2_ionic.eduka.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO pour les requêtes de réinitialisation de mot de passe
 */
@Data
@Schema(description = "Réinitialisation de mot de passe avec token")
public class ResetPasswordRequestDto {

    @NotBlank(message = "Le token est obligatoire")
    @Schema(description = "Token de réinitialisation reçu par email")
    private String token;

    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Schema(description = "Nouveau mot de passe (minimum 8 caractères)", example = "newPassword123")
    private String newPassword;

    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    @Schema(description = "Confirmation du nouveau mot de passe", example = "newPassword123")
    private String confirmPassword;
}
