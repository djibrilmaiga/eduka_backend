package com.groupe2_ionic.eduka.dto;

import com.groupe2_ionic.eduka.models.enums.MethodePaiement;
import com.groupe2_ionic.eduka.models.enums.StatutPaiement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class PaiementResponseDto {
    private int id;
    private MethodePaiement methode;
    private BigDecimal montant;
    private StatutPaiement statut;
    private LocalDate datePaiement;
    private String parrainNom;
    private String enfantNom;
    private String organisationNom;
    private String referenceTransaction;
}
