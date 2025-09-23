package com.groupe2_ionic.eduka.dto;

import com.groupe2_ionic.eduka.models.Utilisateur;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class NotificationDto {
    @NotBlank
    private String sujet;

    @NotBlank
    private String message;

    @NotNull
    private Integer destinataireId; // ID de l'utilisateur à notifier

    private boolean envoyerEmail = true;
    private boolean envoyerSms = false;

    private Utilisateur utilisateur;
}
