package com.groupe2_ionic.eduka.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les réponses d'authentification des tuteurs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse d'authentification tuteur")
public class TuteurAuthResponseDto {

    @Schema(description = "Token d'accès JWT")
    private String accessToken;

    @Schema(description = "Type de token", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Durée de validité du token d'accès en secondes")
    private long expiresIn;

    @Schema(description = "Informations tuteur")
    private TuteurInfoDto tuteur;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Informations tuteur")
    public static class TuteurInfoDto {
        @Schema(description = "ID tuteur")
        private Integer id;

        @Schema(description = "Nom")
        private String nom;

        @Schema(description = "Prénom")
        private String prenom;

        @Schema(description = "Téléphone")
        private String telephone;
    }
}
