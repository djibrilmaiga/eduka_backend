package com.groupe2_ionic.eduka.dto;

import com.groupe2_ionic.eduka.models.enums.MotifTransfert;
import com.groupe2_ionic.eduka.models.enums.StatutTransfert;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class TransfertFondResponseDto {
    private int id;
    private MotifTransfert motif;
    private String description;
    private BigDecimal montant;
    private StatutTransfert statut;
    private LocalDate dateDemande;
    private LocalDate dateTraitement;
    private String commentaireValidation;
    private String enfantSourceNom;
    private String enfantCibleNom;
    private String organisationNom;
    private String parrainNom;
}
