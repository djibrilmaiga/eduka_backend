package com.groupe2_ionic.eduka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class BesoinResponseDto {
    private int id;
    private String type;
    private BigDecimal montant;
    private String enfantNom;
    private String enfantPrenom;
}
