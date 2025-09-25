package com.groupe2_ionic.eduka.dto.auth;

import com.groupe2_ionic.eduka.models.enums.RoleUser;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO pour les requêtes d'inscription
 */
@Data
@Schema(description = "Requête d'inscription utilisateur")
public class RegisterRequestDto {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Schema(description = "Adresse email", example = "user@example.com")
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Schema(description = "Numéro de téléphone", example = "+221701234567")
    private String telephone;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Schema(description = "Mot de passe (minimum 8 caractères)", example = "password123")
    private String password;

    @NotNull(message = "Le rôle est obligatoire")
    @Schema(description = "Rôle utilisateur")
    private RoleUser role;
}
