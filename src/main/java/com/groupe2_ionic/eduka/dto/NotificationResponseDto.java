package com.groupe2_ionic.eduka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class NotificationResponseDto {
    private int id;
    private String sujet;
    private String message;
    private LocalDate date;
    private boolean lu;
    private String destinataireNom;
    private String destinatairePrenom;
    private String destinataireEmail;
}
