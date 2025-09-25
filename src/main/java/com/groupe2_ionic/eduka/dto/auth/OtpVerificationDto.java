package com.groupe2_ionic.eduka.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO pour la vérification d'OTP
 */
@Data
@Schema(description = "Vérification du code OTP")
public class OtpVerificationDto {

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Schema(description = "Numéro de téléphone du tuteur", example = "+22377665544")
    private String telephone;

    @NotBlank(message = "Le code OTP est obligatoire")
    @Pattern(regexp = "\\d{6}", message = "Le code OTP doit contenir exactement 6 chiffres")
    @Schema(description = "Code OTP à 6 chiffres", example = "123456")
    private String otpCode;
}
