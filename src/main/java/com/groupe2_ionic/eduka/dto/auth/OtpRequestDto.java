package com.groupe2_ionic.eduka.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO pour les requêtes de génération d'OTP
 */
@Data
@Schema(description = "Requête de génération d'OTP pour tuteur")
public class OtpRequestDto {

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Schema(description = "Numéro de téléphone du tuteur", example = "+22377665544")
    private String telephone;
}
