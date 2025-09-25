package com.groupe2_ionic.eduka.dto.auth;

import com.groupe2_ionic.eduka.models.enums.RoleUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les réponses d'authentification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse d'authentification")
public class AuthResponseDto {

    @Schema(description = "Token d'accès JWT")
    private String accessToken;

    @Schema(description = "Token de rafraîchissement")
    private String refreshToken;

    @Schema(description = "Type de token", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Durée de validité du token d'accès en secondes")
    private long expiresIn;

    @Schema(description = "Informations utilisateur")
    private UserInfoDto user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Informations utilisateur")
    public static class UserInfoDto {
        @Schema(description = "ID utilisateur")
        private Integer id;

        @Schema(description = "Email")
        private String email;

        @Schema(description = "Téléphone")
        private String telephone;

        @Schema(description = "Rôle utilisateur")
        private RoleUser role;

        @Schema(description = "Statut actif")
        private Boolean actif;
    }
}
