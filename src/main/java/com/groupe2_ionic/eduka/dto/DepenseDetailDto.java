package com.groupe2_ionic.eduka.dto;

import com.groupe2_ionic.eduka.models.enums.TypeDepense;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour les détails des dépenses enregistrées
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DepenseDetailDto {

    private int id;
    private String description;
    private BigDecimal montant;
    private LocalDate dateDepense;
    private String categorieDepense;
    private String organisationNom;
    private String justificatif; // URL du document
    private String beneficiaire;
    private boolean validee;

}
