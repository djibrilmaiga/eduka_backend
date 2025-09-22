package com.groupe2_ionic.eduka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class DepenseResponseDto {
    private int id;
    private String typeDepense;
    private String justificatif;
    private BigDecimal montant;
    private LocalDate dateEnregistrement;
    private String organisationNom;
    private String enfantNom;
    private String enfantPrenom;
}
