package com.groupe2_ionic.eduka.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO pour les requêtes de rafraîchissement de token
 */
@Data
@Schema(description = "Requête de rafraîchissement de token")
public class RefreshTokenRequestDto {

    @NotBlank(message = "Le token de rafraîchissement est obligatoire")
    @Schema(description = "Token de rafraîchissement")
    private String refreshToken;
}
