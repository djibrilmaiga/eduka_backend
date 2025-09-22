package com.groupe2_ionic.eduka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NotificationDto {
    @NotBlank
    private String sujet;
    @NotBlank
    private String message;
    @NotNull
    private Integer destinataireId; // ID de l'utilisateur à notifier
}
