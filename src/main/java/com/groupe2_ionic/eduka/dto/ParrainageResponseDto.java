package com.groupe2_ionic.eduka.dto;

import com.groupe2_ionic.eduka.models.enums.StatutParrainage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class ParrainageResponseDto {
    private int id;
    private StatutParrainage statut;
    private LocalDate dateDebut;
    private BigDecimal montantTotal;
    private LocalDate dateFin;
    private String motifFin;
    private String parrainNom;
    private String parrainPrenom;
    private String enfantNom;
    private String enfantPrenom;
    private int nombrePaiements;
    private BigDecimal montantPaye;
    private BigDecimal montantRestant;
}
