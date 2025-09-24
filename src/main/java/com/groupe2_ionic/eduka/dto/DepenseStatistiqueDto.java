package com.groupe2_ionic.eduka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter @NoArgsConstructor @AllArgsConstructor
public class DepenseStatistiqueDto {
    private String typeDepense;
    private BigDecimal totalMontant;
    private long nombreDepenses;
}
